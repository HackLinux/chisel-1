module BlockAA(
    input [15:0] io_in0,
    output[15:0] io_out0,
    output[15:0] io_zLogicAnalyser0_0,
    output[15:0] io_zLogicAnalyser0_1,
    output[15:0] io_zLogicAnalyser0_2
);

  wire[15:0] s2;
  wire[15:0] T0;
  wire[15:0] s1;
  wire[15:0] T1;
  wire[15:0] s0;


  assign io_zLogicAnalyser0_2 = s2;
  assign s2 = T0;
  assign T0 = s1 + 16'h1;
  assign s1 = T1;
  assign T1 = s0 + 16'h1;
  assign s0 = io_in0;
  assign io_zLogicAnalyser0_1 = s1;
  assign io_zLogicAnalyser0_0 = s0;
  assign io_out0 = s2;
endmodule

module BlockA(
    input [15:0] io_in0,
    output[15:0] io_out0,
    output[15:0] io_zLogicAnalyser0_0,
    output[15:0] io_zLogicAnalyser0_1,
    output[15:0] io_zLogicAnalyser0_2
);

  wire[15:0] blockAA_io_zLogicAnalyser0_2;
  wire[15:0] blockAA_io_zLogicAnalyser0_1;
  wire[15:0] blockAA_io_zLogicAnalyser0_0;
  wire[15:0] blockAA_io_out0;


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

module LALogger(input clk, input reset,
    input [7:0] io_config_samplesLeftAfterTrigger,
    input  io_trigger,
    input [47:0] io_probe,
    input  io_log_ready,
    output io_log_valid,
    output io_log_bits_last,
    output[47:0] io_log_bits_fragment
);

  wire[47:0] T0;
  reg [47:0] mem [255:0];
  wire[47:0] T1;
  wire[47:0] T2;
  wire sampleEnable;
  wire T3;
  wire T4;
  reg[1:0] state;
  wire T5;
  wire T6;
  wire T7;
  wire T8;
  reg[7:0] sPushCounter;
  wire T9;
  wire T10;
  wire T11;
  wire T12;
  wire T13;
  reg[7:0] sSampleCounter;
  wire T14;
  wire T15;
  wire[7:0] T16;
  wire[7:0] T17;
  wire[7:0] T18;
  wire[7:0] T19;
  wire[7:0] T20;
  wire T21;
  wire T22;
  wire[1:0] T23;
  wire[1:0] T24;
  reg[7:0] memWriteAddress;
  wire[7:0] T25;
  reg[7:0] memReadAddress;
  wire T26;
  wire[7:0] T27;
  wire[7:0] T28;
  wire[7:0] T29;
  wire[7:0] T30;

`ifndef SYNTHESIS
  integer initvar;
  initial begin
    #0.002;
    for (initvar = 0; initvar < 256; initvar = initvar+1)
      mem[initvar] = {2{$random}};
    state = {1{$random}};
    sPushCounter = {1{$random}};
    sSampleCounter = {1{$random}};
    memWriteAddress = {1{$random}};
    memReadAddress = {1{$random}};
  end
`endif

  assign io_log_bits_fragment = T0;
  assign T0 = mem[memReadAddress];
  always @(posedge clk)
    if (sampleEnable)
      mem[memWriteAddress] <= T2;
  assign T2 = io_probe;
  assign sampleEnable = T3;
  assign T3 = T15 ? 1'h1 : T4;
  assign T4 = state == 2'h0;
  assign T5 = T21 | T6;
  assign T6 = T7 & io_log_ready;
  assign T7 = T11 & T8;
  assign T8 = sPushCounter == 8'hff;
  assign T9 = T12 | T10;
  assign T10 = T11 & io_log_ready;
  assign T11 = state == 2'h2;
  assign T12 = T15 & T13;
  assign T13 = sSampleCounter == 8'h0;
  assign T14 = T4 | T15;
  assign T15 = state == 2'h1;
  assign T16 = T15 ? T18 : T17;
  assign T17 = io_config_samplesLeftAfterTrigger;
  assign T18 = sSampleCounter - 8'h1;
  assign T19 = T10 ? T20 : 8'h0;
  assign T20 = sPushCounter + 8'h1;
  assign T21 = T22 | T12;
  assign T22 = T4 & io_trigger;
  assign T23 = T6 ? 2'h0 : T24;
  assign T24 = T12 ? 2'h2 : 2'h1;
  assign T25 = memWriteAddress + 8'h1;
  assign T26 = T22 | T10;
  assign T27 = T10 ? T30 : T28;
  assign T28 = T29 + 8'h2;
  assign T29 = memWriteAddress + io_config_samplesLeftAfterTrigger;
  assign T30 = memReadAddress + 8'h1;
  assign io_log_bits_last = T7;
  assign io_log_valid = T11;

  always @(posedge clk) begin
    if(reset) begin
      state <= 2'h0;
    end else if(T5) begin
      state <= T23;
    end
    if(T9) begin
      sPushCounter <= T19;
    end
    if(T14) begin
      sSampleCounter <= T16;
    end
    if(reset) begin
      memWriteAddress <= 8'h0;
    end else if(sampleEnable) begin
      memWriteAddress <= T25;
    end
    if(reset) begin
      memReadAddress <= 8'h0;
    end else if(T26) begin
      memReadAddress <= T27;
    end
  end
