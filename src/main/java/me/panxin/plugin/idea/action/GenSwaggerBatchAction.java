package me.panxin.plugin.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class GenSwaggerBatchAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        GenSwaggerBatchDialog formTestDialog = new GenSwaggerBatchDialog(e.getProject());
        formTestDialog.setResizable(true); //是否允许用户通过拖拽的方式扩大或缩小你的表单框，我这里定义为true，表示允许
        formTestDialog.show();
    }
}
