package ttc15.tranj.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes a method loggable via {@link com.jcabi.log.Logger}.
 *
 * <p>
 * For example, this {@code load()} method produces a log line on every call:
 *
 * <pre>
 * &#64;Loggable
 * String load(String resource) {
 *   ...
 * }
 * </pre>
 *
 * At least one of the {@code entry}, {@code exit} or {@code exceptions} must be
 * set to {@code true}. If applied on a class, inherited methods are ignored
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Loggable {

  /**
   * Log method entry?
   */
  boolean entry() default true;

  /**
   * Log method exit?
   */
  boolean exit() default true;

  /**
   * Log method exceptions?
   */
  boolean exceptions() default true;

  /**
   * Skip logging of arguments?
   */
  boolean skipArgs() default false;

}
