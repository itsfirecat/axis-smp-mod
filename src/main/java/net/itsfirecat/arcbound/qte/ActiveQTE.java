package net.itsfirecat.arcbound.qte;

public class ActiveQTE {
    private final QTEType type;
    private final long startTick;
    private final int durationTicks;
    private boolean consumed = false;

    public ActiveQTE(QTEType type, long startTick, int durationTicks) {
        this.type = type;
        this.startTick = startTick;
        this.durationTicks = durationTicks;
    }

    public float getProgress(long currentTick) {
        float progress = (float) (currentTick - startTick) / durationTicks;
        return Math.clamp(progress, 0.0f, 1.0f);
    }

    public boolean isExpired(long currentTick) {
        return currentTick > (startTick + durationTicks);
    }

    public boolean isTimingValid(long currentTick) {
        float progress = getProgress(currentTick);
        return progress >= 0.45f && progress <= 0.55f;
    }

    public QTEType getType() { return type; }
    public boolean isConsumed() { return consumed; }
    public void consume() { this.consumed = true; }
}