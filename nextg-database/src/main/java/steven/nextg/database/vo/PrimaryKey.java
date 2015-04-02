/**
 *
 */
package steven.nextg.database.vo;

/**
 * @author steven.lam.t.f
 *
 */
public class PrimaryKey extends UniqueKey{
	private static final long serialVersionUID = 6093341247286288451L;

	public PrimaryKey(final TableColumn... columns){
		super(columns);
	}
}
