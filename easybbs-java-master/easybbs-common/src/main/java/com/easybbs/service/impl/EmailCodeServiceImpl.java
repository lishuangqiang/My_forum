package com.easybbs.service.impl;

import com.easybbs.entity.config.WebConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.SysSetting4EmailDto;
import com.easybbs.entity.enums.PageSize;
import com.easybbs.entity.po.EmailCode;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.query.EmailCodeQuery;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.entity.query.UserInfoQuery;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.EmailCodeMapper;
import com.easybbs.mappers.UserInfoMapper;
import com.easybbs.service.EmailCodeService;
import com.easybbs.utils.StringTools;
import com.easybbs.utils.SysCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;


/**
 * 邮箱验证码 业务接口实现
 */
@Service("emailCodeService")
public class EmailCodeServiceImpl implements EmailCodeService {

    private static final Logger logger = LoggerFactory.getLogger(EmailCodeServiceImpl.class);

    @Resource
    private EmailCodeMapper<EmailCode, EmailCodeQuery> emailCodeMapper;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private WebConfig webConfig;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<EmailCode> findListByParam(EmailCodeQuery param) {
        return this.emailCodeMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(EmailCodeQuery param) {
        return this.emailCodeMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<EmailCode> findListByPage(EmailCodeQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<EmailCode> list = this.findListByParam(param);
        PaginationResultVO<EmailCode> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(EmailCode bean) {
        return this.emailCodeMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<EmailCode> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.emailCodeMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<EmailCode> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.emailCodeMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据EmailAndCode获取对象
     */
    @Override
    public EmailCode getEmailCodeByEmailAndCode(String email, String code) {
        return this.emailCodeMapper.selectByEmailAndCode(email, code);
    }

    /**
     * 根据EmailAndCode修改
     */
    @Override
    public Integer updateEmailCodeByEmailAndCode(EmailCode bean, String email, String code) {
        return this.emailCodeMapper.updateByEmailAndCode(bean, email, code);
    }

    /**
     * 根据EmailAndCode删除
     */
    @Override
    public Integer deleteEmailCodeByEmailAndCode(String email, String code) {
        return this.emailCodeMapper.deleteByEmailAndCode(email, code);
    }

    private void sendEmailCode(String toEmail, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            //邮件发件人
            helper.setFrom(webConfig.getSendUserName());
            //邮件收件人 1或多个
            helper.setTo(toEmail);

            SysSetting4EmailDto emailDto = SysCacheUtils.getSysSetting().getEmailSetting();
            //邮件主题
            helper.setSubject(emailDto.getEmailTitle());
            //邮件内容
            helper.setText(String.format(emailDto.getEmailContent(), code));
            //邮件发送时间
            helper.setSentDate(new Date());
            javaMailSender.send(message);
        } catch (Exception e) {
            logger.error("邮件发送失败", e);
            throw new BusinessException("邮件发送失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendEmailCode(String toEmail, Integer type) {
        //如果是注册，校验邮箱是否已存在
        if (type == 0) {
            UserInfo userInfo = userInfoMapper.selectByEmail(toEmail);
            if (null != userInfo) {
                throw new BusinessException("邮箱已经存在");
            }
        }

        String code = StringTools.getRandomNumber(Constants.LENGTH_5);
        if (webConfig.getSendEmailCode() != null && webConfig.getSendEmailCode()) {
            sendEmailCode(toEmail, code);
        }
        emailCodeMapper.disableEmailCode(toEmail);
        EmailCode emailCode = new EmailCode();
        emailCode.setCode(code);
        emailCode.setEmail(toEmail);
        emailCode.setStatus(0);
        emailCode.setCreateTime(new Date());
        emailCodeMapper.insert(emailCode);
    }

    @Override
    public void checkCode(String email, String code) {
        EmailCode emailCode = emailCodeMapper.selectByEmailAndCode(email, code);
        if (null == emailCode) {
            throw new BusinessException("邮箱验证码不正确");
        }
        if (emailCode.getStatus() == 1 || System.currentTimeMillis() - emailCode.getCreateTime().getTime() > Constants.LENGTH_15 * 1000 * 60) {
            throw new BusinessException("邮箱验证码已失效");
        }
        emailCodeMapper.disableEmailCode(email);
    }
}