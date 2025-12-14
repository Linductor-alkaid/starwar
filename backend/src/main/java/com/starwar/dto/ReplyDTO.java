package com.starwar.dto;

import lombok.Data;

@Data
public class ReplyDTO {
    private Long postId;
    private String content;
    private Long parentId;
}


