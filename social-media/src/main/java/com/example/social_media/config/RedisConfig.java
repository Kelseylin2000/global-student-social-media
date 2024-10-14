package com.example.social_media.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import io.lettuce.core.TimeoutOptions;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    @Value("${spring.redis.timeout}")
    private long timeout;

    @Value("${spring.redis.ssl}")
    private boolean sslOrNot;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        
        LettuceClientConfiguration clientConfig;
        RedisStandaloneConfiguration redisStandaloneConfiguration;

        if(sslOrNot){
            ClientOptions clientOptions = ClientOptions.builder()
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .timeoutOptions(TimeoutOptions.builder()
                    .fixedTimeout(Duration.ofMillis(timeout))
                    .build())
                .sslOptions(SslOptions.builder()
                    .jdkSslProvider()
                    .build())
                .build();

            clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofMillis(timeout))
                .useSsl()
                .build();

            redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
            redisStandaloneConfiguration.setPassword(redisPassword);
        }else{
            ClientOptions clientOptions = ClientOptions.builder()
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .timeoutOptions(TimeoutOptions.builder()
                    .fixedTimeout(Duration.ofMillis(timeout))
                    .build())
                .build();

            clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .commandTimeout(Duration.ofMillis(timeout))
                .build();

            redisStandaloneConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
        }

        return new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setKeySerializer(new StringRedisSerializer()); 
        template.setValueSerializer(jackson2JsonRedisSerializer); 
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();

        return template;
    }
}