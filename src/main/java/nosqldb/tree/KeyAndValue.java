package nosqldb.tree;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyAndValue {
    /*存储索引关键字*/
    private String key;
    /*存储数据*/
    private Object value;

}
