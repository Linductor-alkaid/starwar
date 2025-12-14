package com.starwar.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReplyVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String nickname;
    private String content;
    private Long parentId;
    private LocalDateTime createdAt;
}


