package com.melodigm.post.protocol.data;

public class GetPostPositionDataReq {
	String POI = "";
	String POI_MOVE_FLAG = "";
    String POST_TYPE = "";

    public String getPOI() {
        return POI;
    }

    public void setPOI(String POI) {
        this.POI = POI;
    }

    public String getPOI_MOVE_FLAG() {
        return POI_MOVE_FLAG;
    }

    public void setPOI_MOVE_FLAG(String POI_MOVE_FLAG) {
        this.POI_MOVE_FLAG = POI_MOVE_FLAG;
    }

    public String getPOST_TYPE() {
        return POST_TYPE;
    }

    public void setPOST_TYPE(String POST_TYPE) {
        this.POST_TYPE = POST_TYPE;
    }
}
