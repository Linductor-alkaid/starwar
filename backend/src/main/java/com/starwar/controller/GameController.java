package com.starwar.controller;

import com.starwar.common.PageResult;
import com.starwar.common.Result;
import com.starwar.dto.GameRecordDTO;
import com.starwar.service.GameService;
import com.starwar.util.JwtUtil;
import com.starwar.vo.GameRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/game")
public class GameController {
    
    @Autowired
    private GameService gameService;
    
    @PostMapping("/record")
    public Result<?> saveRecord(@RequestHeader("Authorization") String token,
                               @RequestBody GameRecordDTO recordDTO) {
        Long userId = getUserIdFromToken(token);
        gameService.saveRecord(userId, recordDTO);
        return Result.success("保存成功", null);
    }
    
    @GetMapping("/history")
    public Result<PageResult<GameRecordVO>> getHistory(@RequestHeader("Authorization") String token,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getUserIdFromToken(token);
        return Result.success(gameService.getUserHistory(userId, page, size));
    }
    
    @GetMapping("/leaderboard")
    public Result<?> getLeaderboard(@RequestHeader(value = "Authorization", required = false) String token) {
        // 如果提供了token，返回包含当前用户信息的排行榜
        if (token != null && token.startsWith("Bearer ")) {
            try {
                Long userId = getUserIdFromToken(token);
                return Result.success(gameService.getLeaderboardWithUser(userId));
            } catch (Exception e) {
                // token无效时，返回不包含用户信息的排行榜（前10名）
                return Result.success(gameService.getLeaderboardWithUser(null));
            }
        }
        // 未登录用户，返回不包含用户信息的排行榜（前10名）
        return Result.success(gameService.getLeaderboardWithUser(null));
    }
    
    private Long getUserIdFromToken(String token) {
        token = token.substring(7); // 去除 "Bearer " 前缀
        return JwtUtil.getUserId(token);
    }
}

