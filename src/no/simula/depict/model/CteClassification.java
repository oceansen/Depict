package no.simula.depict.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class CteClassification 
{
	private String id;
	private String name;
	@XmlElement(name="Class")
	private List<CteClass> classes;
	private int x;
	private int y;

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
	
	public List<CteClass> getClasses() {
		return classes;
	}
	public void setClasses(List<CteClass> classes) {
		this.classes = classes;
	}
	
	@XmlAttribute
	public int getX() {
		return x;
	}
	public void setX(int x) {
		this.x = x;
	}
	
	@XmlAttribute
	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public String toString() {
		return "\nCteClassification [id=" + id + ", name=" + name + ", classes="
				+ classes + "]";
	}
}
