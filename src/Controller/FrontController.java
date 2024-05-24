package Controller;

import java.io.IOException;
import java.io.PrintWriter;
<<<<<<< Updated upstream
=======
import java.lang.reflect.Method;
>>>>>>> Stashed changes
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
<<<<<<< Updated upstream
import java.util.List;

import AnnotationController.AnnotationController;
=======
import java.util.HashMap;
import java.util.List;

import AnnotationController.AnnotationController;
import AnnotationController.AnnotationGet;
import classe.Mapping;
>>>>>>> Stashed changes
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontController extends HttpServlet {
<<<<<<< Updated upstream
    boolean controllerScanee = false;
    private String packageName; // Variable pour stocker le nom du package
    private static List<String> controllerNames = new ArrayList<>();

=======
    private String packageName; // Variable pour stocker le nom du package
    private static List<String> controllerNames = new ArrayList<>();
    HashMap <String,Mapping> urlMaping = new HashMap<>() ;
    
>>>>>>> Stashed changes
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        packageName = config.getInitParameter("packageControllerName"); // Récupération du nom du package
<<<<<<< Updated upstream
=======
        scanControllers(packageName);
>>>>>>> Stashed changes
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        StringBuffer requestURL = request.getRequestURL();
<<<<<<< Updated upstream
        PrintWriter out = response.getWriter();

        if (!controllerScanee) {
            scanControllers(packageName, out);
        }
        response.setContentType("text/html");
        out.println("<p>" + requestURL.toString() + "</p>");
        out.close();
    }

=======
        String[] requestUrlSplitted = requestURL.toString().split("/");
        String controllerSearched = requestUrlSplitted[requestUrlSplitted.length-1];
        
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");

        if (!urlMaping.containsKey(controllerSearched)) {
            out.println("<p>"+"Aucune methode associee a ce chemin."+"</p>");
        }
        else {
            Mapping mapping = urlMaping.get(controllerSearched);
            
            out.println("<p>" + requestURL.toString() + "</p>");
            out.println("<p>" + mapping.getClassName() + "</p>");
            out.println("<p>" + mapping.getMethodeName() + "</p>");

            out.close();
        }
    }   

>>>>>>> Stashed changes
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

<<<<<<< Updated upstream
    private void scanControllers(String packageName, PrintWriter out) {
        try {
=======
    private void scanControllers(String packageName) {
        try {

>>>>>>> Stashed changes
            // Charger le package et parcourir les classes
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);
            Path classPath = Paths.get(resource.toURI());
            Files.walk(classPath)
                    .filter(f -> f.toString().endsWith(".class"))
                    .forEach(f -> {
                        String className = packageName + "." + f.getFileName().toString().replace(".class", "");
                        try {
                            Class<?> clazz = Class.forName(className);
                            if (clazz.isAnnotationPresent(AnnotationController.class)
                                    && !Modifier.isAbstract(clazz.getModifiers())) {
                                controllerNames.add(clazz.getSimpleName());
<<<<<<< Updated upstream
=======
                                Method[] methods= clazz.getMethods();

                                for(Method m : methods){
                                    if(m.isAnnotationPresent(AnnotationGet.class)){
                                        Mapping mapping =new Mapping(className , m.getName());
                                        AnnotationGet AnnotationGet = m.getAnnotation(AnnotationGet.class);
                                        String annotationValue = AnnotationGet.value();
                                        urlMaping.put(annotationValue, mapping);
                                    }
                                }
>>>>>>> Stashed changes
                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
<<<<<<< Updated upstream
            for (String controller : controllerNames) {
                out.println(controller);
            }
            controllerScanee = false;
=======
>>>>>>> Stashed changes
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
