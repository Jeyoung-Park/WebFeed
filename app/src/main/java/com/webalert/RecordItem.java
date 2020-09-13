package com.webalert;

import java.util.ArrayList;

public class RecordItem {
    private Long id;
    private String title, address, keyword;
    private int changeDetection, keywordNumber;

    public ArrayList getList() {
        return list;
    }

    public void setList(ArrayList list) {
        this.list = list;
    }

    private ArrayList list;

    public RecordItem(){}

    public RecordItem(Long id, String title, String address, String keyword, int changeDetection) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.keyword=keyword;
        this.changeDetection = changeDetection;
    }

    public RecordItem(Long id, String title, String address, String keyword, int changeDetection, int keywordNumber) {
        this.id = id;
        this.title = title;
        this.address = address;
        this.keyword = keyword;
        this.changeDetection = changeDetection;
        this.keywordNumber = keywordNumber;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getChangeDetection() {
        return changeDetection;
    }

    public void setChangeDetection(int changeDetection) {
        this.changeDetection = changeDetection;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getKeywordNumber() {
        return keywordNumber;
    }

    public void setKeywordNumber(int keywordNumber) {
        this.keywordNumber = keywordNumber;
    }
}
