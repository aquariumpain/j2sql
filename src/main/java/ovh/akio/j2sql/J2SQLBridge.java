package ovh.akio.j2sql;

import ovh.akio.j2sql.annotations.Column;
import ovh.akio.j2sql.database.Connector;
import ovh.akio.j2sql.database.J2SQLStatement;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class J2SQLBridge {

    private static String QUERY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS %1$s (%2$s, PRIMARY KEY (%3$s));";
    private static String QUERY_UPDATE = "UPDATE %1$s SET %2$s WHERE %3$s;";
    private static String QUERY_SELECT = "SELECT * FROM %1$s WHERE %2$s;";
    private static String QUERY_REMOVE = "DELETE FROM %1$s WHERE %2$s;";
    private static String QUERY_INSERT = "INSERT INTO %1$s VALUE (%2$s);";

    private Object data;
    private HashMap<String, String> fieldsType = new HashMap<>();
    private ArrayList<J2SQLField> fieldList = new ArrayList<>();

    public J2SQLBridge(Object data) {

        this.data = data;

        this.fieldsType.put(String.class.getTypeName(), " VARCHAR(191)");
        this.fieldsType.put(Integer.class.getTypeName(), " INT");
        this.fieldsType.put(int.class.getTypeName(), " INT");
        this.fieldsType.put(Float.class.getTypeName(), " FLOAT");
        this.fieldsType.put(float.class.getTypeName(), " FLOAT");
        this.fieldsType.put(Double.class.getTypeName(), " DOUBLE");
        this.fieldsType.put(double.class.getTypeName(), " DOUBLE");
        this.fieldsType.put(Long.class.getTypeName(), " BIGINT");
        this.fieldsType.put(long.class.getTypeName(), " BIGINT");
        this.fieldsType.put(Boolean.class.getTypeName(), " BOOL");
        this.fieldsType.put(boolean.class.getTypeName(), " BOOL");


        for (Field field : this.data.getClass().getDeclaredFields()) {
            for(Annotation annotation : field.getAnnotations()){
                if(annotation instanceof Column) {
                    if(this.fieldsType.containsKey(field.getType().getTypeName())){
                        this.fieldList.add(new J2SQLField(data, field));
                    }else{
                        System.err.println("[J2SQL] Ignoring field [" + field.getName() + "] for the Object [" + data.getClass().getName() + "] : The type of this field is not supported.");
                    }
                }
            }
        }

    }

    private List<J2SQLField> getUniqueColumn() {
        return this.fieldList.stream().filter(J2SQLField::isPrimary).collect(Collectors.toList());
    }

    private List<J2SQLField> getDataColumn() {
        return this.fieldList.stream().filter(J2SQLField::isNotPrimary).collect(Collectors.toList());
    }

    public boolean createTable(Connector database) {

        StringBuilder columns = new StringBuilder();
        StringBuilder primaryKey = new StringBuilder();

        for (J2SQLField j2SQLField : this.fieldList) {
            columns.append("`").append(j2SQLField.getName()).append("` ")
                    .append(fieldsType.get(j2SQLField.getFieldType()));
            if(j2SQLField.isAutoIncrement()){
                columns.append(" AUTO_INCREMENT");
            }
            columns.append(",");
        }

        this.getUniqueColumn().forEach(j2SQLField -> primaryKey.append("`").append(j2SQLField.getName()).append("`,"));

        String _columns = columns.toString().substring(0, columns.toString().length()-1);
        String _primarykey = primaryKey.toString().substring(0, primaryKey.toString().length()-1);

        String query = String.format(QUERY_CREATE_TABLE, this.data.getClass().getName(), _columns, _primarykey);

        try {
            J2SQLStatement statement = database.open(query);
            boolean b = statement.execute();
            statement.close();
            return b;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean insert(Connector database){
        StringBuilder insertData = new StringBuilder();
        for (J2SQLField j2SQLField : this.fieldList) {
            try {
                if(!j2SQLField.isAutoIncrement()){
                    insertData.append("?,");
                }else{
                    insertData.append("NULL,");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        String _insertData = insertData.toString().substring(0, insertData.toString().length()-1);
        try {
            J2SQLStatement statement = database.open(String.format(QUERY_INSERT, this.data.getClass().getName(), _insertData));
            for (J2SQLField j2SQLField : this.fieldList){
                if(!j2SQLField.isAutoIncrement()){
                    try {
                        statement.set(j2SQLField.getValue());
                    }catch (Exception e){
                        e.printStackTrace();
                        return false;
                    }
                }
            }

            boolean b = statement.execute();
            statement.close();
            return b;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(Connector database){
        StringBuilder updatedData = new StringBuilder();
        StringBuilder selectors = new StringBuilder();
        this.getDataColumn().forEach(j2SQLField -> updatedData.append("`").append(j2SQLField.getName()).append("` = ?, "));
        this.getUniqueColumn().forEach(j2SQLField -> selectors.append("`").append(j2SQLField.getName()).append("` = ? AND "));
        String _updatedData = updatedData.toString().substring(0, updatedData.toString().length()-2);
        String _selector = selectors.toString().substring(0, selectors.toString().length()-5);
        try {
            J2SQLStatement statement = database.open(String.format(QUERY_UPDATE, this.data.getClass().getName(), _updatedData, _selector));
            for (J2SQLField j2SQLField : this.getDataColumn())
                statement.set(j2SQLField.getValue());
            for (J2SQLField j2SQLField : this.getUniqueColumn())
                statement.set(j2SQLField.getValue());
            return statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean select(Connector database){
        StringBuilder selectors = new StringBuilder();
        this.getUniqueColumn().forEach(j2SQLField -> selectors.append("`").append(j2SQLField.getName()).append("` = ? AND "));
        String _selector = selectors.toString().substring(0, selectors.toString().length()-5);
        try {
            J2SQLStatement statement = database.open(String.format(QUERY_SELECT, this.data.getClass().getName(),  _selector));
            for (J2SQLField j2SQLField : this.getUniqueColumn())
                statement.set(j2SQLField.getValue());

            ResultSet rs = statement.getResult();

            if(rs.next()){
                for (J2SQLField j2SQLField : this.fieldList) {
                    j2SQLField.setValue(rs.getObject(j2SQLField.getName()));
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(Connector database){
        StringBuilder selectors = new StringBuilder();
        this.getUniqueColumn().forEach(j2SQLField -> selectors.append("`").append(j2SQLField.getName()).append("` = ? AND "));
        String _selector = selectors.toString().substring(0, selectors.toString().length()-5);
        try {
            J2SQLStatement statement = database.open(String.format(QUERY_REMOVE, this.data.getClass().getName(),  _selector));
            for (J2SQLField j2SQLField : this.getUniqueColumn())
                statement.set(j2SQLField.getValue());
            return statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
