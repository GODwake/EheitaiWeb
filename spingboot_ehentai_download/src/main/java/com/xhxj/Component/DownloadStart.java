package com.xhxj.Component;

import com.xhxj.service.EheitaiCatalogService;
import com.xhxj.service.EheitaiDetailPageService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
public class DownloadStart {
    @Autowired
    Download download;

    @Autowired
    EheitaiCatalogService eheitaiCatalogService;
    @Autowired
    EheitaiDetailPageService eheitaiDetailPageService;


    /**
     * 开始接受作品完成消息
     *
     * @throws JMSException
     */
    @Scheduled(initialDelay = 1000, fixedDelay = 1 * 60 * 60 * 1000)
    public Integer Test() throws JMSException {

        //创建ConnectionFactory
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.211.128:61616");
        //创建会话对象

        //创建连接对象Connection
        Connection connection = connectionFactory.createConnection();

        //开启连接
        connection.start();


        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        //创建需要接收的消息地址
        Queue test = session.createQueue("completeGid");


        //接收消息
        MessageConsumer consumer = session.createConsumer(test);



        while (true) {
            System.out.println("mq接收还活着");
            //等待10秒，在10秒内一直处于接收消息状态
            Message message = consumer.receive(1000*10);
            if (message != null) {
                if (message instanceof TextMessage) {

                    TextMessage textMessage = (TextMessage) message;

                    System.out.println("完成爬取的作品：" + textMessage.getText());

                    String text = textMessage.getText();

                    Integer integer = Integer.valueOf(text);

                    //这里需要分别调用spingboot的多线程new 的多线程无法使用
                    download.download(integer, eheitaiCatalogService, eheitaiDetailPageService,download);
                    //关闭资源


                }
            }
        }

//        session.close();
//        connection.close();
    }


}