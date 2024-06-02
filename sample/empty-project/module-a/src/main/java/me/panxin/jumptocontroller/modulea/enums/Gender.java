package me.panxin.jumptocontroller.modulea.enums;

public enum Gender {
    MALE("男"),
    FEMALE("女");

    private String description;

    Gender(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    // 可选：提供一个静态方法，根据描述获取枚举值
    public static Gender fromDescription(String description) {
        for (Gender gender : values()) {
            if (gender.getDescription().equals(description)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("未知的性别描述：" + description);
    }
}