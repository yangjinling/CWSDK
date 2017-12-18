package cwbjsdk.cwsdk.bean;

import android.util.Log;

import cwbjsdk.cwsdk.util.BJCWUtil;

/**
 * Created by leonn on 2017/6/26.
 */

public class PbocDataElementsClass {

    public String AnalysisDataStr;
    public String[] RestAnalysisDataStr = new String[32];
    public int Index_RestAnalysisDataStr = 0;
    public int Index_PBOCDataElements = 0;

    public String ResultInfoShow = "";

    PBOCDataElements gPBOCDataElements = new PBOCDataElements();

    public PbocDataElementsClass() {

    }

    public int setAnalysisData(String str) {
        AnalysisDataStr = str;
        return 0;
    }

    public String getAnalysisData() {
        return AnalysisDataStr;
    }

    public int GetPBOCDataLength(String Str) {

        if (Str.equals("1")) {
            return 1;
        } else if (Str.equals("2")) {
            return 2;
        } else {
            return 0;
        }

    }

    public int CheckIsTag(String val) {

        int i = 0;
        String slenmode = "";
        if (val.length() == 2) {
            slenmode = "1";
        } else if (val.length() == 4) {
            slenmode = "2";
        }

        for (i = 0; i < gPBOCDataElements.Get_pbocDataElements_Length(); i++) {

            if (slenmode.equals(gPBOCDataElements.pbocDataElements[i][0])) {
                if (val.equals(gPBOCDataElements.pbocDataElements[i][1])) {
                    return 1;
                }
            }
        }
        return 0;
    }

    public int AnalysisOneDataElements(String Str) {
        int i = 0;
        int mlength = 0;
        int Tlength = 0;
        String strTag = "";
        String debud_temp = "";
        for (i = 0; i < gPBOCDataElements.Get_pbocDataElements_Length(); i++) {

            mlength = GetPBOCDataLength(gPBOCDataElements.pbocDataElements[i][0]);

            if (Str.length() <= 2) {
                return -1;
            }

            if (mlength == 1) {
                strTag = Str.substring(0, 2);
            } else if (mlength == 2) {
                strTag = Str.substring(0, 4);
            }
            debud_temp = gPBOCDataElements.pbocDataElements[i][1];
            if (strTag.equals(gPBOCDataElements.pbocDataElements[i][1])) {

                //byte[] len = pbocUtile.StrToHex(Str.substring(mlength * 2, mlength * 2 + 2));
                PbocDataElementsClass.BERLength berlenth = GetBERLength(Str.substring(mlength * 2));

                Tlength = berlenth.taglength;

                if (( mlength * 2 + 2 + 2 * berlenth.lnum + Tlength * 2)>(Str.length())) {
                    Tlength = (Str.length()-(mlength * 2 + 2 + 2 * berlenth.lnum))/2;
                }

                AnalysisDataStr = Str.substring(mlength * 2 + 2 + 2 * berlenth.lnum, mlength * 2 + 2 + 2 * berlenth.lnum + Tlength * 2);

                RestAnalysisDataStr[Index_RestAnalysisDataStr] = Str.substring(mlength * 2 + 2 + 2 * berlenth.lnum + Tlength * 2, Str.length());
                Index_PBOCDataElements = i;
                if (RestAnalysisDataStr[Index_RestAnalysisDataStr].length() >= 4) {

                    int ret = CheckIsTag(RestAnalysisDataStr[Index_RestAnalysisDataStr].substring(0, 2));
                    if (ret == 1) {
                        Index_RestAnalysisDataStr++;
                        return 1;
                    }

                    ret = CheckIsTag(RestAnalysisDataStr[Index_RestAnalysisDataStr].substring(0, 4));
                    if (ret == 1) {
                        Index_RestAnalysisDataStr++;
                        return 1;  //输入数据除了一个TAG还有剩余部分
                    }

                    return 0;

                } else {
                    return 2;   //输入数据是一个TAG
                }

            } else {
                continue;
            }
        };

        return -1;

    }

    public int AnalysisDataElementsSubProcess_OneTime(String str) {

        int ret = 0;
        String useString = "";

        Index_PBOCDataElements = 0;
        useString = str;

        while (true) {

            ret = AnalysisOneDataElements(useString);
            Log.e("YJL","ret==="+ret);
            if (ret != -1) {
                ResultInfoShow += (gPBOCDataElements.pbocDataElements[Index_PBOCDataElements][1] + "-");
                ResultInfoShow += (gPBOCDataElements.pbocDataElements[Index_PBOCDataElements][2] + "-");
                ResultInfoShow += (gPBOCDataElements.pbocDataElements[Index_PBOCDataElements][3] + "-");
                ResultInfoShow += (gPBOCDataElements.pbocDataElements[Index_PBOCDataElements][4] + "\r\n");
                AnalysisDataStr = HookTheSpecialTAG(gPBOCDataElements.pbocDataElements[Index_PBOCDataElements][1], AnalysisDataStr);
                ResultInfoShow += (AnalysisDataStr + "\r\n");
            }

            if(gPBOCDataElements.pbocDataElements[Index_PBOCDataElements][2].contains("对象列表"))
            {
                return 0;
            }

            if ((ret == 0) || (ret == -1)) {
                return 0;
            } else {
                useString = AnalysisDataStr;
            }
        }

    }

    public int AnalysisDataElementsProcess() {
        String useStr;

        while (!(Index_RestAnalysisDataStr <= 0)) {

            useStr = RestAnalysisDataStr[--Index_RestAnalysisDataStr];

            if (useStr == "") {
                continue;
            }

            RestAnalysisDataStr[Index_RestAnalysisDataStr] = "";

            AnalysisDataElementsSubProcess_OneTime(useStr);

        }

        return 0;
    }

    public String HookTheSpecialTAG(String TAG, String str) {

        if (TAG.equals("50") || TAG.equals("9F12")||TAG.equals("9F63")) {
            return BJCWUtil.StrToAscii(str);
        }

        return str;
    }

    public class BERLength {

        int lnum;
        int taglength;
    }

    public BERLength GetBERLength(String str) {
        String Ln = str.substring(0, 2);
        byte[] b = BJCWUtil.StrToHex(Ln);
        int i;
        int rlen = 0;

        BERLength berl = new BERLength();

        if ((b[0] & 0x80) == 0x80) {
            int nl = (b[0] & 0x7F);
            Ln = str.substring(2, 2 + nl * 2);
            byte[] bn = BJCWUtil.StrToHex(Ln);

            for (i = 0; i < bn.length; i++) {
                rlen += LengthCombination(bn[i], bn.length - i - 1);
            }
            berl.lnum = nl;
            berl.taglength = rlen;
        } else {
            rlen = b[0];
            berl.lnum = 0;
            berl.taglength = rlen;
        }
        return berl;
    }

    public int LengthCombination(byte len, int n) {
        int i, r;

        r = (len & 0xFF);

        /*if(r<0)
         {
         r = (r&0x7F) + 128;
         }*/
        for (i = 0; i < n; i++) {
            r *= 0x100;
        }
        return r;
    }



}
