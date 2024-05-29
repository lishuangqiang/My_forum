package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.annotation.VerifyParam;
import com.easybbs.controller.base.BaseController;
import com.easybbs.entity.config.WebConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.SessionWebUserDto;
import com.easybbs.entity.enums.*;
import com.easybbs.entity.po.*;
import com.easybbs.entity.query.ForumArticleAttachmentQuery;
import com.easybbs.entity.query.ForumArticleQuery;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.entity.vo.web.FormArticleDetailVO;
import com.easybbs.entity.vo.web.FormArticleUpdateDetailVO;
import com.easybbs.entity.vo.web.ForumArticleAttachmentVo;
import com.easybbs.entity.vo.web.ForumArticleVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.service.*;
import com.easybbs.utils.CopyTools;
import com.easybbs.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/forum")
public class ForumArticleController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ForumArticleController.class);

    @Resource
    private WebConfig webConfig;

    @Resource
    private ForumBoardService forumBoardService;

    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private ForumArticleAttachmentService forumArticleAttachmentService;

    @Resource
    private ForumArticleAttachmentDownloadService forumArticleAttachmentDownloadService;

    @Resource
    private LikeRecordService likeRecordService;

    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/loadArticle")
    public ResponseVO loadArticle(HttpSession session, Integer boardId, Integer pBoardId, Integer orderType, Integer pageNo) {
        ForumArticleQuery articleQuery = new ForumArticleQuery();
        articleQuery.setBoardId(boardId == null || boardId == 0 ? null : boardId);
        articleQuery.setpBoardId(pBoardId);
        articleQuery.setPageNo(pageNo);

        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if (userDto != null) {
            articleQuery.setCurrentUserId(userDto.getUserId());
        } else {
            articleQuery.setStatus(ArticleStatusEnum.AUDIT.getStatus());
        }
        ArticleOrderTypeEnum orderTypeEnum = ArticleOrderTypeEnum.getByType(orderType);
        orderTypeEnum = orderTypeEnum == null ? ArticleOrderTypeEnum.HOT : orderTypeEnum;
        articleQuery.setOrderBy(orderTypeEnum.getOrderSql());
        PaginationResultVO resultVO = forumArticleService.findListByPage(articleQuery);
        return getSuccessResponseVO(convert2PaginationVO(resultVO, ForumArticleVO.class));
    }


    @RequestMapping("/loadBoard4Post")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadBoard4Post(HttpSession session) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        Integer postType = null;
        if (!userDto.getAdmin()) {
            postType = 1;
        }
        return getSuccessResponseVO(forumBoardService.getBoardTree(postType));
    }

    @RequestMapping("/postArticle")
    @GlobalInterceptor(checkLogin = true, checkParams = true, frequencyType = UserOperFrequencyTypeEnum.POST_ARTICLE)
    public ResponseVO postArticle(HttpSession session,
                                  MultipartFile cover,
                                  MultipartFile attachment,
                                  Integer integral,
                                  @VerifyParam(required = true) Integer pBoardId,
                                  Integer boardId,
                                  @VerifyParam(required = true, max = 150) String title,
                                  @VerifyParam String content,
                                  String markdownContent,
                                  @VerifyParam(required = true) Integer editorType,
                                  @VerifyParam(max = 200) String summary) {

        title = StringTools.escapeTitle(title);
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        ForumArticle forumArticle = new ForumArticle();
        forumArticle.setpBoardId(pBoardId);
        forumArticle.setBoardId(boardId);
        forumArticle.setTitle(title);
        forumArticle.setContent(content);
        if (EditorTypeEnum.MARKDOWN.getType().equals(editorType) && StringTools.isEmpty(markdownContent)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        forumArticle.setMarkdownContent(markdownContent);
        forumArticle.setEditorType(editorType);
        forumArticle.setSummary(summary);
        forumArticle.setUserId(userDto.getUserId());
        forumArticle.setNickName(userDto.getNickName());
        forumArticle.setUserIpAddress(userDto.getProvince());
        //附件信息
        ForumArticleAttachment forumArticleAttachment = new ForumArticleAttachment();
        forumArticleAttachment.setIntegral(integral == null ? 0 : integral);
        forumArticleService.postArticle(userDto.getAdmin(), forumArticle, forumArticleAttachment, cover, attachment);
        return getSuccessResponseVO(forumArticle.getArticleId());
    }


    @RequestMapping("/getArticleDetail")
    public ResponseVO getArticleDetail(HttpSession session, String articleId) {
        SessionWebUserDto sessionWebUserDto = getUserInfoFromSession(session);

        ForumArticle forumArticle = forumArticleService.readArticle(articleId);

        if (forumArticle == null
                || (ArticleStatusEnum.NO_AUDIT.getStatus().equals(forumArticle.getStatus()) && (sessionWebUserDto == null || !sessionWebUserDto.getUserId().equals(forumArticle.getUserId()) && !sessionWebUserDto.getAdmin()))
                || ArticleStatusEnum.DEL.getStatus().equals(forumArticle.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        FormArticleDetailVO detailVO = new FormArticleDetailVO();
        detailVO.setForumArticle(CopyTools.copy(forumArticle, ForumArticleVO.class));

        if (forumArticle.getAttachmentType() == 1) {
            ForumArticleAttachmentQuery articleAttachmentQuery = new ForumArticleAttachmentQuery();
            articleAttachmentQuery.setArticleId(forumArticle.getArticleId());
            List<ForumArticleAttachment> forumArticleAttachmentList = forumArticleAttachmentService.findListByParam(articleAttachmentQuery);
            if (!forumArticleAttachmentList.isEmpty()) {
                detailVO.setAttachment(CopyTools.copy(forumArticleAttachmentList.get(0), ForumArticleAttachmentVo.class));
            }
        }

        if (sessionWebUserDto != null) {
            LikeRecord like = likeRecordService.getUserOperRecordByObjectIdAndUserIdAndOpType(articleId, sessionWebUserDto.getUserId(),
                    OperRecordOpTypeEnum.ARTICLE_LIKE.getType());
            if (like != null) {
                detailVO.setHaveLike(true);
            }
        }
        return getSuccessResponseVO(detailVO);
    }

    @RequestMapping("/doLike")
    @GlobalInterceptor(checkLogin = true, checkParams = true, frequencyType = UserOperFrequencyTypeEnum.DO_LIKE)
    public ResponseVO doLike(HttpSession session, @VerifyParam(required = true) String articleId) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        likeRecordService.doLike(articleId, userDto.getUserId(), userDto.getNickName(), OperRecordOpTypeEnum.ARTICLE_LIKE);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getUserDownloadInfo")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO getUserDownloadInfo(HttpSession session, @VerifyParam(required = true) String fileId) {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(getUserInfoFromSession(session).getUserId());
        Map<String, Object> result = new HashMap();
        result.put("userIntegral", userInfo.getCurrentIntegral());
        ForumArticleAttachmentDownload attachmentDownload = forumArticleAttachmentDownloadService.getForumArticleAttachmentDownloadByFileIdAndUserId(fileId,
                getUserInfoFromSession(session).getUserId());
        result.put("haveDownload", attachmentDownload != null);
        return getSuccessResponseVO(result);
    }

    @RequestMapping("/attachmentDownload")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public void attachmentDownload(HttpSession session, HttpServletRequest request, HttpServletResponse response,
                                   @VerifyParam(required = true) String fileId) {
        ForumArticleAttachment attachment = forumArticleAttachmentService.downloadAttachment(fileId, getUserInfoFromSession(session));
        InputStream in = null;
        OutputStream out = null;
        String downloadFileName = attachment.getFileName();
        String filePath = webConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_ATTACHMENT + attachment.getFilePath();
        File file = new File(filePath);
        try {
            in = new FileInputStream(file);
            out = response.getOutputStream();
            response.setContentType("application/x-msdownload; charset=UTF-8");
            // 解决中文文件名乱码问题
            if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0) {//IE浏览器
                downloadFileName = URLEncoder.encode(downloadFileName, "UTF-8");
            } else {
                downloadFileName = new String(downloadFileName.getBytes("UTF-8"), "ISO8859-1");
            }
            response.setHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\"");
            byte[] byteData = new byte[1024];
            int len = 0;
            while ((len = in.read(byteData)) != -1) {
                out.write(byteData, 0, len); // write
            }
            out.flush();
        } catch (Exception e) {
            logger.error("下载异常", e);
            throw new BusinessException("下载失败");
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

            } catch (IOException e) {
                logger.error("IO异常", e);
            }
            try {
                if (out != null) {
                    out.close();
                }

            } catch (IOException e) {
                logger.error("IO异常", e);
            }
        }
    }


    @RequestMapping("/articleDetail4Update")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO articleDetail4Update(HttpSession session, @VerifyParam(required = true) String articleId) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        ForumArticle forumArticle = forumArticleService.getForumArticleByArticleId(articleId);
        if (forumArticle == null || !forumArticle.getUserId().equals(userDto.getUserId())) {
            throw new BusinessException("文章不存在或你无权编辑该文章");
        }
        FormArticleUpdateDetailVO detailVO = new FormArticleUpdateDetailVO();
        detailVO.setForumArticle(forumArticle);
        if (forumArticle.getAttachmentType() == 1) {
            ForumArticleAttachmentQuery articleAttachmentQuery = new ForumArticleAttachmentQuery();
            articleAttachmentQuery.setArticleId(forumArticle.getArticleId());
            List<ForumArticleAttachment> forumArticleAttachmentList = forumArticleAttachmentService.findListByParam(articleAttachmentQuery);
            if (!forumArticleAttachmentList.isEmpty()) {
                detailVO.setAttachment(CopyTools.copy(forumArticleAttachmentList.get(0), ForumArticleAttachmentVo.class));
            }
        }
        return getSuccessResponseVO(detailVO);
    }

    @RequestMapping("/updateArticle")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO updateArticle(HttpSession session,
                                    MultipartFile cover,
                                    MultipartFile attachment,
                                    Integer integral,
                                    @VerifyParam(required = true) String articleId,
                                    @VerifyParam(required = true) Integer pBoardId,
                                    Integer boardId,
                                    @VerifyParam(required = true, max = 150) String title,
                                    @VerifyParam(required = true) String content,
                                    String markdownContent,
                                    @VerifyParam(required = true) Integer editorType,
                                    @VerifyParam(max = 200) String summary,
                                    @VerifyParam(required = true) Integer attachmentType) {
        title = StringTools.escapeTitle(title);
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        ForumArticle forumArticle = new ForumArticle();
        forumArticle.setArticleId(articleId);
        forumArticle.setpBoardId(pBoardId);
        forumArticle.setBoardId(boardId);
        forumArticle.setTitle(title);
        forumArticle.setContent(content);
        forumArticle.setMarkdownContent(markdownContent);
        forumArticle.setEditorType(editorType);
        forumArticle.setSummary(summary);
        forumArticle.setUserIpAddress(userDto.getProvince());
        forumArticle.setAttachmentType(attachmentType);
        forumArticle.setUserId(userDto.getUserId());
        //附件信息
        ForumArticleAttachment forumArticleAttachment = new ForumArticleAttachment();
        forumArticleAttachment.setIntegral(integral == null ? 0 : integral);

        forumArticleService.updateArticle(userDto.getAdmin(), forumArticle, forumArticleAttachment, cover, attachment);
        return getSuccessResponseVO(forumArticle.getArticleId());
    }

    /**
     * 搜索
     *
     * @param keyword
     * @return
     */
    @RequestMapping("/search")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO updateArticle(@VerifyParam(required = true, min = 3) String keyword) {
        ForumArticleQuery query = new ForumArticleQuery();
        query.setTitleFuzzy(keyword);
        PaginationResultVO result = forumArticleService.findListByPage(query);
        return getSuccessResponseVO(result);
    }
}
