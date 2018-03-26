package cwbjsdk.cwsdk.util;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import cwbjsdk.cwsdk.bean.APDUReplyData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;


/**
 * Created by leonn on 2017/5/31.
 */

public class ConnenctionBlueTooth {


    private static final boolean D = true;
    // Member fields
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private boolean finger;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming
    // connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing
    // connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote
    // device
    public static final int STATE_CONNECTED_FAILED = 4; // now connected fail

    private static final UUID MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private String mstrReplayString;
    private Boolean mbGetReply;
    private String TAG = "ConnectionBle";
    public int nApduTime = 6;

    public BluetoothAdapter mBluetoothAdapter;
    public Boolean bInitConnection = false;

    public static boolean bCancle = false; // 是否取消
    private static int nApduType = 0; // 1 是ccid apdu 0是 apdu
    private static boolean bSendApdu = true;


    private static ConnenctionBlueTooth mConnectionBle = new ConnenctionBlueTooth();

    public static ConnenctionBlueTooth getInstance() {
        return mConnectionBle;
    }


    public int sendApduIC(byte apduType, String apdu, int time, APDUReplyData szReply) {
        finger = false;
        sign = false;
        int nLen = apdu.length() / 2;
        int cmdType = 0x6F;
        BJCWUtil.OutputLog("sendApduIC IN");
        szReply.setRetData("");
        szReply.setSW(0x000C);
        nApduType = 1;
        APDUReplyData szReplyData = new APDUReplyData();
        if (apdu.equals("poweron") || apdu.equals("reset")) {
            cmdType = 0x62;
            nLen = 0;
        } else if (apdu.equals("poweroff")) {
            cmdType = 0x63;
            nLen = 0;
        }
        // ccid 标准头
        byte ccidHead[] = {(byte) cmdType, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        int ccidHeadLen = ccidHead.length;
        ccidHead[1] = (byte) nLen;
        ccidHead[2] = (byte) (nLen >> 8);

        // 命令格式 apduType 类型
        byte cmd[] = {(byte) 0xFB, (byte) 0x80, (byte) apduType, 0x00, 0x00, 0x00, 0x16};
        int cmdHeadLen = cmd.length;
        cmd[5] = (byte) ((nLen + ccidHeadLen) >> 8);
        cmd[6] = (byte) (nLen + ccidHeadLen);

        byte szDate[] = new byte[cmdHeadLen + ccidHeadLen];
        // memcpy(&szDate[0],(char*)&cmd[0],(int)cmdHeadLen);
        System.arraycopy(cmd, 0, szDate, 0, cmdHeadLen);
        // memcpy(&szDate[cmdHeadLen],(char*)&ccidHead[0],(int)sizeof(ccidHead));
        System.arraycopy(ccidHead, 0, szDate, cmdHeadLen, ccidHeadLen);
        if (nLen != 0) {
            // HexToAsc(&szDate[cmdHeadLen+ccidHeadLen],(char*)[data
            // bytes],(int)data.length);
            byte[] pbCmd = new byte[nLen];
            int nCmdLen = BJCWUtil.HexToAsc(pbCmd, apdu.getBytes(), apdu.length());
            byte[] szDateTemp = new byte[cmdHeadLen + ccidHeadLen + nCmdLen];
            System.arraycopy(szDate, 0, szDateTemp, 0, cmdHeadLen + ccidHeadLen);
            System.arraycopy(pbCmd, 0, szDateTemp, cmdHeadLen + ccidHeadLen, nCmdLen);
            szDate = new byte[cmdHeadLen + ccidHeadLen + nCmdLen];
            System.arraycopy(szDateTemp, 0, szDate, 0, szDateTemp.length);
        }
        char date[] = new char[2 * szDate.length];
        BJCWUtil.AscToHex(date, szDate, szDate.length);
        String strCmd = new String(date);
        BJCWUtil.OutputLog("sendApduIC::strCmd:" + strCmd);
        sendApdu(strCmd, time, szReplyData);
        if (szReplyData.getRetData().length() >= 20) {
            // memset(buf, 0, sizeof(buf));
            // strcpy(buf, &reply[20]);
            // strcpy(reply, buf);
            szReply.setSW(szReplyData.getSW());
            szReply.setRetData(szReplyData.getRetData().substring(20));
        }
        BJCWUtil.OutputLog("sendApduIC::reply:" + szReplyData.getRetData());
        return szReplyData.getSW();
    }

    // 发送数据
    public int sendApdu(String cmd, int time, APDUReplyData szReply) {
        finger = false;
        int pulSW = 0;
        sign = false;
        BJCWUtil.OutputLog("sendApdu IN");
        String strLog = new String();
        String strSW = new String();
        String strReply = new String();
        mstrReplayString = "";
        mbGetReply = false;
        bSendApdu = true;
        int nReplyCount = 0;
        strLog = String.format("cmd:%s,time:%d", cmd, time);
        BJCWUtil.OutputLog(strLog);
        int nLen = cmd.length() / 2;
        byte[] pbCmd = new byte[nLen];
        byte szDate[] = new byte[nLen + 3];

        szDate[0] = (byte) ~((nLen >> 8) ^ nLen);
        szDate[1] = (byte) (nLen >> 8);
        szDate[2] = (byte) nLen;

        int nCmdLen = BJCWUtil.HexToAsc(pbCmd, cmd.getBytes(), cmd.length());
        System.arraycopy(pbCmd, 0, szDate, 3, nCmdLen);
        if ((time != 0) && (time != 0xFF)) {
            szDate[6] = (byte) time;
        }

        char date[] = new char[2 * (nCmdLen + 3)];
        BJCWUtil.AscToHex(date, szDate, szDate.length);
        String strDate = new String(date);
        if (time == 0) {
            strLog = String.format("sendApdu:%s", cmd);
        } else {
            strLog = String.format("sendApdu:%s", strDate);
        }

        BJCWUtil.OutputLog(strLog);

        nApduTime = (time + 2) * 20;
        szReply.setRetData("");
        szReply.setSW(0x000C);
        bCancle = false;
        // 这里是否判断蓝牙通信已连接呢？
        if (getState() != STATE_CONNECTED) {
            return CONST_PARAM.RT_BLUETOOTH_FAILED;
        }

        // 重发机制
        do {
            if (time == 0) {
                write(pbCmd, 0, nCmdLen);
            } else {
                write(szDate, 0, szDate.length);
            }

            int i = 0;
            while (!mbGetReply) {
                strLog = String.format("bCancle:" + bCancle);
                BJCWUtil.OutputLog(strLog);
                if (bCancle) {
                    BJCWUtil.OutputLog("***取消操作****");
                    mstrReplayString = "5200026d00";
                    break;
                }
                try {
                    Thread.sleep(50);
                    if (i++ > nApduTime) {
                        break;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    BJCWUtil.OutputLog(e.getLocalizedMessage());
                    return CONST_PARAM.RT_SENDDATA_FAILED;
                }
            }
            BJCWUtil.OutputLog(String.format("\nResultDate:%s", mstrReplayString));
            // 添加这个 蓝牙关闭 无数据返回
            if (mState == STATE_NONE) {
                BJCWUtil.OutputLog("关闭蓝牙");
                return CONST_PARAM.RT_BLUETOOTH_FAILED;
            }
            BJCWUtil.OutputLog("sendApdu 1");
            if (mstrReplayString.length() <= 4)//
            {
                BJCWUtil.OutputLog("接收到的数据长度不正确");
                break;
            }
            if (mstrReplayString.equals("00026984") || mstrReplayString.equals("5200026984")) {
                BJCWUtil.OutputLog("6984 错误 重发");
                if (nApduType == 1 && szDate[10] != 0x62) {
                    szReply.setRetData("");
                    szReply.setSW(0x6984);
                    break;
                }
                mbGetReply = false;
                mstrReplayString = "";
                nReplyCount++;
                continue;
            }
            BJCWUtil.OutputLog("sendApdu 2");
            String strStartDate = mstrReplayString.substring(0, 2);

            // 重发机制*************************
            if (!strStartDate.equals("52")) {
                BJCWUtil.OutputLog("52开头 接受错误，重发");
                mbGetReply = false;
                mstrReplayString = "";
                nReplyCount++;
            } else {
                String strReplyLen = mstrReplayString.substring(2, 6);
                int nstrReplyLen = Integer.parseInt(strReplyLen, 16);
                int nReplyLen = mstrReplayString.length() - 6;
                if (nstrReplyLen * 2 != nReplyLen) {
                    BJCWUtil.OutputLog("长度 接受错误，重发");
                    mbGetReply = false;
                    mstrReplayString = "";
                    nReplyCount++;
                } else {
                    BJCWUtil.OutputLog("sendApdu 3");
                    // CGenUtil.OutputLog(String.format("nApduType:%d,mstrReplayString:%s len:%d",
                    // nApduType, mstrReplayString,mstrReplayString.length()));
                    if (nApduType == 1) {
                        if ((szDate[10] == 0x62) && (mstrReplayString.length() <= 30) && (bCancle == false)) {
                            BJCWUtil.OutputLog("上电寻卡失败！");
                            mbGetReply = false;
                            mstrReplayString = "";
                            nReplyCount++;
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            continue;
                        }
                    }

                    // nReplyCount=3;
                    break;
                }
            }
            BJCWUtil.OutputLog("重发****");
        } while (nReplyCount < 6);
        BJCWUtil.OutputLog("sendApdu 4");
        nLen = mstrReplayString.length();
        BJCWUtil.OutputLog(String.format("\nnLen:%d,ResultDate:%s", nLen, mstrReplayString));
        if (nLen >= 4) {
            if (mstrReplayString.equals("00026984")) {
                szReply.setRetData("");
                szReply.setSW(0x6984);
            } else {
                //TODO 数据传递
                strReply = mstrReplayString.substring(6, nLen - 4);// 4
                strSW = mstrReplayString.substring(nLen - 4);
                pulSW = Integer.valueOf(strSW, 16);
                Log.e("YJL", "length-==-=" + strReply.length());
                szReply.setRetData(strReply);
                szReply.setSW(pulSW);
            }

        } else {
            /*
             * //添加这个 蓝牙关闭 无数据返回 if (mState==STATE_NONE) { return
			 * CONST_PARAM.RT_BLUETOOTH_FAILED; }
			 */
            szReply.setRetData(strReply);
            szReply.setSW(0x000C);
        }
        nApduType = 0;
        BJCWUtil.OutputLog(String.format("Reply:%s,SW:%s", strReply, strSW));
        strReply = "";
        return szReply.getSW();
    }

    // 发送数据
    public int sendApduT(String cmd, APDUReplyData szReply) {
        int pulSW = 0;
        String strLog = new String();
        String strSW = new String();
        String strReply = new String();
        mstrReplayString = "";
        mbGetReply = false;
        bSendApdu = false;
        int nReplyCount = 0;
        strLog = String.format("cmd:%s", cmd);
        BJCWUtil.OutputLog(strLog);
        int nLen = cmd.length() / 2;
        byte[] pbCmd = new byte[nLen];
        int nCmdLen = BJCWUtil.HexToAsc(pbCmd, cmd.getBytes(), cmd.length());
        szReply.setRetData("");
        szReply.setSW(0x000C);
        // 重发机制

        // 这里是否判断蓝牙通信已连接呢？
        if (getState() != STATE_CONNECTED) {
            return CONST_PARAM.RT_BLUETOOTH_FAILED;
        }

        do {
            write(pbCmd, 0, nCmdLen);
            int i = 0;
            while (!mbGetReply) {
                try {
                    Thread.sleep(500);
                    if (i++ > nApduTime) {
                        break;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    BJCWUtil.OutputLog(e.getLocalizedMessage());
                    return CONST_PARAM.RT_SENDDATA_FAILED;
                }
            }
            BJCWUtil.OutputLog(String.format("\nResultDate:%s", mstrReplayString));
            // 添加这个 蓝牙关闭 无数据返回
            if (mState == STATE_NONE) {
                BJCWUtil.OutputLog("关闭蓝牙");
                return CONST_PARAM.RT_BLUETOOTH_FAILED;
            }

            if (mstrReplayString.length() <= 4)//
            {
                BJCWUtil.OutputLog("接收到的数据长度不正确");
                break;
            }

            String strStartDate = mstrReplayString.substring(0, 2);

            // 重发机制*************************
            if (!strStartDate.equals("52")) {
                BJCWUtil.OutputLog("52开头 接受错误，重发");
                mbGetReply = false;
                mstrReplayString = "";
                nReplyCount++;
            } else {
                String strReplyLen = mstrReplayString.substring(2, 6);
                int nstrReplyLen = Integer.parseInt(strReplyLen, 16);
                int nReplyLen = mstrReplayString.length() - 6;
                if (nstrReplyLen * 2 != nReplyLen) {
                    BJCWUtil.OutputLog("长度 接受错误，重发");
                    mbGetReply = false;
                    mstrReplayString = "";
                    nReplyCount++;
                } else {
                    // nReplyCount=3;
                    break;
                }

            }
            BJCWUtil.OutputLog("重发****");
        } while (nReplyCount < 6);

        nLen = mstrReplayString.length();
        if (nLen >= 4) {

            strReply = mstrReplayString.substring(0, nLen - 4);
            strSW = mstrReplayString.substring(nLen - 4);
            pulSW = Integer.valueOf(strSW, 16);
            szReply.setRetData(strReply);
            szReply.setSW(pulSW);
            strReply = "";
        } else {
            /*
             * //添加这个 蓝牙关闭 无数据返回 if (mState==STATE_NONE) { return
			 * CONST_PARAM.RT_BLUETOOTH_FAILED; }
			 */
            szReply.setRetData(strReply);
            szReply.setSW(0x000C);
        }
        BJCWUtil.OutputLog(String.format("Reply:%s,SW:%s", strReply, strSW));
        return szReply.getSW();
    }

    public int sendApduFinger(String cmd, APDUReplyData szReply) {
        mstrReplayString = "";
        mbGetReply = false;
        finger = true;
        sign = false;
        bCancle = false;
        byte[] value = BJCWUtil.StrToHex(cmd);
        write(value, 0, value.length);
        while (!mbGetReply) {
            if (null == mstrReplayString) {
                continue;
            }
            if (null != mstrReplayString && (mstrReplayString.endsWith("9000"))) {
                break;
            }
        }
        if (mstrReplayString != null && (mstrReplayString.endsWith("9000"))) {
            int nLen = mstrReplayString.length();
            String strSW = mstrReplayString.substring(nLen - 4);
            int pulSW = Integer.valueOf(strSW, 16);
            Log.e("YJL", "length-==-=" + mstrReplayString.length() + strSW);
            szReply.setRetData(mstrReplayString);
            szReply.setSW(pulSW);
        } else {
            Log.e("YJL", "length-==-=" + 0);
            szReply.setSW(00);
        }
        return szReply.getSW();
    }

    private boolean sign = false;
    private Handler mHandler;


    public void sendApduSign(String cmd, APDUReplyData szReply, Handler handler) {
        mstrReplayString = "";
        mbGetReply = false;
        finger = false;
        sign = true;
        this.mHandler = handler;
        byte[] value = BJCWUtil.StrToHex(cmd);
        write(value, 0, value.length);
        while (!mbGetReply) {
            if (!TextUtils.isEmpty(mstrReplayString) || mstrReplayString.length() == 0) {
                break;
            } else continue;
        }
        if (!TextUtils.isEmpty(mstrReplayString)) {

        }
    }

    public int sendApduCancle(String cmd, int time, APDUReplyData szReply) {
        int nLen;
        finger = false;
        int pulSW = 0;
        sign = false;
        BJCWUtil.OutputLog("sendApdu IN");
        String strLog = new String();
        String strSW = new String();
        String strReply = new String();
        mstrReplayString = "";
        mbGetReply = false;
        bSendApdu = true;
        int nReplyCount = 0;
        // 重发机制
        do {
            byte[] cmds = BJCWUtil.StrToHex(cmd);
            write(cmds, 0, cmds.length);
            int i = 0;
            while (!mbGetReply) {
                if (bCancle) {
                    BJCWUtil.OutputLog("***取消操作****");
                    mstrReplayString = "5200026d00";
                    break;
                }
                try {
                    Thread.sleep(50);
                    if (i++ > nApduTime) {
                        break;
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                    BJCWUtil.OutputLog(e.getLocalizedMessage());
                    return CONST_PARAM.RT_SENDDATA_FAILED;
                }
            }
            BJCWUtil.OutputLog(String.format("\nResultDate:%s", mstrReplayString));
            // 添加这个 蓝牙关闭 无数据返回
            if (mState == STATE_NONE) {
                BJCWUtil.OutputLog("关闭蓝牙");
                return CONST_PARAM.RT_BLUETOOTH_FAILED;
            }
            BJCWUtil.OutputLog("sendApdu 1");
            if (mstrReplayString.length() <= 4)//
            {
                BJCWUtil.OutputLog("接收到的数据长度不正确");
                break;
            }
            if (mstrReplayString.equals("00026984") || mstrReplayString.equals("5200026984")) {
                BJCWUtil.OutputLog("6984 错误 重发");
                mbGetReply = false;
                mstrReplayString = "";
                nReplyCount++;
                continue;
            }
            BJCWUtil.OutputLog("sendApdu 2");
            String strStartDate = mstrReplayString.substring(0, 2);

            // 重发机制*************************
            if (!strStartDate.equals("52")) {
                BJCWUtil.OutputLog("52开头 接受错误，重发");
                mbGetReply = false;
                mstrReplayString = "";
                nReplyCount++;
            } else {
                String strReplyLen = mstrReplayString.substring(2, 6);
                int nstrReplyLen = Integer.parseInt(strReplyLen, 16);
                int nReplyLen = mstrReplayString.length() - 6;
                if (nstrReplyLen * 2 != nReplyLen) {
                    BJCWUtil.OutputLog("长度 接受错误，重发");
                    mbGetReply = false;
                    mstrReplayString = "";
                    nReplyCount++;
                } else {
                    BJCWUtil.OutputLog("sendApdu 3");
                    break;
                }
            }
            BJCWUtil.OutputLog("重发****");
        } while (nReplyCount < 6);
        BJCWUtil.OutputLog("sendApdu 4");
        nLen = mstrReplayString.length();
        BJCWUtil.OutputLog(String.format("\nnLen:%d,ResultDate:%s", nLen, mstrReplayString));
        if (nLen >= 4) {
            if (mstrReplayString.equals("00026984")) {
                szReply.setRetData("");
                szReply.setSW(0x6984);
            } else {
                //TODO 数据传递
                strReply = mstrReplayString.substring(6, nLen - 4);// 4
                strSW = mstrReplayString.substring(nLen - 4);
                pulSW = Integer.valueOf(strSW, 16);
                Log.e("YJL", "length-==-=" + strReply.length());
                szReply.setRetData(strReply);
                szReply.setSW(pulSW);
            }

        } else {
            /*
             * //添加这个 蓝牙关闭 无数据返回 if (mState==STATE_NONE) { return
			 * CONST_PARAM.RT_BLUETOOTH_FAILED; }
			 */
            szReply.setRetData(strReply);
            szReply.setSW(0x000C);
        }
        nApduType = 0;
        BJCWUtil.OutputLog(String.format("Reply:%s,SW:%s", strReply, strSW));
        strReply = "";
        return szReply.getSW();
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    public synchronized void setState(int state) {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        // mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state,
        // -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (D)
            Log.d(TAG, "connect to: " + device);
        BJCWUtil.OutputLog("connect****************############");
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {

            if (mConnectThread != null) {
                BJCWUtil.OutputLog("connect****************mConnectThread cancel");
                mConnectThread.cancel();
                mConnectThread = null;
                setState(STATE_CONNECTED_FAILED);
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            BJCWUtil.OutputLog("connect****************mConnectedThread cancel");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
        if (D)
            Log.d(TAG, "connected, Socket Type:" + socketType);
        BJCWUtil.OutputLog("connected*****************************");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            BJCWUtil.OutputLog("connected*****************************关闭  Socket");
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            BJCWUtil.OutputLog("connected*****************************关闭  InStream和OutStream");
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();
        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D)
            Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see
     */
    public void write(byte[] out, int offset, int len) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                BJCWUtil.OutputLog("write************ 蓝牙写入失败******************");
                return;
            }
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out, offset, len);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        BJCWUtil.OutputLog("connectionFailed");
        setState(STATE_CONNECTED_FAILED);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        BJCWUtil.OutputLog("connectionLost");
        setState(STATE_NONE);
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;
        private int connetTime = 0;
        private boolean connected = true;

        public ConnectThread(BluetoothDevice device) {
            BJCWUtil.OutputLog("ConnectThread************mmSocket");
            mmDevice = device;
            BluetoothSocket tmp = null;
            boolean secure = true;
            @SuppressWarnings("deprecation")
            int sdk = Integer.parseInt(Build.VERSION.SDK);
            if (sdk >= 10) {
                secure = false;
            }
            mSocketType = secure ? "Secure" : "Insecure";
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                } else {
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
                connected = false;
            } catch (IOException e) {
                connected = true;
                Log.v(TAG, "Socket Type: " + mSocketType + "create() failed", e);
                BJCWUtil.OutputLog("ConnectThread************mmSocket****faile");
            }
            mmSocket = tmp;
        }

        public void run() {
            BJCWUtil.OutputLog("BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);
            BJCWUtil.OutputLog("ConnectThread************BEGIN mConnectThread SocketType");
            // Always cancel discovery because it will slow down a connection
            try {
                // 连接建立之前的先配对
                if (mmDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    Method creMethod = BluetoothDevice.class.getMethod("createBond");
                    BJCWUtil.OutputLog("开始配对");
                    creMethod.invoke(mmDevice);
                } else {
                }
            } catch (Exception e) {
                // TODO: handle exception
                BJCWUtil.OutputLog("无法配对！");
                e.printStackTrace();
            }
            //mBluetoothAdapter.cancelDiscovery();
            while (!connected && connetTime <= 10) {
                // Make a connection to the BluetoothSocket
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    BJCWUtil.OutputLog("ConnectThread************creat  Socketing");
                    if (mmSocket == null) {
                        BJCWUtil.OutputLog("ConnectThread************creat socket faile,agin create socket");
                        boolean secure = true;
                        int sdk = Integer.parseInt(Build.VERSION.SDK);
                        if (sdk >= 10) {
                            secure = false;
                        }
                        try {
                            if (secure) {
                                mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                            } else {
                                mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                            }
                        } catch (IOException e) {
                            Log.v(TAG, "Socket Type: " + mSocketType + "create() failed", e);
                        }
                    }
                    mmSocket.connect();
                    connected = true;
                    BJCWUtil.OutputLog("ConnectThread************creat  Socket");
                } catch (IOException e) {
                    // Close the socket
                    connected = false;
                    connetTime++;
                    e.printStackTrace();
                    if (mmSocket != null) {
                        try {
                            BJCWUtil.OutputLog("ConnectThread***********socket closeing");
                            mmSocket.close();
                            mmSocket = null;
                            BJCWUtil.OutputLog("ConnectThread***********socket close");
                        } catch (IOException e2) {
                            BJCWUtil.OutputLog("ConnectThread***********socket during connection failure");
                            Log.e(TAG, "unable to close() " + mSocketType + " socket during connection failure", e2);
                        }
                        BJCWUtil.OutputLog("ConnectThread***********socket connection");
                    } else {
                        BJCWUtil.OutputLog("ConnectThread***********socket not open");
                    }
                    if (connetTime > 9) {
                        connectionFailed();
                    }
                }
            }

            // Reset the ConnectThread because we're done
            synchronized (ConnenctionBlueTooth.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            if (connected) {
                BJCWUtil.OutputLog("ConnectThread***********connected(************)");
                connected(mmSocket, mmDevice, mSocketType);
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device. It handles all
     * incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            BJCWUtil.OutputLog("ConnectedThread*****************************create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                BJCWUtil.OutputLog("ConnectedThread*****************************getInputStream******getOutputStream");
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
                BJCWUtil.OutputLog("ConnectedThread*****************************faile");
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            //   Log.i(TAG, "BEGIN mConnectedThread");
            BJCWUtil.OutputLog("ConnectedThread*****************************BEGIN mConnectedThread");
            byte[] buffers = new byte[1024];
            byte[] buffer = new byte[1024];
            int bytes;
            String strReadDate = new String();
            int nCount = 0;
            byte[] bei = new byte[100];
            // Keep listening to the InputStream while connected
            while (true) {
                synchronized (mmInStream) {
                    try {
                        // Read from the InputStream
                       /* if (sign) {*/
                        boolean valid = true;
                        //判断前六位是不是012345
                        for (int i = 0; i < 6; i++) {
                            int t = ((Integer) mmInStream.read()).byteValue();
                            Log.e("YJL", "i==" + i + "t==" + t);
                            if (t != i) {
                                bei[i] = (byte) t;
                                Log.e("YJL", "i=1=" + i + "t=1=" + t);
                                valid = false;
                                //前六位判断完了跳出循环
                                break;
                            }
                        }
                        if (valid) {
                            //获取图片大小
                            byte[] bufLength = new byte[4];
                            for (int i = 0; i < 4; i++) {
                                bufLength[i] = ((Integer) mmInStream.read()).byteValue();
                            }
                            int PhotoCount = 0;
                            for (int i = 0; i < 4; i++) {
                                int read = ((Integer) mmInStream.read()).byteValue();
                                if (read == 1) {
                                    PhotoCount++;
                                }
                            }  //获取图片的字节
                            int length = BJCWUtil.ByteArrayToInt(bufLength);
                            buffer = new byte[length];
                            for (int i = 0; i < length; i++) {
                                buffer[i] = ((Integer) mmInStream.read()).byteValue();
                            }
                            //通过handler发出去
                            Message msg = Message.obtain();
                            msg.obj = buffer;
                            if (PhotoCount == 4) {
                                mbGetReply = true;
                                msg.what = 7;
                                mHandler.sendMessage(msg);
                                buffers = new byte[1024];
                                buffer = new byte[1024];
                            }
                        } else {
                       /* } else {
                            Thread.sleep(100);*/
                            if (bCancle) {
                                mbGetReply = true;
                                mstrReplayString = "5200026d00";
                                BJCWUtil.OutputLog("ConnectedThread***************取消");
                            }
                            if ((bytes = mmInStream.read(buffers)) > 0) {
                                Log.e("YJL", "bytes==" + bytes);
                                byte[] buf_data = new byte[bytes];
                                for (int i = 0; i < bytes; i++) {
                                    buf_data[i] = buffers[i];
                                }
                                if (finger) {
                                    strReadDate = new String(buf_data, 0, buf_data.length);
                                    Log.e("YJL", "strReadDate==" + strReadDate);
                                    if (strReadDate.startsWith("52")) {
                                    } else {
                                        strReadDate = "52" + strReadDate;
                                    }
                                    mstrReplayString = mstrReplayString + strReadDate;
                                    if (mstrReplayString.endsWith("9000")) {
                                        BJCWUtil.OutputLog("ConnectedThread***************数据完整");
                                        Log.e("YJL", "length---" + mstrReplayString.length());
                                        mbGetReply = true;
//                                break;
                                    } else {
                                        try {
                                            BJCWUtil.OutputLog("ConnectedThread***************数据不完整");
                                            Log.e("YJL", "length---" + mstrReplayString.length());
                                            mbGetReply = false;
                                            sleep(50);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        continue;
                                    }
                                } else if (sign) {
                                    mstrReplayString = mstrReplayString + strReadDate;
                                    Log.e("YJL", "sign===" + mstrReplayString);
                                    if (!TextUtils.isEmpty(mstrReplayString) || mstrReplayString.length() == 0) {
                                        mbGetReply = true;
//                                 mbGetReply = true;
                                        Message msg = Message.obtain();
                                        msg.what = 12;
                                        mHandler.sendMessage(msg);
                                        buffers = new byte[1024];
                                        buffer = new byte[1024];
                                    }
                              /*  Message msg = Message.obtain();
                                msg.what = 7;
                                msg.obj = buf_data;
                                mHandler.sendMessage(msg);*/
                                } else {
                                    strReadDate = BJCWUtil.HexTostr(buf_data, buf_data.length);
                                    if (strReadDate.startsWith("52")) {
                                    } else {
                                        strReadDate = "52" + strReadDate;
                                    }
                                    BJCWUtil.OutputLog("ConnectedThread************数据返回为：：：" + strReadDate);
                                    if (strReadDate.length() == 0) {
                                        nCount++;
                                        if (nCount < 10) {
                                            try {
                                                sleep(100);
                                            } catch (InterruptedException e) {
                                                // TODO Auto-generated catch block
                                                e.printStackTrace();
                                            }
                                            BJCWUtil.OutputLog("ConnectedThread************数据返回为空");
                                            continue;
                                        }
                                        BJCWUtil.OutputLog("ConnectedThread************数据接收失败");
                                        mbGetReply = true;
                                    } else {
                                        nCount = 0;

                                        if (strReadDate.length() < 6) {
                                            mstrReplayString = mstrReplayString + strReadDate;
                                            BJCWUtil.OutputLog("ConnectedThread*******数据返回 长度小于6数据：" + mstrReplayString);
                                            nCount++;
                                            if (nCount < 3) {
                                                try {
                                                    sleep(10);
                                                } catch (InterruptedException e) {
                                                    // TODO Auto-generated catch block
                                                    e.printStackTrace();
                                                }
                                            }
                                            continue;
                                        } else {
                                            mstrReplayString = mstrReplayString + strReadDate;
                                            BJCWUtil.OutputLog("ConnectedThread*******数据返回 长度大于6数据：" + mstrReplayString);
                                        }

                                        if (nCount > 0) {
                                            if (mstrReplayString.length() < 6) {
                                                BJCWUtil.OutputLog("ConnectedThread*******数据不完整" + mstrReplayString);
                                                buffers = new byte[1024];
                                                mbGetReply = true;
                                            }
                                        }
                                        BJCWUtil.OutputLog("ConnectedThread*******mstrReplayString 数据：" + mstrReplayString);
                                        if (mstrReplayString.equals("00026984")) {
                                            BJCWUtil.OutputLog("ConnectedThread***************6984 数据返回错误");
                                            // mstrReplayString = strReadDate;
                                            buffers = new byte[1024];
                                            mbGetReply = true;
                                        } else {
                                            // null00026984 连上有时候返回
                                            if (!mstrReplayString.startsWith("null")) {
                                                String strReplyLen = mstrReplayString.substring(2, 6);// strReadDate
                                                int nstrReplyLen = Integer.parseInt(strReplyLen, 16);
                                                Log.e("YJL", "nstrReplyLen==" + nstrReplyLen);
                                                int nReplyLen = mstrReplayString.length() - 6;// strReadDate
                                                Log.e("YJL", "nReplyLen==" + nReplyLen);
                                                if (nstrReplyLen * 2 != nReplyLen) {
                                                    buffers = new byte[1024];
                                                    BJCWUtil.OutputLog("ConnectedThread***************数据不完整");
                                                    mbGetReply = false;
                                                    // mstrReplayString = mstrReplayString+
                                                    // strReadDate;
                                                } else {
                                                    buffers = new byte[1024];
                                                    BJCWUtil.OutputLog("ConnectedThread***************数据完整");
                                                    // mstrReplayString = strReadDate;
                                                    mbGetReply = true;
                                                }
                                            }

                                        }

                                    }
                                }

                                BJCWUtil.OutputLog("ConnectedThread*********数据" + mstrReplayString.length() + mstrReplayString);
                            }
                        }
                       /* }*/
                        // Send the obtained bytes to the UI Activity
                        // mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes,
                        // -1, buffer).sendToTarget();
                    } catch (IOException e) {
                        Log.e(TAG, "disconnected", e);
                        connectionLost();// 是否也要加到 bInitConnection里面呢
                        BJCWUtil.OutputLog("ConnectedThread******** 连接蓝牙 会出错");
                        // 添加这个 蓝牙关闭 无数据返回 已连上拉蓝牙 再次连接蓝牙 会出错 加一个标识 试试
                        if (bInitConnection) {
                            // 添加这个 蓝牙关闭 无数据返回 已连上拉蓝牙 再次连接蓝牙 会出错
                            BJCWUtil.OutputLog("ConnectedThread*********添加这个   蓝牙关闭 无数据返回  已连上拉蓝牙  再次连接蓝牙 会出错");
                            mstrReplayString = "";
                            mbGetReply = true;
                        }

                        // //////////////////////////
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer, int offset, int len) {
            try {
                BJCWUtil.OutputLog("写入数据********");
                Log.e("YJL", "写入数据---" + buffer.toString());
                mmOutStream.write(buffer, offset, len);
                mmOutStream.flush();
            } catch (IOException e) {
                BJCWUtil.OutputLog("写入数据********出错");
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                // 关闭时添加
                mmInStream.close();
                mmOutStream.close();
                // 本来就有***********
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    public void close() {
        if (null != mConnectedThread) {
            mConnectedThread.cancel();
        }
        if (null != mConnectThread) {
            mConnectThread.cancel();
        }
    }
}
