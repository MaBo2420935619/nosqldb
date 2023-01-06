package nosqldb.example;

import nosqldb.entity.Person;
import nosqldb.template.NoSqlObjTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 该样例实现了基于实体的增删改查
 * 注意，实体的属性上必须用@NoSqlPrimary标注主键，否则异常
 */
public class ObjExample {

    public static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
    public static void main(String[] args) {
//        insert();


//        insertOne();

//        select();

//        update();
//
//        select();
//
//        delete();
    }

    public static void insert() {
        Person zhangsan = new Person().setId("7").setName("zhangsan");
        NoSqlObjTemplate noSqlObjTemplate = new NoSqlObjTemplate();
//        noSqlObjTemplate.insert(zhangsan);
        int time=1000;
        int max=1000;
        for (int j = 0; j < time; j++) {
            List<Person> list = new ArrayList<>();
            for (int i = 0; i < max; i++) {
                zhangsan = new Person().setId(RandUtil.uuId16()).setName(RandUtil.name());
                list.add(zhangsan);
            }
            noSqlObjTemplate.insert(list);
            System.out.println("当前为第"+j);
            System.out.println("当前时间为"+sdf.format(new Date()));
        }

    }

    public static void insertOne() {
        int max=10;
        long sum=0;
        for (int i = 0; i < max; i++) {
            Date startDate = new Date();
            Person zhangsan =  new Person().setId(RandUtil.uuId16()).setName(RandUtil.name());
            NoSqlObjTemplate noSqlObjTemplate = new NoSqlObjTemplate();
            noSqlObjTemplate.insert(zhangsan);
            Date endDate = new Date();
            long time = endDate.getTime() - startDate.getTime();
            sum +=time;
        }
        System.out.println(max+"条数据插入平均耗时"+sum/max);

    }

    public static void delete() {
        NoSqlObjTemplate noSqlObjTemplate = new NoSqlObjTemplate();
        noSqlObjTemplate.delete("1",Person.class);
    }

    public static void update() {
        Person lisi = new Person().setId("1").setName("lisi");
        NoSqlObjTemplate noSqlObjTemplate = new NoSqlObjTemplate();
        noSqlObjTemplate.update(lisi);
    }

    public static void select() {
        NoSqlObjTemplate noSqlObjTemplate = new NoSqlObjTemplate();
        Person select = noSqlObjTemplate.select("1000000489439888", Person.class);
        System.out.println(select);
    }
}
