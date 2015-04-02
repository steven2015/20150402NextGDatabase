/**
 *
 */
package steven.nextg.database.vo;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author steven.lam.t.f
 *
 */
public abstract class TableColumn implements Serializable{
	private static final long serialVersionUID = -9196287178277189288L;
	private final String databaseName;
	private final String tableOwner;
	private final String tableName;
	private final String columnName;
	private final boolean nullable;
	private final String defaultValue;

	protected TableColumn(final String databaseName, final String tableOwner, final String tableName, final String columnName, final boolean nullable, final String defaultValue){
		this.databaseName = databaseName;
		this.tableOwner = tableOwner;
		this.tableName = tableName;
		this.columnName = columnName;
		this.nullable = nullable;
		this.defaultValue = defaultValue;
	}
	public abstract Object get(ResultSet rs, int columnIndex) throws SQLException;
	public abstract Object get(CallableStatement cs, int parameterIndex) throws SQLException;
	public abstract String getDataType();
	public final String getTableOwner(){
		return this.tableOwner;
	}
	public final String getTableName(){
		return this.tableName;
	}
	public final boolean isNullable(){
		return this.nullable;
	}
	public final String getDefaultValue(){
		return this.defaultValue;
	}
	public final String getDatabaseName(){
		return this.databaseName;
	}
	public final String getColumnName(){
		return this.columnName;
	}
}
