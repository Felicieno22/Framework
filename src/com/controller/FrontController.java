package com.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.annotation.FormParam;
import com.annotation.Param;
import com.annotation.RequestBody;
import com.mapping.Mapping;
import com.mapping.MySession;
import com.mapping.Utilities;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class FrontController extends HttpServlet {
    List<String> controllerList;
    HashMap<String, Mapping> urlMethod;
    Utilities utl;

    // protected void getControllerList(String package_name) throws ServletException, ClassNotFoundException {
    //     String bin_path = "WEB-INF/classes/" + package_name.replace(".", "/");
    //     bin_path = getServletContext().getRealPath(bin_path);
    //     File b = new File(bin_path);
    //     controllerList.clear();
        
    //     for (File onefile : b.listFiles()) {
    //         if (onefile.isFile() && onefile.getName().endsWith(".class")) {
    //             Class<?> clazz = Class.forName(package_name + "." + onefile.getName().split(".class")[0]);
    //             if (clazz.isAnnotationPresent(Controller.class))
    //                 controllerList.addAll(clazz);

    //             for (Method method : clazz.getMethods()) {
    //                 if (method.isAnnotationPresent((Class<? extends Annotation>) Get.class)) {
    //                     Mapping mapping = new Mapping();
    //                     String key = method.getAnnotation(Get.class).value();  
    //                     if (urlMappings.containsKey(key)) {
    //                         throw new ServletException("La methode '"+urlMappings.get(key).getMethod().getName()+"' possede deja l'URL '"+key+"' comme annotation, donc elle ne peux pas etre assigner a la methode '"+mapping.getMethod().getName()+"'");
    //                     } else {
    //                         urlMappings.put(key, mapping);
    //                     }
    //                 }
    //             }
    //         }
    //     }
    // }
    protected Object invoke_Method(HttpServletRequest request, String className, Method method) throws IOException, NoSuchMethodException {
        Object returnValue = null;
        try {
            Class<?> clazz = Class.forName(className);
            method.setAccessible(true);
    
            Parameter[] methodParams = method.getParameters();
            Object[] args = new Object[methodParams.length];
    
            Enumeration<String> params = request.getParameterNames();
            Map<String, String> paramMap = new HashMap<>();

    
            while (params.hasMoreElements()) {
                String paramName = params.nextElement();
                paramMap.put(paramName, request.getParameter(paramName));
            }
    
            for (int i = 0; i < methodParams.length; i++) {
                if (methodParams[i].getType().equals(MySession.class)) {
                    HttpSession session = request.getSession();
                    MySession mySession = new MySession(session);
                    args[i] = mySession;
                } else if (methodParams[i].isAnnotationPresent(RequestBody.class)) {
                    Class<?> paramType = methodParams[i].getType();
                    Object paramObject = paramType.getDeclaredConstructor().newInstance();
                    for (Field field : paramType.getDeclaredFields()) {
                        String paramName = field.isAnnotationPresent(FormParam.class) ? field.getAnnotation(FormParam.class).value() : field.getName();
                        if (paramMap.containsKey(paramName)) {
                            field.setAccessible(true);
                            field.set(paramObject, paramMap.get(paramName));
                        }
                    }
                    args[i] = paramObject;
                } else if (methodParams[i].isAnnotationPresent(Param.class)) {
                    String paramName = methodParams[i].getAnnotation(Param.class).name();
                    String paramValue = paramMap.get(paramName);
                    args[i] = paramValue;
                } else {

                    
                    if (paramMap.containsKey(methodParams[i].getName())) {
                        args[i] = paramMap.get(methodParams[i].getName());
                    } else {
                        args[i] = null;
                    }
                }
            }
    
            Object instance = clazz.getDeclaredConstructor().newInstance();
            returnValue = method.invoke(instance, args);
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return returnValue;
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
