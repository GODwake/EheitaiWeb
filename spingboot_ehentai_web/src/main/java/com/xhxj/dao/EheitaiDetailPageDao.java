package com.xhxj.dao;

import com.xhxj.daomain.EheitaiDetailPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EheitaiDetailPageDao extends JpaRepository<EheitaiDetailPage, Integer>, JpaSpecificationExecutor<EheitaiDetailPage> {
    //查詢是否用重複的圖片
    @Query(value = "SELECT * FROM eheitai_detail_page WHERE eheitai_id = ? AND page = ?",nativeQuery =true)
    EheitaiDetailPage findByImgUrlAndPage(Integer gid, Integer page);

    //查询509的图片
    @Query(value = "SELECT url FROM eheitai_detail_page WHERE eheitai_detail_page.img_url = 'https://exhentai.org/img/509.gif'",nativeQuery = true)
    List<String> findByImgUrl509();

    //删除重复的数据,删除正确的数据,留下错误的做测试
    @Query(value = "\tSELECT\n" +
            "\t\tbid\n" +
            "\tFROM\n" +
            "\t\t( SELECT id aid, eheitai_detail_page.img_url, page, eheitai_detail_page.eheitai_id FROM eheitai_detail_page WHERE img_url = 'https://exhentai.org/img/509.gif' ) a,\n" +
            "\t\t( SELECT id bid, eheitai_detail_page.img_url, page, eheitai_detail_page.eheitai_id FROM eheitai_detail_page WHERE img_url != 'https://exhentai.org/img/509.gif' ) b \n" +
            "\tWHERE\n" +
            "\t\ta.eheitai_id = b.eheitai_id \n" +
            "\t\tAND a.page = b.page;" ,nativeQuery = true)
    List<Integer> findByTest509();


}

