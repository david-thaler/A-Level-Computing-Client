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

	@FXML private TabPane tabpane;
	private HashMap<String, TabContentController> tabs = new HashMap<String,TabContentController>();
	private Tab homeTab;
	 Client client;
	String user;
	public String serverIP = "127.0.0.1";

	public void initialize(URL arg0, ResourceBundle arg1) {

		tabpane.tabClosingPolicyProperty().set(TabClosingPolicy.SELECTED_TAB);
		homeTab = new Tab();
		homeTab.setText("Login");
		homeTab.setClosable(false);
		tabpane.getTabs().add(homeTab);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/LoginContent.fxml"));
		try {
			homeTab.setContent((Node)loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		LoginController loginController = (LoginController)loader.getController();
		loginController.setTabsController(this);
		
	}
	
	public TabContentController getTabContentController(String name){
		
		if(tabs.containsKey(name.toLowerCase())){
			return tabs.get(name.toLowerCase());
		}else{
			return null;
		}
		
	}
	
	public void makeTab(String name){
		
		if(tabs.isEmpty()){
			final Tab addTab = new Tab();
			addTab.setText("+");
			addTab.setClosable(false);
			tabpane.getTabs().add(addTab);
			tabs.put("+", null);
			addTab.setOnSelectionChanged(new EventHandler<Event>(){
				public void handle(Event t){
					if(addTab.isSelected()){
						Platform.runLater(new Runnable(){
							public void run(){
								TextInputDialog inputdialog = new TextInputDialog("Room Name");
								inputdialog.setTitle("Enter the room name");
								inputdialog.setHeaderText("");
								inputdialog.setContentText("Enter the room name:");
								Optional<String> input = inputdialog.showAndWait();
								if(input.isPresent()){
									if(tabs.containsKey(input.get().toLowerCase())){
										tabpane.getSelectionModel().selectPrevious();
									}else{
										if(input.get().equalsIgnoreCase("admin")){
											Alert alert = new Alert(AlertType.INFORMATION);
											alert.setTitle("Cannot create room!");
											alert.setHeaderText(null);
											alert.setContentText("You cannot create/join a room called \"admin\".");
											alert.showAndWait();
											tabpane.getSelectionModel().selectPrevious();
										}else{
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
		
		Tab newTab = new Tab();
		newTab.setText(name);
		FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/TabContent.fxml"));
		try {
			newTab.setContent((Node)loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		newTab.setClosable(true);
		final String n = name;
		newTab.setOnClosed(new EventHandler(){
			public void handle(Event event) {
				Platform.runLater(new Runnable(){
					public void run() {
						Left l = new Left();
						l.username = user;
						l.room = n.toLowerCase();
						client.sendTCP(l);
					}
				});
			}
		});
		tabpane.getTabs().add(newTab);
		TabContentController tabcontroller = (TabContentController)loader.getController();
		tabcontroller.setController(this);
		tabcontroller.setTabName(name.toLowerCase());
		tabs.put(name.toLowerCase(), tabcontroller);
		tabpane.getSelectionModel().select(newTab);
	}
	
	public void openAdmin(){
		Tab newTab = new Tab();
		newTab.setText("Admin");
		FXMLLoader loader = new FXMLLoader(getClass().getResource("resources/AdminContent.fxml"));
		try {
			newTab.setContent((Node)loader.load());
		} catch (IOException e) {
			e.printStackTrace();
		}
		AdminController ac = (AdminController) loader.getController();
		ac.setTabsController(this);
		newTab.setClosable(false);
		tabpane.getTabs().add(newTab);
	}
	
	public void connect(String username, String password){
		user = username;
		client = new Client();
		client.start();
		try {
			client.connect(5000, serverIP, 6697);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Kryo kryo = client.getKryo();
		kryo.register(ChatMessage.class);
		kryo.register(Disconnected.class);
		kryo.register(Joined.class);
		kryo.register(Left.class);
		
		client.sendTCP("F/"+username+"/"+password);
		user = username;
		
		client.addListener(new Listener(){
			public void received(Connection connection, Object object){
				if(object instanceof ChatMessage){
					final ChatMessage in = (ChatMessage)object;
					final TabContentController messagetab = tabs.get(in.room.toLowerCase());
					Platform.runLater(new Runnable(){
						public void run(){
							messagetab.addMessageFromUser(in.user, in.message);
						}
					});
				}else if(object instanceof Disconnected){
					Disconnected leaving = (Disconnected)object;
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
				}else if(object instanceof Joined){
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Joined joining = (Joined)object;
					if(joining.username.equalsIgnoreCase(user)){
						if(!tabs.containsKey(joining.room.toLowerCase())){
							final String room = joining.room;
							Platform.runLater(new Runnable(){
								public void run(){
									makeTab(room);
									addUserToTab(user, room);
								}
							});
						}
					}
					if(tabs.containsKey(joining.room.toLowerCase())){
						final Joined joined = joining;
						Platform.runLater(new Runnable(){
							public void run(){
								addUserToTab(joined.username, joined.room);
							}
						});
					}
				}else if(object instanceof String){
					String str = (String)object;
					if(str.contains("/")){
						String[] strr = str.split("/");
						if(strr[0].equals("R")){
							String[] rooms = strr[1].split(",");
							for(String s : rooms){
								final String r = s;
								Platform.runLater(new Runnable(){
									public void run(){
										makeTab(r);
									}
								});
							}
						}else if(strr[0].equals("L")){
							PrintWriter writer = null;
							try {
								writer = new PrintWriter("Log.txt");
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
							for(String m : strr){
								if(m.equals("L")){
								}else{
									writer.println(m);
								}
							}
							writer.close();
						}
					}
				}else if(object instanceof Left){
					final Left l = (Left) object;
					Platform.runLater(new Runnable(){

						public void run() {
							removeUserFromTab(l.username.toLowerCase(), l.room.toLowerCase());
						}
					});
				}
			}
			
			public void disconnected(Connection connection){
				
				Platform.runLater(new Runnable(){
					public void run(){
						tabpane.getTabs().clear();
						tabpane.getTabs().add(homeTab);
					}
				});
				
			}
		});
		
		tabpane.getTabs().remove(homeTab);
		
	}
	
	public void sendMessage(ChatMessage cm){
		if(cm.message.isEmpty() || cm.room.isEmpty() || cm.user.isEmpty()){
		}else{
			client.sendTCP(cm);
		}
	}
	
	public void addUserToTab(String username, String room){
		
		TabContentController tab = (TabContentController)tabs.get(room.toLowerCase());
		tab.addOnlineUser(username);
		
	}
	
	public void removeUserFromTab(String username, String room){
		TabContentController tab = tabs.get(room.toLowerCase());
		tab.removeOnlineUser(username);
	}
	
	public void joinRoom(String room){
		Joined joining = new Joined();
		joining.room = room;
		joining.username = user;
		client.sendTCP(joining);
	}
	
}
