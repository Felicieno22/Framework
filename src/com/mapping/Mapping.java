package com.mapping;

import java.lang.reflect.Method;

public class Mapping {
    String nameClass;
    Method nameMethod; 

    public void add(String n1, Method n2) {
        this.nameClass = n1;
        this.nameMethod = n2;
    }

    public Method getValue() {
        return nameMethod;
    }

    public String getKey(){
        return nameClass;
    }
}

