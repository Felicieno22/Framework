package com.mapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class Mapping {
    String nameClass;
    Method nameMethod; 

    public void add(String n1, String n2) {
        this.nameClass = n1;
        this.nameMethod = n2;
    }

    public String getValue() {
        return nameMethod;
    }

    public String getKey(){
        return nameClass;
    }
    @Override
    public String toString() {
        return "Mapping{" +
                "className='" + nameClass + '\'' +
                ", methodName='" + nameMethod.getName() + '\'' +
                '}';
    }

    public String method_to_string() {
        StringBuilder methodString = new StringBuilder();
        methodString.append(nameMethod.getName()).append("(");

        Parameter[] parameters = nameMethod.getParameters();

        
        for (int i = 0; i < parameters.length; i++) {
            methodString.append(parameters[i].getType().getSimpleName()+" "+parameters[i].getName());
            if (i < parameters.length - 1) {
                methodString.append(", ");
            }
        }

        methodString.append(")");
        return methodString.toString();
    }
}

