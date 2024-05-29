package com.easybbs.interceptor;

import com.easybbs.entity.config.AdminConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.SessionAdminUserDto;
import com.easybbs.entity.enums.ResponseCodeEnum;
import com.easybbs.exception.BusinessException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


//@Component
public class AppInterceptor implements HandlerInterceptor {

    @Resource
    private AdminConfig adminConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (null == handler) {
            return false;
        }
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        /**
         * 全局拦截，避免方法未设置拦截器，导致权限，日志等操作没有记录  如果不强制要求设置 GlobalInterceptor注解，可去掉次拦截器
         */
        if (request.getRequestURI().indexOf("checkCode") != -1 || request.getRequestURI().indexOf("login") != -1) {
            return true;
        }
        checkLogin();
        return true;
    }

    private void checkLogin() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession session = request.getSession();
        SessionAdminUserDto sessionUser = (SessionAdminUserDto) session.getAttribute(Constants.SESSION_KEY);
        if (sessionUser == null && adminConfig.getDev()) {
            sessionUser = new SessionAdminUserDto();
            sessionUser.setAccount("管理员");
            session.setAttribute(Constants.SESSION_KEY, sessionUser);
        }
        if (null == sessionUser) {
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
