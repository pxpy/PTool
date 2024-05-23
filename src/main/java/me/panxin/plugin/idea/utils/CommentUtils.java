package me.panxin.plugin.idea.utils;

import com.intellij.psi.PsiType;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 注释实用程序
 *
 * @author panxin
 * @date 2024/05/08
 */
public class CommentUtils {


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
            if (StringUtils.isEmpty(row) || StringUtils.startsWith(row,"/**")) {
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
