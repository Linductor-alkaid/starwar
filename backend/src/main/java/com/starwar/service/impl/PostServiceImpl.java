package com.starwar.service.impl;

import com.starwar.common.PageResult;
import com.starwar.dto.PostDTO;
import com.starwar.dto.ReplyDTO;
import com.starwar.entity.Post;
import com.starwar.entity.Reply;
import com.starwar.mapper.PostMapper;
import com.starwar.mapper.ReplyMapper;
import com.starwar.service.PostService;
import com.starwar.vo.PostVO;
import com.starwar.vo.ReplyVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private ReplyMapper replyMapper;
    
    @Override
    public void createPost(Long userId, PostDTO postDTO) {
        Post post = new Post();
        post.setUserId(userId);
        post.setTitle(postDTO.getTitle());
        post.setContent(postDTO.getContent());
        post.setViewCount(0);
        post.setReplyCount(0);
        post.setStatus(1);
        
        postMapper.insert(post);
    }
    
    @Override
    public PageResult<PostVO> getPostList(Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        
        List<Post> posts = postMapper.selectList(offset, size);
        Integer total = postMapper.count();
        
        List<PostVO> vos = posts.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(vos, total);
    }
    
    @Override
    public PostVO getPostDetail(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("帖子不存在");
        }
        
        PostVO vo = convertToVO(post);
        incrementViewCount(postId);
        return vo;
    }
    
    @Override
    public void incrementViewCount(Long postId) {
        postMapper.updateViewCount(postId);
    }
    
    @Override
    public void reply(Long userId, ReplyDTO replyDTO) {
        Reply reply = new Reply();
        reply.setPostId(replyDTO.getPostId());
        reply.setUserId(userId);
        reply.setContent(replyDTO.getContent());
        reply.setParentId(replyDTO.getParentId());
        reply.setStatus(1);
        
        replyMapper.insert(reply);
        postMapper.updateReplyCount(replyDTO.getPostId());
    }
    
    @Override
    public List<ReplyVO> getReplies(Long postId) {
        List<Reply> replies = replyMapper.selectByPostId(postId);
        return replies.stream()
            .map(this::convertReplyToVO)
            .collect(Collectors.toList());
    }
    
    private PostVO convertToVO(Post post) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        return vo;
    }
    
    private ReplyVO convertReplyToVO(Reply reply) {
        ReplyVO vo = new ReplyVO();
        BeanUtils.copyProperties(reply, vo);
        return vo;
    }
}

