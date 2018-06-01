package xc.com.apt.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;

import xc.com.apt.annotation.BindeView;

@AutoService(Processor.class)
@SupportedAnnotationTypes("xc.com.apt.annotation.BindeView")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class XAptProcessor extends AbstractProcessor{

    private Elements elementUtils;
    private Filer filer;
    private Types typeUtils;
    private Messager messager;
    private Map<String,ClassInfo> classInfoMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindeView.class);
        for (Element ele : elements) {
            /** 判断Element的类型是否是变量类型Element*/
            if(!(ele instanceof VariableElement)) {
                return false;
            }
            /** 第二种校验方式*/
            if(ele.getKind() != ElementKind.FIELD) {
                return false;
            }
            VariableElement variableElement = (VariableElement)ele;
            /** 获取封装Element，即变量所属的类的Element */
            TypeElement enclosingElement = (TypeElement) variableElement.getEnclosingElement();
            /** 获取全限定名 */
            String qualifiedName = enclosingElement.getQualifiedName().toString();
            /** 获取类名 */
            String className = enclosingElement.getSimpleName().toString();
            /** 获取注解类的包名，将生成后的代码放置一个包名下 */
            String pkgName = elementUtils.getPackageOf(enclosingElement).getQualifiedName().toString();

//            ClassName hostName = ClassName.bestGuess(qualifiedName);
//            /** 获取变量的名字 */
//            String variableName = variableElement.getSimpleName().toString();
//            /** 获取变量的类型 */
//            String variableType = variableElement.asType().toString();
            /** 获取注解的值，也就是View的id*/
            BindeView annotation = ele.getAnnotation(BindeView.class);
            int id = annotation.value();

            /** 将相同类的Element用map装起来*/
            ClassInfo classInfo = classInfoMap.get(qualifiedName);
            if(classInfo == null) {
                classInfo = new ClassInfo(pkgName,className);
                classInfoMap.put(qualifiedName,classInfo);
            }
            /** 用id作为键值，保证唯一性 */
            classInfo.getVariableElementMap().put(id,variableElement);

        }

        /** 收集完信息，生成代码*/
        for (String key:classInfoMap.keySet()) {
            ClassInfo classInfo = classInfoMap.get(key);
            ClassName hostName = ClassName.bestGuess(key);

            MethodSpec method = MethodSpec.methodBuilder("inject")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(void.class)
                    .addAnnotation(Override.class)
                    .addParameter(hostName, "host")
                    .addParameter(Object.class, "object")
                    .addCode( ""
                            + "if(object instanceof android.app.Activity){\n"
                            + getMethodCode(classInfo,"android.app.Activity")
                            + "}else {\n"
                            + getMethodCode(classInfo,"android.view.View")
                            + "}"
                            + ""

                    ).build();

            /**生成class相关信息*/
            ClassName interfaceName = ClassName.bestGuess("xc.com.apt.api.ViewInjector");
            TypeSpec classType = TypeSpec.classBuilder(classInfo.getClassName() + "_ViewInjector")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(method)
                    .addSuperinterface(ParameterizedTypeName.get(interfaceName, hostName))
                    .build();

            JavaFile build = JavaFile.builder(classInfo.getPkg(), classType)
                    .addFileComment("this file don't modify")
                    .build();
            try {
                build.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private String getMethodCode(ClassInfo classInfo, String hostType) {
        StringBuilder builder = new StringBuilder();
        for (int id: classInfo.getVariableElementMap().keySet()) {
            String variableName = classInfo.getVariableElementMap().get(id).getSimpleName().toString();
            String variableType = classInfo.getVariableElementMap().get(id).asType().toString();
            builder.append("\thost." + variableName + " = (" + variableType + ")(" + "((" + hostType + ")object).findViewById(" + id + "));\n");
        }
        return builder.toString();
    }

    private void analysisAnnotated(Element element) {
        StringBuilder builder = new StringBuilder()
                .append("package com.yuntao.annotationprocessor.generated;\n\n")
                .append("public class GeneratedClass {\n\n") // open class
                .append("\tpublic String getMessage() {\n") // open method
                .append("\t\treturn \"");


        // for each javax.lang.model.element.Element annotated with the CustomAnnotation
            String objectType = element.getSimpleName().toString();


            // this is appending to the return statement
            builder.append(objectType).append(" says hello!\\n");


        builder.append("\";\n") // end return
                .append("\t}\n") // close method
                .append("}\n"); // close class



        try { // write the file
            JavaFileObject source = processingEnv.getFiler().createSourceFile("com.yuntao.annotationprocessor.generated.GeneratedClass");


            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // Note: calling e.printStackTrace() will print IO errors
            // that occur from the file already existing after its first run, this is normal
        }

    }
}
