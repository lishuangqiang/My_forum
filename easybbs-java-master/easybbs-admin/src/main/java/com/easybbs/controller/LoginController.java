package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.annotation.VerifyParam;
import com.easybbs.controller.base.BaseController;
import com.easybbs.entity.config.AdminConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.CreateImageCode;
import com.easybbs.entity.dto.SessionAdminUserDto;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.utils.StringTools;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@RestController
public class LoginController extends BaseController {

    @Resource
    private AdminConfig adminConfig;


    /**
     * @Description: 验证码
     * @auther: laoluo
     * @date: 17:28 2022/11/20
     * @param: [request, response, session]
     * @return: void
     */
    @RequestMapping(value = "/checkCode")
    public void checkCode(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws
            IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String code = vCode.getCode();
        session.setAttribute(Constants.CHECK_CODE_KEY, code);
        vCode.write(response.getOutputStream());
    }

    /**
     * @Description: 登录
     * @auther: laoluo
     * @date: 17:34 2022/11/20
     * @param: [session, account, password, checkCode]
     * @return: com.easybbs.entity.vo.ResponseVO
     */
    @RequestMapping("/login")
    @GlobalInterceptor(checkParams = true)
    public ResponseVO login(HttpSession session,
                            @VerifyParam(required = true) String account,
                            @VerifyParam(required = true) String password,
                            @VerifyParam(required = true) String checkCode) {
        try {
            if (!checkCode.equalsIgnoreCase((String) session.getAttribute(Constants.CHECK_CODE_KEY))) {
                throw new BusinessException("图片验证码不正确");
            }

            if (!adminConfig.getAdminAccount().equals(account) || !StringTools.encodeByMD5(adminConfig.getAdminPassword()).equals(password)) {
                throw new BusinessException("账号或密码错误");
            }
            SessionAdminUserDto sessionAdminUserDto = new SessionAdminUserDto();
            sessionAdminUserDto.setAccount(account);
            session.setAttribute(Constants.SESSION_KEY, sessionAdminUserDto);
            return getSuccessResponseVO(sessionAdminUserDto);
        } finally {
            session.removeAttribute(Constants.CHECK_CODE_KEY);
        }
    }
}
