package naste.ui.clickgui;

import naste.Myau;
import naste.module.Module;
import naste.module.modules.*;
import naste.util.animation.Animation;
import naste.util.animation.Easings;
import naste.util.render.Render2D;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Nya 风格 ClickGUI 屏幕（OpenGL 版）。
 * 分类面板 + 模块条目 + 设置组件。
 */
public class ClickGuiScreen extends GuiScreen {
    public static final int BG_OVERLAY = 0xC80E0E11;

    // 背景图
    private int bgTexture = -2; // -2 = 未加载, -1 = 加载失败
    private int bgWidth = 1920, bgHeight = 1080;

    /** 加载背景图（懒加载）。 */
    private int backgroundTexture() {
        if (bgTexture != -2) return bgTexture;
        try {
            // 直接从 jar 读（绕过 MC 资源系统，更可靠）
            java.io.InputStream is = ClickGuiScreen.class.getResourceAsStream("/assets/naste/textures/background.png");
            if (is == null) {
                System.out.println("[Naste] background.png not found in classpath!");
                bgTexture = -1;
                return bgTexture;
            }
            java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(is);
            bgWidth = img.getWidth();
            bgHeight = img.getHeight();
            bgTexture = uploadTexture(img);
            is.close();
            System.out.println("[Naste] background loaded " + bgWidth + "x" + bgHeight + " tex=" + bgTexture);
        } catch (Throwable t) {
            System.out.println("[Naste] background load FAILED: " + t);
            bgTexture = -1;
        }
        return bgTexture;
    }

