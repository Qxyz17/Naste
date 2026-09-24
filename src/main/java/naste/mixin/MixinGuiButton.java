package naste.mixin;

import naste.util.font.Fonts;
import naste.util.render.Render2D;
import naste.util.render.UiTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 重写原版按钮渲染（Setsuna 风格）：
 * 取消原版绘制，用 accent 描边 + 圆角 + 居中文字 + hover 效果。
 */
@Mixin(GuiButton.class)
public abstract class MixinGuiButton {
    @Shadow public int xPosition;
    @Shadow public int yPosition;
    @Shadow public int width;
    @Shadow public int height;
    @Shadow public String displayString;
    @Shadow public boolean enabled;
    @Shadow public boolean visible;
    @Shadow public boolean hovered;

    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    private void drawButton(Minecraft mc, int mouseX, int mouseY, CallbackInfo ci) {
        if (!this.visible) return;
        ci.cancel();

        int accent = UiTheme.accent();
        float x = xPosition, y = yPosition, w = width, h = height;
        float radius = Math.max(3.5f, Math.min(6f, h * 0.28f));

        // hover：背景微亮 + 左侧竖条
        if (enabled && hovered) {
            Render2D.fillRoundRect(x, y, w, h, radius, UiTheme.withAlpha(accent, 38));
            Render2D.fillRoundRect(x + 3, y + 5, 3f, h - 10, 1.5f, UiTheme.withAlpha(accent, 225));
        }
        // 描边
        int borderA = !enabled ? 42 : (hovered ? 235 : 118);
        Render2D.strokeRoundRect(x + 0.5f, y + 0.5f, w - 1, h - 1, radius, 1f, UiTheme.withAlpha(accent, borderA));

        // 文字（居中）
        int textColor = !enabled ? UiTheme.TEXT_FAINT : (hovered ? UiTheme.TEXT : UiTheme.TEXT_MUTED);
        float size = Math.max(7f, Math.min(9f, h * 0.43f));
        String label = displayString;
        float tw = Fonts.width(label, size);
        Fonts.drawCenteredY(label, x + (w - tw) / 2f, y, h, textColor, size);
    }
}
