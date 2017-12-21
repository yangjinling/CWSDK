package cwbj.cwsdk2.util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.media.browse.MediaBrowser;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import cwbjsdk.cwsdk.bean.DataBean;
import cwbjsdk.cwsdk.bean.PbocDataElementsClass;
import cwbjsdk.cwsdk.util.BJCWUtil;
import cwbjsdk.cwsdk.util.ParserIdentity;

/**
 * Created by yangjinling on 2017/12/8.
 */

public class BluetoothGattUtil extends BluetoothGattCallback {
    byte[] GetResponse = {0x00, (byte) 0xC0, 0x00, 0x00, 0x00};
    byte ReadRecordFor6C[] = {0x00, (byte) 0xB2, 0x01, 0x00, 0x00};
    byte ReadRecord[] = {0x00, (byte) 0xB2, 0x01, 0x00, 0x00};
    byte SFI = 1;
    private int errorcount;
    public static DataBean SWdataBean = new DataBean();

    public interface connectCallBack {
        void connectSuccess();

        void dealData(int type, String data);

        void getDataFail(int code);//1:获取的数据不完整

        void connectfail();
    }

    private connectCallBack mCallBack;
    private static BluetoothGattUtil bluetoothGattUtil;
    private BluetoothGatt mBluetoothGatt;
    List<BluetoothGattService> serviceList;
    List<BluetoothGattCharacteristic> characterList;
    StringBuilder builder = new StringBuilder();
    private int mType = 0;
    private int index = 0;
    private byte[] value;

    private BluetoothGattUtil() {
    }

    public static BluetoothGattUtil getInstance() {
        if (null == bluetoothGattUtil) {
            bluetoothGattUtil = new BluetoothGattUtil();
        }
        return bluetoothGattUtil;
    }

