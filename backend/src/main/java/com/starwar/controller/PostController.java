package com.starwar.controller;

import com.starwar.common.PageResult;
import com.starwar.common.Result;
import com.starwar.dto.PostDTO;
import com.starwar.dto.ReplyDTO;
import com.starwar.service.PostService;
import com.starwar.util.JwtUtil;
import com.starwar.vo.PostVO;
import com.starwar.vo.ReplyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/post")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    @PostMapping("/create")
    public Result<?> createPost(@RequestHeader("Authorization") String token,
                                @RequestBody PostDTO postDTO) {
        Long userId = getUserIdFromToken(token);
        postService.createPost(userId, postDTO);
        return Result.success("发布成功", null);
    }
    
    @GetMapping("/list")
    public Result<PageResult<PostVO>> getPostList(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(postService.getPostList(page, size));
    }
    
    @GetMapping("/detail/{postId}")
    public Result<PostVO> getPostDetail(@PathVariable Long postId) {
        return Result.success(postService.getPostDetail(postId));
    }
    
    @PostMapping("/reply")
    public Result<?> reply(@RequestHeader("Authorization") String token,
                          @RequestBody ReplyDTO replyDTO) {
        Long userId = getUserIdFromToken(token);
        postService.reply(userId, replyDTO);
        return Result.success("回复成功", null);
    }
    
    @GetMapping("/replies/{postId}")
    public Result<List<ReplyVO>> getReplies(@PathVariable Long postId) {
        return Result.success(postService.getReplies(postId));
    }
    
    private Long getUserIdFromToken(String token) {
        token = token.substring(7); // 去除 "Bearer " 前缀
        return JwtUtil.getUserId(token);
    }
}

