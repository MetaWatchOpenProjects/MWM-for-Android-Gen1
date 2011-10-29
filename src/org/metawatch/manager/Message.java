 /*****************************************************************************
  *  Copyright (c) 2011 Meta Watch Ltd.                                       *
  *  www.MetaWatch.org                                                        *
  *                                                                           *
  =============================================================================
  *                                                                           *
  *  Licensed under the Apache License, Version 2.0 (the "License");          *
  *  you may not use this file except in compliance with the License.         *
  *  You may obtain a copy of the License at                                  *
  *                                                                           *
  *    http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                           *
  *  Unless required by applicable law or agreed to in writing, software      *
  *  distributed under the License is distributed on an "AS IS" BASIS,        *
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
  *  See the License for the specific language governing permissions and      *
  *  limitations under the License.                                           *
  *                                                                           *
  *****************************************************************************/

 /*****************************************************************************
  * Message.java                                                              *
  * Message                                                                   *
  * Message type constants                                                    *
  *                                                                           *
  *                                                                           *
  *****************************************************************************/

package org.metawatch.manager;

public enum Message
{
  InvalidMessage(0x00),
  
  GetDeviceType(0x01),
  GetDeviceTypeResponse(0x02),
  GetInfoString(0x03),
  GetInfoStringResponse(0x04),
  DiagnosticLoopback(0x05),  
  EnterShippingModeMsg(0x06),
  SoftwareResetMsg(0x07),
  ConnectionTimeoutMsg(0x08),
  TurnRadioOnMsg(0x09),
  TurnRadioOffMsg(0x0a),
  SppReserved(0x0b),
  PariringControlMsg(0x0c),
  EnterSniffModeMsg(0x0d),
  xxReEnterSniffModeMsg(0x0e),
  LinkAlarmMsg(0x0f),
  
  /*
   * OLED display related commands
   */
  OledWriteBufferMsg(0x10),
  OledConfigureModeMsg(0x11),
  OledChangeModeMsg(0x12),
  OledWriteScrollBufferMsg(0x13),
  OledScrollMsg(0x14),
  OledShowIdleBufferMsg(0x15),
  OledCrownMenuMsg(0x16), 
  OledCrownMenuButtonMsg(0x17),
  
  /* 
   * Status and control
   */
   
  /* move the hands hours), mins and seconds */
  AdvanceWatchHandsMsg(0x20),  

  /* config and (dis)enable vibrate */
  SetVibrateMode(0x23),
  
  /* Sets the RTC */
  SetRealTimeClock(0x26),  
  GetRealTimeClock(0x27),
  GetRealTimeClockResponse(0x28),
  
  /* osal nv */
  NvalOperationMsg(0x30),
  NvalOperationResponseMsg(0x31),
  
  /* status of the current display operation */  
  StatusChangeEvent(0x33),
  
  ButtonEventMsg(0x34),

  GeneralPurposePhoneMsg(0x35),
  GeneralPurposeWatchMsg(0x36),
  /*
   * LCD display related commands
   */ 
  WriteBuffer(0x40),
  ConfigureMode(0x41),
  ConfigureIdleBufferSize(0x42),
  UpdateDisplay(0x43),
  LoadTemplate(0x44),
  UpdateMyDisplaySram(0x45), 
  EnableButtonMsg(0x46),
  DisableButtonMsg(0x47),
  ReadButtonConfigMsg(0x48),
  ReadButtonConfigResponse(0x49),
  UpdateMyDisplayLcd(0x4a),
  
  /* */
  BatteryChargeControl(0x52),
  BatteryConfigMsg(0x53),
  LowBatteryWarningMsgHost(0x54),
  LowBatteryBtOffMsgHost(0x55),
  ReadBatteryVoltageMsg(0x56),
  ReadBatteryVoltageResponse(0x57),
  ReadLightSensorMsg(0x58),
  ReadLightSensorResponse(0x59),
  LowBatteryWarningMsg(0x5a),
  LowBatteryBtOffMsg(0x5b),
  
  /*****************************************************************************
   *
   * User Reserved 0x60-0x70-0x80-0x90
   *
   ****************************************************************************/
  
  
  /*****************************************************************************
   *
   * Watch/Internal Use Only
   *
   ****************************************************************************/
  IdleUpdate(0xa0),
  xxxInitialIdleUpdate(0xa1),
  WatchDrawnScreenTimeout(0xa2),
  ClearLcdSpecial(0xa3),
  WriteLcd(0xa4),
  ClearLcd(0xa5),
  ChangeModeMsg(0xa6),
  ModeTimeoutMsg(0xa7),
  WatchStatusMsg(0xa8),
  MenuModeMsg(0xa9),
  BarCode(0xaa),
  ListPairedDevicesMsg(0xab),
  ConnectionStateChangeMsg(0xac),
  ModifyTimeMsg(0xad),
  MenuButtonMsg(0xae),
  ToggleSecondsMsg(0xaf),
  SplashTimeoutMsg(0xb0),
  
  LedChange(0xc0),
  
  QueryMemoryMsg(0xd0),
    
  AccelerometerSteps(0xea),
  AccelerometerRawData(0xeb);
  public byte msg;
  static public final byte start = 0x01; 
  //byte ID()
  //{
	  //return msg;
  //}
  Message(int msg)
  {
	  this.msg = (byte)msg;
  }
  
} ;