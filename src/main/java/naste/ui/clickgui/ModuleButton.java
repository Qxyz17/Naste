package naste.ui.clickgui;

import naste.Myau;
import naste.property.Property;
import naste.property.properties.*;
import naste.ui.clickgui.setting.*;
import naste.util.ColorUtils;
import naste.util.animation.Animation;
import naste.util.animation.Easings;
import naste.util.render.Render2D;
import naste.module.Module;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Nya 风格模块条目。
 * 三动画：toggle（开关高亮）、hover、settings（设置展开）。
 * 背景 = 基础色 + hover + toggle 高亮（三层混合）。
 */
public class ModuleButton {
    // Nya 配色
    private static final int BG_BASE   = 0xFF16161B;
    private static final int BG_HOVER  = 0xFF1F1F26;
    private static final int ACCENT    = 0xFFBB86FC;
    private static final int TEXT_ON   = 0xFFFFFFFF;
    private static final int TEXT_OFF  = 0xFF999999;

    private final Module module;
    private final Panel panel;

    public float x;
    public float y;
    private float width;

    private boolean expanded;

    private final Animation toggleAnim  = new Animation(Easings.CUBIC_OUT, 200, 0f);
    private final Animation hoverAnim   = new Animation(Easings.CUBIC_OUT, 150, 0f);
    private final Animation expandAnim  = new Animation(Easings.EXPO_OUT, 250, 0f);

    private final List<SettingComponent> settings = new ArrayList<>();

    public ModuleButton(Module module, Panel panel, float width) {
        this.module = module;
        this.panel = panel;
        this.width = width;
        this.toggleAnim.setValue(module.isEnabled() ? 1f : 0f);
        buildSettings();
    }

    private void buildSettings() {
        ArrayList<Property<?>> props = Myau.propertyManager.properties.get(module.getClass());
        if (props == null) return;
        float sy = y + getHeaderHeight() + 2;
        for (Property<?> prop : props) {
            if (prop instanceof BooleanProperty) {
                SettingComponent c = new BooleanComponent((BooleanProperty) prop, x, sy, width);
                settings.add(c);
                sy += c.getHeight() + 1;
            } else if (prop instanceof FloatProperty) {
                FloatProperty fp = (FloatProperty) prop;
                SettingComponent c = new NumberComponent(fp.getName(), new NumberComponent.NumberHolder() {
                    public float get() { return fp.getValue(); }
                    public void set(float v) { fp.setValue(v); }
                    public float getMin() { return fp.getMinimum(); }
                    public float getMax() { return fp.getMaximum(); }
                    public String display() { return String.format("%.2f", fp.getValue()); }
                    public boolean integer() { return false; }
                }, x, sy, width);
                settings.add(c);
                sy += c.getHeight() + 1;
            } else if (prop instanceof IntProperty) {
                IntProperty ip = (IntProperty) prop;
                SettingComponent c = new NumberComponent(ip.getName(), new NumberComponent.NumberHolder() {
                    public float get() { return ip.getValue(); }
                    public void set(float v) { ip.setValue((int) v); }
                    public float getMin() { return ip.getMinimum(); }
                    public float getMax() { return ip.getMaximum(); }
                    public String display() { return String.valueOf(ip.getValue()); }
                    public boolean integer() { return true; }
                }, x, sy, width);
                settings.add(c);
                sy += c.getHeight() + 1;
            } else if (prop instanceof PercentProperty) {
                PercentProperty pp = (PercentProperty) prop;
                SettingComponent c = new NumberComponent(pp.getName(), new NumberComponent.NumberHolder() {
                    public float get() { return pp.getValue(); }
                    public void set(float v) { pp.setValue((int) v); }
                    public float getMin() { return pp.getMinimum(); }
                    public float getMax() { return pp.getMaximum(); }
                    public String display() { return pp.getValue() + "%"; }
                    public boolean integer() { return true; }
                }, x, sy, width);
                settings.add(c);
                sy += c.getHeight() + 1;
            } else if (prop instanceof ModeProperty) {
                SettingComponent c = new ModeComponent((ModeProperty) prop, x, sy, width);
                settings.add(c);
                sy += c.getHeight() + 1;
            } else if (prop instanceof ColorProperty) {
                SettingComponent c = new ColorComponent((ColorProperty) prop, x, sy, width);
                settings.add(c);
                sy += c.getHeight() + 1;
            }
        }
    }

    public float getHeaderHeight() {
        return 16f;
    }

    public float getHeight() {
        float h = getHeaderHeight();
        if (expanded) {
            for (SettingComponent c : settings) {
                h += c.getHeight() + 1;
            }
            h += 2;
        }
        return h;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
        // 子组件宽度同步（简化：重新布局）
    }

    public void render(int mouseX, int mouseY) {
        // 同步 toggle 动画
        float tTarget = module.isEnabled() ? 1f : 0f;
        if (Math.abs(toggleAnim.getValue() - tTarget) > 0.001f && !toggleAnim.isRunning()) {
            toggleAnim.animate(tTarget);
        }
        // hover 动画
        boolean hovering = mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + getHeaderHeight();
        float hTarget = hovering ? 1f : 0f;
        if (Math.abs(hoverAnim.getValue() - hTarget) > 0.001f && !hoverAnim.isRunning()) {
            hoverAnim.animate(hTarget);
        }
        // expand 动画
        float eTarget = expanded ? 1f : 0f;
        if (Math.abs(expandAnim.getValue() - eTarget) > 0.001f && !expandAnim.isRunning()) {
            expandAnim.animate(eTarget);
        }

        float toggle = toggleAnim.getValue();
        float hover = hoverAnim.getValue();

        // 背景三层混合
        int bg = BG_BASE;
        bg = ColorUtils.blend(bg, ColorUtils.withAlpha(BG_HOVER, (int) (0xFF * hover)));
        bg = ColorUtils.blend(bg, ColorUtils.withAlpha(ACCENT, (int) (0x66 * toggle)));

        float headerH = getHeaderHeight();
        Render2D.fillRect(x, y, width, headerH, bg);

        // 名字（居中）
        String name = module.getName();
        int nameColor = ColorUtils.interpolate(TEXT_OFF, TEXT_ON, toggle);
        int tw = Minecraft.getMinecraft().fontRendererObj.getStringWidth(name);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                name, (int) (x + (width - tw) / 2f), (int) (y + (headerH - 8) / 2f), nameColor);

        // 设置展开：裁剪 + 渲染子组件
        if (expanded || expandAnim.getValue() > 0.01f) {
            float sy = y + headerH + 2;
            for (SettingComponent c : settings) {
                c.setPosition(x, sy);
                c.setWidth(width);
                c.render(mouseX, mouseY);
                sy += c.getHeight() + 1;
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean onHeader = mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + getHeaderHeight();
        if (onHeader) {
            if (button == 0) {
                module.toggle();
                return true;
            } else if (button == 1) {
                expanded = !expanded;
                return true;
            }
        }
        if (expanded) {
            for (SettingComponent c : settings) {
                if (c.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (expanded) {
            for (SettingComponent c : settings) c.mouseReleased(mouseX, mouseY, button);
        }
    }

    public void mouseDragged(int mouseX, int mouseY, int button) {
        if (expanded) {
            for (SettingComponent c : settings) c.mouseDragged(mouseX, mouseY, button);
        }
    }
}
