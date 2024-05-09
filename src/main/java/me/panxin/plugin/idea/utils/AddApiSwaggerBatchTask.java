package me.panxin.plugin.idea.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;

import java.util.List;

public class AddApiSwaggerBatchTask extends Task.Backgroundable {

    private  final  PsiFile psiFile;
    private final  List<PsiClass> classesToCheck;
    private final int version;

    private final Project project;
    public AddApiSwaggerBatchTask(Project project, PsiFile psiFile, List<PsiClass> classesToCheck,int version,  String title) {
        super(project, title, true); // true 表示任务可以在后台运行，不会阻塞UI
        this.psiFile = psiFile;
        this.classesToCheck = classesToCheck;
        this.project= project;
        this.version = version;
    }

    @Override
    public void run(ProgressIndicator indicator) {
        // 在这里执行你的长任务
        int i = 0;
        for (PsiClass psiClass : classesToCheck) {
            indicator.setFraction((double) i / classesToCheck.size()); // 更新进度条
            indicator.checkCanceled(); // 检查用户是否取消了任务
            // 判断文件是否是Controller
            if (GeneratorUtils.isController(psiClass)) {
                if(version == 2) {
                    new GeneratorUtils(project, psiClass.getContainingFile(), psiClass, "").doGenerate();
                }else{
                    new GeneratorSwagger3(project, psiClass.getContainingFile(), psiClass, "").doGenerate();
                }

            }
        }

    }

    @Override
    public void onSuccess() {
        // 任务成功完成后的回调
        super.onSuccess();
        // 可以在这里显示完成的消息或者进行其他操作
    }
}