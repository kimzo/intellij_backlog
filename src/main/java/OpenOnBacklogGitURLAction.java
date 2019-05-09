import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public class OpenOnBacklogGitURLAction extends BacklogGitURLAction {
    public OpenOnBacklogGitURLAction() {
        super("OpenAtBacklog");
    }

    public void actionPerformed(AnActionEvent event) {
        String url = getUrl(event, URL_TYPE.COMMIT_FILE);
        if (url == null) return;

        CopyPasteManager.getInstance().setContents(new StringSelection(url));
        BrowserUtil.browse(url);
    }
}