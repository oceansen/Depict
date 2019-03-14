package no.simula.depict.data;

public class DatabaseInfo 
{
	private String driverName;
	private String driverVersion;
	private int driverMajorVersion;
	private int driverMinorVersion;
	private String databaseProductName;
	private String databaseProductVersion;
	private String catalogName;
	private String url;
	
	public String getDriverName() {
		return driverName;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	public String getDriverVersion() {
		return driverVersion;
	}
	public void setDriverVersion(String driverVersion) {
		this.driverVersion = driverVersion;
	}
	public int getDriverMajorVersion() {
		return driverMajorVersion;
	}
	public void setDriverMajorVersion(int driverMajorVersion) {
		this.driverMajorVersion = driverMajorVersion;
	}
	public int getDriverMinorVersion() {
		return driverMinorVersion;
	}
	public void setDriverMinorVersion(int driverMinorVersion) {
		this.driverMinorVersion = driverMinorVersion;
	}
	public String getDatabaseProductName() {
		return databaseProductName;
	}
	public void setDatabaseProductName(String databaseProductName) {
		this.databaseProductName = databaseProductName;
	}
	public String getDatabaseProductVersion() {
		return databaseProductVersion;
	}
	public void setDatabaseProductVersion(String databaseProductVersion) {
		this.databaseProductVersion = databaseProductVersion;
	}
	public String getCatalogName() {
		return catalogName;
	}
	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return String.format("DBMS: %s - %s\nDriver: %s - %s %d.%d\nCatalog: %s\nURL: %s", 
				databaseProductName,
				databaseProductVersion,
				driverName,
				driverVersion,
				driverMajorVersion,
				driverMinorVersion,
				catalogName,
				url);
	}
}
