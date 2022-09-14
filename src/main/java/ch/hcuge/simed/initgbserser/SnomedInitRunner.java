package ch.hcuge.simed.initgbserser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.ObjectSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import test.cayenne.OMOP.Concept;
import test.cayenne.OMOP.ConceptAncestor;
import test.cayenne.OMOP.Measurement;
import test.cayenne.OMOP.Observation;

//import ch.hcuge.simed.gbserver.model.api.TreeNode;

//@Component
//@Service
public class SnomedInitRunner /*implements CommandLineRunner */{
	
	final static private String ROOT_NAME = "\\"; 
	
//	@Value("${test}")
	private boolean test = false;
	
	@Value("${ch.hcuge.simed.gbserver.concept.initfilepath}")
	private String initFilePath;
	
	@Value("${ch.hcuge.simed.gbserver.concept.snomedRootCode}")
	private String snomedRootCode;
	
	@Value("${ch.hcuge.simed.gbserver.concept.snomedRootDescendantCodes}")
	private String snomedRootDescendantCodes;
	
	private ObjectContext newObjectContext = null;
	
    private static Logger logger = LoggerFactory.getLogger(SnomedInitRunner.class);


//	private List<TreeNode> fullTreeNodes;

//	private TreeNode rootTreeNode;
	
//	@Autowired
//	private ResourceLoader resourceLoader;
	
	
	// V2 dico to keep all concept/TreeNode already used
	HashMap<Concept, TreeNode> usedConcepts = new HashMap<Concept, TreeNode>();


//	@Override
	public void run(String... args) throws Exception {
		 String testS = (args.length > 0) ? args[0]:"";
		 if (testS.equalsIgnoreCase("test"))
		 {
			 test = true;
		 }
//		    cayenneRuntime = ServerRuntime.builder().addConfig("cayenne-OMOP.xml").build();
        	logger.info("Will try to generate ontology (Snomed concept) cache in  file : " + initFilePath);

			ServerRuntime cayenneRuntime = ServerRuntime.builder().addConfig("cayenne-SNOMED.xml").build();
		    newObjectContext = cayenneRuntime.newContext();	
		    
//		    1) Measurements
        	logger.info("Will try to generate ontology for Measurements");

		    List<Concept> instanciatedConceptForMeasurementString =  getInstanciatedConceptsForMeasurement(true);
			List<Concept> instanciatedConceptForMeasurementNumeric =  getInstanciatedConceptsForMeasurement(false);
			 		    
		    TreeNode snomedRootM = makeSnomedRoots();

		    setPathFromInstanciatedConceptsToRoot(instanciatedConceptForMeasurementString,true,"measurement"); // V2 beast
		    setPathFromInstanciatedConceptsToRoot(instanciatedConceptForMeasurementNumeric,false,"measurement"); // V2 beast
		    
		    // 01.02.22 remove branches not in used under SNOMED CT
		    cleanSnomedCtBranches(snomedRootM);
		    
		    TreeNode measurements = new TreeNode();
		    measurements.setName("Measurements");// SNOMED CT
		    measurements.setStudyId("TEST");
		    measurements.setFullName("\\" + "Measurements" + "\\");
		    measurements.setType("UNKNOWN");
		    measurements.setVisualAttributes(new String[] { "FOLDER", "ACTIVE" });
		    measurements.getChildren().add(snomedRootM);
		    setFullName(measurements,ROOT_NAME);
		    saveToFile(measurements,"measurements");
		    
		    
//		    2) Observations
        	logger.info("Will try to generate ontology for Observations");

		    usedConcepts = new HashMap<Concept, TreeNode>(); // re init 
		    List<Concept> instanciatedConceptString =  getInstanciatedConcepts(true);
		    List<Concept> instanciatedConceptNumeric =  getInstanciatedConcepts(false);		    
		    
//		    HashMap<Concept, String> instanciatedConcepts = getInstanciatedConceptsFromObservation();
		    
		    TreeNode snomedRootO = makeSnomedRoots();
		    
		    setPathFromInstanciatedConceptsToRoot(instanciatedConceptString,true,"observation"); // V2 beast
		    setPathFromInstanciatedConceptsToRoot(instanciatedConceptNumeric,false,"observation"); // V2 beast
		    
		    cleanSnomedCtBranches(snomedRootO);

		    
		    
		    TreeNode observations = new TreeNode();
		    observations.setName("Observations");// SNOMED CT
		    observations.setStudyId("TEST");
		    observations.setFullName("\\" + "Observations" + "\\");
		    observations.setType("UNKNOWN");
		    observations.setVisualAttributes(new String[] { "FOLDER", "ACTIVE" });
		    observations.getChildren().add(snomedRootO);
		    setFullName(observations,ROOT_NAME);

		    if (test)
		    {
//		    	for(Concept LeafConcept : instanciatedConcepts)
//		    	{
//		    		observations.getChildren().add( usedConcepts.get(LeafConcept));
//		    	}
		    }
		    
//		    fullTreeNodes =  Arrays.asList(observations, measurements);

		    saveToFile(observations,"observations");
		    
//		    saveToFile();
		    

            System.exit(0);
	}

