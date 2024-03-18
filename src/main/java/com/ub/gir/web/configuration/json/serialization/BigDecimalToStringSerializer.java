/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.json.serialization;


import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;


/**
 * <p>json 額外序列化物件</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@JacksonStdImpl
public class BigDecimalToStringSerializer extends ToStringSerializer {

    public static final BigDecimalToStringSerializer instance = new BigDecimalToStringSerializer();

    public BigDecimalToStringSerializer() {
        super(Object.class);
    }

    public BigDecimalToStringSerializer(Class<?> handledType) {
        super(handledType);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Object value) {

        if (value == null) {
            return true;
        }

        String str = ((BigDecimal) value).stripTrailingZeros().toPlainString();
        return str.isEmpty();

    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeString(((BigDecimal) value).stripTrailingZeros().toPlainString());
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
        return createSchemaNode("string", true);
    }

    @Override
    public void serializeWithType(Object value, JsonGenerator gen,
                                  SerializerProvider provider, TypeSerializer typeSer)
            throws IOException {
        // no type info, just regular serialization
        serialize(value, gen, provider);
    }

}
