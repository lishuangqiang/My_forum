package com.easybbs.entity.dto;

import com.easybbs.annotation.VerifyParam;

/**
 * 点赞设置
 */
public class SysSetting4LikeDto {
    /**
     * 点赞数量阈值
     */
    @VerifyParam(required = true)
    private Integer likeDayCountThreshold;

    public Integer getLikeDayCountThreshold() {
        return likeDayCountThreshold;
    }

    public void setLikeDayCountThreshold(Integer likeDayCountThreshold) {
        this.likeDayCountThreshold = likeDayCountThreshold;
    }
}
