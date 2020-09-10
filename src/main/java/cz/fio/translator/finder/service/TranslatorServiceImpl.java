package cz.fio.translator.finder.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cz.fio.translator.finder.exception.GitServiceException;
import cz.fio.translator.finder.model.TranslationAO;
import cz.fio.translator.finder.model.dao.TranslateScript;
import cz.fio.translator.finder.repository.TranslatorDAO;

@Service
public class TranslatorServiceImpl implements TranslatorService {

	private static final Logger log = LoggerFactory.getLogger(TranslatorServiceImpl.class);

	private static final String BRANCH_VERSION = "PROD";
	//V PRAC nejsou nektere sekce
	private static final String IB2_SUBSECTION = "cz/fio/ib2";

	private static final int LANGUAGE_CS = 1;
	private static final int TRANSLATOR_STATUS = 1;

	private final TranslatorDAO repository;
	private final GitService gitService;

	public TranslatorServiceImpl(TranslatorDAO repository, GitService gitService) {
		this.repository = repository;
		this.gitService = gitService;
	}

	@Override
	public List<TranslateScript> findUnusedTranslationSections() {

		try {
			var sections = repository.getScriptsForBranch(LANGUAGE_CS, BRANCH_VERSION, TRANSLATOR_STATUS);
			return findUnusedTranslationSections(sections);
		} catch (IOException e) {
			throw new RuntimeException("Error in reading project directory: " + gitService.getRepoFolder(), e);
		} catch (Exception e) {
			throw new RuntimeException("finding unused translation error", e);
		} finally {
			gitService.closeRepo();
		}

	}

	private List<TranslateScript> findUnusedTranslationSections(String branch, List<TranslateScript> unusedSections)
			throws GitServiceException, IOException {
		gitService.checkout(branch);

		final List<Path> srcFiles = new ArrayList<>();

		listSourceFiles(Paths.get(gitService.getRepoFolder()), srcFiles);

		return unusedSections.stream()
				.filter(section -> srcFiles.stream().filter(path -> filterUnusedSection(section, path)).findFirst()
						.isEmpty()).collect(Collectors.toList());

	}

	private List<TranslateScript> findUnusedTranslationSections(List<TranslateScript> sections)
			throws IOException, GitServiceException {

		List<TranslateScript> ib2ProdSection = sections.stream().filter(this::filterBranche)
				.filter(this::filterRootSection).collect(Collectors.toList());

		List<TranslateScript> unusedSections = findUnusedTranslationSections("master", ib2ProdSection);

		List<String> branches = gitService.getIB2RemoteBranches();

		for (String branch : branches) {

			unusedSections = findUnusedTranslationSections(branch, unusedSections);

		}

		return unusedSections;
	}

	@Override
	public List<TranslateScript> findEmptyTranslationSections() {

		var sections = repository.getScriptsForBranch(LANGUAGE_CS, BRANCH_VERSION, TRANSLATOR_STATUS);

		return sections.stream().filter(this::filterBranche)
				.filter(this::filterRootSection).filter(this::filterEmptyTranslationSection).collect(Collectors.toList());

	}

	@Override
	public List<TranslationAO> findUnusedTranslations(String projectPath) {
		Objects.requireNonNull(projectPath);

		final List<Path> srcFiles = new ArrayList<>();
		try {
			listSourceFiles(Paths.get(projectPath), srcFiles);
		} catch (IOException e) {
			throw new RuntimeException("Error in reading project directory: " + projectPath, e);
		}

		log.info("Found files in directory {} {}", projectPath, srcFiles.size());

		/*
		- filter branche and  root section
		- filter section with no class in project
		- filter emtpy sections
		- filter translation
		 */

		return repository.getScriptsForBranch(LANGUAGE_CS, BRANCH_VERSION, TRANSLATOR_STATUS).parallelStream()
				.filter(this::filterBranche).filter(this::filterRootSection).flatMap(
						section -> srcFiles.stream().filter(path -> filterUnusedSection(section, path)).findFirst()
								.map(p -> Stream.of(section)).orElseGet(() -> logUnusedSection(section)))
				.flatMap(this::mapToTranslationAO).filter(t -> filterClassNamesInKey(t.getKey()))
				.filter(t -> filterKeysWithDashChar(t.getKey())).filter(t -> filterDropDownChoiceNUll(t.getKey()))
				.filter(t -> srcFiles.stream().filter(path -> filterUnusedKey(path, t)).findFirst().isEmpty())
				.collect(Collectors.toList());

	}

