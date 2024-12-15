package com.dsoftn.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;

import com.dsoftn.OBJECTS;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class UFile {

    // SYSTEM

    public static boolean startFile(String filePath) {
        return startFile(filePath, null, null);
    }

    public static boolean startFile(String filePath, String param1) {
        return startFile(filePath, param1, null);
    }

    /**
     * Start file with parameters if file exists and is executable
     * If file is Python file (with .py or .pyw extension), it will be started with Python interpreter
     * @param filePath
     * @param param1
     * @param param2
     * @return true if file was started
     */
    public static boolean startFile(String filePath, String param1, String param2) {
        if (!isFile(filePath)) {
            return false;
        }

        String absPath = getAbsolutePath(filePath);
        ProcessBuilder processBuilder;

        if (filePath.endsWith(".py") || filePath.endsWith(".pyw")) {
            if (param1 != null && param2 != null) {
                processBuilder = new ProcessBuilder("python", absPath, param1, param2);
            }
            else if (param1 != null) {
                processBuilder = new ProcessBuilder("python", absPath, param1);
            }
            else {
                processBuilder = new ProcessBuilder("python", absPath);
            }
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            processBuilder.environment().put("LC_ALL", "en_US.UTF-8");
        }
        else {
            if (param1 != null && param2 != null) {
                processBuilder = new ProcessBuilder(absPath, param1, param2);
            }
            else if (param1 != null) {
                processBuilder = new ProcessBuilder(absPath, param1);
            }
            else {
                processBuilder = new ProcessBuilder(absPath);
            }
        }

        processBuilder.redirectErrorStream(true);

        try {
            processBuilder.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }



    // FILES

    /**
     * Concatenate working directory with file path
     * @param filePath
     * @return concatenated path
     */
    public static String concatWorkingDir(String filePath) {
        return System.getProperty("user.dir") + "\\" + filePath;
    }

    /**
     * Check if file exists
     */
    public static boolean isFile(String filePath) {
        try {
            return Files.exists(Path.of(filePath));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get absolute path of file
     * @param filePath
     * @return absolute path of file or null
     */
    public static String getAbsolutePath(String filePath) {
        try {
            return Path.of(filePath).toAbsolutePath().toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Open file dialog
     * @param title
     * @param lastDir
     * @param stage Owner stage or null
     * @return Absolute path of file or null
     */
    public static String getOpenFileDialog(String title, String lastDir, Stage stage) {
        return getOpenFileDialog(title, lastDir, stage, null);
    }

    /**
     * Open file dialog
     * @param title
     * @param lastDir
     * @param stage Owner stage or null
     * @param filters List of file filters stings in format "name|extension"
     * @return Absolute path of file or null
     */
    public static String getOpenFileDialog(String title, String lastDir, Stage stage, List<String> filters) {
        FileChooser fileChooser = new FileChooser();
        
        if (filters != null) {
            for (String filter : filters) {
                String name = filter.split("\\|")[0];
                String extension = filter.split("\\|")[1];
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(name, extension));
            }
        }

        fileChooser.setTitle(title);

        if (lastDir == null) {
            lastDir =   OBJECTS.SETTINGS.isUserSettingExists("lastOpenFileDirectory")
                        ? OBJECTS.SETTINGS.getvSTRING("lastOpenFileDirectory")
                        :  "";
        }

        if (!lastDir.isEmpty()) {
            fileChooser.setInitialDirectory(new File(lastDir));
        }
        
        File selectedFile;
        if (stage == null) {
            selectedFile = fileChooser.showOpenDialog(null);
        }
        else {
            selectedFile = fileChooser.showOpenDialog(stage);
        }
        
        if (selectedFile == null) {
            return null;
        }
        else {
            if (OBJECTS.SETTINGS.isUserSettingExists("lastOpenFileDirectory")) {
                OBJECTS.SETTINGS.setv("lastOpenFileDirectory", selectedFile.getParent());
            }
            return selectedFile.getAbsolutePath();
        }
    }

    /**
     * Save file dialog
     * @param title
     * @param lastDir
     * @param stage Owner stage or null
     * @param fileName Initial file name
     * @return Absolute path of file or null
     */
    public static String getFileSaveDialog(String title, String lastDir, Stage stage, String fileName) {
        return getFileSaveDialog(title, lastDir, stage, fileName, null);
    }

    /**
     * Save file dialog
     * @param title
     * @param lastDir
     * @param stage Owner stage or null
     * @param fileName Initial file name
     * @param filters List of file filters stings in format "name|extension"
     * @return Absolute path of file or null
     */
    public static String getFileSaveDialog(String title, String lastDir, Stage stage, String fileName, List<String> filters) {
        FileChooser fileChooser = new FileChooser();

        if (filters != null) {
            for (String filter : filters) {
                String name = filter.split("\\|")[0];
                String extension = filter.split("\\|")[1];
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(name, extension));
            }
        }

        fileChooser.setTitle(title);

        if (lastDir == null) {
            lastDir =   OBJECTS.SETTINGS.isUserSettingExists("lastSaveFileDirectory")
                        ? OBJECTS.SETTINGS.getvSTRING("lastSaveFileDirectory")
                        :  "";
        }

        if (!lastDir.isEmpty()) {
            fileChooser.setInitialDirectory(new File(lastDir));
        }

        if (fileName != null && !fileName.isEmpty()) {
            fileChooser.setInitialFileName(fileName);
        }

        File selectedFile;
        if (stage == null) {
            selectedFile = fileChooser.showSaveDialog(null);
        }
        else {
            selectedFile = fileChooser.showSaveDialog(stage);
        }

        if (selectedFile == null) {
            return null;
        }
        else {
            if (OBJECTS.SETTINGS.isUserSettingExists("lastSaveFileDirectory")) {
                OBJECTS.SETTINGS.setv("lastSaveFileDirectory", selectedFile.getParent());
            }
            return selectedFile.getAbsolutePath();
        }
    }

    public static boolean copyFile(String source, String destination) {
        source = getAbsolutePath(source);
        destination = getAbsolutePath(destination);

        if (source == null || destination == null) {
            return false;
        }
        
        try {
            Files.copy(Paths.get(source), Paths.get(destination));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteFile(String path) {
        path = getAbsolutePath(path);
        if (path == null) {
            return false;
        }
        
        try {
            Files.deleteIfExists(Paths.get(path));
            return true;
        } catch (IOException e) {
            return false;
        }
    }


}
