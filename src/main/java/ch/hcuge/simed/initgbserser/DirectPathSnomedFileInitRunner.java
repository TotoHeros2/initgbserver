package ch.hcuge.simed.initgbserser;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.cayenne.DataRow;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.configuration.server.ServerRuntime;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.SQLSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import ch.hcuge.simed.initgbserser.bean.Concept;
import test.cayenne.OMOP.Measurement;
import test.cayenne.OMOP.Observation;

@Component
@Service
public class DirectPathSnomedFileInitRunner implements CommandLineRunner {
    private static Logger logger = LoggerFactory.getLogger(DirectPathSnomedFileInitRunner.class);
    
	@Autowired
	private ResourceLoader resourceLoader;
    
    private HashMap<Long, Concept> mapCodeConcept = new HashMap<Long, Concept>();
    private HashMap<Concept, List<Concept>> mapAscendingConcepts = new HashMap<Concept, List<Concept>>();
    
	final static private String ROOT_NAME = "\\"; 
	
//	@Value("${test}")
//	private boolean test = false;
	
	@Value("${ch.hcuge.simed.gbserver.concept.initfilepath}")
	private String initFilePath;
	
	@Value("${ch.hcuge.simed.gbserver.concept.snomedRootCode}")
	private String snomedRootCode;
	
	@Value("${ch.hcuge.simed.gbserver.concept.snomedRootDescendantCodes}")
	private String snomedRootDescendantCodes;
	
	private ObjectContext newObjectContext = null;
	
    // V3 only root branche
	HashMap<Concept, TreeNode> usedConcepts = null;
	
// leafConcept = null;
	
	TreeNode leafTreNode = null;
	
//	String branche = "";
   

	@Override
	public void run(String... args) throws Exception {
		initConcept();
//		System.err.println(mapCodeConcept);
		initAscendingConcepts();

		
		ServerRuntime cayenneRuntime = ServerRuntime.builder().addConfig("cayenne-SNOMED.xml").build();
	    newObjectContext = cayenneRuntime.newContext();	
	    List<String> branches = Arrays.asList("Observations","Measurements");
	    
	    for (String branche : branches)
	    {
	    	usedConcepts = new HashMap<Concept, TreeNode>();
	    	logger.info("Will try to generate ontology for " + branche);
	    	List<Concept> instanciatedConcepts = getTypedConcept(branche);
	
	    	makeSnomedRoots(branche);	
	    	setPathFromInstanciatedConceptsToRoot(instanciatedConcepts,branche.toLowerCase()); // V2 beast

	    	// 01.02.22 remove branches not in used under SNOMED CT
	    	//	    cleanSnomedCtBranches(snomedRootM);

	    	TreeNode measurements = new TreeNode();
	    	measurements.setName(branche);// SNOMED CT
	    	measurements.setStudyId("TEST");
	    	measurements.setFullName("\\" + branche + "\\");
	    	measurements.setType("UNKNOWN");
	    	measurements.setVisualAttributes(new String[] { "FOLDER", "ACTIVE" });
	    	//	    measurements.getChildren().add(snomedRootM);


	    	int i = 0;
	    	TreeNode aLeafTreeNode = null;
	    	ArrayList<TreeNode> children = new ArrayList<TreeNode>();
	    	for (Map.Entry<Concept, TreeNode> entry: usedConcepts.entrySet())
	    	{
	    		//            key[i+] = entry.getKey();
	    		aLeafTreeNode = entry.getValue();
	    		if (aLeafTreeNode.getChildren().size() > 0)
	    		{
		    		eliminateDoublons(aLeafTreeNode);
	    			children.add(aLeafTreeNode);
	    		}
	    	}
	    	measurements.setChildren(children);
	    	setFullName(measurements,ROOT_NAME);
	    	// order 1st level
	    	Collections.sort(children, new TreeNodeComparator());
	    	// 2nd level
	    	for (TreeNode treeNode : measurements.getChildren())
	    	{
	    		children = treeNode.getChildren();
		    	Collections.sort(children, new TreeNodeComparator());
		    	treeNode.setChildren(children);
	    	}
	    	saveToFile(measurements,branche.toLowerCase());
	    }
		System.exit(0);
	}


