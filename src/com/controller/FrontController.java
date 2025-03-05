package com.controller;

import com.google.gson.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
<<<<<<< Updated upstream
import jnd.annotation.Annotation;
import jnd.annotation.GET;
import jnd.annotation.POST;
import jnd.annotation.RequestBody;
import jnd.annotation.RequestParam;
import jnd.mapping.Mapping;
import jnd.mapping.ModelView;
import jnd.mapping.MySession;
=======
import com.annotation.Annotation;
import com.annotation.GET;
import com.annotation.POST;
import com.annotation.RequestBody;
import com.annotation.RequestParam;
import com.annotation.Role;
import com.annotation.ClassRole;
import com.annotation.FormUrl;
import com.mapping.Mapping;
import com.mapping.ModelView;
import com.mapping.MySession;
import com.validation.ValidationError;
import com.validation.ValidationException;
import com.auth.AuthManager;
import com.upload.UploadedFile;
>>>>>>> Stashed changes

import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
<<<<<<< Updated upstream
=======
import java.util.List;
import java.util.Enumeration;
import java.util.Collection;
>>>>>>> Stashed changes

public class FrontController extends HttpServlet {

    private Map<String, Mapping> urlMappings = new HashMap<>();
    private Gson gson = new Gson();  // Instance de Gson pour la sérialisation JSON

    @Override
    public void init() throws ServletException {
        try {
            findControllerClasses();
        } catch (Exception e) {
            logException(e);
            throw new ServletException(e);
        }
    }

    public void findControllerClasses() throws Exception {
        String controllerPackage = getServletConfig().getInitParameter("controller");
        if (controllerPackage == null || controllerPackage.isEmpty()) {
            throw new Exception("Controller package not specified");
        }

        String path = controllerPackage.replace('.', '/');
        File directory = new File(getServletContext().getRealPath("/WEB-INF/classes/" + path));

        if (!directory.exists() || !directory.isDirectory()) {
            throw new Exception("Package directory not found: " + directory.getAbsolutePath());
        }

        findClassesInDirectory(controllerPackage, directory);
    }

