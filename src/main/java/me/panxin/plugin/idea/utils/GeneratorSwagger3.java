package me.panxin.plugin.idea.utils;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.*;
import com.intellij.util.messages.MessageBusConnection;
import me.panxin.plugin.idea.common.util.translator.TranslatorService;
import me.panxin.plugin.idea.listener.AppActivationListener;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static me.panxin.plugin.idea.enums.SwaggerAnnotation.SCHEMA;

/**
 * 发电机 Swagger3
 *
 * @author PanXin
 * @date 2024/05/09
 */
public class GeneratorSwagger3 extends AbstractGenerator {

  private TranslatorService translatorService = ServiceManager.getService(TranslatorService.class);


  public GeneratorSwagger3(Project project, PsiFile psiFile, PsiClass psiClass, String selectionText) {
    super(project, psiFile, psiClass, selectionText, Integer.valueOf(3));
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
  public void generateClassAnnotation(PsiClass psiClass, boolean isController) {
    String commentDesc = CommentUtils.findCommentDescriptionOrTranslate(psiClass,psiClass.getName());
    String annotationFromText, annotation, qualifiedName;
    if (isController) {
      annotation = "Tag";
      qualifiedName = "io.swagger.v3.oas.annotations.tags.Tag";
      annotationFromText = String.format("@%s(name = \"%s\", description = \"%s\")", new Object[] { annotation, commentDesc, commentDesc });
    } else {
      annotation = "Schema";
      qualifiedName = "io.swagger.v3.oas.annotations.media.Schema";
      annotationFromText = String.format("@%s(description = \"%s\")", new Object[] { annotation, commentDesc });
    }
    doWrite(annotation, qualifiedName, annotationFromText, (PsiModifierListOwner)psiClass);
  }
  
  @Override
  public void generateMethodAnnotation(PsiMethod psiMethod) {
    PsiAnnotation[] psiAnnotations = psiMethod.getModifierList().getAnnotations();
    String methodValue = getMappingAttribute(psiAnnotations, "method");
    if (StringUtils.isBlank(methodValue)) {
        return;
    }
    String commentDesc = "";
    Map<String, String> methodParamCommentDesc = null;
    for (PsiElement tmpEle : psiMethod.getChildren()) {
      if (tmpEle instanceof PsiComment) {
        PsiComment classComment = (PsiComment)tmpEle;
        String tmpText = classComment.getText();
        methodParamCommentDesc = CommentUtils.getCommentMethodParam(tmpText);
        commentDesc = CommentUtils.getCommentDesc(tmpText);
      } 
    } 
    PsiAnnotation apiOperationExist = psiMethod.getModifierList().findAnnotation("io.swagger.v3.oas.annotations.Operation");
    String apiOperationAttrValue = getAttribute(apiOperationExist, "summary", commentDesc);
    String apiOperationAttrNotes = getAttribute(apiOperationExist, "description", commentDesc);
    String apiOperationAnnotationText = String.format("@Operation(summary = %s, description = %s)", new Object[] { apiOperationAttrValue, apiOperationAttrNotes });
    PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
    List<String> parameterList = new ArrayList<>(psiParameters.length);
    for (PsiParameter psiParameter : psiParameters) {
      PsiType psiType = psiParameter.getType();
      String dataType = CommentUtils.getDataType(psiType.getCanonicalText(), psiType);
      String paramType = null;
      String required = "";
      String value = null;
      for (PsiAnnotation psiAnnotation : psiParameter.getModifierList().getAnnotations()) {
        if (StringUtils.isEmpty(psiAnnotation.getQualifiedName())) {
            break;
        }
        switch (psiAnnotation.getQualifiedName()) {
          case "org.springframework.web.bind.annotation.RequestHeader":
            paramType = "ParameterIn.HEADER";
            break;
          case "org.springframework.web.bind.annotation.RequestParam":
            paramType = "ParameterIn.QUERY";
            break;
          case "org.springframework.web.bind.annotation.PathVariable":
            paramType = "ParameterIn.PATH";
            break;
          case "org.springframework.web.bind.annotation.RequestBody":
            paramType = "body";
            break;
          case "org.springframework.web.bind.annotation.CookieValue":
            paramType = "ParameterIn.COOKIE";
            break;
        } 
        required = getAttribute(psiAnnotation, "required", "");
        value = getAttribute(psiAnnotation, "value");
      } 
      if (!"body".equals(paramType)) {
        if (Objects.equals(dataType, "file")) {
            paramType = "ParameterIn.QUERY";
        }
        if (!StringUtils.isEmpty(paramType)) {
          String apiImplicitParamText, paramDesc = "";
          if (methodParamCommentDesc != null) {
              paramDesc = methodParamCommentDesc.get(psiParameter.getNameIdentifier().getText());
          }
          System.out.println("value:" + value);
          System.out.println("psiParameter.getNameIdentifier().getText():" + psiParameter.getNameIdentifier().getText());
          if (Objects.equals(required, "false")) {
            apiImplicitParamText = String.format("@Parameter(name = %s, description = \"%s\", in = %s)", new Object[] { StringUtils.isNotEmpty(value) ? value : ("\"" + psiParameter.getNameIdentifier().getText() + "\""), 
                  (paramDesc == null) ? "" : paramDesc, paramType });
          } else {
            apiImplicitParamText = String.format("@Parameter(name = %s, description = \"%s\", in = %s, required = true)", new Object[] { StringUtils.isNotEmpty(value) ? value : ("\"" + psiParameter.getNameIdentifier().getText() + "\""), 
                  (paramDesc == null) ? "" : paramDesc, paramType });
          } 
          parameterList.add(apiImplicitParamText);
        } 
      } 
    } 
    doWrite("Operation", "io.swagger.v3.oas.annotations.Operation", apiOperationAnnotationText, (PsiModifierListOwner)psiMethod);
    if (parameterList.size() == 0) {
        return;
    }
    if (parameterList.size() == 1) {
      doWrite("Parameter", "io.swagger.v3.oas.annotations.Parameter", parameterList.get(0), (PsiModifierListOwner)psiMethod);
    } else {
      String parametersAnnotationText = parameterList.stream().collect(Collectors.joining(",\n", "@Parameters({\n", "\n})"));
      doWrite("Parameters", "io.swagger.v3.oas.annotations.Parameters", parametersAnnotationText, (PsiModifierListOwner)psiMethod);
    } 
    addImport(this.psiFile, "Parameter", "io.swagger.v3.oas.annotations.Parameter");
    addImport(this.psiFile, "ParameterIn", "io.swagger.v3.oas.annotations.enums.ParameterIn");
  }
  
  @Override
  public void doGenerateFieldAnnotation0(PsiField psiField, int position) {
    if (psiField.getModifierList() != null) {
      boolean hasAnnotation = psiField.getModifierList().hasAnnotation("io.swagger.v3.oas.annotations.media.Schema");
      if (hasAnnotation && !getIsCovering()) {
          return;
      }
    } 
    boolean isValidate = getIsValidate(psiField.getAnnotations());
    String commentDesc = CommentUtils.findCommentDescriptionOrTranslate(psiField, psiField.getName());
    String apiModelPropertyText;
    if (isValidate) {
      apiModelPropertyText = String.format("@Schema(description=\"%s\", requiredMode = Schema.RequiredMode.REQUIRED)", new Object[] { commentDesc });
    } else {
      apiModelPropertyText = String.format("@Schema(description=\"%s\")", new Object[] { commentDesc });
    }
    doWrite("Schema", SCHEMA.getQualifiedName(), apiModelPropertyText, (PsiModifierListOwner)psiField);
  }
}