endmodule

module DecoupledFragment_WidthAdapter(input clk, input reset,
    output io_in_ready,
    input  io_in_valid,
    input  io_in_bits_last,
    input [47:0] io_in_bits_fragment,
    input  io_out_ready,
    output io_out_valid,
    output io_out_bits_last,
    output[7:0] io_out_bits_fragment
);

  wire[7:0] T0;
  reg[47:0] shiftRegister;
  wire T1;
  wire shiftRegisterReady;
  wire T2;
  wire T3;
  wire T4;
  reg[2:0] shiftRegisterCounter;
  wire T5;
  wire[2:0] T6;
  wire[2:0] T7;
  wire T8;
  reg[0:0] shiftRegisterValid;
  wire[47:0] T9;
  wire[47:0] T10;
  wire[39:0] T11;
  wire T12;
  wire T13;
  reg[0:0] shiftRegisterLast;

`ifndef SYNTHESIS
  integer initvar;
  initial begin
    #0.002;
    shiftRegister = {2{$random}};
    shiftRegisterCounter = {1{$random}};
    shiftRegisterValid = {1{$random}};
    shiftRegisterLast = {1{$random}};
  end
`endif

  assign io_out_bits_fragment = T0;
  assign T0 = shiftRegister[3'h7:1'h0];
  assign T1 = io_out_ready | shiftRegisterReady;
  assign shiftRegisterReady = T2;
  assign T2 = T8 | T3;
  assign T3 = io_out_ready & T4;
  assign T4 = shiftRegisterCounter == 3'h0;
  assign T5 = io_out_ready | shiftRegisterReady;
  assign T6 = shiftRegisterReady ? 3'h5 : T7;
  assign T7 = shiftRegisterCounter - 3'h1;
  assign T8 = ~ shiftRegisterValid;
  assign T9 = shiftRegisterReady ? io_in_bits_fragment : T10;
  assign T10 = {8'h0, T11};
  assign T11 = shiftRegister[6'h2f:4'h8];
  assign io_out_bits_last = T12;
  assign T12 = shiftRegisterLast & T13;
  assign T13 = shiftRegisterCounter == 3'h0;
  assign io_out_valid = shiftRegisterValid;
  assign io_in_ready = shiftRegisterReady;

  always @(posedge clk) begin
    if(T1) begin
      shiftRegister <= T9;
    end
    if(T5) begin
      shiftRegisterCounter <= T6;
    end
    if(reset) begin
      shiftRegisterValid <= 1'h0;
    end else if(shiftRegisterReady) begin
      shiftRegisterValid <= io_in_valid;
    end
    if(reset) begin
      shiftRegisterLast <= 1'h0;
    end else if(shiftRegisterReady) begin
      shiftRegisterLast <= io_in_bits_last;
    end
  end
endmodule

module DecoupledFragment_HeaderAdder(input clk, input reset,
    input [15:0] io_header,
    output io_in_ready,
    input  io_in_valid,
    input  io_in_bits_last,
    input [7:0] io_in_bits_fragment,
    input  io_out_ready,
    output io_out_valid,
    output io_out_bits_last,
    output[7:0] io_out_bits_fragment
);

  wire[7:0] T0;
  wire[15:0] T1;
  wire[15:0] T2;
  wire[15:0] T3;
  wire[15:0] T4;
  wire[4:0] T5;
  reg[0:0] counter;
  wire T6;
  wire T7;
  wire T8;
  wire T9;
  wire T10;
  wire T11;
  wire T12;
  reg[0:0] done;
  wire T13;
  wire T14;
  wire T15;
  wire[15:0] T16;
  wire T17;
  wire T18;
  wire T19;
  wire T20;
  wire T21;
  wire T22;
  wire T23;

`ifndef SYNTHESIS
  integer initvar;
  initial begin
    #0.002;
    counter = {1{$random}};
    done = {1{$random}};
  end
`endif

  assign io_out_bits_fragment = T0;
  assign T0 = T1[3'h7:1'h0];
  assign T1 = T17 ? T16 : T2;
  assign T2 = T12 ? T4 : T3;
  assign T3 = {8'h0, io_in_bits_fragment};
  assign T4 = io_header >> T5;
  assign T5 = counter * 4'h8;
  assign T6 = T9 | T7;
  assign T7 = T9 & T8;
  assign T8 = counter == 1'h1;
  assign T9 = io_out_ready & io_out_valid;
  assign T10 = T7 ? 1'h0 : T11;
  assign T11 = counter + 1'h1;
  assign T12 = ~ done;
  assign T13 = T7 | T14;
  assign T14 = T9 & io_in_bits_last;
  assign T15 = T14 == 1'h0;
  assign T16 = {8'h0, io_in_bits_fragment};
  assign T17 = T12 == 1'h0;
  assign io_out_bits_last = T18;
  assign T18 = T17 ? io_in_bits_last : T19;
  assign T19 = T12 ? 1'h0 : io_in_bits_last;
  assign io_out_valid = T20;
  assign T20 = T17 ? io_in_valid : T21;
  assign T21 = T12 ? io_in_valid : io_in_valid;
  assign io_in_ready = T22;
  assign T22 = T17 ? io_out_ready : T23;
  assign T23 = T12 ? 1'h0 : io_out_ready;

  always @(posedge clk) begin
    if(reset) begin
      counter <= 1'h0;
    end else if(T6) begin
      counter <= T10;
    end
    if(reset) begin
      done <= 1'h0;
    end else if(T13) begin
      done <= T15;
    end
  end
endmodule

module LogicAnalyser(input clk, input reset,
    //output io_packetSlave_ready
    input  io_packetSlave_valid,
    input  io_packetSlave_bits_last,
    input [7:0] io_packetSlave_bits_fragment,
    input  io_packetMaster_ready,
    output io_packetMaster_valid,
    output io_packetMaster_bits_last,
    output[7:0] io_packetMaster_bits_fragment,
    input [15:0] io_zLogicAnalyser0_0,
    input [15:0] io_zLogicAnalyser0_1,
    input [15:0] io_zLogicAnalyser0_2
);

  wire[7:0] logWidthAdapter_io_out_bits_fragment;
  wire logWidthAdapter_io_out_bits_last;
  wire logWidthAdapter_io_out_valid;
  wire logHeaderAdder_io_in_ready;
  wire[47:0] logger_io_log_bits_fragment;
  wire logger_io_log_bits_last;
  wire logger_io_log_valid;
  wire logWidthAdapter_io_in_ready;
  wire[47:0] probesBits;
  wire[47:0] T0;
  wire[15:0] T1;
  wire[31:0] T2;
  wire[15:0] T3;
  wire[15:0] T4;
  wire trigger;
  wire T5;
  reg[9:0] triggerCounter;
  wire[9:0] T6;
  wire[7:0] logHeaderAdder_io_out_bits_fragment;
  wire logHeaderAdder_io_out_bits_last;
  wire logHeaderAdder_io_out_valid;

`ifndef SYNTHESIS
  integer initvar;
  initial begin
    #0.002;
    triggerCounter = {1{$random}};
  end
`endif

  assign probesBits = T0;
  assign T0 = {T2, T1};
  assign T1 = io_zLogicAnalyser0_2;
  assign T2 = {T4, T3};
  assign T3 = io_zLogicAnalyser0_1;
  assign T4 = io_zLogicAnalyser0_0;
  assign trigger = T5;
  assign T5 = triggerCounter == 10'h3ff;
  assign T6 = triggerCounter + 10'h1;
  assign io_packetMaster_bits_fragment = logHeaderAdder_io_out_bits_fragment;
  assign io_packetMaster_bits_last = logHeaderAdder_io_out_bits_last;
  assign io_packetMaster_valid = logHeaderAdder_io_out_valid;
  LALogger logger(.clk(clk), .reset(reset),
       .io_config_samplesLeftAfterTrigger( 8'h80 ),
       .io_trigger( trigger ),
       .io_probe( probesBits ),
       .io_log_ready( logWidthAdapter_io_in_ready ),
       .io_log_valid( logger_io_log_valid ),
       .io_log_bits_last( logger_io_log_bits_last ),
       .io_log_bits_fragment( logger_io_log_bits_fragment )
  );
  DecoupledFragment_WidthAdapter logWidthAdapter(.clk(clk), .reset(reset),
       .io_in_ready( logWidthAdapter_io_in_ready ),
       .io_in_valid( logger_io_log_valid ),
       .io_in_bits_last( logger_io_log_bits_last ),
       .io_in_bits_fragment( logger_io_log_bits_fragment ),
       .io_out_ready( logHeaderAdder_io_in_ready ),
       .io_out_valid( logWidthAdapter_io_out_valid ),
       .io_out_bits_last( logWidthAdapter_io_out_bits_last ),
       .io_out_bits_fragment( logWidthAdapter_io_out_bits_fragment )
  );
  DecoupledFragment_HeaderAdder logHeaderAdder(.clk(clk), .reset(reset),
       .io_header( 16'haa55 ),
       .io_in_ready( logHeaderAdder_io_in_ready ),
       .io_in_valid( logWidthAdapter_io_out_valid ),
       .io_in_bits_last( logWidthAdapter_io_out_bits_last ),
       .io_in_bits_fragment( logWidthAdapter_io_out_bits_fragment ),
       .io_out_ready( io_packetMaster_ready ),
       .io_out_valid( logHeaderAdder_io_out_valid ),
       .io_out_bits_last( logHeaderAdder_io_out_bits_last ),
       .io_out_bits_fragment( logHeaderAdder_io_out_bits_fragment )
  );

  always @(posedge clk) begin
    triggerCounter <= reset ? 10'h0 : T6;
  end
endmodule

module Test(input clk, input reset,
    input [15:0] io_in0,
    output[15:0] io_out0,
    output io_packetSlave_ready,
    input  io_packetSlave_valid,
    input  io_packetSlave_bits_last,
    input [7:0] io_packetSlave_bits_fragment,
    input  io_packetMaster_ready,
    output io_packetMaster_valid,
    output io_packetMaster_bits_last,
    output[7:0] io_packetMaster_bits_fragment
);

  wire[15:0] blockA_io_zLogicAnalyser0_2;
  wire[15:0] blockA_io_zLogicAnalyser0_1;
  wire[15:0] blockA_io_zLogicAnalyser0_0;
  reg[15:0] counter;
  wire[15:0] T0;
  wire[7:0] logicAnalyser_io_packetMaster_bits_fragment;
  wire logicAnalyser_io_packetMaster_bits_last;
  wire logicAnalyser_io_packetMaster_valid;
  wire[15:0] blockA_io_out0;

`ifndef SYNTHESIS
  integer initvar;
  initial begin
    #0.002;
    counter = {1{$random}};
  end
`endif

  assign T0 = counter + 16'h1;
  assign io_packetMaster_bits_fragment = logicAnalyser_io_packetMaster_bits_fragment;
  assign io_packetMaster_bits_last = logicAnalyser_io_packetMaster_bits_last;
  assign io_packetMaster_valid = logicAnalyser_io_packetMaster_valid;
  assign io_out0 = blockA_io_out0;
  BlockA blockA(
       .io_in0( counter ),
       .io_out0( blockA_io_out0 ),
       .io_zLogicAnalyser0_0( blockA_io_zLogicAnalyser0_0 ),
       .io_zLogicAnalyser0_1( blockA_io_zLogicAnalyser0_1 ),
       .io_zLogicAnalyser0_2( blockA_io_zLogicAnalyser0_2 )
  );
  LogicAnalyser logicAnalyser(.clk(clk), .reset(reset),
       //.io_packetSlave_ready(  )
       .io_packetSlave_valid( io_packetSlave_valid ),
       .io_packetSlave_bits_last( io_packetSlave_bits_last ),
       .io_packetSlave_bits_fragment( io_packetSlave_bits_fragment ),
       .io_packetMaster_ready( io_packetMaster_ready ),
       .io_packetMaster_valid( logicAnalyser_io_packetMaster_valid ),
       .io_packetMaster_bits_last( logicAnalyser_io_packetMaster_bits_last ),
       .io_packetMaster_bits_fragment( logicAnalyser_io_packetMaster_bits_fragment ),
       .io_zLogicAnalyser0_0( blockA_io_zLogicAnalyser0_0 ),
       .io_zLogicAnalyser0_1( blockA_io_zLogicAnalyser0_1 ),
       .io_zLogicAnalyser0_2( blockA_io_zLogicAnalyser0_2 )
  );

  always @(posedge clk) begin
    counter <= reset ? 16'h0 : T0;
  end
endmodule

