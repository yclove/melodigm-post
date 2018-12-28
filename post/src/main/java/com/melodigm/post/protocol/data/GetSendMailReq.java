package com.melodigm.post.protocol.data;

public class GetSendMailReq {
    String SUBJ = "";
    String CONT = "";

    public String getSUBJ() {
        return SUBJ;
    }

    public void setSUBJ(String SUBJ) {
        this.SUBJ = SUBJ;
    }

    public String getCONT() {
        return CONT;
    }

    public void setCONT(String CONT) {
        this.CONT = CONT;
    }
}
