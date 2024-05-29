package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.annotation.VerifyParam;
import com.easybbs.controller.base.BaseController;
import com.easybbs.entity.config.AdminConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.utils.StringTools;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RestController
@RequestMapping("/file")
public class FileController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";


    @Resource
    private AdminConfig adminConfig;

    @RequestMapping("/getAvatar/{userId}")
    @GlobalInterceptor(checkParams = true)
    public void getAvatar(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("userId") String userId) {
        String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
        String avatarPath = adminConfig.getProjectFolder() + avatarFolderName + userId + Constants.AVATAR_SUFFIX;
        File file = new File(avatarPath);
        String imageFolder = Constants.FILE_FOLDER_AVATAR_NAME;
        String imageName = userId + Constants.AVATAR_SUFFIX;
        if (!file.exists()) {
            imageName = Constants.AVATAR_DEFUALT;
            if (!new File(adminConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFUALT).exists()) {
                printNoDefaultImage(response);
                return;
            }
        }
        readImage(response, imageFolder, imageName);
    }

    private void printNoDefaultImage(HttpServletResponse response) {
        response.setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE);
        response.setStatus(HttpStatus.OK.value());
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print("请在头像目录下放置默认头像default_avatar.jpg");
            writer.close();
        } catch (Exception e) {
            logger.error("输出无默认图失败", e);
        } finally {
            writer.close();
        }
    }


    private void readImage(HttpServletResponse response, String imageFolder, String imageName) {
        ServletOutputStream sos = null;
        FileInputStream in = null;
        ByteArrayOutputStream baos = null;
        try {
            if (StringTools.isEmpty(imageFolder) || StringUtils.isBlank(imageName)) {
                return;
            }
            String imageSuffix = StringTools.getFileSuffix(imageName);
            String filePath = adminConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_IMAGE + imageFolder + "/" + imageName;
            if (Constants.FILE_FOLDER_TEMP_2.equals(imageFolder)) {
                filePath = adminConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
            } else if (imageFolder.contains(Constants.FILE_FOLDER_AVATAR_NAME)) {
                filePath = adminConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + imageName;
            }
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            imageSuffix = imageSuffix.replace(".", "");
            if (!Constants.FILE_FOLDER_AVATAR_NAME.equals(imageFolder)) {
                response.setHeader("Cache-Control", "max-age=2592000");
            }
            response.setContentType("image/" + imageSuffix);
            in = new FileInputStream(file);
            sos = response.getOutputStream();
            baos = new ByteArrayOutputStream();
            int ch = 0;
            while (-1 != (ch = in.read())) {
                baos.write(ch);
            }
            sos.write(baos.toByteArray());
        } catch (Exception e) {
            logger.error("读取图片异常", e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sos != null) {
                try {
                    sos.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
        }
    }

    @RequestMapping("/getImage/{imageFolder}/{imageName}")
    @GlobalInterceptor
    public void readPic(@PathVariable("imageFolder") String imageFolder, @PathVariable("imageName") String imageName, HttpServletResponse response) {
        ServletOutputStream sos = null;
        FileInputStream in = null;
        ByteArrayOutputStream baos = null;
        try {
            if (StringTools.isEmpty(imageFolder) || StringUtils.isBlank(imageName)) {
                return;
            }
            String imageSuffix = StringTools.getFileSuffix(imageName);
            String filePath = adminConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_IMAGE + imageFolder + "/" + imageName;
            if (Constants.FILE_FOLDER_TEMP_2.equals(imageFolder)) {
                filePath = adminConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
            }
            File file = new File(filePath);
            if (!file.exists()) {
                return;
            }
            imageSuffix = imageSuffix.replace(".", "");
            response.setHeader("Cache-Control", "max-age=2592000");
            response.setContentType("image/" + imageSuffix);
            in = new FileInputStream(file);
            sos = response.getOutputStream();
            baos = new ByteArrayOutputStream();
            int ch = 0;
            while (-1 != (ch = in.read())) {
                baos.write(ch);
            }
            sos.write(baos.toByteArray());
        } catch (Exception e) {
            logger.error("读取图片异常", e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (sos != null) {
                try {
                    sos.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("IO异常", e);
                }
            }
        }
    }
}
