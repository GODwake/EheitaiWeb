package com.xhxj.web;

import com.xhxj.dao.EheitaiCatalogDao;
import com.xhxj.dao.EheitaiDetailPageDao;
import com.xhxj.daomain.EheitaiCatalog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.selector.Selectable;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class WebMagic implements PageProcessor {

    @Autowired
    AnalysisUrl analysisUrl;
    @Autowired
    EheitaiCatalogDao eheitaiCatalogDao;
    @Autowired
    EheitaiDetailPageDao eheitaiDetailPageDao;
    @Autowired
    WebMagicDate webMagicDate;

    //写出的测试网页
    private String catalog = "e://2.html";
    //网页的编码格式
    private String httpCharset = "";
    //设置一个变量储存当前图片地址用来对比是否到了最后一页
    private String imgSrc = "";

    @Override
    public void process(Page page) {


        //在这里处理获取到的页面
        List<String> list = page.getHtml().css("div.gdtm a").links().all();
        if (list == null || list.size() == 0) {
            //如果空了就说明当前是在图片页面
            System.out.println("这是图片页面:");
            System.out.println(page.getRequest().getUrl());


            //获取当前的连接
            String url = page.getRequest().getUrl();
            //我得获取它是第几页
            System.out.println("这是第:" + page.getHtml().css("div.sn>div>span ", "text").toString());


            //获取当前文件叫什么名


            //如果到了最后一页他的图片地址应该是和当前是重复的.


        } else {
            //没空就说明还在首页
            System.out.println("第一次访问" + list.toString());
            //把这个页面丢给爬虫去访问
            //获取总页数储存到数据库中去
            Selectable regex = page.getHtml().$("div#gdd ");
            //这里是全部的页面信息
            String string = regex.toString();
            Document parse = Jsoup.parseBodyFragment(string);
            System.out.println("------------------------------");
            //已获取总页数
            String text = parse.select("tr:contains(Length:)").select("td.gdt2").text();
            String[] length = text.split(" ");
            //获取上传的时间
            String posted = parse.select("tr:contains(Posted:)").select("td.gdt2").text();
            //获取文件大小
            String fileSize = parse.select("tr:contains(File Size:)").select("td.gdt2").text();
            //获取页面语言
            String language = parse.select("tr:contains(Language:)").select("td.gdt2").text();
            //这个是父id,可以更具这个id去查看历史记录:暂时不知道他有啥用
            String eid = parse.select("tr:contains(Parent:)").select("td.gdt2").text();
            //这是网站的真实id,更具这个去更新数据
            List<String> all = page.getHtml().xpath("//script[@type]").all();
            String gid = all.get(1).split("\n")[4].split(" ")[3].replace(";","");
            //这个地址和id相加就是网站链接
            List<String> all1 = page.getHtml().xpath("//script[@type]").all();
            String token = all1.get(1).split("\n")[5].split(" ")[3].replace(";","");


            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd ss:HH");
            Date postedDate = new Date();
            try {
                postedDate = simpleDateFormat.parse(posted);
            } catch (ParseException e) {
                System.out.println("解析页面时转换时间出错");
                e.printStackTrace();
            }


            //把上面获取的东西存入对象
            EheitaiCatalog eheitaiCatalog = new EheitaiCatalog();
            eheitaiCatalog.setLength(Integer.valueOf(length[0]));
            eheitaiCatalog.setPostedDate(postedDate);
            eheitaiCatalog.setFileSize(fileSize);
            eheitaiCatalog.setLanguage(language);
            eheitaiCatalog.setParent(Integer.valueOf(eid));
            eheitaiCatalog.setGid(Integer.valueOf(gid));
            eheitaiCatalog.setToken(token);


            page.putField("eheitaiCatalog",eheitaiCatalog);

            System.out.println(eheitaiCatalog);

            page.addTargetRequest(list.get(list.size() - 1));
        }


//        page.putField("jobInfo",all);

    }

    public void writeFile(Page page) {
        //把文件写出去看看
        byte[] bytes1 = page.getHtml().toString().getBytes();
        //写出文件看看乍回事
        InputStream content = new ByteArrayInputStream(bytes1);
        try {

            FileOutputStream downloadFile = new FileOutputStream(catalog);
            int index;
            byte[] bytes = new byte[1024];
            while ((index = content.read(bytes)) != -1) {
                downloadFile.write(bytes, 0, index);
                downloadFile.flush();
            }

            downloadFile.close();
        } catch (IOException e) {
            System.out.println("写出文件时报错");
            e.printStackTrace();
        }
    }


//    @PostConstruct
@Scheduled(initialDelay = 1000,fixedDelay = 1*60*60*1000)
    public void httpweb() {
        //抓取页面

        //自己蛋疼写的轮子,至少能用
/*
        httpCharset = analysisUrl.getHttp();
        System.out.println("网站的编码格式为:" + httpCharset);
        //获取解析结果存入sql
        analysisUrl.analysisHtml();
*/

        //下载页面
        //获取下载链接

        //应该写service层的..
        String url = "";
        String title = "";
        int divId = 1329034;
        //要爬取得数据的divid
        List<EheitaiCatalog> byDivId = eheitaiCatalogDao.findByGid(divId);
        //判断获取的作品不要是空的
        if (byDivId.size()!=0){



        for (EheitaiCatalog eheitaiCatalog : byDivId) {
            url = eheitaiCatalog.getUrl();
            title = eheitaiCatalog.getTitle();
        }

        System.out.println("要爬的网站路径~~~~~~" + url);
        //只去爬详情页面的数据
        Spider spider = Spider.create(new WebMagic())
                .addUrl(url)
                .addPipeline(webMagicDate)
                .thread(1);

        //设置爬虫代理
        HttpClientDownloader httpClientDownloader = new HttpClientDownloader();
        httpClientDownloader.setProxyProvider(SimpleProxyProvider.from(new Proxy("127.0.0.1", 1081)));
        spider.setDownloader(httpClientDownloader);
        spider.run();
        }else {
            System.out.println("作品id:"+divId+"没有爬取连接,webmagic找不到要爬取的网页");
        }
    }
    // 用来设置参数
    /**
     * Site.me()可以对爬虫进行一些参数配置，包括编码、超时时间、重试时间、重试次数等。在这里我们先简单设置一下：重试次数为3次，重试时间为3秒。
     */
    Site site = Site
            .me()
            .setTimeOut(10 * 1000) // 设置超时时间，10秒
            .setRetrySleepTime(3 * 1000) // 设置重试时间（如果访问一个网站的时候失败了，Webmagic启动的过程中，会每3秒重复再次执行访问）
            .setRetryTimes(3) // 设置重试次数
            .setCharset("UTF-8") // 获取UTF-8网站的数据
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3664.3 Safari/537.36")
            .addHeader("Cookie", "igneous=6c63cbdc0; ipb_member_id=805259; ipb_pass_hash=1a0592e854f1b08bcb9c2eb40b6455de; yay=0; lv=1546944712-1546960410");


    @Override
    public Site getSite() {
        return site;
    }


}
