package com.xayq.crawler.weixin.utils.email;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class MailUtils {
    public static void main(String[] args) throws Exception {
            sendEmail("805459487@qq.com","这是一个好消息");
    }


    public static void sendEmail(String toAddress,String subJect)  {

        try {
            Properties props = new Properties();

            String host = "smtp.p5w.net"; // 1企业邮箱的smtp服务器
            String from = "sunss@p5w.net"; // 我的企业邮箱地址
            // String to = "zhangcheng@p5w.net"; // 邮件要发送到的邮箱地址
            String username = "sunss@p5w.net";
            String password = "Ss805459487"; //
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.host", host);
            String content = subJect;

            //认证的时候用的是 邮箱全拼 密码    密码
            Authenticator smtpAuth = new PopupAuthenticator(from, password);

            Session session = Session.getDefaultInstance(props, smtpAuth);
            session.setDebug(true);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            BodyPart mdp = new MimeBodyPart();
            mdp.setContent(content, "text/html;charset=gb2312");
            Multipart mm = new MimeMultipart();
            mm.addBodyPart(mdp);
            message.setContent(mm);
            message.setSubject(subJect);
            message.saveChanges();
            Transport transport = session.getTransport("smtp");
            transport.connect(host, username, password);//链接的时候用的是 用户名 密码   密码
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error : Sending mail failure  to : "+toAddress+" ! ");
        }
        System.out.println("Success : Sending mail to :" + toAddress) ;
    }


}
