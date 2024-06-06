package top.wuhunyu.oss.minio;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import top.wuhunyu.oss.api.OssClient;
import top.wuhunyu.oss.enums.ContentTypeEnum;
import top.wuhunyu.oss.properties.MinioProperties;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 自定义minio客户端
 *
 * @author gongzhiqiang
 * @date 2024/06/03 18:51
 **/

@Slf4j
public class MyMinioClient implements OssClient {

    private final MinioProperties minioProperties;

    private final MinioClient minioClient;

    public MyMinioClient(MinioProperties minioProperties) {
        this.minioProperties = minioProperties;

        log.debug("minio 配置明细，endpoint：{}，accessKey：{}，secretKey：{}，region：{}",
                minioProperties.getEndpoint(),
                minioProperties.getAccessKey(),
                minioProperties.getSecretKey(),
                minioProperties.getRegion());

        // 非空校验
        Objects.requireNonNull(minioProperties.getEndpoint(), "minio：endpoint 不能为空");
        Objects.requireNonNull(minioProperties.getAccessKey(), "minio：accessKey 不能为空");
        Objects.requireNonNull(minioProperties.getSecretKey(), "minio：secretKey 不能为空");

        // 构建 minio 客户端
        this.minioClient = MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .region(minioProperties.getRegion())
                .build();
    }


    @Override
    public Boolean isBucketExist(String bucketName) {
        Objects.requireNonNull(bucketName);

        BucketExistsArgs bucketExistsArgs = BucketExistsArgs.builder()
                .bucket(bucketName)
                .build();
        try {
            return minioClient.bucketExists(bucketExistsArgs);
        } catch (Exception e) {
            log.warn("查询 bucket: {} 是否存在异常", bucketName, e);
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean isObjectExist(String bucketName, String objectName) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(objectName);

        StatObjectArgs statObjectArgs = StatObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();
        try {
            minioClient.statObject(statObjectArgs);
            return Boolean.TRUE;
        } catch (Exception ignored) {
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean isFolderExist(String bucketName, String folderName) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(folderName);

        ListObjectsArgs listObjectsArgs = ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(folderName)
                .recursive(Boolean.FALSE)
                .build();
        Iterable<Result<Item>> results = minioClient.listObjects(listObjectsArgs);
        try {
            for (Result<Item> result : results) {
                if (Optional.ofNullable(result.get())
                        .map(Item::isDir)
                        .orElse(Boolean.FALSE)) {
                    return Boolean.TRUE;
                }
            }
        } catch (Exception ignored) {
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean createBucket(String bucketName) {
        Objects.requireNonNull(bucketName);

        // 已经存在
        if (this.isBucketExist(bucketName)) {
            return Boolean.TRUE;
        }
        MakeBucketArgs makeBucketArgs = MakeBucketArgs.builder()
                .bucket(bucketName)
                .build();
        try {
            minioClient.makeBucket(makeBucketArgs);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.warn("创建 bucket: {} 异常", bucketName, e);
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean removeBucket(String bucketName) {
        Objects.requireNonNull(bucketName);

        // 不存在
        if (!this.isBucketExist(bucketName)) {
            return Boolean.TRUE;
        }
        RemoveBucketArgs removeBucketArgs = RemoveBucketArgs.builder()
                .bucket(bucketName)
                .build();
        try {
            minioClient.removeBucket(removeBucketArgs);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.warn("移除 bucket: {} 异常", bucketName, e);
        }
        return Boolean.FALSE;
    }

    @Override
    public OutputStream getObject(String bucketName, String objectName) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(objectName);

        // 文件对象不存在
        if (!this.isObjectExist(bucketName, objectName)) {
            return null;
        }

        // 获取文件对象
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();
        GetObjectResponse getObjectResponse = null;
        try {
            getObjectResponse = minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.warn("获取 object: {} / {} 异常", bucketName, objectName, e);
            return null;
        }

        // 字节输出流
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            getObjectResponse.transferTo(byteArrayOutputStream);
        } catch (IOException e) {
            log.warn("获取 object 字节流: {} / {} 异常", bucketName, objectName, e);
            return null;
        }
        return byteArrayOutputStream;
    }

    @Override
    public OutputStream getObject(String bucketName, String objectName, Long offset, Long length) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(objectName);
        Objects.requireNonNull(offset);
        Objects.requireNonNull(length);

        // 文件对象不存在
        if (!this.isObjectExist(bucketName, objectName)) {
            return null;
        }

        // 获取文件对象
        GetObjectArgs getObjectArgs = GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .offset(offset)
                .length(length)
                .build();
        GetObjectResponse getObjectResponse = null;
        try {
            getObjectResponse = minioClient.getObject(getObjectArgs);
        } catch (Exception e) {
            log.warn("获取切片 object: {} / {} 异常", bucketName, objectName, e);
            return null;
        }

        // 字节输出流
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            getObjectResponse.transferTo(byteArrayOutputStream);
        } catch (IOException e) {
            log.warn("获取切片 object 字节流: {} / {} 异常", bucketName, objectName, e);
            return null;
        }
        return byteArrayOutputStream;
    }

    @Override
    public Boolean removeObject(String bucketName, String objectName) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(objectName);

        // 文件对象不存在
        if (!this.isObjectExist(bucketName, objectName)) {
            return null;
        }

        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build();
        try {
            minioClient.removeObject(removeObjectArgs);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.warn("移除 object: {} / {} 异常", bucketName, objectName, e);
        }
        return Boolean.FALSE;
    }

    @Override
    public String upload4Base64(String bucketName, String base64, String suffix) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(base64);

        return this.upload4Bytes(bucketName, base64.getBytes(StandardCharsets.UTF_8), suffix);
    }

