package uk.co.jlensmeister;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
	//variables to reference the listview objects for the users and chat from the FXML file
	@FXML private ListView UsersListBox;
	@FXML private ListView<String> ChatListBox;
	//variable to reference the textfield messages are inputted in from the FXML file
	@FXML private TextField ChatTextField;
	//variable to reference the send button form the FXML file
	@FXML private Button ChatButton;
	//variables to the panes that split up and organise the objects from the FXML file
	@FXML private SplitPane TabSplitPane;
	@FXML private AnchorPane TabLeftPane;
	//variable to store the room name
	private String tabname;
	//variable to reference the tabscontroller
	private TabsController tc;
	//function called when the controller is created
	public void initialize(URL arg0, ResourceBundle arg1) {
		//add listener for the text field
		ChatTextField.lengthProperty().addListener(new ChangeListener<Number>(){
			public void changed(ObservableValue<? extends Number> observable,
                    Number oldValue, Number newValue) {
				//if there number of characters in the field has got bigger
                if (newValue.intValue() > oldValue.intValue()) {
                	//if the text fields length is greater than or equal to 255
                    if (ChatTextField.getText().length() >= 255) {
                    	//limit the length to 255 by removing any input that is put in past 255
                        ChatTextField.setText(ChatTextField.getText().substring(0, 255));
                    }
                }
            }
		});
		//add a connecting to room message to the chat box
		ChatListBox.getItems().add("Connecting to room...");
		//add a listening for the send button
		ChatButton.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent arg0) {
				//send the message form the user
				sendMessageFromUser(ChatTextField.getText(), tc.user, tabname);
				//clear the textbox
				ChatTextField.setText("");
				//focus on the textbox
				ChatTextField.requestFocus();
			}
		});
		//add listener for if there is a keypressed while the textbox has focus
		ChatTextField.setOnKeyPressed(new EventHandler<KeyEvent>(){
			public void handle(KeyEvent event) {
				//if the button pressed is enter
				if(event.getCode().equals(KeyCode.ENTER)){
					//send the message 
					sendMessageFromUser(ChatTextField.getText(), tc.user, tabname);
					//clear the textbox
					ChatTextField.setText("");
				}
			}
		});
		
	}
	//function to send the message to the main server
	public void sendMessageFromUser(String inMessage, String inUser, String inRoom){
		//create a ChatMessage variable
		ChatMessage send = new ChatMessage();
		//input the message, user and room into the variable
		send.message = inMessage;
		send.user = inUser;
		send.room = inRoom;
		//pass the variable through to the tabscontroller sendmessage function for it to be sent
		tc.sendMessage(send);
		
	}
	//function to add a message to the chatbox
	public void addMessageFromUser(String name, String message){
		//add a message to the chatbox in the format Name: message
		ChatListBox.getItems().add(name + ": " + message);
	}
	//function to receive a reference to the tabscontroller from it
	public void setController(TabsController controller){
		tc = controller;
	}
	//function to set the tabname variable
	public void setTabName(String name){
		tabname = name;
	}
	//function to remove a user from the online users list
	public void removeOnlineUser(String name){
		//if the user is in the list already
		if(UsersListBox.getItems().contains(name)){
			//remove it from the list
			UsersListBox.getItems().remove(name);
			//add a message to chatbox saying the user disconnected from the room
			ChatListBox.getItems().add(name + " disconnected from the room.");
		}
	}
	//function to add a user to the online users list
	public void addOnlineUser(String name){
		//if the user is not in the list already
		if(!(UsersListBox.getItems().contains(name))){
			//add the user to the list
			UsersListBox.getItems().add(name);
			//check if the user is in the tab
			if(isUserInTab(tc.user)){
				//if the user joining the room is you change the name to "You"
				if(name.equals(tc.user)){
					name = "You";
				}
				//put a message saying that the user or "You" connected to the room
				ChatListBox.getItems().add(name + " connected to the room.");
			}
		}
	}
	//function to find out if the user is in the online users list
	public boolean isUserInTab(String name){
		ObservableList users = UsersListBox.getItems();
		//check if the user is in the list
		if(users.contains(name)){
			//return true if it is
			return true;
		}
		//return false if it isn't
		return false;
	}
	
}
