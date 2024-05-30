package me.panxin.plugin.idea.action;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {
    private static final Pattern START_WITH_LETTER_OR_SPACE_PATTERN = Pattern.compile("^[a-zA-Z\\s]");

    public static void main(String[] args) {
        String testString = "a测试";
        Matcher matcher = START_WITH_LETTER_OR_SPACE_PATTERN.matcher(testString);
        boolean matches = matcher.matches();
        if (matcher.find()) {
            System.out.println("匹配成功，匹配的字符串是: '" + matcher.group() + "'");
        } else {
            System.out.println("匹配失败");
        }
    }
}