package me.panxin.plugin.idea.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author PanXin
 * @version $ Id: POJO, v 0.1 2023/05/15 11:24 PanXin Exp $
 */
public class POJO {

    private static Set<String> KEYWORDS = new HashSet<>(Arrays.asList("po","vo","dto","do","model"));

    public static boolean isPOJO(PsiDirectory containingDirectory, String className){
        String directoryName = containingDirectory.getName();
        if (isPOJO(directoryName, className)) {
            return true;
        } else {
            VirtualFile virtualFile = containingDirectory.getVirtualFile();
            if(virtualFile !=null){
                String path = virtualFile.getPath();
                String[] split = path.split("/");
                for (String s : split) {
                    if(KEYWORDS.contains(s.toLowerCase())){
                        return true;
                    }

                }
            }
            return false;
        }
    }

    public static boolean isPOJO(String directoryName, String className) {
        directoryName = directoryName.toLowerCase();
        className = className.toLowerCase();
        // 判断文件是否在 modal、vo、dto 包路径下
        if(KEYWORDS.contains(directoryName)){
            return true;
        }
        if (className != null && (className.endsWith("vo") || className.endsWith("dto") || className.endsWith("po")|| className.endsWith("do"))){
            return true;
        }
        return false;

    }
}