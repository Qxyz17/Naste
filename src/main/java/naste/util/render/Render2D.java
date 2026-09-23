package naste.util.render;

import naste.util.ColorUtils;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

/**
 * OpenGL 2D 绘制原语（Nya 风格）。
 * 替代 skija 的 SkiaRenderer，用 LWJGL2 + GlStateManager 实现。
 *
 * 所有坐标为 GUI 逻辑坐标。
 */
public final class Render2D {
    private Render2D() {}

    private static void setup() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableTexture2D();
    }

    private static void teardown() {
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    private static void setColor(int argb) {
        float a = (argb >> 24 & 0xFF) / 255f;
        float r = (argb >> 16 & 0xFF) / 255f;
        float g = (argb >> 8 & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        GlStateManager.color(r, g, b, a);
    }

    /** 普通矩形填充。 */
    public static void fillRect(float x, float y, float w, float h, int color) {
        if ((color >>> 24) == 0) return;
        setup();
        setColor(color);
        GL11.glBegin(GL11.GL_POLYGON);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x, y + h);
        GL11.glVertex2f(x + w, y + h);
        GL11.glVertex2f(x + w, y);
        GL11.glEnd();
        teardown();
    }

    /**
     * 圆角矩形填充。用 TRIANGLE_FAN（中心 + 边界点）近似。
     * @param radius 圆角半径
     */
    public static void fillRoundRect(float x, float y, float w, float h, float radius, int color) {
        fillRoundRectSelective(x, y, w, h, radius, radius, radius, radius, color);
    }

    /**
     * 可分别指定四个圆角半径的矩形（用于 squircle：只圆某几个角）。
     * 顺序：左上、右上、右下、左下。
     */
    public static void fillRoundRectSelective(float x, float y, float w, float h,
                                              float rTL, float rTR, float rBR, float rBL,
                                              int color) {
        if ((color >>> 24) == 0) return;
        // 限制半径不超过宽高一半
        float maxR = Math.min(w, h) / 2f;
        rTL = Math.min(rTL, maxR);
        rTR = Math.min(rTR, maxR);
        rBR = Math.min(rBR, maxR);
        rBL = Math.min(rBL, maxR);

        setup();
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        float cx = x + w / 2f;
        float cy = y + h / 2f;
        GL11.glVertex2f(cx, cy); // 扇形中心

        int seg = 6; // 每角分段数
        // 从左上角开始，顺时针绕一圈
        // 左上角：圆心 (x+rTL, y+rTL)，角度 180° -> 270°
        addCorner(x + rTL, y + rTL, rTL, 180, 270, seg);
        // 右上角：圆心 (x+w-rTR, y+rTR)，角度 270° -> 360°
        addCorner(x + w - rTR, y + rTR, rTR, 270, 360, seg);
        // 右下角：圆心 (x+w-rBR, y+h-rBR)，角度 0° -> 90°
        addCorner(x + w - rBR, y + h - rBR, rBR, 0, 90, seg);
        // 左下角：圆心 (x+rBL, y+h-rBL)，角度 90° -> 180°
        addCorner(x + rBL, y + h - rBL, rBL, 90, 180, seg);

        // 闭合回起点
        double a0 = Math.toRadians(180);
        GL11.glVertex2f(x + rTL + (float) Math.cos(a0) * rTL,
                y + rTL + (float) Math.sin(a0) * rTL);

        GL11.glEnd();
        teardown();
    }

    private static void addCorner(float cx, float cy, float r, float startDeg, float endDeg, int seg) {
        for (int i = 0; i <= seg; i++) {
            double angle = Math.toRadians(startDeg + (endDeg - startDeg) * i / (double) seg);
            GL11.glVertex2f(cx + (float) Math.cos(angle) * r, cy + (float) Math.sin(angle) * r);
        }
    }

    /** 顶部圆角、底部直角（面板标题栏）。 */
    public static void fillTopRoundedRect(float x, float y, float w, float h, float radius, int color) {
        fillRoundRectSelective(x, y, w, h, radius, radius, 0, 0, color);
    }

    /** 底部圆角、顶部直角（面板底部）。 */
    public static void fillBottomRoundedRect(float x, float y, float w, float h, float radius, int color) {
        fillRoundRectSelective(x, y, w, h, 0, 0, radius, radius, color);
    }

    /** 圆形填充。 */
    public static void fillCircle(float cx, float cy, float radius, int color) {
        if ((color >>> 24) == 0) return;
        setup();
        setColor(color);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(cx, cy);
        int seg = 32;
        for (int i = 0; i <= seg; i++) {
            double angle = i * (Math.PI * 2.0 / seg);
            GL11.glVertex2f(cx + (float) Math.cos(angle) * radius, cy + (float) Math.sin(angle) * radius);
        }
        GL11.glEnd();
        teardown();
    }

    /** 圆角矩形描边。 */
    public static void strokeRoundRect(float x, float y, float w, float h, float radius, float lineWidth, int color) {
        if ((color >>> 24) == 0) return;
        setup();
        setColor(color);
        GL11.glLineWidth(lineWidth);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

        float maxR = Math.min(w, h) / 2f;
        float r = Math.min(radius, maxR);
        int seg = 6;

        GL11.glBegin(GL11.GL_LINE_LOOP);
        addCornerLine(x + r, y + r, r, 180, 270, seg);
        addCornerLine(x + w - r, y + r, r, 270, 360, seg);
        addCornerLine(x + w - r, y + h - r, r, 0, 90, seg);
        addCornerLine(x + r, y + h - r, r, 90, 180, seg);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        teardown();
    }

    private static void addCornerLine(float cx, float cy, float r, float startDeg, float endDeg, int seg) {
        for (int i = 0; i <= seg; i++) {
            double angle = Math.toRadians(startDeg + (endDeg - startDeg) * i / (double) seg);
            GL11.glVertex2f(cx + (float) Math.cos(angle) * r, cy + (float) Math.sin(angle) * r);
        }
    }
}
