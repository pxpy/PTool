package me.panxin.plugin.idea.setting;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import me.panxin.plugin.idea.config.PToolConfig;
import me.panxin.plugin.idea.config.PToolConfigComponent;
import me.panxin.plugin.idea.ui.PToolSettingsView;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

/**
 * @author panxin
 * @date 2024/05/30
 */
public class PToolSettingsConfigurable implements Configurable {

    private PToolConfig config = ServiceManager.getService(PToolConfigComponent.class).getState();
    private PToolSettingsView view = new PToolSettingsView();

    @Nls(capitalization = Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "EasyDocJavadoc";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return view.getComponent();
    }

    @Override
    public boolean isModified() {
        if (!Objects.equals(config.getAuthor(), view.getAuthorTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getDateFormat(), view.getDateFormatTextField().getText())) {
            return true;
        }
        if (!Objects.equals(config.getDocPriority(), view.getDocPriority())) {
            return true;
        }
        if (!Objects.equals(config.getSimpleFieldDoc(), view.getSimpleDocButton().isSelected())) {
            return true;
        }
        if (!Objects.equals(config.getMethodReturnType(), view.getMethodReturnType())) {
            return true;
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        config.setAuthor(view.getAuthorTextField().getText());
        config.setDateFormat(view.getDateFormatTextField().getText());
        config.setSimpleFieldDoc(view.getSimpleDocButton().isSelected());
        config.setMethodReturnType(view.getMethodReturnType());
        config.setDocPriority(view.getDocPriority());

        if (config.getAuthor() == null) {
            throw new ConfigurationException("作者不能为null");
        }
        if (config.getDateFormat() == null) {
            throw new ConfigurationException("日期格式不能为null");
        }
        if (config.getDocPriority() == null) {
            throw new ConfigurationException("类注释优先级不能为null");
        }
        if (config.getSimpleFieldDoc() == null) {
            throw new ConfigurationException("注释形式不能为null");
        }
        if (!PToolConfig.CODE_RETURN_TYPE.equals(config.getMethodReturnType())
            && !PToolConfig.LINK_RETURN_TYPE.equals(config.getMethodReturnType())
            && !PToolConfig.DOC_RETURN_TYPE.equals(config.getMethodReturnType())) {
            throw new ConfigurationException("方法返回模式不能为空");
        }
    }

    @Override
    public void reset() {
        view.refresh();
    }
}
