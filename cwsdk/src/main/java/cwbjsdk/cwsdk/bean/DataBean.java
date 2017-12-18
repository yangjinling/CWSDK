package cwbjsdk.cwsdk.bean;

import android.graphics.Bitmap;
import android.icu.util.ICUUncheckedIOException;

/**
 * Created by leonn on 2017/5/31.
 */

public class DataBean {



    private  String   CurrentBlueToothDevicesName;

    public static final int CMD_FINDDEVICE_OPERATION=0;
    public static final int CMD_CONNECTDEVICE_OPERATION=1;//connectBT
    public static final int CMD_DESTROYDEVICE_OPERATION=2;
    public static final int CMD_OPERATION_CANCEL=3;
    public static final int CMD_IDEREADMESSAGE_OPERATION=4;
    public static final int CMD_SWIPCAPECARD_OPERATION=5;
    public static final int CMD_ICRESETCARD_OPERATION=6;//icResetCard
    public static final int CMD_WAITEEVENT_OPERATION=7;
    public static final int CMD_PBOCREADINFO_OPERATION=8;

    public static final int CMD_TRANSFERCARDONE_OPERATION=9;
    public static final int CMD_TRANSFERCARDTOW_OPERATION=10;
    public static final int CMD_OPERACARDCASETOIC_OPERATION=11;
    public static final int CMD_OPERAREADERTOCARDLIP_OPERATION=12;
    public static final int CMD_CARDSEND_OPERATION=13;
    public static final int CMD_CARDAPDU_OPERATION=14;

    public static final int CMD_BLUTOOTH_STATE=15;//蓝牙连接状态

    public static final int CMD_IMATE_LEVEL=16;//盒子电量
    public static final int CMD_IMATE_FIGNERPRINT=17;//iMateCollectFignerPrint
    public static final int CMD_IMATE_FIGNERFEATURES=18;//Features
    public static final int CMD_IMATE_PWD=19;
    public static final int CMD_IMATE_PWDIMPORTKEY=20;//PwdImportKey
    public static final int CMD_IMATE_PWDREADKEY=21;//PwdReadKey
    public static final int CMD_IMATE_GETSATUS=22;//iMateGetStatus
    public static final int CMD_IMATE_PRINT=23;
    /**
     * @param args
     */
    private int commId;

    // 响应时间戳，yyyyMMddHHmmss
    private String timeStamp;

    //指令成功标志：true成功，false失败
    private String succeed;

    // 指令返回码,0000成功，否则失败，没有报文体
    private String errCode;

    // 指令返回信息，正确信息为空，错误信息要有值
    private String errMsg;

    //盒子列表 以";"分割
    private String deviceList;

    //身份证信息
    private String information;
    //身份证照片
    private Bitmap photo;

    //二磁道数据
    private String track2;
    //三磁道数据
    private String track3;

    //IC卡复位信息
    private String ICCardRestDate;

    //卡片类型：1检测到刷卡；2检测到IC卡；4检测到射频卡
    private int cardType;

    //卡号
    private String cardNum;
    private String strImateName;//盒子名称
    private String strPrintName;//蓝牙 名称
    private String blePrintState;//打印机连接状态
    private String bleState="";//蓝牙设备连接状态

    private long level;//电量
    private String fingerData;//指纹采集数据
    private String featureData;//指纹特征数据
    private String pwd;//密码
    private String key;//密钥
    private String status;

    public String ICcardInfo = null;

    public void SetCommId(int commId) {
        this.commId=commId;
    }
    public int GetCommId()
    {
        return commId;
    }

    public void SetTimeStamp(String timeStamp) {
        this.timeStamp=timeStamp;
    }
    public String GetTimeStamp()
    {
        return timeStamp;
    }

    public void SetErrCode(String errCode) {
        this.errCode=errCode;
    }
    public String GetErrCode()
    {
        return errCode;
    }

    public void SetSucceed(String succeed) {
        this.succeed=succeed;
    }
    public String GetSucceed()
    {
        return succeed;
    }

    public String GetErrMsg() {
        return errMsg;
    }
    public void SetErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public String GetDeviceList() {
        return deviceList;
    }
    public void SetDeviceList(String deviceList) {
        this.deviceList = deviceList;
    }

    public String GetInformation(){
        return information;
    }
    public void SetInformation(String information) {
        this.information = information;
    }

    public Bitmap GetPhoto(){
        return photo;
    }
    public void SetPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public String GetTrack2() {
        return track2;
    }
    public void SetTrack2(String track2) {
        this.track2 = track2;
    }

    public String GetTrack3() {
        return track3;
    }
    public void SetTrack3(String track3) {
        this.track3 = track3;
    }

    public String GetICCardRestDate() {
        return ICCardRestDate;
    }
    public void SetICCardRestDate(String ICCardRestDate) {
        this.ICCardRestDate = ICCardRestDate;
    }

    public int GetCardType() {
        return cardType;
    }
    public void SetCardType(int cardType) {
        this.cardType = cardType;
    }

    public String GetCardNum() {
        return cardNum;
    }
    public void SetCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public long GetLevel() {
        return level;
    }

    public void SetLevel(long level) {
        this.level = level;
    }

    public String GetFingerData(){
        return fingerData;
    }

    public void SetFingerData(String fingerData) {
        this.fingerData = fingerData;
    }

    public String GetFeatureData(){
        return featureData;
    }

    public void SetFeatureData(String featureData) {
        this.featureData = featureData;
    }
    public String GetPwd(){
        return pwd;
    }

    public void SetPwd(String pwd) {
        this.pwd = pwd;
    }

    public String GetKey(){
        return key;
    }

    public void SetKey(String key) {
        this.key = key;
    }

    public String GetStatus(){
        return status;
    }

    public void SetStatus(String status) {
        this.status = status;
    }

    public String GetImateName(){
        return strImateName;
    }
    public void SetImateName(String strImateName) {
        this.strImateName = strImateName;
    }

    public String GetPrintName(){
        return strPrintName;
    }
    public void SetPrintName(String strPrintName) {
        this.strPrintName = strPrintName;
    }

    public String GetBleState() {
        return bleState;
    }

    public void SetBleState(String bleState) {
        this.bleState = bleState;
    }

    public String GetPrintState(){
        return blePrintState;
    }
    public void SetPrintState(String blePrintState) {
        this.blePrintState = blePrintState;
    }

    public String GetCurrentBlueToothDevicesName(){
        return CurrentBlueToothDevicesName;
    }

    public void SetCurrentBlueToothDevicesName(String CurrentBlueToothDevicesName) {
        this.CurrentBlueToothDevicesName = CurrentBlueToothDevicesName;
    }



}
