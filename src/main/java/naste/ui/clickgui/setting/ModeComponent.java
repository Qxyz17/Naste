package naste.ui.clickgui.setting;

import naste.property.properties.ModeProperty;
import naste.util.render.Render2D;
import naste.util.render.UiTheme;

/**
 * 模式组件（Setsuna 风格，17 高）：左名字 + 右模式，点击循环。
 */
public class ModeComponent extends SettingComponent {
    private final ModeProperty property;

    public ModeComponent(ModeProperty property, float x, float y, float width) {
        super(x, y, width);
        this.property = property;
    }

    @Override
    public float getHeight() {
        return 17f;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        updateHover(mouseX, mouseY);
        float hover = hoverAnim.getValue();
        float h = getHeight();

        // 名字（左，垂直居中）
        naste.util.font.Fonts.drawCenteredY(property.getName(), x + 4, y, h,
                UiTheme.TEXT_MUTED, naste.util.font.Fonts.SMALL);

        // 右侧模式（点击循环）
        String mode = property.getModeString();
        float size = naste.util.font.Fonts.SMALL;
        float tw = naste.util.font.Fonts.width(mode, size);
        float modeX = x + width - 4 - tw;
        int color = hover > 0.01f ? UiTheme.TEXT : UiTheme.accent();
        naste.util.font.Fonts.drawCenteredY(mode, modeX, y, h, color, size);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                property.nextMode();
                return true;
            } else if (button == 1) {
                property.previousMode();
                return true;
            }
        }
        return false;
    }
}
