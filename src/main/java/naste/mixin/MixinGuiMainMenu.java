package naste.mixin;

import naste.util.render.Render2D;
import naste.util.render.UiTheme;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 主菜单美化：标题栏 accent 文字（替代原版 Minecraft Logo）。
 */
@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu {
    @Inject(method = "drawScreen", at = @At("TAIL"))
    private void drawScreen(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        ScaledResolution sr = new ScaledResolution(net.minecraft.client.Minecraft.getMinecraft());
        int w = sr.getScaledWidth();
        // NASTE 标题（放在原版 Logo 下方，避免重叠）
        String title = "NASTE";
        float size = 20f;
        float tw = naste.util.font.Fonts.width(title, size);
        float tx = (w - tw) / 2f;
        float ty = 52f;
        Render2D.glowRoundRect(tx - 4, ty - 2, tw + 8, size + 6, 4f, UiTheme.accent(), 0.8f);
        naste.util.font.Fonts.draw(title, tx, ty, UiTheme.accent(), size);
    }
}
