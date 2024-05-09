package me.panxin.plugin.idea.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.jetbrains.annotations.Nullable;

public class GenParentClassConfirmDialog extends DialogWrapper {
  private final String content;
  
  public GenParentClassConfirmDialog(String title, String content) {
    super(true);
    setTitle(title);
    setCancelButtonText("No");
    setOKButtonText("Yes");
    setResizable(false);
    this.content = content;
    init();
  }
  
  @Nullable
  protected JComponent createCenterPanel() {
    return new JLabel(this.content);
  }
}
