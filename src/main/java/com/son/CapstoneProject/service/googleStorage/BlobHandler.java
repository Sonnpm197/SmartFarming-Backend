package com.son.CapstoneProject.service.googleStorage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BlobHandler {

    private static final BlobHandler BLOB_HANDLER = new BlobHandler();

    public static final BlobHandler getInstance() {
        return BLOB_HANDLER;
    }

    /**
     * Example of getting information on a blob.
     */
    // [TARGET get(BlobId)]
    // [VARIABLE "my_unique_bucket"]
    // [VARIABLE "my_blob_name"]
    public Blob getBlobFromId(String bucketName, String blobName) {
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob blob = GoogleCloudStorage.getStorage().get(blobId);
        return blob;
    }

    /**
     * Example of creating a blob from a byte array.
     */
    // [TARGET create(BlobInfo, byte[], BlobTargetOption...)]
    // [VARIABLE "my_unique_bucket"]
    // [VARIABLE "my_blob_name"]
    public Blob createBlobFromByteArray(String bucketName, String blobName, byte[] byteArray, String contentType) {
        BlobId blobId = BlobId.of(bucketName, blobName);

        if (getBlobFromId(bucketName, blobName) != null) {
            // This means this file has duplicated name with another file
            blobName = UUID.randomUUID().toString() + blobName;
            blobId = BlobId.of(bucketName, blobName);
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        Blob blob = GoogleCloudStorage.getStorage().create(blobInfo, byteArray);

        // Make this blob public
        GoogleCloudStorage.getStorage().createAcl(blobId, Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
        return blob;
    }

    /**
     * Example of replacing blob's metadata.
     */
    // [TARGET update(BlobInfo)]
    // [VARIABLE "my_unique_bucket"]
    // [VARIABLE "my_blob_name"]
    public Blob updateBlob(String bucketName, String blobName, byte[] byteArray, String contentType) {
        BlobId blobId = BlobId.of(bucketName, blobName);
        Blob newBlob = null;

        // Delete then create new
        deleteBlob(bucketName, blobName);
        newBlob = createBlobFromByteArray(bucketName, blobName, byteArray, contentType);
        return newBlob;
    }

    /**
     * Example of deleting a blob.
     */
    // [TARGET delete(BlobId)]
    // [VARIABLE "my_unique_bucket"]
    // [VARIABLE "my_blob_name"]
    public boolean deleteBlob(String bucketName, String blobName) {
        BlobId blobId = BlobId.of(bucketName, blobName);
        boolean deleted = GoogleCloudStorage.getStorage().delete(blobId);
        return deleted;
    }

}
