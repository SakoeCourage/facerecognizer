package application;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;



public class Configuration {
	   private static Properties properties;


	    static {
	        properties = new Properties();
	        loadPreferences();
	    }
	    public static String getPreference(String key, String defaultValue) {
	        return properties.getProperty(key, defaultValue);
	    }

	    public static void setPreference(String key, String value) {
	        properties.setProperty(key, value);
	        savePreferences();
	    }


	    public static String getDefaultConfigDir() {
	        String userHome = System.getProperty("user.home");
	        String documentsPath = userHome + File.separator + "Documents";
	        String configDirPath = documentsPath + File.separator + "facerecognizer";
	        String filePath = configDirPath + File.separator + "config.properties";
	        return filePath;
	    }

	    static void loadPreferences() {
	 	        try (FileInputStream input = new FileInputStream(getDefaultConfigDir())) {
		            properties.load(input);
		        } catch (IOException e) {
//		            e.printStackTrace();
		            // Create a new properties file with default values if it doesn't exist
		            try (FileOutputStream output = new FileOutputStream(getDefaultConfigDir())) {
		                properties.store(output, null);
		            } catch (IOException ioException) {
//		                ioException.printStackTrace();
		            }
		        }   
	        } 

	    private static void savePreferences() {
	        try (FileOutputStream output = new FileOutputStream(getDefaultConfigDir())) {
	            properties.store(output, null);
	        } catch (IOException e) {
	            // Handle the exception
//	            e.printStackTrace();
	        }
	    }





}
