package naste.ui.clickgui.setting;

import naste.property.properties.ModeProperty;
import naste.util.ColorUtils;
import naste.util.render.Render2D;
import net.minecraft.client.Minecraft;

/**
 * 模式组件（Nya 风格）：显示当前模式，点击切换。
 */
public class ModeComponent extends SettingComponent {
    private static final int BG   = 0xFF2A2A33;
    private static final int TEXT = 0xFFCCCCCC;

    private final ModeProperty property;

    public ModeComponent(ModeProperty property, float x, float y, float width) {
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
        float hover = hoverAnim.getValue();

        float h = getHeight();
        // 名字
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                property.getName(), (int) (x + 2), (int) y, TEXT);

        // 模式按钮（背景随 hover 变亮）
        String mode = property.getModeString();
        float btnW = width - 4;
        float btnH = 9f;
        float btnX = x + 2;
        float btnY = y + 12;
        int bg = ColorUtils.interpolate(BG, 0xFF3A3A45, hover);
        Render2D.fillRoundRect(btnX, btnY, btnW, btnH, 4f, bg);

        // 居中的模式文字
        int tw = Minecraft.getMinecraft().fontRendererObj.getStringWidth(mode);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                mode, (int) (btnX + (btnW - tw) / 2f), (int) (btnY + 1), 0xFFFFFFFF);
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
