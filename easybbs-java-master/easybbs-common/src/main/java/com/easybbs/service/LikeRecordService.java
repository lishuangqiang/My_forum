package com.easybbs.service;

import com.easybbs.entity.enums.OperRecordOpTypeEnum;
import com.easybbs.entity.po.LikeRecord;
import com.easybbs.entity.query.LikeRecordQuery;
import com.easybbs.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 用户操作记录 业务接口
 */
public interface LikeRecordService {

    /**
     * 根据条件查询列表
     */
    List<LikeRecord> findListByParam(LikeRecordQuery param);

    /**
     * 根据条件查询列表
     */
    Integer findCountByParam(LikeRecordQuery param);

    /**
     * 分页查询
     */
    PaginationResultVO<LikeRecord> findListByPage(LikeRecordQuery param);

    /**
     * 新增
     */
    Integer add(LikeRecord bean);

    /**
     * 批量新增
     */
    Integer addBatch(List<LikeRecord> listBean);

    /**
     * 批量新增/修改
     */
    Integer addOrUpdateBatch(List<LikeRecord> listBean);

    /**
     * 根据OpId查询对象
     */
    LikeRecord getUserOperRecordByOpId(Integer opId);


    /**
     * 根据OpId修改
     */
    Integer updateUserOperRecordByOpId(LikeRecord bean, Integer opId);


    /**
     * 根据OpId删除
     */
    Integer deleteUserOperRecordByOpId(Integer opId);


    /**
     * 根据ObjectIdAndUserIdAndOpType查询对象
     */
    LikeRecord getUserOperRecordByObjectIdAndUserIdAndOpType(String objectId, String userId, Integer opType);


    /**
     * 根据ObjectIdAndUserIdAndOpType修改
     */
    Integer updateUserOperRecordByObjectIdAndUserIdAndOpType(LikeRecord bean, String objectId, String userId, Integer opType);


    /**
     * 根据ObjectIdAndUserIdAndOpType删除
     */
    Integer deleteUserOperRecordByObjectIdAndUserIdAndOpType(String objectId, String userId, Integer opType);

    void doLike(String objectId, String userId, String nickName, OperRecordOpTypeEnum opTypeEnum);
}