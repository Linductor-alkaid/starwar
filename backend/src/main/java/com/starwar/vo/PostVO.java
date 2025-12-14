package com.starwar.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer replyCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


