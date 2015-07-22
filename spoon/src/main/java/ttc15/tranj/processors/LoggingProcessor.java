package ttc15.tranj.processors;

import org.slf4j.Logger;
import spoon.processing.AbstractAnnotationProcessor;
import spoon.reflect.code.CtCatch;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtReturn;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtThrow;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;
import ttc15.tranj.annotation.Loggable;

import java.util.List;

import static ttc15.tranj.processors.UtilFactory.createCtField;

public class LoggingProcessor extends AbstractAnnotationProcessor<Loggable, CtMethod<?>> {

	private final String entryTime = "__entryTime";

	@Override
	public void process(Loggable loggable, CtMethod<?> ctMethod) {
		final String logger = "__logger";

		// create entry point logging.
		if (loggable.entry()) {
			final CtLocalVariable<Long> entryTimeLocalVar = getFactory().Code().createLocalVariable(
					getFactory().Type().LONG_PRIMITIVE,
					entryTime, getFactory().Code().<Long>createCodeSnippetExpression(
							"System.currentTimeMillis()")
			);
			ctMethod.getBody().insertBegin(createTraceLog(logger, buildEntryLog(ctMethod, loggable)));
			ctMethod.getBody().insertBegin(entryTimeLocalVar);
		}

		// create exit point logging.
		if (loggable.exit()) {
			final List<CtReturn<?>> returns = ctMethod
					.getElements(new TypeFilter<CtReturn<?>>(CtReturn.class));
			for (CtReturn<?> aReturn : returns) {
				aReturn.insertBefore(createTraceLog(logger, buildExitLog(ctMethod)));
			}
		}

		// create exceptions point logging.
		if (loggable.exceptions()) {
			final List<CtCatch> catches =
					ctMethod.getElements(new TypeFilter<CtCatch>(CtCatch.class));
			for (CtCatch aCatch : catches) {
				aCatch.getBody().insertBegin(
						createErrorLog(logger, buildExceptionLog(ctMethod),
									   aCatch.getParameter().getSimpleName())
				);
			}

			final List<CtThrow> ctThrows = ctMethod
					.getElements(new TypeFilter<CtThrow>(CtThrow.class));
			for (CtThrow aThrow : ctThrows) {
				aThrow.insertBefore(
						createWarnLog(logger, buildWarnLog(ctMethod))
				);
			}
		}

		// create log field.
		final CtField<Logger> fieldLogger = createCtField(
				getFactory(), logger, getFactory().Type().createReference(Logger.class),
				"org.slf4j.LoggerFactory.getLogger(URLDownload.class)",
				ModifierKind.PRIVATE, ModifierKind.FINAL
		);
		ctMethod.getDeclaringType().addField(fieldLogger);
	}

	public CtStatement createTraceLog(String loggerName, String logMessage) {
		final CtIf ifEntry = getFactory().Core().createIf();
		ifEntry.setCondition(
				getFactory().Code()
							.<Boolean>createCodeSnippetExpression(loggerName + ".isTraceEnabled()")
		);
		ifEntry.setThenStatement(
				getFactory().Code().createCodeSnippetStatement(
						loggerName + ".trace(" + logMessage + ")"
				)
		);
		return ifEntry;
	}

	public CtStatement createWarnLog(String loggerName, String logMessage) {
		final CtIf ifWarn = getFactory().Core().createIf();
		ifWarn.setCondition(
				getFactory().Code()
							.<Boolean>createCodeSnippetExpression(loggerName + ".isWarnEnabled()")
		);
		ifWarn.setThenStatement(
				getFactory().Code().createCodeSnippetStatement(
						loggerName + ".warn(" + logMessage + ")"
				)
		);
		return ifWarn;
	}

	public CtStatement createErrorLog(String loggerName, String logMessage, String exceptionName) {
		return getFactory().Code().createCodeSnippetStatement(
				loggerName + ".error(" + logMessage + ", " + exceptionName + ")"
		);
	}

	public String buildEntryLog(CtMethod<?> ctMethod, Loggable loggable) {
		String entryLog = "\"" + ctMethod.getSimpleName() + "()";
		// skip arguments.
		if (loggable.skipArgs()) {
			entryLog += "[";
			for (CtField<?> field : ctMethod.getDeclaringType().getFields()) {
				entryLog += field.getSimpleName() + "=\" + " + field.getSimpleName() + " + \",";
			}
			for (CtParameter<?> parameter : ctMethod.getParameters()) {
				entryLog +=
						parameter.getSimpleName() + "=\" + " + parameter.getSimpleName() + " + \",";

			}
			entryLog += "]";
		}
		entryLog += ": entry\"";
		return entryLog;
	}

	public String buildWarnLog(CtMethod<?> ctMethod) {
		return "\"" + ctMethod.getSimpleName() + "(): interrupted.";
	}

	public String buildExitLog(CtMethod<?> ctMethod) {
		return "\"" + ctMethod.getSimpleName() + "(): exit in \" + (System.currentTimeMillis() - "
				+ entryTime + ") + \" ms\"";
	}

	public String buildExceptionLog(CtMethod<?> ctMethod) {
		return "\"" + ctMethod.getSimpleName() + "(): exception\"";
	}
}
