package com.easybbs.entity.enums;

public enum SysSettingCodeEnum {
    AUDIT("audit", "com.easybbs.entity.dto.SysSetting4AuditDto", "auditStting", "审核设置"),
    COMMENT("comment", "com.easybbs.entity.dto.SysSetting4CommentDto", "commentSetting", "评论设置"),
    POST("post", "com.easybbs.entity.dto.SysSetting4PostDto", "postSetting", "帖子设置"),
    LIKE("like", "com.easybbs.entity.dto.SysSetting4LikeDto", "likeSetting", "点赞设置"),
    REGISTER("register", "com.easybbs.entity.dto.SysSetting4RegisterDto", "registerSetting", "注册设置"),
    EMAIL("email", "com.easybbs.entity.dto.SysSetting4EmailDto", "emailSetting", "邮件设置");

    private String code;
    private String classZ;
    private String propName;
    private String desc;

    SysSettingCodeEnum(String code, String classZ, String propName, String desc) {
        this.code = code;
        this.classZ = classZ;
        this.propName = propName;
        this.desc = desc;
    }

    public static SysSettingCodeEnum getByCode(String code) {
        for (SysSettingCodeEnum item : SysSettingCodeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getClassZ() {
        return classZ;
    }

    public String getPropName() {
        return propName;
    }
}
