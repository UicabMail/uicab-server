package cn.uicab.statics;

public enum UserStatus {

    DELAY(-1), NORMAL(0), Block(2);

    private int value;

    private UserStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}