/******************************************************************************
*
* Copyright (C) 2009 - 2014 Xilinx, Inc.  All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* Use of the Software is limited solely to applications:
* (a) running on a Xilinx device, or
* (b) that interact with a Xilinx device through a bus or interconnect.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
* XILINX  BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF
* OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*
* Except as contained in this notice, the name of the Xilinx shall not be used
* in advertising or otherwise to promote the sale, use or other dealings in
* this Software without prior written authorization from Xilinx.
*
******************************************************************************/

/*
 * helloworld.c: simple test application
 *
 * This application configures UART 16550 to baud rate 9600.
 * PS7 UART (Zynq) is not initialized by this application, since
 * bootrom/bsp configures it to baud rate 115200
 *
 * ------------------------------------------------
 * | UART TYPE   BAUD RATE                        |
 * ------------------------------------------------
 *   uartns550   9600
 *   uartlite    Configurable only in HW design
 *   ps7_uart    115200 (configured by bootrom/bsp)
 */

#include <stdio.h>
#include "platform.h"
#include "xil_printf.h"
//#include "xuartps_hw.h"	// already include in xuartps.h
#include "xuartps.h"
#include "xparameters.h"

XUartPs uart_ps;	//  instance of the UART device




int main()
{
    init_platform();
    xil_printf("\nHello World\n\r");


    // test on UART ps
    int BUFFER_SIZE = 16;	// do not set too much to overflow FIFO
    char send_buffer[BUFFER_SIZE + 1];
    char recv_buffer[BUFFER_SIZE + 1];
    int status = 0;
    XUartPs_Config *config;


    for (int i = 0; i < BUFFER_SIZE; i++) send_buffer[i] = i % 256;

    config = XUartPs_LookupConfig(XPAR_XUARTPS_0_BASEADDR);	// CHANGE UART PORT HERE!!!
    if (config == NULL) {
    	printf("Error in config lookup!\n");
    	return XST_FAILURE;
    }


    status = XUartPs_CfgInitialize(&uart_ps, config, config->BaseAddress);
    if (status != XST_SUCCESS) {
    	printf("Error in cfg initialize!\n");
		return XST_FAILURE;
    }
	XUartPs_SetBaudRate(&uart_ps, 115200);

    /* Check hardware build. */
	status = XUartPs_SelfTest(&uart_ps);
	if (status != XST_SUCCESS) {
		printf("Error in self test!\n");
		return XST_FAILURE;
	}


	/* Configure UART mode */
	int mode = XUartPs_ReadReg(uart_ps.Config.BaseAddress, XUARTPS_MR_OFFSET);
	printf("original mode = 0x%x\n", mode);
	XUartPs_WriteReg(uart_ps.Config.BaseAddress, XUARTPS_MR_OFFSET,
			(XUARTPS_MR_STOPMODE_2_BIT | XUARTPS_MR_PARITY_NONE | XUARTPS_MR_CHARLEN_8_BIT));



    XUartPs_SetOperMode(&uart_ps, XUARTPS_OPER_MODE_NORMAL);




    int recv_cnt = 0;
    while (1) {
    	int recved = XUartPs_Recv(&uart_ps, (u8*)(recv_buffer + 0), BUFFER_SIZE);
    	recv_cnt += recved;
    	recv_buffer[recv_cnt] = 0;	// end
    	if (recv_cnt >= 1) {
//    		printf("%s", recv_buffer);
//    		printf("%i\n", recv_buffer[0]);

    		/* print string manually */
    		for (int j = 0; j < BUFFER_SIZE; j++) {
    			if (recv_buffer[j] != 0) {
    				printf("%c", recv_buffer[j]);
    			} else {
    				break;
    			}
    		}

    		recv_cnt = 0;
    	}
    }

    /* Restore to normal mode. */
	XUartPs_SetOperMode(&uart_ps, XUARTPS_OPER_MODE_NORMAL);


    cleanup_platform();
    return 0;
}
