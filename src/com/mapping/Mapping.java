package com.mapping;

public class Mapping {
    String className;
    String methodeName;

    public Mapping(String className, String methodeName) {
        this.className = className;
        this.methodeName = methodeName;
    }


    public String getMethodeName() {
        return methodeName;
    }

    public String getClassName(){
        return className;
    }
}