	private void cleanSnomedCtBranches(TreeNode snomedRoot) {
		Iterator<TreeNode> itr = snomedRoot.getChildren().iterator();
		while (itr.hasNext()) {
			TreeNode treeNode = itr.next();
			if (treeNode.getChildren().size() == 0)
			{
				itr.remove();
			}
		}
		
	}

	private List<Concept> getInstanciatedConceptsForMeasurement(boolean isString) {
		Expression typeExpression = null;
		if (isString)
			typeExpression = Measurement.VALUE_AS_NUMBER.isNull();
		else 
			typeExpression = Measurement.VALUE_AS_NUMBER.isNotNull();

		List<Long> ids = ObjectSelect.columnQuery(Measurement.class, Measurement.MEASUREMENT_CONCEPT_ID).distinct().where(typeExpression).select(newObjectContext);
//		typeForConcepts = new HashMap<Concept, String>(ids.size());
		List<Concept> concepts = new ArrayList<Concept>();
		for (Long id:ids)
		{
			Concept concept =  ObjectSelect.query(Concept.class).where(Concept.CONCEPT_ID.eq(id)).selectOne(newObjectContext);
			if (concept.getVocabularyId().equalsIgnoreCase("Snomed"))
			{			
//				typeForConcepts.put(concept, value);
				concepts.add(concept);
			}
		}
		return concepts;
	}

	private void setFullName(TreeNode treeNode, String prefix) {
		treeNode.setFullName(prefix + treeNode.getName() + ROOT_NAME);
		treeNode.setConceptPath(treeNode.getFullName());
		System.err.println(treeNode.getFullName());	
		for (TreeNode aNode: treeNode.getChildren())
		{
			setFullName(aNode,treeNode.getFullName());
		}		
	}

	private void setPathFromInstanciatedConceptsToRoot(List<Concept> instanciatedConcepts,boolean isString,String branche) {
		String prefixName = branche.substring(0,1).toUpperCase() + "-";
		
		// V2 
		for (Concept concept: instanciatedConcepts)
		{
			// 1st step create treenode
			TreeNode treeNode = new TreeNode();
			treeNode.setName(concept.getConceptName() + " (" + /*prefixName + */  concept.getConceptCode() + ")");
			treeNode.setStudyId("TEST");
//			treeNode.setFullName(treeNodeUpLevel.getFullName() + concept.getConceptName() + "\\");
//			System.err.println(treeNode.getFullName());
			treeNode.setType("NUMERIC");// NUMERICAL /UNKNOWN / CATEGORICAL
			treeNode.setVisualAttributes(new String[] { "LEAF", "ACTIVE", "NUMERICAL" });
			
			if (isString)
			{
				treeNode.setType("CATEGORICAL");// NUMERICAL /UNKNOWN / CATEGORICAL
				treeNode.setVisualAttributes(new String[] { "LEAF", "ACTIVE", "CATEGORICAL" });
			}
			else
			{
				treeNode.setType("NUMERIC");// NUMERICAL /UNKNOWN / CATEGORICAL
				treeNode.setVisualAttributes(new String[] { "LEAF", "ACTIVE", "NUMERICAL" });
			}
			
			treeNode.setConceptCode(prefixName + concept.getConceptCode());

//			Constraint constraint = new Constraint();
//			constraint.setType("study_name");
//			constraint.setStudyId("TEST");
//			
//			Constraint constraintInt0 = new Constraint();
//			constraintInt0.setType("concept");
//			
//			Constraint constraintInt1 = new Constraint();
//			constraintInt1.setType("study_name");
//			constraintInt1.setStudyId("TEST");	
//			
//			constraint.setArgs(new Constraint[] { constraintInt0, constraintInt1});
			
//			treeNode.setConstraint(constraint);
			usedConcepts.put(concept, treeNode);
			
			Metadata metadata = new Metadata();
			metadata.setSubjectDimension(branche);
			treeNode.setMetadata(metadata);
			
			
			// go up until you find an used concept
			if (!test)
			buildTreeRootForAscendingConcept(concept,treeNode);
		}
	}
	

