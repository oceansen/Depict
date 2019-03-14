package no.simula.depict.model;

import javax.xml.bind.annotation.XmlAttribute;

public class CteClass 
{
	private String id;
	private String name;
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
		return "\nCteClass [id=" + id + ", name=" + name + "]";
	}
}
