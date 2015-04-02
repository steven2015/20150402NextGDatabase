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
public class BigDecimalColumn extends TableColumn{
	private static final long serialVersionUID = -6331589140516443594L;
	private final int precision;
	private final int scale;

	public BigDecimalColumn(final String databaseName, final String tableOwner, final String tableName, final String columnName, final boolean nullable, final String defaultValue, final int precision, final int scale){
		super(databaseName, tableOwner, tableName, columnName, nullable, defaultValue);
		this.precision = precision;
		this.scale = scale;
	}
	@Override
	public Object get(final ResultSet rs, final int columnIndex) throws SQLException{
		return rs.getBigDecimal(columnIndex);
	}
	@Override
	public Object get(final CallableStatement cs, final int parameterIndex) throws SQLException{
		return cs.getBigDecimal(parameterIndex);
	}
	@Override
	public String getDataType(){
		if(this.precision == 0){
			return "NUMBER";
		}else if(this.scale == 0){
			return "NUMBER(" + this.precision + ")";
		}else{
			return "NUMBER(" + this.precision + "," + this.scale + ")";
		}
	}
	public final int getPrecision(){
		return this.precision;
	}
	public final int getScale(){
		return this.scale;
	}
}
