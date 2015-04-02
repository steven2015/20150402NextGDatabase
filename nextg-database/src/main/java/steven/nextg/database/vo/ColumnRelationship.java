/**
 *
 */
package steven.nextg.database.vo;

import java.io.Serializable;

/**
 * @author steven.lam.t.f
 *
 */
public abstract class ColumnRelationship implements Serializable{
	private static final long serialVersionUID = -8097283404267870335L;
	private final TableColumn firstColumn;
	private final TableColumn secondColumn;

	protected ColumnRelationship(final TableColumn firstColumn, final TableColumn secondColumn){
		this.firstColumn = firstColumn;
		this.secondColumn = secondColumn;
	}
	public final TableColumn getFirstColumn(){
		return this.firstColumn;
	}
	public final TableColumn getSecondColumn(){
		return this.secondColumn;
	}
}
