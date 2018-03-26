package cwbjsdk.cwsdk.sdk;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import cwbjsdk.cwsdk.bean.APDUReplyData;
import cwbjsdk.cwsdk.bean.DataBean;
import cwbjsdk.cwsdk.bean.PbocDataElementsClass;
import cwbjsdk.cwsdk.bean.STR_OBJ;
import cwbjsdk.cwsdk.util.BJCWUtil;
import cwbjsdk.cwsdk.util.CONST_PARAM;
import cwbjsdk.cwsdk.util.ConnenctionBlueTooth;
import cwbjsdk.cwsdk.util.DataBeanCallback;
import cwbjsdk.cwsdk.util.ParserIdentity;
import cwbjsdk.cwsdk.util.SMS4Class;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by leonn on 2017/5/31.
 */

public class CWSDK {


    private final int nCancletimout = 5;
    // 数据基本类型
    // #define OBJ_TYPE_BAS 0
    // #define OBJ_TYPE_STR 1
    private final int OBJ_TYPE_BAS = 0;
    private final int OBJ_TYPE_STR = 1;
    private static int nTime = 40;
    private static Byte apduType = 0x20;
    private static int nTag = 0;
    private static int nSlot = 0;
    private static Byte bEventMask = 0x10;
    private static String strICNum = new String();
    // private ArrayList<STR_OBJ> ObjStrs=null;
    // private STR_OBJ ObjStr;
    private static String nErrorCode = "";
    public DataBean SWdataBean = new DataBean();
    private static CWSDK mImateHY = new CWSDK();
    private static ConnenctionBlueTooth mConnectionBle = ConnenctionBlueTooth.getInstance();
    public DataBeanCallback IDataBeanCallback;
    private static int nCancleApdu = 0; // 取消之后 0表示idReadMessage 1表示swipeCard
    // 2表示icResetCard 3表示waitEvent
    private APDUReplyData szCancleReply = new APDUReplyData();
    private int nBTRetyConectTime = 0;
    private BluetoothDevice mBluetoothDevice = null;
    private boolean mBleState = false;
    // 蓝牙扫描相关设备
    //private IntentFilter btDiscoveryFilter = new IntentFilter();
    // 导入密钥
    private static int nType;
    private static String strKey;
    private STR_OBJ ObjStr;
    // 打印输出内容
    public int bCtrlMode = 0;
    private static String strPrintData;
    private static String TAG = "imatecw";
    private static boolean bOperatCancleFast = false;
    private static boolean bActionStarted = false;
    private static boolean bActionDisConnected = false;// DISCONNECTED
    private static boolean bActionFound = false;// FOUND
    private static boolean bActionFinished = false;// DISCOVERY_FINISHED
    private static boolean bIsCancleApdu = false;
    private int nBroadcastOrder = 0;
    private int g_nConnectState = 0;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();// 本机蓝牙适配器对象
    private static ConnenctionBlueTooth mConnenctionBlueTooth = ConnenctionBlueTooth.getInstance();
    private Context mContext = null;
    String DevicesStr = null;
    private static CWSDK mImateCW = new CWSDK();
    List<String> items = null;
    public int workmode = 0;
    public Handler mhandler;
    final int[] key = {0x11111111, 0x11111111, 0x11111111, 0x11110136};
    private GetMagicCardAsynTask magicCardAsynTask;
    private CWSDK.pbocReadInfoAsynTask pbocReadInfoAsynTask;
    private CWSDK.idReadMessageAsynTask idReadMessageAsynTask;
    private SignAsynTask signAsynTask;
    private PsamAsynTask psamAsynTask;
    private GetFprinterTestAsynTask fprinterTestAsynTask;
    private CWSDK.cancleAsynTask cancleAsynTask;


    private void LogOut(String strData) {
        // Log.v("CGenUtil", strData);
        Log.i("Cashway", strData);
    }

    void SendtoUIMessage(String str, int ctr) {


        if (ctr == 1) {
            Message msg = new Message();
            msg.what = 0x1;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 2) {
            Message msg = new Message();
            msg.what = 0x2;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 3) {
            //银行卡
            Message msg = new Message();
            msg.what = 0x3;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 5) {
            /*读取身份证信息*/
            Message msg = new Message();
            msg.what = 0x5;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 6) {
            /*获取版本*/
            Message msg = new Message();
            msg.what = 0x6;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 7) {
            //键盘录入错误或取消、获取卡片信息失败、超时等错误信息、指纹取消、ic卡数据获取错误，超时等错误
            Message msg = new Message();
            msg.what = 0x7;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 8) {
            //指纹
            Message msg = new Message();
            msg.what = 0x8;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 9) {
            /*读取磁条卡成功*/
            Message msg = new Message();
            msg.what = 0x9;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 10) {
            //明文
            Message msg = new Message();
            msg.what = 10;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 0x11) {
            //密文
            Message msg = new Message();
            msg.what = 0x11;
            msg.obj = str;
            mhandler.sendMessage(msg);
        } else if (ctr == 12) {
            //取消
            Message msg = new Message();
            msg.what = 12;
            msg.obj = str;
            mhandler.sendMessage(msg);
        }

    }


    private void initAction() {
        bActionStarted = false;
        bActionDisConnected = false;
        bActionFound = false;

    }


    public void connectionDevice(BluetoothDevice device) {
        synchronized (this) {

            int nCount = 0;
            LogOut("connectionDevice IN");
//            if (device.getName().startsWith("CW") || device.getName().startsWith("BJ")) {
            LogOut("正在连接安全 盒子设备");
            if (bActionFound) {
                LogOut("设备已连接 ，无需再次连接");
                return;
            }
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            SWdataBean.SetCommId(DataBean.CMD_BLUTOOTH_STATE);
            SWdataBean.SetImateName(device.getName());
            strImateName = device.getName();
            if (mConnenctionBlueTooth.getState() == ConnenctionBlueTooth.STATE_CONNECTED) {
                SWdataBean.SetBleState("已连接");
                IDataBeanCallback.postData(SWdataBean);
                return;
            } else {
                mConnenctionBlueTooth.connect(device, true);
                bActionFound = true;
                while (nCount < 100) {
                    try {
                        if (mConnenctionBlueTooth.getState() == ConnenctionBlueTooth.STATE_CONNECTED) {
                            SWdataBean.SetBleState("已连接");
                            nCount = 0;
                            break;
                        } else {
                            SWdataBean.SetImateName("");
                            SWdataBean.SetBleState("未连接");
                        }
                        Thread.sleep(20 * nCount);
                        nCount++;
                    } catch (InterruptedException e) {
                    }
                }
                IDataBeanCallback.postData(SWdataBean);
            }

//            } else {
//                LogOut("设备不符合连接要求！");
//            }
            LogOut("connectionDevice OUT");
        }
    }

    /*键盘输入pin*/
    public void GetKeyPad(int timeout, int mode) {

        LogOut("GetMagicCard IN");
        bCtrlMode = mode;
        nTime = timeout;
        bIsCancleApdu = true;
        new GetKeyPadAsynTask().execute("");


    }

    private class GetKeyPadAsynTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected Integer doInBackground(String... params) {
            SMS4Class smscala = new SMS4Class();
            int i;
            LogOut("GetKeyPadAsynTask IN");
            APDUReplyData szReply = new APDUReplyData();
            int nRet = 0;

