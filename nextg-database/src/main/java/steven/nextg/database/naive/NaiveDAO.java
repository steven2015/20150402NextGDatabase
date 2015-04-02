/**
 *
 */
package steven.nextg.database.naive;

import java.io.Closeable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import steven.nextg.database.DatabaseEnvironment;
import steven.nextg.database.vo.BigDecimalColumn;
import steven.nextg.database.vo.ConnectionProperties;
import steven.nextg.database.vo.DateColumn;
import steven.nextg.database.vo.StringColumn;
import steven.nextg.database.vo.TableColumn;

/**
 * @author steven.lam.t.f
 *
 */
public class NaiveDAO implements Closeable{
	private static final Logger LOG = LogManager.getLogger();
	private static final Object[] EMPTY_ARRAY = new Object[0];
	// synchronized
	private final byte[] lock = new byte[0];
	private volatile Statement currentStatement;
	private final List<Object[]> queryResults = new ArrayList<>();
	// normal
	private final String databaseName;
	private final boolean production;
	private final Connection connection;
	private final Map<String, Statement> cachedStatements = new HashMap<>();
	private volatile TableColumn[] queryColumns;
	private volatile boolean queryCompleted;
	private volatile Exception lastException;
	private volatile boolean allowUpdateInProduction;

	public NaiveDAO(final String databaseName, final String user) throws SQLException{
		this.databaseName = databaseName;
		final ConnectionProperties properties = DatabaseEnvironment.INSTANCE.get(databaseName);
		this.production = properties.isProduction();
		this.connection = properties.openConnection(user);
		this.allowUpdateInProduction = false;
	}
	public static final NaiveDAO asDefault(final String databaseName) throws SQLException{
		final ConnectionProperties properties = DatabaseEnvironment.INSTANCE.get(databaseName);
		return new NaiveDAO(databaseName, properties.getDefaultUser());
	}
	public static final NaiveDAO asSystem(final String databaseName) throws SQLException{
		final ConnectionProperties properties = DatabaseEnvironment.INSTANCE.get(databaseName);
		return new NaiveDAO(databaseName, properties.getSystemUser());
	}
	@Override
	public void close(){
		this.cancel();
		for(final Statement cachedStatement : this.cachedStatements.values()){
			try{
				cachedStatement.close();
			}catch(final Exception e){
			}
		}
		this.cachedStatements.clear();
		if(this.connection != null){
			try{
				this.connection.rollback();
			}catch(final Exception e){
				NaiveDAO.LOG.error("Cannot rollback connection.", e);
			}
			try{
				this.connection.close();
			}catch(final Exception e){
				NaiveDAO.LOG.error("Cannot close connection.", e);
			}
		}
	}
	public void cancel(){
		synchronized(this.lock){
			if(this.currentStatement != null){
				try{
					this.currentStatement.cancel();
				}catch(final Exception e){
					NaiveDAO.LOG.error("Cannot cancel statement.", e);
				}
				this.currentStatement = null;
			}
		}
	}
	public boolean select(final String sql, final Object... parameters){
		try{
			this.queryCompleted = false;
			final PreparedStatement ps;
			synchronized(this.lock){
				this.currentStatement = this.cachedStatements.get(sql);
				if(this.currentStatement == null){
					this.currentStatement = this.connection.prepareStatement(sql);
					this.cachedStatements.put(sql, this.currentStatement);
				}
				ps = (PreparedStatement)this.currentStatement;
				if(parameters != null){
					for(int i = 0; i < parameters.length; i++){
						ps.setObject(i + 1, parameters[i]);
					}
				}
			}
			final ResultSetMetaData meta = ps.getMetaData();
			final TableColumn[] columns = new TableColumn[meta.getColumnCount()];
			for(int i = 0; i < columns.length; i++){
				final String className = meta.getColumnClassName(i + 1);
				if("java.lang.String".equals(className)){
					columns[i] = new StringColumn(this.databaseName, null, null, meta.getColumnLabel(i + 1), false, null, null, 0);
				}else if("java.math.BigDecimal".equals(className)){
					columns[i] = new BigDecimalColumn(this.databaseName, null, null, meta.getColumnLabel(i + 1), false, null, 0, 0);
				}else if("java.sql.Timestamp".equals(className)){
					columns[i] = new DateColumn(this.databaseName, null, null, meta.getColumnLabel(i + 1), false, null);
				}else{
					columns[i] = new StringColumn(this.databaseName, null, null, meta.getColumnLabel(i + 1), false, null, null, 0);
				}
			}
			this.queryColumns = columns;
			try(final ResultSet rs = ps.executeQuery();){
				ps.clearParameters();
				final List<Object[]> rows = new ArrayList<>();
				final int limit = 100;
				while(rs.next()){
					final Object[] row = new Object[columns.length];
					for(int i = 0; i < columns.length; i++){
						row[i] = columns[i].get(rs, i + 1);
					}
					rows.add(row);
					if(rows.size() >= limit){
						synchronized(this.lock){
							this.queryResults.addAll(rows);
						}
						rows.clear();
					}
				}
				synchronized(this.lock){
					this.queryResults.addAll(rows);
				}
				rows.clear();
				this.queryCompleted = true;
			}
			return true;
		}catch(final Exception e){
			NaiveDAO.LOG.error("Cannot select.", e);
			this.lastException = e;
			return false;
		}finally{
			this.cancel();
		}
	}
	public int getFetchedQueryResultCount(){
		return this.queryResults.size();
	}
	public List<Object[]> getQueryResults(final int fromIndex, final int toIndex){
		synchronized(this.lock){
			final int actualToIndex = Math.min(toIndex, this.queryResults.size());
			return this.queryResults.subList(fromIndex, actualToIndex);
		}
	}
	public int update(final String sql, final Object... parameters){
		try{
			if(this.production && this.allowUpdateInProduction == false){
				throw new Exception("Cannot perform update in production environment.");
			}
			final PreparedStatement ps;
			synchronized(this.lock){
				this.currentStatement = this.cachedStatements.get(sql);
				if(this.currentStatement == null){
					this.currentStatement = this.connection.prepareStatement(sql);
					this.cachedStatements.put(sql, this.currentStatement);
				}
				ps = (PreparedStatement)this.currentStatement;
				if(parameters != null){
					for(int i = 0; i < parameters.length; i++){
						ps.setObject(i + 1, parameters[i]);
					}
				}
			}
			return ps.executeUpdate();
		}catch(final Exception e){
			NaiveDAO.LOG.error("Cannot update.", e);
			this.lastException = e;
			return -1;
		}finally{
			this.cancel();
		}
	}
	public Object[] execute(final String sql, final SQLType[] outParameters){
		try{
			final CallableStatement cs;
			synchronized(this.lock){
				this.currentStatement = this.connection.prepareCall(sql);
				cs = (CallableStatement)this.currentStatement;
				if(outParameters != null){
					for(int i = 0; i < outParameters.length; i++){
						cs.registerOutParameter(i + 1, outParameters[i]);
					}
				}
			}
			final ResultSetMetaData meta = cs.getMetaData();
			final TableColumn[] columns = new TableColumn[meta.getColumnCount()];
			for(int i = 0; i < columns.length; i++){
				final String className = meta.getColumnClassName(i + 1);
				if("java.lang.String".equals(className)){
					columns[i] = new StringColumn(this.databaseName, null, null, meta.getColumnLabel(i + 1), false, null, null, 0);
				}else if("java.math.BigDecimal".equals(className)){
					columns[i] = new BigDecimalColumn(this.databaseName, null, null, meta.getColumnLabel(i + 1), false, null, 0, 0);
				}else if("java.sql.Timestamp".equals(className)){
					columns[i] = new DateColumn(this.databaseName, null, null, meta.getColumnLabel(i + 1), false, null);
				}else{
					columns[i] = new StringColumn(this.databaseName, null, null, meta.getColumnLabel(i + 1), false, null, null, 0);
				}
			}
			cs.execute();
			if(columns.length == 0){
				return NaiveDAO.EMPTY_ARRAY;
			}else{
				final Object[] row = new Object[columns.length];
				for(int i = 0; i < columns.length; i++){
					row[i] = columns[i].get(cs, i + 1);
				}
				return row;
			}
		}catch(final Exception e){
			NaiveDAO.LOG.error("Cannot update.", e);
			this.lastException = e;
			return null;
		}finally{
			this.cancel();
		}
	}
	public final TableColumn[] getQueryColumns(){
		return this.queryColumns;
	}
	public final boolean isQueryCompleted(){
		return this.queryCompleted;
	}
	public final Exception getLastException(){
		return this.lastException;
	}
	public final boolean isAllowUpdateInProduction(){
		return this.allowUpdateInProduction;
	}
	public final void setAllowUpdateInProduction(final boolean allowUpdateInProduction){
		this.allowUpdateInProduction = allowUpdateInProduction;
	}
	public final String getDatabaseName(){
		return this.databaseName;
	}
}
