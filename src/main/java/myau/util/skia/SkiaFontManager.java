package myau.util.skia;

import io.github.humbleui.skija.Font;
import io.github.humbleui.skija.FontMgr;
import io.github.humbleui.skija.FontStyle;
import io.github.humbleui.skija.Typeface;

/**
 * Skija 字体管理：加载并缓存字体。
 *
 * 第一版用系统默认无衬线字体（FontMgr 查找），避免依赖项目里不存在的 ttf 资源。
 * 后续可换成 HarmonyOS Sans SC + Material Symbols。
 */
public final class SkiaFontManager {
    private static Typeface regular;
    private static Typeface bold;

    private SkiaFontManager() {}

    /** 获取常规字体（懒加载）。 */
    public static Typeface regular() {
        if (regular == null) {
            regular = FontMgr.getDefault().matchFamilyStyle("sans-serif", FontStyle.NORMAL);
            if (regular == null) {
                regular = FontMgr.getDefault().matchFamilyStyle(null, FontStyle.NORMAL);
            }
        }
        return regular;
    }

    /** 获取粗体。 */
    public static Typeface bold() {
        if (bold == null) {
            bold = FontMgr.getDefault().matchFamilyStyle("sans-serif", FontStyle.BOLD);
            if (bold == null) {
                bold = regular();
            }
        }
        return bold;
    }

    /** 按字号创建 Font（调用方负责 close）。 */
    public static Font font(float sizePx) {
        return new Font(regular(), sizePx);
    }

    /** 按字号创建粗体 Font（调用方负责 close）。 */
    public static Font boldFont(float sizePx) {
        return new Font(bold(), sizePx);
    }
}