            if (bCtrlMode == 0) {
                /*明文获取pin*/
                nRet = mConnectionBle.sendApdu("FB802380000003000001", nTime, szReply);
            } else {
                /*密文获取pin*/
                nRet = mConnectionBle.sendApdu("FB8025800000080000000000000000", nTime, szReply);
                nRet = mConnectionBle.sendApdu("FB802380000003010001", nTime, szReply);
            }

            if (nRet == 0x9000) {
                String StrVer = szReply.getRetData().toString();
                if (bCtrlMode == 0) {
                    SendtoUIMessage(StrVer, 10);
                } else {
                    int[] cipher = smscala.SMS4ValueToIntArray(StrVer);
                    int[] plain1 = smscala.decrypt(cipher, key);
                    String strplain = smscala.SMS4ValueToString(plain1);
                    SendtoUIMessage(StrVer + ";" + strplain, 0x11);
                }
            } else if (nRet == 0x6985) {
                SendtoUIMessage("取消按键", 7);
            } else {
                SendtoUIMessage("键盘输入失败", 7);
            }
            return null;
        }

    }

    /*获取磁条卡信息*/
    public void GetMagicCard(int timeout) {

        LogOut("GetMagicCard IN");
        nTime = timeout;
        bIsCancleApdu = true;
        if (null != cancleAsynTask && !cancleAsynTask.isCancelled()
                && cancleAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
            cancleAsynTask.cancel(true);
            cancleAsynTask = null;
        }
        magicCardAsynTask = new GetMagicCardAsynTask();
        magicCardAsynTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class GetMagicCardAsynTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected Integer doInBackground(String... params) {

            int i;
            LogOut("GetFprinterVerAsynTask IN");
            APDUReplyData szReply = new APDUReplyData();
            int nRet = 0;
            nRet = mConnectionBle.sendApdu("FB800014000000", nTime, szReply);
            if (nRet == 0x9000) {
                String StrVer = szReply.getRetData().toString();

                String StrLenArray = StrVer.substring(0, 5);
                byte[] TrackLenArray = BJCWUtil.StrToHex(StrVer);
                int index = 6;

                String Track1 = StrVer.substring(index, index + TrackLenArray[0] * 2);

                index += TrackLenArray[0] * 2;

                String Track2 = StrVer.substring(index, index + TrackLenArray[1] * 2);

                index += TrackLenArray[1] * 2;

                String Track3 = StrVer.substring(index, index + TrackLenArray[2] * 2);

                byte[] bTrack1Array = BJCWUtil.StrToHex(Track1);
                byte[] bTrack2Array = BJCWUtil.StrToHex(Track2);
                byte[] bTrack3Array = BJCWUtil.StrToHex(Track3);

                String STrack1 = new String(bTrack1Array);
                String STrack2 = new String(bTrack2Array);
                String STrack3 = new String(bTrack3Array);

                SendtoUIMessage(STrack1 + "#" + STrack2 + "#" + STrack3, 9);
            } else {
                SendtoUIMessage("磁条卡读取失败", 7);
            }
            return null;

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /*取消操作*/
    public void cancle(int type) {
        LogOut("Cancle IN");
        switch (type) {
            case 0:
                //读取接触或非接触
                if (pbocReadInfoAsynTask != null && !pbocReadInfoAsynTask.isCancelled()
                        && pbocReadInfoAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
                    pbocReadInfoAsynTask.cancel(true);
                    pbocReadInfoAsynTask = null;
                }
                break;
            case 1:
                //读身份证
                if (idReadMessageAsynTask != null && !idReadMessageAsynTask.isCancelled()
                        && idReadMessageAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
                    idReadMessageAsynTask.cancel(true);
                    idReadMessageAsynTask = null;
                }
                break;
            case 2:
                //指纹读取
                Log.e("YJL", "type==" + type);
                if (fprinterTestAsynTask != null && !fprinterTestAsynTask.isCancelled()
                        && fprinterTestAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
                    fprinterTestAsynTask.cancel(true);
                    fprinterTestAsynTask = null;
                }
                break;
            case 3:
                //读取磁条卡
                if (magicCardAsynTask != null && !magicCardAsynTask.isCancelled()
                        && magicCardAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
                    magicCardAsynTask.cancel(true);
                    magicCardAsynTask = null;
                }
                break;
            case 4:
                //键盘
                break;
            case 5:
                //签名
                if (signAsynTask != null && !signAsynTask.isCancelled()
                        && signAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
                    signAsynTask.cancel(true);
                    signAsynTask = null;
                }
                break;
            case 6:
                //PSAM
                if (psamAsynTask != null && !psamAsynTask.isCancelled()
                        && psamAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
                    psamAsynTask.cancel(true);
                    psamAsynTask = null;
                }
                break;
        }
        cancleAsynTask = new cancleAsynTask();
        cancleAsynTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private class cancleAsynTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected Integer doInBackground(String... params) {

            int i;
            LogOut("Cancle IN");
            APDUReplyData szReply = new APDUReplyData();
            mConnectionBle.sendApduCancle("FA", nTime, szReply);
            SendtoUIMessage("取消成功", 12);
            return null;

        }
    }

    /*指纹特征*/
    public void GetFprinterTest(int type) {
        LogOut("GetFprinterVer IN");
        this.type = type;
        if (null != cancleAsynTask && !cancleAsynTask.isCancelled()
                && cancleAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
            cancleAsynTask.cancel(true);
            cancleAsynTask = null;
        }
        fprinterTestAsynTask = new GetFprinterTestAsynTask();
        fprinterTestAsynTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private class GetFprinterTestAsynTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected void onProgressUpdate(Void... values) {
            if (isCancelled()) return;
        }

        @Override
        protected Integer doInBackground(String... params) {

            int i;
            LogOut("GetFprinterVerAsynTask IN");
            APDUReplyData szReply = new APDUReplyData();
           /* int nRet = 0;
            nRet = mConnectionBle.sendApdu("FB803400000000", nTime, szReply);
            nRet = mConnectionBle.sendApdu("FB80350500000C0000090200040C0100000903", nTime, szReply);
            if (nRet == 0x9000) {
                String StrVer = szReply.getRetData().toString();

                SendtoUIMessage("指纹特征数据为： " + StrVer, 8);
            } else {
                SendtoUIMessage("获取指纹版本错误", 7);
            }
            nRet = mConnectionBle.sendApdu("00000000000000", nTime, szReply);
*/
            // Task被取消了，马上退出
            int nRet = 0;
            if (type == 0) {//版本
                nRet = mConnectionBle.sendApduFinger("08", szReply);
            } else if (type == 1) {//模板
                nRet = mConnectionBle.sendApduFinger("10", szReply);
            } else if (type == 2) {//特征
                nRet = mConnectionBle.sendApduFinger("09", szReply);
            }
            if (nRet == 0x9000) {
                String result = szReply.getRetData().toString();
                String StrVer = result.substring(0, result.length() - 4);
                SendtoUIMessage(StrVer, 8);
            } else {
                SendtoUIMessage("取消成功", 7);
            }
            return null;
        }


        @Override
        protected void onCancelled() {
            Log.e("YJL", "取消");
            super.onCancelled();
        }
    }

    /*指纹模块版本读取*/
    public void GetFprinterVer(int timeout) {
        LogOut("GetFprinterVer IN");
        nTime = timeout;
        bIsCancleApdu = true;
        new GetFprinterVerAsynTask().execute("");

    }

    private class GetFprinterVerAsynTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected Integer doInBackground(String... params) {

            int i;
            LogOut("GetFprinterVerAsynTask IN");
            APDUReplyData szReply = new APDUReplyData();
          /*  int nRet = 0;
            nRet = mConnectionBle.sendApdu("FB803400000000", nTime, szReply);
            nRet = mConnectionBle.sendApdu("FB80350500000C000009020004090000000D03", nTime, szReply);
            if (nRet == 0x9000) {
                String StrVer = szReply.getRetData().toString();
                String StringVer = "";
                for (i = 1; i < StrVer.length(); i++) {
                    StringVer += StrVer.charAt(i++);
                }
                byte[] Ver = BJCWUtil.StrToHex(StringVer);
                String sv = new String(Ver);
                SendtoUIMessage("指纹模块版本为： " + sv, 8);
            } else {
                SendtoUIMessage("获取指纹版本错误", 7);
            }
            nRet = mConnectionBle.sendApdu("00000000000000", nTime, szReply);*/
            int nRet = mConnectionBle.sendApduFinger("08", szReply);
            if (nRet == 0x9000) {
                String result = szReply.getRetData().toString();
                String StrVer = result.substring(0, result.length() - 4);
                SendtoUIMessage(StrVer, 8);
            } else {
                SendtoUIMessage("获取指纹信息错误", 7);
            }
            return null;
        }
    }

    /*指纹模块*/
    public void GetFprinter(int timeout) {
        LogOut("GetFprinter IN");
        nTime = timeout;
        bIsCancleApdu = true;
        new GetFprinterAsynTask().execute("");
    }


    private class GetFprinterAsynTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {


            LogOut("GetFprinterAsynTask IN");
            /*APDUReplyData szReply = new APDUReplyData();
            int nRet = 0;
            nRet = mConnectionBle.sendApdu("FB803400000000", nTime, szReply);

            nTime = 100;
            nRet = mConnectionBle.sendApdu("FB80350500000D00000A0200051b000000001e03", nTime, szReply);
            if (nRet == 0x9000) {
                String StrVer = szReply.getRetData().toString();
                SendtoUIMessage("模板第一次获取数据成功： " + StrVer, 7);
            } else {
                SendtoUIMessage("获取指纹模板错误", 7);
            }

            nTime = 100;
            nRet = mConnectionBle.sendApdu("FB80350500000D00000A0200051b000000011f03", nTime, szReply);
            if (nRet == 0x9000) {
                String StrVer = szReply.getRetData().toString();
                SendtoUIMessage("模板第二次获取数据成功： " + StrVer, 7);
            } else {
                SendtoUIMessage("获取指纹模板错误", 7);
            }

            nTime = 100;
            nRet = mConnectionBle.sendApdu("FB80350500000D00000A0200051b000000021c03", nTime, szReply);

            if (nRet == 0x9000) {
                String StrVer = szReply.getRetData().toString();
                SendtoUIMessage("模板第三次获取数据成功： " + StrVer, 7);
            } else {
                SendtoUIMessage("获取指纹模板错误", 7);
            }

            nTime = 100;
            nRet = mConnectionBle.sendApdu("FB80350500000C0000090200041c0300001B03", nTime, szReply);

            if (nRet == 0x9000) {
                String StrVer = szReply.getRetData().toString();
                SendtoUIMessage(StrVer, 8);
            } else {
                SendtoUIMessage("获取指纹模板错误", 7);
            }
*/
            APDUReplyData szReply = new APDUReplyData();
            int nRet = mConnectionBle.sendApduFinger("10", szReply);
            if (nRet == 0x9000) {
                String result = szReply.getRetData().toString();
                String StrVer = result.substring(0, result.length() - 4);
                SendtoUIMessage(StrVer, 8);
            } else {
                SendtoUIMessage("获取指纹模板错误", 7);
            }
//            nRet = mConnectionBle.sendApdu("00000000000000", nTime, szReply);
            return null;
        }
    }


    /*签名*/
    private int type;

    public void sign(int type) {
        LogOut("Sign IN");
        this.type = type;
        bIsCancleApdu = true;
        if (null != cancleAsynTask && !cancleAsynTask.isCancelled()
                && cancleAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
            cancleAsynTask.cancel(true);
            cancleAsynTask = null;
        }
        signAsynTask = new SignAsynTask();
        signAsynTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class SignAsynTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            LogOut("Sign IN");
            APDUReplyData szReply = new APDUReplyData();
            if (type == 0) {
                mConnectionBle.sendApduSign("11", szReply, mhandler);
            } else {
                mConnectionBle.sendApduSign("12", szReply, mhandler);
            }
//            Log.e("YJL", "nRet==" + nRet);
        /*    if (nRet == 0x9000) {
                String result = szReply.getRetData().toString();
                String signDate = result.substring(0, result.length() - 4);
                Log.e("YJL", "signDatelegth" + signDate.length());
                SendtoUIMessage(signDate, 8);

            } else {
                SendtoUIMessage("签名失败", 7);
            }*/
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    /*获取psam*/

    public void getPsam(int type) {
        LogOut("GetFprinter IN");
        this.type = type;
        bIsCancleApdu = true;
        psamAsynTask = new PsamAsynTask();
        psamAsynTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class PsamAsynTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            APDUReplyData szReply = new APDUReplyData();
            int nRet = 0;
            if (type == 0) {
                mConnectionBle.sendApdu("fB80211400000a62000000000000000000", nTime, szReply);
                nRet = mConnectionBle.sendApdu("fB8021140000156f0b000000000000000000a4040006a00000033301", nCancletimout, szCancleReply);
            } else {
                mConnectionBle.sendApdu("fB80221400000a62000000000000000000", nTime, szReply);
                nRet = mConnectionBle.sendApdu("fB8022140000156f0b000000000000000000a4040006a00000033301", nCancletimout, szCancleReply);
            }
            if (nRet != 0x9000) {
                // mConnectionBle.sendApdu("FB810001000000", nCancletimout, szCancleReply);
                SendtoUIMessage("psam错误", 6);
            } else {

                String StrVer = szReply.getRetData().toString();

//                byte[] Ver = BJCWUtil.StrToHex(StrVer);

                String ShowVer = new String(StrVer);

                SendtoUIMessage(ShowVer, 6);

            }
            mConnectionBle.sendApdu("fB810014000000", nCancletimout, szCancleReply);
            return 0;
        }
    }

    /*获取固件版本*/
    public void GetVersion(int timeout) {
        LogOut("GetVersion IN");
        nTime = timeout;
        bIsCancleApdu = true;

        new GetVersionAsynTask().execute("");
    }

    private class GetVersionAsynTask extends AsyncTask<String, Void, Integer> {


        @Override
        protected Integer doInBackground(String... params) {


            LogOut("GetVersionAsynTask IN");
            APDUReplyData szReply = new APDUReplyData();
            int nRet = 0;
            nRet = mConnectionBle.sendApdu("FB812000000000", 0xFF, szReply);
            //nRet = mConnectionBle.sendApdu("00840000000008", nTime, szReply);
            if (nRet != 0x9000) {
                // mConnectionBle.sendApdu("FB810001000000", nCancletimout, szCancleReply);
                SendtoUIMessage("获取版本错误", 6);
            } else {

                String StrVer = szReply.getRetData().toString();

                byte[] Ver = BJCWUtil.StrToHex(StrVer);

                String ShowVer = new String(Ver);

                SendtoUIMessage(ShowVer, 6);

            }

            return 0;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        @SuppressWarnings("unused")
        protected void onPostExecute(Integer result) {
            if (result == 2) {
                LogOut("取消 不返回操作");
                return;
            }
            bIsCancleApdu = false;
            LogOut("GetVersionAsynTask End");
            // IDataBeanCallback.postData(SWdataBean);
        }
    }

    /*读取身份证信息*/
    public void idReadMessage(int timeout) {
        LogOut("idReadMessage IN");
        nCancleApdu = 0;
        nTime = timeout;
        bIsCancleApdu = true;
        if (null != cancleAsynTask && !cancleAsynTask.isCancelled()
                && cancleAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
            cancleAsynTask.cancel(true);
            cancleAsynTask = null;
        }
        idReadMessageAsynTask = new idReadMessageAsynTask();
        idReadMessageAsynTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /*异步读取身份证信息*/
    private class idReadMessageAsynTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            SWdataBean.SetCommId(DataBean.CMD_IDEREADMESSAGE_OPERATION);
            SWdataBean.SetInformation("");
            LogOut("idReadMessageAsynTask IN");
            APDUReplyData szReply = new APDUReplyData();
            int nRet = 0;
            ParserIdentity identity = new ParserIdentity();
            identity.mContext = mContext;
            // nRet = mConnectionBle.sendApdu("00840000000008", nTime, szReply);
            nRet = mConnectionBle.sendApdu("FB801100000000", nTime, szReply);
            if ((nRet != 0x000C) && (!ConnenctionBlueTooth.bCancle) && (nRet != 0x6985)) {
                mConnectionBle.sendApdu("FB810001000000", nCancletimout, szCancleReply);
            }
            if (nRet != 0x9000) {
                if (nRet == 0x6300) {
                    LogOut("数据错误！");
                    SWdataBean.SetSucceed("false");
                    SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_DATAERROR);// (StrCode);
                    SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_DATAERROR);
                } else if (nRet == 0x6f00) {
                    LogOut("时间超时");
                    SWdataBean.SetSucceed("false");
                    SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_TIMEOUT);// (StrCode);
                    SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_TIMEOUT);
                } else if ((nRet == 0x6d00) || (nRet == 0x6985)) {
                    LogOut("取消");
                    bOperatCancleFast = false;
                    return 2;
                } else {
                    SWdataBean.SetSucceed("false");
                    SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_FIVE);// (StrCode);
                    SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_FIVE);
                }
                return -1;
            }
            bOperatCancleFast = true;
            identity.parserData(szReply.getRetData(), szReply.getRetData().length());
            LogOut("读取身份证成功");
            SWdataBean.SetSucceed("true");
            SWdataBean.SetInformation(identity.strIdDate);
            SWdataBean.SetPhoto(identity.getBmpUser());
