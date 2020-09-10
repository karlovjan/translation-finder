package cz.fio.translator.finder.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cz.fio.translator.finder.exception.GitServiceException;

@Service
public class GitServiceImpl implements GitService {

	private static final Logger log = LoggerFactory.getLogger(GitServiceImpl.class);

	private static final String GIT_REPO_FOLDER = "workRepo";

	private Git git = null;

	private Path repoPath = Paths.get(GIT_REPO_FOLDER);

	private Git getGitRepo() throws GitServiceException {
		if (git == null) {
			initGitRepo();
		}
		return git;
	}

	@Override
	public String getRepoFolder() {
		return GIT_REPO_FOLDER;
	}

	private void initGitRepo() throws GitServiceException {
		createRepoFolder();
		log.info("Cloning git java repo");
		try {
			git = Git.cloneRepository().setURI("ssh://git.private.fio.cz/srv/git/java/").setDirectory(repoPath.toFile())
					.call();

		} catch (GitAPIException e) {
			closeRepo();
			throw new GitServiceException("Create git working repo error", e);
		}
	}

	@Override
	public void closeRepo() {
		if (git != null) {
			log.info("Closing git java repo");
			git.close();
			git = null;
		}

		deleteRepoDir();

	}

	private void deleteRepoDir() {
		log.info("Deleting working git repo folder");
		try {
			Files.walk(repoPath).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(file -> {
				if (!file.delete()) {
					log.info("File {} was not deleted!", file.getName());
				}
			});
		} catch (Exception e) {
			log.error("", e);
		}
	}

	private void createRepoFolder() throws GitServiceException {
		log.info("Creating working git repo folder");
		File repoDir = repoPath.toFile();
		if (repoDir.exists()) {
			deleteRepoDir();
		}

		try {
			Files.createDirectories(repoPath);
		} catch (IOException e) {
			throw new GitServiceException("Create directory for repo failed: " + GIT_REPO_FOLDER, e);
		}
	}

	@Override
	public List<String> getIB2RemoteBranches() throws GitServiceException {
		final String remoteBranchPrefix = "refs/remotes/origin/";
		try {
			var branches = getGitRepo().branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
			return branches.stream().filter(bref -> bref.getName().contains("/ib2-"))
					.map(ref -> ref.getName().substring(remoteBranchPrefix.length()))
					.filter(b -> !"ib2-to-devel".equals(b)).collect(Collectors.toList());
		} catch (Exception e) {
			closeRepo();
			throw new GitServiceException("Get branches error", e);
		}
	}

	@Override
	public void checkout(String branch) throws GitServiceException {

		try {
			Objects.requireNonNull(branch);

			if (isBranchCheckouted(branch)) {
				log.info("Pull branch {}", branch);
				getGitRepo().pull().setRebase(true).call();
			} else if (hasBranchCheckouted(branch)) {
				log.info("Checkout branch {}", branch);
				getGitRepo().checkout().setName(branch).call();
			} else {
				log.info("Checkout branch {}", branch);
				getGitRepo().checkout().setCreateBranch(true).setName(branch)
						.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK)
						.setStartPoint("origin/" + branch).call();
			}
		} catch (Exception e) {
			closeRepo();
			throw new GitServiceException("Checkout error for branch: " + branch, e);
		}
	}

	private boolean isBranchCheckouted(String branch) throws GitServiceException, IOException {
		return getGitRepo().getRepository().getBranch().equals(branch);
	}

	private boolean hasBranchCheckouted(String branch) throws GitServiceException, GitAPIException {
		//returns local branches
		return getGitRepo().branchList().call().stream()
				.anyMatch(b -> b.getName().substring(b.getName().lastIndexOf('/') + 1).equals(branch));
	}
}
