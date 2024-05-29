package com.easybbs.entity.enums;

public enum MessageStatusEnum {
    NO_READ(1, "未读"),
    READ(2, "已读");

    private Integer status;
    private String desc;

    MessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static MessageStatusEnum getByStatus(Integer status) {
        for (MessageStatusEnum item : MessageStatusEnum.values()) {
            if (item.getStatus().equals(status)) {
                return item;
            }
        }
        return null;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