//            SWdataBean.SetPhoto(identity.getIdBmp());
//            Log.e("YJL","bitmap===="+identity.getIdBmp());
            SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCIDE_SUCESS);// (StrCode);
            SWdataBean.SetErrMsg("");
            SendtoUIMessage(" ", 5);
            return 0;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        @SuppressWarnings("unused")
        protected void onPostExecute(Integer result) {
            if (result == 2) {
                LogOut("取消 不返回操作");
                return;
            }
            bIsCancleApdu = false;
            LogOut("idReadMessageAsynTask End");
            IDataBeanCallback.postData(SWdataBean);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }


    public void initialize(Context context, boolean bIsrReceiver, Handler th) {
        // TODO Auto-generated method stub

        //CGenUtil.OutputLog("*******initialize  IN*******");
        initAction();
        bActionFinished = false;
        nBroadcastOrder = 0;

        mhandler = th;

        mContext = context;
        if (mBluetoothAdapter == null) {
            LogOut("BluetoothAdapter NO");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        mConnenctionBlueTooth.mBluetoothAdapter = mBluetoothAdapter;
        if (bIsrReceiver) {
            BJCWUtil.OutputLog("开始注册服务");
            IntentFilter btDiscoveryFilter = new IntentFilter();
            btDiscoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);// 蓝牙扫描开始
            btDiscoveryFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);// 蓝牙扫描状态(SCAN_MODE)发生改变
            btDiscoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);// 蓝牙扫描结束
            btDiscoveryFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);// 监控手机蓝牙的关和打开

            btDiscoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);
            btDiscoveryFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            btDiscoveryFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);

            btDiscoveryFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);//
            btDiscoveryFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            mContext.registerReceiver(BTDiscoveryReceiver, btDiscoveryFilter);

            mBluetoothAdapter.startDiscovery();

        }

        BJCWUtil.OutputLog("*******initialize  OUT*******");

    }

    public void StopDiscovery() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
            BJCWUtil.OutputLog("BT_StopDiscovery");
        }
    }


    public BluetoothDevice GetBlueToothDevicesByMAC(String MAC) {
        BluetoothDevice dev;
        dev = mBluetoothAdapter.getRemoteDevice(MAC);

        return dev;
    }


    public static CWSDK getInstance() {
        return mImateCW;
    }

    public void finalize() {
//		if (btDiscoveryFilter != null) {
//			mContext.unregisterReceiver(BTDiscoveryReceiver);
//		}
        if (mContext != null) {
            mContext.unregisterReceiver(BTDiscoveryReceiver);
        }

        mContext = null;
    }

    private String strImateName = new String();
    private String strPrintName = new String();
    public BroadcastReceiver BTDiscoveryReceiver = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            // SWdataBean.SetCommId(DataBean.CMD_BLUTOOTH_STATE);
            String action = intent.getAction();
            int nCount = 0;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                LogOut("Found：" + device.getName());
                g_nConnectState = device.getBondState();
                switch (g_nConnectState) {
                    case BluetoothDevice.BOND_NONE:// 未配对
                        LogOut("Found：BOND_NONE " + device.getName());
                        // NoBondDevice();

                        DevicesStr = "未配对|" + device.getName() + "|" + device.getAddress();
                        SendtoUIMessage(DevicesStr, 1);
                        DevicesStr = "";
                        break;
                    case BluetoothDevice.BOND_BONDED:// 已配对
                        LogOut("Found：BOND_BONDED " + device.getName());
                        //connectionDevice(device);

                        DevicesStr = "已配对|" + device.getName() + "|" + device.getAddress();
                        SendtoUIMessage(DevicesStr, 1);
                        DevicesStr = "";

                        break;
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                // 状态改变的广播
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                LogOut("CHANGED：" + device.getName());
                // if (device.getName().equalsIgnoreCase(name)) {
                g_nConnectState = device.getBondState();
                switch (g_nConnectState) {
                    case BluetoothDevice.BOND_NONE: {
                        LogOut("CHANGED：BOND_NONE " + device.getName());

                        //兼容发卡箱
                        //NoBondDevice();
                    }
                    break;
                    case BluetoothDevice.BOND_BONDING:
                        LogOut("CHANGED：BOND_BONDING " + device.getName());
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        LogOut("CHANGED：BOND_BONDED " + device.getName());
                        // connectionDevice(device);
                        DevicesStr = "已配对|" + device.getName() + "|" + device.getAddress();
                        SendtoUIMessage(DevicesStr, 1);
                        break;
                }
                // }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LogOut("ACTION_DISCOVERY_STARTED 蓝牙扫描开始");
                SendtoUIMessage("蓝牙扫描开始!", 2);
                nBroadcastOrder = 1;
                if (strImateName.startsWith("CW")) {
                    if (bActionStarted) {
                        LogOut("盒子已经扫描成功，无需重复扫描");
                        return;
                    }
                    bActionStarted = true;
                    LogOut("ACTION_DISCOVERY_STARTED 安全盒子设备");
                    // SWdataBean.SetCommId(DataBean.CMD_BLUTOOTH_STATE);
                    // SWdataBean.SetImateName(strImateName);
                    //if (mConnenctionBlueTooth.getState() == mConnenctionBlueTooth.STATE_CONNECTED) {
                    //    SWdataBean.SetBleState("已连接");
                    //} else {
                    // SWdataBean.SetImateName("");
                    //    SWdataBean.SetBleState("未连接");
                    //}
                    //IDataBeanCallback.postData(SWdataBean);
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                LogOut("ACTION_STATE_CHANGED 蓝牙状态改变");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                //BleChangedState(device, btState);
                if (!mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.startDiscovery();
                }
            }
            // else
            // if(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)){
            // LogOut("ACTION_SCAN_MODE_CHANGED 蓝牙扫描状态(SCAN_MODE)发生改变");
            // }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                LogOut("ACTION_DISCOVERY_FINISHED 蓝牙扫描结束");

                if (workmode == 0) {
                    SendtoUIMessage("蓝牙扫描结束!", 2);
                }

                if (nBroadcastOrder != 1) {
                    //NoBondDevice();
                }

            }
            // else if
            // (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action))
            // {
            // LogOut("正在断开蓝牙连接。。。");
            // }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                LogOut("ACTION_ACL_DISCONNECTED 蓝牙连接已断开！！！");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //disconnectionDevice(device);

            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                LogOut("ACTION_ACL_CONNECTED 监控到有蓝牙设备打开！！！");
                //if (mConnenctionBlueTooth.getState() == mConnenctionBlueTooth.STATE_CONNECTED) {
                //    if (mBluetoothAdapter.isDiscovering()) {
                //        LogOut("停止扫描蓝牙设备");
                //        mBluetoothAdapter.cancelDiscovery(); // 魅族5手机一直不断的跑这个
                //    } else {
                //        LogOut("状态未改变:" + mConnenctionBlueTooth.getState());
                //    }
                // } else {
                //    LogOut("开始扫描蓝牙设备");
                //    mBluetoothAdapter.startDiscovery();
                //}

            }

        }
    };


    // 等待事件，包括磁卡刷卡、Pboc IC插入、放置射频卡。timeout是最长等待时间(秒)
    // eventMask的bit来标识等待事件：
    // 0x01 等待刷卡事件
    // 0x02 等待插卡事件
    // 0x04 等待射频事件
    // 0xFF 等待所有事件
    public void waitEvent(byte eventMask, int timeout) {
        LogOut("waitEvent IN");
        bEventMask = eventMask;
        nTime = timeout;
        bIsCancleApdu = true;
        new waitEventAsynTask().execute("");
    }


    private boolean icReadCard(Byte apduType/* ,char* szNum */, int timeout) {
        LogOut("icReadCard IN");

        APDUReplyData szApduReply = new APDUReplyData();
        PbocDataElementsClass pde = new PbocDataElementsClass();
        int nRet = 0;

        // unsigned short bLen;
        int bLen = 0;
        // NSString *cmd=[[NSString alloc]init];
        String cmd = new String();
        byte ReadRecord[] = {0x00, (byte) 0xB2, 0x01, 0x00, 0x00};
        byte[] GetResponse = {0x00, (byte) 0xC0, 0x00, 0x00, 0x00};
        byte ReadRecordFor6C[] = {0x00, (byte) 0xB2, 0x01, 0x00, 0x00};
        byte SFI = 1;

        // char szReply[1024]={0};
        String szReply = new String();
        // char szHexReadData[1024]={0};
        // String szHexReadData=new String();
        char[] szHexReadData = null;

        // char szCardNumber[1024]={0};
        String szCardNumber = new String();

        // char replay[1024*5] = {0};
        String replay = new String();
        // char szSW[1024]={0};
        String szSW = new String();

        // char szASW[1024]={0};
        // String szASW=new String();
        byte[] szASW = null;
        // unsigned short sw=0;
        int sw = 0;

        LogOut("icResetCard 2");

        // nRet=mConnectionBle.sendApduIC:apduType apdu:@"poweron" replay:replay
        // sw:&sw timeOut:timeout];
        // if (sw!=0x9000) {
        // LogOut(TAG,"上电指令失败！");
        // return false;
        // }
        cmd = "00a4040007A0000003330101";
        nRet = mConnectionBle.sendApduIC(apduType, cmd, nTime, szApduReply);
        // nRet=mConnectionBle.sendApduIC(apduType,cmd,)
        replay = szApduReply.getRetData();
        sw = szApduReply.getSW();
        if (sw != 0X9000) {
            LogOut("icReadCard 0101 error 0");
            if (sw == 0x6984) {
                LogOut("选择 0101 复位");
                // bReadCard=false;
            }
            return false;
        }
        if (replay.length() < 4) {
            LogOut("icReadCard 0101 error 1");
            return false;
        }

        if (replay.length() > 4) {
            String strdata = replay.substring(0, replay.length() - 4);
            Log.e("YJL", "strdata==" + strdata);
            pde.AnalysisDataElementsSubProcess_OneTime(strdata);
            pde.AnalysisDataElementsProcess();
            SWdataBean.ICcardInfo = pde.ResultInfoShow;
        }

        LogOut("icResetCard 3");
        // memset(szSW, 0, sizeof(szSW));
        // strcpy(szSW, &replay[strlen(replay)-4]);
        szSW = replay.substring(replay.length() - 4);

        // HexToAsc(szASW, szSW, (int)strlen(szSW));
        // [CMBCBank_cashway HexToAsc:szASW src:szSW srcLen:(int)strlen(szSW)];
        szASW = new byte[szSW.length() / 2];
        BJCWUtil.HexToAsc(szASW, szSW.getBytes(), szSW.getBytes().length);
        if (szASW[0] == 0x61) {
            GetResponse[4] = szASW[1];
            LogOut("icResetCard 4");
            // memset(szHexReadData, 0, sizeof(szHexReadData));
            // [CMBCBank_cashway AscToHex:szHexReadData pSrc:(char *)GetResponse
            // SrcLen:sizeof(GetResponse)];
            szHexReadData = new char[GetResponse.length * 2];
            BJCWUtil.AscToHex(szHexReadData, GetResponse, GetResponse.length);

            // NSString *strCmd=[[NSString alloc]
            // initWithFormat:@"%s",szHexReadData];
            String strCmd = new String(szHexReadData);
            Log.e("YJL", "strCmd===" + strCmd);
            // mConnectionBle.sendApduIC:apduType apdu:strCmd replay:replay
            // sw:&sw timeOut:timeout];
            mConnectionBle.sendApduIC(apduType, strCmd, timeout, szApduReply);
            sw = szApduReply.getSW();
            replay = szApduReply.getRetData();
            LogOut("icResetCard 52");
            if (sw != 0X9000) {
                LogOut("icReadCard 0101 error 3");
                if (sw == 0x6984) {
                    LogOut("选择 0101 复位  1");
                    // bReadCard=false;
                }
                return false;
            }
            if (replay.length() < 4) {
                LogOut("icReadCard 0101 error 4");
                return false;
            }

            if (replay.length() > 26) {
                String strdata = replay.substring(0, replay.length() - 4);
                pde.AnalysisDataElementsSubProcess_OneTime(strdata);
                pde.AnalysisDataElementsProcess();
                SWdataBean.ICcardInfo = pde.ResultInfoShow;
            }

            // memset(szSW, 0, sizeof(szSW));
            // strcpy(szSW, &replay[strlen(replay)-4]);
            szSW = replay.substring(replay.length() - 4);
        }

        LogOut("icResetCard 6");
        // if (strcmp(szSW, "9000")!=0)

        if (!szSW.equals("9000")) {
            LogOut("icReadCard 102 ");
            cmd = "00a4040007A0000003330102";
            // mConnectionBle.sendApduIC:apduType apdu:cmd replay:replay sw:&sw
            // timeOut:timeout];
            mConnectionBle.sendApduIC(apduType, cmd, timeout, szApduReply);
            sw = szApduReply.getSW();
            replay = szApduReply.getRetData();
            if (sw != 0X9000) {
                LogOut("icReadCard 0102 error 0");
                if (sw == 0x6984) {
                    LogOut("选择 0102 复位");
                    // bReadCard=true;
                }
                return false;
            }
            if (replay.length() < 4) {
                LogOut("icReadCard 0102 error 1");
                return false;
            }
            LogOut("icResetCard 7");
            // memset(szSW, 0, sizeof(szSW));
            // memset(szASW, 0, sizeof(szASW));
            // strcpy(szSW, &replay[strlen(replay)-4]);
            szSW = replay.substring(replay.length() - 4);

            // [CMBCBank_cashway HexToAsc:szASW src:szSW
            // srcLen:(int)strlen(szSW)];
            szASW = new byte[szSW.length() / 2];
            BJCWUtil.HexToAsc(szASW, szSW.getBytes(), szSW.getBytes().length);
            String strSW = new String(szASW);
            LogOut("icResetCard 8");
            // if (strcmp(szSW, "9000")!=0)
            if (!strSW.equals("9000")) {
                LogOut("读取数据错误！");
                return false;
            }
            if (szASW[0] == 0x61) {
                GetResponse[4] = szASW[1];
                LogOut("icResetCard 9");
                // memset(szHexReadData, 0, sizeof(szHexReadData));
                // [CMBCBank_cashway AscToHex:szHexReadData pSrc:(char
                // *)GetResponse SrcLen:sizeof(GetResponse)];

                szHexReadData = new char[GetResponse.length * 2];
                BJCWUtil.AscToHex(szHexReadData, GetResponse, GetResponse.length);
                String strCmd = new String(szHexReadData);
                // NSString *strCmd=[[NSString alloc]
                // initWithFormat:@"%s",szHexReadData];
                // mConnectionBle.sendApduIC:apduType apdu:strCmd replay:replay
                // sw:&sw timeOut:timeout];
                mConnectionBle.sendApduIC(apduType, strCmd, timeout, szApduReply);
                sw = szApduReply.getSW();
                replay = szApduReply.getRetData();
                LogOut("icResetCard 10");
                if (sw != 0X9000) {
                    LogOut("0102数据错误");
                    if (sw == 0x6984) {
                        LogOut("选择 0102 复位");
                        // bReadCard=false;
                    }
                    return false;
                }
                if (replay.length() < 4) {
                    LogOut("icReadCard 101 error 2");
                    return false;
                }
                // memset(szSW, 0, sizeof(szSW));
                // strcpy(szSW, &replay[strlen(replay)-4]);
                szSW = replay.substring(replay.length() - 4);
            }
        }
        while (true) {
            ReadRecord[3] = (byte) ((SFI << (byte) 3) | (byte) 0x04);
            LogOut("icResetCard 11");
            // memset(replay, 0, sizeof(replay));
            // memset(szHexReadData, 0, sizeof(szHexReadData));
            // [CMBCBank_cashway AscToHex:szHexReadData pSrc:(char *)ReadRecord
            // SrcLen:sizeof(ReadRecord)];
            szHexReadData = new char[ReadRecord.length * 2];
            BJCWUtil.AscToHex(szHexReadData, ReadRecord, ReadRecord.length);
            String strCmd = new String(szHexReadData);
            LogOut("icResetCard 12");
            // NSString *strCmd=[[NSString alloc]
            // initWithFormat:@"%s",szHexReadData];
            // mConnectionBle.sendApduIC:apduType apdu:strCmd replay:replay
            // sw:&sw timeOut:timeout];
            Log.e("YJL", "strCmd===" + strCmd);
            mConnectionBle.sendApduIC(apduType, strCmd, timeout, szApduReply);
            sw = szApduReply.getSW();
            replay = szApduReply.getRetData();

            if (sw != 0X9000) {
                LogOut("读取数据错误 1");
                if (sw == 0x6984) {
                    LogOut("读取数据错误 复位");
                    // bReadCard=false;
                }
                return false;
            }
            if (replay.length() < 4) {
                LogOut("icReadCard  数据错误");
                return false;
            }
            // memset(szSW, 0, sizeof(szSW));
            // strcpy(szSW, &replay[strlen(replay)-4]);

            // memset(szASW, 0, sizeof(szASW));
            // strcpy(szSW, &replay[strlen(replay)-4]);
            szSW = replay.substring(replay.length() - 4);
            LogOut("icResetCard 13");
            // [CMBCBank_cashway HexToAsc:szASW src:szSW
            // srcLen:(int)strlen(szSW)];

            szASW = new byte[szSW.length() / 2];
            BJCWUtil.HexToAsc(szASW, szSW.getBytes(), szSW.getBytes().length);
            if (szASW[0] == 0x6C) {
                // memcpy(ReadRecordFor6C, ReadRecord, 4);
                System.arraycopy(ReadRecord, 0, ReadRecordFor6C, 0, 4);
                LogOut("icResetCard 14");
                ReadRecordFor6C[4] = szASW[1];
                // memset(replay, 0, sizeof(replay));
                // memset(szHexReadData, 0, sizeof(szHexReadData));
                // [CMBCBank_cashway AscToHex:szHexReadData pSrc:(char
                // *)ReadRecordFor6C SrcLen:sizeof(ReadRecordFor6C)];

                szHexReadData = new char[ReadRecordFor6C.length * 2];
                BJCWUtil.AscToHex(szHexReadData, ReadRecordFor6C, ReadRecordFor6C.length);
                // NSString *strCmd=[[NSString alloc]
                // initWithFormat:@"%s",szHexReadData];
                // mConnectionBle.sendApduIC:apduType apdu:strCmd replay:replay
                // sw:&sw timeOut:timeout];
                String strCmd1 = new String(szHexReadData);
                mConnectionBle.sendApduIC(apduType, strCmd1, timeout, szApduReply);
                sw = szApduReply.getSW();
                replay = szApduReply.getRetData();
                if (sw != 0X9000) {
                    LogOut("读取数据错误 2");
                    if (sw == 0x6984) {
                        LogOut("读取数据错误 复位 2");
                        // bReadCard=false;
                    }
                    return false;
                }
                if (replay.length() < 4) {
                    LogOut("icReadCard  00B2 error 数据错误");
                    return false;
                }
                // memset(szSW, 0, sizeof(szSW));
                // strcpy(szSW, &replay[strlen(replay)-4]);
                szSW = replay.substring(replay.length() - 4);
            }
            LogOut("icResetCard 15");
            // if (strcmp(szSW, "9000")==0)
            if (szSW.equals("9000")) {
                // memcpy(szReply, replay, strlen(replay)-4);//去掉卡返回的数据
                szReply = replay.substring(0, replay.length() - 4);
                BJCWUtil.OutputLog("卡号数据:" + szReply);
                // int nReply=sizeof(szReply);
                int nReply = szReply.length();
                LogOut("icResetCard 16");
                bLen = 0;
                // char szASCReplyData[1024]={0};
                // 701557136216910103887847D25042200003690300000F
                byte[] szASCReplyData = new byte[nReply / 2];
                // nReply=[CMBCBank_cashway HexToAsc:szASCReplyData src:szReply
                // srcLen:nReply];
                BJCWUtil.HexToAsc(szASCReplyData, szReply.getBytes(), szReply.getBytes().length);
                LogOut("icResetCard 17");
                // ObjStrs=new ArrayList<STR_OBJ>();
                // STR_OBJ ObjStr = null;
                // GetObjStr((unsigned char *)szASCReplyData, (Byte)bLen, 0);
                GetObjStr(szASCReplyData, (byte) bLen, (byte) 0);
                // if (ObjStrs.size()>0)
                // {
                // ObjStr=ObjStrs.get(0);
                // }
                if (ObjStr.wTag != 0x70) {
                    LogOut("icReadCard  70Tag error ");
                    return false;
                }
                bLen += ObjStr.bOffset;
                while ((((Byte) szASCReplyData[bLen] == 0xFF) || (szASCReplyData[bLen] == 0x00)) && (bLen < nReply)) {
                    bLen++;
                }
                if (bLen >= nReply) {
                    ReadRecord[2]++;
                    continue;
                }
                while (bLen < nReply) {
                    // GetObjStr((unsigned char *)szASCReplyData, (Byte)bLen,
                    // 0);//数据存储，解析
                    GetObjStr(szASCReplyData, (byte) bLen, (byte) 0);
                    if (0x01 == ObjStr.bObjType) {
                        bLen += ObjStr.bOffset;
                        continue;
                    }
                    if (ObjStr.wTag == 0x57) {
                        byte CardNumber[] = new byte[ObjStr.wLen];
                        int nCardLen = 0;

                        // char szHexCardNumber[1024]={0};
                        char[] szHexCardNumber = new char[CardNumber.length * 2];
                        // memcpy(CardNumber,
                        // &szASCReplyData[bLen+ObjStr.bOffset], ObjStr.wLen);
                        System.arraycopy(szASCReplyData, bLen + ObjStr.bOffset, CardNumber, 0, ObjStr.wLen);
                        // [CMBCBank_cashway AscToHex:szHexCardNumber pSrc:(char
                        // *)CardNumber SrcLen:(int)sizeof(CardNumber)];
                        BJCWUtil.AscToHex(szHexCardNumber, CardNumber, CardNumber.length);
                        String strCardNum = new String(szHexCardNumber);
                        LogOut("szHexCardNumber=====" + strCardNum);
                        // char *pCardNum=strstr(szHexCardNumber, "D");
                        // if (!pCardNum)
                        // {
                        // LogOut(TAG,"数据错误");
                        // return false;
                        // }
                        // nCardLen=(int)(strlen(szHexCardNumber)-strlen(pCardNum));
                        // memcpy(szCardNumber, szHexCardNumber, nCardLen);
                        // strcpy(szNum, szCardNumber);
                        int nIndex = strCardNum.indexOf("D");
                        if (nIndex == -1) {
                            LogOut("数据错误");
                            return false;
                        }
                        strICNum = strCardNum.substring(0, nIndex);
                        return true;
                    }
                    bLen += (byte) (ObjStr.bOffset + ObjStr.wLen);

                }
                ReadRecord[2]++;
                continue;
            }
            // else if (strcmp(szSW, "6A83")==0)
            else if (szSW.equals("6A83")) {
                SFI++;
                ReadRecord[2] = 1;
                continue;
            } else {
                return false;
            }
        }
    }

    private class pbocReadInfoAsynTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            boolean bReadCard = false;
            strICNum = "";
            SWdataBean.SetCommId(DataBean.CMD_PBOCREADINFO_OPERATION);
            SWdataBean.SetCardNum("");
            LogOut("pbocReadInfoAsynTask IN");
            APDUReplyData szReply = new APDUReplyData();
            int nRet = 0;
            int nCout = 0;
            while ((bReadCard == false) && (++nCout < 3)) {
                nRet = mConnectionBle.sendApduIC(bEventMask, "poweron", nTime, szReply);
                if ((nRet != 0x000C) && (!ConnenctionBlueTooth.bCancle) && (nRet != 0x6985) && (nCout == 2)) {
                    mConnectionBle.sendApdu("FB810001000000", nCancletimout, szCancleReply);
                }
                if (nRet == 0x9000) {
                    LogOut("开始读取卡号的信息");
                    bReadCard = icReadCard(bEventMask, nTime);
                }
            }
            nRet = mConnectionBle.sendApdu("FB810001000000", nCancletimout, szCancleReply);
            if (nRet != 0x9000) {
                if (nRet == 0x6300) {
                    LogOut("数据错误！");
                    SWdataBean.SetSucceed("false");
                    SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_DATAERROR);// (StrCode);
                    SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_DATAERROR);
                } else if (nRet == 0x6f00) {
                    LogOut("时间超时");
                    SWdataBean.SetSucceed("false");
                    SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_TIMEOUT);// (StrCode);
                    SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_TIMEOUT);
                } else if ((nRet == 0x6d00) || (nRet == 0x6985)) {
                    LogOut("取消");
                    bOperatCancleFast = false;
                    SWdataBean.SetCardNum("");
                    return 2;
                } else {
                    SWdataBean.SetSucceed("false");
                    SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_FIVE);// (StrCode);
                    SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_FIVE);
                }
                return -1;
            }
            if (bReadCard) {
                LogOut("pbocReadInfoAsynTask 获取卡号成功");
                SendtoUIMessage(strICNum + "\r\n" + SWdataBean.ICcardInfo, 3);
                SWdataBean.SetCardNum(strICNum);
            } else {
                SWdataBean.SetSucceed("false");
                SWdataBean.SetErrCode("");// (StrCode);
                SWdataBean.SetErrMsg("");
                return -1;
            }
            bOperatCancleFast = true;
            SWdataBean.SetSucceed("true");
            SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCIDE_SUCESS);// (StrCode);
            SWdataBean.SetErrMsg("");
            return 0;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        @SuppressWarnings("unused")
        protected void onPostExecute(Integer result) {
            if (result == 2) {

                LogOut("取消 不返回操作");
                return;
            }
            bIsCancleApdu = false;
            LogOut("pbocReadInfoAsynTask End");
            IDataBeanCallback.postData(SWdataBean);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }


    public void pbocReadInfo(int slot, int timeout) {
        LogOut("pbocReadInfo IN");
        if (slot == 0) {
            //接触卡
            bEventMask = 0x20;
        } else {
            //非接触卡
            bEventMask = 0x10;
        }
        nTime = timeout;
        bIsCancleApdu = true;
        if (null != cancleAsynTask && !cancleAsynTask.isCancelled()
                && cancleAsynTask.getStatus() == AsyncTask.Status.RUNNING) {
            cancleAsynTask.cancel(true);
            cancleAsynTask = null;
        }
        pbocReadInfoAsynTask = new pbocReadInfoAsynTask();
        pbocReadInfoAsynTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private int WaitEventCard(byte bApduType, int timeout) {
        int nRet = 0;
        LogOut("WaitEventCard IN");
        APDUReplyData szReply = new APDUReplyData();
        if (bApduType == 0x20) {
            LogOut("接触卡*******");
            SWdataBean.SetCardType(2);
        } else if (bApduType == 0x10) {
            LogOut("非接触卡*******");
            SWdataBean.SetCardType(4);
        }
        nRet = mConnectionBle.sendApduIC(bApduType, "poweron", timeout, szReply);
        if ((nRet != 0x000C) && (!ConnenctionBlueTooth.bCancle) && (nRet != 0x6985)) {
            mConnectionBle.sendApdu("FB810001000000", nCancletimout, szCancleReply);
        }
        if (nRet != 0x9000) {
            if (nRet == 0x6300) {
                LogOut("数据错误！");
                SWdataBean.SetSucceed("false");
                SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_DATAERROR);// (StrCode);
                SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_DATAERROR);
            } else if (nRet == 0x6f00) {
                LogOut("时间超时");
                SWdataBean.SetSucceed("false");
                SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_TIMEOUT);// (StrCode);
                SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_TIMEOUT);
            } else if ((nRet == 0x6d00) || (nRet == 0x6985)) {
                LogOut("取消");
                bOperatCancleFast = false;
                return 2;
            } else {
                SWdataBean.SetSucceed("false");
                SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_FIVE);// (StrCode);
                SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_FIVE);
            }
            LogOut("WaitEventCard End");
            return -1;
        }
        if (bApduType == 0x20) {
            LogOut("接触卡成功*******");
            SWdataBean.SetICCardRestDate(szReply.getRetData());
        } else if (bApduType == 0x10) {
            if (szReply.getRetData().length() < 20) {
                SWdataBean.SetSucceed("false");
                SWdataBean.SetICCardRestDate("");
                SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCODE_DATAERROR);// (StrCode);
                SWdataBean.SetErrMsg(CONST_PARAM.RT_ERRMSG_DATAERROR);
                return -1;
            }
            LogOut("非接触卡成功*******");
            SWdataBean.SetICCardRestDate(szReply.getRetData().substring(12, 20));
        }
        bOperatCancleFast = true;
        SWdataBean.SetSucceed("true");
        SWdataBean.SetErrCode(CONST_PARAM.RT_ERRCIDE_SUCESS);// (StrCode);
        SWdataBean.SetErrMsg("");
        LogOut("WaitEventCard End");
        return 0;
    }


    private void GetObjStr(byte[] dwAddrInit, byte Index, byte bOption) {
        byte bOffset;
        byte bObjType;
        int wTag;
        int wLen;

        // Byte bBuf[5];
        byte bBuf[] = new byte[5];
        byte i = 0;
        // Byte dwAddr[sizeof((char *)dwAddrInit)-Index];
        byte[] dwAddr = new byte[dwAddrInit.length - Index];
        // memcpy(dwAddr,&dwAddrInit[Index], sizeof((char *)dwAddr));
        System.arraycopy(dwAddrInit, Index, dwAddr, 0, dwAddr.length);

        // if (sizeof((char *)dwAddr) >= 5)//dwAddr.Length
        if (dwAddr.length >= 5) {
            // memcpy(bBuf, dwAddr, 5);
            System.arraycopy(dwAddr, 0, bBuf, 0, 5);
        } else {
            // memcpy(bBuf, dwAddr, sizeof(dwAddr));
            System.arraycopy(dwAddr, 0, bBuf, 0, dwAddr.length);
        }

        if (((bBuf[0] & 0x20) == 0x20) && ((bOption == 0) || ((bOption == 1) && (bBuf[0] != 0xFF))))
            bObjType = (byte) OBJ_TYPE_STR;
        else
            bObjType = (byte) OBJ_TYPE_BAS;

        if (0x1F == (bBuf[0] & 0x1F)) {
            wTag = (int) ((bBuf[0] << 8) | bBuf[++i]);
            i++;
            bOffset = 0x03;
        } else {
            wTag = (int) ((0x00FF) & bBuf[i++]);
            bOffset = 0x02;
        }
        if (0x81 == bBuf[i]) {
            wLen = (int) (bBuf[i + 1]);
            bOffset += 0x01;
        } else if (0x82 == bBuf[i]) {
            wLen = (int) (bBuf[i + 1] << 8 | bBuf[i + 2]);
            bOffset += 0x02;

        } else {
            wLen = (int) (bBuf[i]);
        }
        // ObjStrs.add(new STR_OBJ(bOffset, bObjType, wTag, wLen));
        ObjStr = new STR_OBJ(bOffset, bObjType, wTag, wLen);
    }

    private class waitEventAsynTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            SWdataBean.SetCommId(DataBean.CMD_WAITEEVENT_OPERATION);
            LogOut("waitEventAsynTask IN");
            int nRet = 0;

         /*   if (bEventMask == 0x01) {
                // 等待刷卡
                LogOut("waitEvent 等待刷卡");
                nRet = WaitEventSwipeCard(nTime);
            } else if (bEventMask == 0x02) {
                // 等待插卡事件*/
            LogOut("waitEvent 等待插卡事件");
            nRet = WaitEventCard((byte) 0x20, nTime);
            /*
            } else if (bEventMask == 0x04) {
                // 射频
                LogOut("waitEvent 等待射频");
                nRet = WaitEventCard((byte) 0x10, nTime);
            } else if (bEventMask == 0xF) {
                LogOut("waitEvent 所有事件");
                nRet = WaitEventAll(nTime);
            } else if (bEventMask == (0x01 | 0x02)) {
                LogOut("waitEvent 等待刷卡或者插卡事件");
                nRet = WaitEventCardOrSwipe(nTime);
            } else {
                LogOut("暂未此等待事件标识");
            }*/
            return nRet;
        }

    }

    public void closeBlueTooth() {
        mConnenctionBlueTooth.close();
    }
}
