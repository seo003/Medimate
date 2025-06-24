package com.inhatc.medimate.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

public class SimpleAWSCredentialsProvider implements AWSCredentialsProvider {
    private final AWSCredentials credentials;

    public SimpleAWSCredentialsProvider(String accessKey, String secretKey) {
        this.credentials = new BasicAWSCredentials(accessKey, secretKey);
    }

    @Override
    public AWSCredentials getCredentials() {
        return credentials;
    }
    @Override
    public void refresh() {
        // No-op
    }
}
