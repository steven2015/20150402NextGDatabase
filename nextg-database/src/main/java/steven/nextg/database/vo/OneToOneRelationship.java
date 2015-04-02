/**
 *
 */
package steven.nextg.database.vo;

/**
 * @author steven.lam.t.f
 *
 */
public class OneToOneRelationship extends ColumnRelationship{
	private static final long serialVersionUID = 2587201132131335273L;

	public OneToOneRelationship(final TableColumn firstColumn, final TableColumn secondColumn){
		super(firstColumn, secondColumn);
	}
}
