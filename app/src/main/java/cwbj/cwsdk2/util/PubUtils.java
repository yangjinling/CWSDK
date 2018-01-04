package cwbj.cwsdk2.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import cwbjsdk.cwsdk.bean.APDUReplyData;
import cwbjsdk.cwsdk.bean.PbocDataElementsClass;
import cwbjsdk.cwsdk.bean.STR_OBJ;
import cwbjsdk.cwsdk.util.BJCWUtil;

public class PubUtils {
    public final static UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public final static UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");


    public static boolean isBle = false;//经典蓝牙还是ble蓝牙   false：经典蓝牙  true：ble蓝牙
    public static boolean isWifi = false;
    public static String COMMAND_CURRENT = "";
    public static boolean isMi = false;//是否为获取密文   false：明文 true：密文
    public static boolean isContact = false;//接触还是非接  false：非接 true：接触
    //获取版本指令
    public static String COMMAND_VERSION = "F80007FB812000000000";
    //IC_接触卡指令
    public static String COMMAND_IC_CONTACT_1 = "EE0011FB80201400000A62000000000000000000";
    public static String COMMAND_IC_CONTACT_2 = "E2001DFB8020140000166F0C000000000000000000A4040007A0000003330101";
    public static String COMMAND_IC_CONTACT_3 = "E90016FB80201400000F6F05000000000000000000C0000047";
    public static String COMMAND_IC_CONTACT_4 = "E90016FB80201400000F6F05000000000000000000B2010C00";
    public static String COMMAND_IC_CONTACT_5 = "E90016FB80201400000F6F05000000000000000000B2010C1A";
    public static String COMMAND_IC_CONTACT_6 = "F80007FB810005000000";
    public static String COMMAND_IC_CONTACT[] = new String[]{COMMAND_IC_CONTACT_1, COMMAND_IC_CONTACT_2, COMMAND_IC_CONTACT_3, COMMAND_IC_CONTACT_4, COMMAND_IC_CONTACT_5, COMMAND_IC_CONTACT_6};
    //IC_非接触卡指令
    public static String COMMAND_IC_NOCONTACT_1 = "EE0011FB80101400000A62000000000000000000";
    public static String COMMAND_IC_NOCONTACT_2 = "E2001DFB8010140000166F0C000000000000000000A4040007A0000003330101";
    public static String COMMAND_IC_NOCONTACT_3 = "E90016FB80101400000F6F05000000000000000000B2010C00";
    public static String COMMAND_IC_NOCONTACT_4 = "F80007FB810005000000";
    public static String COMMAND_IC_NOCONTACT[] = new String[]{COMMAND_IC_NOCONTACT_1, COMMAND_IC_NOCONTACT_2, COMMAND_IC_NOCONTACT_3, COMMAND_IC_NOCONTACT_4};

    //身份证指令
    public static String COMMAND_IDCARD_1 = "F80007FB801114000000";
    public static String COMMAND_IDCARD_2 = "F80007FB810005000000";
    public static String COMMAND_IDCARD[] = new String[]{COMMAND_IDCARD_1, COMMAND_IDCARD_1};
    //指纹模块版本指令
    public static String COMMAND_FINGERVERSION_1 = "FB803400000000";
    public static String COMMAND_FINGERVERSION_2 = "FB80350500000C000009020004090000000D03";
    public static String COMMAND_FINGERVERSION_3 = "00000000000000";
    //指纹模块获取指令
    public static String COMMAND_FINGERMODE_1 = "FB803400000000";//
    public static String COMMAND_FINGERMODE_2 = "FB80350500000D00000A0200051b000000001e03";
    public static String COMMAND_FINGERMODE_3 = "FB80350500000D00000A0200051b000000011f03";
    public static String COMMAND_FINGERMODE_4 = "FB80350500000D00000A0200051b000000021c03";
    public static String COMMAND_FINGERMODE_5 = "FB80350500000C0000090200041c0300001B03";
    public static String COMMAND_FINGERMODE_6 = "00000000000000";
    //指纹特征获取指令
    public static String COMMAND_FINGERFEATURE_1 = "FB803400000000";
    public static String COMMAND_FINGERFEATURE_2 = "FB80350500000C0000090200040C0100000903";
    public static String COMMAND_FINGERFEATURE_3 = "00000000000000";

