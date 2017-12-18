package cwbj.cwsdk2.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import cwbj.cwsdk2.util.PubUtils;
import cwbj.cwsdk2.R;
import cwbjsdk.cwsdk.util.BJCWUtil;

public class BLECentralActivity extends Activity implements
        BluetoothAdapter.LeScanCallback, OnClickListener {


    final static String TAG = "BTCW";

    private BluetoothAdapter btAdapter;
    private BluetoothGatt gatt;
    private List<BluetoothGattService> serviceList;
    private List<BluetoothGattCharacteristic> characterList;

    TextView info;
    String infoStr = "";
    Button scanbtn = null;
    Button sendbtn = null;
    Button unpaire = null;


    ListView lvBTDevices;
    ArrayAdapter<String> adtDevices;
    List<String> lstDevices = new ArrayList<String>();
    BluetoothAdapter btAdapt;
    BluetoothDevice btDev;

    BluetoothDevice g_btDev;

    Context g_Context;


    public final static UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public final static UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public final static UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public final static UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public final static UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public final static UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public final static UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    BluetoothGattCharacteristic mTxChar;
    BluetoothGattCharacteristic mRxChar;
    BluetoothGatt mBluetoothGatt = null;
    StringBuilder builder = new StringBuilder();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            builder = new StringBuilder();
//            byte[] Ver = BJCWUtil.StrToHex(msg.obj.toString());
//            String ShowVer = new String(Ver);
            String message = msg.obj.toString();
            if (PubUtils.COMMAND_CURRENT.equals(PubUtils.COMMAND_IDCARD_1)) {
//                identitys.parserData(message, message.length());
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IDCARD_2));
            } else {
                byte[] Ver = BJCWUtil.StrToHex(message);
                String verson = new String(Ver);
                Toast.makeText(getApplicationContext(), "" + verson, Toast.LENGTH_SHORT).show();
            }
            Log.e("YJL", "服务端发送过来的数据length==" + msg.obj.toString().length() + "服务端发送过来的数据" + msg.obj.toString());

        }
    };
    private Button magnetic;
    private Button btn_keymi;
    private Button btn_keyming;
    private Button ic_noncantact;
    private Button ic_contact;
    private Button identity;
//    private ParserIdentity identitys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bluetoothManager.getAdapter();

        this.setContentView(R.layout.ble_central);
        super.onCreate(savedInstanceState);


        info = (TextView) findViewById(R.id.textView3);
        scanbtn = (Button) findViewById(R.id.button2);
        sendbtn = (Button) findViewById(R.id.button3);


        unpaire = (Button) findViewById(R.id.button4);
        magnetic = ((Button) findViewById(R.id.magnetic));
        btn_keymi = ((Button) findViewById(R.id.key_mi));
        btn_keyming = ((Button) findViewById(R.id.key_ming));
        ic_noncantact = ((Button) findViewById(R.id.ic_nocontact));
        ic_contact = ((Button) findViewById(R.id.ic_contact));
        identity = ((Button) findViewById(R.id.identity));
        magnetic.setOnClickListener(this);
        btn_keymi.setOnClickListener(this);
        btn_keyming.setOnClickListener(this);
        ic_noncantact.setOnClickListener(this);
        ic_contact.setOnClickListener(this);
        identity.setOnClickListener(this);
        lvBTDevices = (ListView) findViewById(R.id.listView1);
        adtDevices = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, lstDevices);
        lvBTDevices.setAdapter(adtDevices);
