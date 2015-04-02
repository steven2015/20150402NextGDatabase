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
public class DateColumn extends TableColumn{
	private static final long serialVersionUID = 9201509956713693661L;

	public DateColumn(final String databaseName, final String tableOwner, final String tableName, final String columnName, final boolean nullable, final String defaultValue){
		super(databaseName, tableOwner, tableName, columnName, nullable, defaultValue);
	}
	@Override
	public Object get(final ResultSet rs, final int columnIndex) throws SQLException{
		return rs.getTimestamp(columnIndex);
	}
	@Override
	public Object get(final CallableStatement cs, final int parameterIndex) throws SQLException{
		return cs.getTimestamp(parameterIndex);
	}
	@Override
	public String getDataType(){
		return "DATE";
	}
}
