package myau.util.animation;

/**
 * 时间驱动动画。
 *
 * 两个关键特性（来自 Nya 设计）：
 * 1. 基于 System.currentTimeMillis()，不是帧计数 —— 不同帧率速度一致。
 * 2. 重定向从当前值开始：animate() 时把 startValue 设为 getValue() —— 中断的动画不跳变。
 */
public class Animation {
    private final Easing easing;
    private final long durationMs;

    private boolean running;
    private long startTime;
    private float startValue;
    private float targetValue;
    private float value;

    public Animation(Easing easing, long durationMs) {
        this.easing = easing;
        this.durationMs = durationMs;
        this.value = 0f;
        this.targetValue = 0f;
    }

    public Animation(Easing easing, long durationMs, float initial) {
        this(easing, durationMs);
        this.value = initial;
        this.targetValue = initial;
    }

    /**
     * 动画到目标值。若正在运行，从当前显示值重新起步（不跳变）。
     */
    public void animate(float target) {
        if (target == this.targetValue && running) return;
        this.startValue = getValue();
        this.targetValue = target;
        this.startTime = System.currentTimeMillis();
        this.running = true;
    }

    public float getValue() {
        if (!running) return value;
        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= durationMs) {
            running = false;
            value = targetValue;
            return value;
        }
        float t = (float) elapsed / (float) durationMs;
        float eased = easing.ease(t);
        value = startValue + (targetValue - startValue) * eased;
        return value;
    }

    public boolean isRunning() {
        // 触发一次求值，确保 running 状态被正确更新
        getValue();
        return running;
    }

    public void setValue(float v) {
        this.value = v;
        this.targetValue = v;
        this.running = false;
    }

    public float getTargetValue() {
        return targetValue;
    }
}
