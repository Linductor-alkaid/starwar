package com.starwar.vo;

import lombok.Data;
import java.util.List;

@Data
public class LeaderboardVO {
    private List<GameRecordVO> topList;  // 前10名列表
    private GameRecordVO currentUser;    // 当前用户的最高成绩
    private Integer currentUserRank;     // 当前用户的排名
    private Integer totalUsers;          // 总用户数（有游戏记录的用户数）
}

