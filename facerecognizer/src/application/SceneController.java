package application;



import java.awt.Font;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.bytedeco.javacv.FrameGrabber.Exception;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;



public class SceneController implements Initializable {

	@FXML
	StackPane baseViewSp;
	@FXML
	StackPane rootSp;
    @FXML
    BorderPane rootBorderPane;
    @FXML
    VBox settingsVBox;
    @FXML
    Pane settingPane;
    @FXML
    StackPane settingStackPane;
    @FXML
    Circle statusCircle;
    @FXML
    StackPane circlesp;
    @FXML
    StackPane userDatasp;
    @FXML
    VBox midvb;
    @FXML
    Label lblStatusText;
    @FXML
    private ImageView profilepic;
    @FXML
    ImageView frame;
    @FXML
    TextField txtEmployeeID;
    @FXML
    TextField txtEmployeeFullName;
    @FXML
    TextField txtEmployeeDepartment;
    @FXML
    TextField txtSourceDir;
    @FXML
    TextField txtApiEndpoint;
    @FXML
    Button btnToggleSettingsMenu;
    @FXML
    Button btnsaveSettings;
    @FXML
    Button btncancelSettings;
    @FXML
    Button btnimportFaces;
    @FXML
    ComboBox<String> CmbCamList;
    @FXML
    VBox PersonVb;
    @FXML
    Label timeLabel;
 
    Label onlineDataFetchStatusLabel = new Label();

    private Image idlefacestatic;
    private Image idlefaceanimated;
    public Animation idleStateTransition;
    public Animation faceDetectStateTransition;
    private int retryCount;
    Timeline labelSwapTimeline;

    private Timeline countdonwTimeline = new Timeline();
    private int countdownDuration = 10;
    private int currentCountdownValue;

    final double initialRadius = 180.0;
  
    public boolean isDBready = false;

	private List<String> event = new CopyOnWriteArrayList<>();


	static FaceDetector faceDetect ;;

	private SimpleStringProperty onlineFetchStatus = new SimpleStringProperty("");

	CompletableFuture<Void> fetchEmployeeFuture;

