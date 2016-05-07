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
	//variables to reference the buttons on the admin tab from the FXML file
	@FXML private Button generatelog;
	@FXML private Button makeuseradmin;
	@FXML private Button banuser;
	@FXML private Button unbanuser;
	//variable to reference the tabscontroller
	private TabsController tc;
	//function called when the controller is created
	public void initialize(URL location, ResourceBundle resources) {
		//add a listener for when the generate log button is pressed
		generatelog.setOnMouseClicked(new EventHandler(){
			public void handle(Event event) {
				//start a string requesting an admin request of a log
				String logrequest = "admin/L/";
				//open an input dialog requesting the room name of a logs
				TextInputDialog generatelogdialog = new TextInputDialog("");
				generatelogdialog.setTitle("Get message logs");
				generatelogdialog.setHeaderText("");
				generatelogdialog.setContentText("Enter the room name you wish to get the log of: ");
				//show the dialog and wait for input
				Optional<String> input = generatelogdialog.showAndWait();
				//if there is input
				if(input.isPresent()){
					//add the room to the request string
					logrequest = logrequest + input.get();
					final String req = logrequest;
					Platform.runLater(new Runnable(){
						public void run() {
							//send the admin request to the server
							tc.client.sendTCP(req);
						}
					});
				}
			}
		});
		//add a listener for when the makeuseradmin button is pressed
		makeuseradmin.setOnMouseClicked(new EventHandler(){
			public void handle(Event event) {
				//start a string requesting an admin request of making a user admin
				String admin = "admin/A/";
				//open an input dialog requesting the name of the user
				TextInputDialog makeuseradmindialog = new TextInputDialog("");
				makeuseradmindialog.setTitle("Make User Admin");
				makeuseradmindialog.setHeaderText("");
				makeuseradmindialog.setContentText("Enter the username of the user to be given admin: ");
				//show the dialog and wait for input
				Optional<String> input = makeuseradmindialog.showAndWait();
				//if input is there
				if(input.isPresent()){
					//add the username to the admin request
					admin = admin + input.get();
					final String req = admin;
					Platform.runLater(new Runnable(){
						public void run() {
							//send the admin request to the server
							tc.client.sendTCP(req);
						}
					});
				}
			}
		});
		//add listner for when the banuser button is pressed
		banuser.setOnMouseClicked(new EventHandler(){
			public void handle(Event event) {
				//start a string requesting an admin request of banning a user
				String ban = "admin/B/";
				//open an input dialog requesting the name of the user
				TextInputDialog banuserdialog = new TextInputDialog("");
				banuserdialog.setTitle("Ban a User");
				banuserdialog.setHeaderText("");
				banuserdialog.setContentText("Enter the username of the user to be banned: ");
				//show the dialog and wait for input
				Optional<String> input = banuserdialog.showAndWait();
				//if input is there
				if(input.isPresent()){
					//add the username to the admin request
					ban = ban + input.get();
					final String req = ban;
					Platform.runLater(new Runnable(){
						public void run() {
							//send the admin request to the server
							tc.client.sendTCP(req);
						}
					});
				}
			}
		});
		//add a listener for when the unbanuser button is pressed
		unbanuser.setOnMouseClicked(new EventHandler(){
			public void handle(Event event){
				//start an admin request string for unbanning a user
				String unban = "admin/U/";
				//display a input dialog asking for the username of the user
				TextInputDialog unbanuserdialog = new TextInputDialog("");
				unbanuserdialog.setTitle("Unban a User");
				unbanuserdialog.setHeaderText("");
				unbanuserdialog.setContentText("Enter the username of the user to be unbanned: ");
				//show the dialog and wait for input
				Optional<String> input = unbanuserdialog.showAndWait();
				//if the input is there
				if(input.isPresent()){
					//add the username to the request string
					unban = unban + input.get();
					final String req = unban;
					Platform.runLater(new Runnable(){
						public void run() {
							//send the request to the server
							tc.client.sendTCP(req);
						}
					});
				}
			}
		});
	}
	//function to set the variable to reference the tabscontroller
	public void setTabsController(TabsController tabscontroller){
		tc = tabscontroller;
	}
	
}
