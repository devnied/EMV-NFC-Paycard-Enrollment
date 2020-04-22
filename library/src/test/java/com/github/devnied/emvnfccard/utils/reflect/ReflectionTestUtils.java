package com.github.devnied.emvnfccard.utils.reflect;

public class ReflectionTestUtils {

    public static <T> T invokeMethod(Class<?> targetClass, String name, Object... args) {
        return invokeMethod(null, targetClass, name, args);
    }

    public static <T> T invokeMethod( Object targetObject, String name,
                                      Object... args) {
        return invokeMethod(targetObject, null, name, args);
    }

    /**
     * Invoke the method with the given {@code name} on the provided
     * {@code targetObject}/{@code targetClass} with the supplied arguments.
     * <p>This method traverses the class hierarchy in search of the desired
     * method. In addition, an attempt will be made to make non-{@code public}
     * methods <em>accessible</em>, thus allowing one to invoke {@code protected},
     * {@code private}, and <em>package-private</em> methods.
     * @param targetObject the target object on which to invoke the method; may
     * be {@code null} if the method is static
     * @param targetClass the target class on which to invoke the method; may
     * be {@code null} if the method is an instance method
     * @param name the name of the method to invoke
     * @param args the arguments to provide to the method
     * @return the invocation result, if any
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod( Object targetObject, Class<?> targetClass, String name,
                                     Object... args) {
        try {
            MethodInvoker methodInvoker = new MethodInvoker();
            methodInvoker.setTargetObject(targetObject);
            if (targetClass != null) {
                methodInvoker.setTargetClass(targetClass);
            } else {
                methodInvoker.setTargetClass(targetObject.getClass());
            }
            methodInvoker.setTargetMethod(name);
            methodInvoker.setArguments(args);
            methodInvoker.prepare();

            return (T) methodInvoker.invoke();
        }
        catch (Exception ex) {
            throw new IllegalStateException("Should never get here");
        }
    }
}
