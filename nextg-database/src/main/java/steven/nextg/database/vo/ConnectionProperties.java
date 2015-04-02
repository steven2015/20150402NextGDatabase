/**
 *
 */
package steven.nextg.database.vo;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author steven.lam.t.f
 *
 */
public abstract class ConnectionProperties implements Serializable{
	private static final long serialVersionUID = 2821110232575776577L;
	private final String name;
	private final Map<String, String> users = new HashMap<>();
	private boolean production;
	private String defaultUser;
	private String systemUser;

	public ConnectionProperties(final String name){
		this.name = name;
	}
	public void setDefaultUser(final String user){
		this.defaultUser = user;
	}
	public void setSystemUser(final String user){
		this.systemUser = user;
	}
	public void addUser(final String user, final String password){
		this.users.put(user, password);
	}
	public Connection openConnection() throws SQLException{
		return this.openConnection(this.defaultUser);
	}
	public Connection openConnection(final String user) throws SQLException{
		return this.openConnection(user, this.users.get(user));
	}
	public abstract Connection openConnection(String user, String password) throws SQLException;
	public final String getName(){
		return this.name;
	}
	public final String getDefaultUser(){
		return this.defaultUser;
	}
	public final String getSystemUser(){
		return this.systemUser;
	}
	public final boolean isProduction(){
		return this.production;
	}
	public final void setProduction(final boolean production){
		this.production = production;
	}
}
