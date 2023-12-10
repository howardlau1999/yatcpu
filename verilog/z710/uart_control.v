`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 2023/11/30 00:51:08
// Design Name: 
// Module Name: uart_control
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


module uart_control(
    input enable_uart,
    input tx_in,
    output tx_out
    );
    assign tx_out = (enable_uart) ? tx_in : 1'h1;

endmodule
