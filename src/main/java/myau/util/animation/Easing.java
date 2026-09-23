package myau.util.animation;

/**
 * 缓动函数接口。输入归一化进度 t∈[0,1]，输出缓动后的进度。
 */
public interface Easing {
    float ease(float t);
}
