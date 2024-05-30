package me.panxin.plugin.idea.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Swagger2UpgradeService {

    public static void doReplace(@NotNull Project project, @NotNull Document document) {
        // 读取替换规则
        List<ReplacementRule> rules = loadReplacementRules();

        // 使用 WriteCommandAction 执行写操作
        WriteCommandAction.runWriteCommandAction(project, () -> {
            replaceAnnotations(document, rules);
        });
    }

    private static List<ReplacementRule> loadReplacementRules() {
        List<ReplacementRule> rules = new ArrayList<>();
        try (InputStream is = Swagger2UpgradeService.class.getResourceAsStream("/META-INF/swagger2-upgrade-rules.json")) {
            byte[] bytes = is.readAllBytes();
            String s = new String(bytes);
            JSONArray jsonArray = JSON.parseArray(s);
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                rules.add(new ReplacementRule(
                        jsonObject.getString("pattern"),
                        jsonObject.getString("replacement")
                ));
            }
        } catch (Exception ex) {
            // Handle exceptions (e.g., file not found, JSON parsing errors)
            ex.printStackTrace();
        }
        return rules;
    }

    private static void replaceAnnotations(Document document, List<ReplacementRule> rules) {
        String text = document.getText();

        // 按照读取的顺序执行替换规则
        for (ReplacementRule rule : rules) {
            try {
                text = text.replaceAll(rule.getPattern(), rule.getReplacement());
            } catch (Exception e) {
                System.out.println(rule);
                System.out.println(e);
            }
        }
        document.setText(text);
    }

    // 替换规则类
    private static class ReplacementRule {
        private final String pattern;
        private final String replacement;

        public ReplacementRule(String pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }

        public String getPattern() {
            return pattern;
        }

        public String getReplacement() {
            return replacement;
        }

        @Override
        public String toString() {
            return "ReplacementRule{" +
                    "pattern='" + pattern + '\'' +
                    ", replacement='" + replacement + '\'' +
                    '}';
        }
    }
}