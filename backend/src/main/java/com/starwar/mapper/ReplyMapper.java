package com.starwar.mapper;

import com.starwar.entity.Reply;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ReplyMapper {
    int insert(Reply reply);
    
    List<Reply> selectByPostId(Long postId);
    
    Reply selectById(Long id);
}

