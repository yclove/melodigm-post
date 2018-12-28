package com.melodigm.post.protocol.data;

public class SetPostLikeReq {
    String REG_PLAC_ID = "";
    String REG_PLAC_TYPE = "";
    String ICI = "";
    String TOGGLE_YN = "";

    public String getREG_PLAC_ID() {
        return REG_PLAC_ID;
    }

    public void setREG_PLAC_ID(String REG_PLAC_ID) {
        this.REG_PLAC_ID = REG_PLAC_ID;
    }

    public String getREG_PLAC_TYPE() {
        return REG_PLAC_TYPE;
    }

    public void setREG_PLAC_TYPE(String REG_PLAC_TYPE) {
        this.REG_PLAC_TYPE = REG_PLAC_TYPE;
    }

    public String getICI() {
        return ICI;
    }

    public void setICI(String ICI) {
        this.ICI = ICI;
    }

    public String getTOGGLE_YN() {
        return TOGGLE_YN;
    }

    public void setTOGGLE_YN(String TOGGLE_YN) {
        this.TOGGLE_YN = TOGGLE_YN;
    }
}
