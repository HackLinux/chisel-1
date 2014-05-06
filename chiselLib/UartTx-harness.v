module test;
  reg [1:0] io_config_dataType;
  reg [0:0] io_config_stopType;
  reg [1:0] io_config_parityType;
  reg [19:0] io_config_clockDivider;
  reg [0:0] io_data_valid;
  reg [8:0] io_data_bits;
  wire [0:0] io_data_ready;
  wire [0:0] io_tx;
  reg reset = 1;
  reg clk = 0;
  parameter clk_length = 10;
  always #(clk_length/2) clk = ~clk;
  /*** DUT instantiation ***/
    UartTx
      UartTx(
        .clk(clk),
        .reset(reset),
        .io_config_dataType(io_config_dataType),
        .io_config_stopType(io_config_stopType),
        .io_config_parityType(io_config_parityType),
        .io_config_clockDivider(io_config_clockDivider),
        .io_data_valid(io_data_valid),
        .io_data_bits(io_data_bits),
        .io_data_ready(io_data_ready),
        .io_tx(io_tx)
 );

  /*** resets &&  VCD / VPD dumps ***/
  initial begin
  reset = 1;
	io_data_valid = 0;
	io_config_dataType = 1;
	io_config_stopType = 1;
	io_config_parityType = 2;
	io_config_clockDivider = 8-1;
  #58;
  reset = 0;
	#clk_length;
	io_data_valid = 1;
	io_data_bits = 8'h5;
  end

  /*** ROM & Mem initialization ***/
  integer i = 0;
  initial begin
  #50;
  end

endmodule
