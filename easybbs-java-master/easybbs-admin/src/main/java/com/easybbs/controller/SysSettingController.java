package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.annotation.VerifyParam;
import com.easybbs.controller.base.BaseController;
import com.easybbs.entity.config.AdminConfig;
import com.easybbs.entity.dto.*;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.service.SysSettingService;
import com.easybbs.utils.JsonUtils;
import com.easybbs.utils.OKHttpUtils;
import com.easybbs.utils.StringTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/setting")
public class SysSettingController extends BaseController {
    @Resource
    private SysSettingService sysSettingService;

    @Resource
    private AdminConfig adminConfig;

    @RequestMapping("getSetting")
    public ResponseVO getSetting() {
        return getSuccessResponseVO(sysSettingService.refreshCache());
    }

    @RequestMapping("saveSetting")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO saveSetting(@VerifyParam SysSetting4AuditDto auditDto,
                                  @VerifyParam SysSetting4CommentDto commentDto,
                                  @VerifyParam SysSetting4PostDto postDto,
                                  @VerifyParam SysSetting4LikeDto likeDto,
                                  @VerifyParam SysSetting4RegisterDto registerDto,
                                  @VerifyParam SysSetting4EmailDto emailDto) {
        SysSettingDto sysSettingDto = new SysSettingDto();
        sysSettingDto.setAuditStting(auditDto);
        sysSettingDto.setCommentSetting(commentDto);
        sysSettingDto.setPostSetting(postDto);
        sysSettingDto.setLikeSetting(likeDto);
        sysSettingDto.setEmailSetting(emailDto);
        sysSettingDto.setRegisterSetting(registerDto);
        sysSettingService.saveSetting(sysSettingDto);
        sendWebRequest();
        return getSuccessResponseVO(null);
    }

    private void sendWebRequest() {
        String appKey = adminConfig.getInnerApiAppKey();
        String appSecret = adminConfig.getInnerApiAppSecret();
        Long timestamp = System.currentTimeMillis();
        String sign = StringTools.encodeByMD5(appKey + timestamp + appSecret);
        String url = adminConfig.getWebApiUrl() + "?appKey=" + appKey + "&timestamp=" + timestamp + "&sign=" + sign;
        String responseJson = OKHttpUtils.getRequest(url);
        ResponseVO responseVO = JsonUtils.convertJson2Obj(responseJson, ResponseVO.class);
        if (!STATUC_SUCCESS.equals(responseVO.getStatus())) {
            throw new BusinessException("刷新访客端缓存失败");
        }
    }
}