//        identitys = new ParserIdentity();
//        identitys.mContext = this;

        g_Context = this;

        lvBTDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub

                if (btAdapt.isDiscovering()) btAdapt.cancelDiscovery();

                String str = lstDevices.get(arg2);
                String[] values = str.split("\\|");
                String address = values[2];
                info.append(address + " \n");


                if (!BluetoothAdapter.checkBluetoothAddress(address)) {

                    info.append(" 地址无效\n");
                    return;
                } else {
                    info.append(" 地址有效\n");

                }
                btDev = btAdapt.getRemoteDevice(address);


                if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
                    try {
                        Method createBondMethod = BluetoothDevice.class.getMethod("createBond");

                        info.append("开始配对... \n");

                        Boolean returnValue = (Boolean) createBondMethod.invoke(btDev);

                        if (returnValue) {
                            info.append("配对成功... \n");
                            //lstDevices.add(arg2,"已配对|"+values[1]+"|"+ values[2]);
                            //adtDevices.notifyDataSetChanged();
                        } else {
                            info.append("配对失败... \n");

                        }


                    } catch (NoSuchMethodException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
                    info.setText("");
                    //connect(btDev);
                    g_btDev = btDev;

                    gatt = btDev.connectGatt(g_Context, false, gattCallback);

                    Log.i("BTCW", "connectGatt ... \n");

                }


            }


        });


        scanbtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


                //textview1.setText("建立蓝牙对象......");
                info.setText("建立蓝牙对象......\n");


                btAdapt = BluetoothAdapter.getDefaultAdapter();

                if (btAdapt.isEnabled()) {
                    info.append("本机蓝牙可用! \n");

                } else {
                    info.append("本机蓝牙不可用! \n");
                }


                //注册Receiver来获取蓝牙设备相关的结果

                IntentFilter bluetoothOIntent = new IntentFilter();

                bluetoothOIntent.addAction(BluetoothDevice.ACTION_FOUND);
                bluetoothOIntent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
                bluetoothOIntent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                bluetoothOIntent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

                registerReceiver(searchDevices, bluetoothOIntent);

                btAdapter.startDiscovery();    //开启搜索蓝牙；


            }
        });


        sendbtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //获取版本号

                PubUtils.COMMAND_CURRENT = PubUtils.COMMAND_VERSION;
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_VERSION));


            }
        });


        unpaire.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter != null) {

                    if (!adapter.isEnabled()) {
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(intent);
                    }
                    Set<BluetoothDevice> devices = adapter.getBondedDevices();
                    if (devices.size() > 0) {
                        for (Iterator<BluetoothDevice> it = devices.iterator(); it.hasNext(); ) {
                            BluetoothDevice device = (BluetoothDevice) it.next();
                            info.append("解除配对：" + device.getName() + "\n");
                        }
                    } else {
                        info.append("还没有已配对的远程蓝牙设备！");
                    }
                } else {
                    info.append("本机没有蓝牙设备！");
                }
            }

        });


    }


    public void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.i(TAG, "unpaire devices failed");
        }

    }

    private void writeRXCharacteristic(byte[] value) {

        BluetoothGattCharacteristic mRxChar = null;
        BluetoothGattService mRxService;

        //if (mRxChar == null) {
        mRxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        Log.i(TAG, "mBluetoothGatt :" + mBluetoothGatt);
        Log.i(TAG, "     value :" + PubUtils.ByteArrayToHex(value));
        if (mRxService == null) {
            Log.i(TAG, "Rx service not found!");
            // broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mRxChar = mRxService.getCharacteristic(RX_CHAR_UUID);
        if (mRxChar == null) {
            Log.i(TAG, "Rx charateristic not found!");
            // broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        //}


        mBluetoothGatt.setCharacteristicNotification(mRxChar, true);

        Log.i(TAG, "writeRXCharacteristic 1");
        mRxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        Log.i(TAG, "writeRXCharacteristic 2");
        mRxChar.setValue(value);
        Log.i(TAG, "writeRXCharacteristic 3");
        boolean status = mBluetoothGatt.writeCharacteristic(mRxChar); // 手机是中央，但在GATT中是客户端，�?��发�?是写属�?�?
        //BluetoothGattDescriptor d = mRxChar.getDescriptor(mRxChar.getUuid());
        //  d.setValue(value);
        //mBluetoothGatt.writeDescriptor(d);
        if (status) {
            Log.i(TAG, "writeRXCharacteristic OK!");
        } else {
            Log.i(TAG, "writeRXCharacteristic failed!");

        }


    }


    private void writeRXCharacteristic_continue(byte[] value) {


        Log.i(TAG, "writeRXCharacteristic 1");
        mRxChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        Log.i(TAG, "writeRXCharacteristic 2");
        mRxChar.setValue(value);
        Log.i(TAG, "writeRXCharacteristic 3");
        boolean status = mBluetoothGatt.writeCharacteristic(mRxChar); // 手机是中央，但在GATT中是客户端，�?��发�?是写属�?�?

        if (status) {
            Log.i(TAG, "writeRXCharacteristic OK!");
        } else {
            Log.i(TAG, "writeRXCharacteristic failed!");

        }


    }


    BroadcastReceiver searchDevices = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub


            String action = arg1.getAction();

            Bundle b = arg1.getExtras();

            Object[] lstName = b.keySet().toArray();

            String str;

            // 显示所有收到的消息及其细节
            //for (int i = 0; i < lstName.length; i++) {

            //	String keyName = lstName[i].toString();
            //	et.append(keyName);
            //}


            BluetoothDevice device = null;
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                device = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (device.getBondState() == BluetoothDevice.BOND_NONE) {

                    //if(device.getName()=="123456")
                    {
                        str = "未配对|" + device.getName() + "|" + device.getAddress();

                        //et.append(str);
                        if (lstDevices.indexOf(str) == -1)// 防止重复添加
                        {

                            lstDevices.add(str);

                            adtDevices.notifyDataSetChanged();
                        }
                    }
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {

                    str = "已配对|" + device.getName() + "|" + device.getAddress();
                    if (lstDevices.indexOf(str) == -1)// 防止重复添加
                    {

                        lstDevices.add(str);

                        adtDevices.notifyDataSetChanged();
                    }
                }

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {


                device = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        str = "正在配对...|" + device.getName() + "|" + device.getAddress();
                        lstDevices.add(str);
                        adtDevices.notifyDataSetChanged();
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        str = "已配对|" + device.getName() + "|" + device.getAddress();
                        lstDevices.add(str);
                        adtDevices.notifyDataSetChanged();
                        break;
                    case BluetoothDevice.BOND_NONE:
                        str = "取消配对...|" + device.getName() + "|" + device.getAddress();
                        lstDevices.add(str);
                        adtDevices.notifyDataSetChanged();
                    default:
                        break;
                }


            }


        }


    };


    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onStop() {
        btAdapter.stopLeScan(this);
        super.onStop();
    }

    public void onButtonClicked(View v) {
        ShowInfo("Chris", "onButtonClicked");
        btAdapter.startLeScan(this);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        btAdapter.stopLeScan(this);
        gatt = device.connectGatt(this, true, gattCallback);
        mBluetoothGatt = gatt;
        ShowInfo("Chris", "Device Name:" + device.getName());

    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

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

                } else {
                    Log.i("BTCW", "enableTXNotification failed ");

                }
            }
            //super.onServicesDiscovered(gatt, status);
        }


        public String byte2HexStr(byte[] b) {
            String stmp = "";
            StringBuilder sb = new StringBuilder("");
            if (b != null) {
                for (int n = 0; n < b.length; n++) {
                    stmp = Integer.toHexString(b[n] & 0xFF);
                    sb.append(stmp.length() == 1 ? "0" + stmp : stmp);
                }
            }
            return sb.toString().toUpperCase().trim();
        }


        boolean enableTXNotification() {
            /*
             * if (mBluetoothGatt == null) { L.e(TAG,"mBluetoothGatt null" +
			 * mBluetoothGatt); broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
			 * return; }
			 */
            Log.i(TAG, "enableTXNotification()");

            mTxChar = null;
            mRxChar = null;

            BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID); // 通过服务的UUID获得服务
            if (RxService == null) {
                Log.i(TAG, "Rx service not found!");
                // broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                return false;
            }
            //BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);


            BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(RX_CHAR_UUID);
            if (TxChar == null) {
                Log.i(TAG, "Tx charateristic not found!");
                // broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                return false;
            }

            mBluetoothGatt.setCharacteristicNotification(TxChar, true);

