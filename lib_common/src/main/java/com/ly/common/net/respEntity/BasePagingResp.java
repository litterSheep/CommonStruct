package com.ly.common.net.respEntity;

import java.util.List;

/**
 * 分页通用response
 *
 * @author ly
 * date 2019/10/29 11:14
 */
public class BasePagingResp<T> {

    public List<T> records;
    /**
     * 当前页
     */
    public int current;
    /**
     * 是否首页
     */
    public boolean first;
    public boolean last;
    /**
     * 总页数
     */
    public int pages;
    /**
     * 每页记录数
     */
    public int size;
    /**
     * 总记录数
     */
    public int total;
}
