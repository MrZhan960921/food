package com.zcq.service;


import com.zcq.utils.PagedGridResult;

public interface ItemsESService {

    public PagedGridResult searchItems(String keywords,
                                       String sort,
                                       Integer page,
                                       Integer pageSize);
}
