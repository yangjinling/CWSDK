package  cwbjsdk.cwsdk.util;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.synjones.bluetooth.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import cwbjsdk.cwsdk.bean.IdentityInfo;
import cwbjsdk.cwsdk.bean.sexInfo;

/**
 * Created by leonn on 2017/5/31.
 */


public class ParserIdentity {


    private String strData = "01000400686767712959200020002000200020002000200020002000200020002000310030003300310039003800370030003900300036001753ac4e025e776dc06d3a531753ac4e97671a4e2759665b056e4e531c4eef8d33003500f75320002000200020002000200020002000200020002000200020002000200020003600340032003200320032003100390038003700300039003000360034003200310058001753ac4e025e6c51895b405c776dc06d0652405c200020002000200020003200300030003700300033003200300032003000310037003000330032003000200020002000200020002000200020002000200020002000200020002000200020002000574c66007e00320000ff85175151513e710c8692f08029b61a5df8a38b1286fe72c1cc94a47d35704e37b4a2515dd9a8e22569df240af38cde44f12cb90d73aaafcdbd5259693224dd46b693810876b7bc412551aedc5251515a3e80271352e034b55fc33eaafccd4cbe8326efceda52b6e0c607c3c24e5a847e550f1d1acf46278be3d6968fa665e94867ff44ad99aabe3e7c4c345b25ddf4df709f1f666ddfe7ff8002f6ae51916f885aabe5be57efe94ebc73b94a33d8f3538b1a7f0e43ecc2780ab6bfc3d34ec01f5d65dc10f66c1de02c1eadba1afbb1ced365dec7f25ba9083ed52b1acaf68e360e7c0fb6daee78aaf9f3c193f60cec240e4c217904aaeb6a11337c6b938c22ea267aa82288db5e33ceb1fd1d881fea9e368a430ee26a2f0e9911889c1e6c7db45191a25ec1be47db2591d3f2e422856bea94b5e4c7ebfeb581070f94b4c319556f623351616fa1d6757cace773f815ee53b3bfb2726c2199be17c8a2ef165aeba70fb9bfdb2142116692ca2b7c26def8ae51dabd6c84f582581071a0426de4b4b61c99ecae5126b1fad904deaa060d38d942a91d060fb52249b7e864c34053a8ac7dd20702b32c4e9b6bd5bf5aa0702bd66ff3b981ef02f3170ee5938f42390e3ffe02279df085bfd9fdf62998f446a73fb67dfad79e08978ff56f91e0c0a2c7b5deebe269faca72d29edd0d4dff5741c172b3497423dacbcb01cf9a9c5730e2b686645bc97ce3bd9ad23c4b41f38a09c2bba6477aaf61d94b9598a5dd795638c35f330eae51b35329c7a7be6c0ed6fbac318cf925682c764348093adb85f4a8dc0eb3db16d90b0b8310bd1d8f06477c85697625d8f8055074c7ea65ed310ca95877c5dd74d78d77b3511908164ebcebc424ae51f087325091e485f6221ffa05977eaaf1fc9905539fbb9ddbc896a164b260e733eb8463dc6fa035d64f101a39525fa65939c38d97d89f987433d34e821ae7c392dc4d227c06be1019640139469281bcce4f8f48b1f280fc85ac6c25ee0dfc8b2892c7b9633e37c1fdbea7477b0f095186111ee2034d67413643b63a35e3b2a974cd5f9f2a7c8753ae512583058c7493b20eb0342ad233fc15378adf49b9f108bb8c07c9782292de83635ba7ad906e1d64cedc7f42045bdf587a11c195f0bae21dd3c8b3f94a3ed34a43bde90b9c4d9e059a0f5f42f629dd5b11a51d503cdfaa967e9ac69c812f5a3e0b24f180ecce110a0ea37ca9783bb066cf63c4ab887a841bedb807ec25ea9b35bed9f831ff711024cba22d03c266fced94b39e05055ca3c8587dbf4023594d6f36dca033c0fd59f20bb9ccbe4d9021ac5a3e84bf47fac459b4495b36ed72d89cf4009598036b8247818a62ef1adb5641ca1ed126c257caf47c7b2527a697ac263b5588c5978131556d44274519d1d9611549a8bf6e6b06d86c9879";
    // 个人信息长度
    // 项目 长度（字节） 说明
    // 姓名 30 汉字
    // 性别 2 代码
    // 民族 4 代码
    // 出生 16 年月日：YYYYMMDD
    // 住址 70 汉字和数字
    // 公民身份号码 36 数字
    // 签发机关 30 汉字
    // 有效期起始日期 16 年月日：YYYYMMDD
    // 有效期截止日期 16 年月日：YYYYMMDD
    // 有效期为长期时存储“长期”
    // 最新住址 70 汉字和数字
    private String strName = new String();
    private String strSex = new String();
    private String strNation = new String();
    private String strBirth = new String();
    private String strAddress = new String();
    private String strIdCard = new String();
    private String strIssue = new String();
    private String strStartDate = new String();
    private String strEndDate = new String();
    private String strCurrentAddress = new String();

