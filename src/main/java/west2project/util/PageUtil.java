package west2project.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
public class PageUtil{
    public static <R> List<R> page(List<R> list, Integer pageSize, Integer pageNum){
        //排除非法输入
        if(pageSize <=0 || pageNum<=0 || list==null ||list.isEmpty()){
            return null;
        }
        //防止查询超出范围的不存在对象
        if(pageSize*(pageNum-1)>=list.size()){
            return null;
        }
        //查询最后一页，且最后一页不是满的
        if(pageSize*pageNum > list.size() && pageSize*(pageNum-1)<list.size()){
            return list.subList(pageSize*(pageNum-1), list.size());
        }
        //常规查询
        return list.subList(pageSize*(pageNum-1),pageSize*pageNum);
    }
}
