/**
 *
 */
package steven.nextg.database.vo;

import java.io.Serializable;

/**
 * @author steven.lam.t.f
 *
 */
public class UniqueKey implements Serializable{
	private static final long serialVersionUID = 2846885045869118735L;
	private final String databaseName;
	private final String tableOwner;
	private final String tableName;
	private final TableColumn[] columns;

	public UniqueKey(final TableColumn... columns){
		this.databaseName = columns[0].getDatabaseName();
		this.tableOwner = columns[0].getTableOwner();
		this.tableName = columns[0].getTableName();
		this.columns = columns;
	}
	public final String getTableOwner(){
		return this.tableOwner;
	}
	public final String getTableName(){
		return this.tableName;
	}
	public final TableColumn[] getColumns(){
		return this.columns;
	}
	public final String getDatabaseName(){
		return this.databaseName;
	}
}
