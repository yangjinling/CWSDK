package cwbjsdk.cwsdk.bean;

/**
 * Created by leonn on 2017/5/31.
 */

public class STR_OBJ {

    public byte bOffset;
    public byte bObjType;
    public int wTag;
    public int wLen;

    public STR_OBJ(byte bOffset,byte bObjType,int wTag,int wLen) {
        this.bOffset=bOffset;
        this.bObjType=bObjType;
        this.wTag=wTag;
        this.wLen=wLen;
    }
}
