package cwbjsdk.cwsdk.bean;

/**
 * Created by leonn on 2017/5/31.
 */

public class APDUReplyData {

    private String retData;
    private int SW;

    public APDUReplyData()
    {
        SW = 0;
        retData = new String("");
    }
    public int getSW() {
        return SW;
    }

    public void setSW(int sW) {
        SW = sW;
    }

    public void setRetData(String retData) {
        if(null == retData)
            return;
        this.retData = retData;
    }

    public String getRetData() {
        return retData;
    }

    public String toString() {
        if(retData!=null)
            return "sw:"+Integer.toHexString(SW)+"，szReply:"+retData;
        else
            return Integer.toHexString(SW);
    }
}
