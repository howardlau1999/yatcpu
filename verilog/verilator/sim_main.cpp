#include <verilated.h>          // Defines common routines
#include <iostream>             // Need std::cout
#include <memory>
#include "VTop.h"               // From Verilating "top.v"
#include "verilated_vcd_c.h"


int main(int argc, char** argv) {
    Verilated::commandArgs(argc, argv);   // Remember args
    const std::unique_ptr<VerilatedContext> contextp{new VerilatedContext};
    unsigned long sim_time = 5000;
    if (argc == 2) {
        sscanf(argv[1], "%lu", &sim_time);
    }
    Verilated::traceEverOn(true);
    VerilatedVcdC* tfp = new VerilatedVcdC;
    VTop *top = new VTop;             // Create model
    top->trace(tfp, 99);
    tfp->open("obj_dir/sim.vcd");

    top->reset = 1;           // Set some inputs
    top->clock = 0;

    while (contextp->time() < sim_time && !contextp->gotFinish()) {
	contextp->timeInc(1);
        if (contextp->time() > 4) {
            top->reset = 0;   // Deassert reset
        }
	top->clock = !top->clock;
        top->eval();            // Evaluate model
	tfp->dump(contextp->time());
    }
    tfp->close();
    top->final();               // Done simulating
    delete top;
    return 0;
}
