package test.cayenne.OMOP.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;
import java.util.List;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;

import test.cayenne.OMOP.ConceptAncestor;

/**
 * Class _Concept was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Concept extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String CONCEPT_ID_PK_COLUMN = "concept_id";

    public static final Property<String> CONCEPT_CLASS_ID = Property.create("conceptClassId", String.class);
    public static final Property<String> CONCEPT_CODE = Property.create("conceptCode", String.class);
    public static final Property<Long> CONCEPT_ID = Property.create("conceptId", Long.class);
    public static final Property<String> CONCEPT_NAME = Property.create("conceptName", String.class);
    public static final Property<String> DOMAIN_ID = Property.create("domainId", String.class);
    public static final Property<String> INVALID_REASON = Property.create("invalidReason", String.class);
    public static final Property<String> STANDARD_CONCEPT = Property.create("standardConcept", String.class);
    public static final Property<Timestamp> VALID_END_DATE = Property.create("validEndDate", Timestamp.class);
    public static final Property<Timestamp> VALID_START_DATE = Property.create("validStartDate", Timestamp.class);
    public static final Property<String> VOCABULARY_ID = Property.create("vocabularyId", String.class);
    public static final Property<List<ConceptAncestor>> ANCESTOR_CONCEPTS = Property.create("ancestorConcepts", List.class);

    protected String conceptClassId;
    protected String conceptCode;
    protected Long conceptId;
    protected String conceptName;
    protected String domainId;
    protected String invalidReason;
    protected String standardConcept;
    protected Timestamp validEndDate;
    protected Timestamp validStartDate;
    protected String vocabularyId;

    protected Object ancestorConcepts;

    public void setConceptClassId(String conceptClassId) {
        beforePropertyWrite("conceptClassId", this.conceptClassId, conceptClassId);
        this.conceptClassId = conceptClassId;
    }

    public String getConceptClassId() {
        beforePropertyRead("conceptClassId");
        return this.conceptClassId;
    }

    public void setConceptCode(String conceptCode) {
        beforePropertyWrite("conceptCode", this.conceptCode, conceptCode);
        this.conceptCode = conceptCode;
    }

    public String getConceptCode() {
        beforePropertyRead("conceptCode");
        return this.conceptCode;
    }

    public void setConceptId(Long conceptId) {
        beforePropertyWrite("conceptId", this.conceptId, conceptId);
        this.conceptId = conceptId;
    }

    public Long getConceptId() {
        beforePropertyRead("conceptId");
        return this.conceptId;
    }

    public void setConceptName(String conceptName) {
        beforePropertyWrite("conceptName", this.conceptName, conceptName);
        this.conceptName = conceptName;
    }

    public String getConceptName() {
        beforePropertyRead("conceptName");
        return this.conceptName;
    }

    public void setDomainId(String domainId) {
        beforePropertyWrite("domainId", this.domainId, domainId);
        this.domainId = domainId;
    }

    public String getDomainId() {
        beforePropertyRead("domainId");
        return this.domainId;
    }

    public void setInvalidReason(String invalidReason) {
        beforePropertyWrite("invalidReason", this.invalidReason, invalidReason);
        this.invalidReason = invalidReason;
    }

    public String getInvalidReason() {
        beforePropertyRead("invalidReason");
        return this.invalidReason;
    }

    public void setStandardConcept(String standardConcept) {
        beforePropertyWrite("standardConcept", this.standardConcept, standardConcept);
        this.standardConcept = standardConcept;
    }

    public String getStandardConcept() {
        beforePropertyRead("standardConcept");
        return this.standardConcept;
    }

    public void setValidEndDate(Timestamp validEndDate) {
        beforePropertyWrite("validEndDate", this.validEndDate, validEndDate);
        this.validEndDate = validEndDate;
    }

    public Timestamp getValidEndDate() {
        beforePropertyRead("validEndDate");
        return this.validEndDate;
    }

    public void setValidStartDate(Timestamp validStartDate) {
        beforePropertyWrite("validStartDate", this.validStartDate, validStartDate);
        this.validStartDate = validStartDate;
    }

    public Timestamp getValidStartDate() {
        beforePropertyRead("validStartDate");
        return this.validStartDate;
    }

    public void setVocabularyId(String vocabularyId) {
        beforePropertyWrite("vocabularyId", this.vocabularyId, vocabularyId);
        this.vocabularyId = vocabularyId;
    }

    public String getVocabularyId() {
        beforePropertyRead("vocabularyId");
        return this.vocabularyId;
    }

    public void addToAncestorConcepts(ConceptAncestor obj) {
        addToManyTarget("ancestorConcepts", obj, true);
    }

    public void removeFromAncestorConcepts(ConceptAncestor obj) {
        removeToManyTarget("ancestorConcepts", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<ConceptAncestor> getAncestorConcepts() {
        return (List<ConceptAncestor>)readProperty("ancestorConcepts");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "conceptClassId":
                return this.conceptClassId;
            case "conceptCode":
                return this.conceptCode;
            case "conceptId":
                return this.conceptId;
            case "conceptName":
                return this.conceptName;
            case "domainId":
                return this.domainId;
            case "invalidReason":
                return this.invalidReason;
            case "standardConcept":
                return this.standardConcept;
            case "validEndDate":
                return this.validEndDate;
            case "validStartDate":
                return this.validStartDate;
            case "vocabularyId":
                return this.vocabularyId;
            case "ancestorConcepts":
                return this.ancestorConcepts;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "conceptClassId":
                this.conceptClassId = (String)val;
                break;
            case "conceptCode":
                this.conceptCode = (String)val;
                break;
            case "conceptId":
                this.conceptId = (Long)val;
                break;
            case "conceptName":
                this.conceptName = (String)val;
                break;
            case "domainId":
                this.domainId = (String)val;
                break;
            case "invalidReason":
                this.invalidReason = (String)val;
                break;
            case "standardConcept":
                this.standardConcept = (String)val;
                break;
            case "validEndDate":
                this.validEndDate = (Timestamp)val;
                break;
            case "validStartDate":
                this.validStartDate = (Timestamp)val;
                break;
            case "vocabularyId":
                this.vocabularyId = (String)val;
                break;
            case "ancestorConcepts":
                this.ancestorConcepts = val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.conceptClassId);
        out.writeObject(this.conceptCode);
        out.writeObject(this.conceptId);
        out.writeObject(this.conceptName);
        out.writeObject(this.domainId);
        out.writeObject(this.invalidReason);
        out.writeObject(this.standardConcept);
        out.writeObject(this.validEndDate);
        out.writeObject(this.validStartDate);
        out.writeObject(this.vocabularyId);
        out.writeObject(this.ancestorConcepts);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.conceptClassId = (String)in.readObject();
        this.conceptCode = (String)in.readObject();
        this.conceptId = (Long)in.readObject();
        this.conceptName = (String)in.readObject();
        this.domainId = (String)in.readObject();
        this.invalidReason = (String)in.readObject();
        this.standardConcept = (String)in.readObject();
        this.validEndDate = (Timestamp)in.readObject();
        this.validStartDate = (Timestamp)in.readObject();
        this.vocabularyId = (String)in.readObject();
        this.ancestorConcepts = in.readObject();
    }

}
