package no.simula.depict.data;

import java.io.Serializable;

public class TableColumnMetadata implements Serializable
{
	private static final long serialVersionUID = -6068180687961677532L;
	private String name;
	private boolean primaryKey;
	private int sqlDataType;
	private String sqlTypeName;
	private String tableName;
	
	public TableColumnMetadata(String name)
	{
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
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
		TableColumnMetadata other = (TableColumnMetadata) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public int getSqlDataType() {
		return sqlDataType;
	}

	public void setSqlDataType(int sqlDataType) {
		this.sqlDataType = sqlDataType;
	}

	@Override
	public String toString() {
		return String.format("%s (%s)", name, sqlTypeName);
	}

	public String getSqlTypeName() {
		return sqlTypeName;
	}

	public void setSqlTypeName(String sqlTypeName) {
		this.sqlTypeName = sqlTypeName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
