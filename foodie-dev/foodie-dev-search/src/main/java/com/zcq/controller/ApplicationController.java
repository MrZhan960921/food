package com.zcq.controller;


import com.zcq.service.ItemsESService;
import com.zcq.utils.PagedGridResult;
import com.zcq.utils.ZCQJSONResult;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("items")
public class ApplicationController {


    @Autowired
    private ItemsESService itemsESService;

    @GetMapping("/hello")
    public String hello(){
        return "Hello World";
    }

    @ApiOperation(value = "根据关键字和排序规则分页查询商品列表",notes = "根据关键字和排序规则分页查询商品列表",httpMethod = "GET")
    @GetMapping("/es/search")
    public ZCQJSONResult comments(
            String keywords,
            String sort,
            Integer page,
            Integer pageSize){

        if (StringUtils.isBlank(keywords)){
            return ZCQJSONResult.errorMsg("商品不存在");
        }
        if (page == null){
            page = 1;
        }
        if (pageSize == null){
            pageSize = 20;
        }

        //从0开始分页
        page --;

        PagedGridResult grid = itemsESService.searchItems(keywords,sort,
                page,pageSize);

        return ZCQJSONResult.ok(grid);
    }
}
