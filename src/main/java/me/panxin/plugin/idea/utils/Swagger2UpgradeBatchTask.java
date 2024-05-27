package me.panxin.plugin.idea.utils;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.model.search.SearchService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.util.messages.MessageBusConnection;
import me.panxin.plugin.idea.listener.AppActivationListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Swagger2UpgradeBatchTask extends Task.Backgroundable {

    private final PsiFile psiFile;
    private final List<PsiClass> classesToCheck;
    private List<VirtualFile > collectedFiles = new ArrayList<>();



    private final Project project;

    private final int version;

    public Swagger2UpgradeBatchTask(Project project, PsiFile psiFile, List<PsiClass> classesToCheck, int version, String title) {
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
        List<PsiClass> pojoList = filterSwagger2Classes(indicator, 0.2);

        // 阶段二：在EDT上执行写入操作，并更新剩余的进度
//        ApplicationManager.getApplication().invokeLater(() -> performWritesAndUpdateProgress(pojoList, indicator, 0.2));
    }

    private List<PsiClass> filterSwagger2Classes(ProgressIndicator indicator, double progressForFiltering) {
        List<PsiClass> pojoList = new ArrayList<>();
        ApplicationManager.getApplication().runReadAction(() -> {
            int processed = 0;
            // 定义搜索范围
            GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);

// 定义搜索条件，这里以"@ApiOperation"为例
            String searchPattern = "io.swagger.annotations.";

            traverseAllDocuments(project);
            for (PsiClass psiClass : classesToCheck) {
                indicator.checkCanceled();
                processed++;
                indicator.setFraction(progressForFiltering * ((double) processed / classesToCheck.size()));
                // 判断文件是否是引入了swagger2注解
                    pojoList.add(psiClass);
            }
        });
        return pojoList;
    }

    public void traverseAllDocuments(Project project) {
        // 获取项目根目录
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir != null) {
            // 使用 ReadAction 确保在正确的线程中执行 PSI 操作
            ReadAction.run(() -> {
                // 从根目录开始递归遍历
                traverseDirectory(PsiManager.getInstance(project).findDirectory(baseDir));
            });
        }
    }

    private void traverseDirectory(PsiDirectory directory) {
        if (directory != null) {
            for (PsiElement virtualFile : directory.getChildren()) {
                VirtualFile virtualFile1= (VirtualFile)virtualFile;
                if (StdFileTypes.JAVA.equals(virtualFile1.getFileType())) {
                    // 将文件添加到收集列表中
                    collectedFiles.add(virtualFile1);

                    // 如果是目录，则递归调用
                } else if (virtualFile1.isDirectory()) {
                    traverseDirectory((PsiDirectory) virtualFile);
                }
            }
        }
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
