package com.starwar.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Reply {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String nickname;
    private String content;
    private Long parentId;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


