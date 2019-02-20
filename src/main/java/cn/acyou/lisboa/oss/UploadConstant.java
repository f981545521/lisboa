package cn.acyou.lisboa.oss;

/**
 * 阿里云上传
 * @author youfang
 * @version [1.0.0, 2018-06-19 下午 03:03]
 **/
public class UploadConstant {

    /**
     * 注： accessKeyId和accessKeySecret是全局配置参数，需要按实际值填写。
     */
    public static final String ACCESS_KEY_ID = "LTAIT5SwOfWsF2a7";
    public static final String ACCESS_KEY_SECRET = "reSSVABBqf8dprEbnuB2WGpFELrl4g";

    /**
     * 最大上传大小 ： 30Mb
     */
    public static final Long ACCESS_MAX_SIZE = 31457280L;

    /**
     * OSS Endpoint 地区
     */
    public static final String OSS_ENDPOINT = "http://oss-cn-beijing.aliyuncs.com";

    /**
     * OSS Bucket 名称
     */
    class BUCKETNAME {
        public static final String IB_IMAGES = "ib-images";
        public static final String IB_AUDIOS = "ib-audios";
        public static final String IB_VIDEOS = "ib-videos";
        public static final String IB_OTHERS = "ib-others";

    }

    /**
     * 分片文件大小
     */
    public static final long MULTIPART_PART_SIZE = 1024 * 1024L;


}
