package com.xmcc.mllcommon.util;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.codehaus.jackson.type.TypeReference;
import java.io.IOException;
import java.io.Serializable;

@Slf4j
public class JsonUtil {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.disable(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES);
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,false );
        objectMapper.setFilters(new SimpleFilterProvider().setFailOnUnknownId(false));
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_EMPTY);
    }
    //json转字符串
    public static <T> String object2string(T t){
        if (t==null){
            return null;
        }
        try {
            return t instanceof String ? (String) t:objectMapper.writeValueAsString(t);
        } catch (IOException e) {
            log.info("JsonUtil: object2string exception,Object:{},exception:{}",t,e);
            return null;
        }
    }
    //字符串转对象  TypeReference<List<User>> tTypeReference：
    public static <T> T string2object(String str,TypeReference<T> tTypeReference){
        if(str == null){
            return  null;
        }
        try {
            return tTypeReference.getType().equals(String.class)?(T)str: (T) objectMapper.readValue(str, tTypeReference);
        } catch (IOException e) {
            log.info("JsonUtil: string2object exception,String:{},tTypeReference:{},exception:{}",str,tTypeReference,e);
            return null;
        }
    }
}
