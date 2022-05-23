package riscv.core.fivestage
import chisel3.{when, _}
import chisel3.experimental.ChiselEnum
import chisel3.util._
import riscv.Parameters
import riscv.core.{BusBundle, fivestage}


object MMUStates extends ChiselEnum{
  val idle,level1,level0,setADbit,gotPhyicalAddress,checkpte1,checkpte0 = Value
}

class MMU extends Module{
  val io = IO(new Bundle() {
    val instructions = Input(UInt(Parameters.InstructionWidth))
    val instructions_address = Input(UInt(Parameters.AddrWidth))

    //from satp
    val ppn_from_satp = Input(UInt(20.W))

    val virtual_address = Input(UInt(Parameters.AddrWidth))
    val mmu_occupied_by_mem = Input(Bool())
    val restart = Input(Bool())
    val restart_done = Output(Bool())

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

  val pa = RegInit(0.U(Parameters.AddrWidth))
  val va = io.virtual_address
  val vpn1 = va(31,22)

  val vpn0 = va(21,12)
  val pageoffset = va(11,0)

  val pte = Reg(UInt(Parameters.PTEWidth))

  def mmu_back_to_idle(): Unit ={
    io.pa_valid := false.B
    state := MMUStates.idle
  }

  def raise_page_fault(): Unit ={
    io.ecause := Mux(
      io.mmu_occupied_by_mem,
      MuxLookup(
        io.instructions,
        10.U,
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
      io.mmu_occupied_by_mem,
      io.instructions_address,  //mem_access
      va,   //IF
    )
    when(io.page_fault_responed){
      io.page_fault_signals := false.B
      mmu_back_to_idle()
    }
  }

  io.pa_valid := false.B
  io.bus.request := false.B
  io.bus.read := false.B
  io.bus.address := 0.U
  io.bus.write_data := 0.U
  io.bus.write_strobe := VecInit(Seq.fill(Parameters.WordSize)(false.B))
  io.bus.write := false.B
  io.page_fault_signals := false.B

  //MMU FSM
  //our physical address bits is 32
  when(io.bus.granted){
    when(state === MMUStates.idle){
      //read pte from mem when the bus is free and someone needs the translation
      //mem request take precedence of IF
      io.pa_valid := false.B
      io.bus.read := true.B
      io.restart_done := false.B
      io.bus.address := ((io.ppn_from_satp << Parameters.PageOffsetBits) + (vpn1 << Parameters.PTEBits)) //address of level 1 pte
      state := MMUStates.level1
    }.elsewhen (state === MMUStates.level1){ //don't support the huge page
      //already access the bus,wait for the pte
      io.bus.read := false.B
      when(io.bus.read_valid){
        pte := io.bus.read_data
        when(io.restart){
          io.restart_done := true.B
          state := MMUStates.idle
        }.otherwise{
          state := MMUStates.checkpte1
        }
      }
    }.elsewhen(state === MMUStates.checkpte1){
      //todo: hrpccs :no PMA or PMP check
      when(io.restart) {
        io.restart_done := true.B
        state := MMUStates.idle
      }.elsewhen(pte(0) === 0.U || (pte(2,1) === "b10".U) || (pte(9,8) =/= "b00".U)){
        //raise a page-fault exception corresponding to the original access type
        raise_page_fault()
      }.otherwise {
        io.bus.read := true.B
        io.bus.address := ((pte(29,10) << Parameters.PageOffsetBits) + (vpn0 << Parameters.PTEBits))//address of level 0 pte
        when(io.bus.granted) {
          state := MMUStates.level0
        }
      }
    }.elsewhen (state === MMUStates.level0){
      io.bus.read := false.B
      when(io.bus.read_valid){
        pte := io.bus.read_data
        when(io.restart){
          io.restart_done := true.B
          state := MMUStates.idle
        }.otherwise{
          state := MMUStates.checkpte0
        }
      }
    }.elsewhen(state === MMUStates.checkpte0){
      when(io.restart){
        io.restart_done := true.B
        state := MMUStates.idle
      }.elsewhen(pte(0) === 0.U || (pte(2,1) === "b10".U) || (pte(9,8) =/= "b00".U) || (pte(3,1) === "b000".U)) {
        //raise a page-fault exception corresponding to the original access type
        raise_page_fault()
      }.elsewhen(pte(1) === 1.U || pte(3) === 1.U) {
        //we found a leaf pte
        val instructionInvalid = io.mmu_occupied_by_mem === false.B && pte(3) === 0.U
        val storeInvalid = io.instructions(6, 0) === InstructionTypes.S && pte(2) === 0.U
        val loadInvalid = io.instructions(6, 0) === InstructionTypes.L && pte(1) === 0.U
        when(instructionInvalid || storeInvalid || loadInvalid) {
          //todo:hrpccs :when the privillege switch is done,please add the privillege check
          raise_page_fault()
        }.elsewhen(pte(6) === 0.U || (pte(7) === 0.U && io.instructions(6, 0) === InstructionTypes.S)) {
          //set the access bit and the dirty bit if the instruction is store type
          //as we currently support single core CPU,so we can ignore the concurrent pte change
          //todo:hrpccs :when someone want to have a multicore support \
          // please modify this part acording to riscv-privilege
          val setAbit = io.instructions(6, 0) === InstructionTypes.S
          io.bus.write_data := Cat(pte(31, 8), setAbit, 1.U(1.W), pte(5, 0))
          io.bus.write := true.B
          io.bus.address := ((pte(29,10) << Parameters.PageOffsetBits) + (vpn0 << Parameters.PTEBits))
          for (i <- 0 until Parameters.WordSize) {
            io.bus.write_strobe(i) := true.B
          }
          state := MMUStates.setADbit
        }.otherwise{
          state := MMUStates.gotPhyicalAddress
        }
      }
    }.elsewhen(state === MMUStates.setADbit){
      io.bus.write := false.B
      when(io.bus.write_valid) {
        when(io.restart){
          io.restart_done := true.B
          state := MMUStates.idle
        }.otherwise{
          state := MMUStates.gotPhyicalAddress
        }
      }
    }.elsewhen(state === MMUStates.gotPhyicalAddress){
      when(io.restart){
        io.restart_done := true.B
        state := MMUStates.idle
      }.otherwise{
        io.pa := Cat(pte(31,12),pageoffset)
        io.pa_valid := true.B
        mmu_back_to_idle()
      }
    }
  }


}
