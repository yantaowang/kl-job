package com.kx.service.mapper;

import com.kx.service.data.vo.CommentVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentMapperCustom {

    public List<CommentVO> getCommentList(@Param("paramMap") Map<String, Object> map);

}