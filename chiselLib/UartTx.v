module UartTx(input clk, input reset,
    input [1:0] io_config_dataType,
    input  io_config_stopType,
    input [1:0] io_config_parityType,
    input [19:0] io_config_clockDivider,
    output io_data_ready,
    input  io_data_valid,
    input [8:0] io_data_bits,
    output io_tx
);

  reg[0:0] tx;
  wire T0;
  wire T1;
  wire T2;
  wire T3;
  wire T4;
  wire T5;
  wire T6;
  reg[2:0] sState;
  wire[2:0] T7;
  wire[2:0] T8;
  wire[2:0] T9;
  wire[2:0] T10;
  wire[2:0] T11;
  wire[2:0] T12;
  wire[2:0] T13;
  wire T14;
  wire T15;
  wire T16;
  wire sEvNewBit;
  wire T17;
  reg[19:0] sCounter;
  wire[19:0] T18;
  wire[19:0] T19;
  wire[19:0] T20;
  wire sEvStart;
  wire T21;
  wire lockingForJob;
  wire T22;
  wire T23;
  wire T24;
  wire T25;
  wire[11:0] T26;
  wire[11:0] stopBitCount;
  wire[11:0] T27;
  wire[1:0] T28;
  wire T29;
  wire[11:0] T30;
  reg[3:0] sBitIdx;
  wire[3:0] T31;
  wire[3:0] T32;
  wire[3:0] T33;
  wire[3:0] T34;
  wire[3:0] T35;
  wire T36;
  wire T37;
  wire[3:0] T38;
  wire[3:0] dataBitCount;
  wire[3:0] T39;
  wire[3:0] T40;
  wire[3:0] T41;
  wire T42;
  wire T43;
  wire T44;
  wire T45;
  wire T46;
  wire T47;
  wire T48;
  wire[3:0] T49;
  wire T50;
  wire T51;
  wire T52;
  wire T53;
  wire T54;
  wire T55;
  wire T56;
  wire T57;
  wire T58;
  wire T59;
  wire T60;
  wire T61;
  wire T62;
  reg[0:0] sSum;
  wire T63;
  wire T64;
  wire T65;
  wire T66;
  wire T67;
  wire T68;
  wire T69;

`ifndef SYNTHESIS
  integer initvar;
  initial begin
    #0.002;
    tx = {1{$random}};
    sState = {1{$random}};
    sCounter = {1{$random}};
    sBitIdx = {1{$random}};
    sSum = {1{$random}};
  end
`endif

  assign io_tx = tx;
  assign T0 = reset ? 1'h1 : T1;
  assign T1 = T69 ? 1'h1 : T2;
  assign T2 = T68 ? sSum : T3;
  assign T3 = T62 ? T61 : T4;
  assign T4 = T60 ? 1'h0 : T5;
  assign T5 = T6 ? 1'h1 : tx;
  assign T6 = 3'h0 == sState;
  assign T7 = reset ? 3'h0 : T8;
  assign T8 = T21 ? 3'h1 : T9;
  assign T9 = T23 ? 3'h0 : T10;
  assign T10 = T58 ? 3'h4 : T11;
  assign T11 = T56 ? 3'h3 : T12;
  assign T12 = T54 ? 3'h4 : T13;
  assign T13 = T14 ? 3'h2 : sState;
  assign T14 = T16 & T15;
  assign T15 = 3'h1 == sState;
  assign T16 = sEvNewBit == 1'h1;
  assign sEvNewBit = T17;
  assign T17 = sCounter == 20'h0;
  assign T18 = T17 ? io_config_clockDivider : T19;
  assign T19 = sEvStart ? io_config_clockDivider : T20;
  assign T20 = sCounter - 20'h1;
  assign sEvStart = T21;
  assign T21 = lockingForJob & io_data_valid;
  assign lockingForJob = T22;
  assign T22 = T53 ? 1'h1 : T23;
  assign T23 = T51 & T24;
  assign T24 = T25 == 1'h0;
  assign T25 = T30 != T26;
  assign T26 = stopBitCount - 12'h1;
  assign stopBitCount = T27;
  assign T27 = {10'h0, T28};
  assign T28 = T29 ? 2'h2 : 2'h1;
  assign T29 = 1'h1 == io_config_stopType;
  assign T30 = {8'h0, sBitIdx};
  assign T31 = T50 ? T49 : T32;
  assign T32 = T47 ? 4'h0 : T33;
  assign T33 = T36 ? T35 : T34;
  assign T34 = T14 ? 4'h0 : sBitIdx;
  assign T35 = sBitIdx + 4'h1;
  assign T36 = T45 & T37;
  assign T37 = sBitIdx != T38;
  assign T38 = dataBitCount - 4'h1;
  assign dataBitCount = T39;
  assign T39 = T44 ? 4'h9 : T40;
  assign T40 = T43 ? 4'h8 : T41;
  assign T41 = T42 ? 4'h7 : 4'h8;
  assign T42 = 2'h0 == io_config_dataType;
  assign T43 = 2'h1 == io_config_dataType;
  assign T44 = 2'h2 == io_config_dataType;
  assign T45 = T16 & T46;
  assign T46 = 3'h2 == sState;
  assign T47 = T45 & T48;
  assign T48 = T37 == 1'h0;
  assign T49 = sBitIdx + 4'h1;
  assign T50 = T51 & T25;
  assign T51 = T16 & T52;
  assign T52 = 3'h4 == sState;
  assign T53 = sState == 3'h0;
  assign T54 = T47 & T55;
  assign T55 = io_config_parityType == 2'h0;
  assign T56 = T47 & T57;
  assign T57 = T55 == 1'h0;
  assign T58 = T16 & T59;
  assign T59 = 3'h3 == sState;
  assign T60 = 3'h1 == sState;
  assign T61 = io_data_bits[sBitIdx];
  assign T62 = 3'h2 == sState;
  assign T63 = T45 ? T66 : T64;
  assign T64 = T14 ? T65 : sSum;
  assign T65 = io_config_parityType == 2'h2;
  assign T66 = sSum ^ T67;
  assign T67 = io_data_bits[sBitIdx];
  assign T68 = 3'h3 == sState;
  assign T69 = 3'h4 == sState;
  assign io_data_ready = T23;

  always @(posedge clk) begin
    if(reset) begin
      tx <= 1'h1;
    end else if(T69) begin
      tx <= 1'h1;
    end else if(T68) begin
      tx <= sSum;
    end else if(T62) begin
      tx <= T61;
    end else if(T60) begin
      tx <= 1'h0;
    end else if(T6) begin
      tx <= 1'h1;
    end
    if(reset) begin
      sState <= 3'h0;
    end else if(T21) begin
      sState <= 3'h1;
    end else if(T23) begin
      sState <= 3'h0;
    end else if(T58) begin
      sState <= 3'h4;
    end else if(T56) begin
      sState <= 3'h3;
    end else if(T54) begin
      sState <= 3'h4;
    end else if(T14) begin
      sState <= 3'h2;
    end
    if(T17) begin
      sCounter <= io_config_clockDivider;
    end else if(sEvStart) begin
      sCounter <= io_config_clockDivider;
    end else begin
      sCounter <= T20;
    end
    if(T50) begin
      sBitIdx <= T49;
    end else if(T47) begin
      sBitIdx <= 4'h0;
    end else if(T36) begin
      sBitIdx <= T35;
    end else if(T14) begin
      sBitIdx <= 4'h0;
    end
    if(T45) begin
      sSum <= T66;
    end else if(T14) begin
      sSum <= T65;
    end
  end
endmodule

