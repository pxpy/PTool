package me.panxin.plugin.idea.utils;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import me.panxin.plugin.idea.dialog.GenIncreaseOrCoveringConfirmDialog;
import me.panxin.plugin.idea.dialog.GenParentClassConfirmDialog;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractGenerator {
  public static final String MAPPING_VALUE = "value";
  
  public static final String MAPPING_METHOD = "method";
  
  public static final String REQUEST_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.RequestMapping";
  
  public static final String POST_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.PostMapping";
  
  public static final String GET_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.GetMapping";
  
  public static final String DELETE_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.DeleteMapping";
  
  public static final String PATCH_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.PatchMapping";
  
  public static final String PUT_MAPPING_ANNOTATION = "org.springframework.web.bind.annotation.PutMapping";
  
  public static final String REQUEST_PARAM_TEXT = "org.springframework.web.bind.annotation.RequestParam";
  
  public static final String REQUEST_HEADER_TEXT = "org.springframework.web.bind.annotation.RequestHeader";
  
  public static final String PATH_VARIABLE_TEXT = "org.springframework.web.bind.annotation.PathVariable";
  
  public static final String REQUEST_BODY_TEXT = "org.springframework.web.bind.annotation.RequestBody";
  
  public static final String COOKIE_VALUE_TEXT = "org.springframework.web.bind.annotation.CookieValue";
  
  public static final String SERIAL_VERSION_UID = "serialVersionUID";
  
  public final Project project;
  
  public final PsiFile psiFile;
  
  public final PsiClass psiClass;
  
  public final PsiElementFactory elementFactory;
  
  public final String selectionText;
  
  public final Integer version;
  
  public volatile boolean isCovering = true;
  
  public AbstractGenerator(Project project, PsiFile psiFile, PsiClass psiClass, String selectionText, Integer version) {
    this.project = project;
    this.psiFile = psiFile;
    this.psiClass = psiClass;
    this.selectionText = selectionText;
    this.elementFactory = JavaPsiFacade.getElementFactory(project);
    this.version = version;
  }
  
  public boolean getIsCovering() {
    return this.isCovering;
  }
  
  public void doGenerate() {
    boolean isController = isController(this.psiClass);
    if (StringUtils.isNotEmpty(this.selectionText)) {
      WriteCommandAction.runWriteCommandAction(this.project, () -> generateSelection(this.psiClass, this.selectionText, isController));
      return;
    } 
    if (isController) {
      WriteCommandAction.runWriteCommandAction(this.project, () -> {
            generateClassAnnotation(this.psiClass, true);
            PsiMethod[] methods = this.psiClass.getMethods();
            for (PsiMethod psiMethod : methods) {
              if (!psiMethod.isConstructor())
                generateMethodAnnotation(psiMethod); 
            } 
          });
    } else {
      PsiField[] finalField, field = this.psiClass.getFields();
      PsiField[] allFields = this.psiClass.getAllFields();
      boolean genParent = false;
      if (field.length != allFields.length)
        genParent = (new GenParentClassConfirmDialog("Generate swagger annotation", "Whether to generate annotations for parent classes?")).showAndGet();
      if (genParent) {
        finalField = allFields;
      } else {
        finalField = field;
      } 
      String qualifiedName = "io.swagger.v3.oas.annotations.media.Schema";
      if (this.version.intValue() == 2)
        qualifiedName = "io.swagger.annotations.ApiModelProperty"; 
      boolean alertStatus = getAlertStatus3((PsiDocCommentOwner[])finalField, new String[] { qualifiedName });
      if (alertStatus && this.isCovering) {
        boolean genParent2 = (new GenIncreaseOrCoveringConfirmDialog("Generate swagger annotation", "Please select generate annotations mode.")).showAndGet();
        this.isCovering = !genParent2;
      } 
      WriteCommandAction.runWriteCommandAction(this.project, () -> {
            generateClassAnnotation(this.psiClass, false);
            PsiClass[] innerClasses = this.psiClass.getInnerClasses();
            generateInnerClass(innerClasses);
            for (int i = 0; i < finalField.length; i++)
              doGenerateFieldAnnotation(finalField[i], i); 
          });
    } 
  }
  
  private void generateInnerClass(PsiClass[] innerClasses) {
    if (innerClasses.length > 0)
      for (PsiClass innerClass : innerClasses) {
        generateClassAnnotation(innerClass, false);
        PsiField[] field = innerClass.getAllFields();
        for (int i = 0; i < field.length; i++)
          doGenerateFieldAnnotation(field[i], i); 
        generateInnerClass(innerClass.getInnerClasses());
      }  
  }
  
  public void doWrite(String name, String qualifiedName, String annotationText, PsiModifierListOwner psiModifierListOwner) {
    PsiAnnotation psiAnnotationDeclare = this.elementFactory.createAnnotationFromText(annotationText, (PsiElement)psiModifierListOwner);
    PsiNameValuePair[] attributes = psiAnnotationDeclare.getParameterList().getAttributes();
    PsiAnnotation existAnnotation = psiModifierListOwner.getModifierList().findAnnotation(qualifiedName);
    if (existAnnotation != null)
      existAnnotation.delete(); 
    addImport(this.psiFile, name, qualifiedName);
    PsiAnnotation psiAnnotation = psiModifierListOwner.getModifierList().addAnnotation(name);
    for (PsiNameValuePair pair : attributes)
      psiAnnotation.setDeclaredAttributeValue(pair.getName(), pair.getValue()); 
  }
  
  private void generateSelection(PsiClass psiClass, String selectionText, boolean isController) {
    if (Objects.equals(selectionText, psiClass.getName()))
      generateClassAnnotation(psiClass, isController); 
    PsiMethod[] methods = psiClass.getMethods();
    for (PsiMethod psiMethod : methods) {
      if (Objects.equals(selectionText, psiMethod.getName())) {
        generateMethodAnnotation(psiMethod);
        return;
      } 
    } 
    PsiField[] field = psiClass.getAllFields();
    for (int i = 0; i < field.length; i++) {
      if (Objects.equals(selectionText, field[i].getNameIdentifier().getText())) {
        doGenerateFieldAnnotation(field[i], i);
        return;
      } 
    } 
  }
  
  private boolean isController(PsiClass psiClass) {
    PsiAnnotation[] psiAnnotations = psiClass.getModifierList().getAnnotations();
    for (PsiAnnotation psiAnnotation : psiAnnotations) {
      String controllerAnnotation = "org.springframework.stereotype.Controller";
      String restControllerAnnotation = "org.springframework.web.bind.annotation.RestController";
      if (controllerAnnotation.equals(psiAnnotation.getQualifiedName()) || restControllerAnnotation
        .equals(psiAnnotation.getQualifiedName()))
        return true; 
    } 
    return false;
  }
  
  public String getMappingAttribute(PsiAnnotation[] psiAnnotations, String attributeName) {
    for (PsiAnnotation psiAnnotation : psiAnnotations) {
      String attribute;
      switch ((String)Objects.<String>requireNonNull(psiAnnotation.getQualifiedName())) {
        case "org.springframework.web.bind.annotation.RequestMapping":
          attribute = getAttribute(psiAnnotation, attributeName, "");
          if (Objects.equals("\"\"", attribute))
            return ""; 
          return attribute;
        case "org.springframework.web.bind.annotation.PostMapping":
          return "POST";
        case "org.springframework.web.bind.annotation.GetMapping":
          return "GET";
        case "org.springframework.web.bind.annotation.DeleteMapping":
          return "DELETE";
        case "org.springframework.web.bind.annotation.PatchMapping":
          return "PATCH";
        case "org.springframework.web.bind.annotation.PutMapping":
          return "PUT";
      } 
    } 
    return "";
  }
  
  public String getAttribute(PsiAnnotation psiAnnotation, String attributeName, String comment) {
    if (Objects.isNull(psiAnnotation))
      return "\"" + comment + "\""; 
    PsiAnnotationMemberValue psiAnnotationMemberValue = psiAnnotation.findDeclaredAttributeValue(attributeName);
    if (Objects.isNull(psiAnnotationMemberValue))
      return "\"" + comment + "\""; 
    return psiAnnotationMemberValue.getText();
  }
  
  public String getAttribute(PsiAnnotation psiAnnotation, String attributeName) {
    if (Objects.isNull(psiAnnotation))
      return null; 
    PsiAnnotationMemberValue psiAnnotationMemberValue = psiAnnotation.findDeclaredAttributeValue(attributeName);
    if (Objects.isNull(psiAnnotationMemberValue))
      return null; 
    return psiAnnotationMemberValue.getText();
  }
  
  private static final Map<String, PsiClass> CLASS_MAP = new HashMap<>();
  
  private void doGenerateFieldAnnotation(PsiField psiField, int position) {
    if ("serialVersionUID".equals(psiField.getName()))
      return; 
    doGenerateFieldAnnotation0(psiField, position);
  }
  
  public void addImport(PsiFile file, String className, String qualifiedName) {
    if (!(file instanceof PsiJavaFile))
      return; 
    PsiJavaFile javaFile = (PsiJavaFile)file;
    PsiImportList importList = javaFile.getImportList();
    if (importList == null)
      return; 
    if (CLASS_MAP.get(qualifiedName) != null) {
      addClassToImport(CLASS_MAP.get(qualifiedName), importList);
      return;
    } 
    PsiClass[] psiClasses = PsiShortNamesCache.getInstance(this.project).getClassesByName(className, GlobalSearchScope.allScope(this.project));
    if (psiClasses.length == 0)
      return; 
    PsiClass waiteImportClass = null;
    for (PsiClass psiClass : psiClasses) {
      if (qualifiedName.equals(psiClass.getQualifiedName())) {
        waiteImportClass = psiClass;
        break;
      } 
    } 
    if (waiteImportClass == null)
      return; 
    CLASS_MAP.put(qualifiedName, waiteImportClass);
    addClassToImport(waiteImportClass, importList);
  }
  
  private void addClassToImport(PsiClass waiteImportClass, PsiImportList importList) {
    for (PsiImportStatementBase is : importList.getAllImportStatements()) {
      String impQualifiedName = is.getImportReference().getQualifiedName();
      if (waiteImportClass.getQualifiedName().equals(impQualifiedName))
        return; 
    } 
    importList.add((PsiElement)this.elementFactory.createImportStatement(waiteImportClass));
  }
  
  public boolean getIsValidate(PsiAnnotation[] psiAnnotations) {
    String a = "javax.validation.constraints";
    for (PsiAnnotation tmpEle : psiAnnotations) {
      if (((String)Objects.<String>requireNonNull(tmpEle.getQualifiedName())).startsWith(a))
        return true; 
    } 
    return false;
  }
  
  public boolean getAlertStatus(PsiField psiField) {
    if (psiField.getModifierList() != null) {
      PsiAnnotation annotation = psiField.getModifierList().findAnnotation("io.swagger.annotations.ApiModelProperty");
      return !Objects.isNull(annotation);
    } 
    return false;
  }
  
  public boolean getAlertStatus2(PsiDocCommentOwner psiField, String qualifiedName) {
    if (!this.isCovering)
      return false; 
    if (psiField.getModifierList() != null) {
      PsiAnnotation annotation = psiField.getModifierList().findAnnotation(qualifiedName);
      if (!Objects.isNull(annotation)) {
        boolean genParent = (new GenIncreaseOrCoveringConfirmDialog("Generate swagger annotation", "Please select generate annotations mode.")).showAndGet();
        this.isCovering = !genParent;
      } 
      return this.isCovering;
    } 
    return this.isCovering;
  }
  
  public boolean getAlertStatus3(PsiDocCommentOwner[] psiField, String... qualifiedNames) {
    for (PsiDocCommentOwner psiCommentOwner : psiField) {
      if (psiCommentOwner.getModifierList() != null)
        for (String name : qualifiedNames) {
          if (psiCommentOwner.getModifierList().hasAnnotation(name))
            return true; 
        }  
    } 
    return false;
  }
  
  abstract void generateClassAnnotation(PsiClass paramPsiClass, boolean paramBoolean);
  
  abstract void generateMethodAnnotation(PsiMethod paramPsiMethod);
  
  abstract void doGenerateFieldAnnotation0(PsiField paramPsiField, int paramInt);
}
