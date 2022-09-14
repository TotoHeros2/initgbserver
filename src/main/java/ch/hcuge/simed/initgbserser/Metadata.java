package ch.hcuge.simed.initgbserser;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Metadata implements Serializable {
	private String subject_dimension;

	public Metadata() {}

	public void setSubjectDimension(String subject_dimension) {
		this.subject_dimension = subject_dimension;
	}
	
	@JsonProperty("subject_dimension")
	public String getSubjectDimension() {
		return this.subject_dimension;
	}
}