	Person currentPerson;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	this.onInitialize();
    	this.initializeTranistions();
    	this.initializeUiComponents();
    	this.initializeAppImmages();
    	this.inistializeMidvbChildrens(userDatasp);
    	this.initializeStatusTextLabel();
    	this.reinistialize();
    	
    }


	public void reinistialize()
    {
		 ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		 CompletableFuture<Void> allTasks = CompletableFuture
	                .runAsync(this::initializeBackgroundCamFrame,executorService)
	                .thenRun(this::playCurrentSystemTime)
	                .thenRun(this::initializeFullScreenAbility);
		    	allTasks.thenRun(() -> {
		    		Platform.runLater(() -> {
//		    		Create faces Folder before initializing faceDetect 	
		    		Uitility.checkFacerecognizerImagesFolder();
		    		faceDetect = new FaceDetector();
		    		initializeCam();
		    		});
		    });
    }
	
	public void initializeAppImmages() {
	      this.idlefacestatic = new Image(getClass().getResourceAsStream("./appimages/idlefacestatic.png"));
	      this.idlefaceanimated = new Image(getClass().getResourceAsStream("./appimages/onrecfaceanimated.gif"));
	}
	
	public void initializeTranistions() {
		 this.idleStateTransition = this.getIdleTransitionState();
	      this.faceDetectStateTransition = this.getFaceDetectedTransitionState();
	}


    public void initializeUiComponents() {
    	settingStackPane.getChildren().remove(this.settingPane);
    	PersonVb.setMinWidth(400);
    	settingPane.setMinWidth(430);
    	settingPane.setPadding(new Insets(20, 40, 20, 40));
    	settingsVBox.setMinWidth(400);
    	CmbCamList.setMinWidth(400);
    	btnsaveSettings.setMinWidth(400);
    	btncancelSettings.setMinWidth(400);
    	btnimportFaces.setMinWidth(400);
    	
    	SVGPath syncSvg = new SVGPath();
    	syncSvg.setContent("m11 9l2-.5l.5 2 M13 8.5A6.76 6.76 0 0 1 7 13h0a6 6 0 0 1-5.64-3.95M3 5l-2 .5l-.5-2 M1 5.5C1.84 3.2 4.42 1 7 1h0a6 6 0 0 1 5.64 4");
    	syncSvg.setFill(Color.WHITE); 
    	RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), syncSvg);
        rotateTransition.setByAngle(360); 
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE); 
        rotateTransition.play();
    	
    	onlineDataFetchStatusLabel.textProperty().bind(onlineFetchStatus);
    	onlineDataFetchStatusLabel.setMaxWidth(Double.MAX_VALUE);
    	onlineDataFetchStatusLabel.setStyle("-fx-font-size: 30px;"
    			+ "-fx-padding:5 10 5 10;");
    	onlineDataFetchStatusLabel.setGraphic(syncSvg);
    	onlineDataFetchStatusLabel.setTextAlignment(TextAlignment.JUSTIFY);
    	onlineDataFetchStatusLabel.setAlignment(Pos.CENTER);
    	onlineDataFetchStatusLabel.setGraphicTextGap(5.00);
    	this.PersonVb.getChildren().add(0, onlineDataFetchStatusLabel);
    }


    public void initializeCam(){
    	startCamera();
    }
    
    public void initializeBackgroundCamFrame() {
    	this.frame.fitWidthProperty().bind(this.baseViewSp.widthProperty());
    	this.frame.fitHeightProperty().bind(this.baseViewSp.heightProperty());
    	this.settingPane.setMinHeight(this.baseViewSp.getHeight());
    	 BoxBlur blur = new BoxBlur(10, 10, 3);
    	 this.frame.setEffect(blur);
    }
    
    
    public void initializeStatusTextLabel() {
     	this.lblStatusText.setMaxSize(initialRadius * 1.4, initialRadius * 1.4);
    	this.lblStatusText.setTextAlignment(TextAlignment.CENTER);
    }

    public void inistializeMidvbChildrens(StackPane... controls) {
        for (StackPane control : controls) {
            if(control != null) {
            	   VBox.setVgrow(control, Priority.ALWAYS);
            	   control.setMinHeight(Region.USE_PREF_SIZE);
            	   control.prefHeightProperty().bind(midvb.heightProperty());
            }
        }

    }

    public static void handleWindowClose() {
    	 faceDetect.stop();
    	 Platform.exit();
    	 System.exit(0);

    }

    public void onDataFetchfromApiState() {
    	new Thread(()->{
    		Platform.runLater(()->{
    			this.setStatusText(Textinformation.ONSUBMIT);
    		});
    	}).start();
    }

    public void playOnFaceDetectedAnimationOnly() {
    	if(this.idleStateTransition.getStatus() == Animation.Status.RUNNING) {
			this.idleStateTransition.stop();
		}
		this.faceDetectStateTransition.play();
    }

    public void onFaceDetected() {
     	new Thread(()->{
    		Platform.runLater(()->{
    			this.setStatusText(Textinformation.ONFACEDETECTED);
    			lblStatusText.setGraphic(this.getIdleAnimatedImage());
    			lblStatusText.setContentDisplay(ContentDisplay.TOP);
    			midvb.getChildren().remove(userDatasp);
    	    	if(!midvb.getChildren().contains(circlesp)) {
    	    		midvb.getChildren().add(circlesp);
    	    	}

    		 	this.playOnFaceDetectedAnimationOnly();
    			statusCircle.setRadius(initialRadius);
    		});
    	}).start();
    }

    public void PlayIdleStateAnimationOnly() {
    	if(this.faceDetectStateTransition.getStatus() == Animation.Status.RUNNING) {
			this.faceDetectStateTransition.stop();
		}
		if(this.idleStateTransition.getStatus() == Animation.Status.RUNNING) {

		}else {
			this.idleStateTransition.play();
		}

    }


    public  Label  getfaceLabel() {
    	Label flable = new Label();
        ImageView fim = new ImageView();
   	 	Image gifImage = new Image(getClass().getResourceAsStream("./appimages/idlefaceanimated.gif"));
   	 	fim.setImage(gifImage);
   	 	fim.setPreserveRatio(true);
   	 	fim.setFitHeight(initialRadius);
   	 	fim.setFitWidth(initialRadius);
        flable.setGraphic(fim);
        flable.setText(Textinformation.ONFACEDETECTED);
        flable.setStyle("-fx-text-fill: #083344;"
        		+ "-fx-font-weight: 600;"
        		+ "-fx-graphic-text-gap:20;"
        		+ "-fx-font-size: 20;");
        flable.setContentDisplay(ContentDisplay.TOP);
    	return flable;
    }

    public ImageView getIdleStaticImage() {
    	ImageView iv = new ImageView();
    	iv.setImage(idlefacestatic);
    	iv.setFitHeight(initialRadius);
    	iv.setFitWidth(initialRadius);
    	return iv;
    }

    public ImageView getIdleAnimatedImage() {
    	ImageView iv = new ImageView();
    	iv.setImage(idlefaceanimated);
    	iv.setFitHeight(initialRadius);
    	iv.setFitWidth(initialRadius);
    	return iv;
    }

    
    
    
    public void onInitialize() {
		Platform.runLater(()->{
		btnToggleSettingsMenu.setVisible(false);
   		 this.setStatusText(Textinformation.ONINITIALIZING);
   			statusCircle.setRadius(initialRadius);
   			lblStatusText.setGraphic(this.getIdleStaticImage());
   			lblStatusText.setContentDisplay(ContentDisplay.TOP);
   			midvb.getChildren().remove(userDatasp);
   	    	if(!midvb.getChildren().contains(circlesp)) {
   	    		midvb.getChildren().add(circlesp);
   	    	}
   		});
    }

    

    public void setIDleState() {
    	new Thread(()->{
    		Platform.runLater(()->{
    		 this.setStatusText(Textinformation.ONACTIVE);
    		 	this.PlayIdleStateAnimationOnly();
    			statusCircle.setRadius(initialRadius);
    			lblStatusText.setGraphic(this.getIdleStaticImage());
    			lblStatusText.setContentDisplay(ContentDisplay.TOP);
    			midvb.getChildren().remove(userDatasp);
    	    	if(!midvb.getChildren().contains(circlesp)) {
    	    		midvb.getChildren().add(circlesp);
    	    	}
    		});
    	}).start();
    }



    public void notify(String data,boolean error) {
	    Instant now = Instant.now();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(now, java.time.ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = localDateTime.format(formatter);
        String logs = formattedDateTime + ":\n" + data;
	    event.add(logs);
	    new Thread(() -> {
	        for (String message : event) {
	            try {
	                Thread.sleep(1000);
	            } catch (InterruptedException e) {
	            	e.printStackTrace();
	            }
	        }
	        event.clear();
	    }).start();
	}


	protected void startCamera() {
		String trainingDir = Configuration.getPreference(ConfigKeys.facesDirectory, null);
		File root = new File(trainingDir);
		FilenameFilter imgFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
			}
		};
		File[] imageFiles = root.listFiles(imgFilter);
		if(imageFiles.length == 0 ) {
			this.setStatusText(Textinformation.ONEMPTYTRAINEDFACES);
			btnToggleSettingsMenu.setVisible(true);
		}else {
			new Thread(()->{
			try {
				faceDetect.init(frame,this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			faceDetect.start();
			this.setIDleState();
			btnToggleSettingsMenu.setVisible(true);
			}).start();
		}
		
	}



	protected void restartCam() {
		startCamera();
	}


    public void onPersonFound(Person person) {
    	currentPerson = person;
    	new Thread(()->{
    		Platform.runLater(()->{
    			setCaptureuserData(person);
    		});
    	}).start();


    }
    
    
    public void setOnlineInfoStatus(String Status,boolean isError) {
    	  if(isError) {
    		  Platform.runLater(()->{
    			  onlineDataFetchStatusLabel.setStyle("-fx-text-fill: #fef2f2;");
    			  onlineDataFetchStatusLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
    		  });
    	  }else {
    		 Platform.runLater(()->{
    			 onlineDataFetchStatusLabel.setStyle("-fx-text-fill: #ffffff;");
    		 }); 
    	  }
    		 Platform.runLater(()->{
    			 onlineFetchStatus.set(Status);
    		 });	 
    	  
    }
    
    
    public void getOnlineRecord() {
    	if(currentPerson !=null && !currentPerson.getID().isEmpty()) {
    		setOnlineInfoStatus("Getting recent record...", false);
    		onlineDataFetchStatusLabel.setContentDisplay(ContentDisplay.LEFT);
            AtomicReference<Person> currentEmployee = new AtomicReference<>();
            fetchEmployeeFuture = CompletableFuture.runAsync(() -> {
                  
    				try {
    					try {Person employee = Apiclient.fetchEmployee(currentPerson.getID());
    					currentEmployee.set(employee);}
    					catch(java.lang.Exception e) {
    						setOnlineInfoStatus(e.getMessage(), true);
    					}
    				} catch (java.lang.Exception e) {
    					 setOnlineInfoStatus(e.getMessage(), true);
    				}
               
            });

            fetchEmployeeFuture.thenRun(() -> {
            	if(currentEmployee.get() !=null) {
            	 	 Person employee = currentEmployee.get();
                     Platform.runLater(() -> {
                     		Platform.runLater(()->{
                     			this.setCaptureuserData(employee);
                     		});
                      	setOnlineInfoStatus("Done", false);
                      	onlineDataFetchStatusLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
                     });
            	}else{
            		Platform.runLater(()->{
            			setOnlineInfoStatus("Failed to fetch some items..!", true);
                      	onlineDataFetchStatusLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
            		});
            	}
           
            });
    	}
    
    }
    
    
    
    

    public void setCaptureuserData(Person person) {
    	Image image;
        if(person!=null && person.getImage() != null) {
        	image= SwingFXUtils.toFXImage(person.getImage(), null);
       
        }
        else {
        	 InputStream inputStream = getClass().getResourceAsStream("appimages/imageavatar.png");
        	 image = new Image(inputStream);
        }
        profilepic.setImage(image);
        txtEmployeeID.setText(person.getEmployeeID());
        txtEmployeeFullName.setText(person.getFullName());
        ToggleinPersonPanel();
        startCountdown();
    }

    public void setStatusText(String text) {
    	new Thread(()->{
    		Platform.runLater(()->{
    			lblStatusText.setText(text);
    		});
    	}).start();
    }




    public void generateOuterCircle(double centerX,double centerY,double radius) {
    		Circle outCircle = new Circle();
    	    outCircle.setCenterX(centerX);
    	    outCircle.setCenterY(centerY);
    	    outCircle.setRadius(radius);
    	    outCircle.setStyle("	-fx-fill: transparent;"
    	    		+ "	-fx-stroke-width: 4;"
    	    		+ "	-fx-stroke:  #e0e7ff;"
    	    		+ "-fx-effect: dropshadow(gaussian, #e0e7ff, 1, 1, 0, 0);"
    	    		);

    	    circlesp.getChildren().add(outCircle);
    	    zoomOutWithOpacity(outCircle);
    }






    public Animation getFaceDetectedTransitionState() {
        ScaleTransition zoomOutAnimation = new ScaleTransition(Duration.seconds(0.7), statusCircle);
        zoomOutAnimation.setFromX(0.9);
        zoomOutAnimation.setFromY(0.9);
        zoomOutAnimation.setToX(1);
        zoomOutAnimation.setToY(1);
        zoomOutAnimation.setInterpolator(Interpolator.EASE_OUT);

        zoomOutAnimation.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == Animation.Status.STOPPED) {
                if (statusCircle.getScaleX() >= 1.0) {
                    generateOuterCircle(statusCircle.getCenterX(), statusCircle.getCenterY(), statusCircle.getRadius());
                }
            }
        });

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.7), statusCircle);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0.5);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(zoomOutAnimation, fadeTransition);

        parallelTransition.setCycleCount(Animation.INDEFINITE);
        parallelTransition.setAutoReverse(true);

        return parallelTransition;
    }





    public Animation getIdleTransitionState() {
        ScaleTransition zoomOutAnimation = new ScaleTransition(Duration.seconds(2), statusCircle);
        zoomOutAnimation.setFromX(0.9);
        zoomOutAnimation.setFromY(0.9);
        zoomOutAnimation.setToX(1);
        zoomOutAnimation.setToY(1);
        zoomOutAnimation.setInterpolator(Interpolator.EASE_OUT);

        zoomOutAnimation.statusProperty().addListener((obs, oldStatus, newStatus) -> {
            if (newStatus == Animation.Status.STOPPED) {
                if (statusCircle.getScaleX() >= 1.0) {
                    generateOuterCircle(statusCircle.getCenterX(), statusCircle.getCenterY(), statusCircle.getRadius());
                }
            }
        });

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), statusCircle);
        fadeTransition.setFromValue(1);
        fadeTransition.setToValue(0.5);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(zoomOutAnimation, fadeTransition);

        parallelTransition.setCycleCount(Animation.INDEFINITE);
        parallelTransition.setAutoReverse(true);

        return parallelTransition;
    }


    public void zoomOutWithOpacity(Node node) {
    	
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(2), node);
        scaleTransition.setToX(1.2);
        scaleTransition.setToY(1.2);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(2), node);
        fadeTransition.setFromValue(0.5);
        fadeTransition.setToValue(0);

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);

        parallelTransition.play();
        parallelTransition.setOnFinished((event)->{
        circlesp.getChildren().removeAll(node);
        });
    }



    @FXML
    public void handleOnClockIn() {
//    	make api calls here first
    	this.stopCountdown();
    	this.setIDleState();
    	this.ToggleoutPersonPanel();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    	new Thread(()->{
    		 	scheduler.schedule(()->{
     			this.retryCount = 0;
     			faceDetect.setIsRecFace(true);
// 				faceDetect.setThreadStop(false);
// 				faceDetect.start();
     	 }, 2, TimeUnit.SECONDS);
    		 scheduler.shutdown();
    	}).start();

    }

    @FXML
    public void handleOnMismatch() {
    	this.stopCountdown();
    	this.setIDleState();
    	this.ToggleoutPersonPanel();
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    	new Thread(()->{
    		 	scheduler.schedule(()->{
    		 	this.retryCount++;
    		 	faceDetect.setIsRecFace(true);
// 				faceDetect.setThreadStop(false);
// 				faceDetect.start();
     	 }, 2, TimeUnit.SECONDS);
    		 scheduler.shutdown();
    	}).start();
    }



  /*
   * settings section
   */

    
    @SuppressWarnings("unchecked")
	public void setSettingsData() {
    	new Thread(()->{
    		Platform.runLater(()->{
    			this.txtSourceDir.setText(Configuration.getPreference(ConfigKeys.sourceDirectory, null));
    	    	this.txtApiEndpoint.setText(Configuration.getPreference(ConfigKeys.apiEndPoint, null));
    	    	CmbCamList.itemsProperty().bind(Bindings.createObjectBinding(() -> CameraManager.AvailableVideoCamsList));
    	    	CmbCamList.setValue(Configuration.getPreference(ConfigKeys.cameraIndex, CameraManager.AvailableVideoCamsList.get(0)));
    		});
    	}).start();
    }

    @FXML
    public void saveSettings() {
    	new Thread(()->{
    		Platform.runLater(()->{
    			boolean restartCam = false;
    	    	this.ToggleSettingMenu();
    	    	if(Configuration.getPreference(ConfigKeys.cameraIndex, null) != this.CmbCamList.getValue()) {
    	    		restartCam = true;
    	    	}
    	    	if(this.txtSourceDir.getText() !=null) {
    	    		Configuration.setPreference(ConfigKeys.sourceDirectory, this.txtSourceDir.getText());
    	    	}
    	    	if(this.txtApiEndpoint.getText() != null) {
    	    		Configuration.setPreference(ConfigKeys.apiEndPoint, this.txtApiEndpoint.getText());
    	    	}
    	    	if(this.CmbCamList.getValue().toString() != null) {
    	    		Configuration.setPreference(ConfigKeys.cameraIndex,this.CmbCamList.getValue().toString());
    	    	}
    	    	if(restartCam) {
    	    		Platform.runLater(()->{
    	    			if(faceDetect.isRunning())
        	    		faceDetect.stop();
    	    			this.restartCam();
    	    		});
    	    	}
    		});

    	}).start();

    }

    @FXML
    public void ToggleSettingMenu() {
    	boolean userPane = settingStackPane.getChildren().contains(this.PersonVb);
    	
    	if(userPane) {
    		this.ToggleinPersonPanel();
    	}
    	boolean exist = settingStackPane.getChildren().contains(this.settingPane);
    	if(exist) {
    		
    		TranslateTransition transition1 = new TranslateTransition(Duration.seconds(0.3), settingPane);
    	    transition1.setInterpolator(Interpolator.EASE_OUT);
    	    transition1.setByX(500);
    	    FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(0.3), settingPane);
    	    fadeTransition1.setFromValue(1);
    	    fadeTransition1.setToValue(0);
    	    fadeTransition1.setInterpolator(Interpolator.EASE_OUT);
    	    ParallelTransition parallelTransition = new ParallelTransition();
    	    parallelTransition.getChildren().addAll(fadeTransition1,transition1);
    	    parallelTransition.play();
    		parallelTransition.setOnFinished(event->{
    		
    			settingStackPane.getChildren().remove(this.settingPane);
    		});
    		faceDetect.setThreadStop(false);
    		faceDetect.start();
    		faceDetect.setIsRecFace(true);

    	}else {
    		this.settingPane.setTranslateX(500);
    		settingStackPane.getChildren().add(this.settingPane);
    		faceDetect.setThreadStop(true);
    		TranslateTransition transition1 = new TranslateTransition(Duration.seconds(0.3), settingPane);
    	    transition1.setInterpolator(Interpolator.EASE_OUT);
    	    transition1.setByX(-500);
    	    FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(0.3), settingPane);
    	    fadeTransition1.setFromValue(0);
    	    fadeTransition1.setToValue(1);
    	    fadeTransition1.setInterpolator(Interpolator.EASE_OUT);
    	    ParallelTransition parallelTransition = new ParallelTransition();
    	    parallelTransition.getChildren().addAll(fadeTransition1,transition1);
    	    parallelTransition.play();
    		this.setSettingsData();
    	}
    }


    public void ToggleinPersonPanel() {
    	boolean exist = settingStackPane.getChildren().contains(this.PersonVb);
    	if(!exist) {
    		getOnlineRecord();
    		this.PersonVb.setTranslateX(500);
    		settingStackPane.getChildren().add(this.PersonVb);
    		TranslateTransition transition1 = new TranslateTransition(Duration.seconds(0.3), this.PersonVb);
    	    transition1.setInterpolator(Interpolator.EASE_OUT);
    	    transition1.setByX(-500);
    	    FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(0.3), this.PersonVb);
    	    fadeTransition1.setFromValue(0);
    	    fadeTransition1.setToValue(1);
    	    fadeTransition1.setInterpolator(Interpolator.EASE_OUT);
    	    ParallelTransition parallelTransition = new ParallelTransition();
    	    parallelTransition.getChildren().addAll(fadeTransition1,transition1);
    	    parallelTransition.play();
    	}
    }

    public void ToggleoutPersonPanel() {
    	boolean exist = settingStackPane.getChildren().contains(this.PersonVb);
    	if(exist) {
    		TranslateTransition transition1 = new TranslateTransition(Duration.seconds(0.3), this.PersonVb);
    	    transition1.setInterpolator(Interpolator.EASE_OUT);
    	    transition1.setByX(500);
    	    FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(0.3), this.PersonVb);
    	    fadeTransition1.setFromValue(1);
    	    fadeTransition1.setToValue(0);
    	    fadeTransition1.setInterpolator(Interpolator.EASE_OUT);
    	    ParallelTransition parallelTransition = new ParallelTransition();
    	    parallelTransition.getChildren().addAll(fadeTransition1,transition1);
    	    parallelTransition.play();
    		parallelTransition.setOnFinished(event->{
    			settingStackPane.getChildren().remove(this.PersonVb);
    		    if (fetchEmployeeFuture != null && !fetchEmployeeFuture.isDone()) {
    		        fetchEmployeeFuture.cancel(true);
    		    }
    		});
    	}
    }


    @FXML
    public void choseFaceSource() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Face Source Directory");

        String initialDirectoryPath = Configuration.getPreference("SourceDirectory", null);

        if (initialDirectoryPath != null) {
            File initialDirectory = new File(initialDirectoryPath);
            if (initialDirectory.isDirectory()) {
                directoryChooser.setInitialDirectory(initialDirectory);
            }
        }
        File selectedDirectory = directoryChooser.showDialog(new Stage());
        if (selectedDirectory != null) {
        	this.txtSourceDir.setText(selectedDirectory.getAbsolutePath().toString());
        }
    }


    @FXML
    public void showCopyingModal() {
    	if(Configuration.getPreference(ConfigKeys.sourceDirectory, null) != null) {
    	Stage modalStage = new Stage();
        modalStage.setHeight(200);
        modalStage.setWidth(200);
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.initStyle(StageStyle.UNDECORATED);
        modalStage.setTitle("Progress Dialog");
        Label label = new Label("Copy Face Dataset");
        Button cancelButton = new Button("Cancel");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setStyle("-fx-accent: blue; -fx-background-color: #083344;");

        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(label, progressBar, cancelButton);

        StackPane root = new StackPane(vbox);
        Scene scene = new Scene(root);
        modalStage.setScene(scene);


        Task<Void> copyTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                File sourceDir = new File(Configuration.getPreference(ConfigKeys.sourceDirectory, null));
                File destinationDir = new File(Configuration.getPreference(ConfigKeys.facesDirectory, null));

                File[] sourceFiles = sourceDir.listFiles();

                if (sourceFiles == null) {
                    return null;
                }

                int totalFiles = sourceFiles.length;
                int copiedFiles = 0;

                for (File sourceFile : sourceFiles) {
                    if (sourceFile.isFile() && sourceFile.getName().toLowerCase().endsWith(".jpg")) {
                        try {
                            Path destinationPath = destinationDir.toPath().resolve(sourceFile.getName());
                            Files.copy(sourceFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                            copiedFiles++;
                            double progress = (double) copiedFiles / totalFiles;
                            updateProgress(progress, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                updateProgress(1, 1);
                cancelButton.setText("Close");
//                cancelButton.setOnAction((e) -> {
//                    Platform.runLater(()->{
//                   
//                    	modalStage.close();
//                        ToggleSettingMenu();
////                        Todo restart Stage
//                     
//                        
//                    });
//                });
            }
        };
        cancelButton.setOnAction((e) -> {
            copyTask.cancel();
            Platform.runLater(()->{
            	modalStage.close();
                 ToggleSettingMenu();
//               Todo restart Stage
             
            });
        });

        progressBar.progressProperty().bind(copyTask.progressProperty());
        new Thread(copyTask).start();

        modalStage.showAndWait();
    	}
    }


    private void updateLabel() {
       new Thread(()->{
    	   Platform.runLater(()->{
    		   lblStatusText.setText("SESSION IN "+ currentCountdownValue + "S" );
    	   });
       }).start();
    }

    public void startCountdown() {
    	
    	if(countdonwTimeline.getStatus().equals(Animation.Status.STOPPED)) {
    	     currentCountdownValue = countdownDuration;
    	        updateLabel();
    	        countdonwTimeline = new Timeline(
    	            new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
    	                @Override
    	                public void handle(ActionEvent event) {
    	                     currentCountdownValue--;
    	                     updateLabel();
    	                    if (currentCountdownValue  == -1) {
    	                    	 stopCountdown();
    	                    	handleOnMismatch();
    	                    }

    	                }
    	            })
    	        );
    	        countdonwTimeline.setCycleCount(countdownDuration + 1);
    	        countdonwTimeline.play();
    	} 
	   
   }

    public void stopCountdown() {
        if (countdonwTimeline != null) {
        	countdonwTimeline.stop();
        }
    }
    

    public void playCurrentSystemTime() {
    	// Creating a background thread to update the time
    	Thread timeUpdateThread = new Thread(() -> {
    	Date currentDate = new Date();
    	SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd yyyy", Locale.ENGLISH);
    	String formattedDate = dateFormat.format(currentDate);
    	
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss a");
        while (true) {
            Date now = new Date();
            String formattedTime = sdf.format(now);
            // Update the UI on the  Application Thread
            Platform.runLater(() -> {
                timeLabel.setText( formattedDate + '\n' + formattedTime);
            });
            try {
                Thread.sleep(1000); // Update every second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    	});
       //	setting Daemon to true to terminate when app exits
    	timeUpdateThread.setDaemon(true);
    	timeUpdateThread.start();
   }
    
    
    
    private void initializeFullScreenAbility() {
    	  rootSp.setOnMouseClicked(event -> {
	            if (event.getClickCount() == 2) { 
	                toggleFullScreen(rootSp);
	            }
	        });
    }
    
    private void toggleFullScreen(StackPane root) {
        Stage stage = (Stage) root.getScene().getWindow();
        if (!stage.isFullScreen()) {
            // Enter full-screen mode
            stage.setFullScreen(true);
            stage.hide(); // Hide the window to remove title bar
            stage.show();
            btnToggleSettingsMenu.setVisible(false);
        } else {
            // Exit full-screen mode
            stage.setFullScreen(false);
            stage.show(); // Show the window with title bar
            btnToggleSettingsMenu.setVisible(true);
        }
       
    }
}

