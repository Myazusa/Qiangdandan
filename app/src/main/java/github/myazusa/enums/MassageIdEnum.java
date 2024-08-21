package github.myazusa.enums;

public enum MassageIdEnum {
    Channel1(1),
    Channel2(2);
    private final int value;
    MassageIdEnum(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
