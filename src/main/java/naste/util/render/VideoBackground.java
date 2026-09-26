package naste.util.render;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态背景播放器：从 jar 资源读帧序列，按时间循环播放。
 * 帧格式：/assets/naste/textures/bg/f_0001.jpg ...
 */
public final class VideoBackground {
    private static final String DIR = "/assets/naste/textures/bg/";
    private static final String EXT = ".jpg";
    private static final int FRAMES = 300;
    private static final long FRAME_MS = 1000L / 15L; // 15fps

    private static final Map<Integer, Integer> texCache = new HashMap<>();
    private static int lastFrame = -1;
    private static int lastTex = -1;

    private VideoBackground() {}

    /** 当前应显示的帧号（0~FRAMES-1）。 */
    public static int currentFrame() {
        long t = System.currentTimeMillis() % (FRAMES * FRAME_MS);
        return (int) (t / FRAME_MS);
    }

    /** 绘制动态背景（cover 铺满）。 */
    public static void draw(float x, float y, float w, float h) {
        int frame = currentFrame();
        int tex = textureFor(frame);
        if (tex == -1) return;
        GlStateManager.bindTexture(tex);
        Render2D.drawTextureCover(x, y, w, h, 960, 540);
    }

    private static int textureFor(int frame) {
        Integer cached = texCache.get(frame);
        if (cached != null) return cached;
        // 只保留少量缓存（LRU 简化：超过 32 张就清）
        if (texCache.size() > 32) {
            for (int id : texCache.values()) {
                GL11.glDeleteTextures(id);
            }
            texCache.clear();
        }
        int tex = load(frame + 1);
        if (tex != -1) texCache.put(frame, tex);
        return tex;
    }

    private static int load(int index) {
        String name = String.format(DIR + "f_%04d" + EXT, index);
        try (InputStream is = VideoBackground.class.getResourceAsStream(name)) {
            if (is == null) return -1;
            BufferedImage img = javax.imageio.ImageIO.read(is);
            if (img == null) return -1;
            return upload(img);
        } catch (Throwable t) {
            return -1;
        }
    }

    private static int upload(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int[] px = new int[w * h];
        img.getRGB(0, 0, w, h, px, 0, w);
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(w * h * 4).order(java.nio.ByteOrder.nativeOrder());
        for (int i = 0; i < px.length; i++) {
            int c = px[i];
            buf.put((byte)((c>>16)&0xFF)).put((byte)((c>>8)&0xFF)).put((byte)(c&0xFF)).put((byte)((c>>24)&0xFF));
        }
        buf.flip();
        int id = GL11.glGenTextures();
        GlStateManager.bindTexture(id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return id;
    }
}
