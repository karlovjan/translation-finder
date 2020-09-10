package cz.fio.translator.finder.repository;

import java.util.List;

import cz.fio.translator.finder.model.dao.TranslateScript;
import cz.fio.translator.finder.model.dao.Translation;

public interface TranslatorDAO {
	//ziskanie zoznamu script pre dane parametre (aktualnu verziu dostaneme ako branch="PRAC/TEST/PROD" && status=1 )
	List<TranslateScript> getScriptsForBranch(Integer languageId, String branch, Integer status);

	//ziskanie prekladu
	List<Translation> getTranslation(Long scriptId, Integer languageId, String branch);

}
