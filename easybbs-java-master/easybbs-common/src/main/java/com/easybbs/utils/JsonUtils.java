package com.easybbs.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.easybbs.entity.enums.ResponseCodeEnum;
import com.easybbs.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    public static String convertObj2Json(Object obj) {
        return JSON.toJSONString(obj);
    }

    /**
     * 把json格式的字符串转化成为对象
     * @param json
     * @param classz
     * @return
     * @param <T>
     * @throws BusinessException
     */
    public static <T> T convertJson2Obj(String json, Class<T> classz) throws BusinessException {
        try {
            return JSONObject.parseObject(json, classz);
        } catch (Exception e) {
            logger.error("convertJson2Obj异常，json:{}", json);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    /**
     * 把json数组转化为一个List集合
     * @param json
     * @param classz
     * @return
     * @param <T>
     * @throws BusinessException
     */
    public static <T> List<T> convertJsonArray2List(String json, Class<T> classz) throws BusinessException {
        try {
            return JSONArray.parseArray(json, classz);
        } catch (Exception e) {
            logger.error("convertJsonArray2List,json:{}", json, e);
            throw new BusinessException(ResponseCodeEnum.CODE_601);
        }
    }

    public static void main(String[] args) {

    }
}
