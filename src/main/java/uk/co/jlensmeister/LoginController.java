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

	@FXML private Button LoginButton;
	@FXML private Button RegisterButton;
	@FXML private Button changeServerButton;
	@FXML private TextField username;
	@FXML private PasswordField password;
	@FXML private Label TopLabel;
	
	private TabsController controller;
	private Client client;
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		changeServerButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent arg0){
				TextInputDialog changeServerInput = new TextInputDialog("");
				changeServerInput.setTitle("Your current Master Server is " + controller.serverIP);
				changeServerInput.setHeaderText("Change Master Server IP Address");
				changeServerInput.setContentText("Enter the hostname or IP of the master server: ");
				Optional<String> input = changeServerInput.showAndWait();
				if(input.isPresent()){
					if(input.get().length() < 5 || input.get().length() > 40){
						changeText("IP/Hostname was not set, it should be 5 to 40 characters long.");
					}else{
						controller.serverIP = input.get();
					}
				}
			}
		});
		
		username.lengthProperty().addListener(new ChangeListener<Number>(){
			public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
                if (newValue.intValue() > oldValue.intValue()) {
                    if (username.getText().length() > 20) {
                        username.setText(username.getText().substring(0, 20));
                    }
                }
            }
		});
		
		password.lengthProperty().addListener(new ChangeListener<Number>(){
			public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
                if (newValue.intValue() > oldValue.intValue()) {
                    if (password.getText().length() >= 255) {
                        password.setText(password.getText().substring(0, 255));
                    }
                }
            }
		});
		
		LoginButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent arg0) {
				if(username.getText().isEmpty() || password.getText().isEmpty()){
					changeText("You must enter a username and password.");
				}else if(username.getLength() < 4){
					changeText("Your username must be 4 to 20 characters long.");
				}else if(password.getLength() < 6){
					changeText("Your password must be between 6 and 255 characters long.");
				}else{
				client = new Client();
				client.start();
				try {
					client.connect(5000, controller.serverIP, 6698);
				} catch (IOException e) {
					TopLabel.setText("(Cannot Connect) Sorry the server seems to be down at the moment.");
					TopLabel.setTextFill(Color.RED);
					TopLabel.setFont(new Font("Arial", 15));
					e.printStackTrace();
				}
				
				String send = "L/" + username.getText() + "/" + password.getText();
				client.sendTCP(send);
				
				client.addListener(new Listener(){
					
					public void received(Connection connection, Object object){
						if(object instanceof String){
							String response = (String)object;
							if(response.contains("/")){
								final String[] data = response.split("/");
								if(data[0].equals("I")){

									Platform.runLater(new Runnable(){
										public void run() {
											changeText("A error with the program occured.");
											client.close();
										}
									});
									System.out.println(data[1]);
									
								}else if(data[0].equals("A")){
									Platform.runLater(new Runnable(){

										public void run() {
											changeText(data[1]);
											client.close();
										}
										
									});
								}else if(data[0].equals("S")){
									if(data[1].equals("Y")){
										Platform.runLater(new Runnable(){
											public void run(){
												openAdminTab();
												loginToMainServer();
												client.close();
											}
										});
									}else{
										Platform.runLater(new Runnable(){
											public void run(){
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
		
		RegisterButton.setOnMouseClicked(new EventHandler<MouseEvent>(){

			public void handle(MouseEvent arg0) {
				if(username.getText().isEmpty() || password.getText().isEmpty()){
					changeText("You must enter a username and password.");
				}else if(username.getLength() < 4){
					changeText("Your username must be 4 to 20 characters long.");
				}else if(password.getLength() < 6){
					changeText("Your password must be between 6 and 255 characters long.");
				}else{
					client = new Client();
					client.start();
					try {
						client.connect(5000, controller.serverIP, 6698);
					} catch (IOException e) {
						TopLabel.setText("(Cannot Connect) Sorry the server seems to be down at the moment.");
						TopLabel.setTextFill(Color.RED);
						TopLabel.setFont(new Font("Arial", 15));
						e.printStackTrace();
					}
					
					client.sendTCP("R/" + username.getText() + "/" + password.getText());
					
					client.addListener(new Listener(){
						public void received(Connection connection, Object object){
							if(object instanceof String){
								String s = (String)object;
								if(s.contains("/")){
									String[] tcp = s.split("/");
									if(tcp[0].equals("A")){
										final String errormessage = tcp[1];
										Platform.runLater(new Runnable(){
											public void run(){
												changeText(errormessage);
											}
										});
									}else if(tcp[0].equals("S")){
										Platform.runLater(new Runnable(){
											public void run(){
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
	
	public void changeText(String text){
		
		
		TopLabel.setText(text);
		TopLabel.setTextFill(Color.RED);
		TopLabel.setFont(new Font("Arial", 15));
		
	}
	
	public void openAdminTab(){
		controller.openAdmin();
	}

	public void loginToMainServer(){
		controller.connect(username.getText(), password.getText());
	}
	
	public void setTabsController(TabsController tc){
		controller = tc;
	}
	
}
