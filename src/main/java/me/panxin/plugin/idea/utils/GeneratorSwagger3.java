package me.panxin.plugin.idea.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
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
  private static final String TAG_QUALIFIED_NAME = "io.swagger.v3.oas.annotations.tags.Tag";
  
  private static final String SCHEMA_QUALIFIED_NAME = "io.swagger.v3.oas.annotations.media.Schema";
  
  private static final String OPERATION_QUALIFIED_NAME = "io.swagger.v3.oas.annotations.Operation";
  
  private static final String PARAMETERS_QUALIFIED_NAME = "io.swagger.v3.oas.annotations.Parameters";
  
  private static final String PARAMETER_QUALIFIED_NAME = "io.swagger.v3.oas.annotations.Parameter";
  
  private static final String PARAMETER_IN_QUALIFIED_NAME = "io.swagger.v3.oas.annotations.enums.ParameterIn";
  
  public GeneratorSwagger3(Project project, PsiFile psiFile, PsiClass psiClass, String selectionText) {
    super(project, psiFile, psiClass, selectionText, Integer.valueOf(3));
  }
  
  public void generateClassAnnotation(PsiClass psiClass, boolean isController) {
    PsiComment classComment = null;
    for (PsiElement tmpEle : psiClass.getChildren()) {
      if (tmpEle instanceof PsiComment) {
        String annotationFromText, annotation, qualifiedName;
        classComment = (PsiComment)tmpEle;
        String tmpText = classComment.getText();
        String commentDesc = CommentUtils.getCommentDesc(tmpText);
        if(StringUtils.isEmpty(commentDesc)){
          continue;
        }
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
    } 
    if (Objects.isNull(classComment)) {
      String annotationFromText, annotation, qualifiedName;
      if (isController) {
        annotation = "Tag";
        qualifiedName = "io.swagger.v3.oas.annotations.tags.Tag";
        annotationFromText = String.format("@%s(name = \"%s\")", new Object[] { annotation, "" });
      } else {
        annotation = "Schema";
        qualifiedName = "io.swagger.v3.oas.annotations.media.Schema";
        annotationFromText = String.format("@%s", new Object[] { annotation });
      } 
//      doWrite(annotation, qualifiedName, annotationFromText, (PsiModifierListOwner)psiClass);
    } 
  }
  
  public void generateMethodAnnotation(PsiMethod psiMethod) {
    PsiAnnotation[] psiAnnotations = psiMethod.getModifierList().getAnnotations();
    String methodValue = getMappingAttribute(psiAnnotations, "method");
    if (StringUtils.isBlank(methodValue))
      return; 
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
        if (StringUtils.isEmpty(psiAnnotation.getQualifiedName()))
          break; 
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
        if (Objects.equals(dataType, "file"))
          paramType = "ParameterIn.QUERY"; 
        if (!StringUtils.isEmpty(paramType)) {
          String apiImplicitParamText, paramDesc = "";
          if (methodParamCommentDesc != null)
            paramDesc = methodParamCommentDesc.get(psiParameter.getNameIdentifier().getText()); 
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
    if (parameterList.size() == 0)
      return; 
    if (parameterList.size() == 1) {
      doWrite("Parameter", "io.swagger.v3.oas.annotations.Parameter", parameterList.get(0), (PsiModifierListOwner)psiMethod);
    } else {
      String parametersAnnotationText = parameterList.stream().collect(Collectors.joining(",\n", "@Parameters({\n", "\n})"));
      doWrite("Parameters", "io.swagger.v3.oas.annotations.Parameters", parametersAnnotationText, (PsiModifierListOwner)psiMethod);
    } 
    addImport(this.psiFile, "Parameter", "io.swagger.v3.oas.annotations.Parameter");
    addImport(this.psiFile, "ParameterIn", "io.swagger.v3.oas.annotations.enums.ParameterIn");
  }
  
  public void doGenerateFieldAnnotation0(PsiField psiField, int position) {
    if (psiField.getModifierList() != null) {
      boolean hasAnnotation = psiField.getModifierList().hasAnnotation("io.swagger.v3.oas.annotations.media.Schema");
      if (hasAnnotation && !getIsCovering())
        return; 
    } 
    PsiComment classComment = null;
    boolean isValidate = getIsValidate(psiField.getAnnotations());
    for (PsiElement tmpEle : psiField.getChildren()) {
      if (tmpEle instanceof PsiComment) {
        String apiModelPropertyText;
        classComment = (PsiComment)tmpEle;
        String tmpText = classComment.getText();
        String commentDesc = CommentUtils.getCommentDesc(tmpText);
        if (isValidate) {
          apiModelPropertyText = String.format("@Schema(description=\"%s\", requiredMode = Schema.RequiredMode.REQUIRED)", new Object[] { commentDesc });
        } else {
          apiModelPropertyText = String.format("@Schema(description=\"%s\")", new Object[] { commentDesc });
        } 
        doWrite("Schema", SCHEMA.getQualifiedName(), apiModelPropertyText, (PsiModifierListOwner)psiField);
      } 
    } 
    if (Objects.isNull(classComment))
      doWrite("Schema", SCHEMA.getQualifiedName(), "@Schema(hidden = true)", (PsiModifierListOwner)psiField);
  }
}
