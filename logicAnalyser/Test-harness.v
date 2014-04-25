module test;
  reg [3:0] io_in0;
  wire [3:0] io_out0;
  reg clk = 0;
  parameter clk_length = 120;
  always #clk_length clk = ~clk;
  /*** DUT instantiation ***/
    Test
      Test(
        .io_in0(io_in0),
        .io_out0(io_out0)
 );

  /*** resets &&  VCD / VPD dumps ***/
  initial begin
  #250;
  end

  /*** ROM & Mem initialization ***/
  integer i = 0;
  initial begin
  #50;
  end

endmodule
