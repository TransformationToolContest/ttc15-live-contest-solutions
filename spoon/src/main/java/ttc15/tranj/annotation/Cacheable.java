package ttc15.tranj.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Makes a method response cacheable in memory for some time.
 *
 * <p>
 * For example, this {@code load()} method loads some data from the network keeps them in cache for 5 seconds:
 *
 * <pre>
 * &#064;Cacheable(lifetime = 5000)
 * String load() throws IOException {
 *   ...
 * }
 * </pre>
 * 
 * This annotation is only applicable to methods that takes no arguments and
 * returns a value (i.e. do not return {@code void}). Either {@code lifetime} or
 * {@code forever} must be declared.
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {

  /**
   * Lifetime of an object in cache, in milliseconds. Must be > 0.
   */
  int lifetime() default 1;

  /**
   * Keep in cache forever.
   */
  boolean forever() default false;

}
