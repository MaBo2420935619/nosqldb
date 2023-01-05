package nosqldb.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import nosqldb.annotation.NoSqlPrimary;
import nosqldb.service.TableService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
@Slf4j
/**
 * 主键类型必须是String类型
 */
public class NoSqlObjTemplate {
    /**
     * 增
     */
    public<T>  int insert(List<T> list){
        if (list.size()<1){
            return 0;
        }
        T t = list.get(0);
        Class<?> aClass = t.getClass();
        String simpleName = aClass.getSimpleName();
        String tableName = simpleName.toLowerCase();
        String key=getObjKey(t);
        JSONArray array = new JSONArray();

        for (int i = 0; i < list.size(); i++) {
            t = list.get(i);
            JSONObject jsonObject = (JSONObject) JSONObject.toJSON(t);
            array.add(jsonObject);
        }
        int insert = TableService.insert(tableName, array,key);
        return insert;
    }
    /**
     * 增
     */
    public<T>  int insert(T t){
        Class<?> aClass = t.getClass();
        String simpleName = aClass.getSimpleName();
        String tableName = simpleName.toLowerCase();
        JSONArray array = new JSONArray();
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(t);
        array.add(jsonObject);
        String key=getObjKey(t);
        int insert = TableService.insert(tableName, array,key);
        return insert;
    }
    /**
     * 删
     */
    public boolean delete(String key,Class aClass){
        String simpleName = aClass.getSimpleName();
        String tableName = simpleName.toLowerCase();
        return  TableService.deleteByIndex(tableName, key);
    }
    /**
     * 通过主键修改实体
     */
    public <T> boolean update(T t){
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(t);
        Class<?> aClass = t.getClass();
        String simpleName = aClass.getSimpleName();
        String tableName = simpleName.toLowerCase();
        String key= getObjKey(t);
        String value = getObjValue(t);
        int i = TableService.updateByIndex(tableName,value, jsonObject,key);
        if (i==1){
            return true;
        }else {
            return false;
        }
    }
    /**
     * 通过主键查询实体
     */
    public <T>T select(String key,Class<T> aClass){
        String simpleName = aClass.getSimpleName();
        String tableName = simpleName.toLowerCase();
        String s = TableService.selectByIndex(tableName, key);
        T t = JSON.toJavaObject(JSON.parseObject(s), aClass);
        return t;
    }

    /**
     * 通过类对象查询主键名称
     */
    public static  <T> String getObjKey(T t){
        Class<?> aClass = t.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Class<?> type = declaredFields[i].getType();
            if (type.getSimpleName().equals("String")){
                NoSqlPrimary annotation = declaredFields[i].getAnnotation(NoSqlPrimary.class);
                if (annotation!=null){
                    return declaredFields[i].getName();
                }
            }
        }
        return null;
    }

    /**
     * 通过类对象查询 主键对应的值
     */
    public static  <T> String getObjValue(T t){
        Class<?> aClass = t.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            Class<?> type = declaredFields[i].getType();
            if (type.getSimpleName().equals("String")){
                NoSqlPrimary annotation = declaredFields[i].getAnnotation(NoSqlPrimary.class);
                if (annotation!=null){
                    String name = declaredFields[i].getName();
                    Method[] declaredMethods = aClass.getDeclaredMethods();
                    for (int j = 0; j < declaredMethods.length; j++) {
                        if (declaredMethods[j].getName().toLowerCase().equals("get"+name)){
                            try {
                                return String.valueOf(declaredMethods[j].invoke(t));
                            } catch (Exception e) {
                              log.info(e.getMessage(),e);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
