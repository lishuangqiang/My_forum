package com.easybbs.service;

import com.easybbs.entity.dto.UserMessageCountDto;
import com.easybbs.entity.po.UserMessage;
import com.easybbs.entity.query.UserMessageQuery;
import com.easybbs.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 用户消息 业务接口
 */
public interface UserMessageService {

    /**
     * 根据条件查询列表
     */
    List<UserMessage> findListByParam(UserMessageQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(UserMessageQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param);

    /**
     * 新增
     */
    Integer add(UserMessage bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<UserMessage> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<UserMessage> listBean);

    /**
     * 根据MessageId查询对象
     */
    UserMessage getUserMessageByMessageId(Integer messageId);


    /**
     * 根据MessageId修改
     */
    Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId);


    /**
     * 根据MessageId删除
     */
    Integer deleteUserMessageByMessageId(Integer messageId);


    /**
     * 根据ArticleIdAndCommentIdAndSendUserIdAndMessageType查询对象
     */
    UserMessage getUserMessageByArticleIdAndCommentIdAndSendUserIdAndMessageType(String articleId, Integer commentId, String sendUserId, Integer messageType);


    /**
     * 根据ArticleIdAndCommentIdAndSendUserIdAndMessageType修改
     */
    Integer updateUserMessageByArticleIdAndCommentIdAndSendUserIdAndMessageType(UserMessage bean, String articleId, Integer commentId, String sendUserId,
                                                                                Integer messageType);


    /**
     * 根据ArticleIdAndCommentIdAndSendUserIdAndMessageType删除
     */
    Integer deleteUserMessageByArticleIdAndCommentIdAndSendUserIdAndMessageType(String articleId, Integer commentId, String sendUserId, Integer messageType);

    UserMessageCountDto getUserMessageCount(String userId);

    void readMessageByType(String receivedUserId, Integer messageType);
}