package com.easybbs.service.impl;

import com.easybbs.entity.enums.PageSize;
import com.easybbs.entity.po.ForumArticle;
import com.easybbs.entity.po.ForumBoard;
import com.easybbs.entity.query.ForumArticleQuery;
import com.easybbs.entity.query.ForumBoardQuery;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.ForumArticleMapper;
import com.easybbs.mappers.ForumBoardMapper;
import com.easybbs.service.ForumBoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;


/**
 * 文章板块信息 业务接口实现
 */
@Service("forumBoardService")
public class ForumBoardServiceImpl implements ForumBoardService {

    @Resource
    private ForumBoardMapper<ForumBoard, ForumBoardQuery> forumBoardMapper;

    @Resource
    private ForumArticleMapper<ForumArticle, ForumArticleQuery> forumArticleMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<ForumBoard> findListByParam(ForumBoardQuery param) {
        return this.forumBoardMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(ForumBoardQuery param) {
        return this.forumBoardMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<ForumBoard> findListByPage(ForumBoardQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<ForumBoard> list = this.findListByParam(param);
        PaginationResultVO<ForumBoard> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(ForumBoard bean) {
        return this.forumBoardMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<ForumBoard> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.forumBoardMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<ForumBoard> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.forumBoardMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据BoardId获取对象
     */
    @Override
    public ForumBoard getForumBoardByBoardId(Integer boardId) {
        return this.forumBoardMapper.selectByBoardId(boardId);
    }

    /**
     * 根据BoardId修改
     */
    @Override
    public Integer updateForumBoardByBoardId(ForumBoard bean, Integer boardId) {
        return this.forumBoardMapper.updateByBoardId(bean, boardId);
    }

    /**
     * 根据BoardId删除
     */
    @Override
    public Integer deleteForumBoardByBoardId(Integer boardId) {
        return this.forumBoardMapper.deleteByBoardId(boardId);
    }

    @Override
    public List<ForumBoard> getBoardTree(Integer postType) {
        ForumBoardQuery forumBoardQuery = new ForumBoardQuery();
        forumBoardQuery.setOrderBy("sort asc");
        forumBoardQuery.setPostType(postType);
        List<ForumBoard> forumBoardList = this.forumBoardMapper.selectList(forumBoardQuery);
        return convertLine2Tree(forumBoardList, 0);
    }

    private List<ForumBoard> convertLine2Tree(List<ForumBoard> dataList, Integer pid) {
        List<ForumBoard> children = new ArrayList();
        for (ForumBoard m : dataList) {
            if (m.getpBoardId().equals(pid)) {
                m.setChildren(convertLine2Tree(dataList, m.getBoardId()));
                children.add(m);
            }
        }
        return children;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveForumBoard(ForumBoard forumBoard) {
        if (forumBoard.getBoardId() == null) {
            ForumBoardQuery boardQuery = new ForumBoardQuery();
            boardQuery.setpBoardId(forumBoard.getpBoardId());
            Integer count = this.forumBoardMapper.selectCount(boardQuery);
            forumBoard.setSort(count + 1);
            this.forumBoardMapper.insert(forumBoard);
        } else {
            ForumBoard dbInfo = this.forumBoardMapper.selectByBoardId(forumBoard.getBoardId());
            if (dbInfo == null) {
                throw new BusinessException("板块不存在");
            }
            this.forumBoardMapper.updateByBoardId(forumBoard, forumBoard.getBoardId());
            if (!dbInfo.getBoardName().equals(forumBoard.getBoardName())) {
                forumArticleMapper.updateBoardNameBatch(dbInfo.getpBoardId() == 0 ? 0 : 1, forumBoard.getBoardName(), forumBoard.getBoardId());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeSort(String boardIds) {
        String[] boardIdArray = boardIds.split(",");
        Integer index = 1;
        for (String boardIdStr : boardIdArray) {
            Integer boardId = Integer.parseInt(boardIdStr);
            ForumBoard board = new ForumBoard();
            board.setSort(index);
            forumBoardMapper.updateByBoardId(board, boardId);
            index++;
        }
    }
}