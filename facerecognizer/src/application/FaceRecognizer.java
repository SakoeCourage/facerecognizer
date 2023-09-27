package application;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.cvarrToMat;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.LBPHFaceRecognizer;

public class FaceRecognizer {

	LBPHFaceRecognizer faceRecognizer;

	public File root;
	MatVector images;
	Mat labels;
	SceneController mainScene;

	public void init(SceneController s) {
		this.mainScene = s;
		String trainingDir = Configuration.getPreference(ConfigKeys.facesDirectory, null);

		root = new File(trainingDir);

		FilenameFilter imgFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
			}
		};

		File[] imageFiles = root.listFiles(imgFilter);

		if(imageFiles.length > 0) {

			this.images = new MatVector(imageFiles.length);

			this.labels = new Mat(imageFiles.length, 1, CV_32SC1);
			IntBuffer labelsBuf = labels.createBuffer();

			int counter = 0;
			// reading face images from the folder
			for (File image : imageFiles) {
				Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

				int label = Integer.parseInt(image.getName().split("\\-")[0]);

				images.put(counter, img);

				labelsBuf.put(counter, label);

				counter++;
			}


			this.faceRecognizer = createLBPHFaceRecognizer();

			if(images != null) {
				this.faceRecognizer.train(images, labels);
			}

		}


	}

	public String recognize(IplImage faceData) {

		if(faceData != null) {
			Mat faces = cvarrToMat(faceData);
			cvtColor(faces, faces, CV_BGR2GRAY);
			IntPointer label = new IntPointer(1);
			DoublePointer confidence = new DoublePointer(0);
			this.faceRecognizer.predict(faces, label, confidence);

			String predictedLabel = String.valueOf(label.get(0));

			//Confidence value less than 60 means face is known
			//Confidence value greater than 60 means face is unknown
			 if(confidence.get(0) > 60)
			 {
				 return null;
			 }

			return predictedLabel;
		}else {
			return null;
		}


	}


}
