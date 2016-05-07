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

	//set the variables for the items from the FXML file
	@FXML private Pane pane;
	@FXML private MenuItem menuabout;
	@FXML private MenuItem menuclose;
	
	//variable to reference to the controller class for the tabpane
	private TabsController tc;
	//variable to reference to the cotnroller class for the content of a tab
	private TabContentController tcc;
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		//load the FXML file that contents the tabpane section and put it into the frame of the window
		FXMLLoader tabsloader = null;
		AnchorPane apane = null;
		try {
			tabsloader = new FXMLLoader(getClass().getResource("resources/Tabs.fxml"));
			apane = (AnchorPane)tabsloader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//assgin the tabscontroller variable
		tc = (TabsController)tabsloader.getController();
		//make the width and height of the tabpane the same as the frame
		apane.prefWidthProperty().bind(pane.widthProperty());
		apane.prefHeightProperty().bind(pane.heightProperty());
		//add the tabpane to the frame
		pane.getChildren().add(apane);
		//add listener if someone presses File>Close
		menuclose.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				//stop the program with no error code
				System.exit(0);
			}
		});
		//add listener if someone presses Help>About
		menuabout.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent e){
				//show a popup that tells the user about the program
				Alert dialog = new Alert(AlertType.INFORMATION);
				dialog.setTitle("About");
				dialog.setGraphic(null);
				dialog.setHeaderText(null);
				dialog.setContentText("Version 1.0\nDeveloped by David Thaler.");
				dialog.show();
			}
		});
		
	}
	//function to return the tabscontroller variable if requested
	public TabsController getTabsController(){
		return tc;
	}
	//function to return the tabscontentcontroller variable if requested
	public TabContentController getTabContentController(){
		return tcc;
	}
	
}
