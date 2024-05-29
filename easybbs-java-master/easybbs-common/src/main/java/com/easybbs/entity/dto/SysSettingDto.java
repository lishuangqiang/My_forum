package com.easybbs.entity.dto;

public class SysSettingDto {
    private SysSetting4AuditDto auditStting;

    private SysSetting4CommentDto commentSetting;

    private SysSetting4PostDto postSetting;

    private SysSetting4LikeDto likeSetting;


    private SysSetting4EmailDto emailSetting;

    private SysSetting4RegisterDto registerSetting;

    public SysSetting4AuditDto getAuditStting() {
        return auditStting;
    }


    public SysSetting4RegisterDto getRegisterSetting() {
        return registerSetting;
    }

    public void setRegisterSetting(SysSetting4RegisterDto registerSetting) {
        this.registerSetting = registerSetting;
    }

    public void setAuditStting(SysSetting4AuditDto auditStting) {
        this.auditStting = auditStting;
    }

    public SysSetting4CommentDto getCommentSetting() {
        return commentSetting;
    }

    public void setCommentSetting(SysSetting4CommentDto commentSetting) {
        this.commentSetting = commentSetting;
    }

    public SysSetting4PostDto getPostSetting() {
        return postSetting;
    }

    public void setPostSetting(SysSetting4PostDto postSetting) {
        this.postSetting = postSetting;
    }

    public SysSetting4LikeDto getLikeSetting() {
        return likeSetting;
    }

    public void setLikeSetting(SysSetting4LikeDto likeSetting) {
        this.likeSetting = likeSetting;
    }

    public SysSetting4EmailDto getEmailSetting() {
        return emailSetting;
    }

    public void setEmailSetting(SysSetting4EmailDto emailSetting) {
        this.emailSetting = emailSetting;
    }

}
