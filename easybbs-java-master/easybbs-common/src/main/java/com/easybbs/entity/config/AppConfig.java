package com.easybbs.entity.config;

import com.easybbs.utils.StringTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appConfig")
public class AppConfig {

    /**
     * 文件目录
     */
    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${image.ffmpegCompress:false}")
    private Boolean ffmpegCompress;

    @Value("${inner.api.appKey:''}")
    private String innerApiAppKey;

    @Value("${inner.api.appSecret:''}")
    private String innerApiAppSecret;

    @Value("${isDev:false}")
    private Boolean isDev;

    public String getProjectFolder() {
        if (!StringTools.isEmpty(projectFolder) && !projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }

    public Boolean getFfmpegCompress() {
        return ffmpegCompress;
    }

    public String getInnerApiAppKey() {
        return innerApiAppKey;
    }

    public String getInnerApiAppSecret() {
        return innerApiAppSecret;
    }

    public Boolean getDev() {
        return isDev;
    }
}
