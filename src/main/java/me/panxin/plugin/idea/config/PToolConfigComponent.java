package me.panxin.plugin.idea.config;

import com.google.common.collect.Maps;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import me.panxin.plugin.idea.common.util.translator.Consts;
import me.panxin.plugin.idea.config.PToolConfig.TemplateConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author wangchao
 * @date 2019/08/25
 */
@State(name = "pTool", storages = {@Storage("pTool.xml")})
public class PToolConfigComponent implements PersistentStateComponent<PToolConfig> {
    /** 配置 */
    private PToolConfig config;

    @Nullable
    @Override
    public PToolConfig getState() {
        if (config == null) {
            config = new PToolConfig();
            config.setAuthor(System.getProperty("user.name"));
            config.setKdocAuthor(System.getProperty("user.name"));
            config.setDateFormat(Consts.DEFAULT_DATE_FORMAT);
            config.setDocPriority(PToolConfig.DOC_FIRST);
            config.setKdocDateFormat(Consts.DEFAULT_DATE_FORMAT);
            config.setSimpleFieldDoc(true);
            config.setKdocSimpleFieldDoc(true);
            config.setKdocParamType(PToolConfig.LINK_PARAM_TYPE);
            config.setMethodReturnType(PToolConfig.LINK_RETURN_TYPE);
            config.setWordMap(Maps.newTreeMap());
            config.setProjectWordMap(Maps.newTreeMap());
            config.setTranslator(Consts.MICROSOFT_FREE_TRANSLATOR);
            config.setClassTemplateConfig(new TemplateConfig());
            config.setKdocClassTemplateConfig(new TemplateConfig());
            config.setMethodTemplateConfig(new TemplateConfig());
            config.setKdocMethodTemplateConfig(new TemplateConfig());
            config.setFieldTemplateConfig(new TemplateConfig());
            config.setKdocFieldTemplateConfig(new TemplateConfig());
        }
        return config;
    }

    @Override
    public void loadState(@NotNull PToolConfig state) {
        XmlSerializerUtil.copyBean(state, Objects.requireNonNull(getState()));
    }

}
