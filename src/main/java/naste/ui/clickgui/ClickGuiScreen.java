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

    private final List<Panel> panels = new ArrayList<>();
    private final Animation openAnim = new Animation(Easings.CUBIC_OUT, 220, 0f);

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

        // 遮罩
        int overlay = ((int) (0xC8 * open) << 24) | (BG_OVERLAY & 0x00FFFFFF);
        Render2D.fillRect(0, 0, w, h, overlay);

        // 面板
        for (Panel p : panels) {
            p.render(mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
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
        // TODO: 滚动支持（后续）
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
