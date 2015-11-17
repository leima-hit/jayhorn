/**
 * 
 */
package soottocfg.cfg.expression;

import java.util.Set;

import soottocfg.cfg.Node;
import soottocfg.cfg.Variable;
import soottocfg.cfg.expression.BinaryExpression.BinaryOperator;
import soottocfg.cfg.type.BoolType;
import soottocfg.cfg.type.IntType;
import soottocfg.cfg.type.Type;

/**
 * @author schaef
 *
 */
public abstract class Expression implements Node {

	public abstract Set<Variable> getUsedVariables();
	
	public abstract Set<Variable> getLVariables();
	
	public abstract Type getType();
	
	/**
	 * TODO: this one should be replaced by something that is easier to remove
	 * when restoring boolean types.
	 * @return
	 */
	public Expression castToBoolIfNecessary() {
		if (this.getType()==IntType.instance()) {
			return new IteExpression(new BinaryExpression(BinaryOperator.Eq, this, IntegerLiteral.zero()), BooleanLiteral.falseLiteral(), BooleanLiteral.trueLiteral());
		} else if (this.getType()==BoolType.instance()) {
			return this;
		} else {
			throw new RuntimeException("Cannot cast "+ this + " to Boolean.");
		}
	}
		
}
