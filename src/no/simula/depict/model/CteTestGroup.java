package no.simula.depict.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class CteTestGroup 
{
	private String id;
	private String name;
	@XmlElement(name="TestGroup")
	private List<CteTestGroup> testGroups; 
	@XmlElement(name="TestCase")
	private List<CteTestCase> testCases; 
	
	@XmlAttribute
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<CteTestGroup> getTestGroups() {
		return testGroups;
	}
	public void setTestGroups(List<CteTestGroup> testGroups) {
		this.testGroups = testGroups;
	}
	public List<CteTestCase> getTestCases() {
		return testCases;
	}
	public void setTestCases(List<CteTestCase> testCases) {
		this.testCases = testCases;
	}
	@Override
	public String toString() {
		return "CteTestGroup [id=" + id + ", name=" + name + ", testGroups="
				+ testGroups + ", testCases=" + testCases + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CteTestGroup other = (CteTestGroup) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
