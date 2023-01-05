package nosqldb.example;

import nosqldb.entity.Person;
import nosqldb.template.NoSqlObjTemplate;

/**
 * 该样例实现了基于实体的增删改查
 * 注意，实体的属性上必须用@NoSqlPrimary标注主键，否则异常
 */
public class ObjExample {
    public static void main(String[] args) {
        insert();

        select();

        update();

        select();

//        delete();
    }

    public static void insert() {
        Person zhangsan = new Person().setId("1").setName("zhangsan");
        NoSqlObjTemplate noSqlObjTemplate = new NoSqlObjTemplate();
        noSqlObjTemplate.insert(zhangsan);
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
        noSqlObjTemplate.select("1",Person.class);
    }
}
