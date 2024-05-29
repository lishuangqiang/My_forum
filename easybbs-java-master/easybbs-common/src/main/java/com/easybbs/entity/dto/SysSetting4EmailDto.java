package com.easybbs.entity.dto;

import com.easybbs.annotation.VerifyParam;

public class SysSetting4EmailDto {

    //邮件标题
    @VerifyParam(required = true)
    private String emailTitle;

    //邮件内容
    @VerifyParam(required = true)
    private String emailContent;

    public String getEmailTitle() {
        return emailTitle;
    }

    public void setEmailTitle(String emailTitle) {
        this.emailTitle = emailTitle;
    }

    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }
}
