package myau.util.skia;

/**
 * Nya 风格颜色工具：全部基于 ARGB int（0xAARRGGBB）。
 * 与项目已有的 myau.util.ColorUtil（java.awt.Color 语义）互不影响。
 */
public final class ColorUtils {
    private ColorUtils() {}

    /** 线性插值两个 ARGB 颜色。t∈[0,1]。 */
    public static int interpolate(int a, int b, float t) {
        t = clamp01(t);
        int aa = (a >>> 24) & 0xFF, ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int ba = (b >>> 24) & 0xFF, br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int ra = (int) (aa + (ba - aa) * t);
        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);
        return argb(ra, rr, rg, rb);
    }

    /** 把 overlay 按自身 alpha 混合到 base 上（alpha 混合）。 */
    public static int blend(int base, int overlay) {
        float oa = ((overlay >>> 24) & 0xFF) / 255f;
        if (oa <= 0f) return base;
        if (oa >= 1f) return overlay;
        int ba = (base >>> 24) & 0xFF;
        int br = (base >> 16) & 0xFF, bg = (base >> 8) & 0xFF, bb = base & 0xFF;
        int or = (overlay >> 16) & 0xFF, og = (overlay >> 8) & 0xFF, ob = overlay & 0xFF;
        int rr = (int) (br + (or - br) * oa);
        int rg = (int) (bg + (og - bg) * oa);
        int rb = (int) (bb + (ob - bb) * oa);
        return argb(ba, rr, rg, rb);
    }

    /** 把颜色整体乘以一个透明度系数（0~1），保留原 alpha 的比例。 */
    public static int applyAlpha(int color, float factor) {
        int a = (color >>> 24) & 0xFF;
        int na = (int) (a * clamp01(factor));
        return (na << 24) | (color & 0x00FFFFFF);
    }

    /** 直接设置 alpha（0~255）。 */
    public static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }

    public static int argb(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }
}
