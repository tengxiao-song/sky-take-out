package com.sky.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Data
@AllArgsConstructor
public class S3Util {

    private String accessKeyId;
    private String secretAccessKey;
    private String region;
    private String s3Bucket;

    public String upload(byte[] bytes, String objectName) {
        // 创造AWS基本凭证
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        // 建立S3客户端连接
        S3Client s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();

        // 上传文件请求
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Bucket)
                .key(objectName)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

        // 得到文件的URL
        return s3Client.utilities().getUrl(builder -> builder.bucket(s3Bucket).key(objectName)).toExternalForm();
    }
}
