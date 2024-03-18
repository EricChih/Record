/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.util;


import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import org.apache.commons.lang3.SerializationUtils;


/**
 * <p>Jackson 轉換工具</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
public abstract class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * Constructor
     */
    private JsonUtil() {
        throw new IllegalStateException("JsonUtil utility class");
    }

    /**
     * <p>Convert object to class</p>
     *
     * @param fromObject
     * @param toClazz
     * @return T2
     */
    public static <T1, T2> T2 convertObjectToClass(T1 fromObject, Class<T2> toClazz) {
        return OBJECT_MAPPER.convertValue(fromObject, toClazz);
    }

    /**
     * <p>Convert object to object ( collection )</p>
     *
     * @param fromObject
     * @param toTypeRef
     * @return T2
     */
    public static <T1, T2> T2 convertObjectToTypeRef(T1 fromObject, TypeReference<T2> toTypeRef) {
        return OBJECT_MAPPER.convertValue(fromObject, toTypeRef);
    }

    /**
     * <p>Convert json to object</p>
     *
     * @param json
     * @param clazz
     * @return T
     */
    public static <T> T convertJsonToClass(String json, Class<T> clazz) {

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The given json value: " + json + " cannot be transformed to Json object");
        }

    }

    /**
     * <p>Convert json to object ( collection )</p>
     *
     * @param json
     * @param toTypeRef
     * @return T
     */
    public static <T> T convertJsonToTypeRef(String json, TypeReference<T> toTypeRef) {

        try {
            return OBJECT_MAPPER.readValue(json, toTypeRef);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The given json value: " + json + " cannot be transformed to json type reference object", e);
        }

    }

    /**
     * <p>Convert stream to class</p>
     *
     * @param inputStream
     * @param clazz
     * @return T
     */
    public static <T> T convertStreamToClass(InputStream inputStream, Class<T> clazz) {

        try {
            return OBJECT_MAPPER.readValue(inputStream, clazz);
        } catch (IOException e) {
            throw new IllegalArgumentException("The given stream value: " + inputStream + " cannot be transformed to json object", e);
        }

    }

    /**
     * <p>Convert stream to object ( collection )</p>
     *
     * @param inputStream
     * @param toTypeRef
     * @return T
     */
    public static <T> T convertStreamToTypeRef(InputStream inputStream, TypeReference<T> toTypeRef) {

        try {
            return OBJECT_MAPPER.readValue(inputStream, toTypeRef);
        } catch (IOException e) {
            throw new IllegalArgumentException("The given stream value: " + inputStream + " cannot be transformed to json type reference object", e);
        }

    }

    /**
     * <p>Convert to json</p>
     *
     * @param obj
     * @return String
     */
    public static String convertObjectToJson(Object obj) {

        try {
//			return OBJECT_MAPPER.writerWithDefaultPrettyPrinter ().writeValueAsString ( obj ) ;
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The given object value: " + obj + " cannot be transformed to a Json String");
        }

    }

    /**
     * <p>Convert to json node</p>
     *
     * @param json
     * @return JsonNode
     */
    public static JsonNode convertJsonToJsonNode(String json) {

//		return JsonNodeFactory.instance.objectNode () ;
        try {
            OBJECT_MAPPER.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("The given json value: " + json + " cannot be transformed to Json Node", e);
        }

    }

    /**
     * <p>Convert to json node</p>
     *
     * @param obj
     * @return T2
     */
    public static <T1, T2 extends JsonNode> T2 convertToJsonNode(T1 obj) {
        OBJECT_MAPPER.setNodeFactory(JsonNodeFactory.withExactBigDecimals(true));
        return (T2) OBJECT_MAPPER.valueToTree(obj);
    }

    /**
     * <p>clone to object</p>
     *
     * @param value
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(T value) {
        return (value instanceof Serializable) ? (T) SerializationUtils.clone((Serializable) value) : convertJsonToClass(convertObjectToJson(value), ((Class<T>) value.getClass()));
    }

    /**
     * @author : Seimo_Zhan
     */
    public static class XssStringJsonSerializer extends JsonSerializer<String> {

        /* (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#handledType()
         */
        @Override
        public Class<String> handledType() {
            return String.class;
        }

        /* (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

            if (value != null) {
                String encodedValue = DataUtil.cleanXSS(value);
                jsonGenerator.writeString(encodedValue);
            }

        }

    }

    /**
     * @author : Seimo_Zhan
     */
    public static class LongToStringJsonSerializer extends JsonSerializer<Long> {

        /* (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#handledType()
         */
        @Override
        public Class<Long> handledType() {
            return Long.class;
        }

        /* (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(Long value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException, JsonProcessingException {

            if (value != null) {
                String encodedValue = String.valueOf(value);
                jsonGenerator.writeString(encodedValue);
            }

        }

    }

    /**
     * @author : Seimo_Zhan
     */
    public static class BigIntegerToStringJsonSerializer extends JsonSerializer<BigInteger> {

        /* (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#handledType()
         */
        @Override
        public Class<BigInteger> handledType() {
            return BigInteger.class;
        }

        /* (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(BigInteger value, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException, JsonProcessingException {

            if (value != null) {
                String encodedValue = String.valueOf(value);
                jsonGenerator.writeString(encodedValue);
            }

        }

    }

    /**
     * @author : Seimo_Zhan
     */
    public static class XssStringJsonDeserializer extends JsonDeserializer<String> {

        /* (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonDeserializer#handledType()
         */
        @Override
        public Class<String> handledType() {
            return String.class;
        }

        /* (non-Javadoc)
         * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser, com.fasterxml.jackson.databind.DeserializationContext)
         */
        @Override
        public String deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException, JsonProcessingException {

            String value = parser.getValueAsString();

            return DataUtil.cleanXSS(value);

        }

    }

}
