package naste.mixin;

import naste.util.render.Render2D;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 重写原版屏幕背景：用不透明深色渐变替代原版 dirt 背景。
 * 所有 GuiScreen 都画（不透明），以盖住下层父界面，避免"重叠"。
 */
@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void drawBackground(int tint, CallbackInfo ci) {
        ci.cancel();
        net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(
                net.minecraft.client.Minecraft.getMinecraft());
        // 不透明（alpha 255），盖住下层
        Render2D.fillGradientRectV(0, 0, sr.getScaledWidth(), sr.getScaledHeight(),
                0xFF0A0C10, 0xFF16181E);
    }
}
