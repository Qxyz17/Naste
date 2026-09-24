package naste.ui.clickgui.setting;

import naste.property.properties.BooleanProperty;
import naste.util.ColorUtils;
import naste.util.animation.Animation;
import naste.util.animation.Easings;
import naste.util.render.Render2D;
import net.minecraft.client.Minecraft;

/**
 * 布尔开关组件（Nya 风格）。
 * 胶囊底 + 圆滑块，两者都走动画。
 */
public class BooleanComponent extends SettingComponent {
    // Nya 配色
    private static final int TRACK_OFF = 0xFF2A2A33;
    private static final int TRACK_ON  = 0xFFBB86FC;
    private static final int KNOB      = 0xFFFFFFFF;
    private static final int TEXT      = 0xFFCCCCCC;
    private static final int TEXT_ON   = 0xFFFFFFFF;

    private final BooleanProperty property;
    private final Animation toggleAnim = new Animation(Easings.CUBIC_OUT, 180, 0f);

    public BooleanComponent(BooleanProperty property, float x, float y, float width) {
        super(x, y, width);
        this.property = property;
        this.toggleAnim.setValue(property.getValue() ? 1f : 0f);
    }

    @Override
    public float getHeight() {
        return 17f;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        updateHover(mouseX, mouseY);

        // 同步 toggle 动画目标
        float target = property.getValue() ? 1f : 0f;
        if (Math.abs(toggleAnim.getValue() - target) > 0.001f && !toggleAnim.isRunning()) {
            toggleAnim.animate(target);
        }
        float t = toggleAnim.getValue();

        float h = getHeight();
        // 左侧：属性名
        int textColor = ColorUtils.interpolate(TEXT, TEXT_ON, t);
        float size = naste.util.font.Fonts.SMALL;
        naste.util.font.Fonts.drawCenteredY(property.getName().replace("-", " "),
                x + 4, y, h, textColor, size);

        // 右侧：胶囊开关
        float trackW = 20f;
        float trackH = 11f;
        float trackX = x + width - trackW - 2;
        float trackY = y + (h - trackH) / 2f;
        float radius = trackH / 2f;

        // 胶囊底（off -> on 插值变色）
        int trackColor = ColorUtils.interpolate(TRACK_OFF, TRACK_ON, t);
        Render2D.fillRoundRect(trackX, trackY, trackW, trackH, radius, trackColor);

        // 滑块（x 插值）
        float knobR = (trackH - 3) / 2f;
        float knobMinX = trackX + knobR + 1.5f;
        float knobMaxX = trackX + trackW - knobR - 1.5f;
        float knobX = knobMinX + (knobMaxX - knobMinX) * t;
        float knobY = trackY + trackH / 2f;
        Render2D.fillCircle(knobX, knobY, knobR, KNOB);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            property.setValue(!property.getValue());
            return true;
        }
        return false;
    }
}
