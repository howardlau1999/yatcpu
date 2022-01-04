#include <verilated.h>          // Defines common routines
#include <iostream>             // Need std::cout
#include "VTop.h"               // From Verilating "top.v"

VTop *top;                      // Instantiation of model

vluint64_t main_time = 0;       // Current simulation time
// This is a 64-bit integer to reduce wrap over issues and
// allow modulus.  This is in units of the timeprecision
// used in Verilog (or from --timescale-override)

double sc_time_stamp() {        // Called by $time in Verilog
    return main_time;           // converts to double, to match
                                // what SystemC does
}

int main(int argc, char** argv) {
    Verilated::commandArgs(argc, argv);   // Remember args

    top = new VTop;             // Create model

    top->reset = 1;           // Set some inputs

    while (!Verilated::gotFinish()) {
        if (main_time > 10) {
            top->reset = 0;   // Deassert reset
        }
        if ((main_time % 10) == 1) {
            top->clock = 1;       // Toggle clock
        }
        if ((main_time % 10) == 6) {
            top->clock = 0;
        }
        top->eval();            // Evaluate model
        main_time++;            // Time passes...
        if (main_time > 1000) break;
    }

    top->final();               // Done simulating
    delete top;
    return 0;
}