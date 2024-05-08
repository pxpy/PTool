package me.panxin.plugin.idea.utils;

/**
 * @author PanXin
 * @version $ Id: POJO, v 0.1 2023/05/15 11:24 PanXin Exp $
 */
public class POJO {

    public static boolean isPOJO(String directoryName, String className) {
        directoryName = directoryName.toLowerCase();
        className = className.toLowerCase();
        // 判断文件是否在 modal、vo、dto 包路径下
        if(directoryName.equals("model") || directoryName.equals("vo") || directoryName.equals("po") || directoryName.equals("dto")){
            return true;
        }
        if (className != null && (className.endsWith("vo") || className.endsWith("dto") || className.endsWith("po"))){
            return true;
        }
        return false;

    }
}