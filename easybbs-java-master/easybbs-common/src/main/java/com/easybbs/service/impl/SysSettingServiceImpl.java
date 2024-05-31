package com.easybbs.service.impl;

import com.easybbs.entity.dto.SysSetting4AuditDto;
import com.easybbs.entity.dto.SysSetting4CommentDto;
import com.easybbs.entity.dto.SysSettingDto;
import com.easybbs.entity.enums.PageSize;
import com.easybbs.entity.enums.SysSettingCodeEnum;
import com.easybbs.entity.po.SysSetting;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.entity.query.SysSettingQuery;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.SysSettingMapper;
import com.easybbs.service.SysSettingService;
import com.easybbs.utils.JsonUtils;
import com.easybbs.utils.StringTools;
import com.easybbs.utils.SysCacheUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;


/**
 * 系统设置信息 业务接口实现
 */
@Service("sysSettingService")
public class SysSettingServiceImpl implements SysSettingService {

    private static final Logger logger = LoggerFactory.getLogger(SysSettingServiceImpl.class);

    @Resource
    private SysSettingMapper<SysSetting, SysSettingQuery> sysSettingMapper;

    /**
     * 根据条件查询列表
     */
    @Override
    public List<SysSetting> findListByParam(SysSettingQuery param) {
        return this.sysSettingMapper.selectList(param);
    }

    /**
     * 根据条件查询列表
     */
    @Override
    public Integer findCountByParam(SysSettingQuery param) {
        return this.sysSettingMapper.selectCount(param);
    }

    /**
     * 分页查询方法
     */
    @Override
    public PaginationResultVO<SysSetting> findListByPage(SysSettingQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<SysSetting> list = this.findListByParam(param);
        PaginationResultVO<SysSetting> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
        return result;
    }

    /**
     * 新增
     */
    @Override
    public Integer add(SysSetting bean) {
        return this.sysSettingMapper.insert(bean);
    }

    /**
     * 批量新增
     */
    @Override
    public Integer addBatch(List<SysSetting> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.sysSettingMapper.insertBatch(listBean);
    }

    /**
     * 批量新增或者修改
     */
    @Override
    public Integer addOrUpdateBatch(List<SysSetting> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.sysSettingMapper.insertOrUpdateBatch(listBean);
    }

    /**
     * 根据Code获取对象
     */
    @Override
    public SysSetting getSysSettingByCode(String code) {
        return this.sysSettingMapper.selectByCode(code);
    }

    /**
     * 根据Code修改
     */
    @Override
    public Integer updateSysSettingByCode(SysSetting bean, String code) {
        return this.sysSettingMapper.updateByCode(bean, code);
    }

    /**
     * 根据Code删除
     */
    @Override
    public Integer deleteSysSettingByCode(String code) {
        return this.sysSettingMapper.deleteByCode(code);
    }

    @Override
    public void saveSetting(SysSettingDto sysSettingDto) {
        try {
            Class classz = SysSettingDto.class;
            for (SysSettingCodeEnum codeEnum : SysSettingCodeEnum.values()) {
                PropertyDescriptor pd = new PropertyDescriptor(codeEnum.getPropName(), classz);
                Method method = pd.getReadMethod();
                Object obj = method.invoke(sysSettingDto);
                SysSetting setting = new SysSetting();
                setting.setCode(codeEnum.getCode());
                setting.setJsonContent(JsonUtils.convertObj2Json(obj));
                this.sysSettingMapper.insertOrUpdate(setting);
            }
        } catch (Exception e) {
            logger.error("保存设置失败", e);
            throw new BusinessException("保存设置失败");
        }
    }

    /**
     * 刷新缓存
     * @return
     */
    @Override
    public SysSettingDto refreshCache() {
        try {
            //构造查询条件在数据库中查询设置
            SysSettingDto sysSettingDto = new SysSettingDto();
            List<SysSetting> list = this.sysSettingMapper.selectList(new SysSettingQuery());

            /**
             * 通过枚举类来动态的获取字段
             */
            Class classz = SysSettingDto.class;
            //遍历查询好的list
            for (SysSetting setting : list) {
                //获取json格式的内容
                String jsonContent = setting.getJsonContent();
                if (StringTools.isEmpty(jsonContent)) {
                    continue;
                }
               //获取系统设置的编码
                String code = setting.getCode();
                //根据编码拿到对应的枚举值
                SysSettingCodeEnum codeEnum = SysSettingCodeEnum.getByCode(code);

                //使用 PropertyDescriptor 动态获取 SysSettingDto 类中与枚举值关联的属性的setter方法
                PropertyDescriptor pd = new PropertyDescriptor(codeEnum.getPropName(), classz);
                Method method = pd.getWriteMethod();

                // Class 对象 subClassZ 表示了JSON内容将要被转换成的具体类类型
                Class subClassZ = Class.forName(codeEnum.getClassZ());


                //先通过convertJson2Obj 将对应的json内容读取出来，转换成classZ类型的。之后再通过反射的放法动态地将JSON字符串中的数据映射到 SysSettingDto 对象的相应属性上。
                method.invoke(sysSettingDto, JsonUtils.convertJson2Obj(setting.getJsonContent(), subClassZ));
            }
            //将从数据库中的数据刷新到实体类中
            SysCacheUtils.refresh(sysSettingDto);
            return sysSettingDto;
        } catch (Exception e) {
            logger.error("刷新缓存失败", e);
            throw new BusinessException("刷新缓存失败");
        }
    }

/**
 * 学弟写法，list遍历赋值。效率太低。
 * 修改为上方的定义枚举类之后通过反射进行动态赋值
 * 优点就是如果有新的配置，我们直接在枚举类中新增一个字段就好了，不需要在代码中进行繁琐的取值
  */


//    /**
//     * 刷新缓存
//     * @return
//     */
//    @Override
//    public SysSettingDto refreshCache() {
//        try {
//            //构造查询条件在数据库中查询设置
//            SysSettingDto sysSettingDto = new SysSettingDto();
//            List<SysSetting> list = this.sysSettingMapper.selectList(new SysSettingQuery());
//
//
//            Class classz = SysSettingDto.class;
//            //遍历查询好的list
//            for (SysSetting setting : list) {
//              if(setting.getCode().equals("audit")){
//                  SysSetting4AuditDto auditDto = JsonUtils.convertJson2Obj(setting.getJsonContent(), SysSetting4AuditDto.class);
//                  sysSettingDto.setAuditStting(auditDto);
//              }
//                if(setting.getCode().equals("comment")){
//                    SysSetting4CommentDto commentDto = JsonUtils.convertJson2Obj(setting.getJsonContent(), SysSetting4CommentDto.class);
//                    sysSettingDto.setAuditStting(commentDto);
//                }
//            }
//            //将从数据库中的数据刷新到实体类中
//            SysCacheUtils.refresh(sysSettingDto);
//            return sysSettingDto;
//        } catch (Exception e) {
//            logger.error("刷新缓存失败", e);
//            throw new BusinessException("刷新缓存失败");
//        }
//    }
}