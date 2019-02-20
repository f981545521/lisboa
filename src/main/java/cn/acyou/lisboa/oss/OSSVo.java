package cn.acyou.lisboa.oss;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * @author youfang
 * @date 2018-07-05 下午 10:23
 */
@Data
public class OSSVo implements Serializable{

    private static final long serialVersionUID = -7937518394909244526L;


    private String bucketName;

    private String fileName;

    private MultipartFile file;





}
