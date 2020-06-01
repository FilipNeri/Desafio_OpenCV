package application;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import Utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TelaController {


	@FXML
	private CheckBox checkCinza;

	@FXML
	private Button buttonCam;

	@FXML
	private CheckBox checkLogo;

	@FXML
	private ImageView  imageHistograma;

	@FXML
	private ImageView insertFrame;


	private ScheduledExecutorService timer;

	private VideoCapture capture;


	private Mat logo;

	private boolean cameraAtiva;


	public void initialize()
	{
		this.capture = new VideoCapture();
		this.cameraAtiva = false;
	}

	@FXML
	public void iniciaCamera() {
		this.insertFrame.setFitWidth(600);
		this.insertFrame.setPreserveRatio(true);
		if (!this.cameraAtiva){
			this.capture.open(0);

			if(this.capture.isOpened()) {
				this.cameraAtiva = true;

				Runnable frameGrabber = new Runnable() {
					public void run(){
						Mat frame = geraFrame();
						Image imageToShow = Utils.mat2Image(frame);
						updateImageView(insertFrame, imageToShow);
					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				this.buttonCam.setText("Parar Camera");
			}
			else{

				System.err.println("Impossivel de conectar à camera");
			}
		}
		else{

			this.cameraAtiva = false;
			this.buttonCam.setText("Inicia Camera");
			this.stopAcquisition();
		}
	}

	@FXML
	private void carregaLogo(ActionEvent event) {
		if (checkLogo.isSelected())

			this.logo = Imgcodecs.imread("C:\\Users\\filip\\eclipse-workspace\\Desafio\\src\\imagem\\LOGO UNISUL.png");
	}


	public Mat geraFrame() {
		Mat frame = new Mat();

		if(this.capture.isOpened()) {
			try {

				this.capture.read(frame);
				if(!frame.empty()) {

					if(checkLogo.isSelected() && this.logo !=null) {

						Rect roi = new Rect(frame.cols()-logo.cols(), frame.rows()-logo.rows(), logo.cols(), logo.rows());
						Mat imageROI = frame.submat(roi);
						Core.addWeighted(imageROI, 1.0, logo, 0.7, 0.0, imageROI);	
					}

					if(checkCinza.isSelected()) {
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
					}

					this.showHistogram(frame, checkCinza.isSelected());
				}


			}catch(Exception e) {
				e.printStackTrace();
			}


		}
		return frame;
	}



	private void showHistogram(Mat frame, boolean gray){

		List<Mat> images = new ArrayList<Mat>();
		Core.split(frame, images);


		MatOfInt histSize = new MatOfInt(256);

		MatOfInt channels = new MatOfInt(0);

		MatOfFloat histRange = new MatOfFloat(0, 256);

		Mat hist_b = new Mat();
		Mat hist_g = new Mat();
		Mat hist_r = new Mat();


		Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist_b, histSize, histRange, false);


		if (!gray){
			Imgproc.calcHist(images.subList(1, 2), channels, new Mat(), hist_g, histSize, histRange, false);
			Imgproc.calcHist(images.subList(2, 3), channels, new Mat(), hist_r, histSize, histRange, false);
		}


		int hist_w = 150; 
		int hist_h = 150; 
		int bin_w = (int) Math.round(hist_w / histSize.get(0, 0)[0]);

		Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar(0, 0, 0));

		Core.normalize(hist_b, hist_b, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());


		if (!gray){
			Core.normalize(hist_g, hist_g, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
			Core.normalize(hist_r, hist_r, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
		}


		for (int i = 1; i < histSize.get(0, 0)[0]; i++){

			Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_b.get(i - 1, 0)[0])),
					new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);

			if (!gray){
				Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_g.get(i - 1, 0)[0])),
						new Point(bin_w * (i), hist_h - Math.round(hist_g.get(i, 0)[0])), new Scalar(0, 255, 0), 2, 8,
						0);
				Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_r.get(i - 1, 0)[0])),
						new Point(bin_w * (i), hist_h - Math.round(hist_r.get(i, 0)[0])), new Scalar(0, 0, 255), 2, 8,
						0);
			}
		}

		Image histImg = Utils.mat2Image(histImage);
		updateImageView( imageHistograma, histImg);

	}

	private void stopAcquisition(){
		if (this.timer != null && !this.timer.isShutdown())
		{
			try
			{

				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{

				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}

		if (this.capture.isOpened())
		{

			this.capture.release();
		}
	}

	private void updateImageView(ImageView view, Image image){
		Utils.onFXThread(view.imageProperty(), image);
	}

}
