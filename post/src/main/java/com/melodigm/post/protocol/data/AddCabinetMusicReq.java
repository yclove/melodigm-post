package com.melodigm.post.protocol.data;

import java.util.ArrayList;

public class AddCabinetMusicReq {
    String BXI = "";
    ArrayList<AddCabinetMusicItem> DATA = new ArrayList<>();

    public String getBXI() {
        return BXI;
    }

    public void setBXI(String BXI) {
        this.BXI = BXI;
    }

    public ArrayList<AddCabinetMusicItem> getAddCabinetMusicItem() {
        return DATA;
    }

    public void setAddCabinetMusicItem(ArrayList<AddCabinetMusicItem> addCabinetMusicItem) {
        this.DATA = addCabinetMusicItem;
    }
}