    private String strId = new String();
    private String strIdBmp = new String();
    // private String strFingerPrint = new String();
    // private String strRaw = new String();

    private String name = new String();
    private String sex = new String();
    private String nation = new String();
    private String birth = new String();
    // private String year = new String();
    // private String month = new String();
    // private String date = new String();
    private String address = new String();
    private String idCard = new String();
    private String issue = new String();
    private String startDate = new String();
    private String endDate = new String();
    private String currentAddress = new String();

    // private String id = new String();
    // private String idBmp = new String();
    // private String fingerPrint = new String();
    // private String raw = new String();

    public String strIdDate = new String();

    private ArrayList<IdentityInfo> g_IdentityInfos = null;
    private ArrayList<sexInfo> g_sexInfos = null;

    String strLog = new String();


    public Context mContext = null;
    String wltPath;
    String bmpPath;
    String idbmp;
    private Bitmap bmpUser;

    public ParserIdentity() {

        // 性别
        g_sexInfos = new ArrayList<sexInfo>();
        g_sexInfos.add(new sexInfo("0", "未知"));
        g_sexInfos.add(new sexInfo("1", "男"));
        g_sexInfos.add(new sexInfo("2", "女"));
        g_sexInfos.add(new sexInfo("9", "未说明"));
        g_sexInfos.add(new sexInfo("", ""));

        // 民族
        g_IdentityInfos = new ArrayList<IdentityInfo>();
        g_IdentityInfos.add(new IdentityInfo("01", "汉"));
        g_IdentityInfos.add(new IdentityInfo("02", "蒙古"));
        g_IdentityInfos.add(new IdentityInfo("03", "回"));
        g_IdentityInfos.add(new IdentityInfo("04", "藏"));
        g_IdentityInfos.add(new IdentityInfo("05", "维吾尔"));
        g_IdentityInfos.add(new IdentityInfo("06", "苗"));
        g_IdentityInfos.add(new IdentityInfo("07", "彝"));
        g_IdentityInfos.add(new IdentityInfo("08", "壮"));
        g_IdentityInfos.add(new IdentityInfo("09", "布依"));
        g_IdentityInfos.add(new IdentityInfo("10", "朝鲜"));
        g_IdentityInfos.add(new IdentityInfo("11", "满"));
        g_IdentityInfos.add(new IdentityInfo("12", "侗"));
        g_IdentityInfos.add(new IdentityInfo("13", "瑶"));
        g_IdentityInfos.add(new IdentityInfo("14", "白"));
        g_IdentityInfos.add(new IdentityInfo("15", "土家"));
        g_IdentityInfos.add(new IdentityInfo("16", "哈尼"));
        g_IdentityInfos.add(new IdentityInfo("17", "哈萨克"));
        g_IdentityInfos.add(new IdentityInfo("18", "傣"));
        g_IdentityInfos.add(new IdentityInfo("19", "黎"));
        g_IdentityInfos.add(new IdentityInfo("20", "傈僳"));
        g_IdentityInfos.add(new IdentityInfo("21", "佤"));
        g_IdentityInfos.add(new IdentityInfo("22", "畲"));
        g_IdentityInfos.add(new IdentityInfo("23", "高山"));
        g_IdentityInfos.add(new IdentityInfo("24", "拉祜"));
        g_IdentityInfos.add(new IdentityInfo("25", "水"));
        g_IdentityInfos.add(new IdentityInfo("26", "东乡"));
        g_IdentityInfos.add(new IdentityInfo("27", "纳西"));
        g_IdentityInfos.add(new IdentityInfo("28", "景颇"));
        g_IdentityInfos.add(new IdentityInfo("29", "柯尔克孜"));
        g_IdentityInfos.add(new IdentityInfo("30", "土"));
        g_IdentityInfos.add(new IdentityInfo("31", "达斡尔"));
        g_IdentityInfos.add(new IdentityInfo("32", "仫佬"));
        g_IdentityInfos.add(new IdentityInfo("33", "羌"));
        g_IdentityInfos.add(new IdentityInfo("34", "布朗"));
        g_IdentityInfos.add(new IdentityInfo("35", "撒拉"));
        g_IdentityInfos.add(new IdentityInfo("36", "毛南"));
        g_IdentityInfos.add(new IdentityInfo("37", "仡佬"));
        g_IdentityInfos.add(new IdentityInfo("38", "锡伯"));
        g_IdentityInfos.add(new IdentityInfo("39", "阿昌"));
        g_IdentityInfos.add(new IdentityInfo("40", "普米"));
        g_IdentityInfos.add(new IdentityInfo("41", "塔吉克"));
        g_IdentityInfos.add(new IdentityInfo("42", "怒"));
        g_IdentityInfos.add(new IdentityInfo("43", "乌孜别克"));
        g_IdentityInfos.add(new IdentityInfo("44", "俄罗斯"));
        g_IdentityInfos.add(new IdentityInfo("45", "鄂温克"));
        g_IdentityInfos.add(new IdentityInfo("46", "德昂"));
        g_IdentityInfos.add(new IdentityInfo("47", "保安"));
        g_IdentityInfos.add(new IdentityInfo("48", "裕固"));
        g_IdentityInfos.add(new IdentityInfo("49", "京"));
        g_IdentityInfos.add(new IdentityInfo("50", "塔塔尔"));
        g_IdentityInfos.add(new IdentityInfo("51", "独龙"));
        g_IdentityInfos.add(new IdentityInfo("52", "鄂伦春"));
        g_IdentityInfos.add(new IdentityInfo("53", "赫哲"));
        g_IdentityInfos.add(new IdentityInfo("54", "门巴"));
        g_IdentityInfos.add(new IdentityInfo("55", "珞巴"));
        g_IdentityInfos.add(new IdentityInfo("56", "基诺"));
        g_IdentityInfos.add(new IdentityInfo("", ""));

        bmpUser = null;

    }

