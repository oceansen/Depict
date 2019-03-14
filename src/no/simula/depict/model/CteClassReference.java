package no.simula.depict.model;

import java.io.Serializable;

public class CteClassReference implements Serializable
{
	private static final long serialVersionUID = -6718799582845684475L;
	public static final String PRIMARY_SUFFIX = "1";
	public static final String SECONDARY_SUFFIX = "2";
	
	private String classId;
	private String fullyClassificationName;
	private String className;
	
	public String getFullyClassificationName() {
		return fullyClassificationName;
	}
	public void setFullyClassificationName(String fullyClassificationName) {
		this.fullyClassificationName = fullyClassificationName;
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	public String getClassId() {
		return classId;
	}
	public void setClassId(String classId) {
		this.classId = classId;
	}
	
	@Override
	public String toString() {
		return "CteClassReference [classId=" + classId
				+ ", fullyClassificationName=" + fullyClassificationName
				+ ", className=" + className + "]";
	}
	
	public static String addSuffixToTableName(String fullyClassificationName, String suffix)
	{
		String s[] = fullyClassificationName.split("\\.");
		if (s.length == 2)
			return String.format("%s%s.%s", s[0], suffix, s[1]);
		else if (s.length == 1)
			return String.format("%s%s", s[0], suffix);
			
		return null;
	}
}
