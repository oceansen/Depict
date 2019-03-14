package no.simula.depict.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import no.simula.depict.data.TableRelationship;

public class DepictEdge implements Serializable
{
	private static final long serialVersionUID = -8562557636863530771L;
	private static final String R_ID = "%s.{%s} -> %s.{%s}";
	private static final String R_NAME = "[%s] -> [%s]";
	private static final String R_SHORT_NAME = "%s -> %s";
	
	private String id;
	private String schemaName;
	private String foreignTableName;
	private String foreignTableAlias;
	private String primaryTableName;
	private String primaryTableAlias;
	private List<String> foreignKeyColumns = new ArrayList<String>();
	private List<String> primaryKeyColumns = new ArrayList<String>();
	private boolean selected;
	private boolean surrogate;
	private boolean visited;
	private boolean recursiveRelationship;

	public DepictEdge()
	{
		
	}
	
	public DepictEdge(DepictEdge e) 
	{
		this.id = e.getId();
		this.schemaName = e.getSchemaName();
		this.foreignTableName = e.getForeignTableName();
		this.primaryTableName = e.getPrimaryTableName();
		this.foreignKeyColumns.addAll(e.getForeignKeyColumns());
		this.primaryKeyColumns.addAll(e.getPrimaryKeyColumns());
		this.recursiveRelationship = e.isRecursiveRelationship();
		this.surrogate = e.isSurrogate();
	}
	
	public DepictEdge(TableRelationship r)
	{
		this.schemaName = r.getSchemaName();
		this.primaryTableName = r.getPrimaryTable().getName();
		this.foreignTableName = r.getForeignTable().getName();
		
		for (String c : r.getForeignKeyColumns())
			foreignKeyColumns.add(c);

		for (String s : r.getPrimaryKeyColumns())
			primaryKeyColumns.add(s);

		this.id = String.format(R_ID, foreignTableName, foreignKeyColumns, primaryTableName, primaryKeyColumns);
	}

	@Override
	public String toString() {
		return "DepictEdge [id=" + id + "]";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getForeignTableName() {
		return foreignTableName;
	}

	public void setForeignTableName(String foreignTableName) {
		this.foreignTableName = foreignTableName;
	}

	public String getPrimaryTableName() {
		return primaryTableName;
	}

	public void setPrimaryTableName(String primaryTableName) {
		this.primaryTableName = primaryTableName;
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
		DepictEdge other = (DepictEdge) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public List<String> getForeignKeyColumns() {
		return foreignKeyColumns;
	}

	public List<String> getPrimaryKeyColumns() {
		return primaryKeyColumns;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public String getName()
	{
		String fks = "";
		for (String s : foreignKeyColumns)
		{
			if (fks.length() != 0)
				fks += ", " + s;
			else
				fks = s;
		}
		
		String pks = "";
		for (String s : primaryKeyColumns)
		{
			if (pks.length() != 0)
				pks += ", " + s;
			else
				pks = s;
		}

		return String.format(R_NAME, fks, pks);
	}

	public boolean isSurrogate() {
		return surrogate;
	}

	public void setSurrogate(boolean surrogate) {
		this.surrogate = surrogate;
	}
	
	public String getShortName()
	{
		return String.format(R_SHORT_NAME, foreignTableName, primaryTableName);
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

	public boolean isRecursiveRelationship() {
		return recursiveRelationship;
	}

	public void setRecursiveRelationship(boolean recursiveRelationship) {
		this.recursiveRelationship = recursiveRelationship;
	}

	public String getForeignTableAlias() {
		return foreignTableAlias;
	}

	public void setForeignTableAlias(String foreignTableAlias) {
		this.foreignTableAlias = foreignTableAlias;
	}

	public String getPrimaryTableAlias() {
		return primaryTableAlias;
	}

	public void setPrimaryTableAlias(String primaryTableAlias) {
		this.primaryTableAlias = primaryTableAlias;
	}
}
