module BlockAA(
    input [3:0] io_in0,
    output[3:0] io_out0,
    output[3:0] io_zLogicAnalyser0_0,
    output[3:0] io_zLogicAnalyser0_1,
    output[3:0] io_zLogicAnalyser0_2
);

  wire[3:0] s2;
  wire[3:0] s1;
  wire[3:0] s0;


  assign io_zLogicAnalyser0_2 = s2;
  assign s2 = s1;
  assign s1 = s0;
  assign s0 = io_in0;
  assign io_zLogicAnalyser0_1 = s1;
  assign io_zLogicAnalyser0_0 = s0;
  assign io_out0 = s2;
endmodule

module BlockA(
    input [3:0] io_in0,
    output[3:0] io_out0,
    output[3:0] io_zLogicAnalyser0_0,
    output[3:0] io_zLogicAnalyser0_1,
    output[3:0] io_zLogicAnalyser0_2
);

  wire[3:0] blockAA_io_zLogicAnalyser0_2;
  wire[3:0] blockAA_io_zLogicAnalyser0_1;
  wire[3:0] blockAA_io_zLogicAnalyser0_0;
  wire[3:0] blockAA_io_out0;


  assign io_zLogicAnalyser0_2 = blockAA_io_zLogicAnalyser0_2;
  assign io_zLogicAnalyser0_1 = blockAA_io_zLogicAnalyser0_1;
  assign io_zLogicAnalyser0_0 = blockAA_io_zLogicAnalyser0_0;
  assign io_out0 = blockAA_io_out0;
  BlockAA blockAA(
       .io_in0( io_in0 ),
       .io_out0( blockAA_io_out0 ),
       .io_zLogicAnalyser0_0( blockAA_io_zLogicAnalyser0_0 ),
       .io_zLogicAnalyser0_1( blockAA_io_zLogicAnalyser0_1 ),
       .io_zLogicAnalyser0_2( blockAA_io_zLogicAnalyser0_2 )
  );
endmodule

module LogicAnalyser(
    output io_uart_tx,
    //input  io_uart_rx
    input [3:0] io_zLogicAnalyser0_0,
    input [3:0] io_zLogicAnalyser0_1,
    input [3:0] io_zLogicAnalyser0_2
);

  wire T0;
  wire[11:0] probesBits;
  wire[3:0] T1;
  wire[7:0] T2;
  wire[3:0] T3;
  wire[3:0] T4;


  assign io_uart_tx = T0;
  assign T0 = probesBits != 12'h0;
  assign probesBits = {T2, T1};
  assign T1 = io_zLogicAnalyser0_2;
  assign T2 = {T4, T3};
  assign T3 = io_zLogicAnalyser0_1;
  assign T4 = io_zLogicAnalyser0_0;
endmodule

module Test(
    input [3:0] io_in0,
    output[3:0] io_out0
);

  wire[3:0] blockA_io_zLogicAnalyser0_2;
  wire[3:0] blockA_io_zLogicAnalyser0_1;
  wire[3:0] blockA_io_zLogicAnalyser0_0;
  wire[3:0] blockA_io_out0;


  assign io_out0 = blockA_io_out0;
  BlockA blockA(
       .io_in0( io_in0 ),
       .io_out0( blockA_io_out0 ),
       .io_zLogicAnalyser0_0( blockA_io_zLogicAnalyser0_0 ),
       .io_zLogicAnalyser0_1( blockA_io_zLogicAnalyser0_1 ),
       .io_zLogicAnalyser0_2( blockA_io_zLogicAnalyser0_2 )
  );
  LogicAnalyser logicAnalyser(
       //.io_uart_tx(  )
       //.io_uart_rx(  )
       .io_zLogicAnalyser0_0( blockA_io_zLogicAnalyser0_0 ),
       .io_zLogicAnalyser0_1( blockA_io_zLogicAnalyser0_1 ),
       .io_zLogicAnalyser0_2( blockA_io_zLogicAnalyser0_2 )
  );
endmodule

