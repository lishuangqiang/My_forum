package com.easybbs.service.impl;

import com.easybbs.entity.config.AppConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.FileUploadDto;
import com.easybbs.entity.dto.SysSetting4AuditDto;
import com.easybbs.entity.enums.*;
import com.easybbs.entity.po.ForumArticle;
import com.easybbs.entity.po.ForumArticleAttachment;
import com.easybbs.entity.po.ForumBoard;
import com.easybbs.entity.po.UserMessage;
import com.easybbs.entity.query.ForumArticleAttachmentQuery;
import com.easybbs.entity.query.ForumArticleQuery;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.ForumArticleAttachmentMapper;
import com.easybbs.mappers.ForumArticleMapper;
import com.easybbs.service.ForumArticleService;
import com.easybbs.service.ForumBoardService;
import com.easybbs.service.UserInfoService;
import com.easybbs.service.UserMessageService;
import com.easybbs.utils.FileUtils;
import com.easybbs.utils.ImageUtils;
import com.easybbs.utils.StringTools;
import com.easybbs.utils.SysCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;


/**
 * 文章信息 业务接口实现
 */
@Service("forumArticleService")
public class ForumArticleServiceImpl implements ForumArticleService {

    private static final Logger logger = LoggerFactory.getLogger(ForumArticleServiceImpl.class);

    @Resource
    private ForumArticleMapper<ForumArticle, ForumArticleQuery> forumArticleMapper;

    @Resource
    private ForumArticleAttachmentMapper<ForumArticleAttachment, ForumArticleAttachmentQuery> forumArticleAttachmentMapper;

    @Resource
    private FileUtils fileUtils;

    @Resource
    private ForumBoardService forumBoardService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private AppConfig appConfig;

    @Resource
    private ImageUtils imageUtils;

    @Resource
    private UserMessageService userMessageService;