//            BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
//            if (null != descriptor) {
//                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//                mBluetoothGatt.writeDescriptor(descriptor);
//            }

            mTxChar = TxChar;

            mRxChar = RxService.getCharacteristic(RX_CHAR_UUID);

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
            //super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.i("BTCW", "onCharacteristicChanged");
            Log.e("YJL", "服务器数据过来了");
//            super.onCharacteristicChanged(gatt, characteristic);
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
            if (pulSW == 0x9000) {
                Log.e("YJL", "result===" + result.length());
                String strReply = result.substring(6, result.length() - 4);// 4
                Message msg = new Message();
                msg.obj = strReply;
                mHandler.sendMessage(msg);
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

    };


    void ShowInfo(String st, String s) {
        //infoStr  = infoStr + s  + "\n";
        info.append(s);
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        refresh();
        mBluetoothGatt.disconnect();
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.identity:
                //身份证
                PubUtils.COMMAND_CURRENT = PubUtils.COMMAND_IDCARD_1;
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IDCARD_1));
                break;
            case R.id.magnetic:
                //磁条卡
                PubUtils.COMMAND_CURRENT = PubUtils.COMMAND_MAGNETIC;
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_MAGNETIC));
                break;
            case R.id.key_mi:
                //密文
                PubUtils.COMMAND_CURRENT = PubUtils.COMMAND_PIN_CIPHERTEXT_1;
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_PIN_CIPHERTEXT_1));
                break;
            case R.id.key_ming:
                //明文
                PubUtils.COMMAND_CURRENT = PubUtils.COMMAND_PIN_PLAINTEXT;
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_PIN_PLAINTEXT));
                break;
            case R.id.ic_contact:
                //接触
                PubUtils.COMMAND_CURRENT = PubUtils.COMMAND_IC_CONTACT_1;
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_CONTACT_1));
                break;
            case R.id.ic_nocontact:
                //非接触
                PubUtils.COMMAND_CURRENT = PubUtils.COMMAND_IC_NOCONTACT_1;
                writeRXCharacteristic(BJCWUtil.StrToHex(PubUtils.COMMAND_IC_NOCONTACT_2));
                break;
        }
    }

    public boolean refresh() {
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
}
