package com.easybbs.entity.dto;

import com.easybbs.annotation.VerifyParam;

public class SysSetting4PostDto {
    /**
     * 发帖积分
     */
    @VerifyParam(required = true)
    private Integer postIntegral;

    /**
     * 一天发帖数量
     */
    @VerifyParam(required = true)
    private Integer postDayCountThreshold;

    /**
     * 每天上传图片数量
     */
    @VerifyParam(required = true)
    private Integer dayImageUploadCount;

    /**
     * 附件大小 单位 mb
     */
    @VerifyParam(required = true)
    private Integer attachmentSize;

    public Integer getPostIntegral() {
        return postIntegral;
    }

    public void setPostIntegral(Integer postIntegral) {
        this.postIntegral = postIntegral;
    }

    public Integer getPostDayCountThreshold() {
        return postDayCountThreshold;
    }

    public void setPostDayCountThreshold(Integer postDayCountThreshold) {
        this.postDayCountThreshold = postDayCountThreshold;
    }

    public Integer getDayImageUploadCount() {
        return dayImageUploadCount;
    }

    public void setDayImageUploadCount(Integer dayImageUploadCount) {
        this.dayImageUploadCount = dayImageUploadCount;
    }

    public Integer getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(Integer attachmentSize) {
        this.attachmentSize = attachmentSize;
    }

}