	private List<Concept> getTypedConcept(String branche) {
		int dateNb = 0;
		int charNb = 0;
		int numberNb = 0;
		List<Long> ids = null;
		switch (branche) {
		case "Observations":
			ids = ObjectSelect.columnQuery(Observation.class, Observation.OBSERVATION_CONCEPT_ID).distinct().select(newObjectContext);
			break;
		case "Measurements":
			ids = ObjectSelect.columnQuery(Measurement.class, Measurement.MEASUREMENT_CONCEPT_ID).distinct().select(newObjectContext);
			break;
		default:
			break;
		}
		
		List<Concept> concepts = new ArrayList<Concept>();
//		List<Observation> observations = null;
		NodeType type = null;
		for (Long id:ids)
		{
			test.cayenne.OMOP.Concept conceptDB =  ObjectSelect.query(test.cayenne.OMOP.Concept.class).where(test.cayenne.OMOP.Concept.CONCEPT_ID.eq(id)).selectOne(newObjectContext);
			// pegn 07.06.22 conceptCode = "364075005+162986007"
			String conceptCodes[] = conceptDB.getConceptCode().split("\\+");
			String conceptCode = null;
			if (conceptCodes.length == 0)
			{
				continue;
			}

			conceptCode = conceptCodes[0];
			Concept concept = mapCodeConcept.get(Long.valueOf(conceptCode));
			switch (branche) {
			case "Observations":
				numberNb = nbOfObservationAsNumForConceptId(id);
				charNb = nbOfObservationAsCharForConceptId(id);
				dateNb = nbOfObservationAsDateForConceptId(id);
				break;
			case "Measurements":
				numberNb = nbOfMeasurementAsNumForConceptId(id);
				charNb = nbOfObMeasurementAsCharForConceptId(id);
				// no date for measurement
				break;
			default:
				break;
			}

			if (charNb  >= dateNb && charNb >= numberNb)
			{
				type = NodeType.STRING;				
			}
			else 
			if (numberNb >= charNb && numberNb >= dateNb)
			{
				type = NodeType.NUM;
				
			}
			else
			{
				type = NodeType.DATE;				
			}				
			
			if (concept != null)
			{	
				concept.setType(type);
				concepts.add(concept);
			}
		}
		return concepts;		
	}




	private void eliminateDoublons(TreeNode aLeafTreeNode) {
		ArrayList<TreeNode> childen = aLeafTreeNode.getChildren();
        ArrayList<TreeNode> newchilden = new ArrayList<TreeNode>();
        for (TreeNode aLeaf:childen)
        {
        	if (!newchilden.contains(aLeaf))
        		newchilden.add(aLeaf);
        }
        aLeafTreeNode.setChildren(newchilden);
	}


