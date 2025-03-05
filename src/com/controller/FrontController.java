<<<<<<< Updated upstream
package com.controller;
=======
package jnd.controller;

import com.google.gson.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jnd.annotation.Annotation;
import jnd.annotation.GET;
import jnd.annotation.POST;
import jnd.annotation.RequestBody;
import jnd.annotation.RequestParam;
import jnd.mapping.Mapping;
import jnd.mapping.ModelView;
import jnd.mapping.MySession;
>>>>>>> Stashed changes

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
<<<<<<< Updated upstream
import java.util.List;

import com.mapping.Mapping;
import com.mapping.Utilities;

import annotation.Controller;
import annotation.Get;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
=======
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.List;

// import org.apache.commons.fileupload.FileItem;
// import org.apache.commons.fileupload.disk.DiskFileItemFactory;
// import org.apache.commons.fileupload.servlet.ServletFileUpload;
// import org.apache.commons.fileupload.servlet.ServletRequestContext;
>>>>>>> Stashed changes

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
<<<<<<< Updated upstream
            utl.runFramework(request, response);
=======
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Annotation.class)) {
                Set<String> methodNames = new HashSet<>();
                Set<String> methodSignatures = new HashSet<>();

                for (Method method : clazz.getDeclaredMethods()) {
                    String url = null;
                    String httpVerb = "GET"; // Par défaut, GET

                    if (method.isAnnotationPresent(GET.class)) {
                        GET getAnnotation = method.getAnnotation(GET.class);
                        url = getAnnotation.value();
                    } else if (method.isAnnotationPresent(POST.class)) {
                        POST postAnnotation = method.getAnnotation(POST.class);
                        url = postAnnotation.value();
                        httpVerb = "POST";
                    }

                    if (url != null) {
                        if (urlMappings.containsKey(url)) {
                            throw new Exception("Duplicate URL mapping found for: " + url);
                        }
                        Mapping mapping = new Mapping(clazz.getName(), method.getName(), httpVerb);
                        urlMappings.put(url, mapping);
                    }

                    // Vérification des méthodes ayant le même nom et le même verbe HTTP
                    String methodSignature = method.getName() + "#" + httpVerb;
                    if (methodSignatures.contains(methodSignature)) {
                        throw new Exception("Duplicate method name and HTTP verb found in class: " + className + " for method: " + method.getName());
                    }
                    methodSignatures.add(methodSignature);

                    // Vérification des méthodes ayant le même nom
                    if (methodNames.contains(method.getName())) {
                        throw new Exception("Duplicate method name found in class: " + className + " for method: " + method.getName());
                    }
                    methodNames.add(method.getName());
                }
            }
        } catch (ClassNotFoundException e) {
            throw new Exception("Class not found: " + className, e);
        }
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PrintWriter out = res.getWriter();
        res.setContentType("text/html");

        try {
            String url = req.getRequestURL().toString();
            String contextPath = req.getContextPath();
            String path = url.substring(url.indexOf(contextPath) + contextPath.length());

            Mapping mapping = urlMappings.get(path);
            if (mapping != null) {
                String requestMethod = req.getMethod();
                if (!mapping.getHttpVerb().equalsIgnoreCase(requestMethod)) {
                    throw new Exception("Incorrect HTTP method for URL: " + path);
                }

                Class<?> clazz = Class.forName(mapping.getClassName());
                Method targetMethod = null;
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.getName().equals(mapping.getMethodName())) {
                        targetMethod = method;
                        break;
                    }
                }

                if (targetMethod == null) {
                    throw new Exception("Méthode non trouvée : " + mapping.getMethodName());
                }

                Object instance = clazz.getDeclaredConstructor().newInstance();
                Object[] methodArgs = getMethodArguments(targetMethod, req);

                // Vérifiez si la requête est de type multipart
                if (req.getContentType() != null && req.getContentType().startsWith("multipart/")) {
                    for (Part part : req.getParts()) {
                        String fileName = part.getSubmittedFileName();
                        if (fileName != null) {
                            // Traiter le fichier téléchargé
                            // Enregistrer sur le disque
                            File uploadedFile = new File("/upload/" + fileName);
                            part.write(uploadedFile.getAbsolutePath());
                        }
                    }
                }

                Object result = targetMethod.invoke(instance, methodArgs);

                if (result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    String viewUrl = modelView.getUrl();
                    Map<String, Object> data = modelView.getData();

                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }

                    RequestDispatcher dispatcher = req.getRequestDispatcher(viewUrl);
                    dispatcher.forward(req, res);
                } else {
                    String jsonResponse = gson.toJson(result);
                    out.print(jsonResponse);
                    out.flush();
                }
            } else {
                throw new Exception("Aucune méthode associée à ce chemin");
            }
>>>>>>> Stashed changes
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
