/**
 *
 */
package steven.nextg.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import steven.nextg.database.vo.ConnectionProperties;

/**
 * @author steven.lam.t.f
 *
 */
public enum DatabaseEnvironment{
	INSTANCE;
	private final Map<String, ConnectionProperties> map = new HashMap<>();

	private DatabaseEnvironment(){
	}
	public void add(final ConnectionProperties properties){
		this.map.put(properties.getName(), properties);
	}
	public ConnectionProperties get(final String name){
		return this.map.get(name);
	}
	public Connection openConnection(final String name) throws SQLException{
		return this.get(name).openConnection();
	}
	public Connection openConnection(final String name, final String user) throws SQLException{
		return this.get(name).openConnection(user);
	}
}
