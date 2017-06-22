package me.akio.j2sql;


import me.akio.j2sql.annotations.AutoIncrement;
import me.akio.j2sql.annotations.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class J2SQLField {

    private Object parent = null;
    private Field field = null;
    private boolean isPrimary = false;
    private boolean isAutoIncrement = false;

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
        this.field.setAccessible(true);

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
        return this.field.get(parent);
    }

    public void setValue(Object value) throws Exception {
        this.field.set(parent, value);
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

}
