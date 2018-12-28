package com.melodigm.post.protocol.data;

public class PostSortData {
    String mTitle = "";
    String mContent = "";
    String mCode = "";
    boolean isSelected = false;

    public PostSortData(String code, String title) {
        this.mCode = code;
        this.mTitle = title;
    }

    public PostSortData(String code, String title, String content) {
        this.mCode = code;
        this.mTitle = title;
        this.mContent = content;
    }

    public PostSortData(String code, String title, boolean selected) {
        this.mCode = code;
        this.mTitle = title;
        this.isSelected = selected;
    }

    public PostSortData(String code, String title, String content, boolean selected) {
        this.mCode = code;
        this.mTitle = title;
        this.mContent = content;
        this.isSelected = selected;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public String getmCode() {
        return mCode;
    }

    public void setmCode(String mCode) {
        this.mCode = mCode;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
