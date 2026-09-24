package naste.mixin;

import naste.util.render.Render2D;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 重写原版屏幕背景：用深色渐变替代原版 dirt 背景。
 */
@Mixin(GuiScreen.class)
public class MixinGuiScreen {
    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    private void drawBackground(int tint, CallbackInfo ci) {
        ci.cancel();
        // 深色渐变背景
        int w = net.minecraft.client.Minecraft.getMinecraft().currentScreen != null
                ? net.minecraft.client.Minecraft.getMinecraft().currentScreen.width : 0;
        int h = net.minecraft.client.Minecraft.getMinecraft().currentScreen != null
                ? net.minecraft.client.Minecraft.getMinecraft().currentScreen.height : 0;
        if (w <= 0 || h <= 0) {
            net.minecraft.client.gui.ScaledResolution sr = new net.minecraft.client.gui.ScaledResolution(
                    net.minecraft.client.Minecraft.getMinecraft());
            w = sr.getScaledWidth();
            h = sr.getScaledHeight();
        }
        Render2D.fillGradientRectV(0, 0, w, h, 0xFF0A0C10, 0xFF16181E);
    }
}
