package cs203t10.ryver.market.util;

import java.beans.FeatureDescriptor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

public final class CustomBeanUtils extends BeanUtils {

    public static void copyNonNullProperties(final Object source, final Object target) throws BeansException {
        BeanUtils.copyProperties(source, target, getNullPropertyNames(source));
    }

    private static String[] getNullPropertyNames(final Object source) {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        return Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName ->
                        wrappedSource.getPropertyValue(propertyName) == null)
                .toArray(String[]::new);
    }

    public static boolean nonNullIsSubsetOf(final Object source, final Object target) throws BeansException {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        final BeanWrapper wrappedTarget = new BeanWrapperImpl(target);
        Set<String> sourceNonNullProperties = Stream.of(wrappedSource.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .filter(propertyName ->
                        wrappedSource.getPropertyValue(propertyName) != null)
                .collect(Collectors.toSet());
        Set<String> targetProperties = Stream.of(wrappedTarget.getPropertyDescriptors())
                .map(FeatureDescriptor::getName)
                .collect(Collectors.toSet());
        return targetProperties.containsAll(sourceNonNullProperties);
    }

    public static <T> T getPropertyValueWithName(
            final Object source, final String propertyName, final Class<T> type)
            throws BeansException {
        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        try {
            @SuppressWarnings("unchecked")
            T value = (T) wrappedSource.getPropertyValue(propertyName);
            return value;
        } catch (Exception e) {
            return null;
        }
    }

}

