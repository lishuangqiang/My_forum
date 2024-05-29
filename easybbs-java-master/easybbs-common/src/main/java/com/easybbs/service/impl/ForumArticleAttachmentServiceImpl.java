package com.easybbs.service.impl;

import com.easybbs.entity.dto.SessionWebUserDto;
import com.easybbs.entity.enums.*;
import com.easybbs.entity.po.*;
import com.easybbs.entity.query.ForumArticleAttachmentDownloadQuery;
import com.easybbs.entity.query.ForumArticleAttachmentQuery;
import com.easybbs.entity.query.ForumArticleQuery;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.ForumArticleAttachmentDownloadMapper;
import com.easybbs.mappers.ForumArticleAttachmentMapper;
import com.easybbs.mappers.ForumArticleMapper;
import com.easybbs.service.ForumArticleAttachmentService;
import com.easybbs.service.UserInfoService;
import com.easybbs.service.UserMessageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


/**
 * 文件信息 业务接口实现
 */
@Service("forumArticleAttachmentService")
public class ForumArticleAttachmentServiceImpl implements ForumArticleAttachmentService {

    @Resource
    private ForumArticleAttachmentMapper<ForumArticleAttachment, ForumArticleAttachmentQuery> forumArticleAttachmentMapper;

    @Resource
    private ForumArticleAttachmentDownloadMapper<ForumArticleAttachmentDownload, ForumArticleAttachmentDownloadQuery> forumArticleAttachmentDownloadMapper;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private ForumArticleMapper<ForumArticle, ForumArticleQuery> forumArticleMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ForumArticleAttachment> findListByParam(ForumArticleAttachmentQuery param) {
        return this.forumArticleAttachmentMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ForumArticleAttachmentQuery param) {
        return this.forumArticleAttachmentMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ForumArticleAttachment> findListByPage(ForumArticleAttachmentQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ForumArticleAttachment> list = this.findListByParam(param);
        PaginationResultVO<ForumArticleAttachment> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ForumArticleAttachment bean) {
        return this.forumArticleAttachmentMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ForumArticleAttachment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.forumArticleAttachmentMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ForumArticleAttachment> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.forumArticleAttachmentMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据FileId获取对象
     */
    @Override
    public ForumArticleAttachment getForumArticleAttachmentByFileId(String fileId) {
        return this.forumArticleAttachmentMapper.selectByFileId(fileId);
    }

    /**
     * 根据FileId修改
     */
    @Override
    public Integer updateForumArticleAttachmentByFileId(ForumArticleAttachment bean, String fileId) {
        return this.forumArticleAttachmentMapper.updateByFileId(bean, fileId);
    }

    /**
     * 根据FileId删除
     */
    @Override
    public Integer deleteForumArticleAttachmentByFileId(String fileId) {
        return this.forumArticleAttachmentMapper.deleteByFileId(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumArticleAttachment downloadAttachment(String fileId, SessionWebUserDto sessionWebUserDto) {
        ForumArticleAttachment attachment = this.forumArticleAttachmentMapper.selectByFileId(fileId);
        if (null == attachment) {
            throw new BusinessException("附件不存在");
        }
        //判断下载积分，如果已经下载过，无需积分
        ForumArticleAttachmentDownload download = null;
        if (attachment.getIntegral() > 0 && !sessionWebUserDto.getUserId().equals(attachment.getUserId())) {
            download = this.forumArticleAttachmentDownloadMapper.selectByFileIdAndUserId(fileId, sessionWebUserDto.getUserId());
            if (download == null) {
                //用户未下载过，判断用户积分
                UserInfo userInfo = userInfoService.getUserInfoByUserId(sessionWebUserDto.getUserId());
                if (userInfo.getCurrentIntegral() - attachment.getIntegral() < 0) {
                    throw new BusinessException("积分不够");
                }
            }
        }
        //记录用户下载
        ForumArticleAttachmentDownload updateDownload = new ForumArticleAttachmentDownload();
        updateDownload.setArticleId(attachment.getArticleId());
        updateDownload.setFileId(attachment.getFileId());
        updateDownload.setUserId(sessionWebUserDto.getUserId());
        updateDownload.setDownloadCount(1);
        this.forumArticleAttachmentDownloadMapper.insertOrUpdate(updateDownload);

        //更新附件下载次数
        this.forumArticleAttachmentMapper.updateDownloadCount(fileId);

        //自己下载自己的附件 无需计算积分
        if (sessionWebUserDto.getUserId().equals(attachment.getUserId())) {
            return attachment;
        }
        if (download != null) {
            return attachment;
        }

        //扣除下载用户积分
        userInfoService.updateUserIntegral(sessionWebUserDto.getUserId(), UserIntegralOperTypeEnum.USER_DOWNLOAD_ATTACHMENT,
                UserIntegralChangeTypeEnum.REDUCE.getChangeType(), attachment.getIntegral());

        //给提供附件的用户增加积分
        userInfoService.updateUserIntegral(attachment.getUserId(), UserIntegralOperTypeEnum.DOWNLOAD_ATTACHMENT,
                UserIntegralChangeTypeEnum.ADD.getChangeType(), attachment.getIntegral());

        //记录消息
        ForumArticle forumArticle = forumArticleMapper.selectByArticleId(attachment.getArticleId());

        UserMessage userMessage = new UserMessage();
        userMessage.setMessageType(MessageTypeEnum.DOWNLOAD_ATTACHMENT.getType());
        userMessage.setCreateTime(new Date());
        userMessage.setArticleId(attachment.getArticleId());
        userMessage.setCommentId(0);
        userMessage.setSendUserId(sessionWebUserDto.getUserId());
        userMessage.setSendNickName(sessionWebUserDto.getNickName());
        userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
        userMessage.setReceivedUserId(attachment.getUserId());
        userMessage.setArticleTitle(forumArticle.getTitle());
        if (!sessionWebUserDto.getUserId().equals(attachment.getUserId())) {
            userMessageService.add(userMessage);
        }

        return attachment;
    }
}