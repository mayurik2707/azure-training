package com.example.azureapp.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BlobConfig {

    @Bean
    public BlobServiceClient blobServiceClient(
            @Value("${azure.storage.connection-string:}") String connectionString,
            @Value("${azure.storage.account-name:}") String accountName,
            @Value("${azure.storage.account-key:}") String accountKey
    ) {
        // Option A: Shared Key via connection string (dev)
        if (connectionString != null && !connectionString.isBlank()) {
            return new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        }


        throw new IllegalStateException("No Azure Storage config found. Provide either connection-string, or account-endpoint (+ key for Shared Key, or Managed Identity for AAD).");
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }
}
