package naste.ui.clickgui.setting;

import naste.property.properties.ColorProperty;
import naste.util.render.Render2D;
import net.minecraft.client.Minecraft;

/**
 * 颜色组件（Nya 风格）：预览色块 + 点击切换展开（先做简化：显示色块 + hex）。
 */
public class ColorComponent extends SettingComponent {
    private static final int TEXT = 0xFFCCCCCC;

    private final ColorProperty property;

    public ColorComponent(ColorProperty property, float x, float y, float width) {
        super(x, y, width);
        this.property = property;
    }

    @Override
    public float getHeight() {
        return 22f;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        updateHover(mouseX, mouseY);

        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                property.getName(), (int) (x + 2), (int) y, TEXT);

        // 预览色块
        int color = 0xFF000000 | property.getValue();
        float swW = width - 4;
        float swH = 9f;
        Render2D.fillRoundRect(x + 2, y + 12, swW, swH, 4f, color);

        // hex
        String hex = String.format("%06X", property.getValue());
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                hex, (int) (x + 4), (int) (y + 13), 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        // TODO: 色板选择（后续实现）。当前点击无操作。
        return false;
    }
}
