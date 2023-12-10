`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2023/12/01 15:46:54
// Design Name: 
// Module Name: top_test
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module top_test(

    );
    
    reg clock;
    reg reset;
    reg constant_zero = 1'b0;
    
    wire io_led, io_tx;
    
    localparam CLK_PERIOD = 20;
    initial begin
        clock = 1'b0;
        forever #( CLK_PERIOD / 2 ) clock = ~clock;
    end
    
    
    initial begin
        reset = 1;  // need a down edge to init all components
        #21  reset = 0;  // NOTE!!: must happen together with clock down edge!
    end
    
    Top mytop(clock, reset, io_led, io_tx, constant_zero);
    
endmodule
