package me.panxin.plugin.idea.action;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import me.panxin.plugin.idea.ui.SwaggerGenerateUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GenSwaggerBatchDialog extends DialogWrapper {
    private String projectName;
    private SwaggerGenerateUI swaggerGenerateUI = new SwaggerGenerateUI();

    protected GenSwaggerBatchDialog(@Nullable Project project) {
        super(true);
        setTitle("GenSwaggerBatchDialog"); //设置会话框标题
        this.projectName = project.getName(); //获取到当前项目的名称
        init(); //触发一下init方法，否则swing样式将无法展示在会话框
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return swaggerGenerateUI.getRootPanel();
    }



}
