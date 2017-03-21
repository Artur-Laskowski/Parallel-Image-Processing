package pt_lab3;

import java.io.File;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class ImageProcessingJob {
	public static final String STATUS_WAITING = "waiting";
	public static final String STATUS_INIT = "initializing";
	public static final String STATUS_DONE = "done";
	
	File file;
	SimpleStringProperty status;
	DoubleProperty progress;
	
	public ImageProcessingJob(File file) {
		this.file = file;
		this.status = new SimpleStringProperty(STATUS_WAITING);
		this.progress = new SimpleDoubleProperty(0);
	}
	
	public static ImageProcessingJob of(File file) {
		try {
			ImageProcessingJob job = new ImageProcessingJob(file);
			return job;
		} catch (Exception e) {
			return null;
		}
	}

	public DoubleProperty getProgressProperty() {
		return progress;
	}

	public File getFile() {
		return file;
	}

	public ObservableValue<String> getStatusProperty() {
		return status;
	}
	
	public void setProgressProperty(Double progress) {
		this.progress.set(progress);
	}
	
	public void setStatusProperty(String status) {
		this.status.set(status);
	}
}
