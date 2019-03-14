package no.simula.depict.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TableMetadata implements Serializable
{
	private static final long serialVersionUID = -4892674718673652880L;
	private String catalog;
	private String schema;
	private String name;
	private String id;
	
	private List<TableRelationship> relationships;
	private List<TableColumnMetadata> columns;
	
	public TableMetadata(String id)
	{
		this.id = id;
	}
	
	public TableMetadata(String catalog, String schema, String name)
	{
		this.catalog = catalog;
		this.schema = schema;
		this.name = name;
		relationships = new ArrayList<TableRelationship>();
		columns = new ArrayList<TableColumnMetadata>();

		if (schema != null)
			id = schema + "." + name;
		else
			id = name;
	}

	public String getCatalog() {
		return catalog;
	}
	
	public String getSchema() {
		return schema;
	}

	public String getName() {
		return name;
	}

	public List<TableColumnMetadata> getColumns() {
		if (columns == null)
			columns = new ArrayList<TableColumnMetadata>();
		return columns;
	}

	public List<TableRelationship> getRelationships() {
		return relationships;
	}

	public String getId() 
	{
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
		TableMetadata other = (TableMetadata) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TableMetadata [name=" + name + "]";
	}
}
