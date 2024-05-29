package com.easybbs.entity.dto;

import com.easybbs.annotation.VerifyParam;

public class SysSetting4AuditDto {
    /**
     * 帖子是否需要审核
     */
    @VerifyParam(required = true)
    private Boolean postAudit;
    /**
     * 评论是否需要审核
     */
    @VerifyParam(required = true)
    private Boolean commentAudit;

    public Boolean getPostAudit() {
        return postAudit;
    }

    public void setPostAudit(Boolean postAudit) {
        this.postAudit = postAudit;
    }

    public Boolean getCommentAudit() {
        return commentAudit;
    }

    public void setCommentAudit(Boolean commentAudit) {
        this.commentAudit = commentAudit;
    }
}
