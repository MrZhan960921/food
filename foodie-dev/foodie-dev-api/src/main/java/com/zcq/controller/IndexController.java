package com.zcq.controller;

import com.zcq.enums.YesOrNo;
import com.zcq.pojo.Carousel;
import com.zcq.pojo.Category;
import com.zcq.pojo.vo.CategoryVO;
import com.zcq.pojo.vo.NewItemsVO;
import com.zcq.service.CarouselService;
import com.zcq.service.CategoryService;
import com.zcq.utils.JsonUtils;
import com.zcq.utils.RedisOperator;
import com.zcq.utils.ZCQJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "首页", tags = {"首页展示的相关接口"})
@RestController
@RequestMapping("index")
public class IndexController {

    @Autowired
    private CarouselService carouselService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisOperator redisOperator;

    /**
     * 1。广告图发生改变，删除缓存，然后重置
     * 2。定时重置
     * 3。每个轮播图有可能是一个广告，可能会过期，过期后在重置。
     */

    @ApiOperation(value = "获取首页轮播图列表", notes = "获取首页轮播图列表", httpMethod = "GET")
    @GetMapping("/carousel")
    public ZCQJSONResult carousel() {
        String carouselStr = redisOperator.get("carousel");
        List<Carousel> list = null;
        if (StringUtils.isBlank(carouselStr)) {
            list = carouselService.queryAll(YesOrNo.YES.type);
            redisOperator.set("carousel", JsonUtils.objectToJson(list));
        } else {
            list = JsonUtils.jsonToList(carouselStr, Carousel.class);
        }
        return ZCQJSONResult.ok(list);
    }

    /**
     * 首页分类展示需求：
     * 1. 第一次刷新主页查询大分类，渲染展示到首页
     * 2. 如果鼠标上移到大分类，则加载其子分类的内容，如果已经存在子分类，则不需要加载（懒加载）
     */
    @ApiOperation(value = "获取商品分类(一级分类)", notes = "获取商品分类(一级分类)", httpMethod = "GET")
    @GetMapping("/cats")
    public ZCQJSONResult cats() {
        String categoryStr = redisOperator.get("category");
        List<Category> list = null;
        if (StringUtils.isBlank(categoryStr)) {
            list = categoryService.queryAllRootLevelCat();
            redisOperator.set("category", JsonUtils.objectToJson(list));
        } else {
            list = JsonUtils.jsonToList(categoryStr, Category.class);
        }
        return ZCQJSONResult.ok(list);
    }

    @ApiOperation(value = "获取商品子分类", notes = "获取商品子分类", httpMethod = "GET")
    @GetMapping("/subCat/{rootCatId}")
    public ZCQJSONResult subCat(
            @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
            @PathVariable Integer rootCatId) {

        if (null == rootCatId) {
            return ZCQJSONResult.errorMsg("分类不存在");
        }

        String subCategoryStr = redisOperator.get("subCategory:" + rootCatId);
        List<CategoryVO> list = null;
        if (StringUtils.isBlank(subCategoryStr)) {
            list = categoryService.getSubCatList(rootCatId);
            /**
             * 查询的key在redis中不存在
             * 对应的id在数据库也不存在
             * 此时被非法用户进行攻击，会全部打在db上
             * 造成宕机
             * 这就是redis的缓存穿透
             * 解决方案：把空的数据也缓存起来，比如空字符串，空的对象，空数组或者list
             */
            if (list != null && list.size() > 0) {
                redisOperator.set("subCategory:" + rootCatId, JsonUtils.objectToJson(list));
            }else{
                redisOperator.set("subCategory:" + rootCatId, JsonUtils.objectToJson(list),5*60);
            }
        } else {
            list = JsonUtils.jsonToList(subCategoryStr, CategoryVO.class);
        }
        return ZCQJSONResult.ok(list);
    }

    @ApiOperation(value = "查询每个一级分类下的最新6条商品数据", notes = "查询每个一级分类下的最新6条商品数据", httpMethod = "GET")
    @GetMapping("/sixNewItems/{rootCatId}")
    public ZCQJSONResult sixNewItems(
            @ApiParam(name = "rootCatId", value = "一级分类id", required = true)
            @PathVariable Integer rootCatId) {

        if (rootCatId == null) {
            return ZCQJSONResult.errorMsg("分类不存在");
        }

        List<NewItemsVO> list = categoryService.getSixNewItemsLazy(rootCatId);
        return ZCQJSONResult.ok(list);
    }


}
