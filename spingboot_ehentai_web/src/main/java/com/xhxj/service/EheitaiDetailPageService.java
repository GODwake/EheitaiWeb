package com.xhxj.service;

import com.xhxj.daomain.EheitaiDetailPage;

import java.util.List;

public interface EheitaiDetailPageService {

    //保存对象
    void save(EheitaiDetailPage eheitaiDetailPage);

    /**
     * 根据gid外键去保存对象
     * @param pageeheitaiDetailPage page页面传过来的参数
     * @param gid 外键
     */
    void saveEheitaiDetailPage(EheitaiDetailPage pageeheitaiDetailPage, Integer gid);


    /**
     * 查询下载失败509的数据
     * @return
     */
    List<String> findByImgUrl509();


    /**
     * 删除重复的数据,删除正确的数据,留下错误的做测试
     * @return
     */
    List<Integer> findByTest509();


    void deleteTest509(List<Integer> imgid);
}
