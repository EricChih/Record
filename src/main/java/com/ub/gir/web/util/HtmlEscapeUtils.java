package com.ub.gir.web.util;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class HtmlEscapeUtils {
    public static <T> List<T> escapeList(List<T> list) throws IllegalAccessException {
        for (int i = 0; i < list.size(); i++) {
            list.set(i, escapeObject(list.get(i)));
        }
        return list;
    }

    public static List<String> escapeStringList(List<String> list){
        return list.stream().map(HtmlUtils::htmlEscape).collect(Collectors.toList());
    }

    public static <T> TreeMap<String, T> escapeTreeMap(TreeMap<String, T> map) throws IllegalAccessException {
        for (String key : map.keySet()) {
            T value = map.get(key);
            if (value instanceof String) {
                String stringValue = (String) value;
                map.put(key, (T) HtmlUtils.htmlEscape(stringValue));
            }
        }
        return map;
    }

    public static <T> T escapeObject(T obj) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType().equals(String.class)) {
                String value = (String) field.get(obj);
                if (value != null) {
                    field.set(obj, HtmlUtils.htmlEscape(value));
                }
            }
        }
        return obj;
    }

    public static String escapeString(String str){
        return str == null ? null : HtmlUtils.htmlEscape(str);
    }
}