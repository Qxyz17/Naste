package myau.util.animation;

/**
 * 缓动函数库。Nya 至少需要 LINEAR / CUBIC_OUT / EXPO_OUT。
 */
public final class Easings {
    private Easings() {}

    public static final Easing LINEAR = new Easing() {
        @Override
        public float ease(float t) {
            return t;
        }
    };

    /** 三次方缓出：起步快、收尾慢，最常用的"高级感"曲线。 */
    public static final Easing CUBIC_OUT = new Easing() {
        @Override
        public float ease(float t) {
            float f = 1f - t;
            return 1f - f * f * f;
        }
    };

    /** 指数缓出：比 CUBIC_OUT 更"急"的收尾，用于开关、面板展开。 */
    public static final Easing EXPO_OUT = new Easing() {
        @Override
        public float ease(float t) {
            return t >= 1f ? 1f : 1f - (float) Math.pow(2, -10 * t);
        }
    };

    /** 二次方缓出。 */
    public static final Easing QUAD_OUT = new Easing() {
        @Override
        public float ease(float t) {
            return 1f - (1f - t) * (1f - t);
        }
    };
}
