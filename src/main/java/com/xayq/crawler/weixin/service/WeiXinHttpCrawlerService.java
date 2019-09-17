package com.xayq.crawler.weixin.service;

import com.xayq.crawler.weixin.bean.HttpResult;
import com.xayq.crawler.weixin.utils.ip.IpUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: WeiXinHttpCrawlerService
 * @Description: 微信HTTP爬虫相关操作
 * @Author sunsongsong
 * @Date 2019/9/12 16:15
 * @Version 1.0
 */
@Service
public class WeiXinHttpCrawlerService {

    private static Logger logger = LoggerFactory.getLogger(WeiXinHttpCrawlerService.class);

    private static final String LIST_ERROR = "list_error";
    private static final String LIST_SUCCESS = "list_success";
    private static final String LIST_NONE= "list_none";

    /**
     * 是否使用代理
     */
    private static final int IS_USE_PROXY = 1;

    /**
     * 代理使用的IP和端口
     */
    private static String ipAndPort = null;

    /**
     * 缓存可用cookie的列表
     */
    private static List<String> cacheUsableCookieList = new ArrayList<>();

    @Value("${receiveMailAddress}")
    String receiveMailAddress;

    private static HttpClient httpclient = new DefaultHttpClient();

    private static HttpContext localContext = new BasicHttpContext();

    // 用来存储cookie完成登录后记录相关信息
    private static BasicCookieStore basicCookieStore = new BasicCookieStore();

    // 连接超时时间10秒
    private static int TIME_OUT = 20;

    /**
     * 启用cookie存储
     */
    public static void instance() {
        httpclient.getParams().setIntParameter("http.socket.timeout", TIME_OUT * 1000);
        // Cookie存储
        localContext.setAttribute("http.cookie-store", basicCookieStore);
    }

