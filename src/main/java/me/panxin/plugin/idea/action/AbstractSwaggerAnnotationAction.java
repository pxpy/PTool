package me.panxin.plugin.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractSwaggerAnnotationAction extends AnAction {

    protected abstract void generate(PsiClass psiClass, PsiFile psiFile);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return;
        }
        PsiElement psiElement = event.getDataContext().getData(LangDataKeys.PSI_ELEMENT);
        if (!(psiElement instanceof PsiDirectory)) {
            return;
        }

        PsiDirectory directory = (PsiDirectory) psiElement;
        scanAndPrintPsiClasses(directory, project);
    }

    public void scanAndPrintPsiClasses(PsiDirectory rootDirectory, Project project) {
        collectAndPrintClassesInDirectory(rootDirectory, project);
    }

    private void collectAndPrintClassesInDirectory(PsiDirectory directory, Project project) {
        PsiManager psiManager = PsiManager.getInstance(project);

        // 获取当前目录下的所有Java文件（非递归）
        Collection<PsiFile> javaFiles = FilenameIndex.getAllFilesByExt(project, "java", GlobalSearchScope.projectScope(project))
                .stream()
                .filter(virtualFile -> isUnderDirectory(virtualFile, directory.getVirtualFile()))
                .map(psiManager::findFile)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 处理当前目录下的Java文件
        for (PsiFile psiFile : javaFiles) {
            if (psiFile instanceof PsiClassOwner) {
                PsiClassOwner classOwner = (PsiClassOwner) psiFile;
                for (PsiClass psiClass : classOwner.getClasses()) {
//                    System.out.println("Found Class: " + psiClass.getQualifiedName());
//                    new GeneratorSwagger3(project, psiClass.getContainingFile(), psiClass, "").doGenerate();
                    generate(psiClass, psiFile);
                }
            }
        }

        // 递归处理子目录
        for (PsiDirectory subDirectory : directory.getSubdirectories()) {
            collectAndPrintClassesInDirectory(subDirectory, project);
        }
    }

    // 辅助方法，判断虚拟文件是否位于指定目录下（包括子目录）
    private boolean isUnderDirectory(com.intellij.openapi.vfs.VirtualFile file, com.intellij.openapi.vfs.VirtualFile directory) {
        while (file != null) {
            if (file.equals(directory)) {
                return true;
            }
            file = file.getParent();
        }
        return false;
    }
}
