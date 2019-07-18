package com.son.CapstoneProject.service.googleStorage;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.son.CapstoneProject.common.StringUtils;
import com.son.CapstoneProject.controller.user.CommentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class BlobHandler {

    private static final Logger logger = LoggerFactory.getLogger(BlobHandler.class);

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
        try {
            if (!StringUtils.isNullOrEmpty(bucketName) && !StringUtils.isNullOrEmpty(blobName)) {
                BlobId blobId = BlobId.of(bucketName, blobName);
                return GoogleCloudStorage.getStorage().get(blobId);
            }
            return null;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw e;
        }
    }

    /**
     * Example of creating a blob from a byte array.
     */
    // [TARGET create(BlobInfo, byte[], BlobTargetOption...)]
    // [VARIABLE "my_unique_bucket"]
    // [VARIABLE "my_blob_name"]
    public Blob createBlobFromByteArray(String bucketName, String blobName, byte[] byteArray, String contentType) {
        try {
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
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw e;
        }
    }

    /**
     * Example of replacing blob's metadata.
     */
    // [TARGET update(BlobInfo)]
    // [VARIABLE "my_unique_bucket"]
    // [VARIABLE "my_blob_name"]
    public Blob updateBlob(String bucketName, String blobName, byte[] byteArray, String contentType) {
        try {
            BlobId blobId = BlobId.of(bucketName, blobName);
            // Delete then create new
            deleteBlob(bucketName, blobName);
            return createBlobFromByteArray(bucketName, blobName, byteArray, contentType);
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw e;
        }
    }

    /**
     * Example of deleting a blob.
     */
    // [TARGET delete(BlobId)]
    // [VARIABLE "my_unique_bucket"]
    // [VARIABLE "my_blob_name"]
    public boolean deleteBlob(String bucketName, String blobName) {
        try {
            if (!StringUtils.isNullOrEmpty(bucketName) && !StringUtils.isNullOrEmpty(blobName)) {
                BlobId blobId = BlobId.of(bucketName, blobName);
                // true = deleted / false = not deleted
                return GoogleCloudStorage.getStorage().delete(blobId);
            }
            return false;
        } catch (Exception e) {
            logger.error("An error has occurred", e);
            throw e;
        }
    }

}
