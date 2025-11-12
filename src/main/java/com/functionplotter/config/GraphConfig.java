package com.functionplotter.config;

import android.graphics.Color;

/**
 * Android版本的图形配置类
 * 替代原来的AppConfig
 */
public class GraphConfig {
    // 颜色配置
    public static final int AXIS_COLOR = Color.BLACK;
    public static final int GRID_COLOR = Color.argb(255, 204, 204, 204); // #CCCCCC
    public static final int BACKGROUND_COLOR = Color.WHITE;

    // 线条宽度（像素）
    public static final float AXIS_WIDTH = 3f;
    public static final float FUNCTION_WIDTH = 4f;
    public static final float GRID_WIDTH = 1f;

    // 功能开关
    public static final boolean SHOW_GRID = true;
    public static final boolean SHOW_LABELS = true;
    public static final boolean ANTIALIASING = true;

    // 网格和标签间距
    public static final int GRID_SPACING = 1;

    // 函数颜色序列
    public static final int[] FUNCTION_COLORS = {
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.CYAN
    };

    public static int getFunctionColor(int index) {
        return FUNCTION_COLORS[Math.abs(index) % FUNCTION_COLORS.length];
    }
}