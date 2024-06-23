package com.controller;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.annotation.Annotation;
import com.annotation.GET;
import com.annotation.Objet;
import com.annotation.Param;
import com.annotation.Post;
import com.mapping.Mapping;
import com.mapping.ModelView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
    private final List<String> controllerList = new ArrayList<>();
    private final HashMap<String, Mapping> urlMethod = new HashMap<>();
    private final Set<String> verifiedClasses = new HashSet<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        scanControllers(config);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>FrontController</title>");
            out.println("</head>");
            out.println("<body>");

            StringBuffer requestURL = request.getRequestURL();
            String[] requestUrlSplitted = requestURL.toString().split("/");
            String controllerSearched = requestUrlSplitted[requestUrlSplitted.length - 1];

            out.println("<h2>Classe et méthode associée à l'URL :</h2>");
            if (!urlMethod.containsKey(controllerSearched)) {
                out.println("<p>Aucune méthode associée à ce chemin.</p>");
            } else {
                Mapping mapping = urlMethod.get(controllerSearched);
                Class<?> clazz = Class.forName(mapping.getClassName());
                Method method = null;

                // Find the method that matches the request type (GET or POST)
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.getName().equals(mapping.getMethodeName())) {
                        if (request.getMethod().equalsIgnoreCase("GET") && m.isAnnotationPresent(GET.class)) {
                            method = m;
                            break;
                        } else if (request.getMethod().equalsIgnoreCase("POST") && m.isAnnotationPresent(Post.class)) {
                            method = m;
                            break;
                        }
                    }
                }

                if (method == null) {
                    out.println("<p>Aucune méthode correspondante trouvée.</p>");
                    return;
                }

                Object[] parameters = getMethodParameters(method, request);
                Object ob = clazz.getDeclaredConstructor().newInstance();
                Object returnValue = method.invoke(ob, parameters);
                if (returnValue instanceof String) {
                    out.println("La valeur de retour est " + (String) returnValue);
                } else if (returnValue instanceof ModelView) {
                    ModelView modelView = (ModelView) returnValue;
                    for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
                        request.setAttribute(entry.getKey(), entry.getValue());
                    }
                    RequestDispatcher dispatcher = request.getRequestDispatcher(modelView.getUrl());
                    dispatcher.forward(request, response);
                } else {
                    out.println("Type de données non reconnu");
                }
            }

            out.println("</body>");
            out.println("</html>");
        }
    }

    private void scanControllers(ServletConfig config) {
        String controllerPackage = config.getInitParameter("controller-package");
        System.out.println("Scanning package: " + controllerPackage);

        // Scanner les classes du package donné dans WEB-INF/classes
        try {
            String path = "WEB-INF/classes/" + controllerPackage.replace('.', '/');
            File directory = new File(getServletContext().getRealPath(path));
            if (directory.exists()) {
                scanDirectory(directory, controllerPackage);
            } else {
                System.out.println("Le répertoire n'existe pas: " + directory.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scanDirectory(File directory, String packageName) throws Exception {
        System.out.println("Scanning directory: " + directory.getAbsolutePath());

        for (File file : directory.listFiles()) {
            System.out.println("Processing file: " + file.getName());

            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(Annotation.class) && !verifiedClasses.contains(clazz.getName())) {
                        Annotation annotation = clazz.getAnnotation(Annotation.class);
                        controllerList.add(clazz.getName() + " (" + annotation.value() + ")");
                        verifiedClasses.add(clazz.getName());
                        Method[] methods = clazz.getMethods();
                        for (Method m : methods) {
                            if (m.isAnnotationPresent(GET.class)) {
                                Mapping map = new Mapping(className, m.getName());
                                String valeur = m.getAnnotation(GET.class).value();
                                if (urlMethod.containsKey(valeur)) {
                                    throw new Exception("double url" + valeur);
                                } else {
                                    urlMethod.put(valeur, map);
                                }
                            } else if (m.isAnnotationPresent(Post.class)) {
                                Mapping map = new Mapping(className, m.getName());
                                String valeur = m.getAnnotation(Post.class).value();
                                if (urlMethod.containsKey(valeur)) {
                                    throw new Exception("double url" + valeur);
                                } else {
                                    urlMethod.put(valeur, map);
                                }
                            }
                        }
                        System.out.println("Added controller: " + clazz.getName());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Object convertParameter(String value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return null;
    }

    private Object[] getMethodParameters(Method method, HttpServletRequest request) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(Param.class)) {
                Param param = parameters[i].getAnnotation(Param.class);
                String paramValue = request.getParameter(param.value());
                parameterValues[i] = convertParameter(paramValue, parameters[i].getType());
            } else if (parameters[i].isAnnotationPresent(Objet.class)) {
                Class<?> parameterType = parameters[i].getType();
                Object parameterObject = parameterType.getDeclaredConstructor().newInstance();

                for (Field field : parameterType.getDeclaredFields()) {
                    String fieldName = field.getName();
                    String paramName = parameterType.getSimpleName().toLowerCase() + "." + fieldName;
                    String paramValue = request.getParameter(paramName);

                    if (paramValue != null) {
                        Object convertedValue = convertParameter(paramValue, field.getType());

                        String setterName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                        Method setter = parameterType.getMethod(setterName, field.getType());
                        setter.invoke(parameterObject, convertedValue);
                    }
                }
                parameterValues[i] = parameterObject;
            } else {
                parameterValues[i] = null; // or handle other cases as needed
            }
        }

        return parameterValues;
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
