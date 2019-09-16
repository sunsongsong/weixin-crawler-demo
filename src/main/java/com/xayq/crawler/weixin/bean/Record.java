package com.xayq.crawler.weixin.bean;

import java.util.Date;

public class Record {
    private Integer id;

    private String username;

    private Integer searchType;

    private String searchContent;

    private String searchUrl;

    private String html;

    private String filePath;

    private Integer isEffective;

    private Date createTime;

    private String cookie;

    public Record(Integer id, String username, Integer searchType, String searchContent, String searchUrl, String html, String filePath, Integer isEffective, Date createTime, String cookie) {
        this.id = id;
        this.username = username;
        this.searchType = searchType;
        this.searchContent = searchContent;
        this.searchUrl = searchUrl;
        this.html = html;
        this.filePath = filePath;
        this.isEffective = isEffective;
        this.createTime = createTime;
        this.cookie = cookie;
    }

    public Record() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getSearchType() {
        return searchType;
    }

    public void setSearchType(Integer searchType) {
        this.searchType = searchType;
    }

    public String getSearchContent() {
        return searchContent;
    }

    public void setSearchContent(String searchContent) {
        this.searchContent = searchContent;
    }

    public String getSearchUrl() {
        return searchUrl;
    }

    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getIsEffective() {
        return isEffective;
    }

    public void setIsEffective(Integer isEffective) {
        this.isEffective = isEffective;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
