package cwbjsdk.cwsdk.util;

/**
 * Created by leonn on 2017/5/31.
 */

public class CONST_PARAM {
    public final static int RT_INVALID_PARAMETER = 0x8000000E;   //无效参数
    public final static int RT_SENDDATA_FAILED = 0x8000000A;   //发送失败
    public final static int RT_BLUETOOTH_FAILED = 0x8000000B;   //蓝牙失败

    public final static String RT_ERRCIDE_SUCESS = "0000";
    public final static String RT_ERRCODE_ONE = "0001";
    public final static String RT_ERRMSG_ONE = "本设备不具备蓝牙功能";
    public final static String RT_ERRCODE_TOW = "0002";
    public final static String RT_ERRMSG_TOW = "搜索蓝牙设备失败";
    public final static String RT_ERRCODE_THREE = "0003";
    public final static String RT_ERRMSG_THREE = "未搜索蓝牙设备";
    public final static String RT_ERRCODE_TOUR = "0004";
    public final static String RT_ERRMSG_TOUR = "创建蓝牙通信失败";
    public final static String RT_ERRCODE_FIVE = "0005";
    public final static String RT_ERRMSG_FIVE = "蓝牙通信错误";
    public final static String RT_ERRMSG_PRINT = "0006";

    public final static String RT_ERRCODE_PARAMETER = "0006";
    public final static String RT_ERRMSG_PARAMETER = "参数错误";
    public final static String RT_ERRCODE_DATAERROR = "0007";
    public final static String RT_ERRMSG_DATAERROR = "数据错误";
    public final static String RT_ERRCODE_FINDFAILED = "0008";
    public final static String RT_ERRMSG_FINDFAILED = "寻卡失败";
    public final static String RT_ERRCODE_CANCLEOPERA = "0009";
    public final static String RT_ERRMSG_CANCLEOPERA = "操作取消";
    public final static String RT_ERRCODE_TIMEOUT = "0010";
    public final static String RT_ERRMSG_TIMEOUT = "操作超时";
    public final static String RT_ERRCODE_OPEARTION = "0011";
    public final static String RT_ERRMSG_OPEARTION = "你已拔除卡片或者取消操作，此次操作失败";
    public final static String RT_ERRCODE_CONNECTION = "0012";
    public final static String RT_ERRMSG_CONNECTION = "连接参数错误";
    public final static String RT_ERRCODE_CARDTYPE = "0013";
    public final static String RT_ERRMSG_CARDTYPE = "暂时不支持此类型的卡";

    public final static String RT_ERRCODE_BLE = "0014";
    public final static String RT_ERRMSG_BLE = "蓝牙未连接";
    public final static int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
}