    public void registerCallBack(connectCallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    public void unregisterCallBack() {
        this.mCallBack = null;
    }


    public void writeRXCharacteristic(int type) {
        //type：操作功能
        if (type == 0) {
            //0：获取版本号
            mType = 0;
            value = BJCWUtil.StrToHex(/*"00" + */PubUtils.sendApdu("FB812000000000", 0xFF));
        } else if (type == 1) {
            //1：接触卡
            mType = 1;
            value = BJCWUtil.StrToHex(/*"01" + */PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x20, "poweron", 20), 20));
        } else if (type == 2) {
            //2：非接触卡
            mType = 2;
            value = BJCWUtil.StrToHex(/*"02" + */PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x10, "poweron", 20), 20));
        } else if (type == 3) {
            //3:读身份证
            mType = 3;
            value = BJCWUtil.StrToHex(/*"03" + */PubUtils.sendApdu("FB801100000000", 20));
        } else if (type == 4) {
            //4:磁条卡
            mType = 4;
            value = BJCWUtil.StrToHex(/*"04" + */PubUtils.sendApdu("FB800014000000", 20));
        } else if (type == 5) {
            //5:键盘输入pin--明文
            mType = 5;
            value = BJCWUtil.StrToHex(/*"05" + */PubUtils.sendApdu("FB802380000003000001", 20));
        } else if (type == 6) {
            //6:键盘输入pin--密文
            mType = 6;
            value = BJCWUtil.StrToHex(/*"06" +*/ PubUtils.sendApdu("FB8025800000080000000000000000", 20));
        } else if (type == 7) {
            //7:指纹模块版本
            mType = 7;
            value = BJCWUtil.StrToHex(/*"07" +*/ PubUtils.sendApdu("FB803400000000", 20));
        } else if (type == 8) {
            //8:指纹模块获取
            mType = 8;
            value = BJCWUtil.StrToHex(/*"08" + */PubUtils.sendApdu("FB803400000000", 20));
        } else if (type == 9) {
            //9:指纹特征获取
            mType = 9;
            value = BJCWUtil.StrToHex(/*"09" + */PubUtils.sendApdu("FB803400000000", 20));
        }
        BluetoothGattCharacteristic mRxChar = null;
        BluetoothGattService mRxService;
        mRxService = mBluetoothGatt.getService(PubUtils.RX_SERVICE_UUID);
        Log.e("YJL", "mBluetoothGatt :" + mBluetoothGatt);
        if (mRxService == null) {
            Log.i("YJL", "Rx service not found!");
            return;
        }
        mRxChar = mRxService.getCharacteristic(PubUtils.RX_CHAR_UUID);
        if (mRxChar == null) {
            Log.i("YJL", "Rx charateristic not found!");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(mRxChar, true);
        Log.i("YJL", "writeRXCharacteristic 1");
        mRxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        Log.i("YJL", "writeRXCharacteristic 2");
        mRxChar.setValue(value);
        Log.i("YJL", "writeRXCharacteristic 3");
        boolean status = mBluetoothGatt.writeCharacteristic(mRxChar); // 手机是中央，但在GATT中是客户端，�?��发�?是写属�?�?
        //BluetoothGattDescriptor d = mRxChar.getDescriptor(mRxChar.getUuid());
        //  d.setValue(value);
        //mBluetoothGatt.writeDescriptor(d);
        if (status) {
            Log.i("YJL", "writeRXCharacteristic OK!");
        } else {
            Log.i("YJL", "writeRXCharacteristic failed!");

        }


    }

    public void writeRXCharacteristic(byte[] value) {

        BluetoothGattCharacteristic mRxChar = null;
        BluetoothGattService mRxService;

        //if (mRxChar == null) {
        mRxService = mBluetoothGatt.getService(PubUtils.RX_SERVICE_UUID);
        Log.e("YJL", "mBluetoothGatt :" + mBluetoothGatt);
        Log.i("YJL", "value :" + BJCWUtil.HexTostr(value, value.length));
        if (mRxService == null) {
            Log.i("YJL", "Rx service not found!");
            return;
        }
        mRxChar = mRxService.getCharacteristic(PubUtils.RX_CHAR_UUID);
        if (mRxChar == null) {
            Log.i("YJL", "Rx charateristic not found!");
            return;
        }
        //}


        mBluetoothGatt.setCharacteristicNotification(mRxChar, true);

        Log.i("YJL", "writeRXCharacteristic 1");
        mRxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        Log.i("YJL", "writeRXCharacteristic 2");
        mRxChar.setValue(value);
        Log.i("YJL", "writeRXCharacteristic 3");
        boolean status = mBluetoothGatt.writeCharacteristic(mRxChar); // 手机是中央，但在GATT中是客户端，�?��发�?是写属�?�?
        //BluetoothGattDescriptor d = mRxChar.getDescriptor(mRxChar.getUuid());
        //  d.setValue(value);
        //mBluetoothGatt.writeDescriptor(d);
        if (status) {
            Log.i("YJL", "writeRXCharacteristic OK!");
        } else {
            Log.i("YJL", "writeRXCharacteristic failed!");

        }


    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                        int newState) {
        //ShowInfo("Chris", "onConnectionStateChange");
        //info.append("onConnectionStateChange");
        //super.onConnectionStateChange(gatt, status, newState);


        //info.append("BluetoothGattCallback onConnectionStateChange status:" + status + " newState:" + newState);
        Log.i("BTCW", "BluetoothGattCallback onConnectionStateChange status:" + status + " newState:" + newState);
        if (gatt != null && gatt.getDevice() != null && gatt.getDevice().getName() != null) {
            Log.i("BTCW", "BluetoothGattCallback onConnectionStateChange dev:" + gatt.getDevice().getName());
        }
        String intentAction;
        if (newState == BluetoothProfile.STATE_CONNECTED) {

            //info.append("connected from GATT server.");

            Log.i("BTCW", "connected from GATT server.");
            Boolean ret = gatt.discoverServices();

            //info.append("Attempting to start service discovery:" + ret);
            Log.i("BTCW", "Attempting to start service discovery:" + ret);
            if (ret) {

                // intentAction = ACTION_GATT_CONNECTED;
                // mConnectionState = STATE_CONNECTED;
                // broadcastUpdate(intentAction);
            } else {
                Log.i("BT", "Disconnected from GATT server.");

            }

        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i("BTCW", "Disconnected from GATT server.");
            gatt.disconnect();
            gatt.close();

        }



		/*
            switch (newState) {
			case BluetoothProfile.STATE_CONNECTED:
				info.append("STATE_CONNECTED");
				gatt.discoverServices();

				break;
			case BluetoothProfile.STATE_DISCONNECTED:
				info.append("STATE_DISCONNECTED");
				break;
			case BluetoothProfile.STATE_CONNECTING:
				info.append("STATE_CONNECTING");
				break;
			case BluetoothProfile.STATE_DISCONNECTING:
				info.append("STATE_DISCONNECTING");
				break;
			default:
				info.append("Default " + newState);
			}*/

    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.i("BTCW", "onServicesDiscovered");
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mBluetoothGatt = gatt;
            serviceList = gatt.getServices();
            for (int i = 0; i < serviceList.size(); i++) {
                BluetoothGattService theService = serviceList.get(i);
                Log.i("BTCW", "ServiceName:" + theService.getUuid());

                characterList = theService.getCharacteristics();
                for (int j = 0; j < characterList.size(); j++) {
                    Log.i("BTCW",
                            "---CharacterName:"
                                    + characterList.get(j).getUuid());
                }
            }


            boolean ret = enableTXNotification();

            if (ret) {
                Log.i("BTCW", "enableTXNotification OK ");
                builder = new StringBuilder();
                mCallBack.connectSuccess();
            } else {
                Log.i("BTCW", "enableTXNotification failed ");
                mCallBack.connectfail();
            }
        }
        //super.onServicesDiscovered(gatt, status);
    }


    boolean enableTXNotification() {
            /*
             * if (mBluetoothGatt == null) { L.e(TAG,"mBluetoothGatt null" +
			 * mBluetoothGatt); broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			 * return; }
			 */
        Log.i("YJL", "enableTXNotification()");

        BluetoothGattService RxService = mBluetoothGatt.getService(PubUtils.RX_SERVICE_UUID); // 通过服务的UUID获得服务
        if (RxService == null) {
            Log.i("YJL", "Rx service not found!");
            // broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return false;
        }
        //BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);


        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(PubUtils.RX_CHAR_UUID);
        if (TxChar == null) {
            Log.i("YJL", "Tx charateristic not found!");
            // broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return false;
        }

        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

//            BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
//            if (null != descriptor) {
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                mBluetoothGatt.writeDescriptor(descriptor);
//            }


        return true;
    }


    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        Log.i("BTCW", "onCharacteristicRead");
        //super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {
        Log.i("BTCW", "onCharacteristicWrite");
    }


    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        Log.i("BTCW", "onCharacteristicChanged");
        Log.e("YJL", "服务器数据过来了");
        byte[] buffer = characteristic.getValue();
        int bytes = buffer.length;
        byte[] buf_data = new byte[bytes];
        for (int i = 0; i < bytes; i++) {
            buf_data[i] = buffer[i];
        }
        String strReadDate = BJCWUtil.HexTostr(buf_data, buf_data.length);
        Log.e("YJL", "strReadDate===" + strReadDate);
        builder.append(strReadDate);
        String result = builder.toString();
        Log.e("YJL", "builder===" + builder.toString().length());
        int pulSW = Integer.valueOf(builder.toString().substring(builder.toString().length() - 4), 16);
        boolean completion = PubUtils.judgeData(result);
        if (completion) {
            if (pulSW == 0x9000) {
                if (mType == 1) {
                    //接触
                    index++;
                    Log.e("YJL", "index===" + index);
                    if (index == 1) {
                        if (builder.toString().length() <= 30) {
                            errorcount++;
                            if (errorcount <= 4) {
                                builder = new StringBuilder();
                                index = 0;
                                writeRXCharacteristic(1);
                            } else {
                                index = 5;
                                builder = new StringBuilder();
                                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                            }
                        } else {
                            writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x20, "00a4040007A0000003330101", 20), 20)));
                            builder = new StringBuilder();
                        }
                    } else if (index == 2) {
                        sendContact(2, result);
                    } else if (index == 3) {
                        sendContact(3, result);
                    } else if (index == 4) {
                        sendContact(4, result);
                    } else if (index == 5) {
                        sendContact(5, result);
                    } else if (index == 6) {
                        if (errorcount > 0) {
                            builder = new StringBuilder();
                            errorcount = 0;
                            index = 0;
                            mCallBack.getDataFail(100);
                        } else {
                            errorcount = 0;
                            mCallBack.dealData(0, SWdataBean.GetCardNum() + "\r\n" + SWdataBean.ICcardInfo);
                            builder = new StringBuilder();
                            errorcount = 0;
                            index = 0;
                        }
                    }
                } else if (mType == 2) {
                    //非接
                    index++;
                    Log.e("YJL", "index===" + index);
                    if (index == 1) {
                        if (builder.toString().length() <= 30) {
                            errorcount++;
                            if (errorcount <= 4) {
                                builder = new StringBuilder();
                                index = 0;
                                writeRXCharacteristic(2);
                            } else {
                                index = 3;
                                builder = new StringBuilder();
                                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                            }
                        } else {
                            writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x10, "00a4040007A0000003330101", 20), 20)));
                            builder = new StringBuilder();
                        }
                    } else if (index == 2) {
                        sendNoContact(2, result);
                    } else if (index == 3) {
                        sendNoContact(3, result);
                    } else if (index == 4) {
                        if (errorcount > 0) {
                            builder = new StringBuilder();
                            errorcount = 0;
                            index = 0;
                            mCallBack.getDataFail(100);
                        } else {
                            mCallBack.dealData(0, SWdataBean.GetCardNum() + "\r\n" + SWdataBean.ICcardInfo);
                            builder = new StringBuilder();
                            errorcount = 0;
                            index = 0;
                        }
                    }
                } else if (mType == 3) {
                    //身份证
                    index++;
                    if (index == 1) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("FB810001000000", 5)));
                        sendData(completion, result);
                    } else {
                        builder = new StringBuilder();
                        index = 0;
                    }
                } else if (mType == 6) {
                    //密文
                    index++;
                    if (index == 1) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("FB802380000003010002", 20)));
                        builder = new StringBuilder();
                    } else {
                        sendData(completion, result);
                        index = 0;
                    }
                } else if (mType == 7) {
                    //指纹模块版本
                    index++;
                    if (index == 1) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000C000009020004090000000D03", 20)));
                        builder = new StringBuilder();
                    } else if (index == 2) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("00000000000000", 20)));
                        sendData(completion, result, 1);
                    } else if (index == 3) {
                        builder = new StringBuilder();
                        index = 0;
                    }

                } else if (mType == 8) {
                    //指纹模板
                    index++;
                    if (index == 1) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000D00000A0200051b000000001e03", 100)));
                        builder = new StringBuilder();
                    } else if (index == 2) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000D00000A0200051b000000011f03", 100)));
                        sendData(completion, result, 3);
                    } else if (index == 3) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000D00000A0200051b000000021c03", 100)));
                        sendData(completion, result, 3);
                        builder = new StringBuilder();
                    } else if (index == 4) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000C0000090200041c0300001B03", 100)));
                        sendData(completion, result, 3);
                        builder = new StringBuilder();
                    } else if (index == 5) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("00000000000000", 100)));
                        sendData(completion, result, 2);
                        builder = new StringBuilder();
                    } else if (index == 6) {
                        builder = new StringBuilder();
                        index = 0;
                    }
                } else if (mType == 9) {
                    //指纹特征
                    index++;
                    if (index == 1) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000C0000090200040C0100000903", 20)));
                        builder = new StringBuilder();
                    } else if (index == 2) {
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("00000000000000", 20)));
                        sendData(completion, result, 2);
                    } else if (index == 3) {
                        builder = new StringBuilder();
                        index = 0;
                    }
                } else {
                    //其他情况
                    sendData(completion, result);
                }
            } else {
                if (mType == 1) {
                    //接触
                    errorcount++;
                    if (errorcount <= 4) {
                        index = 0;
                        builder = new StringBuilder();
                        writeRXCharacteristic(1);
                    } else {
                        index = 5;
                        builder = new StringBuilder();
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                    }
                } else if (mType == 2) {
                    //非接触
                    errorcount++;
                    if (errorcount <= 4) {
                        index = 0;
                        builder = new StringBuilder();
                        writeRXCharacteristic(2);
                    } else {
                        index = 3;
                        builder = new StringBuilder();
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                    }

                } else if (mType == 3) {
                    //超时
                    writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.sendApdu("FB810001000000", 5)));
                    mCallBack.getDataFail(pulSW);
                    index = 1;
                    builder = new StringBuilder();
                } else {
                    mCallBack.getDataFail(pulSW);
                    builder = new StringBuilder();
                }
            }
        }

    }


    @Override
    public void onDescriptorRead(BluetoothGatt gatt,
                                 BluetoothGattDescriptor descriptor, int status) {
        Log.i("BTCW", "onDescriptorRead");
        //super.onDescriptorRead(gatt, descriptor, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt,
                                  BluetoothGattDescriptor descriptor, int status) {
        Log.i("BTCW", "onDescriptorWrite");
        super.onDescriptorWrite(gatt, descriptor, status);
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        Log.i("BTCW", "onReliableWriteCompleted");
        //super.onReliableWriteCompleted(gatt, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        Log.i("BTCW", "onReadRemoteRssi");
        //super.onReadRemoteRssi(gatt, rssi, status);
    }

    private void sendData(boolean completion, String message) {
        if (completion) {
            BJCWUtil.OutputLog("ConnectedThread***************数据完整");
            String strReply = message.substring(6, message.length() - 4);// 4
            mCallBack.dealData(0, strReply);
            builder = new StringBuilder();
        } else {
            BJCWUtil.OutputLog("ConnectedThread***************数据不完整");
            mCallBack.getDataFail(1);
            builder = new StringBuilder();
        }
    }

    private void sendData(boolean completion, String message, int type) {
        if (completion) {
            String strReply = message.substring(6, message.length() - 4);// 4
            if (type == 1) {
                BJCWUtil.OutputLog("ConnectedThread***************数据完整");
                String StrVer = strReply.toString();
                String StringVer = "";
                for (int i = 1; i < StrVer.length(); i++) {
                    StringVer += StrVer.charAt(i++);
                }
                byte[] Ver = BJCWUtil.StrToHex(StringVer);
                String sv = new String(Ver);
                //模块版本特殊处理展示
                mCallBack.dealData(0, sv);
            } else if (type == 2) {
                //展示
                mCallBack.dealData(0, strReply);
            } else if (type == 3) {
                //吐司
                mCallBack.dealData(1, strReply);
            }
            builder = new StringBuilder();
        } else {
            BJCWUtil.OutputLog("ConnectedThread***************数据不完整");
            mCallBack.getDataFail(1);
            builder = new StringBuilder();
        }
    }

    private boolean refresh() {
        try {
            Method localMethod = mBluetoothGatt.getClass().getMethod("refresh");
            if (localMethod != null) {
                Log.e("YJL", "刷新");
                return (Boolean) localMethod.invoke(mBluetoothGatt);
            }
        } catch (Exception localException) {
            Log.e("refreshServices()", "An exception occured while refreshing device");
        }
        return false;
    }

    public void disconnect() {
        refresh();
        builder = null;
        if (null != mBluetoothGatt)
            mBluetoothGatt.disconnect();
        mBluetoothGatt = null;
        if (null != identitys) {
            if (null != identitys.getBmpUser() && !identitys.getBmpUser().isRecycled()) {
                identitys.getBmpUser().recycle();
                identitys = null;
            }
        }
    }

    private void sendNoContact(int index, String result) {
        Log.e("YJL", "error===" + errorcount + "index===" + index);
        if (index == 2) {
            String strReply = result.substring(26, result.length() - 4);// 4
            if (strReply.length() > 26) {
                PbocDataElementsClass pde = new PbocDataElementsClass();
                String strdata = strReply.substring(0, strReply.length() - 4);
                Log.e("YJL", "strdata==" + strdata);
                pde.AnalysisDataElementsSubProcess_OneTime(strdata);
                pde.AnalysisDataElementsProcess();
                SWdataBean.ICcardInfo = pde.ResultInfoShow;
                Log.e("YJL", "获取到的数据" + SWdataBean.ICcardInfo);
                ReadRecord[3] = (byte) ((SFI << (byte) 3) | (byte) 0x04);
                Log.e("YJL", "icResetCard 11");
                char[] szHexReadData = new char[ReadRecord.length * 2];
                BJCWUtil.AscToHex(szHexReadData, ReadRecord, ReadRecord.length);
                String strCmd = new String(szHexReadData);
                Log.e("YJL", "strCmd===" + strCmd);
                String cmd = PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x10, strCmd, 20), 20);
                writeRXCharacteristic(BJCWUtil.StrToHex(cmd));
                builder = new StringBuilder();
            } else {
                //100上电寻卡失败
                errorcount++;
                if (errorcount < 4) {
                    this.index = 0;
                    builder = new StringBuilder();
                    writeRXCharacteristic(2);
                } else {
                    this.index = 3;
                    builder = new StringBuilder();
                    writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                }
            }
        }
        if (index == 3) {
            String strReply = result.substring(20, result.length() - 4);// 4
            if (strReply.length() < 26) {
                Log.e("YJL", "icReadCard  00B2 error 数据错误");
                //100上电寻卡失败
                errorcount++;
                if (errorcount < 4) {
                    this.index = 0;
                    builder = new StringBuilder();
                    writeRXCharacteristic(2);
                } else {
                    this.index = 3;
                    builder = new StringBuilder();
                    writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                }
            } else {
                errorcount = 0;
                String resultCardNum = PubUtils.getCardNum(strReply);
                SWdataBean.SetCardNum(resultCardNum);
                Log.e("YJL", "卡号" + resultCardNum);
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                builder = new StringBuilder();
            }
        }
    }

    private void sendContact(int index, String result) {
        Log.e("YJL", "error===" + errorcount + "index===" + index);
        if (index == 2) {
            String strReply = result.substring(26, result.length() - 4);
            if (strReply.length() < 4) {
                Log.e("YJL", "icReadCard 0101 error 1");
                //上电寻卡失败
                errorcount++;
                if (errorcount < 6) {
                    this.index = 0;
                    builder = new StringBuilder();
                    writeRXCharacteristic(1);
                } else {
                    this.index = 5;
                    builder = new StringBuilder();
                    writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                }
            } else {
                String szSW = strReply.substring(strReply.length() - 4);
                byte[] szASW = new byte[szSW.length() / 2];
                BJCWUtil.HexToAsc(szASW, szSW.getBytes(), szSW.getBytes().length);
                if (szASW[0] == 0x61) {
                    GetResponse[4] = szASW[1];
                    char[] szHexReadData = new char[GetResponse.length * 2];
                    BJCWUtil.AscToHex(szHexReadData, GetResponse, GetResponse.length);
                    String strCmd = new String(szHexReadData);
                    Log.e("YJL", "strCmd===" + strCmd);
                    String cmd = PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x20, strCmd, 20), 20);
                    writeRXCharacteristic(BJCWUtil.StrToHex(cmd));
                    builder = new StringBuilder();
                    Log.e("YJL", "cmd===" + cmd + "接触2===" + PubUtils.COMMAND_IC_CONTACT[2]);
                } else {
                    errorcount++;
                    if (errorcount < 6) {
                        this.index = 0;
                        builder = new StringBuilder();
                        writeRXCharacteristic(1);
                    } else {
                        this.index = 5;
                        builder = new StringBuilder();
                        writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                    }
                }
            }
        } else if (index == 3) {
            String strReply = result.substring(26, result.length() - 4);// 4
            if (strReply.length() > 26) {
                PbocDataElementsClass pde = new PbocDataElementsClass();
                String strdata = strReply.substring(0, strReply.length() - 4);
                Log.e("YJL", "strdata==" + strdata);
                pde.AnalysisDataElementsSubProcess_OneTime(strdata);
                pde.AnalysisDataElementsProcess();
                SWdataBean.ICcardInfo = pde.ResultInfoShow;
                Log.e("YJL", "获取到的数据" + SWdataBean.ICcardInfo);
                ReadRecord[3] = (byte) ((SFI << (byte) 3) | (byte) 0x04);
                Log.e("YJL", "icResetCard 11");
                char[] szHexReadData = new char[ReadRecord.length * 2];
                BJCWUtil.AscToHex(szHexReadData, ReadRecord, ReadRecord.length);
                String strCmd = new String(szHexReadData);
                Log.e("YJL", "icResetCard 12");
                Log.e("YJL", "strCmd===" + strCmd);
                String cmd = PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x20, strCmd, 20), 20);
                builder = new StringBuilder();
                writeRXCharacteristic(BJCWUtil.StrToHex(cmd));
            } else {
                errorcount++;
                if (errorcount < 6) {
                    this.index = 0;
                    builder = new StringBuilder();
                    writeRXCharacteristic(1);
                } else {
                    this.index = 5;
                    builder = new StringBuilder();
                    writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                }
            }

        } else if (index == 4) {
            String strReply = result.substring(6, result.length() - 4);// 4
            String szSW = strReply.substring(strReply.length() - 4);
            Log.e("YJL", "icResetCard 13");
            byte[] szASW = new byte[szSW.length() / 2];
            BJCWUtil.HexToAsc(szASW, szSW.getBytes(), szSW.getBytes().length);
            if (szASW[0] == 0x6C) {
                // memcpy(ReadRecordFor6C, ReadRecord, 4);
                System.arraycopy(ReadRecord, 0, ReadRecordFor6C, 0, 4);
                Log.e("YJL", "icResetCard 14");
                ReadRecordFor6C[4] = szASW[1];
                char[] szHexReadData = new char[ReadRecordFor6C.length * 2];
                BJCWUtil.AscToHex(szHexReadData, ReadRecordFor6C, ReadRecordFor6C.length);
                String strCmd1 = new String(szHexReadData);
                String cmd = PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x20, strCmd1, 20), 20);
                writeRXCharacteristic(BJCWUtil.StrToHex(cmd));
                builder = new StringBuilder();
                Log.e("YJL", "cmd===" + cmd + "接触4===" + PubUtils.COMMAND_IC_CONTACT[3]);
            } else {
                errorcount++;
                if (errorcount < 6) {
                    this.index = 0;
                    builder = new StringBuilder();
                    writeRXCharacteristic(1);
                } else {
                    this.index = 5;
                    builder = new StringBuilder();
                    writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                }
            }
        } else if (index == 5) {
            String strReply = result.substring(20, result.length() - 4);// 4
            if (strReply.length() < 26) {
                Log.e("YJL", "icReadCard  00B2 error 数据错误");
                //上电寻卡失败
                errorcount++;
                if (errorcount < 6) {
                    this.index = 0;
                    builder = new StringBuilder();
                    writeRXCharacteristic(1);
                } else {
                    this.index = 5;
                    builder = new StringBuilder();
                    writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                }
            } else {
                String results = PubUtils.getCardNum(strReply);
                Log.e("YJL", "卡号" + results);
                errorcount = 0;
                SWdataBean.SetCardNum(results);
                builder = new StringBuilder();
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
            }
        }
    }

    private ParserIdentity identitys = new ParserIdentity();


}
