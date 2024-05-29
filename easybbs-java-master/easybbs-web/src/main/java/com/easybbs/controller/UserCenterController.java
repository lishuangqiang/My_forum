package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.annotation.VerifyParam;
import com.easybbs.controller.base.BaseController;
import com.easybbs.entity.dto.SessionWebUserDto;
import com.easybbs.entity.dto.UserMessageCountDto;
import com.easybbs.entity.enums.ArticleStatusEnum;
import com.easybbs.entity.enums.MessageTypeEnum;
import com.easybbs.entity.enums.ResponseCodeEnum;
import com.easybbs.entity.enums.UserStatusEnum;
import com.easybbs.entity.po.ForumArticle;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.query.ForumArticleQuery;
import com.easybbs.entity.query.LikeRecordQuery;
import com.easybbs.entity.query.UserIntegralRecordQuery;
import com.easybbs.entity.query.UserMessageQuery;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.entity.vo.web.ForumArticleVO;
import com.easybbs.entity.vo.web.UserInfoVO;
import com.easybbs.entity.vo.web.UserMessageVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.service.*;
import com.easybbs.utils.CopyTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

@RestController("userCenterController")
@RequestMapping("/ucenter")
public class UserCenterController extends BaseController {

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private ForumArticleService forumArticleService;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private LikeRecordService likeRecordService;

    @Resource
    private UserIntegralRecordService userIntegralRecordService;

    @RequestMapping("/getUserInfo")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO getUserInfo(@VerifyParam(required = true) String userId) {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        if (null == userInfo || UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        ForumArticleQuery articleQuery = new ForumArticleQuery();
        articleQuery.setUserId(userId);
        articleQuery.setStatus(ArticleStatusEnum.AUDIT.getStatus());
        Integer postCount = forumArticleService.findCountByParam(articleQuery);
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setPostCount(postCount);

        LikeRecordQuery recordQuery = new LikeRecordQuery();
        recordQuery.setAuthorUserId(userId);
        Integer likeCount = likeRecordService.findCountByParam(recordQuery);
        userInfoVO.setLikeCount(likeCount);
        userInfoVO.setCurrentIntegral(userInfo.getCurrentIntegral());
        return getSuccessResponseVO(userInfoVO);
    }

    @RequestMapping("/updateUserInfo")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public ResponseVO updateUserInfo(HttpSession session, Integer sex,
                                     @VerifyParam(max = 100) String personDescription,
                                     MultipartFile avatar) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userDto.getUserId());
        userInfo.setSex(sex);
        userInfo.setPersonDescription(personDescription);
        userInfoService.updateUserInfo(userInfo, avatar);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadUserIntegralRecord")
    @GlobalInterceptor(checkParams = true, checkLogin = true)
    public ResponseVO loadUserIntegralRecord(HttpSession session, Integer pageNo, String createTimeStart, String createTimeEnd) {
        UserIntegralRecordQuery recordQuery = new UserIntegralRecordQuery();
        recordQuery.setUserId(getUserInfoFromSession(session).getUserId());
        recordQuery.setPageNo(pageNo);
        recordQuery.setCreateTimeStart(createTimeStart);
        recordQuery.setCreateTimeEnd(createTimeEnd);
        recordQuery.setOrderBy("record_id desc");
        PaginationResultVO resultVO = userIntegralRecordService.findListByPage(recordQuery);
        return getSuccessResponseVO(resultVO);
    }

    @RequestMapping("/loadUserArticle")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO loadUserArticle(HttpSession session,
                                      @VerifyParam(required = true) String userId,
                                      @VerifyParam(required = true) Integer type,
                                      Integer pageNo) {
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        if (null == userInfo || UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException(ResponseCodeEnum.CODE_404);
        }
        ForumArticleQuery articleQuery = new ForumArticleQuery();
        articleQuery.setOrderBy("post_time desc");
        if (type == 0) {
            articleQuery.setUserId(userId);
        } else if (type == 1) {
            articleQuery.setCommentUserId(userId);
        } else if (type == 2) {
            articleQuery.setLikeUserId(userId);
        }
        //当前用户展示待审核
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if (userDto != null) {
            articleQuery.setCurrentUserId(userDto.getUserId());
        } else {
            articleQuery.setStatus(ArticleStatusEnum.AUDIT.getStatus());
        }
        articleQuery.setPageNo(pageNo);
        PaginationResultVO<ForumArticle> result = forumArticleService.findListByPage(articleQuery);
        return getSuccessResponseVO(convert2PaginationVO(result, ForumArticleVO.class));
    }

    @RequestMapping("/getMessageCount")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getMessageCount(HttpSession session) {
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        if (null == userDto) {
            return getSuccessResponseVO(new UserMessageCountDto());
        }
        return getSuccessResponseVO(userMessageService.getUserMessageCount(userDto.getUserId()));
    }

    /**
     * 消息列表
     *
     * @param session
     * @return
     */
    @RequestMapping("/loadMessageList")
    @GlobalInterceptor(checkLogin = true, checkParams = true)
    public ResponseVO loadMessageList(HttpSession session, @VerifyParam(required = true) String code, Integer pageNo) {
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByCode(code);
        if (null == messageTypeEnum) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        SessionWebUserDto userDto = getUserInfoFromSession(session);
        UserMessageQuery userMessageQuery = new UserMessageQuery();
        userMessageQuery.setPageNo(pageNo);
        userMessageQuery.setReceivedUserId(userDto.getUserId());
        userMessageQuery.setMessageType(messageTypeEnum.getType());
        userMessageQuery.setOrderBy("message_id desc");
        PaginationResultVO result = userMessageService.findListByPage(userMessageQuery);
        if (pageNo == null || pageNo == 1) {
            userMessageService.readMessageByType(userDto.getUserId(), messageTypeEnum.getType());
        }
        PaginationResultVO resultVO = convert2PaginationVO(result, UserMessageVO.class);
        return getSuccessResponseVO(resultVO);
    }
}
