package ttc15.tranj.processors;

import spoon.processing.AbstractAnnotationProcessor;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;
import ttc15.tranj.annotation.Cacheable;

import java.util.List;

import static ttc15.tranj.processors.UtilFactory.createCtField;

public class CacheProcessor extends AbstractAnnotationProcessor<Cacheable, CtMethod<?>> {

	@Override
	public void process(Cacheable cacheable, CtMethod<?> ctMethod) {
		final String getCacheLastAccessed = "__getCacheLastAccessed";
		final String getCacheContent = "__getCacheContent";

		// create fields.
		final CtField<Long> fieldCacheLastAccess = createCtField(
				getFactory(), getCacheLastAccessed, getFactory().Type().LONG_PRIMITIVE, "0",
				ModifierKind.PRIVATE
		);
		final CtField fieldCacheContent = createCtField(
				getFactory(), getCacheContent, ctMethod.getType(), null, ModifierKind.PRIVATE
		);
		ctMethod.getDeclaringType().addField(fieldCacheLastAccess);
		ctMethod.getDeclaringType().addField(fieldCacheContent);

		// Gets all returns to cache them.
		final List<CtReturn> returns = ctMethod.getElements(new TypeFilter<CtReturn>(CtReturn.class));
		for (CtReturn ctReturn : returns) {
			ctReturn.insertBefore(getFactory().Code().createVariableAssignment(
					fieldCacheContent.getReference(), false,
					ctReturn.getReturnedExpression()));
			ctReturn.insertBefore(getFactory().Code().createVariableAssignment(
					fieldCacheLastAccess.getReference(), false,
					getFactory().Code().<Long>createCodeSnippetExpression("System.currentTimeMillis()")));
			ctReturn.setReturnedExpression(getFactory().Code().createCodeSnippetExpression(getCacheContent));
		}

		// add lifetime.
		final CtIf anIf = getFactory().Core().createIf();
		anIf.setCondition(getFactory().Code().<Boolean>createCodeSnippetExpression(
				"System.currentTimeMillis() - " + getCacheLastAccessed + " < " + cacheable
						.lifetime() + " && " + getCacheContent + " != null")
		);
		final CtReturn<Object> aReturn = getFactory().Core().createReturn();
		aReturn.setReturnedExpression(
				getFactory().Code().createCodeSnippetExpression(getCacheContent));
		anIf.setThenStatement(aReturn);
		anIf.setParent(ctMethod.getBody());
		ctMethod.getBody().insertBegin(anIf);

		// add forever.
		final CtIf ifForever = getFactory().Core().createIf();
		ifForever.setCondition(getFactory().Code().<Boolean>createCodeSnippetExpression(
				cacheable.forever() + " && " + getCacheContent + " != null"
		));
		ifForever.setThenStatement(aReturn);
		ifForever.setParent(ctMethod.getBody());
		ctMethod.getBody().insertBegin(ifForever);
	}
}
