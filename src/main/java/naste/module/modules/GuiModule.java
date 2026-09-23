package naste.module.modules;

import naste.module.Module;
import naste.ui.clickgui.ClickGuiScreen;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public class GuiModule extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private ClickGuiScreen clickGui;

    public GuiModule() {
        super("ClickGui", false);
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnabled() {
        setEnabled(false);
        clickGui = new ClickGuiScreen();
        mc.displayGuiScreen(clickGui);
    }
}
