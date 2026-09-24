package naste.ui.clickgui;

import naste.Myau;
import naste.property.Property;
import naste.property.properties.*;
import naste.ui.clickgui.setting.*;
import naste.util.animation.Animation;
import naste.util.animation.Easings;
import naste.util.render.Render2D;
import naste.util.render.UiTheme;
import naste.module.Module;
import naste.util.KeyBindUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 模块行（Setsuna 风格）。
 * 模块是 Panel body 里的一行（17 高），无独立外框；
 * 展开时设置行内缩显示。
 */
public class ModuleButton {
    public static final float HEADER_H = 17f;
    private static final float SECTION_INSET = 4f;

    private final Module module;
    private final Panel panel;

    public float x;
    public float y;
    private float width;

    private boolean expanded;
    private boolean binding;

    private final Animation toggleAnim = new Animation(Easings.CUBIC_OUT, 200, 0f);
    private final Animation hoverAnim  = new Animation(Easings.CUBIC_OUT, 150, 0f);
    private final Animation expandAnim = new Animation(Easings.CUBIC_OUT, 250, 0f);

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
        float sy = y + HEADER_H + 1;
        for (Property<?> prop : props) {
            SettingComponent c = null;
            if (prop instanceof BooleanProperty) {
                c = new BooleanComponent((BooleanProperty) prop, x, sy, width);
            } else if (prop instanceof FloatProperty) {
                FloatProperty fp = (FloatProperty) prop;
                c = new NumberComponent(fp.getName(), new NumberComponent.NumberHolder() {
                    public float get() { return fp.getValue(); }
                    public void set(float v) { fp.setValue(v); }
                    public float getMin() { return fp.getMinimum(); }
                    public float getMax() { return fp.getMaximum(); }
                    public String display() { return String.format("%.2f", fp.getValue()); }
                    public boolean integer() { return false; }
                }, x, sy, width);
            } else if (prop instanceof IntProperty) {
                IntProperty ip = (IntProperty) prop;
                c = new NumberComponent(ip.getName(), new NumberComponent.NumberHolder() {
                    public float get() { return ip.getValue(); }
                    public void set(float v) { ip.setValue((int) v); }
                    public float getMin() { return ip.getMinimum(); }
                    public float getMax() { return ip.getMaximum(); }
                    public String display() { return String.valueOf(ip.getValue()); }
                    public boolean integer() { return true; }
                }, x, sy, width);
            } else if (prop instanceof PercentProperty) {
                PercentProperty pp = (PercentProperty) prop;
                c = new NumberComponent(pp.getName(), new NumberComponent.NumberHolder() {
                    public float get() { return pp.getValue(); }
                    public void set(float v) { pp.setValue((int) v); }
                    public float getMin() { return pp.getMinimum(); }
                    public float getMax() { return pp.getMaximum(); }
                    public String display() { return pp.getValue() + "%"; }
                    public boolean integer() { return true; }
                }, x, sy, width);
            } else if (prop instanceof ModeProperty) {
                c = new ModeComponent((ModeProperty) prop, x, sy, width);
            } else if (prop instanceof ColorProperty) {
                c = new ColorComponent((ColorProperty) prop, x, sy, width);
            }
            if (c != null) {
                settings.add(c);
                sy += c.getHeight() + 1;
            }
        }
    }

    public float getHeaderHeight() {
        return HEADER_H;
    }

    public float getHeight() {
        float h = HEADER_H;
        if (expanded) {
            for (SettingComponent c : settings) h += c.getHeight() + 1;
        }
        return h;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        float sy = y + HEADER_H + 1;
        for (SettingComponent c : settings) {
            c.setPosition(x, sy);
            c.setWidth(width);
            sy += c.getHeight() + 1;
        }
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public void render(int mouseX, int mouseY) {
        float tTarget = module.isEnabled() ? 1f : 0f;
        if (Math.abs(toggleAnim.getValue() - tTarget) > 0.001f && !toggleAnim.isRunning()) toggleAnim.animate(tTarget);
        boolean hovering = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEADER_H;
        float hTarget = hovering ? 1f : 0f;
        if (Math.abs(hoverAnim.getValue() - hTarget) > 0.001f && !hoverAnim.isRunning()) hoverAnim.animate(hTarget);
        float eTarget = expanded ? 1f : 0f;
        if (Math.abs(expandAnim.getValue() - eTarget) > 0.001f && !expandAnim.isRunning()) expandAnim.animate(eTarget);

        float toggle = toggleAnim.getValue();
        float hover = hoverAnim.getValue();
        float expand = expandAnim.getValue();

        int accent = UiTheme.accent();

        // 行背景（hover 时微亮，开启时淡 accent）
        int rowBg = 0;
        if (hover > 0.01f) rowBg = UiTheme.withAlpha(UiTheme.SURFACE_HOVER, (int) (140 * hover));
        if (toggle > 0.01f) {
            int accentBg = UiTheme.withAlpha(accent, (int) (60 * toggle));
            rowBg = rowBg == 0 ? accentBg : naste.util.ColorUtils.blend(rowBg, accentBg);
        }
        if (rowBg != 0) {
            Render2D.fillRect(x, y, width, HEADER_H, rowBg);
        }

        // 开启模块左侧 accent 指示条
        if (toggle > 0.01f) {
            Render2D.fillRoundRect(x, y + 3, 2f, HEADER_H - 6, 1f,
                    UiTheme.withAlpha(accent, (int) (255 * toggle)));
        }

        // 名字（左）
        float nameSize = 9f;
        int nameColor = module.isEnabled() ? UiTheme.TEXT : UiTheme.TEXT_MUTED;
        naste.util.font.Fonts.drawCenteredY(module.getName(), x + 6, y, HEADER_H, nameColor, nameSize);

        // 右侧：绑定 / ^v
        String right;
        if (binding) right = "...";
        else if (module.getKey() != 0) right = KeyBindUtil.getKeyName(module.getKey());
        else right = expanded ? "-" : "+";
        int rightColor = module.getKey() != 0 && !binding ? UiTheme.TEXT_FAINT : UiTheme.TEXT_MUTED;
        float rw = naste.util.font.Fonts.width(right, 9f);
        naste.util.font.Fonts.drawCenteredY(right, x + width - 6 - rw, y, HEADER_H,
                binding ? accent : rightColor, 9f);

        // 设置（展开）：内缩背景
        if (expand > 0.01f) {
            float sy = y + HEADER_H + 1;
            for (SettingComponent c : settings) {
                float ch = c.getHeight();
                Render2D.fillRoundRect(x + SECTION_INSET, sy + 1,
                        width - SECTION_INSET * 2, ch - 2, UiTheme.RADIUS_TINY, UiTheme.SETTING_BG);
                c.setPosition(x, sy);
                c.setWidth(width);
                c.render(mouseX, mouseY);
                sy += ch + 1;
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        boolean onHeader = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + HEADER_H;
        if (onHeader) {
            if (button == 0) { module.toggle(); return true; }
            else if (button == 1) { expanded = !expanded; setPosition(x, y); return true; }
            else if (button == 2) { binding = true; return true; }
        }
        if (expanded) {
            for (SettingComponent c : settings) {
                if (c.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (expanded) for (SettingComponent c : settings) c.mouseReleased(mouseX, mouseY, button);
    }

    public void mouseDragged(int mouseX, int mouseY, int button) {
        if (expanded) for (SettingComponent c : settings) c.mouseDragged(mouseX, mouseY, button);
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        if (binding) {
            if (keyCode == 1 || keyCode == 211) module.setKey(0);
            else module.setKey(keyCode);
            binding = false;
            return true;
        }
        return false;
    }

    public boolean isBinding() {
        return binding;
    }
}
