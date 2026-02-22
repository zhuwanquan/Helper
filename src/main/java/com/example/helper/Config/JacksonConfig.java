package com.example.helper.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE) // 使用 snake_case 命名
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // 日期序列化为字符串
                .featuresToEnable(SerializationFeature.INDENT_OUTPUT) // 格式化输出（生产环境可关闭）
                .modules(new JavaTimeModule()) // 支持 Java 8 时间类型
                .build();
    }
}
