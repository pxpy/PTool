package me.panxin.plugin.idea.common.util.translator;

import me.panxin.plugin.idea.config.PToolConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author panxin
 * @date 2024/05/30
 */
public abstract class AbstractTranslator implements Translator {

    private final Map<String, String> en2chCacheMap = new ConcurrentHashMap<>();
    private final Map<String, String> ch2enCacheMap = new ConcurrentHashMap<>();

    /** 配置 */
    private PToolConfig config;

    @Override
    public String en2Ch(String text) {
        if (text == null || text.length() == 0) {
            return "";
        }
        String res = en2chCacheMap.get(text);
        if (res != null && res.length() > 0) {
            return res;
        }
        res = translateEn2Ch(text);
        if (res != null && res.length() > 0) {
            en2chCacheMap.put(text, res);
        }
        return res;
    }

    @Override
    public String ch2En(String text) {
        if (text == null || text.length() == 0) {
            return "";
        }
        String res = ch2enCacheMap.get(text);
        if (res != null && res.length() > 0) {
            return res;
        }
        res = translateCh2En(text);
        if (res != null && res.length() > 0) {
            ch2enCacheMap.put(text, res);
        }
        return res;
    }

    @Override
    public Translator init(PToolConfig config) {
        this.config = config;
        return this;
    }

    @Override
    public PToolConfig getConfig() {
        return this.config;
    }

    /**
     * 清除缓存
     */
    @Override
    public void clearCache() {
        en2chCacheMap.clear();
        ch2enCacheMap.clear();
    }

    /**
     * 中译英
     *
     * @param text 文本
     * @return {@link String}
     */
    protected abstract String translateCh2En(String text);

    /**
     * 英译中
     *
     * @param text 文本
     * @return {@link String}
     */
    protected abstract String translateEn2Ch(String text);
}
