package cn.acyou.lisboa.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.MultipartConfigElement;
import java.util.Collections;
import java.util.List;

/**
 *
 */
@Configuration
@Slf4j
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    /**
     * 根据类型注入一个List，只要Spring中有这个类型的都加到这个集合中
     */
    @Autowired(required = false)
    private List<HandlerInterceptor> interceptorList = Collections.emptyList();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        for (HandlerInterceptor handlerInterceptor : interceptorList){
            registry.addInterceptor(handlerInterceptor);
        }
        super.addInterceptors(registry);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars*//**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("/dist/**").addResourceLocations("classpath:/dist/");
    }


    /**
     *  使用CORS解决解决跨域问题
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "DELETE", "PUT");
    }

    @Bean
    public MultipartConfigElement configElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize("300MB");
        factory.setMaxRequestSize("300MB");
        return factory.createMultipartConfig();
    }

}
