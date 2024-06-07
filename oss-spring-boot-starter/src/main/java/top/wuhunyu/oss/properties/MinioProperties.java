package top.wuhunyu.oss.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * minio 配置参数
 *
 * @author gongzhiqiang
 * @date 2024/06/05 12:34
 **/

@Data
@ConditionalOnProperty(prefix = "spring.oss.minio", name = "enabled", havingValue = "true")
@ConfigurationProperties(prefix = "spring.oss.minio")
public class MinioProperties {

    /**
     * 是否启用
     * true: 启用; false: 禁用
     */
    private Boolean enabled = Boolean.TRUE;

    /**
     * minio服务接入点
     */
    private String endpoint;

    /**
     * 访问keu
     */
    private String accessKey;

    /**
     * 访问密钥
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 默认 bucket 名称
     */
    private String defaultBucket;

}
