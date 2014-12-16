package org.motechproject.mds.annotations;

import org.motechproject.mds.event.CrudEventType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>CrudEvents</code> annotation is used by developers to specify which
 * CRUD operations should send Motech events. CrudEvents value is an array of
 * one or more values specified in {@link org.motechproject.mds.event.CrudEventType}
 * enum, that is: CREATE, UPDATE, DELETE. There are also two special values - ALL, NONE.
 * When provided, all CRUD operations are enabled/disabled for entity, regardless of presence
 * of other values.
 * <p/>
 * This annotation is processed by
 * {@link org.motechproject.mds.annotations.internal.CrudEventsProcessor}
 * and can be applied only to class which is also annotated with
 * {@link Entity}. It has no effect otherwise.
 *
 * @see org.motechproject.mds.event.CrudEventType
 * @see org.motechproject.mds.annotations.internal.CrudEventsProcessor
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CrudEvents {
    CrudEventType[] value();
}