    @Lazy
    @Resource
    private ForumArticleServiceImpl forumArticleService;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ForumArticle> findListByParam(ForumArticleQuery param) {
        return this.forumArticleMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ForumArticleQuery param) {
        return this.forumArticleMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ForumArticle> findListByPage(ForumArticleQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ForumArticle> list = this.findListByParam(param);
        PaginationResultVO<ForumArticle> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ForumArticle bean) {
        return this.forumArticleMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ForumArticle> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.forumArticleMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ForumArticle> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.forumArticleMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据ArticleId获取对象
     */
    @Override
    public ForumArticle getForumArticleByArticleId(String articleId) {
        return this.forumArticleMapper.selectByArticleId(articleId);
    }

    /**
     * 根据ArticleId修改
     */
    @Override
    public Integer updateForumArticleByArticleId(ForumArticle bean, String articleId) {
        return this.forumArticleMapper.updateByArticleId(bean, articleId);
    }

    /**
     * 根据ArticleId删除
     */
    @Override
    public Integer deleteForumArticleByArticleId(String articleId) {
        return this.forumArticleMapper.deleteByArticleId(articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void postArticle(Boolean isAdmin, ForumArticle article, ForumArticleAttachment forumArticleAttachment, MultipartFile cover, MultipartFile attachment) {
        //基本校验
        checkArticle(isAdmin, article);

        String articleId = StringTools.getRandomString(Constants.LENGTH_15);
        article.setArticleId(articleId);
        article.setPostTime(new Date());
        article.setLastUpdateTime(new Date());

        if (cover != null) {
            FileUploadDto fileUploadDto = fileUtils.uploadFile2Local(cover, FileUploadTypeEnum.ARTICLE_COVER, Constants.FILE_FOLDER_IMAGE);
            article.setCover(fileUploadDto.getLocalPath());
        }

        if (attachment != null) {
            article.setAttachmentType(Constants.ONE);
            //上传附件
            uploadAttachment(article, forumArticleAttachment, attachment, false);
        } else {
            article.setAttachmentType(Constants.ZERO);
        }

        //文章是否需要审核
        if (isAdmin) {
            article.setStatus(ArticleStatusEnum.AUDIT.getStatus());
        } else {
            SysSetting4AuditDto auditDto = SysCacheUtils.getSysSetting().getAuditStting();
            article.setStatus(auditDto.getPostAudit() ? ArticleStatusEnum.NO_AUDIT.getStatus() :
                    ArticleStatusEnum.AUDIT.getStatus());
        }

        //替换图片
        String content = article.getContent();
        if (!StringTools.isEmpty(content)) {
            String month = imageUtils.resetImageHtml(content);
            //避免替换博客中template关键，所以前后带上/
            String replaceMonth = "/" + month + "/";
            content = content.replace(Constants.FILE_FOLDER_TEMP, replaceMonth);
            article.setContent(content);
            String markdownContent = article.getMarkdownContent();
            if (!StringTools.isEmpty(markdownContent)) {
                markdownContent = markdownContent.replace(Constants.FILE_FOLDER_TEMP, replaceMonth);
                article.setMarkdownContent(markdownContent);
            }
        }

        this.forumArticleMapper.insert(article);

        //增加积分
        Integer postIntegral = SysCacheUtils.getSysSetting().getPostSetting().getPostIntegral();
        if (postIntegral > 0 && ArticleStatusEnum.AUDIT.getStatus().equals(article.getStatus())) {
            this.userInfoService.updateUserIntegral(article.getUserId(),
                    UserIntegralOperTypeEnum.POST_COMMENT, UserIntegralChangeTypeEnum.ADD.getChangeType(), postIntegral);
        }
    }

    @Override
    public void updateArticle(Boolean isAdmin, ForumArticle article, ForumArticleAttachment forumArticleAttachment, MultipartFile cover, MultipartFile attachment) throws BusinessException {
        //判断文章是否是本人
        ForumArticle dbInfo = forumArticleMapper.selectByArticleId(article.getArticleId());
        if (!isAdmin && !dbInfo.getUserId().equals(article.getUserId())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //基本校验
        checkArticle(isAdmin, article);
        article.setLastUpdateTime(new Date());

        if (cover != null) {
            FileUploadDto fileUploadDto = fileUtils.uploadFile2Local(cover, FileUploadTypeEnum.ARTICLE_COVER, Constants.FILE_FOLDER_IMAGE);
            article.setCover(fileUploadDto.getLocalPath());
        }

        if (attachment != null) {
            article.setAttachmentType(Constants.ONE);
            //上传附件
            uploadAttachment(article, forumArticleAttachment, attachment, true);
        }

        //获取数据库中的附件
        ForumArticleAttachmentQuery attachmentQuery = new ForumArticleAttachmentQuery();
        attachmentQuery.setArticleId(article.getArticleId());
        List<ForumArticleAttachment> articleAttachmentList = this.forumArticleAttachmentMapper.selectList(attachmentQuery);
        ForumArticleAttachment dbAttachment = null;
        if (!articleAttachmentList.isEmpty()) {
            dbAttachment = articleAttachmentList.get(0);
        }

        if (dbAttachment != null) {
            if (article.getAttachmentType() == 0) {
                //删除之前的附件
                new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_ATTACHMENT + dbAttachment.getFilePath()).delete();
                this.forumArticleAttachmentMapper.deleteByFileId(dbAttachment.getFileId());
            } else {
                //更新积分
                if (!dbAttachment.getIntegral().equals(forumArticleAttachment.getIntegral())) {
                    ForumArticleAttachment integralUpdate = new ForumArticleAttachment();
                    integralUpdate.setIntegral(forumArticleAttachment.getIntegral());
                    this.forumArticleAttachmentMapper.updateByFileId(integralUpdate, dbAttachment.getFileId());
                }
            }
        }
        //文章是否需要审核
        if (isAdmin) {
            article.setStatus(ArticleStatusEnum.AUDIT.getStatus());
        } else {
            SysSetting4AuditDto auditDto = SysCacheUtils.getSysSetting().getAuditStting();
            article.setStatus(auditDto.getPostAudit() ? ArticleStatusEnum.NO_AUDIT.getStatus() :
                    ArticleStatusEnum.AUDIT.getStatus());
        }

        //替换图片
        String content = article.getContent();
        if (!StringTools.isEmpty(content)) {
            String month = imageUtils.resetImageHtml(content);
            //避免替换博客中template关键，所以前后带上/
            String replaceMonth = "/" + month + "/";
            content = content.replace(Constants.FILE_FOLDER_TEMP, replaceMonth);
            article.setContent(content);
            String markdownContent = article.getMarkdownContent();
            if (!StringTools.isEmpty(markdownContent)) {
                markdownContent = markdownContent.replace(Constants.FILE_FOLDER_TEMP, replaceMonth);
                article.setMarkdownContent(markdownContent);
            }
        }

        this.forumArticleMapper.updateByArticleId(article, article.getArticleId());
    }

    private void checkArticle(Boolean isAdmin, ForumArticle article) {
        EditorTypeEnum editorTypeEnum = EditorTypeEnum.getByType(article.getEditorType());
        if (null == editorTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if (!StringTools.isEmpty(article.getSummary()) && article.getSummary().length() > Constants.LENGTH_200) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        resetBoardInfo(isAdmin, article);
    }

    private void resetBoardInfo(Boolean isAdmin, ForumArticle article) {
        ForumBoard board = forumBoardService.getForumBoardByBoardId(article.getpBoardId());
        if (null == board || board.getPostType() == 0 && !isAdmin) {
            throw new BusinessException("一级板块不存在");
        }
        article.setpBoardName(board.getBoardName());
        if (article.getBoardId() != null && article.getBoardId() != 0) {
            board = forumBoardService.getForumBoardByBoardId(article.getBoardId());
            if (null == board || board.getPostType() == 0 && !isAdmin) {
                throw new BusinessException("二级板块不存在");
            }
            article.setBoardName(board.getBoardName());
        } else {
            article.setBoardId(0);
            article.setBoardName("");
        }
    }

    public void uploadAttachment(ForumArticle article, ForumArticleAttachment attachment, MultipartFile file, Boolean isUpdate) {
        Integer allowSizeMb = SysCacheUtils.getSysSetting().getPostSetting().getAttachmentSize();
        long allowSize = allowSizeMb * Constants.FILE_SIZE_1M;
        if (file.getSize() > allowSize) {
            throw new BusinessException("附件最大只能" + allowSizeMb + "MB");
        }
        ForumArticleAttachment dbInfo = null;
        // isUpdate = true 修改
        if (isUpdate) {
            ForumArticleAttachmentQuery attachmentQuery = new ForumArticleAttachmentQuery();
            attachmentQuery.setArticleId(article.getArticleId());
            List<ForumArticleAttachment> articleAttachmentList = this.forumArticleAttachmentMapper.selectList(attachmentQuery);
            if (!articleAttachmentList.isEmpty()) {
                dbInfo = articleAttachmentList.get(0);
                //删除之前的附件
                new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_ATTACHMENT + dbInfo.getFilePath()).delete();
            }
        }

        FileUploadDto fileUploadDto = fileUtils.uploadFile2Local(file, FileUploadTypeEnum.ARTICLE_ATTACHMENT, Constants.FILE_FOLDER_ATTACHMENT);
        if (dbInfo == null) {
            attachment.setFileId(StringTools.getRandomNumber(Constants.LENGTH_15));
            attachment.setArticleId(article.getArticleId());
            attachment.setFileName(fileUploadDto.getOriginalFilename());
            attachment.setFilePath(fileUploadDto.getLocalPath());
            attachment.setFileSize(file.getSize());
            attachment.setDownloadCount(0);
            attachment.setUserId(article.getUserId());
            attachment.setFileType(AttachmentFileTypeEnum.ZIP.getType());
            forumArticleAttachmentMapper.insert(attachment);
        } else {
            ForumArticleAttachment updateInfo = new ForumArticleAttachment();
            updateInfo.setFileName(fileUploadDto.getOriginalFilename());
            updateInfo.setFileSize(file.getSize());
            updateInfo.setFilePath(fileUploadDto.getLocalPath());
            forumArticleAttachmentMapper.updateByFileId(updateInfo, dbInfo.getFileId());
        }
    }

    @Override
    public ForumArticle readArticle(String artcileId) {
        ForumArticle forumArticle = this.forumArticleMapper.selectByArticleId(artcileId);
        if (forumArticle == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        if (ArticleStatusEnum.AUDIT.getStatus().equals(forumArticle.getStatus())) {
            forumArticleMapper.updateArticleCount(UpdateArticleCountTypeEnum.READ_COUNT.getType(), 1, artcileId);
        }
        return forumArticle;
    }

    @Override
    public void updateBoard(String articleId, Integer pBoardId, Integer boardId) {
        ForumArticle forumArticle = new ForumArticle();
        forumArticle.setpBoardId(pBoardId);
        forumArticle.setBoardId(boardId);
        resetBoardInfo(true, forumArticle);
        forumArticleMapper.updateByArticleId(forumArticle, articleId);
    }

    @Override
    public void delArticle(String articleIds) {
        String[] articleIdArray = articleIds.split(",");
        for (String articleId : articleIdArray) {
            forumArticleService.delArticleSignle(articleId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delArticleSignle(String articleId) {
        ForumArticle article = forumArticleMapper.selectByArticleId(articleId);
        if (null == article || ArticleStatusEnum.DEL.getStatus().equals(article.getStatus())) {
            return;
        }
        ForumArticle updateInfo = new ForumArticle();
        updateInfo.setStatus(ArticleStatusEnum.DEL.getStatus());
        forumArticleMapper.updateByArticleId(updateInfo, articleId);

        Integer integral = SysCacheUtils.getSysSetting().getPostSetting().getPostIntegral();
        if (integral > 0 && ArticleStatusEnum.AUDIT.getStatus().equals(article.getStatus())) {
            userInfoService.updateUserIntegral(article.getUserId(), UserIntegralOperTypeEnum.DEL_ARTICLE, UserIntegralChangeTypeEnum.REDUCE.getChangeType(),
                    integral);
        }
        UserMessage userMessage = new UserMessage();
        userMessage.setReceivedUserId(article.getUserId());
        userMessage.setMessageType(MessageTypeEnum.SYS.getType());
        userMessage.setCreateTime(new Date());
        userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
        userMessage.setMessageContent("文章【" + article.getTitle() + "】被管理员删除");
        userMessageService.add(userMessage);
    }

    @Override
    public void auditArticle(String articleIds) {
        String[] articleIdArray = articleIds.split(",");
        for (String articleId : articleIdArray) {
            forumArticleService.auditArticleSingle(articleId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditArticleSingle(String articleId) {
        ForumArticle article = getForumArticleByArticleId(articleId);
        if (article == null || !ArticleStatusEnum.NO_AUDIT.getStatus().equals(article.getStatus())) {
            return;
        }
        ForumArticle updateInfo = new ForumArticle();
        updateInfo.setStatus(ArticleStatusEnum.AUDIT.getStatus());
        forumArticleMapper.updateByArticleId(updateInfo, articleId);

        Integer integral = SysCacheUtils.getSysSetting().getPostSetting().getPostIntegral();
        if (integral > 0) {
            userInfoService.updateUserIntegral(article.getUserId(), UserIntegralOperTypeEnum.POST_ARTICLE, UserIntegralChangeTypeEnum.ADD.getChangeType(),
                    integral);
        }
    }
}
