import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ide.CopyPasteManager;

import java.awt.datatransfer.StringSelection;

public class CreatePullRequestGitURLAction extends BacklogGitURLAction {
    public CreatePullRequestGitURLAction() {
        super("Create PullRequest");
    }

    public void actionPerformed(AnActionEvent event) {
        String url = getUrl(event, URL_TYPE.PULL_REQUEST);
        if (url == null) return;

        CopyPasteManager.getInstance().setContents(new StringSelection(url));
        BrowserUtil.browse(url);
    }
}