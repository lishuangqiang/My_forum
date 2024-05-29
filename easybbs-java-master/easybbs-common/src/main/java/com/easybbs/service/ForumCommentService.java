package com.easybbs.service;

import com.easybbs.entity.po.ForumComment;
import com.easybbs.entity.query.ForumCommentQuery;
import com.easybbs.entity.vo.PaginationResultVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * 评论 业务接口
 */
public interface ForumCommentService {

    /**
     * 根据条件查询列表
     */
    List<ForumComment> findListByParam(ForumCommentQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(ForumCommentQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<ForumComment> findListByPage(ForumCommentQuery param);

    /**
     * 新增
     */
    Integer add(ForumComment bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<ForumComment> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<ForumComment> listBean);

    /**
     * 根据CommentId查询对象
     */
    ForumComment getForumCommentByCommentId(Integer commentId);


    /**
     * 根据CommentId修改
     */
    Integer updateForumCommentByCommentId(ForumComment bean, Integer commentId);


    /**
     * 根据CommentId删除
     */
    Integer deleteForumCommentByCommentId(Integer commentId);

    void postComment(ForumComment comment, MultipartFile file);

    void changeTopType(String userId, Integer commentId, Integer topType);

    void delComment(String commentIds);

    void auditComment(String commentId);
}