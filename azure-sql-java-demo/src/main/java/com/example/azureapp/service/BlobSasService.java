package com.example.azureapp.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.sas.SasProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class BlobSasService {
    private final BlobServiceClient blobServiceClient;
    private final String accountName;
    private final String containerName;
    private final String blobName;
    private final int minutesValid;
    private final int clockSkewMins;

    public BlobSasService(
            BlobServiceClient blobServiceClient,
            @Value("${azure.storage.account-name}") String accountName,
            @Value("${azure.storage.container-name}") String containerName,
            @Value("${azure.storage.blob-name}") String blobName,
            @Value("${azure.storage.sas.minutes-valid:15}") int minutesValid,
            @Value("${azure.storage.sas.clock-skew-mins:5}") int clockSkewMins
    ) {
        this.blobServiceClient = blobServiceClient;
        this.accountName = accountName;
        this.containerName = containerName;
        this.blobName = blobName;
        this.minutesValid = minutesValid;
        this.clockSkewMins = clockSkewMins;
    }

    private BlobClient blobClient() {
        return blobServiceClient.getBlobContainerClient(containerName).getBlobClient(blobName);
    }

    /**
     * =========================
     * Option A: Shared Key SAS
     * =========================
     * Works when the BlobServiceClient was built using a credential with the storage account key
     * (connection string or StorageSharedKeyCredential).
     */
    public String generateReadOnlySasUrlWithSharedKey() {
        var client = blobClient().getBlockBlobClient();

        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(clockSkewMins);
        OffsetDateTime expiry = start.plusMinutes(minutesValid);

        BlobSasPermission perms = new BlobSasPermission().setReadPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiry, perms)
                .setStartTime(start)
                .setProtocol(SasProtocol.HTTPS_ONLY);

        // This convenience method requires the client to be constructed with a shared key credential
        String sas = client.generateSas(values);

        return client.getBlobUrl() + "?" + sas;
    }

}
