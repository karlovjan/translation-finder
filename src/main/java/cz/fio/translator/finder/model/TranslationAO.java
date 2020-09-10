package cz.fio.translator.finder.model;

import java.util.Objects;

import cz.fio.translator.finder.model.dao.Translation;

public class TranslationAO {

	private Long scriptId; //section id
	private String name; //section name
	private String key; // translation key in section
	private boolean emptySection; //no translation keys
	private boolean unusedSection; //no section in project, there is no class with the section name

	public TranslationAO(Long scriptId, String name, String key) {
		this(scriptId, name, key, false, false);
	}

	public TranslationAO(Long scriptId, String name, boolean emptySection) {
		this(scriptId, name, "", emptySection, false);
	}

	public TranslationAO(Long scriptId, String name, boolean emptySection, boolean unusedSection) {
		this(scriptId, name, "", emptySection, unusedSection);
	}

	private TranslationAO(Long scriptId, String name, String key, boolean emptySection, boolean unusedSection) {
		this.scriptId = scriptId;
		this.name = name;
		this.key = key;
		this.emptySection = emptySection;
		this.unusedSection = unusedSection;
	}

	public TranslationAO(Translation translation) {
		this(translation.getScriptId(), translation.getScriptName(), translation.getKey());
	}

	public static TranslationAO copyOf(TranslationAO translation) {
		Objects.requireNonNull(translation);
		return new TranslationAO(translation.getScriptId(), translation.getName(), translation.getKey(),
				translation.isEmptySection(), translation.isUnusedSection());
	}

	public Long getScriptId() {
		return scriptId;
	}

	public String getName() {
		return name;
	}

	public String getKey() {
		return key;
	}

	public boolean isEmptySection() {
		return emptySection;
	}

	public boolean isUnusedSection() {
		return unusedSection;
	}

	@Override
	public String toString() {
		if (isUnusedSection()) {
			return "Translation{" + "scriptId=" + scriptId + ", name='" + name + '\'' + ", unusedSection='"
					+ unusedSection + '\'' + '}';
		}
		if (isEmptySection()) {
			return "Translation{" + "scriptId=" + scriptId + ", name='" + name + '\'' + ", emptySection='"
					+ emptySection + '\'' + '}';
		}
		return "Translation{" + "scriptId=" + scriptId + ", name='" + name + '\'' + ", key='" + key + '\'' + '}';
	}
}
