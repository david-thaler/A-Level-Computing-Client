package uk.co.jlensmeister;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class FrameController implements Initializable{

	@FXML private Pane pane;
	
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
		
	}

	public TabsController getTabsController(){
		return tc;
	}
	
	public TabContentController getTabContentController(){
		return tcc;
	}
	
}
