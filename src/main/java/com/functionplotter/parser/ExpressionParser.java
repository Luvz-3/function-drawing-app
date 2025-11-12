package com.functionplotter.parser;

import java.util.ArrayList;
import java.util.List;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * 函数表达式解析器
 * 使用 exp4j 库解析数学表达式
 */
public class ExpressionParser {
    private final List<Expression> expressions;
    private final List<String> expressionStrings;
    private final List<Boolean> validExpressions;
    private final List<String> errors;

    public ExpressionParser() {
        expressions = new ArrayList<>();
        expressionStrings = new ArrayList<>();
        validExpressions = new ArrayList<>();
        errors = new ArrayList<>();
    }

    /**
     * 添加或更新函数表达式
     */
    public boolean setExpression(int index, String expression) {
        // 确保有足够的容量
        while (expressions.size() <= index) {
            expressions.add(null);
            expressionStrings.add("");
            validExpressions.add(false);
            errors.add("");
        }

        expressionStrings.set(index, expression);

        try {
            Expression exp = new ExpressionBuilder(expression)
                    .variables("x")
                    .functions()
                    .build();

            expressions.set(index, exp);
            validExpressions.set(index, true);
            errors.set(index, "");
            return true;

        } catch (IllegalArgumentException e) {
            expressions.set(index, null);
            validExpressions.set(index, false);
            errors.set(index, "语法错误: " + e.getMessage());
            return false;
        } catch (Exception e) {
            expressions.set(index, null);
            validExpressions.set(index, false);
            errors.set(index, "未知错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 计算指定函数在x点的值
     */
    public double evaluate(int functionIndex, double x) throws Exception {
        if (functionIndex >= expressions.size() || expressions.get(functionIndex) == null) {
            throw new Exception("函数未定义或表达式无效");
        }

        if (!validExpressions.get(functionIndex)) {
            throw new Exception("表达式无效: " + errors.get(functionIndex));
        }

        try {
            return expressions.get(functionIndex).setVariable("x", x).evaluate();
        } catch (ArithmeticException e) {
            throw new Exception("数学错误: " + e.getMessage());
        } catch (Exception e) {
            throw new Exception("计算错误: " + e.getMessage());
        }
    }

    /**
     * 批量计算函数值（优化性能）
     */
    public double[] evaluateRange(int functionIndex, double[] xValues) {
        if (!isValid(functionIndex)) {
            return new double[xValues.length];
        }

        double[] yValues = new double[xValues.length];
        Expression exp = expressions.get(functionIndex);

        for (int i = 0; i < xValues.length; i++) {
            try {
                yValues[i] = exp.setVariable("x", xValues[i]).evaluate();
                // 处理无穷大和NaN
                if (Double.isInfinite(yValues[i]) || Double.isNaN(yValues[i])) {
                    yValues[i] = Double.NaN;
                }
            } catch (Exception e) {
                yValues[i] = Double.NaN;
            }
        }

        return yValues;
    }

    // 工具方法
    public boolean isValid(int index) {
        return index < validExpressions.size() && validExpressions.get(index);
    }

    @SuppressWarnings("unused")
    public String getError(int index) {
        return index < errors.size() ? errors.get(index) : "";
    }

    @SuppressWarnings("unused")
    public String getExpression(int index) {
        return index < expressionStrings.size() ? expressionStrings.get(index) : "";
    }

    @SuppressWarnings("unused")
    public int getFunctionCount() {
        return expressions.size();
    }

    @SuppressWarnings("unused")
    public void removeFunction(int index) {
        if (index < expressions.size()) {
            expressions.remove(index);
            expressionStrings.remove(index);
            validExpressions.remove(index);
            errors.remove(index);
        }
    }

    public void clearAll() {
        expressions.clear();
        expressionStrings.clear();
        validExpressions.clear();
        errors.clear();
    }

    /**
     * 获取支持的函数列表（用于UI提示）
     */
    @SuppressWarnings("unused")
    public static String[] getSupportedFunctions() {
        return new String[] {
                "基本运算: +, -, *, /, ^",
                "三角函数: sin(x), cos(x), tan(x), asin(x), acos(x), atan(x)",
                "双曲函数: sinh(x), cosh(x), tanh(x)",
                "对数指数: log(x), log10(x), exp(x)",
                "其他函数: sqrt(x), abs(x), ceil(x), floor(x)",
                "常数: pi, e"
        };
    }

    @SuppressWarnings("unused")
    public static String[] getExampleExpressions() {
        return new String[] {
                "sin(x)", "cos(x)", "x^2", "x^3 - 2*x + 1",
                "sin(x) * cos(x)", "log(x + 1)", "sqrt(x^2 + 1)",
                "exp(-x^2/2)/sqrt(2*pi)", "sin(x)/x"
        };
    }
}