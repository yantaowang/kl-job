package com.kx.service.mapper;

import com.kx.service.base.MyMapper;
import com.kx.service.data.pojo.Fans;
import com.kx.service.data.vo.FansVO;
import com.kx.service.data.vo.VlogerVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface FansMapperCustom extends MyMapper<Fans> {

    public List<VlogerVO> queryMyFollows(@Param("paramMap") Map<String, Object> map);

    public List<FansVO> queryMyFans(@Param("paramMap") Map<String, Object> map);

}