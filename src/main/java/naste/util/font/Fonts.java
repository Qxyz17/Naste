package naste.util.font;

/**
 * 字体单例 + 字号分级（Nya 风格，针对中文优化）。
 */
public final class Fonts {
    private Fonts() {}

    // 字号分级（中文优化，比 Nya 的 9/10.5 稍大以保证清晰）
    // Setsuna 字号分级（匹配模块高 17）
    public static final float TITLE  = 12f;
    public static final float BODY   = 9f;
    public static final float SMALL  = 9f;
    public static final float TINY   = 7.5f;

    private static CustomFontRenderer renderer;
    private static CustomFontRenderer iconRenderer;

    /** 懒加载字体渲染器。 */
    public static CustomFontRenderer get() {
        if (renderer == null) {
            // 资源路径（classpath）
            renderer = CustomFontRenderer.fromResource("/fonts/msyh.ttf", BODY);
        }
        return renderer;
    }

    /** 懒加载图标渲染器（lucide 图标字体）。 */
    public static CustomFontRenderer getIconRenderer() {
        if (iconRenderer == null) {
            iconRenderer = CustomFontRenderer.fromResource("/fonts/lucide.ttf", 16f);
        }
        return iconRenderer;
    }

    /** 绘制图标字符（lucide.ttf 的 unicode glyph）。 */
    public static void drawIcon(String glyph, float x, float y, int color, float size) {
        CustomFontRenderer r = getIconRenderer();
        if (r != null) {
            r.drawString(glyph, x, y, color, size);
        }
    }

    /** 图标宽度。 */
    public static float iconWidth(String glyph, float size) {
        CustomFontRenderer r = getIconRenderer();
        if (r != null) {
            return r.getWidth(glyph, size);
        }
        return 0;
    }

    /** 图标垂直居中绘制。 */
    public static void drawIconCenteredY(String glyph, float x, float top, float height, int color, float size) {
        drawIcon(glyph, x, top + (height - lineHeight(size)) / 2f, color, size);
    }

    /** 是否可用（字体未加载时回退到 MC 自带字体）。 */
    public static boolean available() {
        return get() != null;
    }

    /** 统一绘制入口（带阴影）。字体不可用时回退 MC 字体。 */
    public static void draw(String text, float x, float y, int color, float size) {
        CustomFontRenderer r = get();
        if (r != null) {
            r.drawStringShadow(text, x, y, color, size);
        } else {
            net.minecraft.client.Minecraft.getMinecraft().fontRendererObj
                    .drawStringWithShadow(text, x, y, color);
        }
    }

    /** 统一宽度测量。 */
    public static float width(String text, float size) {
        CustomFontRenderer r = get();
        if (r != null) {
            return r.getWidth(text, size);
        }
        return net.minecraft.client.Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
    }

    /** 真实显示行高（比 size 大，约 size * 1.3）。用于垂直居中。 */
    public static float lineHeight(float size) {
        return size * 1.3f;
    }

    /**
     * 在给定高度内垂直居中绘制。
     * @param top    区域顶部 y
     * @param height 区域高
     */
    public static void drawCenteredY(String text, float x, float top, float height, int color, float size) {
        float lh = lineHeight(size);
        draw(text, x, top + (height - lh) / 2f, color, size);
    }
}
