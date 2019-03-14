package no.simula.depict.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class CteTree 
{
	private String id;
	private String name;
	private String root;
	private CteComposition composition;
	
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
	
	@XmlAttribute
	public String getRoot() {
		return root;
	}
	public void setRoot(String root) {
		this.root = root;
	}

	@XmlElement(name="Composition")
	public CteComposition getComposition() {
		return composition;
	}
	public void setComposition(CteComposition composition) {
		this.composition = composition;
	}
	
	@Override
	public String toString() {
		return "CteTree [id=" + id + ", name=" + name + ", root=" + root
				+ ", composition=" + composition + "]";
	}
}
