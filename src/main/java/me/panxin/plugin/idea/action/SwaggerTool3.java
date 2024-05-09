package me.panxin.plugin.idea.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import me.panxin.plugin.idea.utils.GeneratorSwagger3;

/**
 * 招摇工具3
 *
 * @author PanXin
 * @date 2024/05/09
 */
public class SwaggerTool3 extends AnAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    Project project = anActionEvent.getProject();
    Editor editor = (Editor)anActionEvent.getData(PlatformDataKeys.EDITOR);
    assert editor != null;
    assert project != null;
    PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
    PsiClass psiClass = (PsiClass)PsiTreeUtil.findChildOfAnyType((PsiElement)psiFile, new Class[] { PsiClass.class });
    String selectionText = editor.getSelectionModel().getSelectedText();
    (new GeneratorSwagger3(project, psiFile, psiClass, selectionText)).doGenerate();
  }
}
