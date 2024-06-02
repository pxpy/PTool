package me.panxin.plugin.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import me.panxin.plugin.idea.utils.GeneratorSwagger3;
import me.panxin.plugin.idea.utils.GeneratorUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class GenAllSwagger3AnnotationAction extends AbstractSwaggerAnnotationAction {

    @Override
    protected void generate(PsiClass psiClass, PsiFile psiFile) {
        new GeneratorSwagger3(psiClass.getProject(), psiFile, psiClass, "").doGenerate();
    }
}
