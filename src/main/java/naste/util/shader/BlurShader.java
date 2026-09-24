package naste.util.shader;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL20;

/**
 * 9 抽样高斯模糊 shader（Nya 背景模糊用）。
 * 单趟 3x3 近似，配合降采样达到顺滑模糊。
 */
public class BlurShader extends Shader {
    private static final String fragment = String.join(
            "\n",
            "uniform sampler2D texture;",
            "uniform vec2 texelSize;",
            "uniform float radius;",
            "void main(void) {",
            "    vec4 sum = vec4(0.0);",
            "    float total = 0.0;",
            "    for (float x = -radius; x <= radius; x += 1.0) {",
            "        for (float y = -radius; y <= radius; y += 1.0) {",
            "            float weight = 1.0 / (1.0 + abs(x) + abs(y));",
            "            vec2 offset = vec2(x, y) * texelSize;",
            "            sum += texture2D(texture, gl_TexCoord[0].xy + offset) * weight;",
            "            total += weight;",
            "        }",
            "    }",
            "    gl_FragColor = sum / total;",
            "}"
    );

    public BlurShader() {
        super(fragment);
    }

    @Override
    public void onLink() {
        this.setUniform("texture");
        this.setUniform("texelSize");
        this.setUniform("radius");
    }

    @Override
    public void onUse() {
        GL20.glUseProgram(this.programId);
        GL20.glUniform1i(this.getUniformLocationCached("texture"), 0);
        float invW = 1.0f / Minecraft.getMinecraft().displayWidth;
        float invH = 1.0f / Minecraft.getMinecraft().displayHeight;
        GL20.glUniform2f(this.getUniformLocationCached("texelSize"), invW, invH);
        GL20.glUniform1f(this.getUniformLocationCached("radius"), 1.5f);
    }
}
