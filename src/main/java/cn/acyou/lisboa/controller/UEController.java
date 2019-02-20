package cn.acyou.lisboa.controller;

import cn.acyou.lisboa.oss.OSSUploadUtil;
import com.baidu.ueditor.ActionEnter;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.UUID;

/**
 * @author youfang
 * @version [1.0.0, 2019-02-20 下午 12:03]
 * @since [天天健身/商品模块]
 **/
@Slf4j
@Controller
@RequestMapping("ue")
public class UEController {
    /**
     * 百度富文本编辑器：图片上传
     *
     * @param request
     * @param response
     */
    @RequestMapping("/upload")
    public void imgUploadByUeditor(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("utf-8");
        response.setHeader("Content-Type", "text/html");
        ServletContext application = request.getServletContext();
        String rootPath = application.getRealPath("/");
        PrintWriter out = response.getWriter();
        out.write(new ActionEnter(request, rootPath).exec());
    }

    @RequestMapping("/config")
    public void config(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, JSONException {
        String action = request.getParameter("action");
        log.info("当前调用的方法：{}", action);
        response.setContentType("application/json");
        String rootPath = request.getSession().getServletContext().getRealPath("/");
        try {
            if ("config".equals(action)) {
                ActionEnter actionEnter = new ActionEnter(request, rootPath);
                String exec = actionEnter.exec();
                PrintWriter writer = response.getWriter();
                writer.write(exec);
                writer.flush();
                writer.close();
            }
            if ("uploadimage".equals(action) || "uploadvideo".equals(action) || "uploadfile".equals(action)) {
                Map<String, MultipartFile> files = Maps.newHashMap();
                MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
                if (resolver.isMultipart(request)){
                    MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
                    files = multipartRequest.getFileMap();
                }
                JSONObject jo = new JSONObject();
                for (MultipartFile file : files.values()) {
                    String title = UUID.randomUUID().toString() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
                    log.info("上传文件：{}", title);
                    String ossPath = OSSUploadUtil.uploadOssStream(title, file.getInputStream());
                    jo.put("state", "SUCCESS");
                    //原来的文件名
                    jo.put("original", file.getName());
                    //文件大小
                    jo.put("size", file.getSize());
                    //随意，代表的是鼠标经过图片时显示的文字
                    jo.put("title", file.getName());
                    jo.put("type", FilenameUtils.getExtension(file.getOriginalFilename()));
                    jo.put("url", ossPath);
                    //OSS 预览图
                    //http://ib-others.oss-cn-beijing.aliyuncs.com/0a33b37c-d920-4b7d-96b7-33b2b77befca.mp4?x-oss-process=video/snapshot,t_5000,f_jpg,m_fast

                    PrintWriter pw = response.getWriter();
                    pw.write(jo.toString());
                    pw.flush();
                    pw.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @RequestMapping("index")
    public ModelAndView toIndex() {
        return new ModelAndView("index");
    }
}
