package naste.ui.clickgui.setting;

import naste.util.animation.Animation;
import naste.util.animation.Easings;

/**
 * Nya 风格设置组件基类。
 * 每个设置组件都有：布局（x/y/w/h）、悬停动画、渲染、鼠标事件。
 */
public abstract class SettingComponent {
    protected float x;
    protected float y;
    protected float width;

    /** 悬停动画（0 -> 1）。 */
    protected final Animation hoverAnim = new Animation(Easings.CUBIC_OUT, 150, 0f);

    public SettingComponent(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    /** 组件高度。 */
    public abstract float getHeight();

    /** 渲染。 */
    public abstract void render(int mouseX, int mouseY);

    /** 更新悬停状态（在 render 前或每帧调用）。 */
    protected void updateHover(int mouseX, int mouseY) {
        boolean hovering = isHovered(mouseX, mouseY);
        float target = hovering ? 1f : 0f;
        if (Math.abs(hoverAnim.getValue() - target) > 0.001f && !hoverAnim.isRunning()) {
            hoverAnim.animate(target);
        }
    }

    protected boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width
                && mouseY >= y && mouseY <= y + getHeight();
    }

    /** 左键点击。返回是否消费。 */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    public void mouseDragged(int mouseX, int mouseY, int button) {
    }

    public void keyTyped(char typedChar, int keyCode) {
    }
}
