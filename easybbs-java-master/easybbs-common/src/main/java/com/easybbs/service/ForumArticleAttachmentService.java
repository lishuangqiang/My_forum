package com.easybbs.service;

import com.easybbs.entity.dto.SessionWebUserDto;
import com.easybbs.entity.po.ForumArticleAttachment;
import com.easybbs.entity.query.ForumArticleAttachmentQuery;
import com.easybbs.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 文件信息 业务接口
 */
public interface ForumArticleAttachmentService {

    /**
     * 根据条件查询列表
     */
    List<ForumArticleAttachment> findListByParam(ForumArticleAttachmentQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(ForumArticleAttachmentQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<ForumArticleAttachment> findListByPage(ForumArticleAttachmentQuery param);

    /**
     * 新增
     */
    Integer add(ForumArticleAttachment bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<ForumArticleAttachment> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<ForumArticleAttachment> listBean);

    /**
     * 根据FileId查询对象
     */
    ForumArticleAttachment getForumArticleAttachmentByFileId(String fileId);


    /**
     * 根据FileId修改
     */
    Integer updateForumArticleAttachmentByFileId(ForumArticleAttachment bean, String fileId);


    /**
     * 根据FileId删除
     */
    Integer deleteForumArticleAttachmentByFileId(String fileId);

    ForumArticleAttachment downloadAttachment(String fileId, SessionWebUserDto sessionWebUserDto);
}