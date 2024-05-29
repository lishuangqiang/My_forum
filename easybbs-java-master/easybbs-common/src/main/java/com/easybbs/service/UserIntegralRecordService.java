package com.easybbs.service;

import com.easybbs.entity.po.UserIntegralRecord;
import com.easybbs.entity.query.UserIntegralRecordQuery;
import com.easybbs.entity.vo.PaginationResultVO;

import java.util.List;


/**
 * 
 * 用户积分记录表 业务接口
 * 
 */
public interface UserIntegralRecordService {

	/**
	 * 根据条件查询列表
	 */
	List<UserIntegralRecord> findListByParam(UserIntegralRecordQuery param);

	/**
	 * 根据条件查询列表
	 */
	Integer findCountByParam(UserIntegralRecordQuery param);

	/**
	 * 分页查询
	 */
	PaginationResultVO<UserIntegralRecord> findListByPage(UserIntegralRecordQuery param);

	/**
	 * 新增
	 */
	Integer add(UserIntegralRecord bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<UserIntegralRecord> listBean);

	/**
	 * 批量新增/修改
	 */
	Integer addOrUpdateBatch(List<UserIntegralRecord> listBean);

	/**
	 * 根据RecordId查询对象
	 */
	UserIntegralRecord getUserIntegralRecordByRecordId(Integer recordId);


	/**
	 * 根据RecordId修改
	 */
	Integer updateUserIntegralRecordByRecordId(UserIntegralRecord bean,Integer recordId);


	/**
	 * 根据RecordId删除
	 */
	Integer deleteUserIntegralRecordByRecordId(Integer recordId);

}