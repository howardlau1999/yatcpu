package riscv.core.fivestage
import chisel3._
import chisel3.experimental.ChiselEnum
import chisel3.util._
import riscv.Parameters
import riscv.core.{BusBundle, fivestage}

import scala.collection.IndexedSeq

object MMUStates extends ChiselEnum{
  val idle,level1,level0,setADbit,gotPhyicalAddress = Value
}

object InstructionTypes {
  val L = "b0000011".U
  val I = "b0010011".U
  val S = "b0100011".U
  val RM = "b0110011".U
  val B = "b1100011".U
}

class MMU extends Module{
  val io = IO(new Bundle() {
    //from satp
    val mmu_enable = Input(Bool())

    val instructions = Input(UInt(Parameters.InstructionWidth))
    val instructions_address = Input(UInt(Parameters.AddrWidth))

    //from satp
    val ppn_from_satp = Input(UInt(10.W))

    val virtual_address = Input(UInt(Parameters.AddrWidth))
    val if_request = Input(Bool())
    val mem_request = Input(Bool())
    val pa_read_done = Input(Bool())


    val stall_flag_if = Output(Bool())
    val stall_flag_mem = Output(Bool())
    val busy = Output(Bool())
    val pa_valid = Output(Bool())
    val pa = Output(UInt(Parameters.AddrWidth))

    val page_fault_signals = Output(Bool())
    val va_cause_page_fault = Output(Bool())
    val ecause = Output(UInt(Parameters.DataWidth))
    val epc = Output(UInt(Parameters.AddrWidth))
    val page_fault_responed = Input(Bool())

    val bus = new BusBundle()
  })
  val opcode = io.instructions(6,0)

  val state = RegInit(MMUStates.idle)
  val busy = RegInit(false.B)

  val pa = RegInit(0.U(Parameters.AddrWidth))
  val ppn = RegInit(0.U( (Parameters.AddrBits - Parameters.PageOffsetBits).W ) )
  val va = io.virtual_address
  val vpn1 = va(31,22)

  val vpn0 = va(21,12)
  val pageoffset = va(11,0)

  io.stall_flag_if := false.B
  io.stall_flag_mem := false.B
  io.busy := false.B
  io.pa_valid := false.B


  io.bus.request := false.B
  io.bus.read := false.B
  io.bus.address := 0.U
  io.bus.write_data := 0.U
  io.bus.write_strobe := VecInit(Seq.fill(Parameters.WordSize)(false.B))
  io.bus.write := false.B

  val mmu_occupy_by_mem = RegInit(true.B) // true indicate that mem are using the mmu,otherwise the IF
  val pte1 = Reg(UInt(Parameters.PTEWidth))
  val pte0 = Reg(UInt(Parameters.PTEWidth))

  def mmu_back_to_idle(): Unit ={
    io.stall_flag_if := false.B
    io.stall_flag_mem := false.B
    io.busy := false.B
    io.pa_valid := false.B
    state := MMUStates.idle
  }

  def raise_page_fault(): Unit ={
    io.ecause := Mux(
      mmu_occupy_by_mem,
      MuxLookup(
        io.instructions,
        10.U
        IndexedSeq(
          InstructionTypes.S->15.U,
          InstructionTypes.L->13.U,
        )
      ),
      12.U // Instruction page fault
    )
    io.va_cause_page_fault := va //for mtval
    io.page_fault_signals := true.B
    io.epc := Mux(    //info stored before the exception handler, will start again from this pc
      mmu_occupy_by_mem,
      io.instructions_address,  //mem_access
      va,   //IF
    )
    when(io.page_fault_responed){
      io.page_fault_signals := false.B
      mmu_back_to_idle()
    }
  }

