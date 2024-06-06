package top.wuhunyu.oss.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * oss客户端
 *
 * @author gongzhiqiang
 * @date 2024/06/03 12:05
 **/

public interface OssClient {

    /**
     * 判断一个 bucket 是否存在
     *
     * @param bucketName bucket
     * @return true：存在；false：不存在
     */
    Boolean isBucketExist(String bucketName);

    /**
     * 判断一个文件对象是否存在
     *
     * @param bucketName bucket
     * @param objectName 文件对象全路径名称
     * @return true：存在；false：不存在
     */
    Boolean isObjectExist(String bucketName, String objectName);

    /**
     * 判断一个目录是否存在
     *
     * @param bucketName bucket
     * @param folderName 目录
     * @return true：存在；false：不存在
     */
    Boolean isFolderExist(String bucketName, String folderName);

    /**
     * 创建一个 bucket
     *
     * @param bucketName bucket
     * @return true：成功；false：失败
     */
    Boolean createBucket(String bucketName);

    /**
     * 移除一个 bucket
     *
     * @param bucketName bucket
     * @return true：成功；false：失败
     */
    Boolean removeBucket(String bucketName);

    /**
     * 获取一个文件对象的输出流
     *
     * @param bucketName bucket
     * @param objectName 文件对象的绝对访问路径
     * @return 文件对象的输出流
     */
    OutputStream getObject(String bucketName, String objectName);

    /**
     * 获取一个大文件对象某个切片的输出流
     *
     * @param bucketName bucket
     * @param objectName 文件对象的绝对访问路径
     * @param offset     位移
     * @param length     长度
     * @return 文件对象的输出流
     */
    OutputStream getObject(String bucketName, String objectName, Long offset, Long length);

    /**
     * 删除一个文件对象
     *
     * @param bucketName bucket
     * @param objectName 文件对象的绝对访问路径
     * @return true：成功；false：失败
     */
    Boolean removeObject(String bucketName, String objectName);

    /**
     * 以 base64 字符串的形式上传一个文件
     *
     * @param bucketName bucket
     * @param base64     base64 字符串
     * @param suffix     上传文件的后缀，没有 .
     * @return 上传完毕后文件在文件服务器的路径
     */
    String upload4Base64(String bucketName, String base64, String suffix);

    /**
     * 上传一个本地文件
     *
     * @param bucketName bucket
     * @param localFile  本地文件的绝对路径
     * @return 上传完毕后文件在文件服务器的路径
     */
    String upload4LocalFile(String bucketName, String localFile);

    /**
     * 以输入流的形式上传一个文件
     *
     * @param bucketName  bucket
     * @param inputStream 输入流对象
     * @param suffix      上传文件的后缀，没有 .
     * @return 上传完毕后文件在文件服务器的路径
     */
    String upload4InputStream(String bucketName, InputStream inputStream, String suffix);

    /**
     * 以字节数组的形式上传一个文件
     *
     * @param bucketName bucket
     * @param bytes      字节数组
     * @param suffix     上传文件的后缀，没有 .
     * @return 上传完毕后文件在文件服务器的路径
     */
    String upload4Bytes(String bucketName, byte[] bytes, String suffix);

    /**
     * 合并多个文件对象为一个文件对象
     * 被合并的文件对象需要已存在于文件服务器中
     *
     * @param bucketName        bucket
     * @param sourceObjectNames 需要被合并的文件绝对路径
     * @param suffix            上传文件的后缀，没有 .
     * @return 合并完毕后文件在文件服务器的路径
     */
    String composeObjects(String bucketName, List<String> sourceObjectNames, String suffix);

    /**
     * 获取一个文件对象的临时访问凭证，需要指定有效时间，有效时间单位是 分
     *
     * @param bucketName   bucket
     * @param objectName   文件对象的绝对访问路径
     * @param expireMinute 有效时间，单位 分
     * @return 临时访问凭证完整地址
     */
    String getPresignedObjectUrl4Get(String bucketName, String objectName, Integer expireMinute);

    /**
     * 获取一个文件对象的临时上传凭证，需要指定有效时间，有效时间单位是 分
     *
     * @param bucketName   bucket
     * @param objectName   文件对象的绝对访问路径
     * @param expireMinute 有效时间，单位 分
     * @return 临时上传凭证完整地址
     */
    String getPresignedObjectUrl4Put(String bucketName, String objectName, Integer expireMinute);

}
