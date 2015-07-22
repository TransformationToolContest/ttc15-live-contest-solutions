package ttc15.tranj.processors;

import spoon.processing.AbstractAnnotationProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtTry;
import spoon.reflect.code.CtWhile;
import spoon.reflect.declaration.CtMethod;
import ttc15.tranj.annotation.RetryOnFailure;

import java.util.ArrayList;
import java.util.List;

import static ttc15.tranj.processors.UtilFactory.createCtBlock;
import static ttc15.tranj.processors.UtilFactory.createCtCatch;
import static ttc15.tranj.processors.UtilFactory.createCtThrow;

public class RetryProcessor extends AbstractAnnotationProcessor<RetryOnFailure, CtMethod<?>> {
	public void process(RetryOnFailure retryOnFailure, CtMethod<?> ctMethod) {

		final CtBlock finalBlock = getFactory().Core().createBlock();

		final String retryCountName = "__retryCount";
		final CtLocalVariable<Integer> retryCount = getFactory().Code().createLocalVariable(
				getFactory().Type().INTEGER_PRIMITIVE, retryCountName,
				getFactory().Code().<Integer>createCodeSnippetExpression("0"));

		finalBlock.addStatement(retryCount);

		final CtWhile aWhile = getFactory().Core().createWhile();
		aWhile.setLoopingExpression(getFactory().Code().<Boolean>createCodeSnippetExpression("true"));

		final CtTry aTry = getFactory().Core().createTry();
		aTry.setBody(ctMethod.getBody());

		// add escalate.
		for (Class<? extends Throwable> anEscalate : retryOnFailure.escalate()) {
			final String exceptionVarName = "e";
			final CtCatch e = createCtCatch(getFactory(), exceptionVarName, anEscalate,
											createCtBlock(getFactory(),
														  createCtThrow(getFactory(),
																		exceptionVarName)));
			aTry.addCatcher(e);
		}

		// add retry.
		for (Class<? extends Throwable> aRetry : retryOnFailure.retry()) {
			final String exceptionVarName = "e";

			final List<CtStatement> ctStatements = new ArrayList<CtStatement>();

			// add retry cases.
			ctStatements.add(getFactory().Code().createCodeSnippetStatement(retryCountName + "++"));

			// add delay.
			final CtTry aTryDelay = getFactory().Core().createTry();
			aTryDelay.setBody(createCtBlock(getFactory(), getFactory().Code().createCodeSnippetStatement("Thread.sleep(" + retryOnFailure.delay() + ")")));
			aTryDelay.addCatcher(createCtCatch(getFactory(), "e1", InterruptedException.class, createCtBlock(getFactory(), createCtThrow(getFactory(), exceptionVarName))));

			// add attempts.
			final CtIf anIf = getFactory().Core().createIf();
			anIf.setCondition(getFactory().Code().<Boolean>createCodeSnippetExpression(retryCountName + " > " + retryOnFailure.attempts()));
			anIf.setThenStatement(createCtThrow(getFactory(), exceptionVarName));
			anIf.setElseStatement(aTryDelay);
			ctStatements.add(anIf);

			final CtBlock<?> ctBlock = getFactory().Core().createBlock();
			ctBlock.setStatements(ctStatements);
			final CtCatch e = createCtCatch(getFactory(), exceptionVarName, aRetry, ctBlock);
			aTry.addCatcher(e);
		}

		// Apply transformation.
		aWhile.setBody(aTry);
		finalBlock.addStatement(aWhile);
		ctMethod.setBody(finalBlock);
	}
}
