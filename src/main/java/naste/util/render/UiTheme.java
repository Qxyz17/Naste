package naste.util.render;

/**
 * 设计 token 系统（移植自 Setsuna UiTheme）。
 * 三级表面 + 三级描边 + 三级文字 + 可切换 accent。
 *
 * 颜色全部为 ARGB int。
 */
public final class UiTheme {
    private UiTheme() {}

    // ---- 表面（半透明深灰，Setsuna 风格）----
    public static final int BACKDROP        = argb(170, 3, 6, 7);
    public static final int SURFACE         = argb(246, 12, 16, 18);
    public static final int SURFACE_ALT     = argb(238, 17, 22, 24);
    public static final int SURFACE_RAISED  = argb(246, 25, 32, 34);
    public static final int SURFACE_HOVER   = argb(250, 31, 40, 42);
    public static final int CONTROL         = argb(244, 21, 27, 29);
    public static final int CONTROL_HOVER   = argb(250, 34, 43, 45);
    public static final int HEADER          = argb(248, 14, 19, 21);

    // 面板/设置块（Setsuna ClickGUI 用）
    public static final int PANEL_BODY      = argb(224, 38, 39, 41);
    public static final int PANEL_EDGE      = argb(135, 8, 10, 13);
    public static final int SETTING_BG      = argb(188, 31, 33, 36);

    // ---- 描边 ----
    public static final int BORDER          = rgb(53, 65, 67);
    public static final int BORDER_STRONG   = rgb(73, 88, 90);
    public static final int BORDER_SOFT     = argb(138, 56, 68, 70);
    public static final int SHADOW          = argb(105, 0, 0, 0);

    // ---- 文字（三级）----
    public static final int TEXT            = rgb(241, 246, 244);
    public static final int TEXT_MUTED      = rgb(166, 178, 174);
    public static final int TEXT_FAINT      = rgb(103, 117, 113);
    public static final int TEXT_DIM        = rgb(170, 170, 170);
    public static final int TEXT_OFF        = rgb(85, 85, 85);

    // ---- 语义色 ----
    public static final int SUCCESS = rgb(84, 211, 143);
    public static final int WARNING = rgb(244, 183, 86);
    public static final int DANGER  = rgb(238, 100, 96);
    public static final int INFO    = rgb(91, 174, 255);

    // ---- 圆角分级（大模块 / 中模块 / 小元素 / 极小元素）----
    public static final float RADIUS_LARGE = 14f;  // 主面板
    public static final float RADIUS       = 8f;   // 卡片/功能组
    public static final float RADIUS_SMALL = 6f;   // 按钮/标签
    public static final float RADIUS_TINY  = 3f;   // 徽章/状态点

    // ---- 可切换 accent ----
    /** 预设配色（名称 -> accent RGB）。 */
    private static final int[][] PRESETS = {
            {62, 214, 180},   // Setsuna 青绿（默认）
            {187, 134, 252},  // Nya 紫
            {91, 174, 255},   // 蓝
            {244, 183, 86},   // 橙
            {238, 100, 96},   // 红
            {84, 211, 143},   // 绿
            {255, 120, 200},  // 粉
    };
    private static final String[] PRESET_NAMES = {
            "Teal", "Purple", "Blue", "Orange", "Red", "Green", "Pink"
    };
    private static int accentIndex = 0;

    public static int accent() {
        int[] c = PRESETS[accentIndex];
        return rgb(c[0], c[1], c[2]);
    }

    public static int accentDark() {
        int[] c = PRESETS[accentIndex];
        return rgb(c[0] * 52 / 100, c[1] * 52 / 100, c[2] * 52 / 100);
    }

    public static int accentSoft() {
        int[] c = PRESETS[accentIndex];
        return argb(46, c[0], c[1], c[2]);
    }

    public static void setAccentIndex(int i) {
        accentIndex = ((i % PRESETS.length) + PRESETS.length) % PRESETS.length;
    }

    public static int getAccentIndex() {
        return accentIndex;
    }

    public static String accentName() {
        return PRESET_NAMES[accentIndex];
    }

    public static int presetCount() {
        return PRESETS.length;
    }

    /** 第 i 个预设的预览色（不改变当前 accent）。 */
    public static int presetColor(int i) {
        int[] c = PRESETS[i];
        return rgb(c[0], c[1], c[2]);
    }

    // ---- 工具 ----
    public static int withAlpha(int color, int alpha) {
        return (clamp(alpha) << 24) | (color & 0x00FFFFFF);
    }

    public static int rgb(int r, int g, int b) {
        return argb(255, r, g, b);
    }

    public static int argb(int a, int r, int g, int b) {
        return (clamp(a) << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
