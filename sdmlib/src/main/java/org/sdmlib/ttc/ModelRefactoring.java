package org.sdmlib.ttc;
import org.junit.Test;
import org.sdmlib.codegen.Parser;
import org.sdmlib.models.classes.Annotation;
import org.sdmlib.models.classes.ClassModel;
import org.sdmlib.models.classes.Clazz;
import org.sdmlib.models.classes.Method;
import org.sdmlib.models.classes.logic.GenClass;
import org.sdmlib.models.classes.templates.ReplaceText;
import org.sdmlib.models.classes.templates.Template;
import org.sdmlib.models.classes.templates.TemplateListener;
import org.sdmlib.models.classes.templates.TemplateResult;

import de.uniks.networkparser.graph.GraphAnnotation;

public class ModelRefactoring {

	public static void main(String[] args) {
		String input = "src";
		String output = "gen";
		for(String item : args) {
			if(item.equalsIgnoreCase("-?")) {
				System.out.println("SDMLib Solution");
				System.out.println("java -jar ttc15-live-contest.jar -src=<input Directory> -gen=<output Directory>");
				return;
			}
			String searchItem = item.toLowerCase();
			if(searchItem.startsWith("-src=")) {
				input = searchItem.substring(5);
			}
			if(searchItem.startsWith("-gen=")) {
				output = searchItem.substring(5);
			}
		}
		new ModelRefactoring().transform(input, output);
	}
	
	private void transform(String input, String output) {
		ClassModel model = new ClassModel();
		// WORKAROUND
		String packageName = input.replaceAll("\\\\", ".");
		packageName = input.replaceAll("/", ".");
		
		model.getGenerator().updateFromCode("", packageName);
		model.dumpHTML("ParsedClassModel");
		for(Clazz clazz : model.getClasses()) {
			for(Method method : clazz.getMethods()) {
				for(Annotation annotation : method.getAnnotations()) {
					System.out.println(annotation.getName());
					GraphAnnotation graphAnnotation = GraphAnnotation.create(annotation.getName());
//					if(graphAnnotation.getName().equals("@RetryOnFailure")) {
					method.setBody(addRetrying(method, graphAnnotation));
//					}
				}
				System.out.println(method);
			}
		}
		model.generate(output);
	}

