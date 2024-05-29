package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.annotation.VerifyParam;
import com.easybbs.controller.base.BaseController;
import com.easybbs.entity.config.WebConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.enums.UserOperFrequencyTypeEnum;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.utils.ScaleFilter;
import com.easybbs.utils.StringTools;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/json;charset=UTF-8";


    @Resource
    private WebConfig webConfig;

    /**
     * @Description: 件上传
     * @auther: 程序员老罗
     * @date: 2021/1/30
     * @param: [file]
     * @return: com.easypay.entity.vo.AjaxResponseVO
     */
    @RequestMapping("uploadImage")
    @GlobalInterceptor(checkLogin = true, frequencyType = UserOperFrequencyTypeEnum.IMAGE_UPLAOD)
    public ResponseVO uploadImage(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String fileExtName = StringTools.getFileSuffix(fileName);
        if (!ArrayUtils.contains(Constants.IMAGE_SUFFIX, fileExtName)) {
            throw new BusinessException("请选择图片文件上传");
        }
        String path = copyFile(file);
        Map<String, String> fileMap = new HashMap();
        fileMap.put("fileName", path);
        return getSuccessResponseVO(fileMap);
    }

    private String copyFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();
            String fileExtName = StringTools.getFileSuffix(fileName);
            String fileRealName = StringTools.getRandomString(Constants.LENGTH_30) + fileExtName;
            String folderPath = webConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_TEMP;
            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File uploadFile = new File(folderPath + File.separator + fileRealName);
            file.transferTo(uploadFile);
            if (webConfig.getFfmpegCompress() && file.getSize() >= Constants.FILE_SIZE_500KB) {
                fileRealName = StringTools.getRandomString(Constants.LENGTH_30) + fileExtName;
                ScaleFilter.compressImageWidthPercentage(uploadFile, new BigDecimal(0.7), new File(folderPath + File.separator + fileRealName));
            }
            return Constants.FILE_FOLDER_TEMP_2 + "/" + fileRealName;
        } catch (Exception e) {
            logger.error("上传文件失败", e);
            throw new BusinessException("上传文件失败");
        }
    }

    @RequestMapping("/getAvatar/{userId}")
    @GlobalInterceptor(checkLogin = false, checkParams = true)
    public void getAvatar(HttpServletResponse response, @VerifyParam(required = true) @PathVariable("userId") String userId) {
        String avatarFolderName = Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_AVATAR_NAME;
        String avatarPath = webConfig.getProjectFolder() + avatarFolderName + userId + Constants.AVATAR_SUFFIX;
        File file = new File(avatarPath);
        String imageFolder = Constants.FILE_FOLDER_AVATAR_NAME;
        String imageName = userId + Constants.AVATAR_SUFFIX;
        if (!file.exists()) {
            imageName = Constants.AVATAR_DEFUALT;
            if (!new File(webConfig.getProjectFolder() + avatarFolderName + Constants.AVATAR_DEFUALT).exists()) {
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

    @RequestMapping("/getImage/{imageFolder}/{imageName}")
    public void getImage(HttpServletResponse response, @PathVariable("imageFolder") String imageFolder, @PathVariable("imageName") String imageName) {
        readImage(response, imageFolder, imageName);
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
            String filePath = webConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_IMAGE + imageFolder + "/" + imageName;
            if (Constants.FILE_FOLDER_TEMP_2.equals(imageFolder)) {
                filePath = webConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + "/" + imageName;
            } else if (imageFolder.contains(Constants.FILE_FOLDER_AVATAR_NAME)) {
                filePath = webConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imageFolder + imageName;
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
}
