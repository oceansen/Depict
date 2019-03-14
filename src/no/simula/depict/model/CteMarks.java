package no.simula.depict.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

public class CteMarks implements Serializable
{
	private static final long serialVersionUID = -1490801217276631962L;
	//
	// Represents attribute 'true' containing a space separated list of the selected classes (actual values of fields) for the current test
	private String selectedClasses; 

	@XmlAttribute(name="true")
	public String getSelectedClasses() {
		return selectedClasses;
	}

	public void setSelectedClasses(String selectedClasses) {
		this.selectedClasses = selectedClasses;
	}

	@XmlTransient
	public String[] getSelectedClassesAsList()
	{
		return selectedClasses.split(" ");
	}
	
	@Override
	public String toString() {
		return "CteMarks [selectedClasses=" + selectedClasses + "]";
	}
}
