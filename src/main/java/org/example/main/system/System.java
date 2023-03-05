package org.example.main.system;

import org.example.main.api.Api;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.JarFile;

public class System {
    public static void main(String[] args) {
        var plugins = new ArrayList<Api>();

        var jars = new ArrayList<URI>();

        Arrays.stream(Objects.requireNonNull(new File("plugins").listFiles((dir, name) -> name.endsWith(".jar")))).toList().forEach(file -> {
            try {
                jars.add(file.toURI());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //iterate through all jars
        for (var jar : jars) {
            try {
                //create a new class loader for each jar
                var classLoader = new URLClassLoader(new URL[]{jar.toURL()}, System.class.getClassLoader());

                //get all classes in the jar
                var classes = new JarFile(new File(jar)).stream()
                        .filter(entry -> entry.getName().endsWith(".class"))
                        .filter(entry -> !entry.getName().endsWith("module-info.class"))
                        .toList();

                //iterate through all classes
                for (var clazz : classes) {
                    //get the class name
                    var className = clazz.getName().replace(".class", "").replace("/", ".");

                    //load the class
                    var loadedClass = classLoader.loadClass(className);

                    //check if the class implements the Api interface
                    if (loadedClass.getInterfaces().length > 0 && loadedClass.getInterfaces()[0].equals(Api.class)) {
                        //cast the class to the Api interface
                        var api = (Api) loadedClass.getDeclaredConstructor().newInstance();

                        //add the class to the list of plugins
                        plugins.add(api);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //iterate through all plugins
        for (var plugin : plugins) {
            //enable the plugin
            plugin.onEnable();
        }

        //iterate through all plugins
        for (var plugin : plugins) {
            //disable the plugin
            plugin.onDisable();
        }
    }
}