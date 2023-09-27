package application;



import application.FaceRecognizer;

import javafx.util.Duration;
import application.Person;
import java.awt.BasicStroke;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_objdetect;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvRect;
import org.bytedeco.javacpp.opencv_core.CvSeq;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.bytedeco.javacpp.helper.opencv_core.AbstractCvMemStorage;





public class FaceDetector implements Runnable {

	Person user = null;

	FaceRecognizer faceRecognizer = new FaceRecognizer();
	MotionDetector motionDetector = new MotionDetector();
	OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
	Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	Person output = null;

	@FXML
	public Label ll;
	private Exception exception = null;

	public String classiferName;
	public File classifierFile;

	private int saveImageCount = 1;
	private int maxImages = 3;


	public boolean saveFace = false;
	public boolean isRecFace = false;
	public boolean isOutput = false;
	public boolean isOcrMode = false;
	public boolean isMotion = false;
	public boolean isEyeDetection = false;
	public boolean isSmile = false;
	public boolean isUpperBody = false;
	public boolean isFullBody = false;
	private boolean stop = false;

	private CvHaarClassifierCascade classifier = null;
	private CvHaarClassifierCascade classifierEye = null;
	private CvHaarClassifierCascade classifierProfileFace = null;
	private CvHaarClassifierCascade classifierUpperBody = null;
	private CvHaarClassifierCascade classifierFullBody = null;
	private CvHaarClassifierCascade classifierSmile = null;
	private CvHaarClassifierCascade classifierEyeglass = null;


	public CvMemStorage storage = null;
	private FrameGrabber grabber = null;
	private IplImage grabbedImage = null, temp, temp2, grayImage = null, smallImage = null;
	public ImageView frames2;
	public ImageView frames;


	private CvSeq faces = null;
	private CvSeq eyes = null;
	private CvSeq smile = null;
	private CvSeq upperBody = null;
	private CvSeq sideface = null;
	private CvSeq fullBody = null;

	private Label currentPerson = null;


	public String employeeID;
	public int code;
	public int reg;
	public int age;
	public String fname; //first name
	public String Lname; //last name
	public String sec; //section
	public String name;

	private SceneController mainScene  = null;

	private ExecutorService executor;

	private ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> timeoutFuture;

	public void init(ImageView frames,SceneController s) throws org.bytedeco.javacv.FrameGrabber.Exception {
		this.frames = frames;
		this.mainScene = s;
		faceRecognizer.init(s);
		
		setClassifier("haar/haarcascade_frontalface_alt.xml");
		setClassifierEye("haar/haarcascade_eye.xml");
		setClassifierEyeGlass("haar/haarcascade_eye_tree_eyeglasses.xml");
		classifierProfileFace("haar/haarcascade_profileface.xml");
		setClassifierFullBody("haar/haarcascade_fullbody.xml");
		setClassifierUpperBody("haar/haarcascade_upperbody.xml");
		int deviceIndex= 0;
		try {
			var userCamOption = Configuration.getPreference(ConfigKeys.cameraIndex, CameraManager.AvailableVideoCamsList.get(0));
			if(userCamOption != null) {
				 deviceIndex = CameraManager.AvailableVideoCamsList.indexOf(userCamOption);
				grabber = OpenCVFrameGrabber.createDefault(deviceIndex);
			}else {
				grabber = OpenCVFrameGrabber.createDefault(0);
			}
			grabber.setImageWidth((int)frames.getFitWidth());
			grabber.setImageHeight((int)frames.getFitHeight());
			grabber.start();
			grabbedImage = grabberConverter.convert(grabber.grab());
			storage = AbstractCvMemStorage.create();
			this.setIsRecFace(true);
		} catch (Exception e) {
			System.out.println("null grabber exception");
			if (grabber != null)
			grabber.release();
			grabber = new OpenCVFrameGrabber(deviceIndex);
			grabber.setImageWidth((int)frames.getFitWidth());
			grabber.setImageHeight((int)frames.getFitHeight());
			grabber.start();
			grabbedImage = grabberConverter.convert(grabber.grab());
			this.setIsRecFace(true);

		}

	}

    public void start() {
        executor = Executors.newSingleThreadExecutor();
        executor.execute(this);
    }

