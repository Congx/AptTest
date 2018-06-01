package xc.com.apt.api;

/**
 * @author 01373506
 * @date 2018/5/28
 */
public interface ViewInjector<T> {
    void inject(T host, Object object);
}
