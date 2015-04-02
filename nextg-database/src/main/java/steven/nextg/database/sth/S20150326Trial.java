/**
 *
 */
package steven.nextg.database.sth;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import oracle.jdbc.OracleDriver;
import steven.nextg.database.PersistenceManager;
import steven.nextg.database.naive.NaiveDAO;
import steven.nextg.database.vo.BigDecimalColumn;
import steven.nextg.database.vo.DateColumn;
import steven.nextg.database.vo.PrimaryKey;
import steven.nextg.database.vo.StringColumn;
import steven.nextg.database.vo.TableColumn;

/**
 * @author steven.lam.t.f
 *
 */
public class S20150326Trial{
	public static final void main(final String[] args) throws SQLException, IOException{
		DriverManager.registerDriver(new OracleDriver());
		PersistenceManager.INSTANCE.initialize();
		try(final NaiveDAO dao = NaiveDAO.asSystem("cnposuat");){
			System.out.println(dao.select("select * from dba_tab_columns where table_name = 'IV_PICK_REQ_LINE'"));
			System.out.println(dao.getFetchedQueryResultCount());
			for(final TableColumn column : dao.getQueryColumns()){
				System.out.print(column.getColumnName() + "\t");
			}
			System.out.println();
			for(final Object[] row : dao.getQueryResults(0, Integer.MAX_VALUE)){
				for(final Object o : row){
					System.out.print(o + "\t");
				}
				System.out.println();
			}
		}
	}
	public static final TableColumn[] getTableColumns(final NaiveDAO dao, final String tableName){
		if(dao.select("select owner, table_name, column_name, data_type, nvl(data_length, 0), nvl(data_precision, 0), nvl(data_scale, 0), nullable, data_default from dba_tab_columns where table_name = ? order by column_id", tableName)){
			final TableColumn[] columns = new TableColumn[dao.getFetchedQueryResultCount()];
			final List<Object[]> rows = dao.getQueryResults(0, Integer.MAX_VALUE);
			for(int i = 0; i < columns.length; i++){
				final Object[] row = rows.get(i);
				final String dataType = (String)row[3];
				if("NUMBER".equals(dataType)){
					columns[i] = new BigDecimalColumn(dao.getDatabaseName(), (String)row[0], (String)row[1], (String)row[2], "Y".equals(row[7]), (String)row[8], ((BigDecimal)row[5]).intValue(), ((BigDecimal)row[6]).intValue());
				}else if("DATE".equals(dataType)){
					columns[i] = new DateColumn(dao.getDatabaseName(), (String)row[0], (String)row[1], (String)row[2], "Y".equals(row[7]), (String)row[8]);
				}else{
					columns[i] = new StringColumn(dao.getDatabaseName(), (String)row[0], (String)row[1], (String)row[2], "Y".equals(row[7]), (String)row[8], dataType, ((BigDecimal)row[4]).intValue());
				}
			}
			return columns;
		}
		return null;
	}
	public static final PrimaryKey getPrimaryKey(final NaiveDAO dao, final String tableName){
		if(dao.select("select cc.column_name from dba_cons_columns cc, dba_constraints c where cc.owner = c.owner and cc.constraint_name = c.constraint_name and c.constraint_type = 'P' and c.table_name = ?", tableName)){
			dao.getQueryResults(0, Integer.MAX_VALUE);
		}
		return null;
	}
}
