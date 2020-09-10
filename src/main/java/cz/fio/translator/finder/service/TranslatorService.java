package cz.fio.translator.finder.service;

import java.util.List;

import cz.fio.translator.finder.model.TranslationAO;
import cz.fio.translator.finder.model.dao.TranslateScript;

public interface TranslatorService {

	List<TranslateScript> findEmptyTranslationSections();

	List<TranslateScript> findUnusedTranslationSections();

	List<TranslationAO> findUnusedTranslations();
}
