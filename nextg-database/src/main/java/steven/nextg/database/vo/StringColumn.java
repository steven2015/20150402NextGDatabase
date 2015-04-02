/**
 *
 */
package steven.nextg.database.vo;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author steven.lam.t.f
 *
 */
public class StringColumn extends TableColumn{
	private static final long serialVersionUID = -8685716340837919505L;
	private final String dataType;
	private final int length;

	public StringColumn(final String databaseName, final String tableOwner, final String tableName, final String columnName, final boolean nullable, final String defaultValue, final String dataType, final int length){
		super(databaseName, tableOwner, tableName, columnName, nullable, defaultValue);
		this.dataType = dataType;
		this.length = length;
	}
	@Override
	public Object get(final ResultSet rs, final int columnIndex) throws SQLException{
		return rs.getString(columnIndex);
	}
	@Override
	public Object get(final CallableStatement cs, final int parameterIndex) throws SQLException{
		return cs.getString(parameterIndex);
	}
	@Override
	public String getDataType(){
		return this.dataType + "(" + this.length + ")";
	}
	public final int getLength(){
		return this.length;
	}
}
