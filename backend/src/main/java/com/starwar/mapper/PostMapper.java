package com.starwar.mapper;

import com.starwar.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostMapper {
    int insert(Post post);
    
    List<Post> selectList(@Param("offset") Integer offset,
                         @Param("limit") Integer limit);
    
    Post selectById(Long id);
    
    int updateViewCount(Long id);
    
    int updateReplyCount(Long id);
    
    int count();
}

