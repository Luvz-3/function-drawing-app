package com.functionplotter.math;

import java.util.ArrayList;
import java.util.List;

import com.functionplotter.parser.ExpressionParser;       
/**
 * 数学计算引擎
 * 负责高级数学计算和函数分析
 */
public class MathEngine {
    private ExpressionParser parser;
    
    public MathEngine(ExpressionParser parser) {
        this.parser = parser;
    }
    
    /**
     * 生成等间距的x值数组
     */
    public double[] generateXValues(double xMin, double xMax, int points) {
        double[] xValues = new double[points];
        double step = (xMax - xMin) / (points - 1);
        
        for (int i = 0; i < points; i++) {
            xValues[i] = xMin + i * step;
        }
        
        return xValues;
    }
    
    /**
     * 计算函数在指定范围内的y值
     */
    public double[] calculateFunction(int functionIndex, double xMin, double xMax, int points) {
        double[] xValues = generateXValues(xMin, xMax, points);
        return parser.evaluateRange(functionIndex, xValues);
    }
    
    /**
     * 自动调整y轴范围以适应函数值
     */
    public double[] calculateOptimalYRange(int functionIndex, double xMin, double xMax) {
        int samplePoints = 1000;
        double[] yValues = calculateFunction(functionIndex, xMin, xMax, samplePoints);
        
        double yMin = Double.MAX_VALUE;
        double yMax = Double.MIN_VALUE;
        int validPoints = 0;
        
        for (double y : yValues) {
            if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                yMin = Math.min(yMin, y);
                yMax = Math.max(yMax, y);
                validPoints++;
            }
        }
        
        // 如果没有有效点，使用默认范围
        if (validPoints == 0) {
            return new double[]{-5, 5};
        }
        
        // 添加一些边距
        double range = yMax - yMin;
        if (range < 1e-10) {
            // 如果范围太小，扩大范围
            yMin -= 1;
            yMax += 1;
        } else {
            yMin -= range * 0.1;
            yMax += range * 0.1;
        }
        
        return new double[]{yMin, yMax};
    }
    
    /**
    * 计算函数的导数（数值方法）- 使用默认步长
    */
    public double[] calculateDerivative(int functionIndex, double[] xValues) {
        return calculateDerivative(functionIndex, xValues, 1e-5); // 调用带步长参数的方法
    }

    /**
    * 计算函数的导数（数值方法）- 可指定步长
    */
    public double[] calculateDerivative(int functionIndex, double[] xValues, double h) {
        double[] derivative = new double[xValues.length];
    
        for (int i = 0; i < xValues.length; i++) {
            try {
                double x = xValues[i];
                double f1 = parser.evaluate(functionIndex, x + h);
                double f2 = parser.evaluate(functionIndex, x - h);
                derivative[i] = (f1 - f2) / (2 * h);
            } catch (Exception e) {
                derivative[i] = Double.NaN;
            }
        }
    
        return derivative;
    }
    
    /**
     * 计算函数的积分（数值方法 - 梯形法则）
     */
    public double calculateIntegral(int functionIndex, double a, double b, int intervals) {
        double h = (b - a) / intervals;
        double sum = 0;
        
        try {
            double fa = parser.evaluate(functionIndex, a);
            double fb = parser.evaluate(functionIndex, b);
            sum = (fa + fb) / 2;
            
            for (int i = 1; i < intervals; i++) {
                double x = a + i * h;
                double fx = parser.evaluate(functionIndex, x);
                sum += fx;
            }
            
            return sum * h;
        } catch (Exception e) {
            return Double.NaN;
        }
    }
    
    /**
     * 寻找函数的根（二分法）
     */
    public double findRoot(int functionIndex, double a, double b, double tolerance) {
        try {
            double fa = parser.evaluate(functionIndex, a);
            double fb = parser.evaluate(functionIndex, b);
            
            if (fa * fb > 0) {
                return Double.NaN; // 根可能不存在或需要更好的初始区间
            }
            
            for (int i = 0; i < 100; i++) {
                double c = (a + b) / 2;
                double fc = parser.evaluate(functionIndex, c);
                
                if (Math.abs(fc) < tolerance || (b - a) / 2 < tolerance) {
                    return c;
                }
                
                if (fa * fc < 0) {
                    b = c;
                    fb = fc;
                } else {
                    a = c;
                    fa = fc;
                }
            }
            
            return Double.NaN; // 未收敛
        } catch (Exception e) {
            return Double.NaN;
        }
    }
    
    /**
     * 寻找函数的极值点
     */
    public double[] findExtrema(int functionIndex, double xMin, double xMax) {
        // 简化的极值点查找（通过导数为零的点）
        int points = 1000;
        double[] xValues = generateXValues(xMin, xMax, points);
        double[] derivative = calculateDerivative(functionIndex, xValues);
        
        List<Double> extrema = new ArrayList<>();
        
        for (int i = 1; i < points - 1; i++) {
            if (derivative[i] * derivative[i-1] <= 0 && 
                !Double.isNaN(derivative[i]) && !Double.isNaN(derivative[i-1])) {
                // 使用更精确的方法细化极值点
                double root = findRootDerivative(functionIndex, xValues[i-1], xValues[i+1]);
                if (!Double.isNaN(root)) {
                    extrema.add(root);
                }
            }
        }
        
        return extrema.stream().mapToDouble(Double::doubleValue).toArray();
    }
    
    private double findRootDerivative(int functionIndex, double a, double b) {
        return findRoot(functionIndex, a, b, 1e-8); // 使用导数函数
    }
    
    /**
     * 计算函数统计信息
     */
    public FunctionStatistics calculateStatistics(int functionIndex, double xMin, double xMax) {
        int points = 1000;
        double[] yValues = calculateFunction(functionIndex, xMin, xMax, points);
        
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        int validCount = 0;
        
        for (double y : yValues) {
            if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                min = Math.min(min, y);
                max = Math.max(max, y);
                sum += y;
                validCount++;
            }
        }
        
        double average = validCount > 0 ? sum / validCount : Double.NaN;
        
        // 计算标准差
        double variance = 0;
        if (validCount > 0) {
            for (double y : yValues) {
                if (!Double.isNaN(y) && !Double.isInfinite(y)) {
                    variance += Math.pow(y - average, 2);
                }
            }
            variance /= validCount;
        }
        
        return new FunctionStatistics(min, max, average, Math.sqrt(variance), validCount);
    }
    
    /**
     * 函数统计信息类
     */
    public static class FunctionStatistics {
        public final double min;
        public final double max;
        public final double average;
        public final double standardDeviation;
        public final int validPoints;
        
        public FunctionStatistics(double min, double max, double average, 
                                 double standardDeviation, int validPoints) {
            this.min = min;
            this.max = max;
            this.average = average;
            this.standardDeviation = standardDeviation;
            this.validPoints = validPoints;
        }
    }
}