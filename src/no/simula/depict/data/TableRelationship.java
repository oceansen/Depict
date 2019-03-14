package no.simula.depict.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TableRelationship implements Serializable
{
	private static final long serialVersionUID = 4432480301365753427L;
	private String catalogName;
	private String schemaName;
	private String name;
	TableMetadata primaryTable;
	TableMetadata foreignTable;
	private List<String> primaryKeyColumns;
	private List<String> foreignKeyColumns;
	
	public TableRelationship(String name, String catalogName, String schemaName, String primaryTableName, String foreignTableName)
	{
		if (name != null)
			this.name = name;
		else
			this.name = String.format("%s->%s", foreignTableName, primaryTableName);
		
		this.catalogName = catalogName;
		this.schemaName = schemaName;
		this.primaryTable = new TableMetadata(catalogName, schemaName, primaryTableName);
		this.foreignTable = new TableMetadata(catalogName, schemaName, foreignTableName);
		primaryKeyColumns = new ArrayList<String>();
		foreignKeyColumns = new ArrayList<String>();
	}

	public List<String> getPrimaryKeyColumns() {
		return primaryKeyColumns;
	}

	public List<String> getForeignKeyColumns() {
		return foreignKeyColumns;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public String getCatalogName() {
		return catalogName;
	}

	@Override
	public String toString() {
		return "TableRelationship [name=" + name + ", primaryTable="
				+ primaryTable + ", foreignTable=" + foreignTable + "]";
	}

	public TableMetadata getPrimaryTable() {
		return primaryTable;
	}

	public TableMetadata getForeignTable() {
		return foreignTable;
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
		TableRelationship other = (TableRelationship) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}
}
