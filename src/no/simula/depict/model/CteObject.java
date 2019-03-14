package no.simula.depict.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.io.ByteOrderMark;

@XmlRootElement(name="TestonaObject")
@XmlType(propOrder = {"id", "tree", "testGroup", "tag"})
public class CteObject 
{
	private String id;
	private CteTree cteTree;
	private CteTestGroup testGroup;
	private CteTag tag;
	private boolean schemaMissing;
	private String catalogName;
	private String schemaName;
	private String modelName;
	private String binaryModelFilePath;
	private ByteOrderMark bom;
	
	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlElement(name="Tree")
	public CteTree getTree() {
		return cteTree;
	}

	public void setTree(CteTree tree) {
		this.cteTree = tree;
	}

	@XmlElement(name="TestGroup")
	public CteTestGroup getTestGroup() {
		return testGroup;
	}

	public void setTestGroup(CteTestGroup testGroup) {
		this.testGroup = testGroup;
	}

	@XmlElement(name="Tag")
	public CteTag getTag() {
		return tag;
	}

	public void setTag(CteTag tag) {
		this.tag = tag;
	}

	@XmlTransient
	public boolean isSchemaMissing() {
		return schemaMissing;
	}

	public void setSchemaMissing(boolean schemaMissing) {
		this.schemaMissing = schemaMissing;
	}
	
	@XmlTransient
	public String getCatalogName() {
		return catalogName;
	}

	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}

	@XmlTransient
	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	@XmlTransient
	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	
	@XmlTransient
	public String getBinaryModelFilePath() {
		return binaryModelFilePath;
	}

	public void setBinaryModelFilePath(String binaryModelFilePath) {
		this.binaryModelFilePath = binaryModelFilePath;
	}

	/*
	public static CteComposition getCatalog(CteObject cteo)
	{
		return cteo.getTree().getComposition(); 
	}

	//
	// Note: Detect if the CTE XL model has a definition for the hierarchy catalog/schema/table or simply catalog/table
	public static CteComposition getSchema(CteComposition catalog)
	{
		CteComposition cc = catalog.getCompositions().get(0);
		
		//
		// If none level of composition exists under the one under the catalog, this means that no schema exists.
		if (cc.getCompositions() == null) 
			return null;

		return cc; //catalog.getCompositions().get(0);
	}
	*/
	
	@XmlTransient
	public ByteOrderMark getBom() {
		return bom;
	}

	public void setBom(ByteOrderMark bom) {
		this.bom = bom;
	}

	@Override
	public String toString() {
		return "CteObject [id=" + id + ", cteTree=" + cteTree + ", testGroup="
				+ testGroup + "]";
	}
}
