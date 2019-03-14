package no.simula.depict.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class CteComposition 
{
	private String id;
	private String name;
	@XmlElement(name="Composition")
	private List<CteComposition> compositions;
	@XmlElement(name="Classification")
	private List<CteClassification> classifications;
	@XmlElement(name="Tag")
	private List<CteTag> tags;
	private String style;
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
	
	public List<CteComposition> getCompositions() {
		return compositions;
	}
	public void setCompositions(List<CteComposition> compositions) {
		this.compositions = compositions;
	}
	public List<CteClassification> getClassifications() {
		return classifications;
	}
	public void setClassifications(List<CteClassification> classifications) {
		this.classifications = classifications;
	}
	public List<CteTag> getTags() {
		return tags;
	}
	public void setTags(List<CteTag> tags) {
		this.tags = tags;
	}

	@XmlAttribute
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
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
		return "CteComposition [id=" + id + ", name=" + name
				+ ", compositions=" + compositions + ", classifications="
				+ classifications + ", tags=" + tags + "]";
	}
}
