package com.son.CapstoneProject.service.googleStorage;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;

public class BucketHandler {

    private final Storage storage;

    public BucketHandler() {
        this.storage = GoogleCloudStorage.getStorage();
    }

    /**
     * Example of creating a bucket.
     */
    // [TARGET create(BucketInfo, BucketTargetOption...)]
    // [VARIABLE "my_unique_bucket"]
    public Bucket createBucket(String bucketName) {
        // [START createBucket]
        Bucket bucket = storage.create(BucketInfo.of(bucketName));
        // [END createBucket]
        return bucket;
    }

    /**
     * Example of creating a bucket with storage class and location.
     */
    // [TARGET create(BucketInfo, BucketTargetOption...)]
    // [VARIABLE "my_unique_bucket"]
    public Bucket createBucketWithStorageClassAndLocation(String bucketName, StorageClass storageClass, String location) {
        // [START createBucketWithStorageClassAndLocation]
        Bucket bucket =
                storage.create(
                        BucketInfo.newBuilder(bucketName)
                                // See here for possible values: http://g.co/cloud/storage/docs/storage-classes
                                //.setStorageClass(StorageClass.COLDLINE)
                                .setStorageClass(storageClass)
                                // Possible values: http://g.co/cloud/storage/docs/bucket-locations#location-mr
//                                .setLocation("asia")
                                .setLocation(location)
                                .build());
        // [END createBucketWithStorageClassAndLocation]
        return bucket;
    }

    /** Example of listing buckets, specifying the page size and a name prefix. */
    // [TARGET list(BucketListOption...)]
    // [VARIABLE "bucket_"]
    public Page<Bucket> listBucketsWithSizeAndPrefix(String prefix) {
        // [START listBucketsWithSizeAndPrefix]
        // Include a prefix of bucket-name to reduce search space.
        // For more information read https://cloud.google.com/storage/docs/json_api/v1/buckets/list
        Page<Bucket> buckets =
                storage.list(Storage.BucketListOption.pageSize(100), Storage.BucketListOption.prefix(prefix));
        for (Bucket bucket : buckets.iterateAll()) {
            // do something with the bucket
        }
        // [END listBucketsWithSizeAndPrefix]
        return buckets;
    }

    /** Example of updating bucket information. */
    // [TARGET update(BucketInfo, BucketTargetOption...)]
    // [VARIABLE "my_unique_bucket"]
    public Bucket updateBucket(String bucketName) {
        // [START updateBucket]
        BucketInfo bucketInfo = BucketInfo.newBuilder(bucketName).setVersioningEnabled(true).build();
        Bucket bucket = storage.update(bucketInfo);
        // [END updateBucket]
        return bucket;
    }
}
