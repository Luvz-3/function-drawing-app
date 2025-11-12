package com.functionplotter.drawing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;

import com.functionplotter.coordinate.CoordinateSystem;
import com.functionplotter.config.GraphConfig;

import java.util.ArrayList;
import java.util.List;

public class GraphRenderer {
    private CoordinateSystem coordinateSystem;

    // 删除这行：private AppConfig config;

    // Android绘图工具
    private Paint axisPaint, gridPaint, functionPaint, textPaint, backgroundPaint;
    private Path functionPath;

    // 函数数据
    private List<double[]> functionsXData;
    private List<double[]> functionsYData;
    private List<Boolean> functionVisibility;
    private List<Integer> functionColors;

    public GraphRenderer(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        this.functionsXData = new ArrayList<>();
        this.functionsYData = new ArrayList<>();
        this.functionVisibility = new ArrayList<>();
        this.functionColors = new ArrayList<>();

        initPaints();
        functionPath = new Path();
    }

    private void initPaints() {
        // 背景画笔
        backgroundPaint = new Paint();
        backgroundPaint.setColor(GraphConfig.BACKGROUND_COLOR);
        backgroundPaint.setStyle(Paint.Style.FILL);

        // 坐标轴画笔
        axisPaint = new Paint();
        axisPaint.setColor(GraphConfig.AXIS_COLOR);
        axisPaint.setStrokeWidth(GraphConfig.AXIS_WIDTH);
        axisPaint.setStyle(Paint.Style.STROKE);
        axisPaint.setAntiAlias(GraphConfig.ANTIALIASING);

        // 网格画笔
        gridPaint = new Paint();
        gridPaint.setColor(GraphConfig.GRID_COLOR);
        gridPaint.setStrokeWidth(GraphConfig.GRID_WIDTH);
        gridPaint.setStyle(Paint.Style.STROKE);

        // 函数曲线画笔
        functionPaint = new Paint();
        functionPaint.setColor(GraphConfig.getFunctionColor(0));
        functionPaint.setStrokeWidth(GraphConfig.FUNCTION_WIDTH);
        functionPaint.setStyle(Paint.Style.STROKE);
        functionPaint.setAntiAlias(GraphConfig.ANTIALIASING);

        // 文本画笔
        textPaint = new Paint();
        textPaint.setColor(GraphConfig.AXIS_COLOR);
        textPaint.setTextSize(36);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setAntiAlias(GraphConfig.ANTIALIASING);
    }

    public void render(Canvas canvas, int width, int height) {
        // 更新坐标系尺寸
        coordinateSystem.setScreenSize(width, height);

        // 绘制背景
        drawBackground(canvas, width, height);

        // 绘制网格
        if (GraphConfig.SHOW_GRID) {
            drawGrid(canvas, width, height);
        }

        // 绘制坐标轴
        drawAxes(canvas, width, height);

        // 绘制函数图像
        drawFunctions(canvas);

        // 绘制坐标标签
        if (GraphConfig.SHOW_LABELS) {
            drawLabels(canvas, width, height);
        }
    }

    private void drawBackground(Canvas canvas, int width, int height) {
        canvas.drawRect(0, 0, width, height, backgroundPaint);
    }

    private void drawGrid(Canvas canvas, int width, int height) {
        int gridSpacing = GraphConfig.GRID_SPACING;
        double xMin = coordinateSystem.getXMin();
        double xMax = coordinateSystem.getXMax();
        double yMin = coordinateSystem.getYMin();
        double yMax = coordinateSystem.getYMax();

        // 绘制垂直网格线
        for (double x = Math.ceil(xMin); x <= xMax; x += gridSpacing) {
            if (Math.abs(x) < 1e-10) continue;

            float screenX = coordinateSystem.mathToScreenX(x);
            if (screenX >= 0 && screenX <= width) {
                canvas.drawLine(screenX, 0, screenX, height, gridPaint);
            }
        }

        // 绘制水平网格线
        for (double y = Math.ceil(yMin); y <= yMax; y += gridSpacing) {
            if (Math.abs(y) < 1e-10) continue;

            float screenY = coordinateSystem.mathToScreenY(y);
            if (screenY >= 0 && screenY <= height) {
                canvas.drawLine(0, screenY, width, screenY, gridPaint);
            }
        }
    }

    private void drawAxes(Canvas canvas, int width, int height) {
        // 绘制x轴
        float xAxisY = coordinateSystem.mathToScreenY(0);
        if (xAxisY >= 0 && xAxisY <= height) {
            canvas.drawLine(0, xAxisY, width, xAxisY, axisPaint);

            // 绘制x轴箭头（简化版）
            float arrowSize = 20f;
            Path arrowPath = new Path();
            arrowPath.moveTo(width, xAxisY);
            arrowPath.lineTo(width - arrowSize, xAxisY - arrowSize/2);
            arrowPath.lineTo(width - arrowSize, xAxisY + arrowSize/2);
            arrowPath.close();
            canvas.drawPath(arrowPath, axisPaint);
        }

        // 绘制y轴
        float yAxisX = coordinateSystem.mathToScreenX(0);
        if (yAxisX >= 0 && yAxisX <= width) {
            canvas.drawLine(yAxisX, 0, yAxisX, height, axisPaint);

            // 绘制y轴箭头
            float arrowSize = 20f;
            Path arrowPath = new Path();
            arrowPath.moveTo(yAxisX, 0);
            arrowPath.lineTo(yAxisX - arrowSize/2, arrowSize);
            arrowPath.lineTo(yAxisX + arrowSize/2, arrowSize);
            arrowPath.close();
            canvas.drawPath(arrowPath, axisPaint);
        }
    }

