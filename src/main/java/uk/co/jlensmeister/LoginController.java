package uk.co.jlensmeister;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class LoginController implements Initializable{
	//variables to reference the login, register and changeserver buttons form the FXML file
	@FXML private Button LoginButton;
	@FXML private Button RegisterButton;
	@FXML private Button changeServerButton;
	//variables to reference the username and password fields from the FXML file
	@FXML private TextField username;
	@FXML private PasswordField password;
	//variable to reference the label at the top for error messages from the FXML file
	@FXML private Label TopLabel;
	//variable to reference the tabscontroller
	private TabsController controller;
	//variable to reference the client instance that connects to the auth server
	private Client client;
	//function called when the controller is launcher
	public void initialize(URL arg0, ResourceBundle arg1) {
		//add a listener for when the changeserver button is pressed
		changeServerButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent arg0){
				//open a input dialog asking for the Hostname/IP and displaying the current one
				TextInputDialog changeServerInput = new TextInputDialog("");
				changeServerInput.setTitle("Your current Master Server is " + controller.serverIP);
				changeServerInput.setHeaderText("Change Master Server IP Address");
				changeServerInput.setContentText("Enter the hostname or IP of the master server: ");
				//show the dialog and wait for input
				Optional<String> input = changeServerInput.showAndWait();
				//when input is submitted
				if(input.isPresent()){
					//if the length is less than 5 or greater than 40
					if(input.get().length() < 5 || input.get().length() > 40){
						//display error message
						changeText("IP/Hostname was not set, it should be 5 to 40 characters long.");
					}else{
						//set the serverIP to the input
						controller.serverIP = input.get();
					}
				}
			}
		});
		//add a listener for the username textbox
		username.lengthProperty().addListener(new ChangeListener<Number>(){
			public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
				//if the length of the input is getting bigger
                if (newValue.intValue() > oldValue.intValue()) {
                	//if the length is greater than 20 then stop taking input and cut off the input at 20 characters
                    if (username.getText().length() > 20) {
                        username.setText(username.getText().substring(0, 20));
                    }
                }
            }
		});
		//add a listener for the password textbox
		password.lengthProperty().addListener(new ChangeListener<Number>(){
			public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
				//if the length of the input is getting bigger
                if (newValue.intValue() > oldValue.intValue()) {
                	//if the length is greater than or equal to 255 then stop taking input and cut off the input at 255 characters
                    if (password.getText().length() >= 255) {
                        password.setText(password.getText().substring(0, 255));
                    }
                }
            }
		});
		//add a listener for when the login button is pressed
		LoginButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent arg0) {
				//if the username or password is empty
				if(username.getText().isEmpty() || password.getText().isEmpty()){
					//display error message
					changeText("You must enter a username and password.");
				//if the username is too short
				}else if(username.getLength() < 4){
					//display error message
					changeText("Your username must be 4 to 20 characters long.");
				//if password is too short
				}else if(password.getLength() < 6){
					//display error message
					changeText("Your password must be between 6 and 255 characters long.");
				}else{
				//connect to the authentication server on port 6698
				client = new Client();
				client.start();
				try {
					client.connect(5000, controller.serverIP, 6698);
				} catch (IOException e) {
					//if it cannot be connected to display error message
					TopLabel.setText("(Cannot Connect) Sorry the server seems to be down at the moment.");
					TopLabel.setTextFill(Color.RED);
					TopLabel.setFont(new Font("Arial", 15));
					e.printStackTrace();
				}
				//send a login string
				String send = "L/" + username.getText() + "/" + password.getText();
				client.sendTCP(send);
				//add a listener for received data form the server
				client.addListener(new Listener(){
					//function to be called if data is received
					public void received(Connection connection, Object object){
						//if data is in the form of a string
						if(object instanceof String){
							//cast the object as a string
							String response = (String)object;
							if(response.contains("/")){
								final String[] data = response.split("/");
								//if there was a problem with the server
								if(data[0].equals("I")){
									Platform.runLater(new Runnable(){
										public void run() {
											//display error message and close connection to the server
											changeText("A error with the program occured.");
											client.close();
										}
									});
									//print out the error in the console for error checking
									System.out.println(data[1]);
								//if there was an issue with the details
								}else if(data[0].equals("A")){
									Platform.runLater(new Runnable(){
										public void run() {
											//display the error message given by the server and close the connection to the server
											changeText(data[1]);
											client.close();
										}
									});
								//if the login was successful
								}else if(data[0].equals("S")){
									//if the user is an admin
									if(data[1].equals("Y")){
										Platform.runLater(new Runnable(){
											public void run(){
												//open the admin tab and login to the main server
												openAdminTab();
												loginToMainServer();
												client.close();
											}
										});
									}else{
										Platform.runLater(new Runnable(){
											public void run(){
												//login to the main server
												loginToMainServer();
												client.close();
											}
										});
									}
								}
								
							}
						}
					}
					
				});
				
				}
			}
		});
		//add a listener if the register button is pressed
		RegisterButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent arg0) {
				//if the username or password is empty
				if(username.getText().isEmpty() || password.getText().isEmpty()){
					//display error message
					changeText("You must enter a username and password.");
				//if the username is too short
				}else if(username.getLength() < 4){
					//display error message
					changeText("Your username must be 4 to 20 characters long.");
				//if password is too short
				}else if(password.getLength() < 6){
					//display error message
					changeText("Your password must be between 6 and 255 characters long.");
				}else{
					//open connection to the auth server on port 6698
					client = new Client();
					client.start();
					try {
						client.connect(5000, controller.serverIP, 6698);
					} catch (IOException e) {
						//if it can't connect display error message
						TopLabel.setText("(Cannot Connect) Sorry the server seems to be down at the moment.");
						TopLabel.setTextFill(Color.RED);
						TopLabel.setFont(new Font("Arial", 15));
						e.printStackTrace();
					}
					//send registration string to the server
					client.sendTCP("R/" + username.getText() + "/" + password.getText());
					//add listener for when data is received
					client.addListener(new Listener(){
						public void received(Connection connection, Object object){
							//if the data is in the form of a string
							if(object instanceof String){
								//cast the data to the string type
								String s = (String)object;
								if(s.contains("/")){
									String[] tcp = s.split("/");
									//if there was an error with the registration
									if(tcp[0].equals("A")){
										final String errormessage = tcp[1];
										Platform.runLater(new Runnable(){
											public void run(){
												//display error message given by the auth server
												changeText(errormessage);
											}
										});
									//if the registration was successful
									}else if(tcp[0].equals("S")){
										Platform.runLater(new Runnable(){
											public void run(){
												//display message saying the reigstration was successful and the user can login
												changeText("Registration successful, please login.");
											}
										});
									}
								}
							}
						}
					});
					
				}
			}
			
		});
		
	}
	//function to change the text of the label at the top
	public void changeText(String text){
		//set the labels text and font and colour
		TopLabel.setText(text);
		TopLabel.setTextFill(Color.RED);
		TopLabel.setFont(new Font("Arial", 15));
	}
	//function to open the admintab
	public void openAdminTab(){
		//call the function to open the admin tab in the tabscontroller
		controller.openAdmin();
	}
	//function to login to the main server
	public void loginToMainServer(){
		//call the function to connect to the main server in the tabscontroller
		controller.connect(username.getText(), password.getText());
	}
	//function to set the variable to reference the tabscontroller
	public void setTabsController(TabsController tc){
		controller = tc;
	}
}