    public void terminate() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }




	@Override
	public void run() {
		try {

			grayImage = cvCreateImage(cvGetSize(grabbedImage), 8, 1); //converting image to grayscale

			//reducing the size of the image to speed up the processing

			smallImage = cvCreateImage(cvSize(grabbedImage.width() / 4, grabbedImage.height() / 4), 8, 1);

			stop = false;

			while (!stop && (grabbedImage = grabberConverter.convert(grabber.grab())) != null) {

				Frame frame = grabberConverter.convert(grabbedImage);
				BufferedImage image = paintConverter.getBufferedImage(frame, 2.2 / grabber.getGamma());
				Graphics2D g2 = image.createGraphics();

				if (faces == null) {
					
					cvClearMemStorage(storage);
					//creating a temporary image
					temp = cvCreateImage(cvGetSize(grabbedImage), grabbedImage.depth(), grabbedImage.nChannels());

					cvCopy(grabbedImage, temp);

					cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
					cvResize(grayImage, smallImage, CV_INTER_AREA);
					cvEqualizeHist(grayImage, grayImage);
					//cvHaarDetectObjects(image, cascade, storage, scale_factor, min_neighbors, flags, min_size, max_size)
					faces = cvHaarDetectObjects(smallImage, classifier, storage, 1.1, 1, CV_HAAR_DO_CANNY_PRUNING);

					//face detection
					CvPoint org = null;
					if (grabbedImage != null) {

						if (isEyeDetection) { 		//eye detection logic
							eyes = cvHaarDetectObjects(smallImage, classifierEye, storage, 1.1, 1,
									CV_HAAR_DO_CANNY_PRUNING);

							if (eyes.total() == 0) {
								eyes = cvHaarDetectObjects(smallImage, classifierEyeglass, storage, 1.1, 1,
										CV_HAAR_DO_CANNY_PRUNING);

							}

							printResult(eyes, eyes.total(), g2);

						}
						if (faces.total() == 0) {
							faces = cvHaarDetectObjects(smallImage, classifierProfileFace, storage, 1.1, 1,
									CV_HAAR_DO_CANNY_PRUNING);

						}

				if (faces != null) {
							g2.setColor(Color.green);
							g2.setStroke(new BasicStroke(2));
							int total = faces.total();

							for (int i = 0; i < total; i++) {
								
								if(isRecFace) {
									  this.resetTimer();
									  timeoutFuture = timeoutExecutor.schedule(() -> {
								            // This code will run if no face is detected within the specified time (2 seconds in this case)
								            Platform.runLater(() -> {
								                mainScene.setIDleState();
								            });
								        }, 3, TimeUnit.SECONDS);

									  new Thread(() -> {
								            Platform.runLater(() -> {
								                mainScene.onFaceDetected();
								            });
								        }).start();
								}
								//printing rectange box where face detected frame by frame
								CvRect r = new CvRect(cvGetSeqElem(faces, i));
								g2.drawRect((r.x() * 4), (r.y() * 4), (r.width() * 4), (r.height() * 4));
								CvRect re = new CvRect((r.x() * 4), r.y() * 4, (r.width() * 4), r.height() * 4);

								cvSetImageROI(temp, re);
								org = new CvPoint(r.x(), r.y());

								if (isRecFace) {
									
									this.employeeID = faceRecognizer.recognize(temp);

									//getting recognised user from the database
									if(employeeID != null)
									{
										this.setIsRecFace(false);
									
										user = new Person(employeeID,this.findFullNameByEmployeeID(employeeID));
										this.output = user;
										this.terminate();
										new Thread(() -> {
										    Platform.runLater(() -> {
										    	mainScene.onPersonFound(user);
										    });
										}).start();
										 this.resetTimer();
									}else {

									}
								}

//								if (saveFace) {
//									cvSaveImage("tempface/tempimage"+ saveImageCount + ".jpg", temp);
//									saveImageCount ++;
//									if(saveImageCount > maxImages) {
//										resetImageCount();
//									}
//								}
							}
							this.saveFace = false;
							faces = null;
						}

						WritableImage showFrame = SwingFXUtils.toFXImage(image, null);

						javafx.application.Platform.runLater(new Runnable(){
							@Override
							 public void run() {
								frames.setImage(showFrame);
								frames.setPreserveRatio(false);
							 }
							 });

						if (isMotion) {
							new Thread(() -> {

								try {

									motionDetector.init(grabbedImage, g2);

								} catch (InterruptedException ex) {
								} catch (Exception e) {

									e.printStackTrace();
								}

							}).start();

						}
						isMotion = false;

					}
					cvReleaseImage(temp);
				}

			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}
	}

	File folder = new File(Configuration.getPreference(ConfigKeys.facesDirectory, null));
	public String findFullNameByEmployeeID(String employeeID) {
        // Check if the provided path is a directory
        if (!folder.isDirectory()) {
            System.err.println("Invalid folder path.");
            return null;
        }
        
        File[] files = folder.listFiles((dir, name) -> name.startsWith(employeeID + "-"));
        
        if (files == null || files.length == 0) {
            System.out.println("No matching files found for EmployeeID: " + employeeID);
            return null;
        }
        
        // Assuming there's only one file with the given EmployeeID
        String fullName = files[0].getName();
        
        // Extract the FullName from the file name
        fullName = fullName.substring(employeeID.length() + 1, fullName.lastIndexOf('.') - 1).replace('_', ' ');
        
        return fullName;
    }
	
	public void resetTimer() {
	    if (timeoutFuture != null) {
            timeoutFuture.cancel(true);
        }
	}

	public void stop() {
		stop = true;
	    try {
	        if (grabber != null) {
	            grabber.stop();
	            grabber.release();
	        }
	    } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
//	        e.printStackTrace();
	    }


	    if (this.frames != null) {
	        this.frames.setImage(null);
	    }
	   
	    resetTimer();
	    grabber = null;
	    grabbedImage = grayImage = smallImage = null;
	}



	public void setClassifier(String name) {
		try {
			setClassiferName(name);
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");
			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}



	public void setClassifierEye(String name) {

		try {

			classiferName = name;
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierEye = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}


	public void setClassifierSmile(String name) {

		try {

			setClassiferName(name);
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierSmile = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}

	public void printResult(CvSeq data, int total, Graphics2D g2) {
		for (int j = 0; j < total; j++) {
			CvRect eye = new CvRect(cvGetSeqElem(eyes, j));

			g2.drawOval((eye.x() * 4), (eye.y() * 4), (eye.width() * 4), (eye.height() * 4));

		}
	}

	public void classifierProfileFace(String name) {
		try {

			classiferName = name;
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierProfileFace = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}


	public void setClassifierFullBody(String name) {

		try {

			setClassiferName(name);
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierFullBody = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}

	public void setClassifierEyeGlass(String name) {

		try {

			setClassiferName(name);
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierEyeglass = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}

	public void setClassifierUpperBody(String name) {

		try {

			classiferName = name;
			classifierFile = Loader.extractResource(classiferName, null, "classifier", ".xml");

			if (classifierFile == null || classifierFile.length() <= 0) {
				throw new IOException("Could not extract \"" + classiferName + "\" from Java resources.");
			}

			// Preload the opencv_objdetect module to work around a known bug.
			Loader.load(opencv_objdetect.class);
			classifierUpperBody = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
			classifierFile.delete();
			if (classifier.isNull()) {
				throw new IOException("Could not load the classifier file.");
			}

		} catch (Exception e) {
			if (exception == null) {
				exception = e;

			}
		}

	}

	public String getClassiferName() {
		return classiferName;
	}

	public void setClassiferName(String classiferName) {
		this.classiferName = classiferName;
	}

	public void setFrames2(ImageView frames2) {
		this.frames2 = frames2;
	}

	public void setSmile(boolean isSmile) {
		this.isSmile = isSmile;
	}

	public void setUpperBody(boolean isUpperBody) {
		this.isUpperBody = isUpperBody;
	}

	public void setFullBody(boolean isFullBody) {
		this.isFullBody = isFullBody;
	}

	public boolean isEyeDetection() {

		return isEyeDetection;
	}

	public void setEyeDetection(boolean isEyeDetection) {
		this.isEyeDetection = isEyeDetection;
	}

	public boolean getOcrMode() {
		return isOcrMode;
	}

	public void setOcrMode(boolean isOcrMode) {
		this.isOcrMode = isOcrMode;
	}

	public void destroy() {
	}

	public boolean isMotion() {
		return isMotion;
	}

	public void setMotion(boolean isMotion) {
		this.isMotion = isMotion;
	}

	public Person getOutput() {
		return output;
	}

	public void clearOutput() {
		this.output = null;
	}



	public String getemployeeID() {
		return employeeID;
	}

	public void setemployeeID(String employeeID) {
		this.employeeID = employeeID;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	public String getLname() {
		return Lname;
	}

	public void setLname(String lname) {
		Lname = lname;
	}

	public int getReg() {
		return reg;
	}

	public void setReg(int reg) {
		this.reg = reg;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getSec() {
		return sec;
	}

	public void setSec(String sec) {
		this.sec = sec;
	}




	public void setSaveFace(Boolean f) {
		this.saveFace = f;
	}

	public Boolean getIsRecFace() {
		return isRecFace;
	}

	public void setIsRecFace(Boolean isRecFace) {
		this.isRecFace = isRecFace;
	}

	public void setLabelPerson(Label label) {
		this.currentPerson = label;
	}

	public void resetImageCount() {
		this.saveImageCount = 1;
	}

	public int getImageCount() {
		return saveImageCount;
	}

	public void setMaxImages(int mx) {
		 this.maxImages = mx;
	}
	public void setThreadStop(boolean status) {
		 this.stop = status;
	}

	public int getMaxImages() {
		return maxImages;
	}
	
	public boolean isRunning () {
		return this.stop;
	}

}
