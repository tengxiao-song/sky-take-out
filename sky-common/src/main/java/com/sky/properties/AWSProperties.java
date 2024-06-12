package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "sky.aws")
public class AWSProperties {

    private String accessKeyId;
    private String secretAccessKey;
    private String region;
    private String s3Bucket;
}
