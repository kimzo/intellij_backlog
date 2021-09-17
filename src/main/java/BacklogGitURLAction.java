import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.*;
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
import java.util.List;

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
        GitRepository gitRepository = repositoryManager.getRepositoryForFileQuick(file);
        if (gitRepository == null)
            return null;
        GitBranch gitBranch = gitRepository.getCurrentBranch();
        if (gitBranch == null)
            return null;

        Collection<GitRemote> remotes = gitRepository.getRemotes();
        if (remotes == null || remotes.size() == 0)
            return null;

        String remoteUrl = ((GitRemote) remotes.toArray()[0]).getFirstUrl();
        remoteUrl = remoteUrl.replace(".git.", ".").replace(".git", "/");

        if (remoteUrl.startsWith("https://")) {

        } else if (remoteUrl.contains("@")) {
            remoteUrl = remoteUrl.replace(":/", "/git/");
            remoteUrl = "https://" + remoteUrl.substring(remoteUrl.indexOf("@") + 1);
        } else {
            return null;
        }
        String branch = gitBranch.getName();

        // add sub  directory
        String subPath = "";
        String localGitRoot = gitRepository.getRoot().getPath();
        if (viewBasePath.length() > localGitRoot.length()) {
            subPath = viewBasePath.substring(localGitRoot.length() + 1) + "/";
        }


        if (urlType == URL_TYPE.COMMIT_FILE) {
            CaretModel caretModel = editor.getCaretModel();
            List<CaretState> caretStates = caretModel.getCaretsAndSelections();
            int startLine, endLine;
            if (caretStates.size() < 1) {
                startLine = endLine = caretModel.getLogicalPosition().line + 1;
            } else {
                CaretState caretState = caretStates.get(0);
                startLine = caretState.getSelectionStart().line + 1;
                endLine = caretState.getSelectionEnd().line + 1;
            }
            String position = "#" + (startLine != endLine ? startLine + "-" + endLine : startLine);
            return remoteUrl + "blob/" + branch + "/" + subPath + filePath + position;
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