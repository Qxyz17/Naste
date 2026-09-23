package myau.ui.skia;

import io.github.humbleui.skija.Bitmap;
import io.github.humbleui.skija.ImageInfo;
import io.github.humbleui.skija.Surface;
import myau.util.skia.SkiaRenderer;

/**
 * 第 0 步可行性验证：
 * 确认 skija native 库能在 OpenMyau 的运行环境（Forge 1.8.9 + Java 8）加载，
 * 且软件光栅 Surface + Canvas + Paint + 像素回读全链路可用。
 *
 * 另外验证 io.github.humbleui.types（主版本 53）在 Java 8 运行时可用
 * —— 通过调用 SkiaRenderer 的 squircle 方法（依赖 Rect/RRect）。
 *
 * 注意：本项目 toolchain 是 Java 8，本文件不能用 var / 新版语法。
 */
public class SkijaVerify {
    public static void main(String[] args) {
        try {
            System.out.println("[SkijaVerify] loading skija...");

            // 1. 触发 Surface 类静态初始化 -> 加载 native 库
            Surface surface = Surface.makeRasterN32Premul(100, 100);

            // 2. 画一个红色圆
            io.github.humbleui.skija.Canvas canvas = surface.getCanvas();
            io.github.humbleui.skija.Paint paint = new io.github.humbleui.skija.Paint().setColor(0xFFFF0000);
            canvas.drawCircle(50, 50, 30, paint);

            // 3. 通过 SkiaRenderer 画一个顶部圆角矩形（验证 types 库 Rect/RRect 运行时可用）
            SkiaRenderer.fillTopRoundedRect(canvas, 10, 10, 60, 30, 8f, 0xFFBB86FC);
            SkiaRenderer.strokeRoundRect(canvas, 10, 10, 60, 30, 8f, 2f, 0xFFFFFFFF);

            System.out.println("[SkijaVerify] SkiaRenderer squircle + stroke OK");

            // 4. 回读像素
            io.github.humbleui.skija.Image image = surface.makeImageSnapshot();
            Bitmap bitmap = new Bitmap();
            bitmap.allocPixels(ImageInfo.makeN32Premul(100, 100));
            image.readPixels(bitmap, 0, 0);

            System.out.println("[SkijaVerify] Surface + Canvas + Paint OK");
            System.out.println("[SkijaVerify] Bitmap: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            System.out.println("[SkijaVerify] ALL PASS");
        } catch (Throwable t) {
            System.out.println("[SkijaVerify] FAILED");
            t.printStackTrace();
        }
    }
}
