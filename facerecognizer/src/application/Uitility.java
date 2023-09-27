package application;
import java.io.File;

import javafx.application.Platform;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;




public class Uitility {




    public static void setControlsDisabled(boolean disabled, Control... controls) {
        for (Control control : controls) {
            if(control != null) {
            	control.setDisable(disabled);
            }
        }
    }


    public static void setTextControlsToEmpty( TextField... txtfields) {
        for (TextField tf : txtfields) {
            if(tf != null) {
            	tf.setText("");
            }
        }
    }

    public static boolean areAnyFieldsEmpty(TextField... textFields) {
        for (TextField textField : textFields) {
            if (textField.getText().trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }


    public static void showMessages(Pane pane,String message,boolean error) {
    		pane.getChildren().clear();
    		HBox hb = new HBox();
    		Label lbl = new Label();
    		SVGPath svgPath = new SVGPath();
    		if(!error) {
    			Platform.runLater(()->{
    				svgPath.setContent("M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16zm.93-9.412-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 1 1 0-2 1 1 0 0 1 0 2z");
        			pane.setStyle("-fx-background-color:#0891b2;");
    			});

    		}else if(error) {
    			Platform.runLater(()->{
    				svgPath.setContent("M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16zm.93-9.412-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 1 1 0-2 1 1 0 0 1 0 2z");
        			pane.setStyle("-fx-background-color:#dc2626;");
    			});
    		}
    		svgPath.setStyle("-fx-fill: #ffffff;");
    		lbl.setText(message);
    		lbl.setStyle("-fx-text-fill: #ffffff;");
    		hb.setSpacing(5);
    		hb.getChildren().addAll(svgPath,lbl);
    		hb.setStyle("-fx-padding: 20;");
    		pane.getChildren().add(hb);
    }



    public static void checkFacerecognizerImagesFolder() {
        String userHome = System.getProperty("user.home");
        String documentsPath = userHome + File.separator + "Documents";
        String folderPath = documentsPath + File.separator + ConfigKeys.fileDefaultFolder + File.separator + "images";
        File folder = new File(folderPath);

        if (!folder.exists()) {
            boolean folderCreated = folder.mkdirs();
            if (folderCreated) {
                Configuration.setPreference(ConfigKeys.facesDirectory, folderPath);
            } else {
            }
        } else {
            Configuration.setPreference(ConfigKeys.facesDirectory, folderPath.toString());
        }
    }
}
