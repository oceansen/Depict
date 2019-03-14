package no.simula.depict;

public class GlobalSymbols 
{
	public enum JDBC_DRIVER_CLASS
	{
		POSTGRESQL("PostgreSQL", "postgresql", "//localhost", "5432", "org.postgresql.Driver", "jdbc:%s:%s:%s/%s"),
		MYSQL("MySQL (Connector/J)", "mysql", "//localhost", "3306", "com.mysql.jdbc.Driver", "jdbc:%s:%s:%s/%s"),
		MS_SQL_2012("MS SQL Server", "sqlserver", "//localhost", "1433", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:%s:%s:%s;DatabaseName=%s"),
		SYBASE_SQL_ANYWHERE("Sybase SQL Anywhere (JConnect)", "sybase:Tds", "localhost", "2638", "com.sybase.jdbc4.jdbc.SybDriver", "jdbc:%s:%s:%s/%s"),
		ORACLE11G("Oracle", "oracle:thin", "localhost", "1521", "oracle.jdbc.OracleDriver", "jdbc:%s:@%s:%s/%s"), //SYS admin
		FIREBIRD("Firebird", "firebirdsql", "localhost", "3050", "org.firebirdsql.jdbc.FBDriver", "jdbc:%s:%s/%s:%s"); //SYSDBA masterkey
		
		private String name;
		private String driverName;
		private String server;
		private String port;
		private String driverClass;
		private String connectionStringTemplate;
		
		JDBC_DRIVER_CLASS(String name, String driverName, String server, String port, String driverClass, String connectionStringTemplate)
		{
			this.name = name;
			this.driverName = driverName;
			this.server = server;
			this.port = port;
			this.driverClass = driverClass;
			this.connectionStringTemplate = connectionStringTemplate;
		}
		public String getName() {
			return name;
		}
		public String getDriverName() {
			return driverName;
		}
		public String getServer() {
			return server;
		}
		public String getPort() {
			return port;
		}
		public String getDriverClass() {
			return driverClass;
		}
		public String getConnectionStringTemplate() {
			return connectionStringTemplate;
		}
		
		public static JDBC_DRIVER_CLASS getFromName(String name) 
		{
			if (name != null) 
			{
				for (JDBC_DRIVER_CLASS j : JDBC_DRIVER_CLASS.values()) 
				{
					if (name.equals(j.name)) 
						return j;
				}
			}
			return null;
		}
		public static JDBC_DRIVER_CLASS getFromDriverName(String driverName) 
		{
			if (driverName != null) 
			{
				for (JDBC_DRIVER_CLASS j : JDBC_DRIVER_CLASS.values()) 
				{
					if (driverName.equals(j.driverName)) 
						return j;
				}
			}
			return null;
		}
	}
	
	// JPA / EclipseLink
	public static final String PU_NAME = "depict";
	public static final String DBR_TABLE_CATALOG = "TABLE_CAT";
	public static final String DBR_TABLE_SCHEMA = "TABLE_SCHEM";
	public static final String DBR_TABLE_TYPE = "TABLE_TYPE";
	public static final String DBR_TABLE_NAME = "TABLE_NAME";

	public static final String DBR_TABLE_TYPE_TABLE = "TABLE";
	
	// GUI
	public static final String VIEW_ID_NAVIGATOR = "no.simula.depict.navigator";
	public static final String VIEW_ID_PROPERTYSHEET = "org.eclipse.ui.views.PropertySheet";
	public static final String VIEW_ID_LOG = "org.eclipse.pde.runtime.LogView";
	public static final String VIEW_ID_MODELGRAPH = "no.simula.depict.ui.views.modelgraph";
	public static final String VIEW_ID_SURGICALQUERY = "no.simula.depict.ui.views.surgicalquery";
	public static final String VIEW_ID_DBFILTER = "no.simula.depict.ui.views.dbfilter";
	public static final String VIEW_ID_DBINFO = "no.simula.depict.ui.views.dbinfo";
	public static final String VIEW_ID_INTERACTION_COVERAGE_GRAPH = "no.simula.depict.ui.views.interactioncoveragegraph";
	public static final String VIEW_ID_INTERACTION_COVERAGE_TABLE = "no.simula.depict.ui.views.interactioncoverage";
	
	//
	// OSGi event defs.
	public static final String EVENT_ID_MODELGRAPH_CREATED = "no/simula/depict/event/modelgraphcreated";
	public static final String EVENT_PARAM_MODELGRAPH_OBJECT = "no/simula/depict/param/modelgraphobject";
	public static final String EVENT_ID_INTERACTION_COVERAGE_REFRESHED = "no/simula/depict/event/interaction_coverage_refreshed";
	public static final String EVENT_PARAM_INTERACTION_COVERAGE_OBJECT = "no/simula/depict/param/interaction_coverage_object";
	public static final String EVENT_PARAM_CTE_RESOURCE = "no/simula/depict/param/cte_resource";
	
	
	//
	// File extension
	//public static final String FILE_EXT_CTEXL = "cte";
	public static final String FILE_EXT_TESTONA = "testona";
}
