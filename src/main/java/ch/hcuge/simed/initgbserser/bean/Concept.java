package ch.hcuge.simed.initgbserser.bean;

import ch.hcuge.simed.initgbserser.NodeType;

public class Concept {
	
	private Long conceptCode;
	private String conceptName;
	private NodeType type;
	
	public Concept(Long conceptCode, String conceptName) {
		super();
		this.conceptCode = conceptCode;
		this.conceptName = conceptName;
	}
	

	public Long getConceptCode() {
		return conceptCode;
	}


	public void setConceptCode(Long conceptCode) {
		this.conceptCode = conceptCode;
	}


	public String getConceptName() {
		return conceptName;
	}
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}


	public NodeType getType() {
		return type;
	}


	public void setType(NodeType type) {
		this.type = type;
	}
	

}
