package com.zcq.mapper;


import com.zcq.my.mapper.MyMapper;
import com.zcq.pojo.ItemsComments;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ItemsCommentsMapperCustom extends MyMapper<ItemsComments> {

    public void saveComments(Map<String, Object> map);


}