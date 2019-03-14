package no.simula.depict.data;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class PooledConnectionFactory extends BasePooledObjectFactory<ConnectionFactory>
{
	private CteConnectionInfo connectionInfo;
	
	PooledConnectionFactory(CteConnectionInfo connectionInfo)
	{
		this.connectionInfo = connectionInfo;
	}
	
	@Override
	public ConnectionFactory create() throws Exception 
	{
        //
        // Creates a connection factory object which will be used by
        // the pool to create the connection object. We passes the
        // JDBC url info, username and password.
        ConnectionFactory cf = new DriverManagerConnectionFactory(
        		connectionInfo.toString(),
        		connectionInfo.getUser(),
        		connectionInfo.getPassword());
		return cf;
	}

	@Override
	public PooledObject<ConnectionFactory> wrap(ConnectionFactory cf) 
	{
        return new DefaultPooledObject<ConnectionFactory>(cf);
	}
}
