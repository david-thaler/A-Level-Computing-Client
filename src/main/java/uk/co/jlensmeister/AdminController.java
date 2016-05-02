package uk.co.jlensmeister;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;

public class AdminController implements Initializable{

	@FXML private Button generatelog;
	@FXML private Button makeuseradmin;
	@FXML private Button banuser;
	
	private TabsController tc;
	
	public void initialize(URL location, ResourceBundle resources) {
		
		generatelog.setOnMouseClicked(new EventHandler(){
			public void handle(Event event) {
				String logrequest = "admin/L/";
				TextInputDialog changeServerInput = new TextInputDialog("");
				changeServerInput.setTitle("Get message logs");
				changeServerInput.setHeaderText("");
				changeServerInput.setContentText("Enter the room name you wish to get the log of: ");
				Optional<String> input = changeServerInput.showAndWait();
				if(input.isPresent()){
					logrequest = logrequest + input.get();
				}
				final String req = logrequest;
				Platform.runLater(new Runnable(){
					public void run() {
						tc.client.sendTCP(req);
					}
				});
			}
		});
		
		makeuseradmin.setOnMouseClicked(new EventHandler(){
			public void handle(Event event) {
				String admin = "admin/A/";
				TextInputDialog changeServerInput = new TextInputDialog("");
				changeServerInput.setTitle("Make User Admin");
				changeServerInput.setHeaderText("");
				changeServerInput.setContentText("Enter the username of the user to be given admin: ");
				Optional<String> input = changeServerInput.showAndWait();
				if(input.isPresent()){
					admin = admin + input.get();
				}
				final String req = admin;
				Platform.runLater(new Runnable(){
					public void run() {
						tc.client.sendTCP(req);
					}
				});
			}
		});
		
		banuser.setOnMouseClicked(new EventHandler(){
			public void handle(Event event) {
				String ban = "admin/B/";
				TextInputDialog changeServerInput = new TextInputDialog("");
				changeServerInput.setTitle("Ban a User");
				changeServerInput.setHeaderText("");
				changeServerInput.setContentText("Enter the username of the user to be banned: ");
				Optional<String> input = changeServerInput.showAndWait();
				if(input.isPresent()){
					ban = ban + input.get();
				}
				final String req = ban;
				Platform.runLater(new Runnable(){
					public void run() {
						tc.client.sendTCP(req);
					}
				});
			}
		});
	}

	public void setTabsController(TabsController tabscontroller){
		tc = tabscontroller;
	}
	
}
