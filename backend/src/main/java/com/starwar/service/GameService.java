package com.starwar.service;

import com.starwar.common.PageResult;
import com.starwar.dto.GameRecordDTO;
import com.starwar.vo.GameRecordVO;
import com.starwar.vo.LeaderboardVO;

public interface GameService {
    void saveRecord(Long userId, GameRecordDTO recordDTO);
    
    PageResult<GameRecordVO> getUserHistory(Long userId, Integer page, Integer size);
    
    PageResult<GameRecordVO> getLeaderboard(Integer page, Integer size);
    
    LeaderboardVO getLeaderboardWithUser(Long userId);
}


