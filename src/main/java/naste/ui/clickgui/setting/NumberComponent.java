package naste.ui.clickgui.setting;

import naste.util.ColorUtils;
import naste.util.render.Render2D;
import net.minecraft.client.Minecraft;

/**
 * 数值组件（Nya 风格）：滑条 + 拖拽改值。
 * 支持 Float / Int / Percent。
 */
public class NumberComponent extends SettingComponent {
    private static final int TRACK_BG = 0xFF2A2A33;
    private static final int FILL     = 0xFFBB86FC;
    private static final int TEXT     = 0xFFCCCCCC;

    /** 数值读写抽象。 */
    public interface NumberHolder {
        float get();
        void set(float value);
        float getMin();
        float getMax();
        /** 显示字符串。 */
        String display();
        /** 是否为整数（决定取整）。 */
        boolean integer();
    }

    private final String name;
    private final NumberHolder holder;
    private boolean dragging;

    public NumberComponent(String name, NumberHolder holder, float x, float y, float width) {
        super(x, y, width);
        this.name = name;
        this.holder = holder;
    }

    @Override
    public float getHeight() {
        return 22f;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        updateHover(mouseX, mouseY);

        float h = getHeight();
        // 名字（左）
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                name, (int) (x + 2), (int) y, TEXT);
        // 值（右）
        String val = holder.display();
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                val,
                (int) (x + width - 2 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(val)),
                (int) y, TEXT);

        // 滑条
        float trackX = x + 2;
        float trackY = y + 12;
        float trackW = width - 4;
        float trackH = 4f;
        float radius = trackH / 2f;

        Render2D.fillRoundRect(trackX, trackY, trackW, trackH, radius, TRACK_BG);

        float min = holder.getMin();
        float max = holder.getMax();
        float prog = (max - min) == 0 ? 0 : (holder.get() - min) / (max - min);
        prog = Math.max(0, Math.min(1, prog));
        float fillW = trackW * prog;
        if (fillW > 0) {
            Render2D.fillRoundRect(trackX, trackY, fillW, trackH, radius, FILL);
        }
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            dragging = true;
            updateValueFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public void mouseDragged(int mouseX, int mouseY, int button) {
        if (dragging && button == 0) {
            updateValueFromMouse(mouseX);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        dragging = false;
    }

    private void updateValueFromMouse(int mouseX) {
        float trackX = x + 2;
        float trackW = width - 4;
        float prog = (mouseX - trackX) / trackW;
        prog = Math.max(0, Math.min(1, prog));
        float min = holder.getMin();
        float max = holder.getMax();
        float value = min + (max - min) * prog;
        if (holder.integer()) {
            value = Math.round(value);
        }
        holder.set(value);
    }
}
