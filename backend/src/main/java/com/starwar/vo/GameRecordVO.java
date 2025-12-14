package com.starwar.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameRecordVO {
    private Long id;
    private Long userId;
    private String username;
    private Integer score;
    private Integer level;
    private Integer playTime;
    private LocalDateTime recordDate;
}


