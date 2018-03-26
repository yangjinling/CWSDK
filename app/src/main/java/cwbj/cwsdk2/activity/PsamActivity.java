package cwbj.cwsdk2.activity;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import cwbj.cwsdk2.R;
import cwbjsdk.cwsdk.bean.DataBean;
import cwbjsdk.cwsdk.sdk.CWSDK;
import cwbjsdk.cwsdk.util.DataBeanCallback;

public class PsamActivity extends AppCompatActivity {
    private static CWSDK cwsdk = CWSDK.getInstance();

    String sMAC = "00:00:00:00";
    BluetoothDevice WorkDev = null;
    private final int timeout = 20;
    private Button psam1;
    private Button psam2;
    Handler bluetoothHandler = null;
    private ImageView btn_back;
    private TextView tv_title;
    private ProgressBar prograssBar;
    private TextView tv_content;
    private Button cancle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psam);
        psam1 = ((Button) findViewById(R.id.psam1));
        psam2 = ((Button) findViewById(R.id.psam2));
        tv_content = ((TextView) findViewById(R.id.psam_content));
        btn_back = ((ImageView) findViewById(R.id.btn_back));
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_title = ((TextView) findViewById(R.id.main_title));
        cancle = ((Button) findViewById(R.id.cancle));
        tv_title.setText(R.string.action_pasm);
        prograssBar = ((ProgressBar) findViewById(R.id.prograss));
        sMAC = null;
        sMAC = getIntent().getStringExtra("MAC");
        bluetoothHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0x6) {
                    prograssBar.setVisibility(View.GONE);
                    tv_content.setText("pasm内容为：：：：" + msg.obj.toString());
                }
            }
        };
        WorkDev = cwsdk.GetBlueToothDevicesByMAC(sMAC);
        cwsdk.initialize(PsamActivity.this, true, bluetoothHandler);
        cwsdk.workmode = 1;
        cwsdk.IDataBeanCallback = new DataBeanCallback() {
            @Override
            public Boolean postData(DataBean dataBean) {
                // TODO Auto-generated method stub
//                GetCmdId(dataBean);
                return null;
            }
        };
        psam1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prograssBar.setVisibility(View.VISIBLE);
                cwsdk.connectionDevice(WorkDev);
                cwsdk.getPsam(0);
            }
        });
        psam2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prograssBar.setVisibility(View.VISIBLE);
                cwsdk.connectionDevice(WorkDev);
                cwsdk.getPsam(1);
            }
        });
//        cancle.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cwsdk.cancle(6);
//            }
//        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        cwsdk.finalize();
        cwsdk.StopDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
