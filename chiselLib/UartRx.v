module UartRx(input clk, input reset,
    input [1:0] io_config_dataType,
    input  io_config_stopType,
    input [1:0] io_config_parityType,
    input [19:0] io_config_clockDivider,
    input  io_rx,
    output io_data_valid,
    output[7:0] io_data_bits_data,
    output io_data_bits_error
);

  wire T0;
  wire T1;
  wire T2;
  reg[0:0] sSum;
  wire T3;
  wire T4;
  wire sEvNewBit;
  wire T5;
  reg[19:0] sCounter;
  wire[19:0] T6;
  wire[19:0] T7;
  wire[19:0] T8;
  wire[19:0] T9;
  wire[18:0] T10;
  wire sEvStart;
  wire T11;
  wire T12;
  reg[0:0] sRx;
  reg[0:0] sRxBuffer;
  wire T13;
  reg[2:0] sState;
  wire T14;
  wire T15;
  wire T16;
  wire T17;
  wire T18;
  wire T19;
  reg[3:0] sBitIdx;
  wire T20;
  wire T21;
  wire T22;
  wire T23;
  wire T24;
  wire T25;
  wire T26;
  wire T27;
  wire T28;
  wire T29;
  wire T30;
  wire T31;
  wire T32;
  wire T33;
  wire[3:0] T34;
  wire[3:0] T35;
  wire[3:0] T36;
  wire[3:0] T37;
  wire T38;
  wire T39;
  wire T40;
  wire T41;
  wire T42;
  wire T43;
  wire T44;
  wire T45;
  wire T46;
  wire T47;
  wire T48;
  wire T49;
  wire T50;
  wire T51;
  wire T52;
  wire T53;
  wire T54;
  wire[2:0] T55;
  wire[2:0] T56;
  wire[2:0] T57;
  wire[2:0] T58;
  wire[2:0] T59;
  wire[2:0] T60;
  wire[2:0] T61;
  wire T62;
  wire T63;
  wire T64;
  wire T65;
  wire T66;
  wire T67;
  wire T68;
  wire T69;
  wire T70;
  wire T71;
  reg[7:0] soData;
  wire[7:0] T72;
  wire[15:0] T73;
  wire[15:0] T74;
  wire[15:0] T75;
  wire[15:0] T76;
  wire[15:0] T77;
  wire[15:0] T78;
  wire[15:0] T79;
  wire[15:0] T80;
  wire T81;

`ifndef SYNTHESIS
  integer initvar;
  initial begin
    #0.002;
    sSum = {1{$random}};
    sCounter = {1{$random}};
    sRx = {1{$random}};
    sRxBuffer = {1{$random}};
    sState = {1{$random}};
    sBitIdx = {1{$random}};
    soData = {1{$random}};
  end
`endif

  assign io_data_bits_error = T0;
  assign T0 = T44 ? 1'h1 : T1;
  assign T1 = T70 ? T2 : 1'h0;
  assign T2 = sSum == 1'h0;
  assign T3 = T63 | T4;
  assign T4 = T62 & sEvNewBit;
  assign sEvNewBit = T5;
  assign T5 = sCounter == 20'h0;
  assign T6 = T5 ? io_config_clockDivider : T7;
  assign T7 = sEvStart ? T9 : T8;
  assign T8 = sCounter - 20'h1;
  assign T9 = {1'h0, T10};
  assign T10 = io_config_clockDivider >> 1'h1;
  assign sEvStart = T11;
  assign T11 = T13 & T12;
  assign T12 = sRx == 1'h0;
  assign T13 = 3'h0 == sState;
  assign T14 = T43 | T15;
  assign T15 = T41 & T16;
  assign T16 = T38 | T17;
  assign T17 = T19 & T18;
  assign T18 = io_config_stopType == 1'h1;
  assign T19 = sBitIdx == 4'h1;
  assign T20 = T21 | T4;
  assign T21 = T28 | T22;
  assign T22 = T24 & T23;
  assign T23 = io_config_parityType == 2'h0;
  assign T24 = T26 & T25;
  assign T25 = sBitIdx == 4'h7;
  assign T26 = T27 & sEvNewBit;
  assign T27 = 3'h2 == sState;
  assign T28 = sEvNewBit | T29;
  assign T29 = T31 & T30;
  assign T30 = sRx == 1'h0;
  assign T31 = T33 & T32;
  assign T32 = sEvNewBit == 1'h1;
  assign T33 = 3'h1 == sState;
  assign T34 = T4 ? 4'h0 : T35;
  assign T35 = T22 ? 4'h0 : T36;
  assign T36 = T29 ? 4'h0 : T37;
  assign T37 = sBitIdx + 4'h1;
  assign T38 = T40 & T39;
  assign T39 = io_config_stopType == 1'h0;
  assign T40 = sBitIdx == 4'h0;
  assign T41 = T42 & sEvNewBit;
  assign T42 = 3'h4 == sState;
  assign T43 = T46 | T44;
  assign T44 = T41 & T45;
  assign T45 = sRx == 1'h0;
  assign T46 = T47 | T4;
  assign T47 = T50 | T48;
  assign T48 = T24 & T49;
  assign T49 = T23 == 1'h0;
  assign T50 = T51 | T22;
  assign T51 = T54 | T52;
  assign T52 = T31 & T53;
  assign T53 = T30 == 1'h0;
  assign T54 = T11 | T29;
  assign T55 = T15 ? 3'h0 : T56;
  assign T56 = T44 ? 3'h0 : T57;
  assign T57 = T4 ? 3'h4 : T58;
  assign T58 = T48 ? 3'h3 : T59;
  assign T59 = T22 ? 3'h4 : T60;
  assign T60 = T52 ? 3'h0 : T61;
  assign T61 = T29 ? 3'h2 : 3'h1;
  assign T62 = 3'h3 == sState;
  assign T63 = T31 | T26;
  assign T64 = T4 ? T69 : T65;
  assign T65 = T26 ? T68 : T66;
  assign T66 = T67;
  assign T67 = io_config_parityType == 2'h2;
  assign T68 = sSum ^ sRx;
  assign T69 = sSum ^ sRx;
  assign T70 = T42 & T71;
  assign T71 = io_config_parityType != 2'h0;
  assign io_data_bits_data = soData;
  assign T72 = T73[3'h7:1'h0];
  assign T73 = T26 ? T75 : T74;
  assign T74 = {8'h0, soData};
  assign T75 = T77 | T76;
  assign T76 = sRx << sBitIdx;
  assign T77 = T80 & T78;
  assign T78 = ~ T79;
  assign T79 = 1'h1 << sBitIdx;
  assign T80 = {8'h0, soData};
  assign io_data_valid = T81;
  assign T81 = T15 ? 1'h1 : T44;

  always @(posedge clk) begin
    if(T3) begin
      sSum <= T64;
    end
    sCounter <= T6;
    sRx <= reset ? 1'h1 : sRxBuffer;
    sRxBuffer <= reset ? 1'h1 : io_rx;
    if(reset) begin
      sState <= 3'h0;
    end else if(T14) begin
      sState <= T55;
    end
    if(T20) begin
      sBitIdx <= T34;
    end
    soData <= T72;
  end
endmodule

