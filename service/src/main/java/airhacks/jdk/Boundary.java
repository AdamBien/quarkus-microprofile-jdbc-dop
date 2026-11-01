package airhacks.jdk;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Stereotype;
import jakarta.transaction.Transactional;

/// A business component's boundary: application scoped, every business method runs in a transaction the
/// container opens and commits. The controls' connections enlist in it, so several control calls in one
/// boundary method are all or nothing.
@Stereotype
@ApplicationScoped
@Transactional
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Boundary {
}
