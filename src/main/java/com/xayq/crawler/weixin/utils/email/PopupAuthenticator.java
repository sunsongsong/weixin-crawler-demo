package com.xayq.crawler.weixin.utils.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;


public class PopupAuthenticator extends Authenticator {
    String    username=null;
    String    password=null;


    public PopupAuthenticator(){}

    public PopupAuthenticator(String username, String password
    ){
        this.username=username;
        this.password=password;
    }


    public PasswordAuthentication performCheck(String user, String pass){
        username = user;
        password = pass;
        return getPasswordAuthentication();
    }


    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }


}
