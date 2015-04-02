/**
 *
 */
package steven.nextg.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import steven.nextg.database.util.TnsnamesUtils;
import steven.nextg.database.vo.ConnectionProperties;
import steven.nextg.database.vo.OracleConnectionProperties;
import steven.nextg.database.vo.PrimaryKey;
import steven.nextg.database.vo.TableColumn;
import steven.nextg.database.vo.UniqueKey;

/**
 * @author steven.lam.t.f
 *
 */
public enum PersistenceManager{
	INSTANCE;
	private final byte[] columnLock = new byte[0];
	private final Map<String, TableColumn[]> columnMap = new HashMap<>();
	private final byte[] primaryKeyLock = new byte[0];
	private final Map<String, PrimaryKey> primaryKeyMap = new HashMap<>();
	private final byte[] uniqueKeyLock = new byte[0];
	private final Map<String, UniqueKey[]> uniqueKeyMap = new HashMap<>();

	private PersistenceManager(){
	}
	public void initialize() throws IOException{
		for(final OracleConnectionProperties properties : TnsnamesUtils.load(new File(System.getenv("TNS_ADMIN") + File.separator + "tnsnames.ora"))){
			DatabaseEnvironment.INSTANCE.add(properties);
		}
		try(final InputStream is = new FileInputStream(new File(System.getenv("SQLPATH") + File.separator + "db.properties")); final InputStreamReader isr = new InputStreamReader(is, "UTF8"); final BufferedReader br = new BufferedReader(isr);){
			String line = null;
			int mode = 0;
			while((line = br.readLine()) != null){
				line = line.trim();
				if("# dev".equals(line)){
					mode = 1;
				}else if("# uat".equals(line)){
					mode = 2;
				}else if("# production".equals(line)){
					mode = 3;
				}else if(line.length() > 0 && mode > 0){
					final int equalIndex = line.indexOf('=');
					final String name = line.substring(0, equalIndex);
					final String[] parts = line.substring(equalIndex + 1).split(",");
					final ConnectionProperties properties = DatabaseEnvironment.INSTANCE.get(name);
					if(properties != null){
						for(int i = 0; i < parts.length; i += 2){
							properties.addUser(parts[i], parts[i + 1]);
						}
						properties.setSystemUser(parts[0]);
						if(mode == 1){
							properties.setDefaultUser("sys_iv");
							properties.addUser("sys_iv", "sys_iv");
							properties.addUser("syspos", "syspos");
							properties.addUser("sysvip", "sysvip");
							properties.addUser("sysapp", "sysapp");
							properties.addUser("sm1", "sm1");
						}else if(mode == 2){
							properties.setDefaultUser("sys_iv");
						}else if(mode == 3){
							properties.setDefaultUser(parts[0]);
							properties.setProduction(true);
						}
					}
				}
			}
		}
	}
	private String getKey(final String databaseName, final String tableOwner, final String tableName){
		return databaseName + "." + tableOwner + "." + tableName;
	}
	public void putTableColumns(final String databaseName, final String tableOwner, final String tableName, final TableColumn[] columns){
		synchronized(this.columnLock){
			this.columnMap.put(this.getKey(databaseName, tableOwner, tableName), columns);
		}
	}
	public TableColumn[] getTableColumns(final String databaseName, final String tableOwner, final String tableName){
		synchronized(this.columnLock){
			return this.columnMap.get(this.getKey(databaseName, tableOwner, tableName));
		}
	}
	public TableColumn getTableColumn(final String databaseName, final String tableOwner, final String tableName, final String columnName){
		synchronized(this.columnLock){
			final TableColumn[] columns = this.getTableColumns(databaseName, tableOwner, tableName);
			if(columns == null){
				return null;
			}else{
				for(final TableColumn column : columns){
					if(column.getColumnName().equals(columnName)){
						return column;
					}
				}
				return null;
			}
		}
	}
	public void putPrimaryKey(final String databaseName, final String tableOwner, final String tableName, final PrimaryKey primaryKey){
		synchronized(this.primaryKeyLock){
			this.primaryKeyMap.put(this.getKey(databaseName, tableOwner, tableName), primaryKey);
		}
	}
	public PrimaryKey getPrimaryKey(final String databaseName, final String tableOwner, final String tableName){
		synchronized(this.primaryKeyLock){
			return this.primaryKeyMap.get(this.getKey(databaseName, tableOwner, tableName));
		}
	}
	public void putUniqueKey(final String databaseName, final String tableOwner, final String tableName, final UniqueKey[] uniqueKeys){
		synchronized(this.uniqueKeyLock){
			this.uniqueKeyMap.put(this.getKey(databaseName, tableOwner, tableName), uniqueKeys);
		}
	}
	public UniqueKey[] getUniqueKey(final String databaseName, final String tableOwner, final String tableName){
		synchronized(this.uniqueKeyLock){
			return this.uniqueKeyMap.get(this.getKey(databaseName, tableOwner, tableName));
		}
	}
}
