package com.starwar.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Post {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer replyCount;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