    private static int uploadTexture(java.awt.image.BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);
        java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocateDirect(w * h * 4)
                .order(java.nio.ByteOrder.nativeOrder());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = pixels[y * w + x];
                buf.put((byte) ((argb >> 16) & 0xFF));
                buf.put((byte) ((argb >> 8) & 0xFF));
                buf.put((byte) (argb & 0xFF));
                buf.put((byte) ((argb >> 24) & 0xFF));
            }
        }
        buf.flip();
        int texId = org.lwjgl.opengl.GL11.glGenTextures();
        net.minecraft.client.renderer.GlStateManager.bindTexture(texId);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER, org.lwjgl.opengl.GL11.GL_LINEAR);
        org.lwjgl.opengl.GL11.glTexParameteri(org.lwjgl.opengl.GL11.GL_TEXTURE_2D,
                org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER, org.lwjgl.opengl.GL11.GL_LINEAR);
        org.lwjgl.opengl.GL11.glTexImage2D(org.lwjgl.opengl.GL11.GL_TEXTURE_2D, 0,
                org.lwjgl.opengl.GL11.GL_RGBA, w, h, 0,
                org.lwjgl.opengl.GL11.GL_RGBA, org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE, buf);
        return texId;
    }

    private final List<Panel> panels = new ArrayList<>();
    private final Animation openAnim = new Animation(Easings.CUBIC_OUT, 220, 0f);

    /** 呼吸灯强度（0.7 ~ 1.0 循环，周期 2s）。 */
    private float breath() {
        double phase = (System.currentTimeMillis() % 2000L) / 2000.0;
        return (float) (0.85 + 0.15 * Math.sin(phase * Math.PI * 2));
    }

    public ClickGuiScreen() {
        // 分类（复用旧 ClickGui 的分类）
        List<Module> combat = new ArrayList<>();
        combat.add(Myau.moduleManager.getModule(AimAssist.class));
        combat.add(Myau.moduleManager.getModule(AutoClicker.class));
        combat.add(Myau.moduleManager.getModule(KillAura.class));
        combat.add(Myau.moduleManager.getModule(Wtap.class));
        combat.add(Myau.moduleManager.getModule(Velocity.class));
        combat.add(Myau.moduleManager.getModule(Freeze.class));
        combat.add(Myau.moduleManager.getModule(Reach.class));
        combat.add(Myau.moduleManager.getModule(TargetStrafe.class));
        combat.add(Myau.moduleManager.getModule(NoHitDelay.class));
        combat.add(Myau.moduleManager.getModule(AntiFireball.class));
        combat.add(Myau.moduleManager.getModule(LagRange.class));
        combat.add(Myau.moduleManager.getModule(HitBox.class));
        combat.add(Myau.moduleManager.getModule(MoreKB.class));
        combat.add(Myau.moduleManager.getModule(Refill.class));
        combat.add(Myau.moduleManager.getModule(HitSelect.class));

        List<Module> movement = new ArrayList<>();
        movement.add(Myau.moduleManager.getModule(AntiAFK.class));
        movement.add(Myau.moduleManager.getModule(Fly.class));
        movement.add(Myau.moduleManager.getModule(Speed.class));
        movement.add(Myau.moduleManager.getModule(LongJump.class));
        movement.add(Myau.moduleManager.getModule(Sprint.class));
        movement.add(Myau.moduleManager.getModule(SafeWalk.class));
        movement.add(Myau.moduleManager.getModule(Jesus.class));
        movement.add(Myau.moduleManager.getModule(Blink.class));
        movement.add(Myau.moduleManager.getModule(NoFall.class));
        movement.add(Myau.moduleManager.getModule(NoSlow.class));
        movement.add(Myau.moduleManager.getModule(KeepSprint.class));
        movement.add(Myau.moduleManager.getModule(Eagle.class));
        movement.add(Myau.moduleManager.getModule(NoJumpDelay.class));
        movement.add(Myau.moduleManager.getModule(AntiVoid.class));

        List<Module> render = new ArrayList<>();
        render.add(Myau.moduleManager.getModule(ESP.class));
        render.add(Myau.moduleManager.getModule(Chams.class));
        render.add(Myau.moduleManager.getModule(FullBright.class));
        render.add(Myau.moduleManager.getModule(Tracers.class));
        render.add(Myau.moduleManager.getModule(NameTags.class));
        render.add(Myau.moduleManager.getModule(Xray.class));
        render.add(Myau.moduleManager.getModule(TargetHUD.class));
        render.add(Myau.moduleManager.getModule(Indicators.class));
        render.add(Myau.moduleManager.getModule(BedESP.class));
        render.add(Myau.moduleManager.getModule(ItemESP.class));
        render.add(Myau.moduleManager.getModule(ViewClip.class));
        render.add(Myau.moduleManager.getModule(NoHurtCam.class));
        render.add(Myau.moduleManager.getModule(HUD.class));
        render.add(Myau.moduleManager.getModule(GuiModule.class));
        render.add(Myau.moduleManager.getModule(ChestESP.class));
        render.add(Myau.moduleManager.getModule(Trajectories.class));
        render.add(Myau.moduleManager.getModule(Radar.class));

        List<Module> player = new ArrayList<>();
        player.add(Myau.moduleManager.getModule(AutoHeal.class));
        player.add(Myau.moduleManager.getModule(AutoTool.class));
        player.add(Myau.moduleManager.getModule(ChestStealer.class));
        player.add(Myau.moduleManager.getModule(InvManager.class));
        player.add(Myau.moduleManager.getModule(InvWalk.class));
        player.add(Myau.moduleManager.getModule(Scaffold.class));
        player.add(Myau.moduleManager.getModule(AutoBlockIn.class));
        player.add(Myau.moduleManager.getModule(SpeedMine.class));
        player.add(Myau.moduleManager.getModule(FastPlace.class));
        player.add(Myau.moduleManager.getModule(GhostHand.class));
        player.add(Myau.moduleManager.getModule(MCF.class));
        player.add(Myau.moduleManager.getModule(AntiDebuff.class));

        List<Module> misc = new ArrayList<>();
        misc.add(Myau.moduleManager.getModule(Spammer.class));
        misc.add(Myau.moduleManager.getModule(BedNuker.class));
        misc.add(Myau.moduleManager.getModule(BedTracker.class));
        misc.add(Myau.moduleManager.getModule(LightningTracker.class));
        misc.add(Myau.moduleManager.getModule(NoRotate.class));
        misc.add(Myau.moduleManager.getModule(NickHider.class));
        misc.add(Myau.moduleManager.getModule(AntiObbyTrap.class));
        misc.add(Myau.moduleManager.getModule(AntiObfuscate.class));
        misc.add(Myau.moduleManager.getModule(AutoAnduril.class));
        misc.add(Myau.moduleManager.getModule(InventoryClicker.class));

        Comparator<Module> cmp = Comparator.comparing(m -> m.getName().toLowerCase());
        combat.sort(cmp);
        movement.sort(cmp);
        render.sort(cmp);
        player.sort(cmp);
        misc.sort(cmp);

        float px = 20;
        float py = 20;
        float gap = 8;
        panels.add(new Panel("Combat", combat, px, py)); px += Panel.WIDTH + gap;
        panels.add(new Panel("Movement", movement, px, py)); px += Panel.WIDTH + gap;
        panels.add(new Panel("Render", render, px, py)); px += Panel.WIDTH + gap;
        panels.add(new Panel("Player", player, px, py)); px += Panel.WIDTH + gap;
        panels.add(new Panel("Misc", misc, px, py));
    }

    @Override
    public void initGui() {
        super.initGui();
        openAnim.animate(1f);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float open = openAnim.getValue();

        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth();
        int h = sr.getScaledHeight();

        // 背景图（cover 铺满）+ 半透明遮罩
        int a = (int) (0xC8 * open);
        int bgTex = backgroundTexture();
        if (bgTex != -1) {
            net.minecraft.client.renderer.GlStateManager.bindTexture(bgTex);
            Render2D.drawTextureCover(0, 0, w, h, bgWidth, bgHeight);
            // 遮罩（60~70% 黑）
            Render2D.fillRect(0, 0, w, h, (a << 24) | 0x000000);
        } else {
            int top = (a << 24) | 0x0A0C10;
            int bottom = (a << 24) | 0x16181E;
            Render2D.fillGradientRectV(0, 0, w, h, top, bottom);
        }

        // 面板
        for (Panel p : panels) {
            p.render(mouseX, mouseY);
        }

        // 调色盘（右上角一排色块）
        renderPalette(w, h, mouseX, mouseY);

        // 水印（呼吸灯）
        float breath = breath() * open;
        String watermark = "Naste";
        float wmSize = 14f;
        float wmW = naste.util.font.Fonts.width(watermark, wmSize);
        float wmX = w - wmW - 10;
        float wmY = 10;
        // 发光底
        Render2D.glowRoundRect(wmX - 2, wmY - 1, wmW + 4, wmSize + 4, 3f,
                naste.util.render.UiTheme.accent(), breath);
        naste.util.font.Fonts.draw(watermark, wmX, wmY,
                naste.util.render.UiTheme.withAlpha(naste.util.render.UiTheme.TEXT, (int) (255 * breath)), wmSize);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // 调色盘布局
    private static final float SWATCH_SIZE = 14f;
    private static final float SWATCH_GAP = 6f;
    private static final float PALETTE_MARGIN = 10f;

    private float paletteX(int w) {
        int count = naste.util.render.UiTheme.presetCount();
        float totalW = count * SWATCH_SIZE + (count - 1) * SWATCH_GAP;
        return w - PALETTE_MARGIN - totalW;
    }

    private float paletteY(int h) {
        return h - PALETTE_MARGIN - SWATCH_SIZE;
    }

    private void renderPalette(int w, int h, int mouseX, int mouseY) {
        int count = naste.util.render.UiTheme.presetCount();
        float px = paletteX(w);
        float py = paletteY(h);
        for (int i = 0; i < count; i++) {
            float sx = px + i * (SWATCH_SIZE + SWATCH_GAP);
            int color = naste.util.render.UiTheme.presetColor(i);
            boolean selected = i == naste.util.render.UiTheme.getAccentIndex();
            boolean hover = mouseX >= sx && mouseX <= sx + SWATCH_SIZE && mouseY >= py && mouseY <= py + SWATCH_SIZE;
            // 色块
            Render2D.fillRoundRect(sx, py, SWATCH_SIZE, SWATCH_SIZE, 4f, color);
            // 选中/悬停描边
            if (selected) {
                Render2D.strokeRoundRect(sx - 1.5f, py - 1.5f, SWATCH_SIZE + 3, SWATCH_SIZE + 3, 5f, 1.5f, 0xFFFFFFFF);
            } else if (hover) {
                Render2D.strokeRoundRect(sx - 1f, py - 1f, SWATCH_SIZE + 2, SWATCH_SIZE + 2, 4.5f, 1f, 0x88FFFFFF);
            }
        }
    }

    private boolean paletteClicked(int mouseX, int mouseY, int w, int h) {
        int count = naste.util.render.UiTheme.presetCount();
        float px = paletteX(w);
        float py = paletteY(h);
        for (int i = 0; i < count; i++) {
            float sx = px + i * (SWATCH_SIZE + SWATCH_GAP);
            if (mouseX >= sx && mouseX <= sx + SWATCH_SIZE && mouseY >= py && mouseY <= py + SWATCH_SIZE) {
                naste.util.render.UiTheme.setAccentIndex(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) throws IOException {
        // 优先给正在绑定的模块
        for (Panel p : panels) {
            if (p.keyTyped(typedChar, keyCode)) return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        if (button == 0) {
            ScaledResolution sr = new ScaledResolution(mc);
            if (paletteClicked(mouseX, mouseY, sr.getScaledWidth(), sr.getScaledHeight())) return;
        }
        for (Panel p : panels) {
            if (p.mouseClicked(mouseX, mouseY, button)) break;
        }
        super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        for (Panel p : panels) {
            p.mouseReleased(mouseX, mouseY, button);
        }
    }

    @Override
    public void mouseClickMove(int mouseX, int mouseY, int button, long timeSinceLast) {
        for (Panel p : panels) {
            p.mouseDragged(mouseX, mouseY, button);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            float delta = wheel > 0 ? 12f : -12f;
            for (Panel p : panels) {
                p.setPosition(p.getX(), p.getY() + delta);
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
