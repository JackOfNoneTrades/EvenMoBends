package net.gobbob.mobends.compat;

import net.minecraft.util.MathHelper;

/** Per-boat, render-time easing shared by the oars and the player's pose. */
public final class EtFuturumRowingState {
    private static final double REST_TICKS = 6.0;
    private static final double START_TICKS = 4.0;
    private final Paddle left = new Paddle();
    private final Paddle right = new Paddle();
    private double lastTime = Double.NEGATIVE_INFINITY;
    private Pose pose;

    public Pose sample(double time, boolean leftActive, float leftPhase, boolean rightActive, float rightPhase) {
        // Boat, player, armor and shadow passes may all sample the same frame.
        if (this.pose != null && time <= this.lastTime) return this.pose;
        this.left.update(time, this.lastTime, leftActive, leftPhase);
        this.right.update(time, this.lastTime, rightActive, rightPhase);
        this.lastTime = time;
        this.pose = new Pose(this.left.phase, this.right.phase, this.left.stroke, this.right.stroke);
        return this.pose;
    }

    public static final class Pose {
        public final float leftPhase;
        public final float rightPhase;
        public final float leftStroke;
        public final float rightStroke;

        private Pose(float leftPhase, float rightPhase, float leftStroke, float rightStroke) {
            this.leftPhase = leftPhase;
            this.rightPhase = rightPhase;
            this.leftStroke = leftStroke;
            this.rightStroke = rightStroke;
        }

        public static Pose unmodified(float leftPhase, float rightPhase) {
            return new Pose(leftPhase, rightPhase, MathHelper.sin(leftPhase), MathHelper.sin(rightPhase));
        }
    }

    private static final class Paddle {
        private boolean initialized;
        private boolean active;
        private float phase;
        private float stroke;
        private float phaseOffset;
        private float strokeOffset;
        private double transitionStart = Double.NEGATIVE_INFINITY;

        private void update(double time, double lastTime, boolean active, float rawPhase) {
            if (!this.initialized) {
                this.initialized = true;
                this.active = active;
                this.phase = active ? rawPhase : EtFuturumPaddlePose.REST_PHASE;
                this.stroke = active ? MathHelper.sin(rawPhase) : 0.0f;
                return;
            }
            if (this.active != active) {
                this.active = active;
                this.transitionStart = lastTime;
                float targetPhase = active ? rawPhase : EtFuturumPaddlePose.REST_PHASE;
                this.phaseOffset = wrapRadians(this.phase - targetPhase);
                this.strokeOffset = this.stroke - (active ? MathHelper.sin(rawPhase) : 0.0f);
            }
            float progress = (float)Math.min(1.0, (time - this.transitionStart) / (active ? START_TICKS : REST_TICKS));
            float remaining = 1.0f - progress * progress * (3.0f - 2.0f * progress);
            // Take the short path around the cycle, rather than unwinding accumulated strokes.
            this.phase = (active ? rawPhase : EtFuturumPaddlePose.REST_PHASE) + this.phaseOffset * remaining;
            this.stroke = MathHelper.clamp_float(
                (active ? MathHelper.sin(rawPhase) : 0.0f) + this.strokeOffset * remaining, -1.0f, 1.0f);
        }
    }

    private static float wrapRadians(float angle) {
        float turn = (float)(Math.PI * 2.0);
        angle %= turn;
        if (angle >= Math.PI) angle -= turn;
        if (angle < -Math.PI) angle += turn;
        return angle;
    }
}