    public boolean parserData(String data, int len) {
        int identityLen = 0;
        int identityBmpLen = 0;
        int nbeginIndex = 0;
        int nendIndex = 0;
        if (data.length() < 10 || len == 0) {
            System.out.println("数据为空");
            return false;
        }
        String strIdentityLen = data.substring(0, 4);
        String strIdentityBmpLen = data.substring(4, 8);
        identityLen = Integer.parseInt(strIdentityLen, 16) * 2;
        identityBmpLen = Integer.parseInt(strIdentityBmpLen, 16) * 2;
        strLog = String.format("identityLen:%d,identityBmpLen:%d", identityLen, identityBmpLen);
        System.out.println(strLog);

        nbeginIndex = 8;
        nendIndex = 8 + identityLen;
        strId = data.substring(nbeginIndex, nendIndex);
        System.out.println("id:" + strId);
        nbeginIndex += identityLen;
        nendIndex += identityBmpLen;
        strIdBmp = data.substring(nbeginIndex, nendIndex);
        System.out.println("idBmp:" + strIdBmp);
        setIdbmp(strIdBmp);
        parserIdData(strId, strId.length());
        return true;
    }

    private boolean parserIdData(String data, int len) {
        System.out.println("data:" + data);
        if (data.length() == 0 || len == 0) {
            System.out.println("数据为空");
            return false;
        }
        // 姓名
        int offset = 0;
        int datalen = 60;
        System.out.println("offset:" + offset + "datalen：" + datalen);
        // memcpy(sex,&pData[offset],datalen);
        strName = data.substring(offset, datalen);
        System.out.println("strName:" + strName);
        // 性别
        offset += datalen;
        datalen += 4;
        System.out.println("offset:" + offset + "datalen：" + datalen);
        // memcpy(sex,&pData[offset],datalen);
        strSex = data.substring(offset, datalen);
        System.out.println("strSex:" + strSex);
        // 民族
        offset += 4;
        datalen += 8;
        System.out.println("offset:" + offset + "datalen：" + datalen);
        // memcpy(nation,&pData[offset],datalen);
        strNation = data.substring(offset, datalen);
        System.out.println("strNation:" + strNation);
        // 出生
        offset += 8;
        datalen += 32;
        System.out.println("offset:" + offset + "datalen：" + datalen);
        // memcpy(birth,&pData[offset],datalen);
        strBirth = data.substring(offset, datalen);
        System.out.println("strBirth:" + strBirth);
        // 住址
        offset += 32;
        datalen += 140;
        System.out.println("offset:" + offset + "datalen：" + datalen);
        // memcpy(address,&pData[offset],datalen);
        strAddress = data.substring(offset, datalen);
        System.out.println("strAddress:" + strAddress);
        // 公民身份号码
        offset += 140;
        datalen += 72;
        System.out.println("offset:" + offset + "datalen：" + datalen);
        // memcpy(idCard,&pData[offset],datalen);
        strIdCard = data.substring(offset, datalen);
        System.out.println("strIdCard:" + strIdCard);
        // 签发机关
        offset += 72;
        datalen += 60;
        System.out.println("offset:" + offset + "datalen：" + datalen);
        // memcpy(issue,&pData[offset],datalen);
        strIssue = data.substring(offset, datalen);
        System.out.println("strIssue:" + strIssue);
        // 有效期起始日期
        offset += 60;
        datalen += 32;
        System.out.println("offset:" + offset + "datalen：" + datalen);
        // memcpy(startDate,&pData[offset],datalen);
        strStartDate = data.substring(offset, datalen);
        System.out.println("strStartDate:" + strStartDate);
        // 有效期截止日期
        offset += 32;
        datalen += 32;
        System.out.println("offset:" + offset + "datalen：" + datalen);
        // memcpy(endDate,&pData[offset],datalen);
        strEndDate = data.substring(offset, datalen);
        System.out.println("strEndDate:" + strEndDate);
        // 最新住址
        offset += 32;
        datalen += (len - offset);
        System.out.println("offset:" + offset + "datalen：" + datalen);
        if (datalen > 0) {
            // currentAddress =[[NSData alloc] initWithBytes:&pData[offset]
            // length:datalen];
            strCurrentAddress = data.substring(offset, datalen);
            System.out.println("strCurrentAddress:" + strCurrentAddress);
        }
        transform();
        strIdDate = IdInfo();
        return true;
    }

