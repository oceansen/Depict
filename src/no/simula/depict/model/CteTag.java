package no.simula.depict.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

public class CteTag 
{
	private String id;
	private String type;
	@XmlElement(name="Content")
	private List<CteContent> contents;

	@XmlAttribute
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<CteContent> getContents() {
		return contents;
	}
	public void setContents(List<CteContent> contents) {
		this.contents = contents;
	}

	@Override
	public String toString() {
		return "CteTag [id=" + id + ", type=" + type + ", contents=" + contents
				+ "]";
	}
	
	public CteContent findByKey(String key)
	{
		CteContent cc = new CteContent();
		cc.setKey(key);
		
		int idx;
		if ((idx = getContents().indexOf(cc)) != -1)
			return getContents().get(idx);
		
		return null;
	}
}
