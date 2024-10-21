package com.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mapping.Mapping;
import com.mapping.Utilities;

import annotation.Controller;
import annotation.Get;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    List<String> controllerList;
    HashMap<String, Mapping> urlMethod;
    Utilities utl;

    protected void getControllerList(String package_name) throws ServletException, ClassNotFoundException {
        String bin_path = "WEB-INF/classes/" + package_name.replace(".", "/");
        bin_path = getServletContext().getRealPath(bin_path);
        File b = new File(bin_path);
        controllerList.clear();
        
        for (File onefile : b.listFiles()) {
            if (onefile.isFile() && onefile.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(package_name + "." + onefile.getName().split(".class")[0]);
                if (clazz.isAnnotationPresent(Controller.class))
                    controllerList.add(clazz);

                for (Method method : clazz.getMethods()) {
                    if (method.isAnnotationPresent((Class<? extends Annotation>) Get.class)) {
                        Mapping mapping = new Mapping();
                        // String key = "/"+clazz.getSimpleName()+"/"+method.getName();   
                        String key = method.getAnnotation(Get.class).value();  
                        if (urlMappings.containsKey(key)) {
                            throw new ServletException("La methode '"+urlMappings.get(key).getMethod().getName()+"' possede deja l'URL '"+key+"' comme annotation, donc elle ne peux pas etre assigner a la methode '"+mapping.getMethod().getName()+"'");
                        } else {
                            urlMappings.put(key, mapping);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init() throws ServletException {
        controllerList = new ArrayList<>();
        urlMethod = new HashMap<>();
        utl = new Utilities();
        try {
            utl.initializeControllers(this, this.controllerList, urlMethod);
        } catch (Exception e) {
            
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, Exception {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            utl.runFramework(request, response);
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
