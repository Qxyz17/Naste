package naste.ui.clickgui;

import naste.module.Module;
import naste.ui.clickgui.ModuleButton;
import naste.util.ColorUtils;
import naste.util.animation.Animation;
import naste.util.animation.Easings;
import naste.util.render.Render2D;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Nya 风格分类面板。
 * 标题栏（分类名 + 旋转箭头）+ 内容区（ModuleButton 列表）。
 * 左键拖动标题栏移动。
 */
public class Panel {
    private static final int HEADER_BG = 0xFF16161B;
    private static final int BODY_BG   = 0xFF0E0E11;
    private static final int ACCENT    = 0xFFBB86FC;
    private static final int TEXT      = 0xFFFFFFFF;

    public static final float HEADER_H = 20f;
    public static final float WIDTH = 110f;
    private static final float PADDING = 3f;

    private final String name;
    private final List<ModuleButton> buttons = new ArrayList<>();

    private float x;
    private float y;
    private boolean expanded = true;
    private boolean dragging;
    private float dragX, dragY;

    private final Animation expandAnim = new Animation(Easings.EXPO_OUT, 250, 1f);

    public Panel(String name, List<Module> modules, float x, float y) {
        this.name = name;
        this.x = x;
        this.y = y;
        float by = y + HEADER_H + PADDING;
        for (Module m : modules) {
            ModuleButton b = new ModuleButton(m, this, WIDTH - PADDING * 2);
            b.setPosition(x + PADDING, by);
            buttons.add(b);
            by += b.getHeaderHeight() + 1;
        }
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        relayout();
    }

    private void relayout() {
        float by = y + HEADER_H + PADDING;
        for (ModuleButton b : buttons) {
            b.setPosition(x + PADDING, by);
            by += b.getHeaderHeight() + 1;
        }
    }

    public String getName() {
        return name;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void render(int mouseX, int mouseY) {
        // expand 动画
        float eTarget = expanded ? 1f : 0f;
        if (Math.abs(expandAnim.getValue() - eTarget) > 0.001f && !expandAnim.isRunning()) {
            expandAnim.animate(eTarget);
        }
        float ep = expandAnim.getValue();

        // 计算内容高度
        float contentH = 0;
        for (ModuleButton b : buttons) contentH += b.getHeight() + 1;
        float bodyH = contentH + PADDING;

        // 面板投影
        Render2D.fillRoundRect(x + 2, y + 3, WIDTH, HEADER_H + bodyH * ep, 8f, 0x50000000);
        // 内容背景
        if (ep > 0.01f) {
            Render2D.fillRoundRect(x, y + HEADER_H, WIDTH, bodyH * ep, 8f, BODY_BG);
        }
        // 标题栏（顶部圆角）
        Render2D.fillTopRoundedRect(x, y, WIDTH, HEADER_H, 8f, HEADER_BG);
        // 强调条（左侧小竖条）
        Render2D.fillRoundRect(x + 4, y + 6, 3f, HEADER_H - 12, 1.5f, ACCENT);

        // 标题
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                name, (int) (x + 12), (int) (y + (HEADER_H - 8) / 2f), TEXT);

        // 展开箭头（+/-，简化用文字）
        String arrow = expanded ? "-" : "+";
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                arrow, (int) (x + WIDTH - 12), (int) (y + (HEADER_H - 8) / 2f), TEXT);

        // 内容（裁剪到 bodyH*ep 高度）
        if (ep > 0.01f) {
            // 简化：不裁剪，直接画（Nya 用 clipRect；这里内容超出可接受）
            float by = y + HEADER_H + PADDING;
            for (ModuleButton b : buttons) {
                b.setPosition(x + PADDING, by);
                b.render(mouseX, mouseY);
                by += b.getHeaderHeight() + 1;
                if (by > y + HEADER_H + bodyH * ep) break;
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        // 标题栏：左键拖动 / 右键折叠
        boolean onHeader = mouseX >= x && mouseX <= x + WIDTH
                && mouseY >= y && mouseY <= y + HEADER_H;
        if (onHeader) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
                return true;
            } else if (button == 1) {
                expanded = !expanded;
                return true;
            }
        }
        // 内容
        if (expanded) {
            for (ModuleButton b : buttons) {
                if (b.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
        for (ModuleButton b : buttons) b.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseDragged(int mouseX, int mouseY, int button) {
        if (dragging) {
            setPosition(mouseX - dragX, mouseY - dragY);
        } else {
            for (ModuleButton b : buttons) b.mouseDragged(mouseX, mouseY, button);
        }
    }

    public boolean isDragging() {
        return dragging;
    }
}
