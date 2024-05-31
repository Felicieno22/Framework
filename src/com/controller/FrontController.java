package com.controller;

import com.annotation.AnnotationController;
import com.annotation.AnnotationGet;
import com.mapping.Mapping;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.reflect.Method;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
public class FrontController extends HttpServlet {
    private HashMap<String, Mapping> urlMappings = new HashMap<>();
    protected ArrayList<String> listeControlleurs = new ArrayList<String>();

    public void getListeControlleurs(String packagename) throws Exception {
        String bin_path = "WEB-INF/classes/" + packagename.replace(".", "/");
        bin_path = getServletContext().getRealPath(bin_path);

        File b = new File(bin_path);
        for (File fichier : b.listFiles()) {
            if (fichier.isFile() && fichier.getName().endsWith(".class")) {
                String className = packagename + "." + fichier.getName().replace(".class", "");
                Class<?> classe = Class.forName(className);
                if (classe.isAnnotationPresent(Controller.class)) {
                    for (Method method : classe.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Get.class)) {
                            Get getAnnotation = method.getAnnotation(Get.class);
                            String url = getAnnotation.value();
                            Mapping mapping = new Mapping(classe.getName(), method.getName());
                            urlMappings.put(url, mapping);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            getListeControlleurs(getServletContext().getInitParameter("controllerPackage"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void processRequested(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PrintWriter out = res.getWriter();
        try {
            String url = req.getRequestURL().toString();
            String contextPath = req.getContextPath();
            String path = url.substring(url.indexOf(contextPath) + contextPath.length());

            out.println("URL: " + url);
            out.println("Path: " + path);

            Mapping mapping = urlMappings.get(path);
            if (mapping != null) {
                out.println("Mapping trouvé : " + mapping);

                
                Class<?> clazz = Class.forName(mapping.getClassName());
                Method method = clazz.getDeclaredMethod(mapping.getMethodeName());

                
                Object instance = clazz.getDeclaredConstructor().newInstance();

                
                String result = (String) method.invoke(instance);

                
                out.println("Résultat de la méthode : " + result);
            } else {
                out.println("Aucune méthode associée à ce chemin");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            out.println("Classe non trouvée : " + e.getMessage());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            out.println("Méthode non trouvée : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Erreur : " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processRequest(req, resp);
    }

}
