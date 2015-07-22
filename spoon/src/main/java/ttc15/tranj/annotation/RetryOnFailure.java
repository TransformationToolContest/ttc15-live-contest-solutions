package ttc15.tranj.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Retry the method in case of an exception.
 *
 * <p>
 * For example, this {@code load()} method will retry to load the URL content if
 * it fails at the first attempt:
 *
 * <pre>
 * &#064;RetryOnFailure(attempts = 2)
 * String load(URL url) throws IOException {
 *   ...
 * }
 * </pre>
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RetryOnFailure {

  /**
   * How many times to retry. Must be > 0.
   */
  int attempts() default 1;

  /**
   * Delay between attempts, in milliseconds. Must be >= 0.
   */
  long delay() default 0;

  /**
   * Which exceptions to ignore (in case of what exception types stop trying).
   * These exceptions must be either runtime exceptions or subclasses of the
   * method declared thrown checked exceptions.
   */
  Class<? extends Throwable>[] escalate() default {};

  /**
   * When to retry (in case of what exception types).
   */
  Class<? extends Throwable>[] retry() default { Throwable.class };

}
