package cz.fio.translator.finder.service;

import java.util.List;

import cz.fio.translator.finder.exception.GitServiceException;

public interface GitService {

	List<String> getIB2RemoteBranches() throws GitServiceException;

	void checkout(String branch) throws GitServiceException;

	String getRepoFolder();

	void closeRepo();
}
