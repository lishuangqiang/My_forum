package com.easybbs.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebConfig extends AppConfig {
    @Value("${admin.emails:}")
    private String adminEmails;

    /**
     * 发送人
     */
    @Value("${spring.mail.username:}")
    private String sendUserName;

    /**
     * 是否发送邮件
     */
    @Value("${send.mail.open:true}")
    private Boolean isSendEmailCode;

    public String getSendUserName() {
        return sendUserName;
    }

    public Boolean getSendEmailCode() {
        return isSendEmailCode;
    }

    public String getAdminEmails() {
        return adminEmails;
    }
}
