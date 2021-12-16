verilog:
	sbt run

test:
	sbt test

bitstream:
	pushd && cd vivado && vivado -mode batch -source generate_bitstream.tcl && popd

board:
	pushd && cd vivado && vivado -mode batch -source program_device.tcl && popd

.PHONY: verilog test bitstream board
