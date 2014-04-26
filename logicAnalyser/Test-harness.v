module test;
  reg [15:0] io_in0;
  reg [0:0] io_packetSlave_valid;
  reg [0:0] io_packetSlave_bits_last;
  reg [7:0] io_packetSlave_bits_fragment;
  wire [0:0] io_packetMaster_ready;
  wire [15:0] io_out0;
  wire [0:0] io_packetSlave_ready;
  wire [0:0] io_packetMaster_valid;
  wire [0:0] io_packetMaster_bits_last;
  wire [7:0] io_packetMaster_bits_fragment;
  reg reset = 1;
  reg clk = 0;
  parameter clk_length = 120;
  always #clk_length clk = ~clk;
  /*** DUT instantiation ***/
    Test
      Test(
        .clk(clk),
        .reset(reset),
        .io_in0(io_in0),
        .io_packetSlave_valid(io_packetSlave_valid),
        .io_packetSlave_bits_last(io_packetSlave_bits_last),
        .io_packetSlave_bits_fragment(io_packetSlave_bits_fragment),
        .io_packetMaster_ready(io_packetMaster_ready),
        .io_out0(io_out0),
        .io_packetSlave_ready(io_packetSlave_ready),
        .io_packetMaster_valid(io_packetMaster_valid),
        .io_packetMaster_bits_last(io_packetMaster_bits_last),
        .io_packetMaster_bits_fragment(io_packetMaster_bits_fragment)
 );

  /*** resets &&  VCD / VPD dumps ***/
  initial begin
  reset = 1;
  #250;
  reset = 0;
  end

  /*** ROM & Mem initialization ***/
  integer i = 0;
  initial begin
  #50;
    for (i = 0 ; i < 256 ; i = i + 1) begin
      Test.logicAnalyser.logger.mem[i] = 0;
    end
  end


	assign io_packetMaster_ready = 1;
endmodule
