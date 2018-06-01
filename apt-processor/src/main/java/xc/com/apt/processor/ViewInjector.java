package xc.com.apt.processor;

/**
 * @author 01373506
 * @date 2018/5/28
 */
public interface ViewInjector<T> {
    void inject(T host,Object object);
}
