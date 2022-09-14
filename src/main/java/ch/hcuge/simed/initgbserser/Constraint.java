package ch.hcuge.simed.initgbserser;

import java.io.Serializable;

public class Constraint  implements Serializable{
	private String study_id;
	private String type;
	private Constraint[] args;

	public Constraint() {}

	public void setStudyId(String study_id) {
		this.study_id = study_id;
	}

	public String getStudyId() {
		return this.study_id;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return this.type;
	}

	public void setArgs(Constraint[] args) {
		this.args = args;
	}

	public Constraint[] getArgs() {
		return this.args;
	}
}

