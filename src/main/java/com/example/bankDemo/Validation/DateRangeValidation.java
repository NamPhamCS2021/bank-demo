//import jakarta.validation.Constraint;
//import jakarta.validation.ConstraintValidator;
//import jakarta.validation.Payload;
//import org.springframework.stereotype.Component;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
//@Target({ElementType.TYPE})
//@Retention(RetentionPolicy.RUNTIME)
//@Constraint(validatedBy = DateRangeValidator.class)
//public @interface ValidDateRange {
//    String message() default "Start date must be before end date";
//    Class<?>[] groups() default {};
//    Class<? extends Payload>[] payload() default {};
//    String start();
//    String end();
//}
//
//@Component
//public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {
//    // Implementation
//}