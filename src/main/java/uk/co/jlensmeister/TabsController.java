package uk.co.jlensmeister;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import uk.co.jlensmeister.*;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class TabsController implements Initializable{
	//variable to reference the tabpane from the FXML file
	@FXML private TabPane tabpane;
	//Maps a string to the tabscontentcontroller, matching the room name to it's controller instance
	private HashMap<String, TabContentController> tabs = new HashMap<String,TabContentController>();
	//variable to hold the homeTab
	private Tab homeTab;
	//global variable to hold the client connection to the server so it can be accessed by
	//other classes
	Client client;
	//current username in use
	String user;
	//string to store the IP/Hostname of the main server
	public String serverIP = "127.0.0.1";
	//called when the controller is launched
	public void initialize(URL arg0, ResourceBundle arg1) {
		//make it so when a tab is closed it closes the selected tab only and 
		//the X in the tab only appears in the current one
		tabpane.tabClosingPolicyProperty().set(TabClosingPolicy.SELECTED_TAB);
		//create the login tab and add it to the tabpane
		homeTab = new Tab();
		homeTab.setText("Login");
		homeTab.setClosable(false);
		tabpane.getTabs().add(homeTab);
		//get the FXML file for the login tab content and put it in the new tab
		FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/LoginContent.fxml"));
		try {
			homeTab.setContent((Node)loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//get the controller for the login screen and pass the tabscontroller variable through
		LoginController loginController = (LoginController)loader.getController();
		loginController.setTabsController(this);
		
	}
	//function to get the tabcontentctroller of a room
	public TabContentController getTabContentController(String name){
		//if it is found return the name if not return nothing
		if(tabs.containsKey(name.toLowerCase())){
			return tabs.get(name.toLowerCase());
		}else{
			return null;
		}
		
	}
	//function to make anew tab
	public void makeTab(String name){
		//check if there is no join new room tab
		if(tabs.isEmpty()){
			//create the tab and set it's title to +
			final Tab addTab = new Tab();
			addTab.setText("+");
			//don't allow it to be closed
			addTab.setClosable(false);
			//add it to the tabpane
			tabpane.getTabs().add(addTab);
			//put it in the tabs map
			tabs.put("+", null);
			//add listener for when it is selected
			addTab.setOnSelectionChanged(new EventHandler<Event>(){
				public void handle(Event t){
					//if the tab is selected
					if(addTab.isSelected()){
						Platform.runLater(new Runnable(){
							public void run(){
								//open a input box asking for the room name to join
								TextInputDialog inputdialog = new TextInputDialog("Room Name");
								inputdialog.setTitle("Enter the room name");
								inputdialog.setHeaderText("");
								inputdialog.setContentText("Enter the room name:");
								//show the dialog and wait for a response
								Optional<String> input = inputdialog.showAndWait();
								//if the response has been submitted
								if(input.isPresent()){
									//if the room already exists don't create new room and select the last tab
									if(tabs.containsKey(input.get().toLowerCase())){
										tabpane.getSelectionModel().selectPrevious();
									}else{
										//if the new room name is admin alert the user they cannot make a room with that name
										if(input.get().equalsIgnoreCase("admin")){
											Alert alert = new Alert(AlertType.INFORMATION);
											alert.setTitle("Cannot create room!");
											alert.setHeaderText(null);
											alert.setContentText("You cannot create/join a room called \"admin\".");
											alert.showAndWait();
											//select the last selected tab
											tabpane.getSelectionModel().selectPrevious();
										}else{
											//join the new room
											joinRoom(input.get());
											tabpane.getSelectionModel().selectLast();
										}
									}
								}
							}
						});
					}
				}
			});
		}
		//create the new tab and set it's title to the room name
		Tab newTab = new Tab();
		newTab.setText(name);
		//load the chat content FXML file to the tab
		FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/TabContent.fxml"));
		try {
			newTab.setContent((Node)loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//alow the new tab to be closed
		newTab.setClosable(true);
		final String n = name;
		//add listener for if the tab is closed
		newTab.setOnClosed(new EventHandler(){
			public void handle(Event event) {
				Platform.runLater(new Runnable(){
					public void run() {
						//create a Left class with the username and room
						Left l = new Left();
						l.username = user;
						l.room = n.toLowerCase();
						//send the Left class to the main server to be distributed
						client.sendTCP(l);
					}
				});
			}
		});
		//add the new tab to the tabpane
		tabpane.getTabs().add(newTab);
		TabContentController tabcontroller = (TabContentController)loader.getController();
		//pass the tabcontroller through to the tabcontentcontroller
		tabcontroller.setController(this);
		//tell the tabcontentcontroller what the room name is
		tabcontroller.setTabName(name.toLowerCase());
		//add the new room and controller to the tabs map
		tabs.put(name.toLowerCase(), tabcontroller);
		//select the new tab
		tabpane.getSelectionModel().select(newTab);
	}
	//function to open the admin tab
	public void openAdmin(){
		//create new tab for it and set the title as admin
		Tab newTab = new Tab();
		newTab.setText("Admin");
		//load the FXML file contents onto then tab
		FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/AdminContent.fxml"));
		try {
			newTab.setContent((Node)loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//get the controller
		AdminController ac = (AdminController) loader.getController();
		//pass through the tabscontroller variable through
		ac.setTabsController(this);
		//don't allow the tab to be closed
		newTab.setClosable(false);
		//add the new tab to the tabpane
		tabpane.getTabs().add(newTab);
	}
	//function to connect to the main server with a username and password
	public void connect(String username, String password){
		user = username;
		//initialise the client variable and connect to the main server with the IP and port 6697
		client = new Client();
		client.start();
		try {
			client.connect(5000, serverIP, 6697);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//register the classes to be sent to and form the server with the TCP api
		Kryo kryo = client.getKryo();
		kryo.register(ChatMessage.class);
		kryo.register(Disconnected.class);
		kryo.register(Joined.class);
		kryo.register(Left.class);
		//send a first time login string to the server to get full access
		client.sendTCP("F/"+username+"/"+password);
		user = username;
		//add a listener for received data form the server
		client.addListener(new Listener(){
			//method to be called if data is received from the server
			public void received(Connection connection, Object object){
				//if the data received is of a ChatMessage type
				if(object instanceof ChatMessage){
					//cast the object as a ChatMessage
					final ChatMessage in = (ChatMessage)object;
					//get the controller of the tab which is the room that the message is sent in
					final TabContentController messagetab = tabs.get(in.room.toLowerCase());
					Platform.runLater(new Runnable(){
						public void run(){
							//add the message in the tab through the addMessageFromUser function
							messagetab.addMessageFromUser(in.user, in.message);
						}
					});
				//if the data received is of a Diconnected type
				}else if(object instanceof Disconnected){
					//cast the object as a Disconnected class
					Disconnected leaving = (Disconnected)object;
					//loop through all the tabs and send a removeuserfromtab function to all the tabs
					Iterator it = tabs.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry pair = (Map.Entry)it.next();
				        final String user = leaving.username;
				        final String room = (String)pair.getKey();
				        Platform.runLater(new Runnable(){
				        	public void run() {
						        removeUserFromTab(user, room);
							}
				        });
				    }
				   //if the data received is of a Joined type
				}else if(object instanceof Joined){
					//Wait for 100 milliseconds to make sure there is no interruptions by other joined notifications
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					//cast the object as a Joined class
					Joined joining = (Joined)object;
					//if the joined alert is about the current user of the applications
					if(joining.username.equalsIgnoreCase(user)){
						//if the tab does not already exist
						if(!tabs.containsKey(joining.room.toLowerCase())){
							//create new tab for the room and add the current user to the tab
							final String room = joining.room;
							Platform.runLater(new Runnable(){
								public void run(){
									makeTab(room);
									addUserToTab(user, room);
								}
							});
						}
					}
					//if a tab for the room exists
					if(tabs.containsKey(joining.room.toLowerCase())){
						final Joined joined = joining;
						//add user to the tab
						Platform.runLater(new Runnable(){
							public void run(){
								addUserToTab(joined.username, joined.room);
							}
						});
					}
				//if the data received is a string
				}else if(object instanceof String){
					//cast the object to a string 
					String str = (String)object;
					if(str.contains("/")){
						String[] strr = str.split("/");
						//if the received data is about the rooms the user was last in
						if(strr[0].equals("R")){
							//put all the rooms into an array
							String[] rooms = strr[1].split(",");
							//loop the the rooms
							for(String s : rooms){
								final String r = s;
								//add tabs for the room
								Platform.runLater(new Runnable(){
									public void run(){
										makeTab(r);
									}
								});
							}
						//if the data being receieved is a Log for an admin
						}else if(strr[0].equals("L")){
							//create a writer to the log file Log.txt
							PrintWriter writer = null;
							try {
								writer = new PrintWriter("Log.txt");
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
							//for each message that was received print it to the log
							//do not print the L at the start of the string
							for(String m : strr){
								if(m.equals("L")){
								}else{
									writer.println(m);
								}
							}
							//close the writer
							writer.close();
						}
					}
				//if the data received is of a Left type
				}else if(object instanceof Left){
					//cast the object to the Left class
					final Left l = (Left) object;
					Platform.runLater(new Runnable(){
						public void run() {
							//call the removeuserfromtab function for the user that left from the room
							removeUserFromTab(l.username.toLowerCase(), l.room.toLowerCase());
						}
					});
				}
			}
			//method called if the connection is disconnected
			public void disconnected(Connection connection){
				
				Platform.runLater(new Runnable(){
					public void run(){
						//remove all the tabs and open the login screen again so the user can login again
						tabpane.getTabs().clear();
						tabpane.getTabs().add(homeTab);
					}
				});
				
			}
		});
		//remove the login tab from the tabpane as it is no longer needed
		tabpane.getTabs().remove(homeTab);
		
	}
	//function to send a message
	public void sendMessage(ChatMessage cm){
		//check to see if the ChatMessage variable is empty
		if(cm.message.isEmpty() || cm.room.isEmpty() || cm.user.isEmpty()){
		}else{
			//if not send the ChatMessage variable to the server to be distributed
			client.sendTCP(cm);
		}
	}
	//function to add a user to a tab
	public void addUserToTab(String username, String room){
		//get the tab controller for the specified room
		TabContentController tab = (TabContentController)tabs.get(room.toLowerCase());
		//pass through the username to the addonlineuser function in the controller
		tab.addOnlineUser(username);
		
	}
	//function to remove a user form a tab
	public void removeUserFromTab(String username, String room){
		//get the controller for the tab of the specified room
		TabContentController tab = tabs.get(room.toLowerCase());
		//pass through the username to the removeonlineuser function in the controller
		tab.removeOnlineUser(username);
	}
	//function to tell the main server that the client is joining a room
	public void joinRoom(String room){
		//create a Joined class to send to the server
		Joined joining = new Joined();
		//input all the details needed
		joining.room = room;
		joining.username = user;
		//send the Joined instance to the main server to be distributed
		client.sendTCP(joining);
	}
	
}