    private void drawFunctions(Canvas canvas) {
        for (int i = 0; i < functionsXData.size(); i++) {
            if (functionVisibility.get(i)) {
                drawSingleFunction(canvas, i);
            }
        }
    }

    private void drawSingleFunction(Canvas canvas, int functionIndex) {
        double[] xData = functionsXData.get(functionIndex);
        double[] yData = functionsYData.get(functionIndex);

        if (xData.length == 0 || yData.length == 0) return;

        // 设置函数颜色
        int color = functionColors.size() > functionIndex ?
                functionColors.get(functionIndex) : GraphConfig.getFunctionColor(functionIndex);
        functionPaint.setColor(color);

        functionPath.reset();
        boolean isFirstPoint = true;
        float lastX = 0, lastY = 0;  // 跟踪上一个点

        // 使用Path绘制连续曲线
        for (int i = 0; i < xData.length; i++) {
            if (!Double.isNaN(yData[i]) && !Double.isInfinite(yData[i]) &&
                    coordinateSystem.isPointVisible(xData[i], yData[i])) {

                float screenX = coordinateSystem.mathToScreenX(xData[i]);
                float screenY = coordinateSystem.mathToScreenY(yData[i]);

                if (isFirstPoint) {
                    functionPath.moveTo(screenX, screenY);
                    isFirstPoint = false;
                    lastX = screenX;
                    lastY = screenY;
                } else {
                    // 使用变量跟踪上一个点
                    float distance = (float) Math.sqrt(
                            Math.pow(screenX - lastX, 2) + Math.pow(screenY - lastY, 2)
                    );

                    if (distance < 200) { // 最大跳跃距离
                        functionPath.lineTo(screenX, screenY);
                    } else {
                        functionPath.moveTo(screenX, screenY); // 重新开始
                    }

                    lastX = screenX;
                    lastY = screenY;
                }
            }
        }

        canvas.drawPath(functionPath, functionPaint);

        // 绘制数据点（简化版）
        if (xData.length < 100) {
            for (int i = 0; i < xData.length; i++) {
                if (!Double.isNaN(yData[i]) && !Double.isInfinite(yData[i]) &&
                        coordinateSystem.isPointVisible(xData[i], yData[i])) {

                    float screenX = coordinateSystem.mathToScreenX(xData[i]);
                    float screenY = coordinateSystem.mathToScreenY(yData[i]);
                    canvas.drawCircle(screenX, screenY, 4f, functionPaint);
                }
            }
        }
    }

    private void drawLabels(Canvas canvas, int width, int height) {
        // 修改这行：使用GraphConfig替代config
        int gridSpacing = GraphConfig.GRID_SPACING;
        double xMin = coordinateSystem.getXMin();
        double xMax = coordinateSystem.getXMax();
        double yMin = coordinateSystem.getYMin();
        double yMax = coordinateSystem.getYMax();

        // 绘制x轴标签
        for (double x = Math.ceil(xMin); x <= xMax; x += gridSpacing) {
            if (Math.abs(x) < 1e-10) continue;

            float screenX = coordinateSystem.mathToScreenX(x);
            float screenY = coordinateSystem.mathToScreenY(0);

            if (screenX >= 50 && screenX <= width - 50 &&
                    screenY >= 50 && screenY <= height - 50) {

                String label = formatNumber(x);
                // 计算文本宽度（近似值）
                float textWidth = textPaint.measureText(label);
                canvas.drawText(label, screenX - textWidth/2, screenY + 40, textPaint);
            }
        }

        // 绘制y轴标签
        for (double y = Math.ceil(yMin); y <= yMax; y += gridSpacing) {
            if (Math.abs(y) < 1e-10) continue;

            float screenX = coordinateSystem.mathToScreenX(0);
            float screenY = coordinateSystem.mathToScreenY(y);

            if (screenX >= 50 && screenX <= width - 50 &&
                    screenY >= 50 && screenY <= height - 50) {

                String label = formatNumber(y);
                float textWidth = textPaint.measureText(label);
                canvas.drawText(label, screenX - textWidth - 10, screenY + 15, textPaint);
            }
        }

        // 绘制原点标签
        float originX = coordinateSystem.mathToScreenX(0);
        float originY = coordinateSystem.mathToScreenY(0);
        if (originX >= 30 && originX <= width - 30 &&
                originY >= 30 && originY <= height - 30) {
            canvas.drawText("0", originX + 10, originY - 10, textPaint);
        }
    }

    private String formatNumber(double value) {
        if (Math.abs(value) < 1e-10) return "0";
        if (Math.abs(value) < 0.001 || Math.abs(value) > 1000) {
            return String.format("%.1e", value);
        } else {
            return String.format("%.1f", value).replaceAll("\\.0+$", "");
        }
    }

    public void setFunctionData(int index, double[] xData, double[] yData) {
        while (functionsXData.size() <= index) {
            functionsXData.add(new double[0]);
            functionsYData.add(new double[0]);
            functionVisibility.add(true);
            functionColors.add(GraphConfig.getFunctionColor(index));
        }

        functionsXData.set(index, xData);
        functionsYData.set(index, yData);
    }

    public void setFunctionVisibility(int index, boolean visible) {
        if (index < functionVisibility.size()) {
            functionVisibility.set(index, visible);
        }
    }

    public void clearAllFunctions() {
        functionsXData.clear();
        functionsYData.clear();
        functionVisibility.clear();
        functionColors.clear();
    }
}