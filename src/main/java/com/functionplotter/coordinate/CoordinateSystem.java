package com.functionplotter.coordinate;

/**
 * 坐标系系统
 * 负责数学坐标和屏幕坐标之间的转换
 */
public class CoordinateSystem {
    private double xMin, xMax, yMin, yMax;
    private int screenWidth, screenHeight;
    private double xScale, yScale;

    public CoordinateSystem(double xMin, double xMax, double yMin, double yMax,
                            int screenWidth, int screenHeight) {
        setCoordinateRange(xMin, xMax, yMin, yMax);
        setScreenSize(screenWidth, screenHeight);
    }

    /**
     * 设置数学坐标范围
     */
    public void setCoordinateRange(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        calculateScales();
    }

    /**
     * 设置屏幕尺寸
     */
    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        calculateScales();
    }

    private void calculateScales() {
        this.xScale = screenWidth / (xMax - xMin);
        this.yScale = screenHeight / (yMax - yMin);
    }

    /**
     * 数学坐标转屏幕坐标
     */
    public int mathToScreenX(double mathX) {
        return (int) ((mathX - xMin) * xScale);
    }

    public int mathToScreenY(double mathY) {
        return screenHeight - (int) ((mathY - yMin) * yScale);
    }

    /**
     * 屏幕坐标转数学坐标
     */
    public double screenToMathX(int screenX) {
        return xMin + screenX / xScale;
    }

    public double screenToMathY(int screenY) {
        return yMin + (screenHeight - screenY) / yScale;
    }

    /**
     * 检查点是否在可见范围内
     */
    public boolean isPointVisible(double mathX, double mathY) {
        return mathX >= xMin && mathX <= xMax && mathY >= yMin && mathY <= yMax;
    }

    /**
     * 自动调整坐标范围以适应数据
     */
    public void autoAdjustRange(double[] xValues, double[] yValues) {
        if (xValues.length == 0 || yValues.length == 0) return;

        double dataXMin = Double.MAX_VALUE;
        double dataXMax = Double.MIN_VALUE;
        double dataYMin = Double.MAX_VALUE;
        double dataYMax = Double.MIN_VALUE;

        // 找到有效数据的范围
        for (int i = 0; i < Math.min(xValues.length, yValues.length); i++) {
            if (!Double.isNaN(yValues[i]) && !Double.isInfinite(yValues[i])) {
                dataXMin = Math.min(dataXMin, xValues[i]);
                dataXMax = Math.max(dataXMax, xValues[i]);
                dataYMin = Math.min(dataYMin, yValues[i]);
                dataYMax = Math.max(dataYMax, yValues[i]);
            }
        }

        // 如果没有有效数据，保持原范围
        if (dataXMin == Double.MAX_VALUE) return;

        // 添加边距
        double xMargin = (dataXMax - dataXMin) * 0.1;
        double yMargin = (dataYMax - dataYMin) * 0.1;

        setCoordinateRange(
                dataXMin - xMargin,
                dataXMax + xMargin,
                dataYMin - yMargin,
                dataYMax + yMargin
        );
    }

    /**
     * 平移坐标系
     */
    public void pan(double deltaX, double deltaY) {
        double xRange = xMax - xMin;
        double yRange = yMax - yMin;

        setCoordinateRange(
                xMin + deltaX * xRange,
                xMax + deltaX * xRange,
                yMin + deltaY * yRange,
                yMax + deltaY * yRange
        );
    }

    /**
     * 缩放坐标系（以指定点为中心）
     */
    public void zoom(double factor, double centerX, double centerY) {
        double newXRange = (xMax - xMin) / factor;
        double newYRange = (yMax - yMin) / factor;

        setCoordinateRange(
                centerX - newXRange / 2,
                centerX + newXRange / 2,
                centerY - newYRange / 2,
                centerY + newYRange / 2
        );
    }

    /**
     * 缩放到显示所有函数
     */
    public void zoomToFit(double[][] allXValues, double[][] allYValues) {
        if (allXValues.length == 0) return;

        double overallXMin = Double.MAX_VALUE;
        double overallXMax = Double.MIN_VALUE;
        double overallYMin = Double.MAX_VALUE;
        double overallYMax = Double.MIN_VALUE;

        for (int i = 0; i < allXValues.length; i++) {
            for (int j = 0; j < allXValues[i].length; j++) {
                if (!Double.isNaN(allYValues[i][j]) && !Double.isInfinite(allYValues[i][j])) {
                    overallXMin = Math.min(overallXMin, allXValues[i][j]);
                    overallXMax = Math.max(overallXMax, allXValues[i][j]);
                    overallYMin = Math.min(overallYMin, allYValues[i][j]);
                    overallYMax = Math.max(overallYMax, allYValues[i][j]);
                }
            }
        }

        if (overallXMin != Double.MAX_VALUE) {
            double xMargin = (overallXMax - overallXMin) * 0.1;
            double yMargin = (overallYMax - overallYMin) * 0.1;

            setCoordinateRange(
                    overallXMin - xMargin,
                    overallXMax + xMargin,
                    overallYMin - yMargin,
                    overallYMax + yMargin
            );
        }
    }

    // Getter 方法
    public double getXMin() { return xMin; }
    public double getXMax() { return xMax; }
    public double getYMin() { return yMin; }
    public double getYMax() { return yMax; }
    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public double getXScale() { return xScale; }
    public double getYScale() { return yScale; }

    /**
     * 获取坐标轴交点的屏幕坐标
     */
    public Point getOriginScreenPoint() {
        int originX = mathToScreenX(0);
        int originY = mathToScreenY(0);
        return new Point(originX, originY);
    }

    /**
     * 简单的Point类
     */
    public static class Point {
        public final int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}