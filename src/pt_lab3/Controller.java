package pt_lab3;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.FileChooser;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ForkJoinPool;

import javax.imageio.ImageIO;

public class Controller implements Initializable {
	
	@FXML TableView<ImageProcessingJob> imagesTable;
	@FXML TableColumn<ImageProcessingJob, String> imageNameColumn;
	@FXML TableColumn<ImageProcessingJob, Double> progressColumn;
	
	@FXML TableColumn<ImageProcessingJob, String> statusColumn;
	
	@FXML Label label;
	
	List<File> selectedFiles;
	
	long start = 0;
	long duration = 0;

    ObservableList<ImageProcessingJob> jobs = FXCollections.observableArrayList();
    
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		imageNameColumn.setCellValueFactory(
				p->new SimpleStringProperty(p.getValue().getFile().getName()));
		
		statusColumn.setCellValueFactory(
				p->p.getValue().getStatusProperty());
		
		progressColumn.setCellFactory(
				ProgressBarTableCell.<ImageProcessingJob>forTableColumn());
		
		progressColumn.setCellValueFactory(
				p->p.getValue().getProgressProperty().asObject());
	}
	
	@FXML
	void setFiles(ActionEvent event) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("JPG images", "*.jpg"));
		selectedFiles = fileChooser.showOpenMultipleDialog(null);
		for (File f : selectedFiles) {
			jobs.add(ImageProcessingJob.of(f));
		}
		imagesTable.setItems(jobs);
	}
	
	@FXML
	void processFiles(ActionEvent event) {
		start = System.currentTimeMillis();
		for (ImageProcessingJob j : jobs) {
			j.setProgressProperty(0.0);
		}
		ForkJoinPool forkJoinPool = new ForkJoinPool(3);
		forkJoinPool.submit(() -> jobs.parallelStream().forEach(this::convertStart));
	}
	
	@FXML
	void processFiles2(ActionEvent event) {
		start = System.currentTimeMillis();
		for (ImageProcessingJob j : jobs) {
			j.setProgressProperty(0.0);
		}
		new Thread(this::backgroundJobParalell).start();
	}
	
	@FXML
	void processFiles3(ActionEvent event) {
		start = System.currentTimeMillis();
		for (ImageProcessingJob j : jobs) {
			j.setProgressProperty(0.0);
		}
		new Thread(this::backgroundJob).start();
	}
	
	@FXML
	void getTime(ActionEvent event) {
		label.setText(duration + "ms");
	}
	
	private void backgroundJobParalell() {
		//wersja wspó³bie¿na
		jobs.parallelStream().forEach(this::convertStart);
	}
	
	private void backgroundJob() {
		//wersja sekwencyjna
		jobs.stream().forEach(this::convertStart);
	}
	
	private void convertStart(ImageProcessingJob job) {
		Platform.runLater(()->job.setStatusProperty(ImageProcessingJob.STATUS_INIT));
		
		File file = job.getFile();
		File dir = new File(file.getParent()+"\\output\\");
		convertToGrayscale(file, dir, job.getProgressProperty());
        Platform.runLater(() -> job.setStatusProperty(ImageProcessingJob.STATUS_DONE));
        duration = System.currentTimeMillis() - start;
	}
	
	private void convertToGrayscale(
		File originalFile, //oryginalny plik graficzny
		File outputDir, //katalog docelowy
		DoubleProperty progressProp//w³asnoœæ okreœlaj¹ca postêp operacji
	) {
		try {
			//wczytanie oryginalnego pliku do pamiêci
			BufferedImage original = ImageIO.read(originalFile);
					
			//przygotowanie bufora na grafikê w skali szaroœci
			BufferedImage grayscale = new BufferedImage(
					original.getWidth(), original.getHeight(), original.getType());

		    //przetwarzanie piksel po pikselu
			for (int i = 0; i < original.getWidth(); i++) {
				for (int j = 0; j < original.getHeight(); j++) { 
			
		            //pobranie sk³adowych RGB
					int red = new Color(original.getRGB(i, j)).getRed();
					int green = new Color(original.getRGB(i, j)).getGreen();
					int blue = new Color(original.getRGB(i, j)).getBlue(); 
		 
		            //obliczenie jasnoœci piksela dla obrazu w skali szaroœci
					int luminosity = (int) (0.21*red + 0.71*green + 0.07*blue);
					//przygotowanie wartoœci koloru w oparciu o obliczon¹ jaskoœæ
					int newPixel =
							new Color(luminosity, luminosity, luminosity).getRGB(); 
		 
		            //zapisanie nowego piksela w buforze
					grayscale.setRGB(i, j, newPixel);
				} 
		 
		        //obliczenie postêpu przetwarzania jako liczby z przedzia³u [0, 1]
				double progress = (1.0 + i) / original.getWidth();
				//aktualizacja w³asnoœci zbindowanej z paskiem postêpu w tabeli
				Platform.runLater(() -> progressProp.set(progress));
			} 
		 
		    //przygotowanie œcie¿ki wskazuj¹cej na plik wynikowy
			Path outputPath =
					Paths.get(outputDir.getAbsolutePath(), originalFile.getName());
			//zapisanie zawartoœci bufora do pliku na dysku
			ImageIO.write(grayscale, "jpg", outputPath.toFile()); 
		 
		} catch (IOException ex) {
			//translacja wyj¹tku
			throw new RuntimeException(ex);
		}
	}
}
