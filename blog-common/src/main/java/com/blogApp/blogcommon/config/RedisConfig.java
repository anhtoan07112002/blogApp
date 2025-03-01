package com.blogApp.blogcommon.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${app.service.name:unknown}")
    private String serviceName;
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.debug("Cấu hình Redis connection factory: host={}, port={}", redisHost, redisPort);
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisHost);
        configuration.setPort(redisPort);
        if (!redisPassword.isEmpty()) {
            configuration.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        log.debug("Cấu hình ObjectMapper cho Redis serialization");
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.registerModule(new JavaTimeModule());

        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        
        log.debug("ObjectMapper đã được cấu hình: defaultTyping=NON_FINAL, typeInfoAs=PROPERTY");
        return mapper;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, 
                                                      @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        log.debug("Cấu hình RedisTemplate với GenericJackson2JsonRedisSerializer");
        
        // Tạo serializer với wrapper để logging
        GenericJackson2JsonRedisSerializer originalSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        RedisSerializer<Object> loggingSerializer = new RedisSerializer<Object>() {
            @Override
            public byte[] serialize(Object source) {
                if (source == null) {
                    return null;
                }
                try {
                    log.debug("Serialize object: type={}", source.getClass().getName());
                    return originalSerializer.serialize(source);
                } catch (Exception e) {
                    log.error("Lỗi serialize: type={}, error={}", source.getClass().getName(), e.getMessage(), e);
                    throw e;
                }
            }

            @Override
            public Object deserialize(byte[] bytes) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                try {
                    Object result = originalSerializer.deserialize(bytes);
                    log.debug("Deserialize thành công: result={}", 
                             (result != null ? result.getClass().getName() : "null"));
                    return result;
                } catch (Exception e) {
                    log.error("Lỗi deserialize: error={}", e.getMessage(), e);
                    throw e;
                }
            }
        };

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(loggingSerializer);

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(loggingSerializer);

        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        log.debug("RedisTemplate đã được cấu hình hoàn tất");
        return template;
    }
}
