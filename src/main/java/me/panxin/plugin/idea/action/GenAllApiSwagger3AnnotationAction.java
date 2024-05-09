package me.panxin.plugin.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import me.panxin.plugin.idea.utils.AddApiSwaggerBatchTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GenAllApiSwagger3AnnotationAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            PsiManager psiManager = PsiManager.getInstance(project);
            GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
            PsiPackage rootPackage = JavaPsiFacade.getInstance(psiManager.getProject()).findPackage("");

            List<PsiClass> classesToCheck = new ArrayList<>();
            processPackage(rootPackage, searchScope, classesToCheck);
            assert project != null;
            new AddApiSwaggerBatchTask(project, null, classesToCheck,3,"给接口批量添加swagger注解").queue();
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
