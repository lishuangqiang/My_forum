package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.annotation.VerifyParam;
import com.easybbs.controller.base.BaseController;
import com.easybbs.entity.dto.SessionWebUserDto;
import com.easybbs.entity.enums.*;
import com.easybbs.entity.po.ForumComment;
import com.easybbs.entity.po.LikeRecord;
import com.easybbs.entity.query.ForumCommentQuery;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.service.ForumCommentService;
import com.easybbs.service.LikeRecordService;
import com.easybbs.utils.StringTools;
import com.easybbs.utils.SysCacheUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class ForumCommentController extends BaseController {

    @Resource
    private ForumCommentService forumCommentService;

    @Resource
    private LikeRecordService likeRecordService;


    @RequestMapping("/loadComment")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadComment(HttpSession session, @VerifyParam(required = true) String articleId, Integer pageNo, Integer orderType) {
        if (!SysCacheUtils.getSysSetting().getCommentSetting().getCommentOpen()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        ForumCommentQuery commentQuery = new ForumCommentQuery();
        commentQuery.setArticleId(articleId);
        commentQuery.setLoadChildren(true);
        String orderBy = orderType == null || orderType == 0 ? "good_count desc,comment_id asc" : "comment_id desc";
        commentQuery.setOrderBy("top_type desc," + orderBy);
        commentQuery.setPageNo(pageNo);

        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if (userDto != null) {
            commentQuery.setQueryLikeType(true);
            commentQuery.setCurrentUserId(userDto.getUserId());
        } else {
            commentQuery.setStatus(CommentStatusEnum.AUDIT.getStatus());
        }
        commentQuery.setPageSize(PageSize.SIZE50.getSize());
        commentQuery.setStatus(CommentStatusEnum.AUDIT.getStatus());
        commentQuery.setpCommentId(0);
        return getSuccessResponseVO(forumCommentService.findListByPage(commentQuery));
    }

    /**
     * 发表评论
     *
     * @param session
     * @param image
     * @param articleId
     * @param pCommentId
     * @param content
     * @param replyUserId
     * @return
     */
    @RequestMapping("/postComment")
    @GlobalInterceptor(checkLogin = true, checkParams = true, frequencyType = UserOperFrequencyTypeEnum.POST_COMMENT)
    public ResponseVO postComment(HttpSession session,
                                  @VerifyParam(required = true) String articleId,
                                  @VerifyParam(required = true) Integer pCommentId,
                                  @VerifyParam(min = 5, max = 800) String content,
                                  String replyUserId, MultipartFile image) {
        //是否关闭评论
        if (!SysCacheUtils.getSysSetting().getCommentSetting().getCommentOpen()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //单独判断内容和图片
        if (image == null && StringTools.isEmpty(content)) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        ForumComment comment = new ForumComment();
        content = StringTools.escapeHtml(content);
        comment.setUserId(userDto.getUserId());
        comment.setNickName(userDto.getNickName());
        comment.setUserIpAddress(userDto.getProvince());
        comment.setpCommentId(pCommentId);
        comment.setArticleId(articleId);
        comment.setContent(content);
        comment.setReplyUserId(replyUserId);
        comment.setTopType(CommentTopTypeEnum.NO_TOP.getType());
        forumCommentService.postComment(comment, image);
        if (pCommentId != 0) {
            ForumCommentQuery commentQuery = new ForumCommentQuery();
            commentQuery.setArticleId(articleId);
            commentQuery.setpCommentId(pCommentId);
            commentQuery.setOrderBy("top_type desc,comment_id asc");
            commentQuery.setCurrentUserId(userDto.getUserId());
            List<ForumComment> children = forumCommentService.findListByParam(commentQuery);
            return getSuccessResponseVO(children);
        }
        return getSuccessResponseVO(comment);
    }

    @RequestMapping("/doLike")
    @GlobalInterceptor(checkLogin = true, checkParams = true, frequencyType = UserOperFrequencyTypeEnum.DO_LIKE)
    public ResponseVO doLike(HttpSession session,
                             @VerifyParam(required = true) Integer commentId) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        String objectId = String.valueOf(commentId);
        likeRecordService.doLike(objectId, userDto.getUserId(), userDto.getNickName(), OperRecordOpTypeEnum.COMMENT_LIKE);
        LikeRecord userOperRecord = likeRecordService.getUserOperRecordByObjectIdAndUserIdAndOpType(objectId, userDto.getUserId(),
                OperRecordOpTypeEnum.COMMENT_LIKE.getType());
        ForumComment comment = forumCommentService.getForumCommentByCommentId(commentId);
        comment.setLikeType(userOperRecord == null ? null : 1);
        return getSuccessResponseVO(comment);
    }

    @RequestMapping("/changeTopType")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO changeTopType(HttpSession session,
                                    @VerifyParam(required = true) Integer commentId, @VerifyParam(required = true) Integer topType) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        forumCommentService.changeTopType(userDto.getUserId(), commentId, topType);
        return getSuccessResponseVO(null);
    }

}