    private boolean transform() {
        name = unicodeTOString(AssembledDate(strName));
        System.out.println("name:" + name);

        sex = unicodeTOString(AssembledDate(strSex));
        // System.out.println("sex:" + sex);
        for (int i = 0; i < g_sexInfos.size(); i++) {
            sexInfo sexInfoParm = g_sexInfos.get(i);
            if (sexInfoParm.index.equals(sex)) {
                sex = sexInfoParm.name;
                break;
            }
        }
        System.out.println("sex:" + sex);

        nation = unicodeTOString(AssembledDate(strNation));
        // System.out.println("nation:" + nation);

        for (int i = 0; i < g_IdentityInfos.size(); i++) {
            IdentityInfo IdentityInfoParm = g_IdentityInfos.get(i);
            // System.out.println(IdentityInfoParm.index+IdentityInfoParm.name);
            if (IdentityInfoParm.index.equals(nation)) {
                nation = IdentityInfoParm.name;
                break;
            }
        }
        System.out.println("nation:" + nation);

        birth = unicodeTOString(AssembledDate(strBirth));
        System.out.println("birth:" + birth);

        address = unicodeTOString(AssembledDate(strAddress));
        System.out.println("address:" + address);

        idCard = unicodeTOString(AssembledDate(strIdCard));
        System.out.println("idCard:" + idCard);

        issue = unicodeTOString(AssembledDate(strIssue));
        System.out.println("issue:" + issue);

        startDate = unicodeTOString(AssembledDate(strStartDate));
        System.out.println("startDate:" + startDate);

        endDate = unicodeTOString(AssembledDate(strEndDate));
        System.out.println("endDate:" + endDate);

        currentAddress = unicodeTOString(AssembledDate(strCurrentAddress));
        System.out.println("currentAddress:" + currentAddress);
        return true;
    }

