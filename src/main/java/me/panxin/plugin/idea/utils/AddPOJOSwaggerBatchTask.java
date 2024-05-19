package me.panxin.plugin.idea.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.messages.MessageBusConnection;
import me.panxin.plugin.idea.listener.AppActivationListener;

import java.util.ArrayList;
import java.util.List;

public class AddPOJOSwaggerBatchTask extends Task.Backgroundable {

    private final PsiFile psiFile;
    private final List<PsiClass> classesToCheck;

    private final Project project;

    private final int version;

    public AddPOJOSwaggerBatchTask(Project project, PsiFile psiFile, List<PsiClass> classesToCheck, int version, String title) {
        super(project, title, true); // true 表示任务可以在后台运行，不会阻塞UI
        this.psiFile = psiFile;
        this.classesToCheck = classesToCheck;
        this.project = project;
        this.version = version;
        // 设置消息监听
        AppActivationListener listener = AppActivationListener.getInstance();
        Application app = ApplicationManager.getApplication();
        Disposable disposable = Disposer.newDisposable();
        Disposer.register(app, disposable);
        MessageBusConnection connection = app.getMessageBus().connect(disposable);
        connection.subscribe(ApplicationActivationListener.TOPIC, listener);
        listener.activate();
    }

    @Override
    public void run(ProgressIndicator indicator) {

        // 阶段一：在后台线程筛选POJO类
        List<PsiClass> pojoList = filterPOJOClasses(indicator, 0.2);

        // 阶段二：在EDT上执行写入操作，并更新剩余的进度
        ApplicationManager.getApplication().invokeLater(() -> performWritesAndUpdateProgress(pojoList, indicator, 0.2));
    }

    private List<PsiClass> filterPOJOClasses(ProgressIndicator indicator, double progressForFiltering) {
        List<PsiClass> pojoList = new ArrayList<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            int processed = 0;
            for (PsiClass psiClass : classesToCheck) {
                indicator.checkCanceled();
                processed++;
                indicator.setFraction(progressForFiltering * ((double) processed / classesToCheck.size()));
                // 判断文件是否是POJO
                PsiFile containingFile = psiClass.getContainingFile();
                PsiDirectory containingDirectory = containingFile.getContainingDirectory();
                String directoryName = containingDirectory.getName();
                if (POJO.isPOJO(directoryName, psiClass.getName())) {
                    pojoList.add(psiClass);
                }
            }
        });
        return pojoList;
    }

    private void performWritesAndUpdateProgress(List<PsiClass> pojoList, ProgressIndicator indicator, double initialProgress) {
        int totalTasks = pojoList.size();
        for (int i = 0; i < totalTasks; i++) {
            final int currentIndex = i; // 添加此行来创建一个新的final变量
            indicator.setFraction(initialProgress + (1.0 - initialProgress) * ((double) i / totalTasks));
            indicator.checkCanceled();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                PsiClass psiClass = pojoList.get(currentIndex);
                try {
                    if (version == 2) {
                        new GeneratorUtils(project, psiClass.getContainingFile(), psiClass, "").doGenerate();
                    } else {
                        new GeneratorSwagger3(project, psiClass.getContainingFile(), psiClass, "").doGenerate();
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
            });
        }
    }

    @Override
    public void onSuccess() {
        // 任务成功完成后的回调
        super.onSuccess();
        // 可以在这里显示完成的消息或者进行其他操作
    }
}
