package com.xhxj.controller;

import com.xhxj.daomain.EheitaiCatalog;
import com.xhxj.daomain.EheitaiDetailPage;
import com.xhxj.service.EheitaiCatalogService;
import com.xhxj.service.EheitaiDetailPageService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.HttpClientDownloader;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.proxy.Proxy;
import us.codecraft.webmagic.proxy.SimpleProxyProvider;
import us.codecraft.webmagic.scheduler.QueueScheduler;
import us.codecraft.webmagic.selector.Selectable;

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
    EheitaiCatalogService eheitaiCatalogDao;
    @Autowired
    EheitaiDetailPageService eheitaiDetailPageDao;
    @Autowired
    WebMagicDate webMagicDate;

    //写出的测试网页
    private String catalog = "e://2.html";
    //网页的编码格式
    private String httpCharset = "";
    //设置一个变量储存当前图片地址用来对比是否到了最后一页
    private String imgSrc = "";

    private Integer count = 0;

    private Integer img = 0;

    public WebMagic() {
    }

    public WebMagic(EheitaiCatalogService eheitaiCatalogService, EheitaiDetailPageService eheitaiDetailPageService) {
        this.eheitaiCatalogDao = eheitaiCatalogService;
        this.eheitaiDetailPageDao = eheitaiDetailPageService;
    }

    private static Spider spider = null;


    @Override
    public void process(Page page) {


        if (Integer.valueOf(page.getRawText().length()) > 280) {


            //在这里处理获取到的页面
            List<String> list = page.getHtml().css("div.gdtm a").links().all();
            if (list == null || list.size() == 0) {
                //图片页面
                parseDetailPageImgHtml(page);

            } else {
                //作品首页
//            System.out.println("第一次访问" + list.toString());
                //解析首页
                parseDetailPageHomeHtml(page);

                //获取首页的第二页链接
//            List<String> string = page.getHtml().xpath("/html/body/div[@gtb] //table/tbody/tr").links().all();
                List<String> string = page.getHtml().$("div.gtb table>tbody>tr").links().all();


                page.addTargetRequests(string);
                //把所有获取到的数据用过去
                //判断去重复
                repetition(page, list);


            }
            //到这里if
        } else {
            System.out.println("ip被禁止,应该把该网址添加到新的爬虫库");
            //如果被禁就把该地址拿去重试
            try {
                FileWriter fileWriter = new FileWriter("./banUrl.txt", true);
                fileWriter.write(page.getUrl().toString() + "\n");
                fileWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * springboot已启动就去加载全部的url
     */
    //@PostConstruct
    /*public void WebMagic() {
        allUrl = eheitaiCatalogDao.findByUrl();

        allImgUrl = eheitaiDetailPageDao.findByUrl();

    }*/

    //一开始就要去查询数据库中的数据,放着等待比对
    private static List<String> allUrl = null;

    //查询全部图片表中的url
    private static List<String> allImgUrl = null;


    /**
     * 去判断有没有获取到重复的id
     * 一个简单的判断
     *
     * @param page
     * @param list 获取到的图片链接
     */


    private void repetition(Page page, List<String> list) {





        //通知爬虫去爬取没有的图片
        page.addTargetRequests(list);


    }


    /**
     * 去测试一下如果连接已经在数据库中,就不要去访问了
     *
     * @param page 用来调用添加连接的方法
     * @param list 用来
     */
    private void addUrl(Page page, List<String> list) {


        page.addTargetRequests(list);

    }

    /**
     * 解析图片下载页面
     *
     * @param page
     */
    private void parseDetailPageImgHtml(Page page) {


        EheitaiDetailPage eheitaiDetailPage = new EheitaiDetailPage();
        //设置当前页面地址
        eheitaiDetailPage.setUrl(page.getRequest().getUrl());
        //设置文件信息
        String filelog = page.getHtml().$("div#i2>div").all().get(1);
        String[] divs = Jsoup.parseBodyFragment(filelog).select("div").text().split(" :: ");
        eheitaiDetailPage.setFileLog(divs[0]);
        eheitaiDetailPage.setResolution(divs[1]);
        eheitaiDetailPage.setFileSize(divs[2]);

        //设置当前第几页
        eheitaiDetailPage.setPage(Integer.parseInt(page.getHtml().css("div.sn>div>span ", "text").toString()));
        //设置当前图片下载地址
//        eheitaiDetailPage.setImgUrl();
        Document document = Jsoup.parseBodyFragment(page.getHtml().$("div#i3").toString());
        String imgurl = document.select("img").attr("src");
        eheitaiDetailPage.setImgUrl(imgurl);
        //获取gid
        String gid = page.getHtml().xpath("/html/body/script[2]").toString().split("<script type=\"text/javascript\">")[1].split("</script>")[0].split(";")[0].split(" ")[1].split("=")[1];

        Integer integer = Integer.valueOf(gid);
        //把所需要的对象传过去
        page.putField("eheitaiDetailPage", eheitaiDetailPage);
        page.putField("gid", integer);


        //看看是不是最后一页了最后一页通知下载进程去下载
        //当前页
        String currentpage = page.getHtml().css("div.sn>div>span ", "text").toString();
        //总页面
        String lastpage = page.getHtml().xpath("//*[@id='i2']/div[1]/div/span[2]/text()").toString();

//        //这是下载到99时的页数
//        int duob =(int) (Integer.valueOf(lastpage) * 0.99);
//        //当前页面快要完全下载的时候,通知后台去检测是否完成
//        //这是当前页
//        Integer integer1 = Integer.valueOf(currentpage);
//
//
        //我在下面已经写了判断,只要是图片就进去判断


            //已经是图片页页尾,该作品爬取完毕
            //用中间件MQ通知下载器,需要把当前作品id传过去
            page.putField("complete", gid);



    }

    /**
     * 获取首页的解析
     *
     * @param page
     */
    private void parseDetailPageHomeHtml(Page page) {

        //获取总页数储存到数据库中去
        Selectable regex = page.getHtml().$("div#gdd ");
        //这里是全部的页面信息
        String string = regex.toString();
        Document parse = Jsoup.parseBodyFragment(string);
//        System.out.println("------------------------------");
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
        String gid = all.get(1).split("\n")[4].split(" ")[3].replace(";", "");
        //这个地址和id相加就是网站链接
        List<String> all1 = page.getHtml().xpath("//script[@type]").all();
        String token = all1.get(1).split("\n")[5].split(" ")[3].replace(";", "");


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
        if (!eid.equalsIgnoreCase("none")) {
            eheitaiCatalog.setParent(Integer.valueOf(eid));
        }
        eheitaiCatalog.setGid(Integer.valueOf(gid));
        eheitaiCatalog.setToken(token);


        page.putField("eheitaiCatalog", eheitaiCatalog);

//        System.out.println("解析首页完成" + eheitaiCatalog);

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

    // 用来设置参数
    /**
     * Site.me()可以对爬虫进行一些参数配置，包括编码、超时时间、重试时间、重试次数等
     */
    Site site = Site
            .me()
            .setTimeOut(1000*2) // 设置超时时间,服务器在国外设置大一些
            .setRetrySleepTime(1000) // 设置重试时间（如果访问一个网站的时候失败了，Webmagic启动的过程中，会每3秒重复再次执行访问）
            .setRetryTimes(2) // 设置重试次数
            .setCharset("UTF-8") // 获取UTF-8网站的数据
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3664.3 Safari/537.36")
            .addHeader("Cookie", "igneous=6c63cbdc0; ipb_member_id=805259; ipb_pass_hash=1a0592e854f1b08bcb9c2eb40b6455de; yay=0; lv=1546944712-1546960410");


    @Override
    public Site getSite() {
        return site;
    }


    //    @PostConstruct
//    @Scheduled(initialDelay = 1000, fixedDelay = 1 * 60 * 60 * 1000)


    public void httpWebStart(List<String> url, List<Proxy> proxies, EheitaiCatalogService eheitaiCatalog, EheitaiDetailPageService eheitaiDetailPage) {
        this.eheitaiCatalogDao = eheitaiCatalog;
        this.eheitaiDetailPageDao = eheitaiDetailPage;
        httpweb(url, proxies);
    }

    /**
     * 爬虫开始的地方
     *
     * @param url 需要爬取的连接
     */
    public void httpweb(List<String> url, List<Proxy> proxies) {
        //这是一个抓取

        if (url != null && proxies != null) {


            String[] strings = url.toArray(new String[url.size()]);
            //给爬虫设置参数


            spider = Spider.create(new WebMagic(eheitaiCatalogDao, eheitaiDetailPageDao))
                    .addUrl(strings)
                    .addPipeline(webMagicDate)
                    //设置去重
//                    .setScheduler(new QueueScheduler().setDuplicateRemover(new BloomFilterDuplicateRemover(10000000)))
                    .setScheduler(new QueueScheduler().setDuplicateRemover(new WebMagicScheduler(eheitaiCatalogDao, eheitaiDetailPageDao)))
                    .thread(100);


//            System.out.println("要爬的网站路径~~~~~~" + url);
            //只去爬详情页面的数据


            HttpClientDownloader httpClientDownloader = new HttpClientDownloader();


            httpClientDownloader.setProxyProvider(
                    //把连接池list丢进去
                    SimpleProxyProvider.from(proxies));

            //设置爬虫代理
            spider.setDownloader(httpClientDownloader);
            spider.run();


        }

    }

    /**
     * 关闭方法
     */
    public void stop() {
        spider.stop();
    }


}