	private void buildTreeRootForAscendingConcept(Concept conceptDownLevel, TreeNode downTreeNode) {
		List<Concept> ascendants = getDirectAscendants(conceptDownLevel); // to find with min = 1
		for (Concept concept:ascendants)
		{
			if (usedConcepts.get(concept) == null)
			{


				TreeNode treeNode = new TreeNode();
				treeNode.setName(concept.getConceptName() + " (" + concept.getConceptCode() + ")");

				treeNode.setStudyId("TEST");
				//			treeNode.setFullName(treeNodeUpLevel.getFullName() + concept.getConceptName() + "\\");
				treeNode.setType("UNKNOWN");
				treeNode.setConceptCode(concept.getConceptCode());
				treeNode.setVisualAttributes(new String[] { "FOLDER", "ACTIVE" });


//				Constraint constraint1 = new Constraint();
//				constraint1.setType("study_name");
//				constraint1.setStudyId("TEST");
//				treeNode.setConstraint(constraint1);

				// add treenode to Level
				treeNode.getChildren().add(downTreeNode);
				usedConcepts.put(concept, treeNode);

				// go down next level
				buildTreeRootForAscendingConcept(concept,treeNode);
			}
			else 
			{
				usedConcepts.get(concept).getChildren().add(downTreeNode);// add an existing TreeNode
			}
		}

	}

	private List<Concept> getDirectAscendants(Concept conceptUpLevel) {
		List<ConceptAncestor> list = ObjectSelect.query(ConceptAncestor.class).where(
				ConceptAncestor.DESCENDANT_CONCEPT.eq(conceptUpLevel)
				.andExp(ConceptAncestor.MIN_LEVELS_OF_SEPARATION.eq(1))
//				.andExp(ConceptAncestor.MAX_LEVELS_OF_SEPARATION.eq(1))
				)
				.select(conceptUpLevel.getObjectContext());	
		
		List<Concept> concepts = new ArrayList<Concept>();
		for (ConceptAncestor ca: list)
		{
			Concept aConcept = ca.getAscendantConcept();
			if (aConcept.getVocabularyId().equalsIgnoreCase("Snomed"))
				concepts.add(aConcept);
		}

		return concepts;
	}
	
	private void saveToFile(TreeNode treeNode, String prefix) throws IOException {
        Path path = Paths.get(prefix + initFilePath);// work
        
        try {
			Files.delete(path);
		} catch (NoSuchFileException e) {
			// TODO Auto-generated catch block
		}
		byte[] rootByte = SerializationUtils.serialize(treeNode /*fullTreeNodes*/);
		Files.write(path, rootByte, StandardOpenOption.CREATE_NEW);
        logger.info("Ontology cache " + prefix + " writed to file" );		
	}

//	private void saveToFile() throws IOException {
//        Path path = Paths.get(initFilePath);// work
//        
//        try {
//			Files.delete(path);
//		} catch (NoSuchFileException e) {
//			// TODO Auto-generated catch block
//		}
//		byte[] rootByte = SerializationUtils.serialize(fullTreeNodes);
//		Files.write(path, rootByte, StandardOpenOption.CREATE_NEW);
//        logger.info("Ontology cache writed to file" );		
//	}

