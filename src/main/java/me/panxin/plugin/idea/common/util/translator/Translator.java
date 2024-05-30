package me.panxin.plugin.idea.common.util.translator;


import me.panxin.plugin.idea.config.PToolConfig;

/**
 * 翻译
 *
 * @author panxin
 * @date 2024/05/30
 */
public interface Translator {

    /**
     * 英译中
     *
     * @param text 文本
     * @return {@link String}
     */
    String en2Ch(String text);

    /**
     * 中译英
     *
     * @param text 文本
     * @return {@link String}
     */
    String ch2En(String text);

    /**
     * 初始化
     *
     * @param config 配置
     * @return {@link Translator}
     */
    Translator init(PToolConfig config);

    /**
     * 获取配置
     *
     * @return {@link PToolConfig}
     */
    PToolConfig getConfig();

    /**
     * 清除缓存
     */
    void clearCache();

}
