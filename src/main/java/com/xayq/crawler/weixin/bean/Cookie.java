package com.xayq.crawler.weixin.bean;

import java.util.Date;

public class Cookie {
    private Integer id;

    private String uid;

    private String username;

    private String password;

    private String ip;

    private Integer port;

    private String cookie;

    private Integer isEffective;

    private Date createTime;

    private Date updateTime;

    public Cookie(Integer id, String uid, String username, String password, String ip, Integer port, String cookie, Integer isEffective, Date createTime, Date updateTime) {
        this.id = id;
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.ip = ip;
        this.port = port;
        this.cookie = cookie;
        this.isEffective = isEffective;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    public Cookie() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