	public String addRetrying(Method method, GraphAnnotation annotation) {
		Clazz clazz = method.getClazz();
		
		GraphAnnotation retryOnFailure = annotation.getAnnotation("@RetryOnFailure");
		GraphAnnotation loggable = annotation.getAnnotation("@Loggable");
		GraphAnnotation cacheable = annotation.getAnnotation("@Cacheable");
		
		
		GenClass genClazz = clazz.getClassModel().getGenerator().getOrCreate(clazz);
		Template template = new Template();
		template.withTemplate(
				"{{start}}\n" +
				"{{body}}\n" +
				"{{exit}}\n" + 
				"{{exitSuccess}}");
		
		// RetryOnFailure
		ReplaceText bodyRetry = new ReplaceText("body").withTemplate(template);
		bodyRetry.withActive(retryOnFailure != null);
		bodyRetry.withValue(
				"// added retry counter\n" +
				"int __retryCount = 0;\n" +
				"\n"+
				"// added loop\n"+
				"while (true) {\n"+
				"    try {\n"+
				"    // the content of the original method\n"+
				"    {{BodyText}}\n"+
				"    } catch ({{EscalationException}} e) {\n"+
				"        // added escalation cases\n"+
				"        throw e;\n"+
				"    } catch ({{RetryingException}} e) {\n"+
				"        // added retry cases\n"+
				"        __retryCount += 1;\n"+
				"\n"+
				"       if (__retryCount > {{RetryingCount}}) {\n"+
				"           throw e;\n"+
				"       } else {\n"+
				"           // added delay\n"+
				"           try {\n" +
				"              Thread.sleep({{sleeping}});\n"+
				"           } catch (InterruptedException e1) {\n"+
				"              throw e;\n"+
				"           }\n"+
				"       }\n"+
				"    }\n" +
				"}");
		
		ReplaceText startCacheable = new ReplaceText("start").withTemplate(template);
		startCacheable.withActive(cacheable != null);
		startCacheable.withRunnable(new TemplateListener() {
			@Override
			public void run(ReplaceText replaceText, int pos, String text) {
				Template attrAccess = new Template(Parser.ATTRIBUTE+":__{{name}}CacheLastAccessed");
				attrAccess.withTemplate("//	added bookkeeping fields\n"
										+ "     private long __{{name}}CacheLastAccessed = 0;");
				attrAccess.insert(genClazz.getParser(), "name", method.getName());
				Template attrContent = new Template(Parser.ATTRIBUTE+":__{{name}}CacheContent");
				attrContent.withTemplate("//	added bookkeeping fields\n"
										+ "     private byte[] __{{name}}CacheContent = null;");
				attrContent.insert(genClazz.getParser(), "name", method.getName());
			}
		});
		startCacheable.withValue("// added condition\n"+
					   "if (System.currentTimeMillis() - __{{name}}CacheLastAccessed < 1000 && __{{name}}CacheContent != null) {\n" +
					   "    return __{{name}}CacheContent;\n" +
					   "}\n" +
					   "{{start}}");
		
		
		ReplaceText exitSuccessCacheable = new ReplaceText("exitSuccess").withTemplate(template);
		exitSuccessCacheable.withActive(cacheable != null);
		startCacheable.withValue("// added\n" +
								"__{{name}}CacheContent = buffer.toByteArray();\n" +
								"__{{name}}CacheLastAccessed = System.currentTimeMillis();\n" +
								"{{exitSuccess}}");
		
		
		ReplaceText startLoggable = new ReplaceText("start").withTemplate(template);
		startLoggable.withActive(loggable != null);
		startLoggable.withValue("// added entry point logging\n" +
								"long __entryTime = System.currentTimeMillis();\n" +
								"if (__logger.isTraceEnabled()) {\n" +
								"   __logger.trace(String.format(\"{{name}}() [url=]: entry\", url));\n" +
								"}\n" +
								"{{start}}");
		startLoggable.withImport("org.slf4j.Logger");
		startLoggable.withRunnable(new TemplateListener() {
			@Override
			public void run(ReplaceText replaceText, int pos, String text) {
				Template attrLogger = new Template(Parser.ATTRIBUTE+":__logger");
				attrLogger.withTemplate("// added logger\n"
										+ "     private final org.slf4j.Logger __logger = org.slf4j.LoggerFactory.getLogger({{ClassName}}.class)");
				attrLogger.insert(genClazz.getParser(), "ClassName", clazz.getName());
			}
		});
		
		ReplaceText exitLoggable = new ReplaceText("exit").withTemplate(template);
		exitLoggable.withActive(loggable != null);
		exitLoggable.withValue("if (__logger.isTraceEnabled()) {\n" +
								"  __logger.trace(String.format(\"{{name}}: exit in %d ms\", System.currentTimeMillis() - __entryTime));\n" +
								"}\n" +
								"{{exitSuccess}}");
		
		ReplaceText exceptionLoggable = new ReplaceText("exception").withTemplate(template);
		exceptionLoggable.withActive(loggable != null);
		exceptionLoggable.withValue("// added exception handling\n" +
							   "__logger.error(\"{{name}}(): exception\", e);\n" +
								"{{exception}}");
		
		System.out.println(retryOnFailure.getValue("escalate", ""));
		System.out.println(retryOnFailure.getValue("retry"));
		System.out.println(retryOnFailure.getValue("attempts"));
		System.out.println(retryOnFailure.getValue("delay"));

		// Remove Placeholder
		new ReplaceText("exitSuccess").withTemplate(template);
		new ReplaceText("body").withTemplate(template);
		new ReplaceText("exception").withTemplate(template);
		new ReplaceText("exit").withTemplate(template);
		new ReplaceText("start").withTemplate(template);
		new ReplaceText("name").withTemplate(template).withValue(method.getName());
		
		TemplateResult values = template.execute(
		   "BodyText", method.getBody(), 
		   "EscalationException", retryOnFailure.getValue("escalate"),
		   "RetryingException", retryOnFailure.getValue("retry"),
		   "RetryingCount", retryOnFailure.getValue("attempts"),
		   "sleeping", retryOnFailure.getValue("delay"));
		
		return values.getTextValue();
	}
	
	@Test
	public void testRefactoring() {
		ClassModel model = new ClassModel();
		model.getGenerator().updateFromCode("src/ttc15-tranj/src/main/java", "ttc15.tranj.test");
		model.dumpHTML("ParsedClassModel");
		for(Clazz clazz : model.getClasses()) {
			for(Method method : clazz.getMethods()) {
				for(Annotation annotation : method.getAnnotations()) {
					System.out.println(annotation.getName());
					GraphAnnotation graphAnnotation = GraphAnnotation.create(annotation.getName());
					if(graphAnnotation.getName().equals("@RetryOnFailure")) {
						method.setBody(addRetrying(method, graphAnnotation));
					}
				}
				System.out.println(method);
			}
//			GenClass genClazz = model.getGenerator().getOrCreate(clazz);
//			Parser parser = genClazz.getParser();
//			SimpleKeyValueList<String, SymTabEntry> symTab = parser.getSymTab();
//
//			for(int i=0;i<symTab.size();i++) {
//				SymTabEntry valueByIndex = symTab.getValueByIndex(i);
//				if(Parser.METHOD.equalsIgnoreCase(valueByIndex.getKind())) {
//					String annotations = valueByIndex.getAnnotations();
//					Method method = getMethod(clazz, symTab.getKeyByIndex(i));
//					System.out.println(annotations);
//					System.out.println(method);
//				}
//			}
		}
	}
	
//	public Method getMethod(Clazz clazz, String signature) {
//		for(Method method : clazz.getMethods()) {
//			if(signature.equalsIgnoreCase(getMethodParserSignature(method))) {
//				return method;
//			}
//		}
//		return null;
//	}

//	public String getMethodParserSignature(Method method) {
//		StringBuilder sb = new StringBuilder();
//
//		sb.append("method:" + method.getName() + "(");
//		ParameterSet parameters = method.getParameter();
//		for (int i = 0; i < parameters.size(); i++) {
//			sb.append(parameters.get(i).getType().getValue());
//			if (i < parameters.size() - 1) {
//				sb.append(", ");
//			}
//		}
//		sb.append(")");
//		return sb.toString();
//	}
	   
}
