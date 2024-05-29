package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.annotation.VerifyParam;
import com.easybbs.controller.base.BaseController;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.FileUploadDto;
import com.easybbs.entity.enums.FileUploadTypeEnum;
import com.easybbs.entity.po.ForumBoard;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.service.ForumBoardService;
import com.easybbs.utils.FileUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

@RestController
@RequestMapping("/board")
public class ForumBoardController extends BaseController {

    @Resource
    private ForumBoardService forumBoardService;

    @Resource
    private FileUtils fileUtils;

    @RequestMapping("/loadBoard")
    public ResponseVO loadBoard() {
        return getSuccessResponseVO(forumBoardService.getBoardTree(null));
    }

    @RequestMapping("/saveBoard")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO saveBoard(Integer boardId,
                                @VerifyParam(required = true) Integer pBoardId,
                                @VerifyParam(required = true) String boardName,
                                String boardDesc,
                                Integer postType,
                                MultipartFile cover) {
        ForumBoard forumBoard = new ForumBoard();
        forumBoard.setBoardId(boardId);
        forumBoard.setpBoardId(pBoardId);
        forumBoard.setBoardName(boardName);
        forumBoard.setBoardDesc(boardDesc);
        forumBoard.setPostType(postType);
        if (cover != null) {
            FileUploadDto uploadDto = fileUtils.uploadFile2Local(cover, FileUploadTypeEnum.ARTICLE_COVER, Constants.FILE_FOLDER_IMAGE);
            forumBoard.setCover(uploadDto.getLocalPath());
        }
        forumBoardService.saveForumBoard(forumBoard);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/delBoard")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO delBoard(@VerifyParam(required = true) Integer boardId) {
        forumBoardService.deleteForumBoardByBoardId(boardId);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/changeBoardSort")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO changeSort(@VerifyParam(required = true) String boardIds) {
        forumBoardService.changeSort(boardIds);
        return getSuccessResponseVO(null);
    }
}
