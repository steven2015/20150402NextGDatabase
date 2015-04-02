/**
 *
 */
package steven.nextg.database.vo;

/**
 * @author steven.lam.t.f
 *
 */
public class OneToManyRelationship extends ColumnRelationship{
	private static final long serialVersionUID = 7917240557714818239L;

	public OneToManyRelationship(final TableColumn firstColumn, final TableColumn secondColumn){
		super(firstColumn, secondColumn);
	}
}
