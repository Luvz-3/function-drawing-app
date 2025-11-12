package com.functionplotter.appandroid;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private GraphView graphView;
    private EditText functionInput;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化视图组件
        graphView = findViewById(R.id.graphView);
        functionInput = findViewById(R.id.functionInput);

        // 改为局部变量（修复警告）
        Button plotButton = findViewById(R.id.plotButton);
        Button clearButton = findViewById(R.id.clearButton);

        // 设置按钮点击事件
        plotButton.setOnClickListener(v -> plotFunction());
        clearButton.setOnClickListener(v -> clearGraph());

        // 添加自动测试功能
        setupAutoTest();
    }

    /**
     * 设置自动测试：应用启动后自动绘制示例函数
     */
    private void setupAutoTest() {
        // 延迟1秒后执行自动测试，确保界面已加载完成
        handler.postDelayed(() -> {
            // 测试1：绘制一个简单的正弦函数
            functionInput.setText("sin(x)");
            plotFunction();

            // 延迟3秒后测试第二个函数
            handler.postDelayed(() -> {
                // 测试2：绘制抛物线
                functionInput.setText("x^2");
                plotFunction();

                // 延迟3秒后测试第三个函数
                handler.postDelayed(() -> {
                    // 测试3：绘制复杂函数
                    functionInput.setText("sin(x)*cos(x)");
                    plotFunction();

                    // 显示测试完成提示
                    Toast.makeText(MainActivity.this,
                            "自动测试完成！可以手动输入其他函数测试",
                            Toast.LENGTH_LONG).show();

                }, 3000); // 3秒延迟

            }, 3000); // 3秒延迟

        }, 1000); // 1秒延迟
    }

    private void plotFunction() {
        String function = functionInput.getText().toString().trim();
        if (!function.isEmpty()) {
            graphView.plotFunction(function);
            // 添加成功提示
            Toast.makeText(this, "正在绘制: " + function, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "请输入函数表达式", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearGraph() {
        functionInput.setText("");
        graphView.clear();
        Toast.makeText(this, "已清除图形", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理Handler，避免内存泄漏
        handler.removeCallbacksAndMessages(null);
    }
}