    private String IdInfo() {

        StringBuilder strBuilId = new StringBuilder();
        String strDate = new String();
        strBuilId.append(name);
        strBuilId.append(";");
        strBuilId.append(sex);
        strBuilId.append(";");
        strBuilId.append(nation);
        strBuilId.append(";");
        strBuilId.append(birth);
        strBuilId.append(";");
        strBuilId.append(address);
        strBuilId.append(";");
        strBuilId.append(idCard);
        strBuilId.append(";");
        strBuilId.append(issue);
        strBuilId.append(";");
        if (startDate.length() == 8) {
            strDate = startDate.substring(0, 4) + "." + startDate.substring(4, 6) + "." + startDate.substring(6, 8) + "-";
            // +endDate.substring(0,4)+"."+endDate.substring(4,6)+"."+endDate.substring(6,8);
        }
        if (endDate.length() == 8) {
            strDate += endDate.substring(0, 4) + "." + endDate.substring(4, 6) + "." + endDate.substring(6, 8);
        } else {
            if (endDate.equals("长期")) {
                strDate += "长期";
            }
        }
        strBuilId.append(strDate);
        strBuilId.append(";");
        strBuilId.append(currentAddress);
        System.out.println("strId:" + strBuilId.toString());
        return strBuilId.toString();
    }

    private void setBmpUser(Bitmap bmpUser) {
        this.bmpUser = bmpUser;
    }

    public Bitmap getBmpUser() {
        return bmpUser;
    }


    public void setIdbmp(String idbmp) {
        this.idbmp = idbmp;
        System.out.println(idbmp);
        if (idbmp != "") {
            decodeWltStep(idbmp);
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(new File(bmpPath));
                this.setBmpUser(BitmapFactory.decodeStream(fis));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int decodeWltStep(String str) {
        //  bmpPath = Environment.getExternalStorageDirectory() +
        //  "/wltlib/zp.bmp";
        // wltPath = Environment.getExternalStorageDirectory() +
        // "/wltlib/zp.wlt";
        bmpPath = mContext.getFileStreamPath("zp.bmp").getAbsolutePath();
        wltPath = mContext.getFileStreamPath("photo.wlt").getAbsolutePath();
        try {
            System.out.println("decode wlt begin");
            File wltFile = new File(wltPath);
            if (wltFile.exists())
                System.out.println("创建成功");
            System.out.println("decode wlt step 1");
            FileOutputStream fos = new FileOutputStream(wltFile);
            System.out.println("decode wlt step 2");
            fos.write(BJCWUtil.StrToHex(str));
            fos.close();

            if (wltFile.exists())
                System.out.println("创建成功");

            System.out.println("decode wlt step 3");
            DecodeWlt dw = new DecodeWlt();
            System.out.println("decode wlt step 4");
            System.out.println("wltPath:" + wltPath + "-----bmpPath:" + bmpPath);
            int result = dw.Wlt2Bmp(wltPath, bmpPath);
            System.out.println("decode wlt finish");
            BJCWUtil.OutputLog("图片解析---" +
                    "result======" + result);
            return result;

        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("photo decode error:" + ioe.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("photo decode error:" + e.getMessage());
        }
        return 0;
    }

    // java 需要把UNICODE数据 反转一下 再转码
    private String AssembledDate(String strData) {
        StringBuilder data = new StringBuilder();
        if (strData.length() % 4 != 0) {
            return null;
        }
        int nCount = strData.length() / 4;
        int nbeginIndex = 0;
        int nendIndex = 0;
        for (int i = 0; i < nCount; i++) {
            nbeginIndex = i * 4;
            nendIndex = (i + 1) * 4;
            data.append(reverseStr(strData.substring(nbeginIndex, nendIndex)));
        }
        return data.toString();
    }

    private String reverseStr(String strData) {
        StringBuilder data = new StringBuilder();
        if (strData.length() != 4) {
            return null;
        }
        data.append(strData.substring(2, 4));
        data.append(strData.substring(0, 2));
        return data.toString();
    }

    private String unicodeTOString(String strData) {

        String str = new String();
        int nbeginIndex = 0;
        int nendIndex = 0;
        StringBuffer string = new StringBuffer();
        // String[] hex = unicode.split("\\\\u");
        int nCount = strData.length() / 4;
        for (int i = 0; i < nCount; i++) {
            // 转换出每一个代码点
            nbeginIndex = i * 4;
            nendIndex = (i + 1) * 4;
            str = strData.substring(nbeginIndex, nendIndex);
            int data = Integer.parseInt(str, 16);
            // 追加成string
            string.append((char) data);
        }
        return string.toString();
    }

}
