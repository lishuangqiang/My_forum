package com.easybbs.utils;

import com.easybbs.entity.config.AppConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.FileUploadDto;
import com.easybbs.entity.enums.DateTimePatternEnum;
import com.easybbs.entity.enums.FileUploadTypeEnum;
import com.easybbs.exception.BusinessException;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;

@Component
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    @Resource
    private AppConfig appConfig;

    public FileUploadDto uploadFile2Local(MultipartFile file, FileUploadTypeEnum uploadTypeEnum, String folder) {
        try {
            FileUploadDto uploadDto = new FileUploadDto();
            String originalFilename = file.getOriginalFilename();
            String fileSuffix = StringTools.getFileSuffix(originalFilename);
            if (originalFilename.length() > Constants.LENGTH_200) {
                originalFilename = StringTools.getFileName(originalFilename).substring(0, Constants.LENGTH_190) + fileSuffix;
            }
            if (!ArrayUtils.contains(uploadTypeEnum.getSuffixArray(), fileSuffix)) {
                throw new BusinessException("文件类型不正确");
            }
            String month = DateUtil.format(new Date(), DateTimePatternEnum.YYYYMM.getPattern());
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder + folder + month + "/");
            String fileName = StringTools.getRandomString(Constants.LENGTH_15) + fileSuffix;
            File targetFile = new File(targetFileFolder.getPath() + "/" + fileName);
            String localPath = month + "/" + fileName;

            if (uploadTypeEnum == FileUploadTypeEnum.AVATAR) {
                targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
                targetFile = new File(targetFileFolder.getPath() + "/" + folder + Constants.AVATAR_SUFFIX);
                localPath = folder + Constants.AVATAR_SUFFIX;
            }
            if (!targetFileFolder.exists()) {
                targetFileFolder.mkdirs();
            }
            file.transferTo(targetFile);
            //压缩图片
            if (uploadTypeEnum == FileUploadTypeEnum.COMMENT_IMAGE) {
                //ffmpeg压缩
                if (appConfig.getFfmpegCompress() && file.getSize() >= Constants.FILE_SIZE_500KB) {
                    String fileRealName = StringTools.getRandomString(Constants.LENGTH_15) + fileSuffix;
                    //压缩
                    ScaleFilter.compressImageWidthPercentage(targetFile, new BigDecimal(0.7), new File(targetFile.getParent() + "/" + fileRealName));
                    localPath = month + "/" + fileRealName;
                    //生成缩略图
                    String thumbnailName = fileRealName.replace(".", "_.");
                    File thumbnail = new File(targetFile.getParent() + "/" + thumbnailName);
                    ScaleFilter.createThumbnailWidthFFmpeg(new File(targetFile.getParent() + "/" + fileRealName), 200, thumbnail, false);
                } else {
                    //生成缩略图
                    String thumbnailName = targetFile.getName().replace(".", "_.");
                    File thumbnail = new File(targetFile.getParent() + "/" + thumbnailName);
                    Boolean thumbnalCreated = ScaleFilter.createThumbnail(targetFile, 200, 200, thumbnail);
                    if (!thumbnalCreated) {
                        org.apache.commons.io.FileUtils.copyFile(targetFile, thumbnail);
                    }
                }
            } else if (uploadTypeEnum == FileUploadTypeEnum.AVATAR || uploadTypeEnum == FileUploadTypeEnum.ARTICLE_COVER) {
                if (appConfig.getFfmpegCompress()) {
                    //ffmpeg压缩不能替换压缩，所以需要重新命名然后删除原来的文件
                    String thumbnailName = StringTools.getRandomString(Constants.LENGTH_30) + fileSuffix;
                    String targetFilePath = baseFolder + folder + month + File.separator + thumbnailName;
                    //头像路径特殊处理
                    if (uploadTypeEnum == FileUploadTypeEnum.AVATAR) {
                        targetFilePath = baseFolder + Constants.FILE_FOLDER_AVATAR_NAME + folder + "_temp" + Constants.AVATAR_SUFFIX;
                    }
                    Boolean compress = ScaleFilter.createThumbnailWidthFFmpeg(targetFile, 200, new File(targetFilePath), true);
                    if (compress) {
                        //头像去除temp使用uid
                        if (uploadTypeEnum == FileUploadTypeEnum.AVATAR) {
                            new File(targetFilePath).renameTo(new File(targetFilePath.replace("_temp", "")));
                        } else {
                            localPath = month + "/" + thumbnailName;
                        }
                    }
                } else {
                    ScaleFilter.createThumbnail(targetFile, 200, 200, targetFile);
                }
            }
            uploadDto.setLocalPath(localPath);
            uploadDto.setOriginalFilename(originalFilename);
            return uploadDto;
        } catch (BusinessException e) {
            logger.error("文件+" +
                    "" +
                    "上传失败", e);
            throw e;
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            throw new BusinessException("文件上传失败");
        }
    }


}
