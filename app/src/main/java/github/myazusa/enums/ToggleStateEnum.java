package github.myazusa.enums;

public enum ToggleStateEnum {
    Default(0),
    Triggered(1),
    Disabled(2);
    private final int value;

    // 构造函数
    ToggleStateEnum(int value) {
        this.value = value;
    }

    // 获取枚举常量的值
    public int getValue() {
        return value;
    }
}
