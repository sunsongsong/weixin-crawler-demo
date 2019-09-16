package com.xayq.crawler.weixin.demo;

import com.google.gson.Gson;
import com.xayq.crawler.weixin.bean.HttpResult;
import com.xayq.crawler.weixin.bean.Record;
import com.xayq.crawler.weixin.utils.ip.IpUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用cookie进行查询
 */
public class UseCookieQueryDemo {

    private static Logger logger = LoggerFactory.getLogger(UseCookieQueryDemo.class);

    private static HttpClient httpclient = new DefaultHttpClient();
    private static HttpContext localContext = new BasicHttpContext();
    // cookie存储用来完成登录后记录相关信息
    private static BasicCookieStore basicCookieStore = new BasicCookieStore();
    // 连接超时时间10秒
    private static int TIME_OUT = 10;
    /**
     * 启用cookie存储
     */
    public static void instance() {
        httpclient.getParams().setIntParameter("http.socket.timeout", TIME_OUT * 1000);
        // Cookie存储
        localContext.setAttribute("http.cookie-store", basicCookieStore);
    }

    public void UseCookieQueryDemo(){
        instance();
    }

    public String checkCookieIsEffective(String cookie){
        String testSearchUrl = "https://s.weibo.com/weibo/2019";
        HttpGet httpGet = new HttpGet(testSearchUrl);
        Header header = new BasicHeader("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1;  .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; InfoPath.2)");
        httpGet.addHeader(header);

        header = new BasicHeader("Content-Type","application/x-www-form-urlencoded");
        httpGet.addHeader(header);

        header = new BasicHeader("Cookie",cookie);
        httpGet.addHeader(header);
        HttpResult httpResult = HttpResult.empty();
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet, localContext);
            httpResult = new HttpResult(localContext, httpResponse);
        } catch (IOException e) {
            logger.error(e.getMessage());
            httpGet.abort();
        }

        Gson gson = new Gson();
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("success",false);

        String html = new String(httpResult.getResponse());
        String uid = checkUseCookieSuccess(html);
        if(!"".equals(uid)){
            //获取uid
            params.put("success",true);
            params.put("uid",uid);
            logger.info("验证cookie是否可用 成功！");
        }
        return gson.toJson(params);
    }

    /**
     * 根据返回的html数据中,是否含有 特殊标识（）,来判断是否登录成功
     * 如果登陆成功并且拿到用户的uid返回
     * @param html
     * @return
     */
    public String checkUseCookieSuccess(String html){
        String uid = "";
        if(html != null && html.contains("$CONFIG['nick']")){
            logger.info("checkUseCookieSuccess html中含有特殊标识 $CONFIG['nick']");
            String rgex = "'nick'] = '(.*?)';";
            List<String> list = new ArrayList<>();
            Pattern pattern = Pattern.compile(rgex,Pattern.CASE_INSENSITIVE);// 匹配的模式
            Matcher m = pattern.matcher(html);
            while (m.find()) {
                String str = m.group(1);
                list.add(str);
            }
            if(list.size() == 1){
                uid = list.get(0);
            }
        }
        if(uid.startsWith("用户")){
            uid = uid.replace("用户","");
        }
        logger.info("checkUseCookieSuccess 获取uid={}",uid);
        return uid;
    }

    public Record query(int cookieId, String uid, String username, String cookie, String suffix, String searchContent, String searchUrl){
        logger.info("开始使用cookie进行搜索查询查询...");
        logger.info("查询参数：cookieId={} uid={} username={} searchUrl={}",cookieId,uid,username,searchUrl);
        Record record = new Record();

        HttpGet httpGet = new HttpGet(searchUrl);
        Header header = new BasicHeader("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1;  .NET CLR 2.0.50727; .NET CLR 3.0.04506.648; .NET CLR 3.5.21022; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; InfoPath.2)");
        httpGet.addHeader(header);

        header = new BasicHeader("Content-Type","application/x-www-form-urlencoded");
        httpGet.addHeader(header);

        header = new BasicHeader("Cookie",cookie);
        httpGet.addHeader(header);
        HttpResult httpResult = HttpResult.empty();
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet, localContext);
            httpResult = new HttpResult(localContext, httpResponse);
        } catch (IOException e) {
            logger.error(e.getMessage());
            httpGet.abort();
        }
        String html = new String(httpResult.getResponse());

        //写入文件
        String filePath = WriteStringToFile(suffix,html);

        record.setUsername(username);
        record.setCookie(cookie);
        record.setSearchType(0);
        record.setSearchContent(searchContent);
        record.setSearchUrl(searchUrl);
        record.setHtml(html);
        record.setFilePath(filePath);

        if(html != null && html.contains(uid)){ //判断当前cookie请求是否有效
            record.setIsEffective(1);
        }else {
            record.setIsEffective(0);
        }

        return record;
    }

    public static String WriteStringToFile(String suffix, String content) {

        String fileName = new SimpleDateFormat("yyyyMMdd-kkmmss").format(new Date());
        fileName += "_" + suffix;
        String path = "E://weixin-crawler-demo-result//"+fileName+".html";
        File file = new File(path);
        if(!file.exists()){
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.flush();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            path = "";
        }
        return path;
    }

    private static final String LIST_ERROR = "list_error";
    private static final String LIST_SUCCESS = "list_success";
    private static final String LIST_NONE= "list_none";

    public static void main(String[] args) {

        //在header中携带cookie,实现登陆,进行http请求的查询
        String query = "万科"; //查询内容万科
        String url = "https://weixin.sogou.com/weixin?query=" + query
            + "&type=2"
            + "&page=2"
            + "&ie=utf8";

        HttpGet httpGet = new HttpGet(url);

        Header header = new BasicHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
        httpGet.addHeader(header);

        header = new BasicHeader("User-Agent","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpGet.addHeader(header);

        header = new BasicHeader("Host","weixin.sogou.com");
        httpGet.addHeader(header);

        header = new BasicHeader("Referer","https://weixin.sogou.com");
        httpGet.addHeader(header);

        header = new BasicHeader("Content-Type","application/x-www-form-urlencoded");
        httpGet.addHeader(header);

        header = new BasicHeader("Cookie",
            "SUV=00ABD51C71C89E9C5D5D107FDB96C351; " +
                "CXID=F2FE9A1DEC7F85B1F935CCB5C440B0E5; " +
                "SUID=AE9350014B238B0A5D6CA7C100057CF3; " +
                "ad=llllllllll2Nb3KxlllllVCHHFGllllllqjFrkllll9llllllZlll5@@@@@@@@@@; " +
                "ABTEST=3|1568102873|v1; " +
                "IPLOC=CN6101; " +
                "weixinIndexVisited=1; " +
                "SNUID=E4224D18181C8A59C0AB884A19FBD521; " +
                "JSESSIONID=aaaY9BOwYVPq25ZUTH8Yw; " +
                "sct=1");
        httpGet.addHeader(header);

        //伪造随机IP
        header = new BasicHeader("X-FORWARDED-FOR", IpUtils.getRandomIp());
        httpGet.addHeader(header);

        HttpResult httpResult = HttpResult.empty();
        try {
            HttpResponse httpResponse = httpclient.execute(httpGet, localContext);
            httpResult = new HttpResult(localContext, httpResponse);
        } catch (IOException e) {
            logger.error(e.getMessage());
            httpGet.abort();
        }
        String body = new String(httpResult.getResponse());
        System.out.println(body);

        if(body.contains("我们的系统检测到您网络中存在异常访问请求")){
            WriteStringToFile(LIST_ERROR,new String(httpResult.getResponse()));
        }else if(body.contains(query + "的相关微信公众号文章 – 搜狗微信搜索")){
            WriteStringToFile(LIST_SUCCESS,new String(httpResult.getResponse()));
        }else {
            WriteStringToFile(LIST_NONE,new String(httpResult.getResponse()));
        }


    }

}
