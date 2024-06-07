package top.wuhunyu.oss.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.wuhunyu.oss.api.OssClient;
import top.wuhunyu.oss.minio.MyMinioClient;
import top.wuhunyu.oss.properties.MinioProperties;

/**
 * oss 自动装配
 *
 * @author gongzhiqiang
 * @date 2024/06/05 21:54
 **/

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class OssAutoconfigure {

    @Bean("myMinioClient")
    @ConditionalOnBean(MinioProperties.class)
    @ConditionalOnMissingBean(OssClient.class)
    public OssClient myMinioClient(MinioProperties minioProperties) {
        return new MyMinioClient(minioProperties);
    }

}
