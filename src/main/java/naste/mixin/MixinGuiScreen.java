package naste.mixin;

import naste.util.render.Render2D;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 原版屏幕背景：背景图 + 遮罩。
 * hook 三个背景方法（无参 drawBackground / 带 tint / drawWorldBackground）。
 */
@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    private static int bgTex = -2;
    private static int bgW = 1920, bgH = 1080;

    private static int bgTexture() {
        if (bgTex != -2) return bgTex;
        try {
            java.io.InputStream is = MixinGuiScreen.class.getResourceAsStream("/assets/naste/textures/background.png");
            if (is == null) { bgTex = -1; return bgTex; }
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(is);
            bgW = img.getWidth();
            bgH = img.getHeight();
            bgTex = upload(img);
            is.close();
        } catch (Throwable t) { bgTex = -1; }
        return bgTex;
    }

    private static int upload(java.awt.image.BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int[] px = new int[w * h];
        img.getRGB(0, 0, w, h, px, 0, w);
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(w * h * 4).order(java.nio.ByteOrder.nativeOrder());
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int c = px[y * w + x];
            buf.put((byte)((c>>16)&0xFF)).put((byte)((c>>8)&0xFF)).put((byte)(c&0xFF)).put((byte)((c>>24)&0xFF));
        }
        buf.flip();
        int id = org.lwjgl.opengl.GL11.glGenTextures();
        net.minecraft.client.renderer.GlStateManager.bindTexture(id);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER, org.lwjgl.opengl.GL11.GL_LINEAR);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER, org.lwjgl.opengl.GL11.GL_LINEAR);
        org.lwjgl.opengl.GL11.glTexImage2D(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0, org.lwjgl.opengl.GL11.GL_RGBA, w, h, 0, org.lwjgl.opengl.GL11.GL_RGBA, org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, buf);
        return id;
    }

    private void drawBg() {
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(
                net.minecraft.client.Minecraft.getMinecraft());
        int w = sr.getScaledWidth(), h = sr.getScaledHeight();
        // 动态背景（视频帧序列）
        naste.util.render.VideoBackground.draw(0, 0, w, h);
        Render2D.fillRect(0, 0, w, h, 0xB0000000); // 遮罩
    }

    // 无参 drawBackground（主菜单用）—— 混淆名 func_148123_a
    @Inject(method = "func_148123_a", at = @At("HEAD"), cancellable = true, remap = false)
    private void drawBg0(CallbackInfo ci) {
        ci.cancel();
        drawBg();
    }

    // 带 tint drawBackground —— func_146278_c
    @Inject(method = "func_146278_c", at = @At("HEAD"), cancellable = true, remap = false)
    private void drawBg1(int tint, CallbackInfo ci) {
        ci.cancel();
        drawBg();
    }

    // drawWorldBackground（游戏内菜单）—— func_146270_b
    @Inject(method = "func_146270_b", at = @At("HEAD"), cancellable = true, remap = false)
    private void drawBg2(int tint, CallbackInfo ci) {
        ci.cancel();
        drawBg();
    }
}
