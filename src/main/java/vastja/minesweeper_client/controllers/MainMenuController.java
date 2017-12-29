package vastja.minesweeper_client.controllers;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import vastja.minesweeper_client.App;
import vastja.minesweeper_client.utils.Client;
import vastja.minesweeper_client.utils.Request;

public class MainMenuController implements Initializable, IController {
	
	private static final String CONNECTING = "Connecting to server, please wait ...";
	private static final String CONNECTED = "Connected to server";

	boolean startGamePressed;
	
    @FXML
    public Button btnStartGame;
    
    @FXML
    public Button btnExitGame;
    
    @FXML
    public Label conInfoLabel;
    
    @FXML
    public Label respInfoLabel;
    
    
    @FXML
    public ProgressIndicator progressIndicator;
    
    @FXML
    public VBox conVbox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    	startGamePressed = false;
    	
    	Client.getConnection().testConnection();
	}
    
    @FXML
    public void startGame() throws IOException {
    	startGamePressed = true;
    	Request request = new Request(Client.START_GAME);
    	Client.getConnection().send(request);
    	progressIndicator.setVisible(true);
    	btnStartGame.setDisable(true);	
    }
    
    @FXML
    public void exitGame() {
    	App.exitApp();
    }
	
	@Override
	public void goToDisconnectPage() {
		App.disconnected();
	}
	
	@Override
	public void disconnected() {
		App.disconnected();
	}
	
	@Override
	public void connected() {
		conInfoLabel.setText(CONNECTED);
		conVbox.setId("connection-vbox-connected");
	}
	
	@Override
	public void reconnecting() {
		btnStartGame.setDisable(true);
		conInfoLabel.setText(CONNECTING);
		conVbox.setId("connection-vbox-reconnecting");
	}
	
	@Override
	public void responseTimeout() {
		disconnected();
	}
	
	@Override
	public void corruptedMessages() {
		disconnected();
	}
	
	@Override
	public void requestRefused() {
		disconnected();
	}
	
	@Override
	public void reconnected() {
		if (!startGamePressed) {
			btnStartGame.setDisable(false);
		}
		connected();
	}
}