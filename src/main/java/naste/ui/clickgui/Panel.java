package naste.ui.clickgui;

import naste.module.Module;
import naste.util.animation.Animation;
import naste.util.animation.Easings;
import naste.util.render.Render2D;
import naste.util.render.UiTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类面板（Setsuna 风格）。
 * 标题栏 + 统一 body 容器（模块是 body 里的行）。
 * 左键拖动标题栏，右键折叠。
 */
public class Panel {
    public static final float HEADER_H = 20f;
    public static final float WIDTH = 112f;
    private static final float PADDING = 3f;
    private static final float MODULE_GAP = 0f;

    private final String name;
    private final List<ModuleButton> buttons = new ArrayList<>();

    private float x;
    private float y;
    private boolean expanded = true;
    private boolean dragging;
    private float dragX, dragY;

    private final Animation expandAnim = new Animation(Easings.CUBIC_OUT, 250, 1f);

    public Panel(String name, List<Module> modules, float x, float y) {
        this.name = name;
        this.x = x;
        this.y = y;
        float by = y + HEADER_H + PADDING;
        for (Module m : modules) {
            ModuleButton b = new ModuleButton(m, this, WIDTH - PADDING * 2);
            b.setPosition(x + PADDING, by);
            buttons.add(b);
            by += b.getHeaderHeight();
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
            by += b.getHeight();
        }
    }

    public String getName() {
        return name;
    }

    private static String categoryGlyph(String category) {
        int cp;
        switch (category) {
            case "Combat":   cp = 0xE2B4; break;
            case "Movement": cp = 0xE3B9; break;
            case "Render":   cp = 0xE1DD; break;
            case "Player":   cp = 0xE19F; break;
            case "Misc":     cp = 0xE29C; break;
            default:         cp = 0xE154; break;
        }
        return String.valueOf((char) cp);
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void render(int mouseX, int mouseY) {
        float eTarget = expanded ? 1f : 0f;
        if (Math.abs(expandAnim.getValue() - eTarget) > 0.001f && !expandAnim.isRunning()) {
            expandAnim.animate(eTarget);
        }
        float ep = expandAnim.getValue();

        // 内容高度
        float contentH = 0;
        for (ModuleButton b : buttons) contentH += b.getHeight();
        float bodyH = contentH + PADDING * 2;

        int accent = UiTheme.accent();

        // 面板投影
        Render2D.fillRoundRect(x + 2, y + 3, WIDTH, HEADER_H + bodyH * ep,
                UiTheme.RADIUS_LARGE, 0x50000000);

        // body（统一背景）
        if (ep > 0.01f) {
            Render2D.fillRoundRect(x, y + HEADER_H, WIDTH, bodyH * ep,
                    UiTheme.RADIUS_LARGE, UiTheme.SURFACE);
        }
        // 标题栏
        Render2D.fillTopRoundedRect(x, y, WIDTH, HEADER_H, UiTheme.RADIUS_LARGE, UiTheme.HEADER);

        // 分类图标
        naste.util.font.Fonts.drawIconCenteredY(categoryGlyph(name), x + 6, y, HEADER_H, accent, 11f);
        // 标题
        naste.util.font.Fonts.drawCenteredY(name, x + 22, y, HEADER_H, UiTheme.TEXT, 9f);
        // 折叠标记
        String arrow = expanded ? "-" : "+";
        float aw = naste.util.font.Fonts.width(arrow, 9f);
        naste.util.font.Fonts.drawCenteredY(arrow, x + WIDTH - 8 - aw, y, HEADER_H, UiTheme.TEXT_MUTED, 9f);

        // 模块行
        if (ep > 0.01f) {
            float by = y + HEADER_H + PADDING;
            for (ModuleButton b : buttons) {
                b.setPosition(x + PADDING, by);
                b.render(mouseX, mouseY);
                by += b.getHeight();
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
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
        if (expanded) {
            float by = y + HEADER_H + PADDING;
            for (ModuleButton b : buttons) {
                b.setPosition(x + PADDING, by);
                by += b.getHeight();
            }
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

    public boolean keyTyped(char typedChar, int keyCode) {
        for (ModuleButton b : buttons) {
            if (b.keyTyped(typedChar, keyCode)) return true;
        }
        return false;
    }

    public boolean isDragging() {
        return dragging;
    }
}
