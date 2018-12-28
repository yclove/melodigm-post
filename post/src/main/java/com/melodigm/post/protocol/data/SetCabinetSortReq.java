package com.melodigm.post.protocol.data;

import java.util.ArrayList;

public class SetCabinetSortReq {
	private String BXI = "";
    private ArrayList<OstSortDataItem> SOS_LIST = new ArrayList<>();

    public String getBXI() {
        return BXI;
    }

    public void setBXI(String BXI) {
        this.BXI = BXI;
    }

    public ArrayList<OstSortDataItem> getSOS_LIST() {
        return SOS_LIST;
    }

    public void setSOS_LIST(OstSortDataItem SOS_LIST) {
        this.SOS_LIST.add(SOS_LIST);
    }

    public void clearList() {
        this.SOS_LIST.clear();
    }
}
