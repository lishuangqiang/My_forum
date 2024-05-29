package com.easybbs.utils;


import com.easybbs.entity.config.AppConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.enums.DateTimePatternEnum;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ImageUtils {

    @Resource
    private AppConfig appConfig;

    public String resetImageHtml(String html) {
        String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
        List<String> imageList = getImageList(html);
        for (String img : imageList) {
            resetImage(img, month);
        }
        return month;
    }


    private String resetImage(String imagePath, String month) {
        if (StringTools.isEmpty(imagePath) || !imagePath.contains(Constants.FILE_FOLDER_TEMP_2)) {
            return imagePath;
        }
        imagePath = imagePath.replace(Constants.READ_IMAGE_PATH, "");
        if (StringTools.isEmpty(month)) {
            month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
        }
        String imageFileName = month + "/" + imagePath.substring(imagePath.lastIndexOf("/") + 1);
        File targetFile = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_IMAGE + imageFileName);
        try {
            FileUtils.copyFile(new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + imagePath), targetFile);
        } catch (IOException e) {
            e.printStackTrace();
            return imagePath;
        }
        return imageFileName;
    }

    private List<String> getImageList(String content) {
        List<String> imageList = new ArrayList<>();
        String regEx_img = "(<img.*src\\s*=\\s*(.*?)[^>]*?>)";
        Pattern p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        Matcher m_image = p_image.matcher(content);
        while (m_image.find()) {
            String img = m_image.group();
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)")
                    .matcher(img);
            while (m.find()) {
                String imageUrl = m.group(1);
                imageList.add(imageUrl);
            }
        }
        return imageList;
    }
}
