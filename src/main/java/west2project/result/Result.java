package west2project.result;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.Data;
import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private Base base;
    private T data;

    public static<T> Result<T> success() {
        Result<T>result= new Result<>();
        Base base=new Base();
        base.setCode(200);
        base.setMsg("success");
        result.setBase(base);
        return result;
    }
    public static<T> Result<T> success(T object) {
        Result<T> result= new Result<>();
        Base base=new Base();
        base.setCode(200);
        base.setMsg("success");
        result.setBase(base);
        result.setData(object);
        return result;
    }

    public static<T> Result<T> error(String msg) {
        Result<T> result= new Result<>();
        Base base=new Base();
        base.setCode(-1);
        base.setMsg(msg);
        result.setBase(base);
        return result;
    }

    /*
    * 以下为正确的，上面code全是1000或者-1
    * */

    public static<T> Result<T> success(int code) {
        Result<T>result= new Result<>();
        Base base=new Base(code,"success");
        result.setBase(base);
        return result;
    }

    public static<T> Result<T> success(int code,T object) {
        Result<T> result= new Result<>();
        Base base=new Base(code,"success");
        result.setBase(base);
        result.setData(object);
        return result;
    }

    public static<T> Result<T> error(int code,String msg) {
        Result<T> result= new Result<>();
        Base base=new Base(code,msg);
        result.setBase(base);
        return result;
    }

    public String asJsonString() {
        return JSONObject.toJSONString(this, SerializerFeature.WriteMapNullValue);
    }
}