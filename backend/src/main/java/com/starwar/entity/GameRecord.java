package com.starwar.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class GameRecord {
    private Long id;
    private Long userId;
    private String username;
    private Integer score;
    private Integer level;
    private Integer playTime;
    private LocalDateTime recordDate;
}


