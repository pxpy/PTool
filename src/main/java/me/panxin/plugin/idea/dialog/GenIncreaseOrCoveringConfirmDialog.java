package me.panxin.plugin.idea.dialog;

import com.intellij.openapi.ui.DialogWrapper;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.jetbrains.annotations.Nullable;

public class GenIncreaseOrCoveringConfirmDialog extends DialogWrapper {
  private final String content;
  
  public GenIncreaseOrCoveringConfirmDialog(String title, String content) {
    super(true);
    setTitle(title);
    setCancelButtonText("Overwrite");
    setOKButtonText("Increase");
    setResizable(false);
    this.content = content;
    init();
  }
  
  @Nullable
  protected JComponent createCenterPanel() {
    return new JLabel(this.content);
  }
}
