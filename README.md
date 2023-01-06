


# 一、nosqldb介绍
**github地址   https://github.com/MaBo2420935619/nosqldb**

**nosqldb是一个用Java实现的基于文件存储的 非关系型数据库管理系统，由个人开发者mabo独立开发**

nosqldb 将不同表的数据保存在不同的目录下，而不是将所有数据放在一个大仓库内，这样就增加了查询速度并提高了灵活性

![image](https://img-blog.csdnimg.cn/img_convert/c5df7e16c7df2f26bf6ed10c93c1bb80.png)


**每个表的目录下有2个文件:**

 - data.nosqldb存储数据
 - index.nosqldb存储索引
如下图所示
![image](https://img-blog.csdnimg.cn/c15b0fe322a342f18e598e3f7da57228.png)




# 二、nosqldb功能介绍
**nosqldb无需对表结构进行定义，即无需创建表就可以进行数据库操作。**
对表数据进行操作支持两种方式，一种是基于对象进行操作，一种是基于json格式的数据进行操作。
如下图所示对person表进行插入操作

 - 对象操作

```java
 public static void insert() {
        Person zhangsan = new Person().setId("1").setName("zhangsan");
        NoSqlObjTemplate noSqlObjTemplate = new NoSqlObjTemplate();
        noSqlObjTemplate.insert(zhangsan);
    }
```

 - json操作

```java
 		JSONArray array = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id","2");
        jsonObject.put("msgid","6");
        array.add(jsonObject);
        TableService.insert("person",array,"id");
```

**nosqldb支持增删改查操作，具体使用样例参考github中example**

# 三、数据存储结构介绍

## 1. 数据文件存储结构(data.nosqldb)
如图所示，数据在文件中以json字符串进行存储，读取的时候在转化为对象或者json进行使用
并且该数据库支持批量存储
![在这里插入图片描述](https://img-blog.csdnimg.cn/e2ab4cf0995043db8f855600f08c00e2.png)


##  2.索引文件存储结构(index.mbdb)
索引文件一条数据存储了三条数据用符号 | 进行分割，key存储数据的唯一id，position存储数据的指针，方便查询数据，表头的999008用于表示数据的数据量大小，因为查询数据采用二分法查询，所以需要记录数据的数据量。

![在这里插入图片描述](https://img-blog.csdnimg.cn/e827bd6a31a34982a72ebd25cfe8012f.png)

#  三、优化点
## 1. 不支持连表查询
## 2. 不支持分片存储
目前数据库的的一张表数据都存储在同一张表中。
可以进行优化，当数据库文件大小超过一定大小时，将文件存储到不同文件中。当使用分片存储时，需要修改索引结构，增加分片编号，查询时根据索引中的分片编号选择文件进行查询。
## 3. 碎片整理
 由于删除操作的存在，删除时为了保证其他数据的位置不发生变化，只是对当前行的数据进行空字符串替代，并没有进行行数据的移动操作。所以当大量数据被删除时，会导致数据库的文件中存在大量的空行。所以删除操作并不会使数据文件变小。所以最好时能进行数据库文件的定时碎片整理。重新整理后，重新生成数据库的索引。


目前该数据库还在使用测试阶段，并且github中提供了该数据库的所有源代码。
欢迎各位大佬提出意见和建议进行数据库优化升级！
