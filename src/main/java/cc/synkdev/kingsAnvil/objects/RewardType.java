package cc.synkdev.kingsAnvil.objects;

public enum RewardType {
    CONSOLE("CONSOLE"), PLAYER("PLAYER"), MONEY("MONEY"), XP("XP");
    String arg;
    RewardType(String arg) {
        this.arg = arg;
    }
}
