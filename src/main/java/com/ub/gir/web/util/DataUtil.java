/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.util;


import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.ub.gir.web.plugins.checkmarx.ToolPlugins;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;


/**
 * <p>資料處理轉換工具</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public abstract class DataUtil {

    private static final Logger logger = LoggerFactory.getLogger(DataUtil.class);

    /**
     * Constructor
     */
    private DataUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Convert object to byte array
     *
     * @param object
     * @return
     */
    public static byte[] fromObjectToByteArray(Serializable object) {
        return SerializationUtils.serialize(object);
    }

    /**
     * Convert byte array to object
     *
     * @param bytes
     * @return
     */
    public static Object fromByteArrayToObject(byte[] bytes) {
        return SerializationUtils.deserialize(bytes);
    }

    /**
     * @param <T>
     * @param source
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFromByte(byte[] source) {

        if (source==null) return null;

        try (ByteArrayInputStream bis = new ByteArrayInputStream(source);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            T value = null;

            if (ToolPlugins.checkAuthorization("authorization")) {
                value = (T) ois.readObject();
            }

            return value;

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * @param object
     * @return
     */
    public static byte[] toByte(Object object) {

        if (object==null) return null;

        byte[] data = null;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(object);
            oos.flush();
            data = bos.toByteArray();

            return data;

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }

    /**
     * @param obj
     * @return
     * @throws Exception
     */
    public static Map<String, Object> objectToMap(Object obj) throws Exception {

        if (obj==null) return null;

        boolean ret = true;
        Class<? extends Object> oo = obj.getClass();
        List<Class<? extends Object>> clazzs = new ArrayList<Class<? extends Object>>();

        while (ret) {

            clazzs.add(oo);

            oo = oo.getSuperclass();

            if (oo==null || oo==Object.class) break;

        }

        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0; i < clazzs.size(); i++) {

            Field[] declaredFields = clazzs.get(i).getDeclaredFields();

            for (Field field : declaredFields) {

                int mod = field.getModifiers();

                if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
                    continue;
                }

                field.setAccessible(true);

                Object value = field.get(obj);

                if (value==null) continue;

                map.put(field.getName(), value);

            }

        }

        return map;

    }

    /**
     * <p>Generate a set of random tokens</p>
     *
     * @return String
     */
    public static String generateToken(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * @param target sensitive data(like IMSI). e.g ('5476377362899153869')
     * @param mask   mask pattern . e.g. '##########*********'
     * @return value with mask e.g. '5476377362*********'
     */
    public static String mask(String target, String mask) {

        if (StringUtils.isBlank(mask)) {
            return target;
        }

        int maskStart = mask.indexOf("*");
        int maskEnd = mask.lastIndexOf("*") + 1;

        mask = StringUtils.rightPad("", maskEnd - maskStart, "*");

        return StringUtils.overlay(target, mask, maskStart, maskEnd);

    }

    /**
     * replace sensitive string (XSS)
     *
     * @param value
     * @return
     */
    public static String cleanXSS(String value) {

        if (value==null || value.length()==0) {
            return value;
        }

        value = value.replaceAll("<", "& lt;").replaceAll(">", "& gt;");
        value = value.replaceAll("\\(", "& #40;").replaceAll("\\)", "&#41;");
        value = value.replaceAll("'", "& #39;");
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        value = value.replaceAll("script", "");

        return value;

    }

    public static String preventLogForging(String originalDetails) {
        if(originalDetails == null) return null;
        return originalDetails
                .replace("\r\n", "\\CRLF")
                .replace("\n", "\\LF")
                .replace("\r", "\\CR");
    }

    public static String encodeResVal(String val) {
        return ESAPI.encoder().encodeForHTML(val);
    }

}
