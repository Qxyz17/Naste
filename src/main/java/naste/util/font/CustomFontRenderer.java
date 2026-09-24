package naste.util.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于 ttf 的自定义字形渲染器（Nya 用 HarmonyOS Sans SC）。
 *
 * 原理：
 *   1. java.awt.Font 加载 ttf
 *   2. 每个字符用 GlyphVector 生成形状
 *   3. 画到 BufferedImage -> 上传成 GL 纹理
 *   4. 缓存字形，按 (char, size) 索引
 *
 * 1.8.9 兼容：LWJGL2 + GlStateManager。
 */
public class CustomFontRenderer {
    /** 单个字形缓存。 */
    private static class Glyph {
        int textureId;
        int width;          // 纹理像素宽（超采样后）
        int height;         // 纹理像素高
        float displayWidth; // 显示宽度（= width / SS）
        float displayHeight;// 显示高度
        int advance;        // 字距（显示坐标）
        int ascent;         // 基线到顶（显示坐标）
        float leftBearing;  // 左侧留白（显示坐标）
    }

    private final Font awtFont;
    private final Map<Long, Glyph> cache = new HashMap<>();
    private final FontRenderContext frc = new FontRenderContext(null, true, true);

    // 用于生成字形的画布
    private final BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    public CustomFontRenderer(Font awtFont) {
        this.awtFont = awtFont;
    }

    /** 从 jar 资源加载字体。 */
    public static CustomFontRenderer fromResource(String path, float size) {
        try (InputStream is = CustomFontRenderer.class.getResourceAsStream(path)) {
            if (is == null) {
                return null;
            }
            Font base = Font.createFont(Font.TRUETYPE_FONT, is);
            return new CustomFontRenderer(base.deriveFont(size));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private long key(char c, float size) {
        return ((long) c << 32) | (Float.floatToIntBits(size) & 0xFFFFFFFFL);
    }

    /** 超采样倍率：字形按 size*SS 生成，显示时缩放回来，以获得平滑边缘。 */
    private static final int SS = 2;

    /**
     * 获取字形。关键：所有字形使用**统一 baseline**（字体 ascent），
     * 避免不同字符 bounds 不同导致的"歪、大小不一"。
     */
    private Glyph getGlyph(char c, float size) {
        long k = key(c, size);
        Glyph g = cache.get(k);
        if (g != null) return g;

        float renderSize = size * SS;
        Font font = awtFont.deriveFont(renderSize);

        // 统一基准：用 FontMetrics 的 ascent/descent
        java.awt.font.FontRenderContext frc = this.frc;
        java.awt.font.LineMetrics lm = font.getLineMetrics("Ag", frc);
        int ascent = (int) Math.ceil(lm.getAscent());
        int descent = (int) Math.ceil(lm.getDescent());

        String s = String.valueOf(c);
        GlyphVector gv = font.createGlyphVector(frc, s);

        // 字符的水平边界（用于确定纹理宽度），但垂直位置统一用 baseline
        java.awt.geom.Rectangle2D visual = gv.getVisualBounds();
        int left = (int) Math.floor(visual.getMinX()) - 1;
        int right = (int) Math.ceil(visual.getMaxX()) + 1;
        int w = Math.max(1, right - left);
        // 高度统一为 ascent + descent（所有字一样高）
        int h = Math.max(1, ascent + descent);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setColor(Color.WHITE);
        g2.setFont(font);
        // 关键：从统一 baseline 画（y = ascent），水平按 visual bounds 对齐
        g2.drawString(s, -left, ascent);
        g2.dispose();

        int texId = uploadTexture(img);

        g = new Glyph();
        g.textureId = texId;
        g.width = w;
        g.height = h;
        g.displayWidth = w / (float) SS;
        g.displayHeight = h / (float) SS;
        // 字距：用字体的统一 advance（不用 per-glyph，避免间距乱）
        g.advance = (int) Math.ceil(font.getStringBounds(s, frc).getWidth() / (float) SS);
        // 左偏移（把 visual left 对齐到 pen position）
        g.leftBearing = left / (float) SS;
        g.ascent = (int) Math.ceil(ascent / (float) SS);
        cache.put(k, g);
        return g;
    }

    private int uploadTexture(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);

        // ARGB -> RGBA
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(w * h * 4)
                .order(java.nio.ByteOrder.nativeOrder());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = pixels[y * w + x];
                buf.put((byte) ((argb >> 16) & 0xFF)); // R
                buf.put((byte) ((argb >> 8) & 0xFF));  // G
                buf.put((byte) (argb & 0xFF));         // B
                buf.put((byte) ((argb >> 24) & 0xFF)); // A
            }
        }
        buf.flip();

        int texId = GL11.glGenTextures();
        GlStateManager.bindTexture(texId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return texId;
    }

    /** 绘制字符串。x/y 为左上角。 */
    public void drawString(String text, float x, float y, int color, float size) {
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(r, g, b, a);

        float cx = x;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                cx += size * 0.3f;
                continue;
            }
            Glyph glyph = getGlyph(c, size);
            if (glyph == null) continue;

            GlStateManager.bindTexture(glyph.textureId);
            // 水平：pen position + 左留白（统一 baseline 对齐）
            float gx = cx + glyph.leftBearing;
            float gw = glyph.displayWidth;
            float gh = glyph.displayHeight;
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0);
            GL11.glVertex2f(gx, y);
            GL11.glTexCoord2f(0, 1);
            GL11.glVertex2f(gx, y + gh);
            GL11.glTexCoord2f(1, 1);
            GL11.glVertex2f(gx + gw, y + gh);
            GL11.glTexCoord2f(1, 0);
            GL11.glVertex2f(gx + gw, y);
            GL11.glEnd();

            cx += glyph.advance;
        }

        GlStateManager.resetColor();
        GlStateManager.disableBlend();
    }

    /** 带阴影绘制。 */
    public void drawStringShadow(String text, float x, float y, int color, float size) {
        int shadowColor = (color & 0xFF000000);
        drawString(text, x + 1, y + 1, shadowColor, size);
        drawString(text, x, y, color, size);
    }

    /** 字体行高（近似）。 */
    public float getHeight(float size) {
        return size + 2f;
    }

    /** 测量字符串宽度。 */
    public float getWidth(String text, float size) {
        float w = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                w += size * 0.3f;
                continue;
            }
            Glyph glyph = getGlyph(c, size);
            if (glyph != null) w += glyph.advance;
        }
        return w;
    }
}
