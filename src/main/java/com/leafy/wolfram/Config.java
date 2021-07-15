package com.leafy.wolfram;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineMessagingClientBuilder;
import com.linecorp.bot.client.LineSignatureValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
public class Config {
    @Autowired
    private Environment env;

    private final String CHANNEL_SECRET = "com.linecorp.channel_secret";
    private final String CHANNEL_ACCESS_TOKEN = "com.linecorp.channel_access_token";

    @Bean(name=CHANNEL_SECRET)
    public String getChannelSecret() {
        return env.getProperty(CHANNEL_SECRET);
    }

    @Bean(name=CHANNEL_ACCESS_TOKEN)
    public String getChannelAccessToken() {
        return env.getProperty(CHANNEL_ACCESS_TOKEN);
    }

    @Bean(name="wolframAppID")
    public String getWolframAppID() {
        return env.getProperty("com.wolfram.alpha.appid");
    }

    @Bean(name="lineMessagingClient")
    public LineMessagingClient getMessagingClient() {
        return LineMessagingClient
                .builder(getChannelAccessToken())
                .apiEndPoint(LineMessagingClientBuilder.DEFAULT_API_END_POINT)
                .connectTimeout(LineMessagingClientBuilder.DEFAULT_CONNECT_TIMEOUT)
                .readTimeout(LineMessagingClientBuilder.DEFAULT_READ_TIMEOUT)
                .writeTimeout(LineMessagingClientBuilder.DEFAULT_WRITE_TIMEOUT)
                .build();
    }

    @Bean(name="lineSignatureValidator")
    public LineSignatureValidator getSignatureValidator() {
        return new LineSignatureValidator(getChannelSecret().getBytes());
    }
}
