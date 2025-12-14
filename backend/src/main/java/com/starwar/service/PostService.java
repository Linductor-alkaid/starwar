package com.starwar.service;

import com.starwar.common.PageResult;
import com.starwar.dto.PostDTO;
import com.starwar.dto.ReplyDTO;
import com.starwar.vo.PostVO;
import com.starwar.vo.ReplyVO;

import java.util.List;

public interface PostService {
    void createPost(Long userId, PostDTO postDTO);
    
    PageResult<PostVO> getPostList(Integer page, Integer size);
    
    PostVO getPostDetail(Long postId);
    
    void incrementViewCount(Long postId);
    
    void reply(Long userId, ReplyDTO replyDTO);
    
    List<ReplyVO> getReplies(Long postId);
}

