package no.simula.depict.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

public class CteTestCase implements Serializable
{
	private static final long serialVersionUID = 6940697840631815803L;
	private String id;
	private String name;
	private String fullName;
	private CteMarks marks;
	
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
	
	@XmlTransient
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	@XmlElement(name="Marks")
	public CteMarks getMarks() {
		return marks;
	}
	public void setMarks(CteMarks marks) {
		this.marks = marks;
	}
	@Override
	public String toString() {
		return "CteTestCase [id=" + id + ", name=" + name + ", marks=" + marks
				+ "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
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
		CteTestCase other = (CteTestCase) obj;
		if (fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!fullName.equals(other.fullName))
			return false;
		return true;
	}
}
