package ovh.akio.j2sql;


import ovh.akio.j2sql.annotations.AutoIncrement;
import ovh.akio.j2sql.annotations.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class J2SQLField {

    private Object parent = null;
    private Field field = null;
    private boolean isPrimary = false;
    private boolean isAutoIncrement = false;

    private Method setter = null;
    private Method getter = null;


    public J2SQLField(Object parent, Field field) {

        this.parent = parent;

        for (Annotation annotation : field.getAnnotations()) {
            if(annotation instanceof PrimaryKey) {
                isPrimary = true;
            }
            if(annotation instanceof AutoIncrement) {
                isAutoIncrement = true;
            }
        }
        this.field = field;

        this.init();

    }

    public String getName(){
        return field.getName();
    }

    public String getFieldType(){
        return field.getType().getName();
    }

    public Field getField() {
        return field;
    }

    public Object getValue() throws Exception {
        return this.getter.invoke(parent);
    }

    public void setValue(Object value) throws Exception {
        this.setter.invoke(parent, value);
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public boolean isNotPrimary(){
        return !isPrimary;
    }

    public boolean isAutoIncrement() {
        return isAutoIncrement;
    }

    private void init(){
        for (Method method : parent.getClass().getDeclaredMethods()){
            if ((method.getName().startsWith("set")) && (method.getName().length() == (field.getName().length() + 3))){
                if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())){
                    this.setter = method;
                }
            }
        }


        if(field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)){
            for (Method method : parent.getClass().getDeclaredMethods()){
                if ((method.getName().startsWith("is")) && (method.getName().length() == (field.getName().length() + 2))){
                    if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())){
                        this.getter = method;
                    }
                }
            }
        }else{
            for (Method method : parent.getClass().getDeclaredMethods()){
                if ((method.getName().startsWith("get")) && (method.getName().length() == (field.getName().length() + 3))){
                    if (method.getName().toLowerCase().endsWith(field.getName().toLowerCase())){
                        this.getter = method;
                    }
                }
            }
        }
    }

}