	@Override
	public List<TranslationAO> findUnusedTranslations() {

		try {
			var sections = repository.getScriptsForBranch(LANGUAGE_CS, BRANCH_VERSION, TRANSLATOR_STATUS);

			final List<TranslateScript> unusedSections = findUnusedTranslationSections(sections);

			log.info("Unused sections count {}", unusedSections.size());
			unusedSections.forEach(us -> log.info(us.toString()));

			List<TranslateScript> ib2ProdSection = sections.stream().filter(this::filterBranche)
					.filter(this::filterRootSection).collect(Collectors.toList());

			//filter only used sections
			var usedSectionsStream = ib2ProdSection.stream()
					.filter(s -> unusedSections.stream().noneMatch(us -> s.getName().equals(us.getName())))
					.collect(Collectors.toList());

			List<TranslationAO> unusedTranslations = findUnusedTranslationForUsedSectionsOnMaster(usedSectionsStream);

			List<String> branches = gitService.getIB2RemoteBranches();

			for (String branch : branches) {

				unusedTranslations = findUnusedTranslation(branch, unusedTranslations);

			}

			return unusedTranslations;

		} catch (IOException e) {
			throw new RuntimeException("Error in reading project directory: " + gitService.getRepoFolder(), e);
		} catch (Exception e) {
			throw new RuntimeException("finding unused translation error", e);
		} finally {
			gitService.closeRepo();
		}
	}

	private List<TranslationAO> findUnusedTranslationForUsedSectionsOnMaster(List<TranslateScript> usedSections)
			throws GitServiceException, IOException {
		gitService.checkout("master");

		final List<Path> srcFiles = new ArrayList<>();

		listSourceFiles(Paths.get(gitService.getRepoFolder()), srcFiles);

		return usedSections.stream().flatMap(this::mapToTranslationAO).filter(t -> filterClassNamesInKey(t.getKey()))
				.filter(t -> filterKeysWithDashChar(t.getKey())).filter(t -> filterDropDownChoiceNUll(t.getKey()))
				.filter(t -> srcFiles.stream().filter(path -> filterUnusedKey(path, t)).findFirst().isEmpty())
				.collect(Collectors.toList());
	}

	private List<TranslationAO> findUnusedTranslation(String branch, List<TranslationAO> unusedTranslations)
			throws IOException, GitServiceException {
		gitService.checkout(branch);

		final List<Path> srcFiles = new ArrayList<>();

		listSourceFiles(Paths.get(gitService.getRepoFolder()), srcFiles);

		return unusedTranslations.stream()
				.filter(t -> srcFiles.stream().filter(path -> filterUnusedKey(path, t)).findFirst().isEmpty())
				.collect(Collectors.toList());
	}

	private Stream<TranslationAO> mapToTranslationAO(TranslateScript section) {
		var translationDB = repository.getTranslation(section.getScriptId(), LANGUAGE_CS, BRANCH_VERSION);

		if (translationDB.isEmpty()) {
			return logEmptySection(section);
		}

		return translationDB.stream().map(TranslationAO::new);
	}

	private boolean filterEmptyTranslationSection(TranslateScript section) {
		var translationDB = repository.getTranslation(section.getScriptId(), LANGUAGE_CS, BRANCH_VERSION);

		return translationDB.isEmpty();
	}

	private Stream<TranslateScript> logUnusedSection(TranslateScript section) {
		log.info("Unused section: {}", section);
		return Stream.empty();
	}

	private Stream<TranslationAO> logEmptySection(TranslateScript section) {
		log.info("Empty section: {}", section);
		return Stream.empty();
	}

	private boolean filterBranche(TranslateScript section) {
		return BRANCH_VERSION.equals(section.getBranch());
	}

