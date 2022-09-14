package ch.hcuge.simed.initgbserser;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



public class TreeNode implements Serializable{
	private static final long serialVersionUID = 1L;

	  private String concept_code;
	  private String concept_path;
	  private String name;
	  private String full_name;
	  private String type;
	  private String study_id;
//	  private TreeNode[] children = new TreeNode[0];
	  private ArrayList<TreeNode> children = new ArrayList<TreeNode>();

	  private String[] visual_attributes;
	  private Constraint constraint;
	  private Metadata metadata;


	  public TreeNode() {}

	  public void setConceptCode(String concept_code) {
	    this.concept_code = concept_code;
	  }

	  public String getConceptCode() {
	    return this.concept_code;
	  }

	  public void setConceptPath(String concept_path) {
	    this.concept_path = concept_path;
	  }

	  public String getConceptPath() {
	    return this.concept_path;
	  }

	  public void setName(String name) {
	    this.name = name;
	  }

	  public String getName() {
	    return this.name;
	  }

	  public void setFullName(String full_name) {
	    this.full_name = full_name;
	  }

	  public String getFullName() {
	    return this.full_name;
	  }

	  public void setType(String type) {
	    this.type = type;
	  }

	  public String getType() {
	    return this.type;
	  }

	  public void setStudyId(String study_id) {
	    this.study_id = study_id;
	  }

	  public String getStudyId() {
	    return this.study_id;
	  }

//	  public void setChildren(TreeNode[] children) {
//	    this.children = children;
//	  }
//
//	  public TreeNode[] getChildren() {
//	    return this.children;
//	  }

	  public void setVisualAttributes(String[] visual_attributes) {
	    this.visual_attributes = visual_attributes;
	  }

	  public String[] getVisualAttributes() {
	    return this.visual_attributes;
	  }

	  public void setConstraint(Constraint constraint) {
	    this.constraint = constraint;
	  }

	  public Constraint getConstraint() {
	    return this.constraint;
	  }

	  public void setMetadata(Metadata metadata) {
	    this.metadata = metadata;
	  }

	  public Metadata getMetadata() {
	    return this.metadata;
	  }

	public ArrayList<TreeNode> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<TreeNode> children) {
		this.children = children;
	}

	public TreeNode copy() {
		TreeNode copied = new TreeNode();
		copied.setConceptCode(this.getConceptCode());
		copied.setConceptPath(this.getConceptPath());
		copied.setConstraint(this.getConstraint());
		copied.setFullName(this.getFullName());
		copied.setMetadata(this.getMetadata());
		copied.setName(this.getName());
		copied.setStudyId(this.getStudyId());
		copied.setType(this.getType());
		copied.setVisualAttributes(this.getVisualAttributes());
		
		return copied;
	}
	@Override
	public boolean equals(Object o) {
	    if (o == this)
	        return true;
	    if (!(o instanceof TreeNode))
	        return false;
	    TreeNode other = (TreeNode)o;
	    return this.getConceptCode().equalsIgnoreCase(other.getConceptCode());
	}	
}

