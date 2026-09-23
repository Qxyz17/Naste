package myau.util.skia;

import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;

/**
 * Skia 绘制助手（Nya 风格）。所有坐标为 GUI 逻辑坐标。
 */
public final class SkiaRenderer {
    private SkiaRenderer() {}

    /** 圆角矩形填充。 */
    public static void fillRoundRect(Canvas canvas, float x, float y, float w, float h,
                                     float radius, int color) {
        try (Paint paint = new Paint()) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            paint.setMode(PaintMode.FILL);
            canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), paint);
        }
    }

    /** 普通矩形填充。 */
    public static void fillRect(Canvas canvas, float x, float y, float w, float h, int color) {
        try (Paint paint = new Paint()) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            paint.setMode(PaintMode.FILL);
            canvas.drawRect(Rect.makeXYWH(x, y, w, h), paint);
        }
    }

    /** 圆角矩形描边。 */
    public static void strokeRoundRect(Canvas canvas, float x, float y, float w, float h,
                                       float radius, float strokeWidth, int color) {
        try (Paint paint = new Paint()) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            paint.setMode(PaintMode.STROKE);
            paint.setStrokeWidth(strokeWidth);
            canvas.drawRRect(RRect.makeXYWH(x, y, w, h, radius), paint);
        }
    }

    /** 圆形填充。 */
    public static void fillCircle(Canvas canvas, float cx, float cy, float radius, int color) {
        try (Paint paint = new Paint()) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            paint.setMode(PaintMode.FILL);
            canvas.drawCircle(cx, cy, radius, paint);
        }
    }

    /**
     * 顶部圆角、底部直角的"squircle"矩形（Nya 面板标题栏用）。
     */
    public static void fillTopRoundedRect(Canvas canvas, float x, float y, float w, float h,
                                          float radius, int color) {
        try (Paint paint = new Paint()) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            paint.setMode(PaintMode.FILL);
            // makeLTRB(l, t, r, b, tl, tr, br, bl)
            canvas.drawRRect(RRect.makeLTRB(x, y, x + w, y + h, radius, radius, 0f, 0f), paint);
        }
    }

    /**
     * 底部圆角、顶部直角的"squircle"矩形（Nya 面板底部用）。
     */
    public static void fillBottomRoundedRect(Canvas canvas, float x, float y, float w, float h,
                                             float radius, int color) {
        try (Paint paint = new Paint()) {
            paint.setColor(color);
            paint.setAntiAlias(true);
            paint.setMode(PaintMode.FILL);
            canvas.drawRRect(RRect.makeLTRB(x, y, x + w, y + h, 0f, 0f, radius, radius), paint);
        }
    }

    /**
     * 阴影（skija 原生）。用于面板投影、按钮悬浮。
     * @param dx/dy 偏移，blur 模糊半径，spread 扩散，color ARGB
     */
    public static void shadowRect(Canvas canvas, float x, float y, float w, float h,
                                  float dx, float dy, float blur, int color) {
        canvas.drawRectShadow(Rect.makeXYWH(x, y, w, h), dx, dy, blur, color);
    }

    /** 在指定矩形内裁剪（配合 save/restore 使用）。 */
    public static void clipRect(Canvas canvas, float x, float y, float w, float h) {
        canvas.clipRect(Rect.makeXYWH(x, y, w, h));
    }
}
