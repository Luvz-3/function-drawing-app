package com.functionplotter.appandroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;  // 添加的导入
import android.view.View;

import androidx.annotation.NonNull;

import com.functionplotter.coordinate.CoordinateSystem;
import com.functionplotter.drawing.GraphRenderer;

public class GraphView extends View {
    private GraphRenderer renderer;
    private CoordinateSystem coordinateSystem;
    private String currentFunction = "";

    // 三个构造函数
    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化坐标系统
        coordinateSystem = new CoordinateSystem(-10.0, 10.0, -10.0, 10.0, 0, 0);

        // 初始化GraphRenderer
        renderer = new GraphRenderer(coordinateSystem);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (coordinateSystem != null) {
            coordinateSystem.setScreenSize(w, h);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (renderer != null) {
            renderer.render(canvas, getWidth(), getHeight());
        } else {
            drawBasicCoordinateSystem(canvas);
        }
    }

    private void drawBasicCoordinateSystem(@NonNull Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        Paint axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(3f);

        canvas.drawLine(0, centerY, width, centerY, axisPaint);
        canvas.drawLine(centerX, 0, centerX, height, axisPaint);

        if (currentFunction != null && !currentFunction.isEmpty()) {
            drawBasicFunction(canvas, currentFunction, width, height);
        }
    }

    private void drawBasicFunction(@NonNull Canvas canvas, String function, int width, int height) {
        if (function == null || function.isEmpty()) {
            return;
        }

        Paint functionPaint = new Paint();
        functionPaint.setColor(Color.RED);
        functionPaint.setStrokeWidth(4f);
        functionPaint.setAntiAlias(true);

        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        Path path = new Path();
        boolean firstPoint = true;

        double startX = -10.0, endX = 10.0, step = 0.1;

        if (function.length() > 20) {
            step = 0.2;
        }

        for (double x = startX; x <= endX; x += step) {
            double mathY = calculateFunctionValue(function, x);

            int screenX = (int)(centerX + x * 50);
            int screenY = (int)(centerY - mathY * 50);

            if (screenX >= 0 && screenX <= width && screenY >= 0 && screenY <= height) {
                if (firstPoint) {
                    path.moveTo(screenX, screenY);
                    firstPoint = false;
                } else {
                    path.lineTo(screenX, screenY);
                }
            }
        }

        canvas.drawPath(path, functionPaint);
    }

    private double calculateFunctionValue(String function, double x) {
        try {
            if (function.contains("sin")) {
                return Math.sin(x);
            } else if (function.contains("cos")) {
                return Math.cos(x);
            } else if (function.contains("x^2") || function.toLowerCase().contains("x*x")) {
                return x * x;
            } else if (function.contains("log")) {
                return Math.log(Math.abs(x) + 1e-10);
            } else {
                return evaluateSimpleExpression(function, x);
            }
        } catch (Exception e) {
            return 0;
        }
    }

    private double evaluateSimpleExpression(String expression, double x) {
        String expr = expression.toLowerCase().replace(" ", "");
        expr = expr.replace("x", String.valueOf(x));

        try {
            if (expr.contains("+")) {
                String[] parts = expr.split("\\+");
                return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
            } else if (expr.contains("*")) {
                String[] parts = expr.split("\\*");
                return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
            } else {
                return Double.parseDouble(expr);
            }
        } catch (Exception e) {
            return x;
        }
    }

    public void plotFunction(String function) {
        this.currentFunction = function;

        if (renderer != null && coordinateSystem != null) {
            double start = -10.0, end = 10.0, step = 0.1;

            if (function != null && function.length() > 15) {
                step = 0.2;
            }

            double[] xValues = generateXValues(start, end, step);
            double[] yValues = calculateFunctionValues(function, xValues);

            renderer.setFunctionData(0, xValues, yValues);
        }

        invalidate();
    }

    public void clear() {
        this.currentFunction = "";
        if (renderer != null) {
            renderer.clearAllFunctions();
        }
        invalidate();
    }

    private double[] generateXValues(double start, double end, double step) {
        int count = (int)((end - start) / step) + 1;
        double[] xValues = new double[count];
        for (int i = 0; i < count; i++) {
            xValues[i] = start + i * step;
        }
        return xValues;
    }

    private double[] calculateFunctionValues(String function, double[] xValues) {
        double[] yValues = new double[xValues.length];
        for (int i = 0; i < xValues.length; i++) {
            yValues[i] = calculateFunctionValue(function, xValues[i]);
        }
        return yValues;
    }
}