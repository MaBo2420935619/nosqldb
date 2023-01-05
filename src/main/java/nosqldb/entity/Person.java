package nosqldb.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import nosqldb.annotation.NoSqlPrimary;
@Accessors(chain = true)
@Data
public class Person {

    @NoSqlPrimary //标注主键
    String id;

    String name;
}
