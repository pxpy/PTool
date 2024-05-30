package me.panxin.plugin.idea.utils;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiType;
import me.panxin.plugin.idea.common.util.translator.TranslatorService;
import me.panxin.plugin.idea.config.PToolConfig;
import me.panxin.plugin.idea.config.PToolConfigComponent;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 注释实用程序
 *
 * @author panxin
 * @date 2024/05/08
 */
public class CommentUtils {

    // 静态属性，存储编译后的正则表达式
    private static final Pattern START_WITH_LETTER_OR_SPACE_PATTERN = Pattern.compile("^[a-zA-Z\\s]");
    private static PToolConfig config;
    private static TranslatorService translatorService = ServiceManager.getService(TranslatorService.class);

    {
        PToolConfig config = ServiceManager.getService(PToolConfigComponent.class).getState();
        CommentUtils.config = config;
    }

    public static String getDataType(String dataType, PsiType psiType) {
        String typeName = BaseTypeEnum.findByName(dataType);
        if (StringUtils.isNotEmpty(typeName)) {
            return typeName;
        }
        if (BaseTypeEnum.isName(dataType)) {
            return dataType;
        }
        String multipartFileText = "org.springframework.web.multipart.MultipartFile";
        String javaFileText = "java.io.File";
        if (psiType.getCanonicalText().equals(multipartFileText)
                || psiType.getCanonicalText().equals(javaFileText)) {
            return "file";
        }
        // 查找是否实现自File类
        for (PsiType superType : psiType.getSuperTypes()) {
            if (superType.getCanonicalText().equals(multipartFileText)
                    || superType.getCanonicalText().equals(javaFileText)) {
                return "file";
            }
        }
        return psiType.getPresentableText();
    }


    /**
     * 获取注解说明  不写/@desc/@describe/@description
     * @param comment 所有注释
     * @return String
     */
    public static String getCommentDesc(String comment) {
        String[] strings = comment.split("\n");
        if (strings.length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : strings) {
            String row = StringUtils.deleteWhitespace(string);
            if (StringUtils.startsWith(row, "/**") && row.length()>3) {
                String content = row.substring(3, row.length() - 2).trim();
                if (!content.isEmpty()) {
                    stringBuilder.append(content);
                }
                continue;
            }

            if (StringUtils.isEmpty(row) || StringUtils.startsWith(row,"/*") || StringUtils.startsWith(row,"*/")) {
                continue;
            }
            if (StringUtils.startsWithIgnoreCase(row,"*@desc")
                    && !StringUtils.startsWithIgnoreCase(row,"*@describe")
                    && !StringUtils.startsWithIgnoreCase(row,"*@description")) {
                appendComment(string, stringBuilder, 5);
            }
            if (StringUtils.startsWithIgnoreCase(row,"*@description")) {
                appendComment(string, stringBuilder, 12);
            }
            if (StringUtils.startsWithIgnoreCase(row,"*@describe")) {
                appendComment(string, stringBuilder, 9);
            }
            if (StringUtils.startsWith(row,"*@") || StringUtils.startsWith(row,"*/")) {
                continue;
            }
            int descIndex = StringUtils.ordinalIndexOf(string,"*",1);
            if (descIndex == -1) {
                descIndex = StringUtils.ordinalIndexOf(string,"//",1);
                descIndex += 1;
            }
            String desc = string.substring(descIndex + 1);
            stringBuilder.append(desc);
        }
        return StringUtils.trim(stringBuilder.toString());
    }

    /**
     * 查找评论描述或翻译
     *
     * @param psiClass psi级
     * @return {@link String}
     */
    public static String findCommentDescriptionOrTranslate(PsiModifierListOwner psiClass, String className) {
        PsiComment classComment = null;
        String commentDesc = null;
        for (PsiElement tmpEle : psiClass.getChildren()) {
            if (tmpEle instanceof PsiComment){
                classComment = (PsiComment) tmpEle;
                // 注释的内容
                String tmpText = classComment.getText();
                commentDesc = CommentUtils.getCommentDesc(tmpText);
                commentDesc.replace("\"", "");
                if(StringUtils.isEmpty(commentDesc)){
                    commentDesc = translatorService.translate(className);
                }
                // 有些类里面会有多条注释
                break;

            }
        }
        if (Objects.isNull(classComment)) {
            commentDesc = translatorService.translate(className);
        }
        if(CommentUtils.isStartWithLetterOrSpace(commentDesc)){
            if(config == null){
                PToolConfig config = ServiceManager.getService(PToolConfigComponent.class).getState();
                CommentUtils.config = config;
            }
            commentDesc = config.getChinesePrefix()+ commentDesc;
        }
        return commentDesc;
    }

    /**
     * 移除注释中不合法的字母
     *
     * @param inputSting 输入刺痛
     * @return {@link String}
     */
    public static String formateAnnotation(String inputSting) {
        String output = inputSting.replace("\"", "");
        output = output.replace("(", "");
        output= output.replace(")", "");
        return output;
    }
    /**
     * 获取注解说明  不写/@desc/@describe/@description
     * @param comment 所有注释
     * @return String
     */
    public static Map<String, String> getCommentMethodParam(String comment) {
        Map<String, String> resultMap = new HashMap<>(4);
        String[] strings = comment.split("\n");
        if (strings.length == 0) {
            return null;
        }
        for (String string : strings) {
            String row = StringUtils.deleteWhitespace(string);
            if (StringUtils.isEmpty(row) || StringUtils.startsWith(row,"/**")) {
                continue;
            }

            if (StringUtils.startsWithIgnoreCase(row,"*@param")) {
                int paramIndex = StringUtils.ordinalIndexOf(string,"m",1);
                string = string.substring(paramIndex + 1).trim().replaceAll( "\\s+", " " );
                String[] s = string.split(" ");
                if (s.length < 2) {
                    continue;
                }

                StringBuilder paramDesc = new StringBuilder();
                for (int i = 1; i < s.length; i++) {
                    paramDesc.append(s[i]);
                    if(i == s.length - 1) {
                        break;
                    }
                    paramDesc.append(" ");
                }
                resultMap.put(s[0], paramDesc.toString());
            }
        }
        return resultMap;
    }


    private static void appendComment(String string, StringBuilder stringBuilder, int index) {
        String lowerCaseStr = string.toLowerCase();
        int descIndex = StringUtils.ordinalIndexOf(lowerCaseStr,"@",1);
        descIndex += index;
        String desc = string.substring(descIndex);
        stringBuilder.append(desc);
    }

    /**
     * 判断字符串是否以英文字母或空格开头。
     *
     * @param str 待检查的字符串
     * @return 如果字符串以英文字母或空格开头，返回 true；否则返回 false
     */
    public static boolean isStartWithLetterOrSpace(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // 使用静态Pattern对象来检查字符串是否符合正则表达式
        return START_WITH_LETTER_OR_SPACE_PATTERN.matcher(str).find();
    }

    /**
     * dfdfdf
     * @param args
     */
    public static void main(String[] args) {

        System.out.println(getCommentMethodParam("/**\n" +
                " * @describe panxin\n" +
                " * @param panxin gggggg ggf f \n" +
                "*@param 2020/4/6 fffff\n" +
                " */"));
//        System.out.println(StringUtils.ordinalIndexOf("*@desc fdfdfdfdf","c",1));
    }


}
