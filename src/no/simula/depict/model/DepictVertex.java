package no.simula.depict.model;

import java.io.Serializable;
import java.util.List;
import no.simula.depict.data.*;

public class DepictVertex implements Serializable
{
	private static final long serialVersionUID = -5671151653852492305L;
	private String id;
	private String name;
	private boolean outputVertex;
	private List<String> primaryKeyFields;
	private boolean recursiveRelationship;
	private boolean visited;
	private String schemaName;
	
	public DepictVertex(String id, String name, String schemaName)
	{
		this.id = id;
		this.name = name;
		this.schemaName = schemaName;
	}
	
	public DepictVertex(TableMetadata tbmd)
	{
		this.id = tbmd.getId();
		this.name = tbmd.getName();
		this.schemaName = tbmd.getSchema();
	}

	@Override
	public String toString() {
		return "DepictVertex [name=" + name + ", outputVertex=" + outputVertex
				+ "]";
	}

	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		DepictVertex other = (DepictVertex) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public boolean isOutputVertex() {
		return outputVertex;
	}

	public void setOutputVertex(boolean outputVertex) {
		this.outputVertex = outputVertex;
	}

	public List<String> getPrimaryKeyFields() {
		return primaryKeyFields;
	}

	public void setPrimaryKeyFields(List<String> primaryKeyFields) {
		this.primaryKeyFields = primaryKeyFields;
	}

	public boolean isRecursiveRelationship() {
		return recursiveRelationship;
	}

	public void setRecursiveRelationship(boolean recursiveRelationship) {
		this.recursiveRelationship = recursiveRelationship;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
}
