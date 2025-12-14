package com.starwar.service.impl;

import com.starwar.common.PageResult;
import com.starwar.dto.GameRecordDTO;
import com.starwar.entity.GameRecord;
import com.starwar.mapper.GameRecordMapper;
import com.starwar.service.GameService;
import com.starwar.vo.GameRecordVO;
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
    
    private GameRecordVO convertToVO(GameRecord record) {
        GameRecordVO vo = new GameRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}

