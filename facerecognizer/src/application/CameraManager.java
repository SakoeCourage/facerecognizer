package application;

import org.bytedeco.javacv.FrameGrabber.Exception;
import org.bytedeco.javacv.VideoInputFrameGrabber;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CameraManager {
	public static ObservableList<String> AvailableVideoCamsList = FXCollections.observableArrayList();

	static {
		try {
			getAvailableCameras();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public static void getAvailableCameras() throws Exception {

        for (int i = 0; i < VideoInputFrameGrabber.getDeviceDescriptions().length; i++) {
            try (var grabber = new VideoInputFrameGrabber(i)) {
				try {
				    grabber.start();
				    AvailableVideoCamsList.add("Camera " + i + ": " + VideoInputFrameGrabber.getDeviceDescriptions()[i]);
				    grabber.stop();
				} catch (Exception e) {
				    e.printStackTrace();
				}
			}
        }
    }
}

