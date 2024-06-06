package top.wuhunyu.oss.enums;

/**
 * 媒体类型枚举
 *
 * @author gongzhiqiang
 * @date 2024/06/03 22:37
 **/

public enum ContentTypeEnum {

    APPLICATION_JSON("application/json"),

    APPLICATION_XML("application/xml"),

    TEXT_HTML("text/html"),

    TEXT_PLAIN("text/plain"),

    IMAGE_JPEG("image/jpeg"),

    IMAGE_PNG("image/png"),

    APPLICATION_OCTET_STREAM("application/octet-stream"),

    APPLICATION_PDF("application/pdf");

    private final String mimeType;

    ContentTypeEnum(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
}
