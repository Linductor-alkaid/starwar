package com.starwar.service.impl;

import com.starwar.common.PageResult;
import com.starwar.dto.GameRecordDTO;
import com.starwar.entity.GameRecord;
import com.starwar.mapper.GameRecordMapper;
import com.starwar.service.GameService;
import com.starwar.vo.GameRecordVO;
import com.starwar.vo.LeaderboardVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameServiceImpl implements GameService {
    
    @Autowired
    private GameRecordMapper gameRecordMapper;
    
    @Override
    public void saveRecord(Long userId, GameRecordDTO recordDTO) {
        GameRecord record = new GameRecord();
        record.setUserId(userId);
        record.setScore(recordDTO.getScore());
        record.setLevel(recordDTO.getLevel());
        record.setPlayTime(recordDTO.getPlayTime());
        
        gameRecordMapper.insert(record);
    }
    
    @Override
    public PageResult<GameRecordVO> getUserHistory(Long userId, Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        
        List<GameRecord> records = gameRecordMapper.selectByUserId(userId, offset, size);
        Integer total = gameRecordMapper.countByUserId(userId);
        
        List<GameRecordVO> vos = records.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(vos, total);
    }
    
    @Override
    public PageResult<GameRecordVO> getLeaderboard(Integer page, Integer size) {
        Integer offset = (page - 1) * size;
        
        List<GameRecord> records = gameRecordMapper.selectLeaderboard(offset, size);
        Integer total = gameRecordMapper.countAll();
        
        List<GameRecordVO> vos = records.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        
        return new PageResult<>(vos, total);
    }
    
    @Override
    public LeaderboardVO getLeaderboardWithUser(Long userId) {
        LeaderboardVO leaderboardVO = new LeaderboardVO();
        
        // 查询前10名（每个用户的最高成绩）
        List<GameRecord> topRecords = gameRecordMapper.selectTopScores(10);
        List<GameRecordVO> topList = topRecords.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
        leaderboardVO.setTopList(topList);
        
        // 如果提供了userId，查询当前用户的最高成绩和排名
        if (userId != null) {
            GameRecord userMaxRecord = gameRecordMapper.selectUserMaxScore(userId);
            if (userMaxRecord != null) {
                GameRecordVO currentUserVO = convertToVO(userMaxRecord);
                leaderboardVO.setCurrentUser(currentUserVO);
                
                // 查询当前用户的排名
                Integer rank = gameRecordMapper.getUserRank(userId);
                leaderboardVO.setCurrentUserRank(rank);
            } else {
                leaderboardVO.setCurrentUserRank(null);
            }
        }
        
        // 统计总用户数（有游戏记录的用户数）
        Integer totalUsers = gameRecordMapper.countDistinctUsers();
        leaderboardVO.setTotalUsers(totalUsers);
        
        return leaderboardVO;
    }
    
    private GameRecordVO convertToVO(GameRecord record) {
        GameRecordVO vo = new GameRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}

