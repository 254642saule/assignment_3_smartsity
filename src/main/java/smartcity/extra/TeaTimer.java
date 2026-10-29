package smartcity.extra;
public final class TeaTimer {
    private TeaTimer() {}
    public static int suggestedMinutes(int cups) {
        if (cups < 1) throw new IllegalArgumentException("cups must be positive");
        return cups <= 2 ? 3 : 4;
    }
}
