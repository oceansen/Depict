package no.simula.depict.data;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class DataManager 
{
	private static final int MAX_IDLE_POOLED_CONNECTION = 10;
	
	private Connection cn;
	private ConnectionFactory cf;
	private PreparedStatement st;
	private ResultSet rs;
	//private DataSource ds;
    private GenericObjectPool<ConnectionFactory> connectionPool = null;
	private int rowCount;
	private ResultSetMetaData rsmd;
	private List<String> columnNames;
	private CteConnectionInfo connectionInfo;
	private String latestSqlStatement;
	
	//public DataSource setup(CteConnectionInfo connectionInfo) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException
	public void setup(CteConnectionInfo connectionInfo) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException
	{
		this.connectionInfo = connectionInfo;
		//
        // Load JDBC Driver class.
		Class.forName(connectionInfo.getDriverClass()).newInstance();

        connectionPool = new GenericObjectPool<ConnectionFactory>(new PooledConnectionFactory(connectionInfo));
        connectionPool.setMaxIdle(MAX_IDLE_POOLED_CONNECTION);
	}
	
	public void open()
	{
		try 
		{
			cf = connectionPool.borrowObject();
			cn = cf.createConnection();

			// Auto commit to 'true' means that each SQL statement will be executed within an individual transaction 
			cn.setAutoCommit(true);
		} 
		catch (Exception e) 
		{
			cn = null;
			cf = null;
			e.printStackTrace();
		}

		rowCount = -1;
		latestSqlStatement = null;
	}
	
	public void close() throws SQLException
	{
		if (rs != null)
			rs.close();
		
		if (st != null)
			st.close();
		
		if (isOpened())
		{
			connectionPool.returnObject(cf);
			//cn.close();
		}		
		cn = null;
		st = null;
		rs = null;
		rowCount = 0;
		columnNames = null;
	}
	
	public boolean isOpened() throws SQLException
	{
		if (cn == null)
			return false;
		
		return cn.isClosed() ? false : true;
	}
	
	public ResultSet execute(String sqlExpression) throws SQLException
	{
		latestSqlStatement = sqlExpression;
		st = cn.prepareStatement(sqlExpression, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		System.out.println(sqlExpression);
		st.setFetchSize(100);
		rs = st.executeQuery();
		rsmd = rs.getMetaData();
		
		return rs;
	}
	
	public DatabaseInfo getDatabaseInfo() throws SQLException
	{
		if (cn == null)
			return null;
		
		DatabaseInfo db = new DatabaseInfo();
		java.sql.DatabaseMetaData dbmd = cn.getMetaData();
		db.setDriverName(dbmd.getDriverName());
		db.setDriverVersion(dbmd.getDriverVersion());
		db.setDriverMajorVersion(dbmd.getDriverMajorVersion());
		db.setDriverMinorVersion(dbmd.getDriverMinorVersion());
		db.setDatabaseProductName(dbmd.getDatabaseProductName());
		db.setDatabaseProductVersion(dbmd.getDatabaseProductVersion());
		
		db.setCatalogName(connectionInfo.getDbName());
		db.setUrl(connectionInfo.toString());
		return db;
	}
	
	public ResultSet getRow(int rowNumber) throws SQLException
	{
		rs.absolute(rowNumber);
		return rs;
	}

	public int getRowCount() throws SQLException 
	{
		if (rowCount != -1)
			return rowCount;
		
		String cnt = String.format("SELECT COUNT(*) CNT FROM (%s) AS CNT_TBL", latestSqlStatement);
		PreparedStatement stt = cn.prepareStatement(cnt, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rsCnt = stt.executeQuery();
		
		if (rsCnt.next())
			rowCount = rsCnt.getInt(1);
		else
			rowCount = 0;

		stt.close();
		rsCnt.close();
		return rowCount;
	}
	
	public int getColumnCount() throws SQLException
	{
		return rsmd.getColumnCount();
	}
	
	public String getColumnName(int columnNumber) throws SQLException
	{
		return rsmd.getColumnLabel(columnNumber);
	}
	
	public List<String> getColumnNames() throws SQLException
	{
		if (columnNames != null)
			return columnNames;
		
		columnNames = new ArrayList<String>();
		int n = getColumnCount();
		for (int i = 1; i <= n; ++i)
			columnNames.add(getColumnName(i));
		
		return columnNames;
	}
	
	public boolean databaseContainsCatalog(String catalog) throws SQLException
	{
		if (cn.getCatalog() == null)
			return true;
		
		return cn.getCatalog().equalsIgnoreCase(catalog);
	}
	
	public boolean databaseContainsSchema(String catalog, String schema) throws SQLException
	{
		DatabaseMetaData dbm = cn.getMetaData();
		ResultSet rs = dbm.getSchemas(catalog, schema);
		boolean r = rs.next();
		rs.close();
		return r;
	}
	
	public int databaseSchemaCount(String catalog) throws SQLException
	{
		DatabaseMetaData dbm = cn.getMetaData();
		ResultSet rs = dbm.getSchemas(catalog, null);
		int n = 0;
		while (rs.next())
			++n;
		
		rs.close();
		return n;
	}

	public boolean databaseContainsTable(String catalog, String schema, String table) throws SQLException
	{
		DatabaseMetaData dbm = cn.getMetaData();
		ResultSet rs = dbm.getTables(catalog, schema, table, null);
		boolean r = rs.next();
		rs.close();
		return r;
	}

	public List<TableColumnMetadata> getColumns(String catalog, String schema, String table) throws SQLException
	{
		//4. COLUMN_NAME String => column name 
		//5. DATA_TYPE int => SQL type from java.sql.Types 

		int COLUMN_NAME = 4;
		int DATA_TYPE = 5;
		int TYPE_NAME = 6;
		
		List<TableColumnMetadata> cols = new ArrayList<TableColumnMetadata>();
		
		DatabaseMetaData dbm = cn.getMetaData();
		ResultSet rs = dbm.getColumns(catalog, schema, table, null);
		while (rs.next())
		{
			TableColumnMetadata c = new TableColumnMetadata(rs.getString(COLUMN_NAME));
			c.setSqlDataType(rs.getInt(DATA_TYPE));
			c.setSqlTypeName(rs.getString(TYPE_NAME));
			cols.add(c);
		}		
		rs.close();
		return cols;
	}
	
	/**
	 * Retrieves a description of the tables available in the given catalog. Only table descriptions matching the catalog, schema and type "TABLE" criteria are returned. 
	 * They are ordered by TABLE_TYPE, TABLE_CAT, TABLE_SCHEM and TABLE_NAME.	 
	 * @param catalog
	 * @param schema
	 * @return
	 * @throws SQLException
	 */
	public List<TableMetadata> getTables(String catalog, String schema) throws SQLException
	{
		//
		// 1. TABLE_CAT String => table catalog (may be null) 
		// 2. TABLE_SCHEM String => table schema (may be null) 
		// 3. TABLE_NAME String => table name 
		// 4. TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM". 
		// 5. REMARKS String => explanatory comment on the table 
		// 6. TYPE_CAT String => the types catalog (may be null) 
		// 7. TYPE_SCHEM String => the types schema (may be null) 
		// 8. TYPE_NAME String => type name (may be null) 
		// 9. SELF_REFERENCING_COL_NAME String => name of the designated "identifier" column of a typed table (may be null) 
		// 10. REF_GENERATION String => specifies how values in SELF_REFERENCING_COL_NAME are created. Values are "SYSTEM", "USER", "DERIVED". (may be null) 

		int TABLE_NAME = 3;
		DatabaseMetaData dbm = cn.getMetaData();
		ResultSet rs = dbm.getTables(catalog, schema, "%", new String[] {"TABLE", "VIEW"});
		List<TableMetadata> tables = new ArrayList<TableMetadata>();
		try
		{
			while (rs.next())
			{
				TableMetadata tmd = new TableMetadata(catalog, schema, rs.getString(TABLE_NAME));
				tables.add(tmd);
			}		
		}
		finally
		{
			rs.close();
		}
		return tables;
	}

	/**
	 * Retrieves a description of the foreign key columns that reference the given table's primary key columns (the foreign keys exported by a table).	 
	 * @param catalog
	 * @param schema
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	public List<TableRelationship> getExportedTables(String catalog, String schema, String table) throws SQLException
	{
		//3. PKTABLE_NAME String => primary key table name
		//4. PKCOLUMN_NAME String => primary key column name 
		//7. FKTABLE_NAME String => foreign key table name being exported 
		//8. FKCOLUMN_NAME String => foreign key column name being exported
		//12. FK_NAME String => foreign key name (may be null)
		//13. PK_NAME String => primary key name (may be null)

		int PKTABLE_NAME = 3;
		int PKCOLUMN_NAME = 4;
		int FKTABLE_NAME = 7;
		int FKCOLUMN_NAME = 8;
		int KEY_SEQ = 9;
		int FK_NAME = 12;

		if (table == null || table.length() == 0)
			return null;
		
		List<TableRelationship> relationships = new ArrayList<TableRelationship>();
		
		DatabaseMetaData dbm = cn.getMetaData();
		ResultSet rs = dbm.getExportedKeys(catalog, schema, table);
		TableRelationship r = null;
		while (rs.next())
		{
			short i = rs.getShort(KEY_SEQ);
			//TableRelationship r = new TableRelationship(rs.getString(FK_NAME), catalog, schema, rs.getString(PKTABLE_NAME), rs.getString(FKTABLE_NAME));
			if (i == 1)
			{
				r = new TableRelationship(rs.getString(FK_NAME), catalog, schema, rs.getString(PKTABLE_NAME), rs.getString(FKTABLE_NAME));
				relationships.add(r);
			}
			
			/*
			int i = relationships.indexOf(r);
			if (i == -1)
				relationships.add(r);
			else
				r = relationships.get(i);
			*/
			
			r.getPrimaryKeyColumns().add(rs.getString(PKCOLUMN_NAME));
			r.getForeignKeyColumns().add(rs.getString(FKCOLUMN_NAME));
		}

		rs.close();
		return relationships;
	}

	/**
	 * Retrieves a description of the primary key columns that are referenced by the given table's foreign key columns (the primary keys imported by a table). 
	 * @param catalog
	 * @param schema
	 * @param table
	 * @return
	 * @throws SQLException
	 */
	public List<TableRelationship> getImportedTables(String catalog, String schema, String table) throws SQLException
	{
		//3. PKTABLE_NAME String => primary key table name
		//4. PKCOLUMN_NAME String => primary key column name 
		//7. FKTABLE_NAME String => foreign key table name being exported 
		//8. FKCOLUMN_NAME String => foreign key column name being exported
		//12. FK_NAME String => foreign key name (may be null)
		//13. PK_NAME String => primary key name (may be null)

		int PKTABLE_NAME = 3;
		int PKCOLUMN_NAME = 4;
		int FKTABLE_NAME = 7;
		int FKCOLUMN_NAME = 8;
		int KEY_SEQ = 9;
		int PK_NAME = 13;
		
		List<TableRelationship> relationships = new ArrayList<TableRelationship>();
		
		DatabaseMetaData dbm = cn.getMetaData();
		ResultSet rs = dbm.getImportedKeys(catalog, schema, table);
		try
		{
			TableRelationship r = null;
			while (rs.next())
			{
				short i = rs.getShort(KEY_SEQ);
				//System.out.println(String.format("(%d) %s.%s ", rs.getShort(KEY_SEQ), rs.getString(FKTABLE_NAME), rs.getString(FKCOLUMN_NAME)));
				if (i == 1)
				{
					r = new TableRelationship(rs.getString(PK_NAME), catalog, schema, rs.getString(PKTABLE_NAME), rs.getString(FKTABLE_NAME));
					relationships.add(r);
				}
				/*
				int i = relationships.indexOf(r);
				if (i == -1)
					relationships.add(r);
				else
					r = relationships.get(i);
				*/
					
				r.getPrimaryKeyColumns().add(rs.getString(PKCOLUMN_NAME));
				r.getForeignKeyColumns().add(rs.getString(FKCOLUMN_NAME));
			}
		}
		finally
		{
			rs.close();
		}
		return relationships;
	}

	public void ping(CteConnectionInfo connectionInfo) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException
	{
		setup(connectionInfo);
		open();
		close();
	}

	public List<String> getPrimaryKey(String catalog, String schema, String table) throws SQLException
	{
		//4.COLUMN_NAME String => column name 
		
		int COLUMN_NAME = 4; 
		List<String> cols = new ArrayList<String>();
		
		DatabaseMetaData dbm = cn.getMetaData();
		ResultSet rs = dbm.getPrimaryKeys(catalog, schema, table);
		while (rs.next())
			cols.add(rs.getString(COLUMN_NAME));
		
		rs.close();
		return cols;
	}
}
