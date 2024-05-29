package com.easybbs.entity.dto;

import com.easybbs.annotation.VerifyParam;

public class SysSetting4CommentDto {
    /**
     * 评论积分
     */
    @VerifyParam(required = true)
    private Integer commentIntegral;

    /**
     * 评论数量阈值
     */
    @VerifyParam(required = true)
    private Integer commentDayCountThreshold;

    @VerifyParam(required = true)
    private Boolean commentOpen;

    public Boolean getCommentOpen() {
        return commentOpen;
    }

    public void setCommentOpen(Boolean commentOpen) {
        this.commentOpen = commentOpen;
    }

    public Integer getCommentIntegral() {
        return commentIntegral;
    }

    public void setCommentIntegral(Integer commentIntegral) {
        this.commentIntegral = commentIntegral;
    }

    public Integer getCommentDayCountThreshold() {
        return commentDayCountThreshold;
    }

    public void setCommentDayCountThreshold(Integer commentDayCountThreshold) {
        this.commentDayCountThreshold = commentDayCountThreshold;
    }
}
