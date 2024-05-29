package com.easybbs.service.impl;

import com.easybbs.entity.dto.UserMessageCountDto;
import com.easybbs.entity.enums.MessageStatusEnum;
import com.easybbs.entity.enums.MessageTypeEnum;
import com.easybbs.entity.enums.PageSize;
import com.easybbs.entity.po.UserMessage;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.entity.query.UserMessageQuery;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.mappers.UserMessageMapper;
import com.easybbs.service.UserMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


/**
 * 用户消息 业务接口实现
 */
@Service("userMessageService")
public class UserMessageServiceImpl implements UserMessageService {

    @Resource
    private UserMessageMapper<UserMessage, UserMessageQuery> userMessageMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<UserMessage> findListByParam(UserMessageQuery param) {
        return this.userMessageMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserMessageQuery param) {
        return this.userMessageMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserMessage> findListByPage(UserMessageQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserMessage> list = this.findListByParam(param);
        PaginationResultVO<UserMessage> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserMessage bean) {
        return this.userMessageMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userMessageMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userMessageMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据MessageId获取对象
     */
    @Override
    public UserMessage getUserMessageByMessageId(Integer messageId) {
        return this.userMessageMapper.selectByMessageId(messageId);
    }

    /**
     * 根据MessageId修改
     */
    @Override
    public Integer updateUserMessageByMessageId(UserMessage bean, Integer messageId) {
        return this.userMessageMapper.updateByMessageId(bean, messageId);
    }

    /**
     * 根据MessageId删除
     */
    @Override
    public Integer deleteUserMessageByMessageId(Integer messageId) {
        return this.userMessageMapper.deleteByMessageId(messageId);
    }

    /**
     * 根据ArticleIdAndCommentIdAndSendUserIdAndMessageType获取对象
     */
    @Override
    public UserMessage getUserMessageByArticleIdAndCommentIdAndSendUserIdAndMessageType(String articleId, Integer commentId, String sendUserId, Integer messageType) {
        return this.userMessageMapper.selectByArticleIdAndCommentIdAndSendUserIdAndMessageType(articleId, commentId, sendUserId, messageType);
    }

    /**
     * 根据ArticleIdAndCommentIdAndSendUserIdAndMessageType修改
     */
    @Override
    public Integer updateUserMessageByArticleIdAndCommentIdAndSendUserIdAndMessageType(UserMessage bean, String articleId, Integer commentId, String sendUserId,
                                                                                       Integer messageType) {
        return this.userMessageMapper.updateByArticleIdAndCommentIdAndSendUserIdAndMessageType(bean, articleId, commentId, sendUserId, messageType);
    }

    /**
     * 根据ArticleIdAndCommentIdAndSendUserIdAndMessageType删除
     */
    @Override
    public Integer deleteUserMessageByArticleIdAndCommentIdAndSendUserIdAndMessageType(String articleId, Integer commentId, String sendUserId, Integer messageType) {
        return this.userMessageMapper.deleteByArticleIdAndCommentIdAndSendUserIdAndMessageType(articleId, commentId, sendUserId, messageType);
    }

    @Override
    public UserMessageCountDto getUserMessageCount(String userId) {
        UserMessageCountDto messageCountDto = new UserMessageCountDto();
        Long totalCount = 0L;
        List<Map> mapList = this.userMessageMapper.selectUserMessageCount(userId);
        for (Map item : mapList) {
            Integer type = (Integer) item.get("messageType");
            Long count = (Long) item.get("count");
            totalCount = totalCount + count;
            if (MessageTypeEnum.SYS.getType().equals(type)) {
                messageCountDto.setSys(count);
            } else if (MessageTypeEnum.COMMENT.getType().equals(type)) {
                messageCountDto.setReply(count);
            } else if (MessageTypeEnum.ARTICLE_LIKE.getType().equals(type)) {
                messageCountDto.setLikePost(count);
            } else if (MessageTypeEnum.COMMENT_LIKE.getType().equals(type)) {
                messageCountDto.setLikeComment(count);
            } else if (MessageTypeEnum.DOWNLOAD_ATTACHMENT.getType().equals(type)) {
                messageCountDto.setDownloadAttachment(count);
            }
        }
        messageCountDto.setTotal(totalCount);
        return messageCountDto;
    }

    @Override
    public void readMessageByType(String receivedUserId, Integer messageType) {
        this.userMessageMapper.updateMessageStatusBatch(null, receivedUserId, messageType, MessageStatusEnum.READ.getStatus());
    }
}