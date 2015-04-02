/**
 *
 */
package steven.nextg.database.vo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author steven.lam.t.f
 *
 */
public class OracleConnectionProperties extends ConnectionProperties implements Serializable{
	private static final long serialVersionUID = 2821110232575776577L;
	private final String host;
	private final int port;
	private final String sid;

	public OracleConnectionProperties(final String name, final String host, final int port, final String sid){
		super(name);
		this.host = host;
		this.port = port;
		this.sid = sid;
	}
	@Override
	public Connection openConnection(final String username, final String password) throws SQLException{
		final Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@" + this.host + ":" + this.port + ":" + this.sid, username, password);
		connection.setAutoCommit(false);
		return connection;
	}
}
