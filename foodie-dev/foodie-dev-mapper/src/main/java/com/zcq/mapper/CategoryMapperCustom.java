package com.zcq.mapper;

import com.zcq.pojo.vo.CategoryVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CategoryMapperCustom {

    /**
     * 自连接查询获取二级三级分类
     * @param rootCatId
     * @return
     */
    public List<CategoryVO> getSubCatList(Integer rootCatId);

}