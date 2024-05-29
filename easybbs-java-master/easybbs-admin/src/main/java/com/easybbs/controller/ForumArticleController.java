package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.annotation.VerifyParam;
import com.easybbs.controller.base.BaseController;
import com.easybbs.entity.config.AdminConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.po.ForumArticle;
import com.easybbs.entity.po.ForumArticleAttachment;
import com.easybbs.entity.query.ForumArticleAttachmentQuery;
import com.easybbs.entity.query.ForumArticleQuery;
import com.easybbs.entity.query.ForumCommentQuery;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.service.ForumArticleAttachmentService;
import com.easybbs.service.ForumArticleService;
import com.easybbs.service.ForumCommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/forum")
public class ForumArticleController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(ForumArticleController.class);

    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private ForumCommentService forumCommentService;

    @Resource
    private ForumArticleAttachmentService forumArticleAttachmentService;

    @Resource
    private AdminConfig adminConfig;

    @RequestMapping("/loadArticle")
    public ResponseVO loadArticle(ForumArticleQuery articleQuery) {
        articleQuery.setOrderBy("post_time desc");
        return getSuccessResponseVO(forumArticleService.findListByPage(articleQuery));
    }


    @RequestMapping("/delArticle")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delArticle(@VerifyParam(required = true) String articleIds) {
        forumArticleService.delArticle(articleIds);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/updateBoard")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO updateBoard(@VerifyParam(required = true) String articleId, @VerifyParam(required = true) Integer pBoardId, Integer boardId) {
        boardId = boardId == null ? 0 : boardId;
        forumArticleService.updateBoard(articleId, pBoardId, boardId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/getAttachment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getAttachment(@VerifyParam(required = true) String articleId) {
        ForumArticleAttachmentQuery articleAttachmentQuery = new ForumArticleAttachmentQuery();
        articleAttachmentQuery.setArticleId(articleId);
        List<ForumArticleAttachment> attachmentList = forumArticleAttachmentService.findListByParam(articleAttachmentQuery);
        if (attachmentList.isEmpty()) {
            throw new BusinessException("附件不存在");
        }
        return getSuccessResponseVO(attachmentList.get(0));
    }

    @RequestMapping("/attachmentDownload")
    @GlobalInterceptor(checkParams = true)
    public void attachmentDownload(HttpServletRequest request, HttpServletResponse response, @VerifyParam(required = true) String fileId) {
        ForumArticleAttachment attachment = forumArticleAttachmentService.getForumArticleAttachmentByFileId(fileId);
        InputStream in = null;
        OutputStream out = null;
        String downloadFileName = attachment.getFileName();
        String filePath = adminConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + Constants.FILE_FOLDER_ATTACHMENT + attachment.getFilePath();
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

    /**
     * @Description: 文章置顶
     * @auther: 程序员老罗
     * @date: 2023/1/10
     * @param: [topType, articleId]
     * @return: com.easybbs.entity.vo.ResponseVO
     */
    @RequestMapping("/topArticle")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO topArticle(@VerifyParam(required = true) Integer topType, @VerifyParam(required = true) String articleId) {
        ForumArticle forumArticle = new ForumArticle();
        forumArticle.setTopType(topType);
        forumArticleService.updateForumArticleByArticleId(forumArticle, articleId);
        return getSuccessResponseVO(null);
    }

    /**
     * @Description: 审核文章
     * @auther: 程序员老罗
     * @date: 2023/1/10
     * @param: [articleId]
     * @return: com.easybbs.entity.vo.ResponseVO
     */
    @RequestMapping("/auditArticle")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO auditArticle(@VerifyParam(required = true) String articleIds) {
        forumArticleService.auditArticle(articleIds);
        return getSuccessResponseVO(null);
    }

    /**
     * @Description: 获取所有评论
     * @auther: 程序员老罗
     * @date: 2023/1/10
     * @param: [commentQuery]
     * @return: com.easybbs.entity.vo.ResponseVO
     */
    @RequestMapping("/loadComment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadComment(ForumCommentQuery commentQuery) {
        commentQuery.setLoadChildren(true);
        commentQuery.setOrderBy("post_time desc");
        return getSuccessResponseVO(forumCommentService.findListByPage(commentQuery));
    }

    /**
     * @Description: 获取文章评论
     * @auther: 程序员老罗
     * @date: 2023/1/10
     * @param: [commentQuery]
     * @return: com.easybbs.entity.vo.ResponseVO
     */
    @RequestMapping("/loadComment4Article")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadComment4Article(ForumCommentQuery commentQuery) {
        commentQuery.setLoadChildren(true);
        commentQuery.setOrderBy("post_time desc");
        commentQuery.setLoadChildren(true);
        commentQuery.setpCommentId(0);
        return getSuccessResponseVO(forumCommentService.findListByParam(commentQuery));
    }

    /**
     * @Description: 删除评论
     * @auther: 程序员老罗
     * @date: 2023/1/10
     * @param: [articleId, commentId]
     * @return: com.easybbs.entity.vo.ResponseVO
     */
    @RequestMapping("/delComment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delComment(@VerifyParam(required = true) String commentIds) {
        forumCommentService.delComment(commentIds);
        return getSuccessResponseVO(null);
    }

    /**
     * @Description: 审核评论
     * @auther: 程序员老罗
     * @date: 2023/1/10
     * @param: [articleId, commentId]
     * @return: com.easybbs.entity.vo.ResponseVO
     */
    @RequestMapping("/auditComment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO auditComment(@VerifyParam(required = true) String commentIds) {
        forumCommentService.auditComment(commentIds);
        return getSuccessResponseVO(null);
    }
}
