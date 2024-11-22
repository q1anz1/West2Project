package west2project.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import west2project.result.Result;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static west2project.context.RedisContexts.REDIS_TIME_UNIT;

@Component
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    static {
        stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
    }

    public void rightPushList(String path, Object key, Object value) {
        redisTemplate.opsForList().rightPush(path + key.toString(), JSONUtil.toJsonStr(value));
    }

    public static void incr(String  path, Object key) {
        stringRedisTemplate.opsForValue().increment(path+key.toString());
    }

    public static void incrWithExpire(String  path, Object key, Long time, TimeUnit timeUnit) {
        incr(path, key);
        stringRedisTemplate.expire(path+key, time, timeUnit);
    }

    public <R> R leftPopList(String path, Object key, Class<R> clazz) {
        Object object = redisTemplate.opsForList().leftPop(path + key.toString());
        if (object != null) {
            return JSONUtil.toBean(object.toString(), clazz);
        } else {
            return null;
        }
    }

    public static void writeDataWithTTL(String path, Object key, Object value, Long time) {
        stringRedisTemplate.opsForValue().set(path + key.toString(), value.toString(), time, REDIS_TIME_UNIT);
    }
    public static void writeDataWithTTL(String path, Object key, Object value, Long time , TimeUnit timeUnit) {
        stringRedisTemplate.opsForValue().set(path + key.toString(), value.toString(), time, timeUnit);
    }

    public static void writeData(String path, Object key, Object value) {
        stringRedisTemplate.opsForValue().set(path + key.toString(), value.toString());
    }

    public static String find(String path, Object key) {
        return stringRedisTemplate.opsForValue().get(path + key.toString());
    }

    public static void writeJsonWithTTL(String path, Object key, Object value, Long time) {
        stringRedisTemplate.opsForValue().set(path + key.toString(), JSONUtil.toJsonStr(value), time, REDIS_TIME_UNIT);
    }

    public static void writeJson(String path, Object key, Object value) {
        stringRedisTemplate.opsForValue().set(path + key.toString(), JSONUtil.toJsonStr(value));
    }

    public static <R> R findJson(String path, Object key, Class<R> type) {
        return JSONUtil.toBean(find(path, key.toString()), type);
    }

    public static <R> List<R> findJsonList(String path, Object key, Class<R> type) {
        return JSONUtil.parseArray(find(path, key.toString())).toList(type);
    }

    //缓存空json法，从缓存中读，如果没有就去数据库取
    public static <R, KEY> Result findJsonWithCache(String path, KEY key, Class<R> type, Function<KEY, R> dbFallback, Long minute) {
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
            throw new RuntimeException("资源不存在");
        }
        //数据库中存在，缓存中不存在
        writeJsonWithTTL(path, key.toString(), JSONUtil.toJsonStr(r), minute);
        return Result.success(r);
    }

    public static <R, KEY> Result findJsonListWithCache(String path, KEY key, Class<R> type, Function<KEY, List<R>> dbFallback, Long minute) {
        String json = stringRedisTemplate.opsForValue().get(path + key.toString());

        //如果存在并且含有有意义的信息，返回
        if (StrUtil.isNotBlank(json)) {
            if (json.charAt(0) == '[') {
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
            throw new RuntimeException("资源不存在");
        }
        //数据库中存在，缓存中不存在
        writeJsonWithTTL(path, key.toString(), JSONUtil.toJsonStr(list), minute);
        return Result.success(list);
    }

    //UV
    public static void saveHyperLogLog(String path, Object key, Object value) {
        stringRedisTemplate.opsForHyperLogLog().add(path + key.toString(), value.toString());
    }

    public static Long count(String path, Object key) {
        return stringRedisTemplate.opsForHyperLogLog().size(path + key.toString());
    }
}
