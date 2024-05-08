package me.panxin.plugin.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtilBase;
import me.panxin.plugin.idea.utils.GeneratorUtils;
import me.panxin.plugin.idea.utils.POJO;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GenAllSwaggerAnnotationAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            PsiManager psiManager = PsiManager.getInstance(project);
            GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
            PsiPackage rootPackage = JavaPsiFacade.getInstance(psiManager.getProject()).findPackage("");

            List<PsiClass> classesToCheck = new ArrayList<>();
            processPackage(rootPackage, searchScope, classesToCheck);
            Editor editor = event.getData(PlatformDataKeys.EDITOR);
            assert editor != null;
            assert project != null;
            PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
            for (PsiClass psiClass : classesToCheck) {

                // 判断文件是否是POJO
                PsiFile containingFile = psiClass.getContainingFile();
                PsiDirectory containingDirectory = containingFile.getContainingDirectory();
                String directoryName = containingDirectory.getName();
                if (POJO.isPOJO(directoryName, psiClass.getName())) {
                    new GeneratorUtils(project, psiFile, psiClass, "").doGenerate();

                }
            }
        }
    }

    private void processPackage(PsiPackage psiPackage, GlobalSearchScope searchScope, List<PsiClass> classesToCheck) {
        for (PsiClass psiClass : psiPackage.getClasses()) {
            classesToCheck.add(psiClass);
        }

        for (PsiPackage subPackage : psiPackage.getSubPackages(searchScope)) {
            processPackage(subPackage, searchScope, classesToCheck);
        }
    }

    private String getClassListAsString(List<PsiClass> classes) {
        StringBuilder sb = new StringBuilder();
        for (PsiClass psiClass : classes) {
            sb.append(psiClass.getQualifiedName()).append("\n");
        }
        return sb.toString();
    }

    private void navigateToClasses(List<PsiClass> classes) {
        for (PsiClass psiClass : classes) {
            psiClass.navigate(true);
        }
    }

}
