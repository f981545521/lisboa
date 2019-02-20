package cn.acyou.lisboa.oss;

import com.aliyun.oss.HttpMethod;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.*;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author youfang
 * @version [1.0.0, 2018-06-20 下午 01:39]
 **/
public class OSSUploadUtil {

    private static final Logger logger = LoggerFactory.getLogger(OSSUploadUtil.class);

    //OSSClient实例
    private static OSSClient ossClient = initOssClient();

    //默认bucket name
    private static String DEFAULT_BUCKET_NAME = UploadConstant.BUCKETNAME.IB_OTHERS;

    /**
     * 阿里云 multiPartFile文件上传
     * @param ossUploadVo file
     * @return URL
     */
    public static String uploadOssByVo(OSSVo ossUploadVo){
        InputStream inputStream = null;
        try {
            inputStream = ossUploadVo.getFile().getInputStream();
            String fileName = ossUploadVo.getFile().getOriginalFilename();
            String title;
            if (ossUploadVo.getFileName() != null && !ossUploadVo.getFileName().trim().equals("")){
                title = ossUploadVo.getFileName() + fileName.substring(fileName.lastIndexOf("."));
            }else {
                title = UUID.randomUUID().toString() + fileName.substring(fileName.lastIndexOf("."));
            }
            // 上传文件流。
            logger.warn("开始上传....");
            ossClient.putObject(ossUploadVo.getBucketName(), title, inputStream);
            logger.warn("上传结束。");
            // 关闭OSSClient。关闭之后就要重新实例化一个
            //ossClient.shutdown();
            return getUploadUrl(ossUploadVo.getBucketName(), title);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 阿里云 流上传
     * @param title title 文件名
     * @param inputStream stream
     * @return URL
     */
    public static String uploadOssStream(String title, InputStream inputStream){
        // 上传文件流。
        logger.warn("开始上传....");
        ossClient.putObject(UploadConstant.BUCKETNAME.IB_OTHERS, title, inputStream);
        logger.warn("上传结束。");
        return getUploadUrl(UploadConstant.BUCKETNAME.IB_OTHERS, title);
    }
    /**
     * 阿里云 URL上传
     * @param inputURL url
     * @return URL
     */
    public static String uploadOssByURLStream(String inputURL){
        URL url = null;
        try {
            url = new URL(inputURL);
            InputStream inputStream = url.openStream();
            String title = UUID.randomUUID().toString() + inputURL.substring(inputURL.lastIndexOf("."));
            // 上传文件流。
            ossClient.putObject(UploadConstant.BUCKETNAME.IB_OTHERS, title, inputStream);
            // 关闭OSSClient。
            ossClient.shutdown();
            return getUploadUrl(UploadConstant.BUCKETNAME.IB_OTHERS, title);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 阿里云 本地文件上传
     * @param localPath localPath 本地路径
     * @return URL
     */
    public static String uploadOssByLocalFile(String localPath){
        File file = new File(localPath);
        String title = file.getName();
        ossClient.putObject(UploadConstant.BUCKETNAME.IB_OTHERS, title, file );
        // 关闭OSSClient。
        ossClient.shutdown();
        return getUploadUrl(UploadConstant.BUCKETNAME.IB_OTHERS, title);
    }

    /**
     * 带进度条的上传
     * @param objectName objectName 文件名称
     * @return URL
     */
    public static String uploadWithProgress(String objectName, InputStream is){
        PutObjectRequest putObjectRequest = new PutObjectRequest(UploadConstant.BUCKETNAME.IB_OTHERS, objectName, is).
                withProgressListener(new PutObjectProgressListener());
        ossClient.putObject(putObjectRequest);
        return getUploadUrl(UploadConstant.BUCKETNAME.IB_OTHERS, objectName);
    }

    /**
     * 判断文件是否存在
     * @param objectName objectName
     * @return true 存在/false 不存在
     */
    public static boolean existFile(String objectName){
        // 判断文件是否存在。
        return ossClient.doesObjectExist(DEFAULT_BUCKET_NAME, objectName);
    }

    /**
     * 分片上传
     * @param localPath localPath 本地文件路径
     * @param objectName objectName 文件名称
     */
    public static String uploadWithMultipart(String localPath, String objectName) throws IOException {
        // 创建OSSClient实例。

        String bucketName = UploadConstant.BUCKETNAME.IB_OTHERS;
        /* 步骤1：初始化一个分片上传事件。
         */
        InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(bucketName, objectName);
        InitiateMultipartUploadResult result = ossClient.initiateMultipartUpload(request);
        // 返回uploadId，它是分片上传事件的唯一标识，您可以根据这个ID来发起相关的操作，如取消分片上传、查询分片上传等。
        String uploadId = result.getUploadId();

        /* 步骤2：上传分片。
         */
        // partETags是PartETag的集合。PartETag由分片的ETag和分片号组成。
        List<PartETag> partETags =  new ArrayList<PartETag>();
        // 计算文件有多少个分片。
        final long partSize = UploadConstant.MULTIPART_PART_SIZE;   // 1MB
        final File sampleFile = new File(localPath);
        long fileLength = sampleFile.length();
        int partCount = (int) (fileLength / partSize);
        if (fileLength % partSize != 0) {
            partCount++;
        }
        // 遍历分片上传。
        for (int i = 0; i < partCount; i++) {
            long startPos = i * partSize;
            long curPartSize = (i + 1 == partCount) ? (fileLength - startPos) : partSize;
            InputStream instream = new FileInputStream(sampleFile);
            // 跳过已经上传的分片。
            instream.skip(startPos);
            UploadPartRequest uploadPartRequest = new UploadPartRequest();
            uploadPartRequest.setBucketName(bucketName);
            uploadPartRequest.setKey(objectName);
            uploadPartRequest.setUploadId(uploadId);
            uploadPartRequest.setInputStream(instream);
            // 设置分片大小。除了最后一个分片没有大小限制，其他的分片最小为100KB。
            uploadPartRequest.setPartSize(curPartSize);
            // 设置分片号。每一个上传的分片都有一个分片号，取值范围是1~10000，如果超出这个范围，OSS将返回InvalidArgument的错误码。
            uploadPartRequest.setPartNumber( i + 1);
            // 每个分片不需要按顺序上传，甚至可以在不同客户端上传，OSS会按照分片号排序组成完整的文件。
            UploadPartResult uploadPartResult = ossClient.uploadPart(uploadPartRequest);
            // 每次上传分片之后，OSS的返回结果会包含一个PartETag。PartETag将被保存到partETags中。
            partETags.add(uploadPartResult.getPartETag());
        }

        /* 步骤3：完成分片上传。
         */
        // 排序。partETags必须按分片号升序排列。
        Collections.sort(partETags, new Comparator<PartETag>() {
            @Override
            public int compare(PartETag p1, PartETag p2) {
                return p1.getPartNumber() - p2.getPartNumber();
            }
        });
        // 在执行该操作时，需要提供所有有效的partETags。OSS收到提交的partETags后，会逐一验证每个分片的有效性。当所有的数据分片验证通过后，OSS将把这些分片组合成一个完整的文件。
        CompleteMultipartUploadRequest completeMultipartUploadRequest =
                new CompleteMultipartUploadRequest(bucketName, objectName, uploadId, partETags);
        ossClient.completeMultipartUpload(completeMultipartUploadRequest);
        return getUploadUrl(bucketName, objectName);
    }
    /**
     * 拼接成返回路径
     * @param bucketName bucketName
     * @return 返回路径
     */
    private static String getUploadUrl(String bucketName, String title){
        String start = UploadConstant.OSS_ENDPOINT.substring(0, UploadConstant.OSS_ENDPOINT.lastIndexOf("/") + 1);
        String end = UploadConstant.OSS_ENDPOINT.substring(UploadConstant.OSS_ENDPOINT.lastIndexOf("/") + 1);
        return start + bucketName + "." + end + "/" + title;
    }

    /**
     * 私有bucket 获取授权
     * @param key key
     * @return url
     */
    private static String generateAuthUrl(String key){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, 60 * 5);
        Date expiration = calendar.getTime();
        URL url = ossClient.generatePresignedUrl(DEFAULT_BUCKET_NAME, key, expiration, HttpMethod.GET);
        return url.toString();
    }

    /**
     * 创建OSSClient实例。
     * @return OSSClient实例
     */
    private static OSSClient initOssClient(){
        logger.warn("初始化OSSclient...");
        return new OSSClient(UploadConstant.OSS_ENDPOINT, UploadConstant.ACCESS_KEY_ID, UploadConstant.ACCESS_KEY_SECRET);
    }

    /**
     * 列举存储空间。
     */
    public static List<String> listBuckets(){
        List<Bucket> bucketsbuckets = ossClient.listBuckets();
        List<String> buckNameList = Lists.transform(bucketsbuckets, new Function<Bucket, String>() {
            @Override
            public String apply(Bucket input) {
                return input.getName();
            }
        });
        return buckNameList;
        //下面这句比较高级，需要有时间研究一下
        //return bucketsbuckets.stream().map(Bucket::getName).collect(Collectors.toList());
    }

    public static void main(String[] args) throws Exception{
/*        String str = "http://fs.w.kugou.com/201806211850/f215f44079296457af01356d640ec274/G123/M0A/1A/08/G4cBAFsp97WAMWxiAEURHVT3DRg816.mp3";
        String str2 = "http://fs.w.kugou.com/201806211905/0b18de10b99d2b6c52e90879140b2f97/G123/M0A/1A/08/G4cBAFsp97WAMWxiAEURHVT3DRg816.mp3";
        System.out.println(OSSUploadUtil.uploadOssByURLStream(str));
        System.out.println(OSSUploadUtil.uploadOssByURLStream(str2));*/
        //OSSUploadUtil.uploadOssByURLStream(str2);
/*        List<String> buckets = OSSUploadUtil.listBuckets();
        for (String s: buckets){
            System.out.println(s);
        }*/
        //本地文件上传
/*        String localPath = "F:\\iotest\\images\\123.jpg";
        String result = OSSUploadUtil.uploadOssByLocalFile(localPath);
        System.out.println(result);*/
        //进度条
/*        String localPath = "F:\\iotest\\images\\2.jpg";
        File file = new File(localPath);
        String result = OSSUploadUtil.uploadWithProgress(file.getName(), new FileInputStream(file));
        System.out.println(result);*/
        //分片上传
/*        String localPath = "F:\\iotest\\321.mp4";
        File file = new File(localPath);
        String result = OSSUploadUtil.uploadWithMultipart(localPath, file.getName());
        System.out.println(result);*/
        //判断文件是否存在
/*        boolean found = existFile("测试.xlsx");
        System.out.println(found);*/
        //获取权限
        String url = OSSUploadUtil.generateAuthUrl("测试.xlsx");
        System.out.println(url);

    }



}
