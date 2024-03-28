package com.yuki.entity;

import lombok.Data;

import java.util.List;

@Data
public class CommentGroup {
    private FLComment flComment;
    private List<SLComment> slComments;
}
