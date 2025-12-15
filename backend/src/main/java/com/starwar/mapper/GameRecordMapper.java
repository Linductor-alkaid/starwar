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
    
    // 查询每个用户的最高成绩（用于排行榜）
    List<GameRecord> selectTopScores(@Param("limit") Integer limit);
    
    // 查询指定用户的最高成绩
    GameRecord selectUserMaxScore(@Param("userId") Long userId);
    
    // 查询指定用户在所有用户最高成绩中的排名
    int getUserRank(@Param("userId") Long userId);
    
    // 统计有游戏记录的用户总数
    int countDistinctUsers();
}


