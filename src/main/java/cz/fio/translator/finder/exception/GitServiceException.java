package cz.fio.translator.finder.exception;

public class GitServiceException extends Exception {
	public GitServiceException(String text){
		super(text);
	}
	public GitServiceException(String text, Throwable exception){
		super(text, exception);
	}
}
