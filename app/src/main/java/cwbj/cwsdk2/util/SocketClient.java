package cwbj.cwsdk2.util;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.socks.library.KLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cwbjsdk.cwsdk.bean.DataBean;
import cwbjsdk.cwsdk.bean.PbocDataElementsClass;
import cwbjsdk.cwsdk.util.BJCWUtil;

/**
 * Created by Administrator on 2016/12/5.
 */

public class SocketClient {
    private Socket client;
    private Context context;
    private int port;           //IP
    private String site;            //端口
    private Thread thread1;
    private Thread thread2;
    private Handler mHandler;
    private boolean isClient = false;
    private OutputStream out;
    private InputStream in;
    private String str;
    byte[] GetResponse = {0x00, (byte) 0xC0, 0x00, 0x00, 0x00};
    byte ReadRecordFor6C[] = {0x00, (byte) 0xB2, 0x01, 0x00, 0x00};
    byte ReadRecord[] = {0x00, (byte) 0xB2, 0x01, 0x00, 0x00};
    byte SFI = 1;
    public DataBean SWdataBean = new DataBean();
    public static int type;
    private static ExecutorService executorService = Executors.newFixedThreadPool(3);

    /**
     * @effect 开启线程建立连接开启客户端
     */
    private ConnectedThread mConnectedThread;

    public void openClientThread() {
        if (null == mClientThread) {
            mClientThread = new ClientThread();
            mClientThread.start();
        }
    }

    private ClientThread mClientThread;

    private class ClientThread extends Thread {
        public ClientThread() {
        }

