package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import application.Person;

import application.Person;

public class Apiclient {
		
	 public static boolean isValidUrl(String url) {
	        // Regular expression pattern to match a URL
	        String regex = "^(https?|ftp)://[a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	        
	        // Create a Pattern object
	        Pattern pattern = Pattern.compile(regex);
	        
	        // Create a Matcher object
	        Matcher matcher = pattern.matcher(url);
	        
	        // Check if the string matches the pattern
	        return matcher.matches();
	    }
	
	  public static Person fetchEmployee(String empid) throws Exception {
//	            String endPoint = Configuration.getPreference(ConfigKeys.apiEndPoint, null);
		  		String employeeDataEndPoint = "http://4.168.227.16:61636/api/employee/bio/";
	            if(!isValidUrl(employeeDataEndPoint)) {
	            	throw new Exception("Invalid Api Resource URL");
	            }else {
	            	 URL url = new URL( employeeDataEndPoint + empid);
	 	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	 	            connection.setRequestMethod("GET");
	 	     
	 	            int responseCode = connection.getResponseCode();
	 	            
	 	            if (responseCode == HttpURLConnection.HTTP_OK) {     
	 	                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	 	                String line;
	 	                StringBuilder response = new StringBuilder();
	 	               
	 	                while ((line = reader.readLine()) != null) {
	 	                    response.append(line);
	 	                }
	 	                reader.close();
	 	                connection.disconnect();
	 	                ObjectMapper objectMapper = new ObjectMapper();
	 	                JsonNode jsonNode = objectMapper.readTree(response.toString());
	 	                
	 	                if ((jsonNode.isArray() && jsonNode.size() == 1 && jsonNode.get(0).isArray() && jsonNode.get(0).size() == 0) || !jsonNode.isArray()) {
	 	                    throw new Exception("Not found");
	 	                }else {
	 	                	   String employeeNumber = jsonNode.at("/0/0/employeeNumber").asText();
	 			                String fullName = jsonNode.at("/0/0/fullName").asText();
	 			                String gender = jsonNode.at("/0/0/sex").asText();
	 			                String ID = jsonNode.at("/0/0/id").asText();
	 			             
	 			                JsonNode empPicNode = jsonNode.at("/0/0/EmpPic");
	 			                
	 			                if (empPicNode != null && empPicNode.isObject()) {
	 			                   
	 			                    JsonNode dataNode = empPicNode.get("data");
	 			                   
	 			                    if (dataNode != null && dataNode.isArray()) {
	 			                        // Convert the byte array into a byte[]
	 			                        byte[] empPicBytes = new byte[dataNode.size()];
	 			                        for (int i = 0; i < dataNode.size(); i++) {
	 			                            empPicBytes[i] = (byte) dataNode.get(i).intValue();
	 			                        }
	 			                        
	 			                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(empPicBytes);
	 			                        BufferedImage empPicImage = ImageIO.read(byteArrayInputStream);
	 			                        
	 			                        
	 			                        return new Person(ID,employeeNumber,fullName,empPicImage,gender);
	 			                    }
	 			                }        
	 			               
	 	                }
	 	                
	 	            } else {
	 	                throw new Exception("HTTP Error:" + responseCode);
	 	            }
	 				return null;
	 	  
	            }
	           
	    }
}