	private void initAscendingConcepts() {
		Resource resource = resourceLoader.getResource("classpath:sct_relations_complete.csv");// work with resources in IDE. Should work in Tomcat (app/WEB-/classes) ?
		try {
			File file = resource.getFile();
		    Scanner sc = new Scanner(file);
		    sc.useDelimiter(",");
		    //setting comma as delimiter pattern
		    while (sc.hasNextLine()) {
		    	String[] csvData =  sc.nextLine().split(sc.delimiter().toString());
		    	if (!csvData[0].equalsIgnoreCase("sourceId") && csvData[0].length() > 0)
		    	{
		    		try {
		    			Concept source = mapCodeConcept.get(Long.valueOf(csvData[0]));
		    			Concept destination = mapCodeConcept.get(Long.valueOf(csvData[1]));
		    			if (mapAscendingConcepts.containsKey(source))
		    			{
		    				mapAscendingConcepts.get(source).add(destination);
		    			}
		    			else
		    			{
		    				ArrayList<Concept> newArray = new ArrayList<Concept>();
		    				newArray.add(destination);
		    				mapAscendingConcepts.put(source, newArray);
		    			}
		    		}
		    		catch (NumberFormatException numEx) {
		    			System.err.println("can not use int for : " + csvData[0] + "/ " + csvData[1]);
		    		}
		    	}
//		      System.out.println(csvData);
		    }
		    sc.close();
		    //closes the scanner  
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private void initConcept() {
		Resource resource = resourceLoader.getResource("classpath:sct_concepts_complete.csv");// work with resources in IDE. Should work in Tomcat (app/WEB-/classes) ?
		try {
			File file = resource.getFile();
		    Scanner sc = new Scanner(file);
		    sc.useDelimiter(",");
		    //setting comma as delimiter pattern
		    while (sc.hasNextLine()) {
		    	String[] csvData =  sc.nextLine().split(sc.delimiter().toString());
		    	if (!csvData[0].equalsIgnoreCase("id") && csvData[0].length() > 0)
		    	{
		    		try {
		    		Long code = Long.valueOf(csvData[0]);
		    		Concept concept = new Concept(code, csvData[1]);
		    		mapCodeConcept.put(code, concept);
		    		}
		    		catch (NumberFormatException numEx) {
		    			System.err.println("can not use int for : " + csvData[0] + "/ " + csvData[1]);
		    		}
		    	}
//		      System.out.println(csvData);
		    }
		    sc.close();
		    //closes the scanner  
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
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
			test.cayenne.OMOP.Concept conceptDB =  ObjectSelect.query(test.cayenne.OMOP.Concept.class).where(test.cayenne.OMOP.Concept.CONCEPT_ID.eq(id)).selectOne(newObjectContext);
			Concept concept = mapCodeConcept.get(Long.valueOf(conceptDB.getConceptCode()));
			if (concept != null)
			{			
//				typeForConcepts.put(concept, value);
				concepts.add(concept);
			}
		}
		return concepts;
	}	
	

	private void makeSnomedRoots(String branche) {
		
			
		
		String[] codes = snomedRootDescendantCodes.split(";");
		
		for (String code : codes)
		{
			Concept descConcept = getConcept(code);
			TreeNode descTreeNode = new TreeNode();
			descTreeNode.setName(descConcept.getConceptName());
			descTreeNode.setStudyId("TEST");
			descTreeNode.setFullName("\\" + branche + "\\ (SNOMED - " + descConcept.getConceptName() + ")\\");
			descTreeNode.setType("UNKNOWN");
			descTreeNode.setConceptCode(descConcept.getConceptCode().toString());
			descTreeNode.setVisualAttributes(new String[] { "FOLDER", "ACTIVE" });
//			rootTreeNode.setChildren(addElement(rootTreeNode.getChildren(), descTreeNode)); // keep at[] array. -> shout be arraylist
//			rootTreeNode.getChildren().add(descTreeNode);

//			buildTreeRootForConcept(descConcept, descTreeNode);		// V1 was for descend from root 
			// V2 keep track of all concept used
			usedConcepts.put(descConcept, descTreeNode);
			

		}
//		return rootTreeNode;
	}


	private Concept getConcept(String code) {
		return mapCodeConcept.get(Long.valueOf(code));
	}
	private void setPathFromInstanciatedConceptsToRoot(List<Concept> instanciatedConcepts,String branche) {
		String prefixName = branche.substring(0,1).toUpperCase() + "-";
		String singularBranche = branche.toLowerCase().substring(0,branche.length() - 1);
		// V2 
		for (Concept concept: instanciatedConcepts)
		{
//			this.leafConcept = concept; // the leaf in treatment
			// 1st step create treenode
			TreeNode treeNode = new TreeNode();
			treeNode.setName(concept.getConceptName() + " (" + /*prefixName + */  concept.getConceptCode() + ")");
			treeNode.setStudyId("TEST");
//			treeNode.setFullName(treeNodeUpLevel.getFullName() + concept.getConceptName() + "\\");
//			System.err.println(treeNode.getFullName());
//			treeNode.setType("NUMERIC");// NUMERICAL /UNKNOWN / CATEGORICAL
//			treeNode.setVisualAttributes(new String[] { "LEAF", "ACTIVE", "NUMERICAL" });
			
			if (concept.getType() == NodeType.STRING)
			{
				treeNode.setType("CATEGORICAL");// NUMERICAL /UNKNOWN / CATEGORICAL
				treeNode.setVisualAttributes(new String[] { "LEAF", "ACTIVE", "CATEGORICAL" });
			}
			else if (concept.getType() == NodeType.NUM)
			{
				treeNode.setType("NUMERIC");// NUMERICAL /UNKNOWN / CATEGORICAL
				treeNode.setVisualAttributes(new String[] { "LEAF", "ACTIVE", "NUMERICAL" });
			}
			else
			{
				treeNode.setType("DATE");
			}
			
			treeNode.setConceptCode(prefixName + concept.getConceptCode());

			
			Metadata metadata = new Metadata();
			metadata.setSubjectDimension(singularBranche);
			treeNode.setMetadata(metadata);
			
			
			
			// keep It 
			this.leafTreNode = treeNode;
			
			
			// go up until you find an used concept
//			if (!test)
			buildTreeRootForAscendingConcept(concept);
		}
	}
	

	private void buildTreeRootForAscendingConcept(Concept conceptDownLevel) {
		List<Concept> ascendants = getDirectAscendants(conceptDownLevel); // to find with min = 1
		for (Concept concept:ascendants)
		{
			if (usedConcepts.get(concept) == null)
			{
				// go down next level
				buildTreeRootForAscendingConcept(concept);
			}
			else 
			{

				// add treenode to Level
				// no more needed for intermediaire concept
				usedConcepts.get(concept).getChildren().add(this.leafTreNode);
//				usedConcepts.put(concept, treeNode);

			}
		}

}


	private List<Concept> getDirectAscendants(Concept conceptDownLevel) {
		// TODO Auto-generated method stub
		return mapAscendingConcepts.get(conceptDownLevel);
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

	private int  nbOfObservationAsNumForConceptId(Long id)
	{
		String sql = "SELECT count(*) FROM observation t0 WHERE t0.value_as_number is not null and t0.observation_concept_id = $id";
		DataRow ww = SQLSelect.dataRowQuery(sql).params("id", id).selectOne(newObjectContext);
		int nb = ((Number) ww.get("count()")).intValue();
		return nb;
	}
	

	private int nbOfObservationAsCharForConceptId(Long id) {
		String sql = "SELECT count(*) FROM observation t0 WHERE t0.value_as_number is null and t0.observation_concept_id = $id  and t0.value_as_string not like '%-%-% %:%:%'";
		DataRow ww = SQLSelect.dataRowQuery(sql).params("id", id).selectOne(newObjectContext);
		int nb = ((Number) ww.get("count()")).intValue();
		return nb;
	}
	private int nbOfObservationAsDateForConceptId(Long id) {
		String sqlDate = "SELECT count(*) FROM observation t0 WHERE t0.value_as_number is null and t0.observation_concept_id = $id  and t0.value_as_string like '%-%-% %:%:%' and LENGTH(t0.value_as_string) = 19";
		DataRow wwDate = SQLSelect.dataRowQuery(sqlDate).params("id", id).selectOne(newObjectContext);
		int nbDate = ((Number) wwDate.get("count()")).intValue();
		
		String sqlTime = "SELECT count(*) FROM observation t0 WHERE t0.value_as_number is null and t0.observation_concept_id = $id  and t0.value_as_string like '%-%-%' and LENGTH(t0.value_as_string) = 10";
		DataRow wwTime = SQLSelect.dataRowQuery(sqlTime).params("id", id).selectOne(newObjectContext);
		int nbTime = ((Number) wwTime.get("count()")).intValue();		
		
		return nbDate + nbTime;
	}	
	
	private int  nbOfMeasurementAsNumForConceptId(Long id)
	{
		String sql = "SELECT count(*) FROM measurement t0 WHERE t0.value_as_number is not null and t0.measurement_concept_id = $id";
		DataRow ww = SQLSelect.dataRowQuery(sql).params("id", id).selectOne(newObjectContext);
		int nb = ((Number) ww.get("count()")).intValue();
		return nb;
	}
	

	private int nbOfObMeasurementAsCharForConceptId(Long id) {
		String sql = "SELECT count(*) FROM measurement t0 WHERE t0.value_as_number is null and t0.measurement_concept_id = $id";
		DataRow ww = SQLSelect.dataRowQuery(sql).params("id", id).selectOne(newObjectContext);
		int nb = ((Number) ww.get("count()")).intValue();
		return nb;
	}
	
	
}
