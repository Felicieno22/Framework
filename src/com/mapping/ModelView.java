package com.mapping;

import java.util.HashMap;
import java.util.Map;

public class ModelView {
    String url;
    HashMap<String,Object> data;

    public ModelView(String url){
        this.url = url;
        this.data = new HashMap<>();
    }

    public void addObjet(String key,Object obj) {
        this.data.put(key,obj);
    }


    public void setUrl(String url) {
        this.url = url;	
    }
    public String getUrl() {
        return url;
    }
    public void setData(HashMap<String,Object> properties) {
        this.data = properties;
    }
    public HashMap<String,Object> getData() {
        return data;
    }
}