        public void run() {
            try {
                /**
                 *  connect()步骤
                 * */
                client = new Socket(site, port);
                client.setSoTimeout(5000);//设置超时时间
                Log.e("YJL", "connect===" + client.isConnected());
                KLog.e("YJL1" + client);
                if (client != null) {
                    isClient = true;
                    connected(client);
                } else {
                    isClient = false;
                    KLog.e("连接失败" + "site=" + site + " ,port=" + port);
                    Message msg = new Message();
                    msg.what = 0;
                    mHandler.sendMessage(msg);
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
                KLog.e(e);
                KLog.e("YJL2" + client);
            } catch (IOException e) {
                e.printStackTrace();
                KLog.e(e);
                KLog.e("YJL3" + client);
            }
        }

        public void cancel() {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void connected(Socket socket) {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        // 启动线程管理连接和传输
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

    }

    private class ConnectedThread extends Thread {
        private final Socket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(Socket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // 获得bluetoothsocket输入输出流
            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                Log.e("YJL", "没有创建临时sockets", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            while (isClient) {
                try {
                    Thread.sleep(1);
                    byte[] buffer = new byte[1024];
                    int bytes = 0;
                    String strReadDate = new String();
                    if ((bytes = mmInStream.read(buffer)) > 0) {
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++) {
                            buf_data[i] = buffer[i];
                        }
                        strReadDate = BJCWUtil.HexTostr(buf_data, buf_data.length);
                        stringBuilder.append(strReadDate);
                        Log.e("YJL", "strReadDate===" + strReadDate);
                        if (null != strReadDate) {
                            String result = stringBuilder.toString();
                            String strSW = result.substring(result.length() - 4);
                            int pulSW = Integer.valueOf(strSW, 16);
                            boolean completion = PubUtils.judgeData(result);
                            if (completion) {
                                if (pulSW == 0x9000) {
                                    if (mType == 1) {
                                        //接触卡
                                        index++;
                                        Log.e("YJL", "index===" + index);
                                        if (index == 1) {
                                            if (stringBuilder.toString().length() <= 30) {
                                                countError++;
                                                if (countError <= 4) {
                                                    stringBuilder = new StringBuilder();
                                                    index = 0;
                                                    sendMsg(1);
                                                } else {
                                                    index = 5;
                                                    stringBuilder = new StringBuilder();
                                                    sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                                                }
                                            } else {
                                                sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x20, "00a4040007A0000003330101", 20), 20)));
                                                stringBuilder = new StringBuilder();
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
                                            if (countError > 0) {
                                                index = 0;
                                                countError = 0;
                                                dealDate("寻卡失败", 100);
                                            } else {
                                                Message msg = new Message();
                                                msg.obj = SWdataBean.GetCardNum() + "\r\n" + SWdataBean.ICcardInfo;
                                                msg.what = 1;
                                                mHandler.sendMessage(msg);
                                                stringBuilder = new StringBuilder();
                                                countError = 0;
                                                index = 0;
                                            }
                                        }
                                    } else if (mType == 2) {
                                        //非接触卡
                                        index++;
                                        Log.e("YJL", "index===" + index);
                                        if (index == 1) {
                                            if (stringBuilder.toString().length() <= 30) {
                                                countError++;
                                                if (countError <= 4) {
                                                    stringBuilder = new StringBuilder();
                                                    index = 0;
                                                    sendMsg(2);
                                                } else {
                                                    index = 3;
                                                    stringBuilder = new StringBuilder();
                                                    sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                                                }
                                            } else {
                                                sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x10, "00a4040007A0000003330101", 20), 20)));
                                                stringBuilder = new StringBuilder();
                                            }
                                        } else if (index == 2) {
                                            sendNoContact(2, result);
                                        } else if (index == 3) {
                                            sendNoContact(3, result);
                                        } else if (index == 4) {
                                            if (countError > 0) {
                                                index = 0;
                                                countError = 0;
                                                dealDate("寻卡失败", 100);
                                            } else {
                                                Message msg = new Message();
                                                msg.obj = SWdataBean.GetCardNum() + "\r\n" + SWdataBean.ICcardInfo;
                                                msg.what = 1;
                                                mHandler.sendMessage(msg);
                                                stringBuilder = new StringBuilder();
                                                countError = 0;
                                                index = 0;
                                            }

                                        }
                                    } else if (mType == 3) {
                                        //身份证
                                        index++;
                                        if (index == 1) {
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("FB810001000000", 5)));
                                            dealDate(result, 1);
                                        } else {
                                            stringBuilder = new StringBuilder();
                                            index = 0;
                                        }
                                    } else if (mType == 6) {
                                        //密文
                                        index++;
                                        if (index == 1) {
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("FB802380000003010002", 20)));
                                            stringBuilder = new StringBuilder();
                                        } else {
                                            dealDate(result, 1);
                                            index = 0;
                                        }
                                    } else if (mType == 7) {
                                        //指纹模块版本
                                        index++;
                                        if (index == 1) {
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000C000009020004090000000D03", 20)));
                                            stringBuilder = new StringBuilder();
                                        } else if (index == 2) {
                                            //主界面弹框显示
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("00000000000000", 20)));
                                            dealDate(result, 1, 8);
                                            stringBuilder = new StringBuilder();
                                        } else if (index == 3) {
                                            stringBuilder = new StringBuilder();
                                            index = 0;
                                        }

                                    } else if (mType == 8) {
                                        //指纹模板
                                        index++;
                                        if (index == 1) {
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000D00000A0200051b000000001e03", 100)));
                                            stringBuilder = new StringBuilder();
                                        } else if (index == 2) {
                                            //吐司提示第一次
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000D00000A0200051b000000011f03", 100)));
                                            dealDate(result, 2, 7);
                                            stringBuilder = new StringBuilder();
                                        } else if (index == 3) {
                                            //吐司提示第二次
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000D00000A0200051b000000021c03", 100)));
                                            dealDate(result, 2, 7);
                                            stringBuilder = new StringBuilder();
                                        } else if (index == 4) {
                                            //吐司提示第三次
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000C0000090200041c0300001B03", 100)));
                                            dealDate(result, 2, 7);
                                            stringBuilder = new StringBuilder();
                                        } else if (index == 5) {
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("00000000000000", 100)));
//                                            主界面弹框显示
                                            dealDate(result, 2, 8);
                                            stringBuilder = new StringBuilder();
                                        } else if (index == 6) {
                                            stringBuilder = new StringBuilder();
                                            index = 0;
                                        }
                                    } else if (mType == 9) {
                                        //指纹特征
                                        index++;
                                        if (index == 1) {
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("FB80350500000C0000090200040C0100000903", 20)));
                                            stringBuilder = new StringBuilder();
                                        } else if (index == 2) {
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.sendApdu("00000000000000", 20)));
                                            //主界面弹框显示
                                            dealDate(result, 2, 8);
                                            stringBuilder = new StringBuilder();
                                        } else if (index == 3) {
                                            stringBuilder = new StringBuilder();
                                            index = 0;
                                        }
                                    } else {
                                        //其他
                                        dealDate(result, 1);
                                    }
                                } else {
                                    //数据完整错误码
                                    if (mType == 1) {
                                        //非接触
                                        countError++;
                                        if (countError <= 4) {
                                            index = 0;
                                            stringBuilder = new StringBuilder();
                                            sendMsg(1);
                                        } else {
                                            index = 5;
                                            stringBuilder = new StringBuilder();
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                                        }
                                    } else if (mType == 2) {
                                        //非接触
                                        countError++;
                                        if (countError <= 4) {
                                            index = 0;
                                            stringBuilder = new StringBuilder();
                                            sendMsg(2);
                                        } else {
                                            index = 3;
                                            stringBuilder = new StringBuilder();
                                            sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                                        }

                                    } else {
                                        dealDate("" + pulSW, 2);
                                    }
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }


        /**
         * 向外发送。
         *
         * @param buffer 发送的数据
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer, 0, buffer.length);
                mmOutStream.flush();
            } catch (IOException e) {
                Log.e("YJL", "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e("YJL", "close() of connect socket failed", e);
            }
        }
    }

    private void dealDate(String result, int code) {
        Message msg = new Message();
        msg.what = code;
        if (code == 1) {
            String strReply = result.substring(6, result.length() - 4);// 4
            msg.obj = strReply;
        } else {
            msg.obj = result;
            stringBuilder = new StringBuilder();
        }
        mHandler.sendMessage(msg);
        stringBuilder = new StringBuilder();
    }

    private void dealDate(String result, int type, int what) {
        String strReply = result.substring(6, result.length() - 4);// 4
        Message msg = new Message();
        msg.what = what;
        if (type == 1) {
            String StrVer = strReply.toString();
            String StringVer = "";
            for (int i = 1; i < StrVer.length(); i++) {
                StringVer += StrVer.charAt(i++);
            }
            byte[] Ver = BJCWUtil.StrToHex(StringVer);
            String sv = new String(Ver);
            msg.obj = sv;
        } else {
            msg.obj = strReply;
        }
        mHandler.sendMessage(msg);
        stringBuilder = new StringBuilder();
    }

    public void unregisterHandler() {
        this.mHandler = null;
    }

    /**
     * 调用时向类里传值
     */
    public void clintValue(Context context, String site, int port, Handler handler) {
        this.context = context;
        this.site = site;
        this.port = port;
        this.mHandler = handler;
    }

    StringBuilder stringBuilder = new StringBuilder();


    private int index = 0;
    private int mType;
    /**
     * @steps write();
     * @effect 发送消息
     */
    private byte[] value;

    public void sendMsg(int type) {
        //创建临时对象
        ConnectedThread r;
        // 同步副本的connectedthread
        synchronized (this) {
//            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // 执行写同步
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
            value = BJCWUtil.StrToHex(/*"04" +*/ PubUtils.sendApdu("FB800014000000", 20));
        } else if (type == 5) {
            //5:键盘输入pin--明文
            mType = 5;
            value = BJCWUtil.StrToHex(/*"05" +*/ PubUtils.sendApdu("FB802380000003000001", 20));
        } else if (type == 6) {
            //6:键盘输入pin--密文
            mType = 6;
            value = BJCWUtil.StrToHex(/*"06" +*/ PubUtils.sendApdu("FB8025800000080000000000000000", 20));
        } else if (type == 7) {
            //7:指纹模块版本
            mType = 7;
            value = BJCWUtil.StrToHex(/*"07" + */PubUtils.sendApdu("FB803400000000", 20));
        } else if (type == 8) {
            //8:指纹模块获取
            mType = 8;
            value = BJCWUtil.StrToHex(/*"08" +*/ PubUtils.sendApdu("FB803400000000", 20));
        } else if (type == 9) {
            //9:指纹特征获取
            mType = 9;
            value = BJCWUtil.StrToHex(/*"09" + */PubUtils.sendApdu("FB803400000000", 20));
        }
        r.write(value);
    }

    public void sendMsg(final byte[] buffer) {
        //创建临时对象
        ConnectedThread r;
        // 同步副本的connectedthread
        synchronized (this) {
//            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // 执行写同步
        r.write(buffer);
    }

    private int countError = 0;

    private void sendNoContact(int index, String result) {
        if (index == 2) {
            String strReply = result.substring(26, result.length() - 4);// 4
            if (strReply.length() > 26) {
                countError = 0;
                PbocDataElementsClass pde = new PbocDataElementsClass();
                String strdata = strReply.substring(0, strReply.length() - 4);
                Log.e("YJL", "strdata==" + strdata);
                pde.AnalysisDataElementsSubProcess_OneTime(strdata);
                pde.AnalysisDataElementsProcess();
                SWdataBean.ICcardInfo = pde.ResultInfoShow;
            }
            Log.e("YJL", "获取到的数据" + SWdataBean.ICcardInfo);
            ReadRecord[3] = (byte) ((SFI << (byte) 3) | (byte) 0x04);
            Log.e("YJL", "icResetCard 11");
            char[] szHexReadData = new char[ReadRecord.length * 2];
            BJCWUtil.AscToHex(szHexReadData, ReadRecord, ReadRecord.length);
            String strCmd = new String(szHexReadData);
            Log.e("YJL", "strCmd===" + strCmd);
            String cmd = PubUtils.sendApdu(PubUtils.sendApduIc((byte) 0x10, strCmd, 20), 20);
            sendMsg(BJCWUtil.StrToHex(cmd));
            stringBuilder = new StringBuilder();
        }
        if (index == 3) {
            String strReply = result.substring(20, result.length() - 4);// 4
            if (strReply.length() < 26) {
                Log.e("YJL", "icReadCard  00B2 error 数据错误");
                countError++;
                if (countError < 6) {
                    this.index = 0;
                    sendMsg(2);
                } else {
                    this.index = 3;
                    sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                }
                stringBuilder = new StringBuilder();
            } else {
                countError = 0;
                String resultCardNum = PubUtils.getCardNum(strReply);
                SWdataBean.SetCardNum(resultCardNum);
                Log.e("YJL", "卡号" + resultCardNum);
                sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                stringBuilder = new StringBuilder();
            }
        }
    }

    private void sendContact(int index, String result) {
        if (index == 2) {
            String strReply = result.substring(26, result.length() - 4);
            if (strReply.length() < 4) {
                Log.e("YJL", "icReadCard 0101 error 1");
                //上电寻卡失败
                countError++;
                if (countError < 6) {
                    this.index = 0;
                    stringBuilder = new StringBuilder();
                    sendMsg(1);
                } else {
                    this.index = 5;
                    stringBuilder = new StringBuilder();
                    sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
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
                    sendMsg(BJCWUtil.StrToHex(cmd));
                    stringBuilder = new StringBuilder();
                    Log.e("YJL", "cmd===" + cmd + "接触2===" + PubUtils.COMMAND_IC_CONTACT[2]);
                } else {
                    //上电寻卡失败
                    countError++;
                    if (countError < 6) {
                        this.index = 0;
                        stringBuilder = new StringBuilder();
                        sendMsg(1);
                    } else {
                        this.index = 5;
                        stringBuilder = new StringBuilder();
                        sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
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
                stringBuilder = new StringBuilder();
                sendMsg(BJCWUtil.StrToHex(cmd));
            } else {
                //上电寻卡失败
                countError++;
                if (countError < 6) {
                    this.index = 0;
                    stringBuilder = new StringBuilder();
                    sendMsg(1);
                } else {
                    this.index = 5;
                    stringBuilder = new StringBuilder();
                    sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
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
                sendMsg(BJCWUtil.StrToHex(cmd));
                stringBuilder = new StringBuilder();
                Log.e("YJL", "cmd===" + cmd + "接触4===" + PubUtils.COMMAND_IC_CONTACT[3]);
            } else {  //上电寻卡失败
                countError++;
                if (countError < 6) {
                    this.index = 0;
                    stringBuilder = new StringBuilder();
                    sendMsg(1);
                } else {
                    this.index = 5;
                    stringBuilder = new StringBuilder();
                    sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
                }
            }
        } else if (index == 5) {
            String strReply = result.substring(20, result.length() - 4);// 4
            if (strReply.length() < 26) {
                countError++;
                if (countError < 6) {
                    this.index = 0;
                    sendMsg(1);
                } else {
                    this.index = 5;
                    sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_4));
                }
            } else {
                String results = PubUtils.getCardNum(strReply);
                Log.e("YJL", "卡号" + results);
                SWdataBean.SetCardNum(results);
                stringBuilder = new StringBuilder();
                sendMsg(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_6));
            }
        }
    }

    public void disconnect() {
        if (null != mClientThread) {
            mClientThread.cancel();
            mClientThread = null;
        }
        if (null != mConnectedThread) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }
}
