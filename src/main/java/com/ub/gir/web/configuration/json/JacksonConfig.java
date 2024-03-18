/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web.configuration.json;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.ub.gir.web.configuration.json.serialization.BigDecimalToStringSerializer;


import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;


/**
 * <p>定義 json bean</p>
 *
 * @author : Seimo_Zhan
 * @version :
 * @Date ： 2023/1/10
 */
@Configuration
@ConditionalOnClass({ObjectMapper.class})
public class JacksonConfig {

    /**
     * TimeZone format
     */
    private static final String TIME_ZONE = "GMT+8:00";

    /**
     * DateTime format
     */
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * Date format
     */
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * Time format
     */
    private static final String TIME_PATTERN = "HH:mm:ss";

    @Bean
    @ConditionalOnMissingBean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder ()
    {

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();

        builder.timeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
        builder.simpleDateFormat(DATETIME_PATTERN);
        builder.serializationInclusion(JsonInclude.Include.NON_NULL);

        builder.indentOutput(true);
        builder.failOnEmptyBeans(false);
        builder.failOnUnknownProperties(false);

        builder.featuresToDisable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.featuresToDisable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);

        builder.featuresToEnable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature());
        builder.featuresToEnable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        builder.featuresToEnable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        builder.featuresToEnable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_PATTERN)));
        builder.serializerByType(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        builder.serializerByType(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));

        builder.deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_PATTERN)));
        builder.deserializerByType(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        builder.deserializerByType(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)));

        builder.modules(createJavaTimeModule());
        builder.modules(new ParameterNamesModule());
        //builder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE) ;

        return builder ;

    }

    private Module createJavaTimeModule ()
    {

        JavaTimeModule module = new JavaTimeModule () ;

        // When the sequence is changed to json, all longs are changed to strings. Because the number type in js cannot contain all java long values
        module.addSerializer(long.class, ToStringSerializer.instance);
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);

        // When serializing Long and BigInteger, convert to String
        module.addSerializer(BigInteger.class, ToStringSerializer.instance);
        //module.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        module.addSerializer(BigDecimal.class, BigDecimalToStringSerializer.instance);

        return module ;

    }

}
