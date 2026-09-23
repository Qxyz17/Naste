package myau.util.skia;

import io.github.humbleui.skija.Bitmap;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.ImageInfo;
import io.github.humbleui.skija.Surface;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Skia 软件光栅画布 -> OpenGL 纹理的桥接。
 *
 * 流程：
 *   1. Surface.makeRasterN32Premul(w, h) —— CPU 画布
 *   2. 用 Skija 画完 -> 读像素到 ByteBuffer
 *   3. 上传为 GL 纹理（复用 textureId，每帧 glTexSubImage2D）
 *   4. 外部用 GlStateManager 把这个纹理画到屏幕
 *
 * 注意：Surface 与纹理按尺寸复用，避免每帧重建。
 */
public final class SkiaCanvasBridge {
    private int width;
    private int height;

    private Surface surface;
    private Canvas canvas;
    private Bitmap readback;
    private byte[] pixelBuffer;

    private int textureId = -1;

    public SkiaCanvasBridge() {}

    /** 确保画布与纹理尺寸正确；尺寸变化时重建。 */
    public void ensureSize(int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (surface != null && w == width && h == height) return;

        // 尺寸变化：释放旧对象
        if (readback != null) readback.close();
        if (surface != null) surface.close();

        this.width = w;
        this.height = h;
        this.surface = Surface.makeRasterN32Premul(w, h);
        this.canvas = surface.getCanvas();
        this.readback = new Bitmap();
        this.readback.allocPixels(ImageInfo.makeN32Premul(w, h));
        this.pixelBuffer = new byte[w * h * 4];

        if (textureId == -1) {
            textureId = GL11.glGenTextures();
        }
    }

    /** 返回 Skija 画布，供上层绘制。调用 ensureSize 之后有效。 */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * 把 Skija 画布内容上传到 GL 纹理。
     * 上传前会自动做上下翻转（Skia 原点在左上，GL 纹理坐标习惯在左下）。
     */
    public void upload() {
        if (surface == null) return;

        // 1. 读回像素（BGRA）
        io.github.humbleui.skija.Image snapshot = surface.makeImageSnapshot();
        snapshot.readPixels(readback, 0, 0);
        snapshot.close();

        byte[] src = readback.readPixels(readback.getImageInfo(), 4L * width, 0, 0);
        if (src == null) return;

        // 2. 垂直翻转（Skia 顶部在前，GL 需要底部在前）
        int rowBytes = width * 4;
        for (int y = 0; y < height; y++) {
            int srcOff = y * rowBytes;
            int dstOff = (height - 1 - y) * rowBytes;
            System.arraycopy(src, srcOff, pixelBuffer, dstOff, rowBytes);
        }

        // 3. 上传
        ByteBuffer buf = ByteBuffer.allocateDirect(pixelBuffer.length).order(ByteOrder.nativeOrder());
        buf.put(pixelBuffer);
        buf.flip();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
    }

    /**
     * 把当前纹理画满屏幕（GUI 逻辑坐标）。参考项目 RenderUtil.drawFramebuffer 的写法。
     * 调用前应已 upload()。
     */
    public void drawToScreen() {
        if (textureId == -1) return;
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.bindTexture(textureId);
        GL11.glBegin(GL11.GL_QUADS);
        // 注意：upload() 已做垂直翻转，因此这里 texCoord 的 v 轴按常规映射即可
        GL11.glTexCoord2d(0.0, 0.0);
        GL11.glVertex2d(0.0, 0.0);
        GL11.glTexCoord2d(0.0, 1.0);
        GL11.glVertex2d(0.0, sr.getScaledHeight());
        GL11.glTexCoord2d(1.0, 1.0);
        GL11.glVertex2d(sr.getScaledWidth(), sr.getScaledHeight());
        GL11.glTexCoord2d(1.0, 0.0);
        GL11.glVertex2d(sr.getScaledWidth(), 0.0);
        GL11.glEnd();
    }

    public int getTextureId() {
        return textureId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /** 释放资源。屏幕关闭时调用。 */
    public void dispose() {
        if (readback != null) {
            readback.close();
            readback = null;
        }
        if (surface != null) {
            surface.close();
            surface = null;
        }
        if (textureId != -1) {
            GL11.glDeleteTextures(textureId);
            textureId = -1;
        }
        canvas = null;
        width = height = 0;
    }
}