	private boolean filterRootSection(TranslateScript section) {
		return section.getName() != null && section.getName().startsWith(IB2_SUBSECTION);

	}

	private boolean filterClassNamesInKey(String translationKey) {
		String[] translationParts = translationKey.split("\\.");
		for (String part : translationParts) {
			if (Character.isUpperCase(part.charAt(0))) {
				//null values for dropdownfields are filtered
				return false;
			}

		}
		return true;
	}

	private boolean filterDropDownChoiceNUll(String translationKey) {
		return !translationKey.endsWith("null");
	}

	private boolean filterKeysWithDashChar(String translationKey) {
		return translationKey.indexOf('-') < 0;
	}

	private boolean filterUnusedSection(TranslateScript section, Path srcFile) {
		boolean isWicketPackage = isWicketPackage(section.getName());
		String packageName = section.getName().substring(0, section.getName().lastIndexOf('/'));
		String className = section.getName().substring(section.getName().lastIndexOf('/') + 1);
		boolean sectionInPackage = srcFile.toString().lastIndexOf(packageName) >= 0;

		if (isWicketPackage) {

			if (sectionInPackage) {
				return true;
			}
		} else if (srcFile.endsWith(section.getName() + ".java")) {
			return true;
		} else if (sectionInPackage) {
			Pattern classNamePattern = Pattern.compile("\\b" + className + "\\b");
			if (searchInFile(classNamePattern, srcFile))
				return true;
		}

		return false;
	}

	private boolean filterUnusedKey(Path srcFile, TranslationAO translation) {
		if (translation.getKey().isBlank()) {
			log.info("Translation with empty key: {}", translation);
			return true;
		}

		//smazim se eliminovat Component-specific resources
		List<String> searchingKeys = reduceToComponentSpecificKeys(translation.getKey());

		for (String key : searchingKeys) {
			if (searchTranslationInFile(key, srcFile))
				return true;

		}

		return false;
	}

	private List<String> reduceToComponentSpecificKeys(String translationKey) {
		String[] translationKeyParts = translationKey.split("\\.");
		List<String> searchingKeys = new ArrayList<>();
		String tmp;
		for (int i = 0; i < translationKeyParts.length; i++) {
			tmp = "";
			for (int j = searchingKeys.size(); j < translationKeyParts.length; j++) {
				tmp += (translationKeyParts[j] + (j < translationKeyParts.length - 1 ? "." : ""));
			}
			searchingKeys.add(tmp);
		}
		return searchingKeys;
	}

	private boolean searchTranslationInFile(String translationKey, Path srcFile) {
		try (BufferedReader reader = Files.newBufferedReader(srcFile, StandardCharsets.UTF_8)) {

			String line;

			while ((line = reader.readLine()) != null) {

				if (line.contains(translationKey + "\"")) {
					return true;
				}

			}

		} catch (Exception e) {
			throw new RuntimeException("Error in reading file " + srcFile.toString(), e);
		}
		return false;
	}

	private boolean searchInFile(Pattern regexPattern, Path srcFile) {
		try (BufferedReader reader = Files.newBufferedReader(srcFile, StandardCharsets.UTF_8)) {

			String line;

			while ((line = reader.readLine()) != null) {

				if (regexPattern.matcher(line).find()) {
					return true;
				}

			}

		} catch (Exception e) {
			throw new RuntimeException("Error in reading file " + srcFile.toString(), e);
		}
		return false;
	}

	private boolean isWicketPackage(String sectionName) {

		return "wicket-package".equals(sectionName.substring(sectionName.lastIndexOf('/') + 1));
	}

	private void listSourceFiles(Path dir, List<Path> resultPaths) throws IOException {
		//glob "*.{java,html}"
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {

			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					listSourceFiles(entry, resultPaths);
				} else if (Files.isRegularFile(entry) && entry.toString().contains("/ib2/") && entry.toString()
						.contains("/src/main/java/") && entry.getFileName() != null && (
						entry.getFileName().toString().endsWith(".java") || entry.getFileName().toString()
								.endsWith(".html"))) {
					resultPaths.add(entry);
				}

			}
		}
	}

}
