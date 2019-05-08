import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public class OpenAtBacklogGitURLAction extends BacklogGitURLAction {
    public OpenAtBacklogGitURLAction() {
        super("OpenAtBacklog");
    }

    public void actionPerformed(AnActionEvent event) {
        String url = getUrl(event, URL_TYPE.COMMIT_FILE);
        if (url == null) return;

        CopyPasteManager.getInstance().setContents(new StringSelection(url));
        BrowserUtil.browse(url);
    }
}