package cz.fio.translator.finder.model.dao;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.MappedSuperclass;
import javax.persistence.SqlResultSetMapping;

@MappedSuperclass
@SqlResultSetMapping(name = "TranslateScript", classes = @ConstructorResult(targetClass = TranslateScript.class, columns = {
		@ColumnResult(name = "ID_script", type = Long.class), @ColumnResult(name = "S10_branch", type = String.class),
		@ColumnResult(name = "S1K_script_name", type = String.class) }))
public class TranslateScript {

	//	@Id
	//	@Column(name = "ID_script")
	private Long scriptId;

	//	@Column(name = "S10_branch")
	private String branch;

	//	@Column(name = "S1K_script_name")
	private String name;

	public TranslateScript(Long scriptId, String branch, String name) {
		this.scriptId = scriptId;
		this.branch = branch;
		this.name = name;
	}

	public Long getScriptId() {
		return scriptId;
	}

	public void setScriptId(Long scriptId) {
		this.scriptId = scriptId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	@Override
	public String toString() {
		return "TranslateScript{" + "scriptId=" + scriptId + ", branch='" + branch + '\'' + ", name='" + name + '\''
				+ '}';
	}
}
