package myau.ui.clickgui;

import myau.util.skia.SkiaCanvasBridge;
import myau.util.skia.SkiaRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * Nya 风格 ClickGUI 屏幕（Skia 版）。
 *
 * 第一版：只画一个测试面板，验证 "Skia 软件光栅 -> GL 纹理 -> 屏幕" 闭环。
 */
public class ClickGuiScreen extends GuiScreen {
    private final SkiaCanvasBridge bridge = new SkiaCanvasBridge();

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth();
        int h = sr.getScaledHeight();

        // 1. 准备画布
        bridge.ensureSize(w, h);

        // 2. 清屏为深色底（Nya 底色 #0E0E11，这里加一点透明度）
        bridge.getCanvas().clear(0xE60E0E11);

        // 3. 画测试内容：紫色圆角面板 + 顶部圆角标题栏
        SkiaRenderer.fillRoundRect(bridge.getCanvas(), 20, 20, 200, 120, 12f, 0xFF16161B);
        SkiaRenderer.fillTopRoundedRect(bridge.getCanvas(), 20, 20, 200, 24, 12f, 0xFFBB86FC);
        SkiaRenderer.strokeRoundRect(bridge.getCanvas(), 20, 20, 200, 120, 12f, 1.5f, 0x33FFFFFF);

        // 4. 上传到 GL 纹理
        bridge.upload();

        // 5. 画到屏幕
        GlStateManager.pushMatrix();
        GlStateManager.color(1f, 1f, 1f, 1f);
        bridge.drawToScreen();
        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        bridge.dispose();
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
