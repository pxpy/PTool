package me.panxin.plugin.idea.listener;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.IdeFrame;
import me.panxin.plugin.idea.common.util.NotificationUtil;
import me.panxin.plugin.idea.common.util.translator.TranslatorService;
import me.panxin.plugin.idea.config.PToolConfig;
import me.panxin.plugin.idea.config.PToolConfigComponent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.net.URI;

/**
 * 应用程序激活监听器
 *
 * @author wangchao
 * @date 2022/03/13
 */
public class AppActivationListener implements ApplicationActivationListener {
    private static final Logger LOGGER = Logger.getInstance(AppActivationListener.class);

    /** 是否已经通知 */
    private volatile boolean hasNotice = false;

    private static class SingletonHolder {
        private static final AppActivationListener INSTANCE = new AppActivationListener();
    }

    public static AppActivationListener getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Override
    public synchronized void applicationActivated(@NotNull IdeFrame ideFrame) {
        activate();
    }

    /**
     * 激活
     */
    public synchronized void activate() {
        support();
        serviceInit();
    }

    /**
     * 支持
     */
    private void support() {
        if (hasNotice) {
            return;
        }

        AnAction starAction = new NotificationAction("⭐ 去点star") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                try {
                    Desktop dp = Desktop.getDesktop();
                    if (dp.isSupported(Desktop.Action.BROWSE)) {
                        dp.browse(URI.create("https://github.com/pxpy/PTool"));
                    }
                } catch (Exception ex) {
                    LOGGER.error("打开链接失败:https://github.com/pxpy/PTool", ex);
                }
            }
        };



        NotificationUtil.notify("支持PTool", "如果这款小而美的插件为您节约了不少时间，请支持一下开发者！",
            starAction, starAction, starAction);

        hasNotice = true;
    }

    /**
     * 服务初始化
     */
    private void serviceInit() {
        PToolConfig config = ServiceManager.getService(PToolConfigComponent.class).getState();

        TranslatorService translatorService = ServiceManager.getService(TranslatorService.class);
        translatorService.init(config);
    }

    @Override
    public void applicationDeactivated(@NotNull IdeFrame ideFrame) {
        applicationActivated(ideFrame);
    }
}