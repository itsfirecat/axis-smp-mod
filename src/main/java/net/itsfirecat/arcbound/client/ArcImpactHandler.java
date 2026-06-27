package net.itsfirecat.arcbound.client;

public class ArcImpactHandler {

    public enum FrameType {
        WHITE,
        BLACK,
        RED,
        CYAN,
        INVERT
    }

    public static final FrameType[] REGULAR_DAP_SEQUENCE = {
            FrameType.WHITE, FrameType.BLACK, FrameType.WHITE
    };

    public static final FrameType[] PERFECT_DAP_SEQUENCE = {
            FrameType.WHITE, FrameType.BLACK, FrameType.INVERT,
            FrameType.WHITE,   FrameType.INVERT, FrameType.BLACK, FrameType.WHITE
    };

    public static final FrameType[] HEAVEN_DAP_SEQUENCE = {
            FrameType.WHITE, FrameType.WHITE, FrameType.BLACK, FrameType.BLACK,
            FrameType.WHITE, FrameType.WHITE, FrameType.BLACK, FrameType.BLACK,
            FrameType.WHITE, FrameType.WHITE, FrameType.INVERT, FrameType.INVERT
    };

    public static volatile boolean   playing          = false;
    public static volatile boolean   renderingPlayer  = false;
    public static volatile boolean   suppressOverlay  = false;
    public static volatile FrameType currentFrameType = FrameType.WHITE;
    public static volatile boolean   whiteFrame       = true;

    private static FrameType[] sequence        = REGULAR_DAP_SEQUENCE;
    private static long        startMs         = 0;
    private static long        frameDurationMs = 33L;

    public static void start(FrameType[] seq, long durationEach, boolean suppress) {
        sequence        = seq;
        frameDurationMs = durationEach;
        startMs         = System.currentTimeMillis();
        currentFrameType = seq[0];
        whiteFrame = (seq[0] == FrameType.WHITE || seq[0] == FrameType.RED);
        playing         = true;
        suppressOverlay = suppress;
    }
// I FUCKING DID IT
    public static void start(int frames, long durationEach, boolean suppress) {
        FrameType[] seq = new FrameType[frames];
        for (int i = 0; i < frames; i++)
            seq[i] = (i % 2 == 0) ? FrameType.WHITE : FrameType.BLACK;
        start(seq, durationEach, suppress);
    }

    public static void start(int frames, long durationEach) {
        start(frames, durationEach, false);
    }

    public static void tick() {
        if (!playing) return;
        long elapsed  = System.currentTimeMillis() - startMs;
        int  frameIdx = (int)(elapsed / frameDurationMs);
        if (frameIdx >= sequence.length) {
            playing          = false;
            currentFrameType = FrameType.WHITE;
            whiteFrame       = true;
            return;
        }
        currentFrameType = sequence[frameIdx];
        whiteFrame = (currentFrameType == FrameType.WHITE || currentFrameType == FrameType.RED);
    }

// -------------------------------------------------------
// Fading flashbang (separate from frame-sequence flashes)
// -------------------------------------------------------
    public static volatile boolean fadingFlash = false;
    private static long fadeStartMs = 0;
    private static long fadeDurationMs = 0;
    private static FrameType fadeColorType = FrameType.WHITE;

    public static void startFadingFlash(FrameType colorType, long durationMs) {
        fadingFlash = true;
        fadeColorType = colorType;
        fadeStartMs = System.currentTimeMillis();
        fadeDurationMs = durationMs;
    }

    public static float getFadeAlpha() {
        if (!fadingFlash) return 0.0f;
        long elapsed = System.currentTimeMillis() - fadeStartMs;
        if (elapsed >= fadeDurationMs) {
            fadingFlash = false;
            return 0.0f;
        }
        return 1.0f - ((float) elapsed / (float) fadeDurationMs);
    }

    public static FrameType getFadeColorType() {
        return fadeColorType;
    }
    public static long getStartMs() { return startMs; }
}