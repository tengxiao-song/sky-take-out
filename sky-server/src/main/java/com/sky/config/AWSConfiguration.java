package com.sky.config;

import com.sky.properties.AWSProperties;
import com.sky.utils.S3Util;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfiguration {

    @Bean
    public S3Util awsUtil(AWSProperties awsProperties) {
        return new S3Util(awsProperties.getAccessKeyId(), awsProperties.getSecretAccessKey(), awsProperties.getRegion(), awsProperties.getS3Bucket());
    }
}
