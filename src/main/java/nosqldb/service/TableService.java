package nosqldb.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import nosqldb.tree.KeyAndValue;
import nosqldb.util.FileUtils;
import nosqldb.util.RandomAccessFileUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class TableService {

    private static String filePath="data//";

    private static String definitionFileName ="//tableDefinition.nosqldb";

    private static String dataFileName ="//data.nosqldb";

    private static String indexFileName ="//index.nosqldb";

    public static int listMaxSize=5000;

    public static int insert(String tableName,JSONArray jsonArray,String primary){
        File file = new File(filePath+tableName + dataFileName);
        File fileIndex = new File(filePath+tableName + indexFileName);
        boolean hasIndex=true;
        if (!fileIndex.exists()){
            hasIndex=false;
        }
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        List<KeyAndValue> list=new ArrayList();
        List<String> datas=new ArrayList();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString(primary)==null){
                throw new RuntimeException("主键不存在,主键: "+primary);
            }
            if (hasIndex){
                String value = jsonObject.getString(primary);
                String s = selectByIndex(tableName, value);
                if (s!=null){
                    throw new RuntimeException("主键重复,无法插入数据,重复的主键为:"+value);
                }
            }

            KeyAndValue keyAndValue = new KeyAndValue(jsonObject.getString(primary),"-1");
            list.add(keyAndValue);
            datas.add(jsonArray.getJSONObject(i).toJSONString());
        }


        List<KeyAndValue> objects = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            String data = datas.get(i);
            KeyAndValue keyAndValue1 = list.get(i);
            Long insert = RandomAccessFileUtils.insert(file.getAbsolutePath(), data);
            objects.add(new KeyAndValue(keyAndValue1.getKey(),insert));
            if ((i+1)%10000==0){
                log.info("新增数据当前条数为"+i);
            }
        }
        createIndex(tableName,objects);
        return jsonArray.size();
    }


    public static Boolean deleteByIndex(String tableName,String key){
        String s2 = selectByIndex(tableName, key);
        if (s2==null){
            throw new RuntimeException("数据查询失败,主键为: "+key);
        }
        Long indexStart = getIndexStart(tableName, key);
        boolean b = RandomAccessFileUtils.deleteByIndex(filePath + tableName + dataFileName, indexStart);
        List<String> list = FileUtils.readFile02(filePath + tableName + indexFileName);
        //重新生成索引
        String s1 = FileUtils.readLine(filePath + tableName + indexFileName, 0);
        String[] split1 = s1.split("\\|");
        int count = Integer.parseInt(split1[2]);
        count=count-1;
        FileUtils.saveAsFileWriter(filePath + tableName +indexFileName,"key|position|"+count,false);
        for (int i = 1; i < list.size(); i++) {
            String s = list.get(i);
            String[] split = s.split("\\|");
            if (!split[0].equals(key)){
                FileUtils.saveAsFileWriter(filePath + tableName +indexFileName,s,true);
            }
        }
        return b;
    }

    public static int updateByIndex(String tableName, String key, JSONObject value,String primary){
        Boolean aBoolean = deleteByIndex(tableName, key);
        if (aBoolean){
            JSONArray array = new JSONArray();
            array.add(value);
            int insert = insert(tableName,array,primary);
            return insert;
        }else {
            return 0;
        }
    }

    public static String selectByIndex(String tableName, String key) {
        //优化为二分法查找
        String indexFile = filePath + tableName + indexFileName;
        File file=new File(indexFile);
        if ( !file.exists()){
            log.info("索引文件不存在，无法查找"+indexFile);
            return null;
        }
        List<String> list = FileUtils.readFileToLineGoLine(indexFile, 0, 1);
        String[] split = list.get(0).split("\\|");
        int max = Integer.parseInt(split[2]);
        if(max==0){
            return null;
        }
        int  i = binarySearch(1, max, key, indexFile);
        if (i==-1){
            return null;
        }
        String s = FileUtils.readLine(filePath + tableName + indexFileName, i);
        String[] split1 = s.split("\\|");
        String s1 = RandomAccessFileUtils.selectByIndex(filePath + tableName + dataFileName, Long.valueOf(split1[1]));
        log.info(key+"指针位置为:"+Long.valueOf(split1[1])+"查询到的数据为: "+s1);
        return s1;
    }



    public static Long getIndexStart(String tableName, String key) {
        //优化为二分法查找
        String indexFile = filePath + tableName + indexFileName;
        List<String> list = FileUtils.readFileToLineGoLine(indexFile, 0, 1);
        String[] split = list.get(0).split("\\|");
        int max = Integer.parseInt(split[2]);
        int  i = binarySearch(1, max, key, indexFile);
        if (i==-1){
            return null;
        }
        String s = FileUtils.readLine(filePath + tableName + indexFileName, i);
        String[] split1 = s.split("\\|");
        return Long.valueOf(split1[1]);
    }

    public static void createIndex(String  tableName,List<KeyAndValue> index) {
//        log.info("开始为表"+tableName+"创建索引");
        String indexFilePath = filePath + tableName + indexFileName;
        File file = new File(indexFilePath);
        JSONObject jsonIndex = new JSONObject();
        for (int i = 0; i < index.size(); i++) {
            KeyAndValue keyAndValue = index.get(i);
            String key = keyAndValue.getKey();
            String position = keyAndValue.getValue().toString();
            jsonIndex.put(String.valueOf(key),position);
        }
        int count=0;
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            List<String> strings = FileUtils.readFile02(indexFilePath);
            for (int i = 1; i < strings.size(); i++) {
                String s = strings.get(i);
                String[] split = s.split("\\|");
                String key =String.valueOf(split[0]);
                String position =split[1];
                String string = jsonIndex.getString(key);
                if (string==null){
                    FileUtils.saveAsFileWriter(file.getAbsolutePath(),key+"|"+position,true);
                    jsonIndex.put(String.valueOf(key),position);
                    KeyAndValue keyAndValue = new KeyAndValue(split[0],split[1]);
                    index.add(keyAndValue);
                }
                if (i%1000==0){
                    log.info("还需要将"+(strings.size()-i)+"写入索引");
                }
            }
        }

        Collections.sort(index, new Comparator<KeyAndValue>() {
            @Override
            public int compare(KeyAndValue o1, KeyAndValue o2) {
              return   stringCompare(o1.getKey(),o2.getKey());
            }
        });
        count=jsonIndex.size();
        //索引写入缓存
        FileUtils.saveAsFileWriter(file.getAbsolutePath(),"key|position|"+ count,false);
        for (int i = 0; i < index.size(); i++) {
            KeyAndValue keyAndValue = index.get(i);
            String key = keyAndValue.getKey();
            String position = keyAndValue.getValue().toString();
            FileUtils.saveAsFileWriter(file.getAbsolutePath(),key+"|"+position,true);
        }
    }


    /**
     * 二分法查找
     * @param min
     * @param max
     * @param key
     * @param filePath
     * @return
     */
    public static int binarySearch(int min,int max,String key,String filePath){
        int middle = (max + min) / 2;
        if (max>min){
            String minIndex = getLineIndex(filePath, min);
            String maxIndex = getLineIndex(filePath, max);
            String middleIndex = getLineIndex(filePath, middle);
            String keyIndex = key;
            if (keyIndex.equals(minIndex)){
                return min;
            }else if (keyIndex.equals(maxIndex)){
                return max;
            }
            if (stringCompare(keyIndex,middleIndex)<0||stringCompare(keyIndex,maxIndex)>0){
                return -1;
            }

            if (minIndex.equals(middleIndex)||maxIndex.equals(middleIndex)){
                return -1;
            }
            else {
                if (stringCompare(middleIndex,keyIndex)>0){
                    return binarySearch(min,middle,key,filePath);
                }else  if (stringCompare(middleIndex,keyIndex)<0){
                    return binarySearch(middle,max,key,filePath);
                }
                else {
                    return middle;
                }
            }
        }
        else {
            String minIndex = getLineIndex(filePath, min);
            String maxIndex = getLineIndex(filePath, max);
            String keyIndex = key;
            if (keyIndex.equals(minIndex)){
                return min;
            }
            if (keyIndex.equals(maxIndex)){
                return max;
            }
        }
        return -1;
    }

    public static String  getLineIndex(String filePath,int line){
        String s = FileUtils.readLine(filePath, line);
        String[] split = s.split("\\|");
        String s1 = split[0];
        return s1;
    }


    public static int stringCompare(String s1,String s2){
        if (s1.compareToIgnoreCase(s2)>0){
            return 1;
        }
        return -1;
    }

}
