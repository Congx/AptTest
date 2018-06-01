package xc.com.apt.processor;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.VariableElement;

/**
 * @author 01373506
 * @date 2018/6/1
 * 存放被注解的类的信息
 */
public class ClassInfo {

    public String pkg;
    public String className;

    public Map<Integer,VariableElement> variableElementMap = new HashMap<>();

    public ClassInfo(String pkg, String className) {
        this.pkg = pkg;
        this.className = className;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<Integer, VariableElement> getVariableElementMap() {
        return variableElementMap;
    }

    public void setVariableElementMap(Map<Integer, VariableElement> variableElementMap) {
        this.variableElementMap = variableElementMap;
    }
}
