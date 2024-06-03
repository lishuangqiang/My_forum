package com.easybbs.service.impl;

import com.easybbs.entity.config.WebConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.SessionWebUserDto;
import com.easybbs.entity.enums.*;
import com.easybbs.entity.po.*;
import com.easybbs.entity.query.*;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.ForumArticleMapper;
import com.easybbs.mappers.ForumCommentMapper;
import com.easybbs.mappers.UserInfoMapper;
import com.easybbs.mappers.UserIntegralRecordMapper;
import com.easybbs.service.EmailCodeService;
import com.easybbs.service.UserInfoService;
import com.easybbs.service.UserMessageService;
import com.easybbs.utils.*;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    @Resource
    private WebConfig webConfig;

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

    @Resource
    private UserIntegralRecordMapper<UserIntegralRecord, UserIntegralRecordQuery> userIntegralRecordMapper;

    @Resource
    private FileUtils fileUtils;

    @Resource
    private EmailCodeService emailCodeService;

    @Resource
    private UserMessageService userMessageService;

    @Resource
    private ForumArticleMapper<ForumArticle, ForumArticleQuery> forumArticleMapper;

    @Resource
    private ForumCommentMapper<ForumComment, ForumCommentQuery> forumCommentMapper;

    /**
     * 根据条件查询列表
     */
    @Override

    public List<UserInfo> findListByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(UserInfoQuery param) {
        return this.userInfoMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<UserInfo> list = this.findListByParam(param);
        PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(UserInfo bean) {
        return this.userInfoMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<UserInfo> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.userInfoMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据UserId获取对象
     */
    @Override
    public UserInfo getUserInfoByUserId(String userId) {
        return this.userInfoMapper.selectByUserId(userId);
    }

    /**
     * 根据UserId修改
     */
    @Override
    public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
        return this.userInfoMapper.updateByUserId(bean, userId);
    }

    /**
     * 根据UserId删除
     */
    @Override
    public Integer deleteUserInfoByUserId(String userId) {
        return this.userInfoMapper.deleteByUserId(userId);
    }

    /**
     * 根据Email获取对象
     */
    @Override
    public UserInfo getUserInfoByEmail(String email) {
        return this.userInfoMapper.selectByEmail(email);
    }

    /**
     * 根据Email修改
     */
    @Override
    public Integer updateUserInfoByEmail(UserInfo bean, String email) {
        return this.userInfoMapper.updateByEmail(bean, email);
    }

    /**
     * 根据Email删除
     */
    @Override
    public Integer deleteUserInfoByEmail(String email) {
        return this.userInfoMapper.deleteByEmail(email);
    }

    /**
     * 根据NickName获取对象
     */
    @Override
    public UserInfo getUserInfoByNickName(String nickName) {
        return this.userInfoMapper.selectByNickName(nickName);
    }

    /**
     * 根据NickName修改
     */
    @Override
    public Integer updateUserInfoByNickName(UserInfo bean, String nickName) {
        return this.userInfoMapper.updateByNickName(bean, nickName);
    }

    /**
     * 根据NickName删除
     */
    @Override
    public Integer deleteUserInfoByNickName(String nickName) {
        return this.userInfoMapper.deleteByNickName(nickName);
    }

    @Override
    public SessionWebUserDto login(String email, String password, String ip) {
        //根据邮箱查询用户
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        //判断密码是否正确
        if (null == userInfo || !userInfo.getPassword().equals(password)) {
            throw new BusinessException("账号或者密码错误");
        }
        //判断用户是否被禁用
        if (UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())) {
            throw new BusinessException("账号已禁用");
        }

        //构造用户实体
        UserInfo updateInfo = new UserInfo();
        //更新上次登录时间
        updateInfo.setLastLoginTime(new Date());
        //记录登录ip
        updateInfo.setLastLoginIp(ip);
        //构造map类，通过ip地址查询用户位置
        Map<String, String> addressInfo = getIpAddress(ip);
        //获取省份简称 pro
        String pro = addressInfo.get("pro");
        //如果省份简称为空  如果查找不到省份的话，就设置为未知
        pro = StringTools.isEmpty(pro) ? Constants.PRO_UNKNOWN : pro;
        //更新上次登录ip地址为pro
        //拿ip用于评论区回显地区
        updateInfo.setLastLoginIpAddress(pro);
        //根据id更新用户
        this.userInfoMapper.updateByUserId(updateInfo, userInfo.getUserId());

        //往返回实体类中设置属性
        SessionWebUserDto sessionWebUserDto = new SessionWebUserDto();
        sessionWebUserDto.setNickName(userInfo.getNickName());
        sessionWebUserDto.setProvince(pro);
        sessionWebUserDto.setUserId(userInfo.getUserId());

        //如果用户是管理员的话
        if (!StringTools.isEmpty(webConfig.getAdminEmails()) && ArrayUtils.contains(webConfig.getAdminEmails().split(","), userInfo.getEmail())) {
            sessionWebUserDto.setAdmin(true);
        } else {
            //如果用户不是管理员的话
            sessionWebUserDto.setAdmin(false);
        }
        return sessionWebUserDto;
    }

    /**
     * 根据ip获取用户省份
     * @param ip
     * @return
     */
    public Map<String, String> getIpAddress(String ip) {
        Map<String, String> addressInfo = new HashMap<>();
        try {
            String url = "http://whois.pconline.com.cn/ipJson.jsp?json=true&ip=" + ip;

            String responseJson = OKHttpUtils.getRequest(url);
            if (StringTools.isEmpty(responseJson)) {
                return addressInfo;
            }
            addressInfo = JsonUtils.convertJson2Obj(responseJson, Map.class);
            return addressInfo;
        } catch (Exception e) {
            logger.error("获取ip所在地失败");
        }
        return addressInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(String email, String nickName, String password, String emailCode) {

        //邮箱不能重复注册
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        if (null != userInfo) {
            throw new BusinessException("邮箱账号已经存在");
        }
        //用户不能重命名
        UserInfo nickNameUser = this.userInfoMapper.selectByNickName(nickName);
        if (null != nickNameUser) {
            throw new BusinessException("昵称已经存在");
        }

        //校验邮箱验证码
        emailCodeService.checkCode(email, emailCode);

        //插入数据库
        String userId = StringTools.getRandomNumber(Constants.LENGTH_10);
        userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setNickName(nickName);
        userInfo.setEmail(email);
        userInfo.setPassword(StringTools.encodeByMD5(password));
        userInfo.setJoinTime(new Date());
        userInfo.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo.setTotalIntegral(0);
        userInfo.setCurrentIntegral(0);
        this.userInfoMapper.insert(userInfo);

        //更新当前用户积分，新增原因为注册，新增五分
        updateUserIntegral(userId, UserIntegralOperTypeEnum.REGISTER, UserIntegralChangeTypeEnum.ADD.getChangeType(), 5);


        //记录消息
        UserMessage userMessage = new UserMessage();
        userMessage.setReceivedUserId(userId);
        userMessage.setMessageType(MessageTypeEnum.SYS.getType());
        userMessage.setCreateTime(new Date());
        userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
        userMessage.setMessageContent(SysCacheUtils.getSysSetting().getRegisterSetting().getRegisterWelcomInfo());
        userMessageService.add(userMessage);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPwd(String email, String password, String emailCode) {
        //查找用户信息
        UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
        //如果查找不到用户
        if (null == userInfo) {
            throw new BusinessException("邮箱账号不存在");
        }

        //校验邮箱验证码
        emailCodeService.checkCode(email, emailCode);

        //更新用户密码
        UserInfo updateInfo = new UserInfo();
        //MD5加密
        updateInfo.setPassword(StringTools.encodeByMD5(password));

        this.userInfoMapper.updateByEmail(updateInfo, email);
    }


    /**
     * 更新用户积分
     * @param userId
     * @param operTypeEnum
     * @param changeType
     * @param integral
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserIntegral(String userId, UserIntegralOperTypeEnum operTypeEnum, Integer changeType, Integer integral) {

        //积分等于修改类型（新增：1，减少：-1） * 分数
        integral = changeType * integral;

        //如果积分为空
        if (integral == 0) {
            return;
        }
        //查找用户
        UserInfo userInfo = userInfoMapper.selectByUserId(userId);
        //如果更新积分的状态是减少，并且用户当前积分+ 待修改后的积分小于0
        if (UserIntegralChangeTypeEnum.REDUCE.getChangeType().equals(changeType) && userInfo.getCurrentIntegral() + integral < 0) {
            //因为我们不能把用户的积分剪成负数，所以如果用户当前积分+ 待修改后的积分小于0，那么就让用户的待减分数等于当前分数就好了，这样就直接减为0了
            integral = changeType * userInfo.getCurrentIntegral();
        }

        //构造用户积分记录类
        UserIntegralRecord record = new UserIntegralRecord();
        //给参数赋值
        record.setUserId(userId);
        record.setOperType(operTypeEnum.getOperType());
        record.setCreateTime(new Date());
        record.setIntegral(integral);
        //插入到积分记录表中
        this.userIntegralRecordMapper.insert(record);
        //从用户表中更新用户积分
        Integer count = this.userInfoMapper.updateIntegral(userId, integral);
        if (count == 0) {
            throw new BusinessException("更新用户积分失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatar) {
        userInfoMapper.updateByUserId(userInfo, userInfo.getUserId());
        if (avatar != null) {
            fileUtils.uploadFile2Local(avatar, FileUploadTypeEnum.AVATAR, userInfo.getUserId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Integer status, String userId) {
        if (UserStatusEnum.DISABLE.getStatus().equals(status)) {
            forumArticleMapper.updateStatusBatchByUserId(ArticleStatusEnum.DEL.getStatus(), userId);
            forumCommentMapper.updateStatusBatchByUserId(CommentStatusEnum.DEL.getStatus(), userId);
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setStatus(status);
        userInfoMapper.updateByUserId(userInfo, userId);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(String userId, String message, Integer integral) {
        UserMessage userMessage = new UserMessage();
        userMessage.setReceivedUserId(userId);
        userMessage.setMessageType(MessageTypeEnum.SYS.getType());
        userMessage.setCreateTime(new Date());
        userMessage.setStatus(MessageStatusEnum.NO_READ.getStatus());
        userMessage.setMessageContent(message);
        userMessageService.add(userMessage);

        UserIntegralChangeTypeEnum changeTypeEnum = UserIntegralChangeTypeEnum.ADD;
        if (integral != null && integral != 0) {
            //扣减积分传入正数
            if (integral < 0) {
                integral = integral * -1;
                changeTypeEnum = UserIntegralChangeTypeEnum.REDUCE;
            }
            updateUserIntegral(userId, UserIntegralOperTypeEnum.ADMIN, changeTypeEnum.getChangeType(), integral);
            dashjkdashj
        }
    }
}