	private List<Concept> getInstanciatedConcepts(boolean isString ) {
		// Get InstanciatedConcepts from Observation /Measurement etc
//		TEXT / NUMERIC
//		HashMap<Concept, String> typeForConcepts = null;
		Expression typeExpression = null;
		if (isString)
			typeExpression = Observation.VALUE_AS_NUMBER.isNull();
		else 
			typeExpression = Observation.VALUE_AS_NUMBER.isNotNull();

		List<Long> ids = ObjectSelect.columnQuery(Observation.class, Observation.OBSERVATION_CONCEPT_ID).distinct().where(typeExpression).select(newObjectContext);
//		typeForConcepts = new HashMap<Concept, String>(ids.size());
		List<Concept> concepts = new ArrayList<Concept>();
		for (Long id:ids)
		{
			Concept concept =  ObjectSelect.query(Concept.class).where(Concept.CONCEPT_ID.eq(id)).selectOne(newObjectContext);
			if (concept.getVocabularyId().equalsIgnoreCase("Snomed"))
			{			
//				typeForConcepts.put(concept, value);
				concepts.add(concept);
			}
		}
		return concepts;
	}
	

	private TreeNode makeSnomedRoots() {
//		List<Long> ids = ObjectSelect.columnQuery(Observation.class, Observation.OBSERVATION_CONCEPT_ID).distinct().select(newObjectContext);
//		List<Concept> concepts = new ArrayList<Concept>();
//		for (Long id:ids)
//		{
//			Concept concept =  ObjectSelect.query(Concept.class).where(Concept.CONCEPT_ID.eq(id)).selectOne(newObjectContext);
//			if (concept.getVocabularyId().equalsIgnoreCase("Snomed"))
//			{
//				concepts.add(concept);
//			}
//		}
//		System.err.println(concepts);
		// SNOMED CT Concept (SNOMED RT+CTV3) root Snomed CT
//		List<Concept> list = ObjectSelect.query(Concept.class).where(Concept.CONCEPT_NAME.eq("Injury of head")).select(newObjectContext);	
//		List<Concept> list = ObjectSelect.query(Concept.class).where(Concept.CONCEPT_NAME.eq("Open wound of face due to animal bite")).select(newObjectContext);// test	
//		List<Concept> list = ObjectSelect.query(Concept.class).where(Concept.CONCEPT_NAME.eq("SNOMED CT Concept")).select(newObjectContext);// test	
//		Concept concept = list.get(0);
		
		Concept concept = getConcept(snomedRootCode);
		TreeNode rootTreeNode = new TreeNode();
		rootTreeNode.setName(concept.getConceptName());// SNOMED CT
		rootTreeNode.setStudyId("TEST");
		rootTreeNode.setFullName("\\" + concept.getConceptName() + "\\");
		rootTreeNode.setType("UNKNOWN");
		rootTreeNode.setConceptCode(concept.getConceptCode());
		rootTreeNode.setVisualAttributes(new String[] { "FOLDER", "ACTIVE" });

		Constraint constraint1 = new Constraint();
		constraint1.setType("study_name");
		constraint1.setStudyId("TEST");
		rootTreeNode.setConstraint(constraint1);
		
		
		
		String[] codes = snomedRootDescendantCodes.split(";");
		
		for (String code : codes)
		{
			Concept descConcept = getConcept(code);
			TreeNode descTreeNode = new TreeNode();
			descTreeNode.setName(descConcept.getConceptName());
			descTreeNode.setStudyId("TEST");
			descTreeNode.setFullName(rootTreeNode.getFullName() + descConcept.getConceptName() + "\\");
			descTreeNode.setType("UNKNOWN");
			descTreeNode.setConceptCode(descConcept.getConceptCode());
			descTreeNode.setVisualAttributes(new String[] { "FOLDER", "ACTIVE" });
//			rootTreeNode.setChildren(addElement(rootTreeNode.getChildren(), descTreeNode)); // keep at[] array. -> shout be arraylist
			rootTreeNode.getChildren().add(descTreeNode);

//			buildTreeRootForConcept(descConcept, descTreeNode);		// V1 was for descend from root 
			// V2 keep track of all concept used
			usedConcepts.put(descConcept, descTreeNode);
			

		}
		return rootTreeNode;
	}
	
	// keep childen as Array []
	static TreeNode[] addElement(TreeNode[] a, TreeNode e) {
	    a  = Arrays.copyOf(a, a.length + 1);
	    a[a.length - 1] = e;
	    return a;
	}
	private Concept getConcept(String code)
	{
		return ObjectSelect.query(Concept.class).where(Concept.CONCEPT_CODE.eq(code)).selectOne(newObjectContext);
	}

}
