package cz.fio.translator.finder.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;

import org.hibernate.procedure.ProcedureOutputs;
import org.hibernate.query.procedure.ProcedureParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import cz.fio.translator.finder.model.dao.TranslateScript;
import cz.fio.translator.finder.model.dao.Translation;

@Repository
public class TranslatorRepository implements TranslatorDAO {

	private static final Logger log = LoggerFactory.getLogger(TranslatorRepository.class);

	@PersistenceContext
	private EntityManager entityManager;

	//ziskanie zoznamu script pre dane parametre (aktualnu verziu dostaneme ako branch="PRAC/TEST/PROD" && status=1 )
	public List<TranslateScript> getScriptsForBranch(Integer languageId, String branch, Integer status) {
		StoredProcedureQuery sp = entityManager
				.createStoredProcedureQuery("fioib_proc.fioowner.wib_get_script_branch", "TranslateScript");
		sp.registerStoredProcedureParameter("@aID_script", Long.class, ParameterMode.IN);
		sp.registerStoredProcedureParameter("@aID_language", Integer.class, ParameterMode.IN);
		sp.registerStoredProcedureParameter("@aS10_branch", String.class, ParameterMode.IN);
		sp.registerStoredProcedureParameter("@aPEN_stav", Integer.class, ParameterMode.IN);

		((ProcedureParameter) sp.getParameter("@aID_script")).enablePassingNulls(true);

		sp.setParameter("@aID_script", null);
		sp.setParameter("@aID_language", languageId);
		sp.setParameter("@aS10_branch", branch);
		sp.setParameter("@aPEN_stav", status);

		try {
			return sp.getResultList();
		} finally {
			try {
				sp.unwrap(ProcedureOutputs.class).release();
			} catch (Exception e) {
				log.error("Release ProcedureOutputs error", e);
			}
		}
	}

	//ziskanie prekladu
	public List<Translation> getTranslation(Long scriptId, Integer languageId, String branch) {

		Query query = entityManager.createNativeQuery(
				"{call fioib_proc.fioowner.wib_get_translate(:@aID_script,:@aS255_symbol,:@aID_language,:@aS10_branch)}",
				"TranslationMapping");

		query.setParameter("@aID_script", scriptId);
		query.setParameter("@aS255_symbol", null);
		query.setParameter("@aID_language", languageId);
		query.setParameter("@aS10_branch", branch);

		return query.getResultList();

	}
}
