import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.GitBranch;
import git4idea.GitUtil;
import git4idea.repo.GitRemote;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;

abstract class BacklogGitURLAction extends AnAction {

    enum URL_TYPE {
        COMMIT_FILE,
        PULL_REQUEST
    }

    public BacklogGitURLAction(String text) {
        super(text);
    }

    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        e.getPresentation().setVisible(
                project != null
                        && editor != null
        );
        e.getPresentation().setIcon(IconLoader.getIcon(
                "/backlog_icon.svg"));
    }

    public String getUrl(AnActionEvent event, URL_TYPE urlType) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        VirtualFile file = event.getDataContext().getData(CommonDataKeys.VIRTUAL_FILE);

        return getUrl(project, editor, file, urlType);
    }

    private String getUrl(Project project, Editor editor, VirtualFile file, URL_TYPE urlType) {
        String viewBasePath = project.getBasePath();
        String filePath = file.getCanonicalPath()
                .replace(viewBasePath + "/", "");

        GitRepositoryManager repositoryManager = GitUtil.getRepositoryManager(project);
        GitRepository gitRepository = repositoryManager.getRepositoryForFile(file);
        if (gitRepository == null)
            return null;
        GitBranch gitBranch = gitRepository.getCurrentBranch();
        if (gitBranch == null)
            return null;

        Collection<GitRemote> remotes = gitRepository.getRemotes();
        if (remotes == null || remotes.size() == 0)
            return null;

        String remoteUrl = ((GitRemote) remotes.toArray()[0]).getFirstUrl();
        // nulab@nulab.git.backlog.jp:/BLG/backlog-scala.git
        // https://nulab.backlog.jp/git/BLG/backlog-scala.git

        remoteUrl = remoteUrl.replace(".git.", ".").replace(".git", "/");
        // nulab@nulab.backlog.jp:/BLG/backlog-scala/
        // https://nulab.backlog.jp/git/BLG/backlog-scala/

        if (remoteUrl.startsWith("https://")) {

        } else if (remoteUrl.contains("@")) {
            remoteUrl = remoteUrl.replace(":/", "/git/");
            remoteUrl = "https://" + remoteUrl.substring(remoteUrl.indexOf("@") + 1);
        } else {
            return null;
        }
        String branch = gitBranch.getName();

        if (urlType == URL_TYPE.COMMIT_FILE) {
            int startLine = editor.getCaretModel().getLogicalPosition().line + 1;
            return remoteUrl + "blob/" + branch + "/" + filePath + "#" + startLine;
        } else if (urlType == URL_TYPE.PULL_REQUEST) {
            try {
                return remoteUrl + "pullRequests/add/master..." + URLEncoder.encode(branch, "utf-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        } else {
            return null;
        }
    }
}