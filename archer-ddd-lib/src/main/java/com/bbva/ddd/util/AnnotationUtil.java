package com.bbva.ddd.util;

import com.bbva.ddd.domain.Handler;
import com.bbva.ddd.domain.aggregates.AggregateBase;
import com.bbva.ddd.domain.aggregates.annotations.Aggregate;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AnnotationUtil {

    public static <C extends Annotation> List<Class> findAllAnnotatedClasses(final Class<C> annotation, final Handler handler) {
        final String mainPackage = handler.getClass().getCanonicalName().split("\\.")[0];

        return findAnnotations(mainPackage, annotation);
    }

    public static <C extends Annotation> List<Class> findAllAnnotatedClasses(final Class<C> annotation) {
        final String mainPackage = AnnotationUtil.class.getCanonicalName().split("\\.")[0];
        return findAnnotations(mainPackage, annotation);
    }

    public static Map<String, Class<? extends AggregateBase>> mapAggregates(final Handler handler) {
        final Map<String, Class<? extends AggregateBase>> aggregatesMap = new HashMap<>();
        final List<Class> classes = AnnotationUtil.findAllAnnotatedClasses(Aggregate.class, handler);

        for (final Class<?> aggregateClass : classes) {
            final Aggregate aggregateAnnotation = aggregateClass.getAnnotation(Aggregate.class);
            final String baseName = aggregateAnnotation.baseName();
            aggregatesMap.put(baseName, aggregateClass.asSubclass(AggregateBase.class));
        }

        return aggregatesMap;
    }

    private static <C extends Annotation> List<Class> findAnnotations(final String mainPackage, final Class<C> annotation) {
        List<Class> handlers = findClassesInPackage(mainPackage, annotation);
        if (handlers.isEmpty()) {
            handlers = findInAllPackages(annotation);
        }
        return handlers;
    }

    public static <C extends Annotation> List<Class> findInAllPackages(final Class<C> annotation) {
        final List<Class> handlers = new ArrayList<>();
        final Package[] packages = Package.getPackages();
        for (final Package packageLoaded : packages) {
            if (!packageLoaded.getName().matches("^(org|sun|java|jdk).*")) {
                handlers.addAll(findClassesInPackage(packageLoaded.getName(), annotation));
            }
        }
        return handlers;
    }

    private static <C extends Annotation> List<Class> findClassesInPackage(final String packageToFind, final Class<C> annotation) {
        final List<Class> handlers = new ArrayList<>();
        final Reflections ref = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageToFind, ClasspathHelper.contextClassLoader(),
                        ClasspathHelper.staticClassLoader()))
                .filterInputsBy(new FilterBuilder().include(".+\\.class")));

        handlers.addAll(ref.getTypesAnnotatedWith(annotation));
        return handlers;
    }

}
