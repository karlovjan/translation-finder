package cz.fio.translator.finder.model.dao;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.MappedSuperclass;
import javax.persistence.SqlResultSetMapping;

@MappedSuperclass
@SqlResultSetMapping(name = "TranslationMapping", classes = @ConstructorResult(targetClass = Translation.class, columns = {
		@ColumnResult(name = "ID_script", type = Long.class),
		@ColumnResult(name = "S255_symbol", type = String.class),
		@ColumnResult(name = "ID_language", type = Integer.class),
		@ColumnResult(name = "TEXT_translate", type = String.class),
		@ColumnResult(name = "S1K_script", type = String.class) }))
public class Translation {

	//	@Column(name = "ID_script")
	private Long scriptId;

	//	@Column(name = "S255_symbol")
	private String key;



	//	@Column(name = "ID_language")
	private Integer lang;

	//	@Column(name = "TEXT_translate")
	private String text;
	//@Column(name = "S1K_script")
	private String scriptName;

	public Translation(Long scriptId, String key, Integer lang, String text, String scriptName) {
		this.scriptId = scriptId;
		this.key = key;
		this.lang = lang;
		this.text = text;
		this.scriptName = scriptName;
	}

	public Long getScriptId() {
		return scriptId;
	}

	public String getKey() {
		return key;
	}

	public String getText() {
		return text;
	}

	public void setScriptId(Long scriptId) {
		this.scriptId = scriptId;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getLang() {
		return lang;
	}

	public void setLang(Integer lang) {
		this.lang = lang;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void setScriptName(String scriptName) {
		this.scriptName = scriptName;
	}
}