    //磁条卡指令
    public static String COMMAND_MAGNETIC = "F80007FB800014000000";
    //明文指令
    public static String COMMAND_PIN_PLAINTEXT = "F5000aFB802314000003010002";
    ////密文指令
    public static String COMMAND_PIN_CIPHERTEXT_1 = "F0000FFB8025140000080000000000000000";
    public static String COMMAND_PIN_CIPHERTEXT_2 = "F5000AFB802314000003010002";

    public static String ip = "172.20.10.3";

    // ///////// StringToHex:String转成byte[],再将每个byte转成两个char表示的hex�?
    public static String ByteArrayToHex(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret.toUpperCase();
    }

    public static String StringToHex(String strSrc) {
        byte[] tmp = strSrc.getBytes(); // 有疑问，当为不可见字符得到的都是0x3f？？
        return ByteArrayToHex(tmp);
    }

    // StringToHexString没有补位0
    public static String StringToHexString(String strPart) {
        String hexString = "";
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString = hexString + strHex;
        }
        return hexString;
    }

    // //////// HexToString:先将每两个ASCII字符合成�?��字节然后转成byte，再将每个byte转成对应的字符串
    // 注意到byte的取值范围为-127~128
    public static String HexToString(String s) {
        if (s.length() % 2 != 0)// 如果长度为奇数返回为空，说明输入有错
            return null;

        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                // baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(
                // i * 2, i * 2 + 2), 16));
                int n = 0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
                baKeyword[i] = (byte) n;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 把byte转化成string，必须经过编�?
        try {
            // s = new String(baKeyword, "utf-8");// UTF-16le:Not
            s = new String(baKeyword);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return s;
    }

    // //////// StrToHex,将形如�?3a5867”转化成�?x3A�?x58�?x67�?
    // 注意传入str之前�?��将小写转成大�?
    public static byte[] StrToHex(String str) {
        if (str.length() % 2 != 0)
            return null;
        byte[] ret = new byte[str.length() / 2];
        StringBuffer buf = new StringBuffer(2);
        int j = 0;
        for (int i = 0; i < str.length(); i++, j++) {
            buf.insert(0, str.charAt(i));
            buf.insert(1, str.charAt(i + 1));
            int t = Integer.parseInt(buf.toString(), 16);
            ret[j] = (byte) t;
            i++;
            buf.delete(0, 2);
        }
        return ret;
    }

    public static byte[] StrToHexBT(String str) {
        if (str.length() % 2 != 0)
            return null;
        byte[] ret = new byte[(str.length() / 2) + 3];
        StringBuffer buf = new StringBuffer(2);
        int j = 0;

        ret[j++] = (byte) 'C';
        ret[j++] = (byte) ((str.length() / 2) >> 8);
        ret[j++] = (byte) ((str.length() / 2));

        for (int i = 0; i < str.length(); i++, j++) {
            buf.insert(0, str.charAt(i));
            buf.insert(1, str.charAt(i + 1));
            int t = Integer.parseInt(buf.toString(), 16);
            ret[j] = (byte) t;
            i++;
            buf.delete(0, 2);
        }
        return ret;
    }

    // ///////// HexTostr,将�?0x3A�?x58�?x67”变成�?3A5876�?
    public static String HexTostr(byte[] hex) {
        String str = "";
        if (hex.length == 0)
            return str;
        for (int i = 0; i < hex.length; i++) {
            String buf = Integer.toString(hex[i] & 0xFF, 16);// & 0xFF去掉负号
            if (buf.length() != 2)// 补位
            {
                str += "0" + buf;
            } else
                str += buf;
        }
        return str;
    }

    // //////////////�?6进制数转换为字符串并按要求补位，�?x123输出占四个字节的为�?0123�?
    public static String PaddingHexintToString(int src, int totalsize) {
        String target;
        target = Integer.toString(src, 16);
        if (target.length() > totalsize)
            return null;
        while (target.length() < totalsize) {
            target = "0" + target;
        }
        return target;
    }

    // ////////byte数组与int类型的转换，在socket传输中，发�?、�?接收的数据都是byte数组，但是int类型�?个byte组成的，如何把一个整形int转换成byte数组，同时如何把�?��长度�?的byte数组转换为int类型,�?��两个�?��的算法：
    // 与VC中高低位顺序恰好相反2008.11.26
    public static byte[] int2byte(int res) {
        byte[] targets = new byte[4];
        targets[0] = (byte) (res & 0xff);// �?���?
        targets[1] = (byte) ((res >> 8) & 0xff);// 次低�?
        targets[2] = (byte) ((res >> 16) & 0xff);// 次高�?
        targets[3] = (byte) (res >>> 24);// �?���?无符号右移�?
        return targets;
    }

    public static int byte2int(byte[] res) {
        // �?��byte数据左移24位变�?x??000000，再右移8位变�?x00??0000
        int targets = (res[0] & 0xff) | ((res[1] << 8) & 0xff00) // | 表示安位�?
                | ((res[2] << 24) >>> 8) | (res[3] << 24);
        return targets;
    }

    public static void Memcpy(byte[] output, byte[] input, int outpos, int inpos, int len) {
        int i;
        for (i = 0; i < len; i++)
            output[outpos + i] = input[inpos + i];
    }

    // 路径格式�?path = "file:///e:/log/log.txt";
    public static String FcopVersion() {
        return System.getProperty("microedition.io.file.FileConnection.version");
    }

    // 读文�?
    public static byte[] showFile(String fileName) {

		/*
         * FileConnection fc = null; byte buf[] = null; try { fc =
		 * (FileConnection) Connector.open(fileName); if (!fc.exists())// 文件是否存在
		 * throw new IOException("file no exists"); // fc.setReadable(true);
		 * InputStream is = fc.openInputStream(); int len = (int)
		 * fc.availableSize(); if (len == 0) len = 1024; byte bOut[] = new
		 * byte[len]; int length = is.read(bOut, 0, len); buf = new
		 * byte[length]; Memcpy(buf, bOut, 0, 0, length); is.close(); } catch
		 * (IOException ex) { System.out.println(ex.toString()); }
		 */
        File file = new File(fileName);
        try {
            BufferedReader br = new BufferedReader(new java.io.FileReader(file));
            StringBuffer sb = new StringBuffer();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            br.close();
            return sb.toString().getBytes();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    // 修改文件
    public static void modifyFile(String path, byte[] newData) {
        byte[] buf = showFile(path);
        int len = buf.length + newData.length;
        byte Data[] = new byte[len];
        Memcpy(Data, buf, 0, 0, buf.length);
        Memcpy(Data, newData, buf.length, 0, newData.length);
        // 先删再写
        DeleteFile(path);
        WriteFile(path, Data);
    }

    // 删除文件
    public static void DeleteFile(String path) {

		/*
         * try { FileConnection fc = (FileConnection) (Connector.open(path)); if
		 * (!fc.exists()) throw new IOException("file exists"); fc.delete(); }
		 * catch (Exception e) { System.out.println("saveFileErr:" +
		 * e.toString()); }
		 */
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File Not Exist");
            return;
        }
        if (file.delete()) {

        } else {
            System.out.println("File Delete Fail");

        }

    }

    // 创建文件,如果文件存在直接删除不保留原文件数据
    public static void WriteFile(String path, byte[] fileData) {
        /*
         * try { FileConnection fc = (FileConnection) (Connector.open(path)); if
		 * (fc.exists()) DeleteFile(path); fc.create(); fc.setWritable(true);
		 * OutputStream os = fc.openOutputStream(); os.write(fileData);
		 * os.close(); } catch (Exception e) { System.out.println("saveFileErr:"
		 * + e.toString()); }
		 */
        File file = new File(path);
        if (file.exists()) {
            DeleteFile(path);
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*
             * if (!file.setWritable(true)) {
			 * 
			 * System.out.println("Set writalbe fail");
			 * 
			 * }
			 */
        }
        try {
            OutputStream os = new FileOutputStream(file);
            os.write(fileData);
            os.close();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

    public static void Writelog(String str) {
        String path = "file:///e:/log/log.txt";
        byte[] buf = str.getBytes();
        WriteFile(path, buf);
    }

    public static void Modifylog(String str) {
        String path = "file:///e:/log/log.txt";
        byte[] buf = str.getBytes();
        modifyFile(path, buf);
    }

    // public static byte[] reverse(byte[]bInData){
    // byte[]buf=new byte[bInData.length];
    // for(int i=0;i<bInData.length;i++)
    // {
    // buf[i]=bInData[bInData.length-i];
    // }
    // return buf;
    // }
    public static void reverse(byte[] pbData) {
        byte temp;
        int l, b, c;
        l = pbData.length / 2;
        for (b = 0, c = pbData.length - 1; b < l; b++, c--) {
            temp = pbData[b];
            pbData[b] = pbData[c];
            pbData[c] = temp;
        }
    }

    // 得到APDU指令返回的数据data即RetDataAPDU中的变量retData；nOff即取偏移；nDatalen即从偏移位置起要使用的数据的长度
    public static String GetApduResponse(byte[] bData, int nOff, int nDatalen) {
        String strRetData = "";
        if (bData == null)
            return strRetData;
        if ((nOff < 0) || (nDatalen < 0) || (nOff + nDatalen > bData.length))
            return strRetData;

        int nRet = 0;
        for (int i = 0; i < nDatalen; i++) {
            if (bData[nOff + i] < 0)// 由于byte取�?范围�?127~128
                nRet = 256 + bData[nOff + i];
            else
                nRet = bData[nOff + i];

            if (nRet < 0x10)// 补位，占两位
                strRetData += ("0");
            strRetData += (Integer.toHexString(nRet) + " ");
            if (i % 8 == 7)// 分行显示，每行显�?个字�?
                strRetData = strRetData.concat("\n");
        }
        return strRetData;
    }

    // 得到设备的状态码,即返回数据的�?��四位,占两个字�?
    public static int GetDeviceStateResponse(byte[] retData, int nretDatalen) {
        byte[] b = new byte[2];
        PubUtils.Memcpy(b, retData, 0, nretDatalen - 2, 2);
        String buf = PubUtils.ByteArrayToHex(b);
        if (nretDatalen == 0x80)
            PubUtils.WriteLogHex("b", b);
        int n = Integer.parseInt(buf, 16);
        return n;
    }

    // 得到设备的返回�?
    public static String GetApudReturnValue(byte[] retData, int nOff, int nretDatalen) {
        byte[] b = new byte[nretDatalen - 2];
        PubUtils.Memcpy(b, retData, 0, nOff, nretDatalen - 2);
        String buf = PubUtils.ByteArrayToHex(b);
        return buf;
    }

    //
    public static String TrimSpace(String strMsg) {
        String buf = strMsg.trim();//
        String ret = "";
        for (int i = 0; i < buf.length(); i++) {
            if (buf.charAt(i) != ' ')
                ret += buf.charAt(i);
        }
        return ret;
    }

    /*
     * 函数说明：saveFile保存日志信息
     */
    public static void saveFile(String path, byte[] fileData) {

        OutputStream os = null;
        File file = new File(path);
        if (file.exists()) {
            int size = (int) file.length();
            size = size + 20;

        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os = new FileOutputStream(file);
            os.write(fileData);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

		/*
         *
		 * try { FileConnection fc = (FileConnection) (Connector.open(path,
		 * Connector.READ | Connector.WRITE)); if (fc.exists()) { int size =
		 * (int) fc.fileSize(); size = size + 20; os =
		 * fc.openOutputStream(size); os.write(fileData); os.close(); }
		 * fc.create(); os = fc.openOutputStream(); os.write(fileData);
		 * os.close(); } catch (Exception e) {
		 * 
		 * }
		 */

    }

    public static int uchar_ByteToInt(byte a, byte b) {
        int r;
        if ((a & 0x80) == 0) {
            r = 0;
        } else {
            r = 0x8000;
        }

        if ((b & 0x80) == 0) {
            r += 0;
        } else {
            r += 0x80;
        }

        a &= 0x7F;
        b &= 0x7F;

        r += (a * 0x100 + b);

        return r;
    }

    public static void writeFileByBytes(String fileName, byte[] buf, int nbuflen, boolean append) {

        File file = new File(fileName);
        OutputStream out = null;
        try {
            // �?��读一个字�?
            out = new FileOutputStream(file, append);
            // out.write(buf);
            out.write(buf, 0, nbuflen);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                }
            }
        }
    }

    public static String p = "/sdcard/log/android.txt";

    public static void WriteLog(String strTag, byte[] fileData) {
        OutputStream os = null;
        File file = new File(p);

        try {
            os = new FileOutputStream(file, true);
            os.write("\n".getBytes());
            os.write(strTag.getBytes());
            os.write(" ".getBytes());
            os.write(fileData);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void WriteLogHex(String fileName, byte[] fileData) {
        /*
         * String a = ByteArrayToHex(fileData);
		 * 
		 * OutputStream os; try { FileConnection fc = (FileConnection)
		 * (Connector.open(p, Connector.READ | Connector.WRITE)); if
		 * (fc.exists()) { int size = (int) fc.fileSize(); size = size + 20; os
		 * = fc.openOutputStream(size); os.write("\n".getBytes());
		 * os.write(fileName.getBytes()); os.write("\n".getBytes());
		 * os.write(a.getBytes()); os.close(); } fc.create(); os =
		 * fc.openOutputStream(); os.write("\n".getBytes());
		 * os.write(fileName.getBytes()); os.write("\n".getBytes());
		 * os.write(a.getBytes()); os.close(); } catch (Exception e) {
		 * 
		 * }
		 * 
		 * }
		 */
        String a = ByteArrayToHex(fileData);
        OutputStream os = null;
        File file = new File(fileName);
        if (file.exists()) {
            int size = (int) file.length();
            size = size + 20;

        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            os = new FileOutputStream(file);
            os.write("\n".getBytes());
            os.write(fileName.getBytes());
            os.write("\n".getBytes());
            os.write(a.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public static int HexToAsc(byte[] pDst, byte[] pSrc, int nSrcLen) {
        int n = 0, m = 0;
        for (int i = 0; i < nSrcLen; i += 2) {

            if (pSrc[m] >= '0' && pSrc[m] <= '9') {
                pDst[n] = (byte) ((pSrc[m] - '0') << 4);
            } else if (pSrc[n] >= 'A' && pSrc[n] <= 'F') {
                pDst[n] = (byte) ((pSrc[m] - 'A' + 10) << 4);
            } else {
                pDst[n] = (byte) ((pSrc[m] - 'a' + 10) << 4);
            }

            m++;


            if (pSrc[m] >= '0' && pSrc[m] <= '9') {
                pDst[n] |= pSrc[m] - '0';
            } else if (pSrc[m] >= 'A' && pSrc[m] <= 'F') {
                pDst[n] |= pSrc[m] - 'A' + 10;
            } else {
                pDst[n] |= pSrc[m] - 'a' + 10;
            }

            n++;
            m++;
        }

        return nSrcLen / 2;
    }

    public static int AscToHex(char[] pDst, byte[] pSrc, int SrcLen) {
        String tab = "0123456789ABCDEF";
        int n = 0, m = 0;
        for (int i = 0; i < SrcLen; i++) {
            pDst[n++] = (char) tab.charAt((char) ((pSrc[m] >> 4) & 0x0f));
            pDst[n++] = (char) tab.charAt(pSrc[m] & 0x0f);
            m++;
        }


        return SrcLen * 2;
    }

    private static String szReply;

    private static void GetObjStr(byte[] dwAddrInit, byte Index, byte bOption) {
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

    private static STR_OBJ ObjStr;

    private static int OBJ_TYPE_BAS = 0;
    private static int OBJ_TYPE_STR = 1;

    public static String getCardNum(String replay) {
        while (true) {
            int bLen = 0;
            byte ReadRecord[] = {0x00, (byte) 0xB2, 0x01, 0x00, 0x00};
            byte[] GetResponse = {0x00, (byte) 0xC0, 0x00, 0x00, 0x00};
            byte ReadRecordFor6C[] = {0x00, (byte) 0xB2, 0x01, 0x00, 0x00};
            byte SFI = 1;
            szReply = replay.substring(0, replay.length() - 4);
            BJCWUtil.OutputLog("卡号数据:" + szReply);
            // int nReply=sizeof(szReply);
            int nReply = szReply.length();
            Log.e("YJL", "icResetCard 16");
            bLen = 0;
            // char szASCReplyData[1024]={0};
            // 701557136216910103887847D25042200003690300000F
            byte[] szASCReplyData = new byte[nReply / 2];
            // nReply=[CMBCBank_cashway HexToAsc:szASCReplyData src:szReply
            // srcLen:nReply];
            BJCWUtil.HexToAsc(szASCReplyData, szReply.getBytes(), szReply.getBytes().length);
            Log.e("yjl", "icResetCard 17");
            // ObjStrs=new ArrayList<STR_OBJ>();
            // STR_OBJ ObjStr = null;
            // GetObjStr((unsigned char *)szASCReplyData, (Byte)bLen, 0);
            GetObjStr(szASCReplyData, (byte) bLen, (byte) 0);
            // if (ObjStrs.size()>0)
            // {
            // ObjStr=ObjStrs.get(0);
            // }
            if (ObjStr.wTag != 0x70) {
                Log.e("YJL", "icReadCard  70Tag error ");
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
                    Log.e("YJL", "szHexCardNumber=====" + strCardNum);
                    int nIndex = strCardNum.indexOf("D");
                    if (nIndex == -1) {
                        Log.e("YJL", "数据错误");
                        return "数据错误";
                    }
                    String strICNum = strCardNum.substring(0, nIndex);
                    return strICNum;
                }
                bLen += (byte) (ObjStr.bOffset + ObjStr.wLen);

            }
            ReadRecord[2]++;
            continue;
        }
    }

    public static String sendApdu(String cmd, int time) {
        BJCWUtil.OutputLog("sendApdu IN");
        String strLog = new String();
        String strSW = new String();
        String strReply = new String();
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
        return strDate;
    }

    public static String sendApduIc(byte apduType, String apdu, int time) {
        int nLen = apdu.length() / 2;
        int cmdType = 0x6F;
        BJCWUtil.OutputLog("sendApduIC IN");
        int nApduType = 1;
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
        return strCmd;
    }

    //判断数据是否完整
    public static boolean judgeData(String result) {
        Log.e("YJL", "result===" + result.length());
        String strReplyLen = result.substring(2, 6);// strReadDate
        int nstrReplyLen = Integer.parseInt(strReplyLen, 16);
        Log.e("YJL", "nstrReplyLen==" + nstrReplyLen);
        int nReplyLen = result.length() - 6;// strReadDate
        Log.e("YJL", "nReplyLen==" + nReplyLen);
        if (nstrReplyLen * 2 != nReplyLen) {
            BJCWUtil.OutputLog("ConnectedThread***************数据不完整");
            return false;
        } else {
            BJCWUtil.OutputLog("ConnectedThread***************数据完整");
            return true;
        }
    }

    /**
     * 根据图片字节数组，对图片可能进行二次采样，不致于加载过大图片出现内存溢出
     *
     * @param bytes
     * @return
     */

    public static Bitmap getBitmapByBytes(byte[] bytes) {

//对于图片的二次采样,主要得到图片的宽与高

        int width = 0;

        int height = 0;

        int sampleSize = 1;//默认缩放为1

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inJustDecodeBounds = true;//仅仅解码边缘区域

//如果指定了inJustDecodeBounds，decodeByteArray将返回为空

        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

//得到宽与高

        height = options.outHeight;

        width = options.outWidth;

//图片实际的宽与高，根据默认最大大小值，得到图片实际的缩放比例

        while ((height / sampleSize > Cache.IMAGE_MAX_HEIGH)

                || (width / sampleSize > Cache.IMAGE_MAX_WIDTH)) {

            sampleSize *= 2;

        }

//不再只加载图片实际边缘

        options.inJustDecodeBounds = false;

//并且制定缩放比例

        options.inSampleSize = sampleSize;

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);

    }

//默认大小

    class Cache {

        public static final int IMAGE_MAX_HEIGH = 854;

        public static final int IMAGE_MAX_WIDTH = 480;

    }

    public static Bitmap bitmap;

    public static Bitmap decodeSampledBitmapFromByteArray(byte[]data,
                                                          int reqWidth, int reqHeight, Rect outPadding){

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        Log.i("outPadding", "---------");
        return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


}
