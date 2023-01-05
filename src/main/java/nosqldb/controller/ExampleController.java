package nosqldb.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nosqldb.service.TableService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExampleController {
    /**
     * 可以基于增删改查操作进行封装，实现madbClient
     * @param args
     */
    public static void main(String[] args) {
        JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id","2");
        jsonObject.put("msgid","6");
        array.add(jsonObject);


        TableService.insert("person",array,"id");

        String person = TableService.selectByIndex("person", "2");
        JSONObject parse = (JSONObject) JSONObject.parse(person);
        System.out.println(parse);



        TableService.updateByIndex("person", "2",jsonObject,"id");

        TableService.deleteByIndex("person","2");
    }

}
