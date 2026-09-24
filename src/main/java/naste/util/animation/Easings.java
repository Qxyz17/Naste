package naste.util.animation;

/**
 * 缓动函数库。
 */
public final class Easings {
    private Easings() {}

    public static final Easing LINEAR = new Easing() {
        public float ease(float t) { return t; }
    };

    /** 三次方缓出：起步快、收尾慢。 */
    public static final Easing CUBIC_OUT = new Easing() {
        public float ease(float t) {
            float f = 1f - t;
            return 1f - f * f * f;
        }
    };

    /** 三次方缓入缓出（近似 cubic-bezier(0.4,0,0.2,1)，用于列表展开/页面切换）。 */
    public static final Easing CUBIC_IN_OUT = new Easing() {
        public float ease(float t) {
            return t < 0.5f
                    ? 4f * t * t * t
                    : 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
        }
    };

    /** 指数缓出。 */
    public static final Easing EXPO_OUT = new Easing() {
        public float ease(float t) {
            return t >= 1f ? 1f : 1f - (float) Math.pow(2, -10 * t);
        }
    };

    /** 二次方缓出。 */
    public static final Easing QUAD_OUT = new Easing() {
        public float ease(float t) {
            return 1f - (1f - t) * (1f - t);
        }
    };

    /** 弹性回弹（back out）：用于按钮点击、强调出现。 */
    public static final Easing BACK_OUT = new Easing() {
        public float ease(float t) {
            float c1 = 1.70158f;
            float c3 = c1 + 1f;
            float f = t - 1f;
            return 1f + c3 * f * f * f + c1 * f * f;
        }
    };

    /** 正弦缓入缓出：用于呼吸灯等循环动画。 */
    public static final Easing SINE_IN_OUT = new Easing() {
        public float ease(float t) {
            return -(float) (Math.cos(Math.PI * t) - 1) / 2f;
        }
    };
}