    @Override
    public String upload4LocalFile(String bucketName, String localFile) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(localFile);

        // 文件存在，且为文件类型
        File file = new File(localFile);
        if (!file.exists() || !file.isFile()) {
            return null;
        }

        String fileName = file.getName();
        // 生成随机的名称
        String objectName = IdUtil.fastSimpleUUID() + "." + FileNameUtil.getSuffix(fileName);
        // 获取文件类型
        String mimeType = FileUtil.getMimeType(fileName);

        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(localFile)
                    .contentType(mimeType)
                    .build();
            ObjectWriteResponse objectWriteResponse = minioClient.uploadObject(uploadObjectArgs);
            return objectWriteResponse.object();
        } catch (Exception e) {
            log.warn("上传 object: {} 异常", bucketName, e);
        }
        return "";
    }

    @Override
    public String upload4InputStream(String bucketName, InputStream inputStream, String fileName) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(inputStream);
        Objects.requireNonNull(fileName);

        // 生成随机的名称
        String objectName = IdUtil.fastSimpleUUID() + "." + fileName;
        String mimeType = FileUtil.getMimeType(objectName);
        try {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(mimeType)
                    .build();
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(putObjectArgs);
            return objectWriteResponse.object();
        } catch (Exception e) {
            log.warn("上传 object: {} 异常", bucketName, e);
        }
        return "";
    }

    @Override
    public String upload4Bytes(String bucketName, byte[] bytes, String suffix) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(bytes);
        Objects.requireNonNull(suffix);

        // 生成随机的文件名称
        String objectName = IdUtil.fastSimpleUUID() + "." + suffix;
        try (ByteArrayInputStream byteArrayInputStream =
                     new ByteArrayInputStream(bytes)) {
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(ContentTypeEnum.APPLICATION_OCTET_STREAM.getMimeType())
                    .build();
            ObjectWriteResponse objectWriteResponse = minioClient.putObject(putObjectArgs);
            return objectWriteResponse.object();
        } catch (Exception e) {
            log.warn("上传 object: {} 异常", bucketName, e);
        }
        return null;
    }

    @Override
    public String composeObjects(String bucketName, List<String> sourceObjectNames, String suffix) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(sourceObjectNames);
        Objects.requireNonNull(suffix);

        // 获取已存在的文件
        Boolean noCompleteExists = sourceObjectNames.stream()
                .map(sourceObjectName -> !this.isObjectExist(bucketName, sourceObjectName))
                .findAny()
                .orElse(Boolean.TRUE);
        if (noCompleteExists) {
            log.info("存在文件未上传成功");
            return null;
        }

        // 生成随机的文件名称
        String objectName = IdUtil.fastSimpleUUID() + "." + suffix;
        // 组合文件
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .sources(sourceObjectNames.stream()
                        .map(sourceObjectName -> ComposeSource.builder()
                                .bucket(bucketName)
                                .object(sourceObjectName)
                                .build()).collect(Collectors.toList()))
                .build();
        try {
            ObjectWriteResponse objectWriteResponse = minioClient.composeObject(composeObjectArgs);
            return objectWriteResponse.object();
        } catch (Exception e) {
            log.warn("合并 object: {} / {} 异常", bucketName, sourceObjectNames, e);
        }
        return null;
    }

    @Override
    public String getPresignedObjectUrl4Get(String bucketName, String objectName, Integer expireMinute) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(objectName);
        Objects.requireNonNull(expireMinute);

        try {
            return this.getPresignedObjectUrl(bucketName, objectName, Method.GET, expireMinute);
        } catch (Exception e) {
            log.warn("获取临时访问凭证 {} / {} 异常", bucketName, objectName);
        }
        return null;
    }

    @Override
    public String getPresignedObjectUrl4Put(String bucketName, String objectName, Integer expireMinute) {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(objectName);
        Objects.requireNonNull(expireMinute);

        try {
            return this.getPresignedObjectUrl(bucketName, objectName, Method.PUT, expireMinute);
        } catch (Exception e) {
            log.warn("获取临时上传凭证 {} / {} 异常", bucketName, objectName);
        }
        return null;
    }

    private String getPresignedObjectUrl(String bucketName, String objectName,
                                         Method method, Integer expireMinute) throws Exception {
        Objects.requireNonNull(bucketName);
        Objects.requireNonNull(objectName);
        Objects.requireNonNull(method);
        Objects.requireNonNull(expireMinute);

        GetPresignedObjectUrlArgs getPresignedObjectUrlArgs = GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .method(method)
                .expiry(expireMinute, TimeUnit.MINUTES)
                .build();
        return minioClient.getPresignedObjectUrl(getPresignedObjectUrlArgs);
    }

}
