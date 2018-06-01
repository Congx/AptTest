package xc.com.apt.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 01373506
 * @date 2018/5/31
 */
public class XcInject {

    public static void bind(Object host) {
        String name = host.getClass().getName();
        try {
            Class<?> aClass = Class.forName(name + "_ViewInjector");
            Method method = aClass.getMethod("inject", host.getClass(), Object.class);
            method.invoke(aClass.newInstance(),host,host);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }
}
