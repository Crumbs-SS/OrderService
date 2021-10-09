package com.crumbs.orderservice.security;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;


public class SecretManager {
    public static String getSecret(String secretName) {
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        String secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
        String accountId = System.getenv("AWS_ACCOUNT_ID");

        String regionName = "us-east-1";
        String secretId = "arn:aws:secretsmanager:" + regionName + ":" + accountId + ":secret:" + secretName;

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        Region region = Region.of(regionName);
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();

        String secret = "";
        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretId)
                .build();
        GetSecretValueResponse getSecretValueResponse;

        try {
            getSecretValueResponse = client.getSecretValue(getSecretValueRequest);
        } catch (Exception ignored) {
            return "Failed to get secret.";
        }

        if (getSecretValueResponse.secretString() != null) {
            secret = getSecretValueResponse.secretString().split(":\"")[1].split("\"")[0];
        }

        return secret;
    }
}