    /**
     * 创建WebDriver
     * @return
     */
    private WebDriver createWebDriver() {
        WebDriver dr = null;

        DesiredCapabilities cap = DesiredCapabilities.chrome();

        System.setProperty("webdriver.chrome.driver", "C:\\tool\\chromedriver.exe");
        try {
            dr = new ChromeDriver(cap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dr.manage().timeouts().pageLoadTimeout(60L, TimeUnit.SECONDS);
        dr.manage().timeouts().implicitlyWait(30L, TimeUnit.SECONDS);
        return dr;
    }

    /**
     * 创建WebDriver --无窗口
     * @return
     */
    private WebDriver createWebDriverNoWindow() {
        WebDriver dr = null;

        DesiredCapabilities cap = DesiredCapabilities.chrome();

        System.setProperty("webdriver.chrome.driver", "C:\\tool\\chromedriver.exe");
        Map<String, Object> contentSettings = new HashMap<String, Object>();
        contentSettings.put("images", 2);
        contentSettings.put("plugins", 2);
        Map<String, Object> preferences = new HashMap<String, Object>();
        preferences.put("profile.default_content_setting_values", contentSettings);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", preferences);
        cap.setCapability("chromeOptions", options);

        try {
            dr = new ChromeDriver(cap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dr.manage().timeouts().pageLoadTimeout(120L, TimeUnit.SECONDS);
        dr.manage().timeouts().implicitlyWait(30L, TimeUnit.SECONDS);
        return dr;
    }

    /**
     * 获取代理IP
     * @return
     */
    public String getProxyIpAndPort(){

        //--https://www.xicidaili.com/ 抓取可用代理ip

        /*WebDriver dr = createWebDriver();
        String ipAndPort = null;
        try{
            dr.get("https://www.xicidaili.com/");

            //获取ip
            String ip = "";
            String ipXpath = "//*[@id=\"ip_list\"]/tbody/tr[47]/td[2]";
            WebElement ipElement = dr.findElement(By.xpath(ipXpath));
            ip = ipElement.getText();

            //获取port
            String port = "";
            String portXpath = "//*[@id=\"ip_list\"]/tbody/tr[47]/td[3]";
            WebElement portElement = dr.findElement(By.xpath(portXpath));
            port = portElement.getText();

            ipAndPort = ip + ":" + port;

            dr.quit();
        }catch (Exception e){
            logger.error("获取代理IP异常");
            dr.quit();
        }*/

        String ipAndPort = "47.107.249.77:31280";
        this.ipAndPort = ipAndPort;
        logger.info("获取代理IP:{}",ipAndPort);


        return ipAndPort;
    }

    /**
     * 创建WebDriver --使用代理
     * @return
     */
    private WebDriver createWebDriverUseProxy() {

        //获取代理ip    结构： 127.0.0.1:8080
        String proxyIpAndPort = getProxyIpAndPort();

        WebDriver dr = null;
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        Proxy proxy = new Proxy();
        proxy.setHttpProxy(proxyIpAndPort).setFtpProxy(proxyIpAndPort).setSslProxy(proxyIpAndPort);
        cap.setCapability(CapabilityType.ForSeleniumServer.AVOIDING_PROXY, true);
        cap.setCapability(CapabilityType.ForSeleniumServer.ONLY_PROXYING_SELENIUM_TRAFFIC, true);
        System.setProperty("http.nonProxyHosts", "localhost");
        cap.setCapability(CapabilityType.PROXY, proxy);

        System.setProperty("webdriver.chrome.driver", "C:/tool/chromedriver.exe");
        try {
            dr = new ChromeDriver(cap);
        } catch (Exception e) {
            System.out.println("创建webdriver出错");
            e.printStackTrace();
        }

        dr.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        dr.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        return dr;
    }

    /**
     * 获取可用的cookie,并转换为String,提供给http请求使用
     * @return
     */
    private String getUsableCookieAndTranformStr() {
        //获取可用cookie
        if(cacheUsableCookieList.size() == 0){
            Set<Cookie> cookie = getCookie();
            if(cookie == null){
                return null;
            }
            //转换为cookieStr
            String cookieStr = transformCookie(cookie);
            cacheUsableCookieList.add(cookieStr);
            return cookieStr;
        }
        return cacheUsableCookieList.get(new Random().nextInt(cacheUsableCookieList.size()));
    }

    /**
     * 打开浏览器，获取cookie
     * @return
     */
    public Set<Cookie> getCookie() {
        logger.info("开始进行搜索,获取微信cookie...");

        int retryTime = 3;

        Set<Cookie> cookies = null;

        //重试3次获取有效cookie,有时候会出现开始异常，再次又正确了
        for(int time = 1; time <= retryTime; time++){

            WebDriver dr = null;
            try {

                if(IS_USE_PROXY == 1){
                    dr = createWebDriverUseProxy();
                }else {
                    dr = createWebDriver();
                }
                dr.get("https://weixin.sogou.com");

                try {
                    String checkXpath = "//div[@class='other']/span[@class='s1']";
                    WebElement checkEle = dr.findElement(By.xpath(checkXpath));
                    String content = checkEle.getAttribute("textContent");
                    if (content.indexOf("您的访问出错了") > -1) {
                        logger.error("WebDriver打开当前链接,出现验证码,跳出重试...");
                        dr.quit();
                        Thread.sleep(10000L);
                        continue;
                    }
                } catch (Exception e) {
                    //出现异常，说明未找到异常页面的标签
                    logger.info("WebDriver打开当前链接,未出现异常！");
                }

                Thread.sleep(2000);

                String keyWord = getKeyWord();
                logger.info("WebDriver开始输入关键字:{},进行搜索...",keyWord);

                //输入搜索关键字
                WebElement searchInput = dr.findElement(By.xpath("//*[@id=\"query\"]"));
                searchInput.sendKeys(new CharSequence[]{keyWord});

                Thread.sleep(2000);

                //点击搜索
                WebElement searchAction = dr.findElement(By.xpath("//*[@id=\"searchForm\"]/div/input[3]"));
                searchAction.click();

                Thread.sleep(2000);

                //进行关键字搜索后，有时候也会出现验证码
                try {
                    String checkXpath = "//div[@class='other']/span[@class='s1']";
                    WebElement checkEle = dr.findElement(By.xpath(checkXpath));
                    String content = checkEle.getAttribute("textContent");
                    if (content.indexOf("您的访问出错了") > -1) {
                        logger.error("WebDriver进行关键字搜索后,出现验证码,跳出重试...");
                        dr.quit();
                        Thread.sleep(10000L);
                        continue;
                    }
                } catch (Exception e) {
                    //出现异常，说明未找到异常页面的标签
                    logger.info("WebDriver进行关键字搜索后,未出现异常！");
                }

                Thread.sleep(10000L);

                cookies = dr.manage().getCookies();

                dr.quit();

                //存入cookie
                if(cookies == null || cookies.isEmpty()){
                    logger.info("WebDriver打开当前链接,未获取到cookie信息,跳出重试...");
                    continue;
                }else{
                    logger.info("WebDriver打开当前链接,获取cookie完成...");
                    return cookies;
                }

            } catch (Exception e) {
                e.printStackTrace();
                String s = UUID.randomUUID().toString();
                try {
                    File screenshot = ((TakesScreenshot) dr).getScreenshotAs(OutputType.FILE);
                    FileUtils.copyFile(screenshot, new File("C:/ccc/" + s + ".jpg"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                logger.info("WebDriver打开当前链接,出现异常,完成截图操作!!!");
                continue;
            }
        }

        //重试3次 仍未获取到有效cookie,发送邮件提醒
        if(cookies == null){
            logger.error("重试3次 WebDriver仍未获取到有效cookie,发送邮件提醒!");
            logger.error("被封ip:{}",IpUtils.getIpAddress());
            //MailUtils.sendEmail(receiveMailAddress, "本次搜索出现验证码");
        }

        return cookies;
    }

    /**
     * 创建HttpGet请求
     * @param url
     * @param cookie
     * @return
     */
    private HttpGet createHttpGet(String url,String cookie){

        HttpGet httpGet = new HttpGet(url);

        Header header = new BasicHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.142 Safari/537.36");
        httpGet.addHeader(header);

        header = new BasicHeader("User-Agent","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
        httpGet.addHeader(header);

        header = new BasicHeader("Host","weixin.sogou.com");
        httpGet.addHeader(header);

        header = new BasicHeader("Referer","https://weixin.sogou.com");
        httpGet.addHeader(header);

        header = new BasicHeader("Content-Type","application/x-www-form-urlencoded");
        httpGet.addHeader(header);

        //设置cookie
        header = new BasicHeader("Cookie",cookie);
        httpGet.addHeader(header);

        //伪造随机IP
//        header = new BasicHeader("X-FORWARDED-FOR", IpUtils.getRandomIp());
//        httpGet.addHeader(header);

        return httpGet;
    }

    /**
     * 对cookie进行转换
     * @param cookie
     * @return
     */
    public String transformCookie(Set<Cookie> cookie){

        String cookieStr = cookie.toString();
        logger.info("开始转换cookie...");
        logger.info(cookie.toString());

        //需要的信息：SUV、CXID、SUID、ad、ABTEST、IPLOC、weixinIndexVisited、SNUID、JSESSIONID、PHPSESSID、sct
//        String partKey[] = {"SUV","CXID","SUID","ad","ABTEST","IPLOC","weixinIndexVisited","SNUID"
//            ,"JSESSIONID","PHPSESSID","sct"};
        String partKey[] = {"SUV","CXID","SUID","ad","ABTEST","weixinIndexVisited","SNUID"
            ,"JSESSIONID","PHPSESSID","sct"};

        String partCookie = "";
        for(String key : partKey){
            String rgex = key + "=" + "(.*?)" + ";";
            String value = findRegxContent(cookieStr,rgex);
            if(StringUtils.isNotEmpty(value)){
                partCookie += key + "=" + value + ";";
            }
        }
        partCookie += "IPLOC=CN6101;";
//        partCookie += "sct=1";
        logger.info("完成转换cookie...");
        logger.info(partCookie);
        return partCookie;
    }

    /**
     * 使用正则匹配获取内容
     * @param content
     * @param rgex
     * @return
     */
    public String findRegxContent(String content, String rgex){
        Pattern pattern = Pattern.compile(rgex,Pattern.CASE_INSENSITIVE);// 匹配的模式
        Matcher m = pattern.matcher(content);
        String regxContent = "";
        while (m.find()) {
            regxContent = m.group(1);
        }
        return regxContent;
    }

    /**
     * 获取搜索的关键字
     * @return
     */
    public String getKeyWord(){
        String keyWordArr[] = {"万科","宜华生活","华兰生物","招商银行","宇通客车",
            "平安银行", "招商地产","九洲药业","新研股份","宇环数控",
            "中信国安","惠博普","中国国贸","白云机场","合盛硅业",
            "宝塔实业","长航凤凰","中兵红箭","东方钽业","中航飞机"
        };
        int size = keyWordArr.length;
        int random = new Random().nextInt(size);
        return keyWordArr[random];
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
        logger.info("输出文件地址：{}",path);
        return path;
    }

    /**
     * 爬取微信入口
     */
    public void crawlerWeiXin() throws InterruptedException {

        instance();

        while (true){

            //1.获取可用cookie
            String cookieStr = getUsableCookieAndTranformStr();
            if(cookieStr == null){
                logger.error("获取不到可用cookie.....");
                return;
            }

            //2.获取搜索的关键字
            String keyWord = getKeyWord();
            logger.info("搜索关键字：{}",keyWord);

            //3.组装url
            String url = "https://weixin.sogou.com/weixin?type=2&ie=utf8&query=#{keyword}";
            try {
                keyWord = URLEncoder.encode(keyWord, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            url = url.replace("#{keyword}", keyWord);


            //拼接搜索类型 一周内 tsn取值：1 一天内 2 一周内 3 一月内 4 一年内
            url += "&tsn=2";

            //4.开始循环 爬取前3页的数据
            for(int page=1; page <= 3; page++){

                //4.1 拼接页数
                String openUrl = url;
                if(page != 1){
                    openUrl += "&page="+page;
                }
                //其它参数
                openUrl += "&_sug_=n";
                openUrl += "&s_from=input";
                openUrl += "&sst0=" + System.currentTimeMillis();

                logger.info("本次栏目页请求链接：{}",openUrl);

                //4.2 组装请求体
                HttpGet httpGet = createHttpGet(openUrl, cookieStr);

                //4.3 发出请求
                HttpResult httpResult = HttpResult.empty();
                String body = null;
                try {
                    if(IS_USE_PROXY == 1){
                        logger.info("使用代理进行请求...");
                        HttpHost proxy = new HttpHost(ipAndPort.split(":")[0], Integer.valueOf(ipAndPort.split(":")[1]),"http");
                        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
                        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(config).build();

                        httpGet.setConfig(config);
                        // 使用TCP短链接无效
                        httpGet.setProtocolVersion(HttpVersion.HTTP_1_0);
                        httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_CLOSE);
                        HttpResponse httpResponse = client.execute(httpGet);
                        logger.info("请求返回状态码：{}",httpResponse.getStatusLine().getStatusCode());
                        if(httpResponse.getStatusLine().getStatusCode() == 200){
                            StringBuffer buffer = new StringBuffer();
                            InputStream inputStream = httpResponse.getEntity().getContent();
                            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                            String str = null;
                            while ((str = bufferedReader.readLine()) != null) {
                                buffer.append(str);
                            }
                            bufferedReader.close();
                            inputStreamReader.close();
                            // 释放资源
                            inputStream.close();
                            body = buffer.toString();
                        }
                    }else {
                        logger.info("不使用代理进行请求...");
                        HttpResponse httpResponse = httpclient.execute(httpGet, localContext);
                        logger.info("请求返回状态码：{}",httpResponse.getStatusLine().getStatusCode());
                        httpResult = new HttpResult(localContext, httpResponse);
                        body = httpResult.getResponse().toString();
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    httpGet.abort();
                }

                if(body == null){
                    logger.info("未获取到请求内容!");
                }

                //System.out.println(body);

                //4.4 判断搜索结果
                if(body.contains("的相关微信公众号文章")){
                    logger.info("携带cookie搜索成功");
                    WriteStringToFile(LIST_SUCCESS,body);
                }
                else if(body.contains("我们的系统检测到您网络中存在异常访问请求")){
                    logger.info("携带cookie搜索异常");
                    WriteStringToFile(LIST_ERROR,body);
                    //移除当前cookie
                    cacheUsableCookieList.remove(cookieStr);
                }
                else if(body.contains("搜狗微信搜索_订阅号及文章内容独家收录")){
                    logger.info("携带cookie未搜索到相关的关键字信息");
                    WriteStringToFile(LIST_NONE,body);
                    //移除当前cookie
                    cacheUsableCookieList.remove(cookieStr);
                }

                //4.5 对搜索成功的结果进行解析


                //4.6 等待10s
                Thread.sleep(2000);


            }

        }

    }

    public static void main(String[] args) throws InterruptedException {
        new WeiXinHttpCrawlerService().crawlerWeiXin();
    }


}
