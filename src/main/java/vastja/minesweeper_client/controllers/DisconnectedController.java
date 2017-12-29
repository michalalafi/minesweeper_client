package vastja.minesweeper_client.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import vastja.minesweeper_client.App;
import vastja.minesweeper_client.utils.Client;
import vastja.minesweeper_client.utils.Request;

public class DisconnectedController implements Initializable, IController {
    
    @FXML
    public Button exitApp;
    
    
    @FXML
    public void exitApp() {
    	App.exitApp();
    }

	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
    
	@Override
	public void goToDisconnectPage() {
	}
	
	@Override
	public void disconnected() {
	}
	
	@Override
	public void connected() {
	}
	
	@Override
	public void reconnecting() {
	}
	
	@Override
	public void responseTimeout() {
	}
	
	@Override
	public void corruptedMessages() {
	} 
    
	@Override
	public void requestRefused() {
	}
	
	@Override
	public void reconnected() {
	}
}