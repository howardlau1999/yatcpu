#include <verilated.h>  // Defines common routines
#include <iostream>  // Need std::cout
#include "VTop.h"  // From Verilating "top.v"
#include "verilated_vcd_c.h"

int main(int argc, char** argv) {
  Verilated::commandArgs(argc, argv);  // Remember args
  vluint64_t main_time = 0;
  unsigned long sim_time = 5000;
  if (argc == 2) {
    sscanf(argv[1], "%lu", &sim_time);
  }

  Verilated::traceEverOn(true);
  VerilatedVcdC* tfp = new VerilatedVcdC;
  VTop* top = new VTop;  // Create model
  top->trace(tfp, 99);
  tfp->open("obj_dir/sim.vcd");

  top->reset = 1;  // Set some inputs
  top->clock = 0;

  while (main_time < sim_time && !Verilated::gotFinish()) {
    ++main_time;
    if (main_time > 4) {
      top->reset = 0;  // Deassert reset
    }
    top->clock = !top->clock;
    top->eval();  // Evaluate model
    tfp->dump(main_time);
  }

  tfp->close();
  top->final();  // Done simulating
  delete top;
  return 0;
}
