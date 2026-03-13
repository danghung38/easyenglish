package com.dxh.Elearning.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.dxh.Elearning.exception.AppException;
import com.dxh.Elearning.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class AwsS3Service {

    private final AmazonS3 s3Client;
    private final String bucketName;

    public AwsS3Service(
            @Value("${aws.s3.access-key}") String accessKey,
            @Value("${aws.s3.secret-key}") String secretKey,
            @Value("${aws.s3.bucket-name}") String bucketName) {

        this.bucketName = bucketName;
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    public String saveImageToS3(MultipartFile photo) {
        try (InputStream inputStream = photo.getInputStream()) {
            String s3Filename = "images/" + UUID.randomUUID() + "_" + photo.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(photo.getContentType());
            metadata.setContentLength(photo.getSize());

            s3Client.putObject(new PutObjectRequest(bucketName, s3Filename, inputStream, metadata));

            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Filename);

        } catch (Exception e) {
            throw new AppException(ErrorCode.UPLOAD_FAIL);
        }
    }

    public String saveAudioToS3(MultipartFile audio) {
        try (InputStream inputStream = audio.getInputStream()) {
            String s3Filename = "audio/" + UUID.randomUUID() + "_" + audio.getOriginalFilename();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(audio.getContentType()); // ví dụ audio/mpeg, audio/wav
            metadata.setContentLength(audio.getSize());

            s3Client.putObject(new PutObjectRequest(bucketName, s3Filename, inputStream, metadata));

            return String.format("https://%s.s3.amazonaws.com/%s", bucketName, s3Filename);

        } catch (Exception e) {
            throw new AppException(ErrorCode.UPLOAD_FAIL);
        }
    }
}