  //MMU FSM
  //we ignore the (31,30) bit of ppn, because our physical address bits is 32
  when(io.mmu_enable && (io.if_request || io.mem_request)){
    when(state === MMUStates.idle){
      //read pte from mem when the bus is free and someone needs the translation
      //mem request take precedence of IF
      when(io.mem_request){
        mmu_occupy_by_mem := true.B
      }.otherwise{
        mmu_occupy_by_mem := false.B
      }
      io.stall_flag_if := !mmu_occupy_by_mem
      io.stall_flag_mem := mmu_occupy_by_mem
      io.busy := true.B
      io.pa_valid := false.B

      io.bus.request := true.B
      io.bus.read := true.B
      io.bus.address := ((ppn << Parameters.PageOffsetBits) + (vpn1 << 2)) //address of level 1 pte
      ppn := io.ppn_from_satp

      when(io.bus.granted === true.B){
        state := MMUStates.level1
      }
    }.elsewhen (state === MMUStates.level1){ //don't support the huge page
      //already access the bus,wait for the pte
      io.stall_flag_if := !mmu_occupy_by_mem
      io.stall_flag_mem := mmu_occupy_by_mem
      io.bus.read := false.B
      io.bus.request := true.B
      when(io.bus.read_valid){
        val pte1 = io.bus.read_data
        //todo: hrpccs :no PMA or PMP check
        when(pte1(0) === 0.U || (pte1(2,1) === "b10".U) || (pte1(9,8) =/= "b00".U)){
          //raise a page-fault exception corresponding to the original access type
          raise_page_fault()
        }.elsewhen(io.mem_request && mmu_occupy_by_mem === false.B){ //memaccess take precedence over IF
          //go back to state idle,restart the translation
          state := MMUStates.idle
        }.otherwise {
          io.bus.request := true.B
          io.bus.read := true.B
          io.bus.address := ((pte1(29,10) << Parameters.PageOffsetBits) + (vpn0 << 2))//address of level 0 pte
          when(io.bus.granted) {
            state := MMUStates.level0
          }
        }
      }
    }.elsewhen (state === MMUStates.level0){
      io.stall_flag_if := !mmu_occupy_by_mem
      io.stall_flag_mem := mmu_occupy_by_mem
      io.bus.read := false.B
      io.bus.request := true.B
      when(io.bus.read_valid){
        val pte0 = io.bus.read_data
        when(pte0(0) === 0.U || (pte0(2,1) === "b10".U) || (pte0(9,8) =/= "b00".U) || (pte0(3,1) === "b000".U)) {
          //raise a page-fault exception corresponding to the original access type
          raise_page_fault()
        }.elsewhen(io.mem_request && mmu_occupy_by_mem === false.B){
          state := MMUStates.idle
        }.elsewhen(pte0(1) === 1.U || pte0(3) === 1.U) {
          //we found a leaf pte
          val instructionInvalid = mmu_occupy_by_mem === false.B && pte(3) === 0
          val storeInvalid = io.instructions(6, 0) === InstructionTypes.S && pte(2) === 0
          val loadInvalid = io.instructions(6, 0) === InstructionTypes.L && pte(1) === 0
          when(instructionInvalid || storeInvalid || loadInvalid) {
            //todo:hrpccs :when the privillege switch is done,please add the privillege check
            raise_page_fault()
          }.elsewhen(pte0(6) === 0.U || (pte0(7) === 0.U && io.instructions(6, 0) === InstructionTypes.S)) {
            //set the access bit and the dirty bit if the instruction is store type
            //as we currently support single core CPU,so we can ignore the concurrent pte change
            //todo:hrpccs :when someone want to have a multicore support \
            // please modify this part acording to riscv-privilege
            val setAbit = io.instructions(6, 0) === InstructionTypes.S
            io.bus.write_data := Cat(pte0(31, 8), setAbit, 1.U(1.W), pte0(5, 0))
            io.bus.request := true.B
            io.bus.write := true.B
            for (i <- 0 until Parameters.WordSize) {
              io.bus.write_strobe(i) := true.B
            }
            when(io.bus.granted) {
              state := MMUStates.setADbit
            }
          }.otherwise{
            state := MMUStates.gotPhyicalAddress
          }
        }
      }
    }.elsewhen(state === MMUStates.setADbit){
      io.bus.request := true.B
      io.bus.write := false.B
      when(io.bus.write_valid) {
        state := MMUStates.gotPhyicalAddress
      }
    }.elsewhen(state === MMUStates.gotPhyicalAddress){
      io.bus.request := false.B
      io.stall_flag_mem := false.B
      io.stall_flag_if := false.B
      io.pa := Cat(pte0(31,12),pageoffset)
      io.pa_valid := true.B
      when(io.pa_read_done){
        mmu_back_to_idle()
      }
    }
  }

}
