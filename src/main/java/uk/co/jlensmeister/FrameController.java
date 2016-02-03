package uk.co.jlensmeister;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class FrameController implements Initializable{

	@FXML private Pane pane;
	@FXML private MenuItem menuabout;
	@FXML private MenuItem menuclose;
	
	private TabsController tc;
	private TabContentController tcc;
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		FXMLLoader tabsloader = null;
		AnchorPane apane = null;
		try {
			tabsloader = new FXMLLoader(getClass().getResource("resources/Tabs.fxml"));
			apane = (AnchorPane)tabsloader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		tc = (TabsController)tabsloader.getController();
		apane.prefWidthProperty().bind(pane.widthProperty());
		apane.prefHeightProperty().bind(pane.heightProperty());
		pane.getChildren().add(apane);
		
		menuclose.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				System.exit(0);
			}
		});
		
		menuabout.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				Alert dialog = new Alert(AlertType.INFORMATION);
				dialog.setTitle("About");
				dialog.setGraphic(null);
				dialog.setHeaderText(null);
				dialog.setContentText("Version 1.0\nDeveloped by David Thaler.");
				dialog.show();
			}
		});
		
	}

	public TabsController getTabsController(){
		return tc;
	}
	
	public TabContentController getTabContentController(){
		return tcc;
	}
	
}
