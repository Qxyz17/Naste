package naste.ui.clickgui.setting;

import naste.property.properties.ColorProperty;
import naste.util.render.Render2D;
import naste.util.render.UiTheme;

/**
 * 颜色组件（Setsuna 风格，17 高）：左名字 + 右色块/hex。
 */
public class ColorComponent extends SettingComponent {
    private final ColorProperty property;

    public ColorComponent(ColorProperty property, float x, float y, float width) {
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
        float h = getHeight();

        // 名字（左，垂直居中）
        naste.util.font.Fonts.drawCenteredY(property.getName(), x + 4, y, h,
                UiTheme.TEXT_MUTED, naste.util.font.Fonts.SMALL);

        // 右侧：hex + 色块
        String hex = String.format("%06X", property.getValue());
        float size = naste.util.font.Fonts.TINY;
        float hw = naste.util.font.Fonts.width(hex, size);
        naste.util.font.Fonts.drawCenteredY(hex, x + width - 4 - 30f - hw - 4, y, h, UiTheme.TEXT, size);

        int color = 0xFF000000 | property.getValue();
        float swW = 30f;
        float swH = 11f;
        Render2D.fillRoundRect(x + width - 4 - swW, y + (h - swH) / 2f, swW, swH, 3f, color);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        // TODO: 色板选择（后续实现）。
        return false;
    }
}
