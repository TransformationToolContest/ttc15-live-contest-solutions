package ttc15.tranj.processors;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtCatchVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtPackageReference;
import spoon.reflect.reference.CtTypeReference;

public final class UtilFactory {

	public static <T> CtField<T> createCtField(Factory factory, String name,
											   CtTypeReference<T> type, String exp,
											   ModifierKind...visibilities) {
		final CtField<T> aField = factory.Core().createField();
		for (ModifierKind modifierKind : visibilities) {
			aField.addModifier(modifierKind);
		}
		aField.setType(type);
		if (exp != null) {
			aField.setDefaultExpression(factory.Code().<T>createCodeSnippetExpression(exp));
		}
		aField.setSimpleName(name);
		return aField;
	}

	public static <T extends CtStatement> CtBlock<?> createCtBlock(Factory factory, T element) {
		final CtBlock<?> block = factory.Core().createBlock();
		block.addStatement(element);
		return block;
	}

	public static CtThrow createCtThrow(Factory factory, String thrownExp) {
		CtThrow aThrow = factory.Core().createThrow();
		aThrow.setThrownExpression(
				factory.Code().<Exception>createCodeSnippetExpression(thrownExp));
		return aThrow;
	}

	public static CtCatch createCtCatch(Factory factory, String nameCatch,
										Class<? extends Throwable> exception,
										CtBlock<?> ctBlock) {
		final CtCatch aCatch = factory.Core().createCatch();
		final CtCatchVariable<Throwable> catchVariable = factory.Core().createCatchVariable();
		catchVariable.setType(UtilFactory.<Throwable>createCtTypeReference(factory, exception));
		catchVariable.setSimpleName(nameCatch);
		aCatch.setParameter(catchVariable);
		aCatch.setBody(ctBlock);
		return aCatch;
	}

	public static <T> CtTypeReference<T> createCtTypeReference(Factory factory,
															   Class<?> originalClass) {
		CtTypeReference<T> ref = factory.Core().createTypeReference();
		ref.setSimpleName(originalClass.getSimpleName());
		ref.setPackage(createCtPackageReference(factory, originalClass.getPackage()));
		return ref;
	}

	public static CtPackageReference createCtPackageReference(Factory factory,
															  Package originalPackage) {
		final CtPackageReference packRef = factory.Core().createPackageReference();
		packRef.setSimpleName(originalPackage.getName());
		return packRef;
	}

	private UtilFactory() {
		throw new AssertionError("No instance.");
	}
}
