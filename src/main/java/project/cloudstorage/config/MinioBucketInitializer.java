package project.cloudstorage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MinioBucketInitializer implements ApplicationRunner {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioBucketInitializer(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String bucketName = minioProperties.bucket();

        boolean bucketExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (bucketExist) {
            return;
        }

        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }
}