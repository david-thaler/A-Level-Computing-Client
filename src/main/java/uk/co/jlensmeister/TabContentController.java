package uk.co.jlensmeister;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

@SuppressWarnings("rawtypes")
public class TabContentController implements Initializable{

	@FXML private ListView UsersListBox;
	@FXML private ListView<String> ChatListBox;
	@FXML private TextField ChatTextField;
	@FXML private Button ChatButton;
	@FXML private SplitPane TabSplitPane;
	@FXML private AnchorPane TabLeftPane;
	
	private String tabname;
	private TabsController tc;
	
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		ChatListBox.getItems().add("Connecting to room...");
		
		ChatButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent arg0) {
				sendMessageFromUser(ChatTextField.getText(), tc.user, tabname);
				ChatTextField.setText("");
				ChatTextField.requestFocus();
			}
		});
		
		ChatTextField.setOnKeyPressed(new EventHandler<KeyEvent>(){
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){
					sendMessageFromUser(ChatTextField.getText(), tc.user, tabname);
					ChatTextField.setText("");
				}
			}
		});
		
	}
	
	public void sendMessageFromUser(String inMessage, String inUser, String inRoom){
		
		ChatMessage send = new ChatMessage();
		send.message = inMessage;
		send.user = inUser;
		send.room = inRoom;
		tc.sendMessage(send);
		
	}
	
	public void addMessageFromUser(String name, String message){
		
		ChatListBox.getItems().add("" + name + ": " + message);
		
	}
	
	public void setController(TabsController controller){
		tc = controller;
	}
	
	public void setTabName(String name){
		tabname = name;
	}
	
	public void removeOnlineUser(String name){
		if(UsersListBox.getItems().contains(name)){
			UsersListBox.getItems().remove(name);
			ChatListBox.getItems().add(name + " disconnected from the room.");
		}
	}
	
	public void addOnlineUser(String name){
		if(!(UsersListBox.getItems().contains(name))){
			UsersListBox.getItems().add(name);
			if(isUserInTab(tc.user)){
				if(name.equals(tc.user)){
					name = "You";
				}
				ChatListBox.getItems().add(name + " connected to the room.");
			}
		}
	}
	
	public boolean isUserInTab(String name){
		ObservableList users = UsersListBox.getItems();
		if(users.contains(name)){
			return true;
		}
		return false;
	}
	
}