    private void findClassesInDirectory(String packageName, File directory) throws Exception {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                findClassesInDirectory(packageName + "." + file.getName(), file);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                addClassIfController(className);
            }
        }
    }

    private void addClassIfController(String className) throws Exception {
        try {
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

<<<<<<< Updated upstream
=======
                // Vérifier les rôles requis au niveau de la classe
                if (clazz.isAnnotationPresent(ClassRole.class)) {
                    ClassRole classRoleAnnotation = clazz.getAnnotation(ClassRole.class);
                    if (!AuthManager.isAuthenticated(req.getSession())) {
                        // Rediriger vers la page de connexion si l'utilisateur n'est pas authentifié
                        res.sendRedirect(req.getContextPath() + "/login");
                        return;
                    }
                    
                    // Vérifier les rôles et le niveau requis au niveau de la classe
                    if (!AuthManager.hasAnyRole(req.getSession(), classRoleAnnotation.value()) ||
                        !AuthManager.hasRequiredLevel(req.getSession(), classRoleAnnotation.level())) {
                        // Rediriger vers une page d'erreur d'autorisation
                        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès non autorisé");
                        return;
                    }
                }

                // Vérifier les rôles requis au niveau de la méthode (surcharge les rôles de la classe)
                if (targetMethod.isAnnotationPresent(Role.class)) {
                    Role roleAnnotation = targetMethod.getAnnotation(Role.class);
                    if (!AuthManager.isAuthenticated(req.getSession())) {
                        // Rediriger vers la page de connexion si l'utilisateur n'est pas authentifié
                        res.sendRedirect(req.getContextPath() + "/login");
                        return;
                    }
                    
                    // Vérifier les rôles et le niveau requis au niveau de la méthode
                    if (!AuthManager.hasAnyRole(req.getSession(), roleAnnotation.value()) ||
                        !AuthManager.hasRequiredLevel(req.getSession(), roleAnnotation.level())) {
                        // Rediriger vers une page d'erreur d'autorisation
                        res.sendError(HttpServletResponse.SC_FORBIDDEN, "Accès non autorisé");
                        return;
                    }
                }

>>>>>>> Stashed changes
                Object instance = clazz.getDeclaredConstructor().newInstance();
                Object[] methodArgs = getMethodArguments(targetMethod, req);

                Object result = targetMethod.invoke(instance, methodArgs);

                if (result instanceof ModelView) {
                    ModelView modelView = (ModelView) result;
                    String viewUrl = modelView.getUrl();
                    Map<String, Object> data = modelView.getData();

                    for (Map.Entry<String, Object> entry : data.entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }
<<<<<<< Updated upstream

                    RequestDispatcher dispatcher = req.getRequestDispatcher(viewUrl);
                    dispatcher.forward(req, res);
                } else {
                    String jsonResponse = gson.toJson(result);
                    out.print(jsonResponse);
                    out.flush();
=======
                } catch (ValidationException ve) {
                    req.setAttribute("validationErrors", ve.getValidationErrors());
                    
                    String formUrl = null;
                    if (targetMethod.isAnnotationPresent(FormUrl.class)) {
                        formUrl = targetMethod.getAnnotation(FormUrl.class).value();
                    }
                    
                    if (formUrl != null) {
                        req.getRequestDispatcher(formUrl).forward(req, res);
                    } else {
                        req.getRequestDispatcher("/WEB-INF/views/error.jsp").forward(req, res);
                    }
>>>>>>> Stashed changes
                }
            } else {
                throw new Exception("Aucune méthode associée à ce chemin");
            }
        } catch (Exception e) {
            logException(e);
            sendErrorPage(res, e.getMessage());
        }
    }

    private Object[] getMethodArguments(Method method, HttpServletRequest req) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        boolean isMultipart = req.getContentType() != null && req.getContentType().toLowerCase().startsWith("multipart/form-data");
        Map<String, String> paramMap = new HashMap<>();
        Map<String, UploadedFile> fileMap = new HashMap<>();

        if (isMultipart) {
            Collection<Part> parts = req.getParts();
            for (Part part : parts) {
                if (part.getSubmittedFileName() == null) {
                    // This is a form field
                    paramMap.put(part.getName(), req.getParameter(part.getName()));
                } else {
                    // This is a file
                    UploadedFile uploadedFile = new UploadedFile(
                        part.getSubmittedFileName(),
                        part.getContentType(),
                        part.getSize(),
                        part.getInputStream()
                    );
                    fileMap.put(part.getName(), uploadedFile);
                }
            }
        } else {
            // Handle regular form parameters
            Enumeration<String> parameterNames = req.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String paramName = parameterNames.nextElement();
                paramMap.put(paramName, req.getParameter(paramName));
            }
        }

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            
            if (parameter.isAnnotationPresent(com.annotation.FileUpload.class)) {
                com.annotation.FileUpload uploadAnnotation = parameter.getAnnotation(com.annotation.FileUpload.class);
                String paramName = uploadAnnotation.value().isEmpty() ? parameter.getName() : uploadAnnotation.value();
                
                if (fileMap.containsKey(paramName)) {
                    UploadedFile file = fileMap.get(paramName);
                    
                    // Validate file type if specified
                    if (uploadAnnotation.allowedTypes().length > 0) {
                        boolean validType = false;
                        String extension = file.getExtension().toLowerCase();
                        for (String type : uploadAnnotation.allowedTypes()) {
                            if (type.toLowerCase().equals(extension)) {
                                validType = true;
                                break;
                            }
                        }
                        if (!validType) {
                            throw new Exception("Invalid file type for " + paramName);
                        }
                    }
                    
                    // Validate file size if specified
                    if (uploadAnnotation.maxSize() > 0 && file.getSize() > uploadAnnotation.maxSize()) {
                        throw new Exception("File size exceeds maximum allowed size for " + paramName);
                    }
                    
                    // Save file if directory is specified
                    if (!uploadAnnotation.directory().isEmpty()) {
                        String uploadDir = getServletContext().getRealPath("/" + uploadAnnotation.directory());
                        file.save(uploadDir);
                    }
                    
                    args[i] = file;
                }
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                Class<?> parameterType = parameter.getType();
                Object parameterObject = parameterType.getDeclaredConstructor().newInstance();
                
                for (Field field : parameterType.getDeclaredFields()) {
                    String paramName = field.isAnnotationPresent(RequestParam.class)
                            ? field.getAnnotation(RequestParam.class).value()
                            : field.getName();
                            
                    String paramValue = isMultipart ? paramMap.get(paramName) : req.getParameter(paramName);
                    
                    if (paramValue != null && !paramValue.isEmpty()) {
                        field.setAccessible(true);
                        field.set(parameterObject, convertParameter(paramValue, field.getType()));
                    } else {
                        throw new Exception("ETU002457//Missing required parameter: " + paramName);
                    }
                }
                args[i] = parameterObject;
            } else if (parameter.getType().equals(MySession.class)) {
                args[i] = new MySession(req.getSession());
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam paramAnnotation = parameter.getAnnotation(RequestParam.class);
                String paramName = paramAnnotation.value().isEmpty() ? parameter.getName() : paramAnnotation.value();
                String paramValue = isMultipart ? paramMap.get(paramName) : req.getParameter(paramName);
                
                if (paramValue == null) {
                    throw new Exception("ETU002457//Missing required parameter: " + paramName);
                }
                args[i] = convertParameter(paramValue, parameter.getType());
            } else {
                throw new Exception("ETU002457//Missing parameter annotation");
            }
        }
        return args;
    }

    private Object convertParameter(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private void logException(Exception e) {
        e.printStackTrace(System.err);
    }

    private void sendErrorPage(HttpServletResponse res, String errorMessage) throws IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("<html><body>");
        out.println("<h1>Error</h1>");
        out.println("<p>" + errorMessage + "</p>");
        out.println("</body></html>");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }
}