package no.simula.depict.data;

import no.simula.depict.GlobalSymbols.JDBC_DRIVER_CLASS;
import no.simula.depict.model.CteComposition;
import no.simula.depict.model.CteModelSymbols;
import no.simula.depict.model.CteTag;

public class CteConnectionInfo
{
	//
	//Class.forName("com.sybase.jdbc4.jdbc.SybDriver");
	//cn = DriverManager.getConnection("jdbc:sybase:Tds:localhost:2638/demo", "DBA", "sql");
	//
	// schema name for sybase GROUPO
	// jdbc:<driver_name>://<server>:<port>/<db_name>
	// Ex.
	// jdbc:postgresql://localhost:5432/depict
	// jdbc:sybase:Tds:localhost:2638/demo
	//private static final String JDBC_CONNSTRING_TEMPLATE = "jdbc:%s:%s:%s/%s"; 
	//private static final String JDBC_CONNSTRING_TEMPLATE = "jdbc:%s:%s:%s;DatabaseName=%s"; 

	private String driverName;
	private String server;
	private String port;
	private String dbName;
	private String user;
	private String password;
	private String driverClass;
	private String connectionStringTemplate;
	
	public CteConnectionInfo()
	{
		/*
		driverName = "postgresql";
		server = "localhost";
		port = "5432";
		dbName = "depict";
		user = "postgres";
		//password = "postgres";
		driverClass = "org.postgresql.Driver";
		*/
	}

	public CteConnectionInfo(CteComposition catalog)
	{
		for (CteTag t : catalog.getTags())
			if (t.getType().equals(CteModelSymbols.CTEXL_TAG_DBCONNECTION))
			{
				driverName = t.findByKey(CteModelSymbols.CTEXL_ATTR_DRIVERNAME).getValue();
				server = t.findByKey(CteModelSymbols.CTEXL_ATTR_SERVER).getValue();
				port = t.findByKey(CteModelSymbols.CTEXL_ATTR_PORT).getValue();
				dbName = t.findByKey(CteModelSymbols.CTEXL_ATTR_DBNAME).getValue(); //catalog.getName();
				user = t.findByKey(CteModelSymbols.CTEXL_ATTR_USER).getValue();
				password = t.findByKey(CteModelSymbols.CTEXL_ATTR_PASSWORD).getValue();
				driverClass = t.findByKey(CteModelSymbols.CTEXL_ATTR_DRIVERCLASS).getValue();
				connectionStringTemplate = t.findByKey(CteModelSymbols.CTEXL_ATTR_CONNECTIONSTRING_TEMPLATE).getValue();
				break;
			}
	}

	public CteConnectionInfo(JDBC_DRIVER_CLASS j)
	{
		driverClass = j.getDriverClass();
		driverName = j.getDriverName();
		connectionStringTemplate = j.getConnectionStringTemplate();
	}
	
	public String getUser() {
		return user;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public String getDbName() {
		return dbName;
	}

	public String toString()
	{
		return String.format(connectionStringTemplate, driverName, server, port, dbName);
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public String getPassword() 
	{
		return password;
	}
	
	/*
	public String getEncryptedPassword() 
	{
		return DatatypeConverter.printHexBinary(encryptedPassword);
	}

	public void setEncryptedPassword(String encryptedPassword)
	{
		this.encryptedPassword = encryptedPassword.getBytes(UTF8);
	}
	*/
	
	public void setPassword(String password) 
	{
		this.password = password;
	}

	public String getDriverName() {
		return driverName;
	}

	public String getConnectionStringTemplate() {
		return connectionStringTemplate;
	}

	public void setConnectionStringTemplate(String connectionStringTemplate) {
		this.connectionStringTemplate = connectionStringTemplate;
	}
	
	/*
	private byte[] encrypt(String plainText, String encryptionKey) throws Exception 
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
		return cipher.doFinal(plainText.getBytes("UTF-8"));
	}
	
	private String decrypt(byte[] cipherText, String encryptionKey) throws Exception
	{
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding", "SunJCE");
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
		cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
		return new String(cipher.doFinal(cipherText),"UTF-8");
	}	
	*/
}
