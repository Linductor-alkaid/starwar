package com.starwar.mapper;

import com.starwar.entity.GameRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface GameRecordMapper {
    int insert(GameRecord gameRecord);
    
    List<GameRecord> selectByUserId(@Param("userId") Long userId,
                                     @Param("offset") Integer offset,
                                     @Param("limit") Integer limit);
    
    int countByUserId(Long userId);
    
    List<GameRecord> selectLeaderboard(@Param("offset") Integer offset,
                                       @Param("limit") Integer limit);
    
    int countAll();
}


