package west2project.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import west2project.context.Contexts;
import west2project.result.Result;

import java.util.List;
import java.util.function.Function;

@Component

public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate, StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void rightPushList(String path, Object key, List<String> list) {
        redisTemplate.opsForList().rightPush(path + key.toString(), list);
    }
    public void rightPushList(String path, Object key, Object value) {
        redisTemplate.opsForList().rightPush(path + key.toString(), value.toString());
    }

    public Object leftPopList(String path, Object key) {
        return redisTemplate.opsForList().leftPop(path+key.toString());
    }
    public void writeDataWithTTL(String path, Object key, Object value, Long time) {
        stringRedisTemplate.opsForValue().set(path + key.toString(), value.toString(), time, Contexts.REDIS_TIME_UNIT);
    }
    public void writeData(String path, Object key, Object value) {
        stringRedisTemplate.opsForValue().set(path + key.toString(), value.toString());
    }
    public String find(String path, Object key) {
        return stringRedisTemplate.opsForValue().get(path + key.toString());
    }

    public void writeJsonWithTTL(String path, Object key, Object value, Long time) {
        stringRedisTemplate.opsForValue().set(path + key.toString(), JSONUtil.toJsonStr(value), time, Contexts.REDIS_TIME_UNIT);
    }
    public void writeJson(String path, Object key, Object value) {
        stringRedisTemplate.opsForValue().set(path + key.toString(), JSONUtil.toJsonStr(value));
    }

    public <R> R findJson(String path, Object key, Class<R> type) {
        return JSONUtil.toBean(find(path, key.toString()), type);
    }
    public <R> List<R> findJsonList(String path, Object key, Class<R> type) {
        return JSONUtil.parseArray(find(path,key.toString())).toList(type);
    }

    //缓存空json法，从缓存中读，如果没有就去数据库取
    public <R, KEY> Result findJsonWithCache(String path, KEY key, Class<R> type, Function<KEY, R> dbFallback, Long minute) {
        String json = stringRedisTemplate.opsForValue().get(path + key.toString());

        //如果存在并且含有有意义的信息，返回
        if (StrUtil.isNotBlank(json)) {
            return Result.success(JSONUtil.toBean(json, type));
        }
        //在前面已经过滤走存在并有意义后，现在存在的都是空json
        if (json != null) {
            return Result.error("不存在");
        }
        //如果压根不存在那就是还没在缓存中，加入缓存
        //从数据库中查询
        R r = dbFallback.apply(key);
        if (r == null) {
            //压根彻底完全不存在
            //将空写入redis
            writeJsonWithTTL(path, key.toString(), "", minute);
            return Result.error("不存在");
        }
        //数据库中存在，缓存中不存在
        writeJsonWithTTL(path, key.toString(), JSONUtil.toJsonStr(r), minute);
        return Result.success(r);
    }
    public <R, KEY> Result findJsonListWithCache(String path, KEY key, Class<R> type, Function<KEY, List<R>> dbFallback, Long minute) {
        String json = stringRedisTemplate.opsForValue().get(path + key.toString());

        //如果存在并且含有有意义的信息，返回
        if (StrUtil.isNotBlank(json)) {
            if(json.charAt(0)=='['){
                JSONArray list = JSONUtil.parseArray(json);
                List<R> re = list.toList(type);
                return Result.success(re);
            }
            return Result.error("不是数组");
        }
        //在前面已经过滤走存在并有意义后，现在存在的都是空json
        if (json != null) {
            return Result.error("不存在");
        }
        //如果压根不存在那就是还没在缓存中，加入缓存
        //从数据库中查询
        List<R> list = dbFallback.apply(key);
        if (list == null) {
            //压根彻底完全不存在
            //将空写入redis
            writeJsonWithTTL(path, key.toString(), "", minute);
            return Result.error("不存在");
        }
        //数据库中存在，缓存中不存在
        writeJsonWithTTL(path, key.toString(), JSONUtil.toJsonStr(list), minute);
        return Result.success(list);
    }
    //UV
    public void saveHyperLogLog(String path, Object key, Object value) {
        stringRedisTemplate.opsForHyperLogLog().add(path + key.toString(), value.toString());
    }

    public Long count(String path, Object key) {
        return stringRedisTemplate.opsForHyperLogLog().size(path + key.toString());
    }
}
