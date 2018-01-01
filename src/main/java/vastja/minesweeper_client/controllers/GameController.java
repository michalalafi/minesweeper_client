package vastja.minesweeper_client.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.corba.se.impl.ior.GenericTaggedComponent;

import javafx.scene.control.TextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import vastja.minesweeper_client.App;
import vastja.minesweeper_client.nodes.Board;
import vastja.minesweeper_client.utils.Client;
import vastja.minesweeper_client.utils.Request;

public class GameController implements Initializable, IController {
	
	private static final String CONNECTING = "Connecting to server, please wait ...";
	private static final String CONNECTED = "Connected to server";
	private static final String RECONNECT_TO_GAME = "Reconnecting to your game ... ";
	private static final String RECONNECTED_TO_GAME = "Reconnected to your game ... ";
	
	private static boolean gameInProgress = false;
	
	private static boolean isMyTurn = false;
	private static Timer connectionTimer; 

	private boolean disconnected =  false;
	private String gameCode;
	private int gameId;
	
    @FXML
    public Board board;
    
    @FXML
    public Label infoLabel;
    
    @FXML
    public TextField gameIdField;
    
    @FXML
    public TextField gameCodeField;
    
    @FXML
    public Button surrenderButton;
    
    @FXML
    public Button exitToMainMenuButton;
    
    @FXML
    public Label conInfoLabel;
    
    @FXML
    public VBox conVbox;
	
    @FXML
    public void surrender() throws IOException {
    	
    	endGame();
    	Request request = new Request(Client.SURRENDER);
    	Client.getConnection().send(request);
    	
    }
    
    @FXML
    public void exitToMainMenuGame() throws IOException {
    	
    	connectionTimer.cancel();
    	
    	if (gameInProgress) {
	    	Request request = new Request(Client.END_GAME);
	    	Client.getConnection().send(request);
    	}
    	App.mainMenu();
    	
    }
    
    
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		refreshGamePropertieslabels();
		
		gameInProgress = true;
		infoLabel.setText("OPONENT TURN");
		
		conInfoLabel.setText(CONNECTED);
		conVbox.setId("connection-vbox-connected");
		
		connectionTimer = new Timer();
		
		connectionTimer.schedule(new TimerTask() {
			
			  @Override
			  public void run() {
				  Client client = Client.getConnection();
				  client.testConnection();
			  }
			  
			}, 0, 1000);	
		
	}
	
	public void refreshGamePropertieslabels() {
		gameIdField.setText(String.valueOf(gameId));
		gameCodeField.setText(gameCode);
	}
	
	private void endGame() {
		surrenderButton.setDisable(true);
		gameInProgress = false;
		isMyTurn = false;
		connectionTimer.cancel();	
	}
	
	public void win(String reason) {
		
		endGame();
		
		if (reason.equalsIgnoreCase("surrender")) {
			infoLabel.setText("YOU ARE WINNER - OPONENT SURRENDERED");
		}
		else if (reason.equalsIgnoreCase("timeout")) {
			infoLabel.setText("YOU ARE WINNER! OPONENT TIMEOUT");
		}
		else  {
			infoLabel.setText("YOU ARE WINNER! :)");
		}	
	}
	
	public void lose(String reason) {
		endGame();
		
		if (reason.equalsIgnoreCase("surrender")) {
			infoLabel.setText("SURRENDER - LOST :(");
		}
		else if (reason.equalsIgnoreCase("timeout")) {
			infoLabel.setText("TIME OUT - LOST :(");
		}
		else  {
			infoLabel.setText("YOU LOST THE GAME :(");
		}
	}
	
	public void draw() {
		gameInProgress = false;
		isMyTurn = false;
		connectionTimer.cancel();
		infoLabel.setText("GAME ENDED WITH DRAW");
	}
	
	public void reveal(int row, int column) {
		
		if (isMyTurn) {
			
			Request request = new Request(Client.REVEAL);
			request.addDataSeg(String.valueOf(row));
			request.addDataSeg(String.valueOf(column));
			
			isMyTurn = false;
			infoLabel.setText("OPONENT TURN");
			
			Client.getConnection().send(request);
		}
	}
	
	public void solve(int row, int column, int result) {
		this.board.getCell(row, column).hit(result);
	}
	
	public void endGameSolve(int row, int column, int result) {
		this.board.getCell(row, column).endGameReveal(result);
	}
	
	public void setMyTurn() {
		isMyTurn = true;
		infoLabel.setText("YOUR TURN");
	}
	
	public void endGameReveal() {
		for (int i = 0; i < board.getSize(); i++) {
			for (int j = 0; j < board.getSize(); j++) {
				board.getCell(i, j).endGameCheck();
			}
		}
		
	}
	
	public String getGameCode() {
		return gameCode;
	}

	public void setGameCode(String gameCode) {
		this.gameCode = gameCode;
	}

	public int getGameId() {
		return gameId;
	}

	public void setGameId(int gameId) {
		this.gameId = gameId;
	}

	@Override
	public void goToDisconnectPage() {
		connectionTimer.cancel();
		App.disconnected();
	}
	
	@Override
	public void disconnected() {
		
		disconnected = true;
		
		Client.getConnection().disconnect();
		
		reconnecting();
		
		if (gameInProgress) {
			Client client = Client.getConnection();
			Thread clientThread = new Thread(client);
			clientThread.setDaemon(true);
			clientThread.start();
		}
		else {
			goToDisconnectPage();
		}
	}
	
	public void reconnectToGame() {
		
		conInfoLabel.setText(RECONNECT_TO_GAME);
		conVbox.setId("connection-vbox-reconnecting");
		Request request = new Request(Client.RECONNECT);
		request.addDataSeg(String.valueOf(gameId));
		request.addDataSeg(String.valueOf(gameCode));
		
		Client.getConnection().send(request);	
	}
	
	public void reconnectedToGame() {
		refreshGamePropertieslabels();
		conInfoLabel.setText(RECONNECT_TO_GAME);
		conVbox.setId("connection-vbox-connected");
	}
	
	@Override
	public void connected() {
		
		conInfoLabel.setText(CONNECTED);
		conVbox.setId("connection-vbox-connected");
		
	}
	
	@Override
	public void responseTimeout() {
		disconnected();
	}
	
	@Override
	public void reconnecting() {
		conVbox.setId("connection-vbox-reconnecting");
		conInfoLabel.setText(CONNECTING);
	}
	
	@Override
	public void corruptedMessages() {
		//TODO
	}
	
	public void reconRefused() {
		connectionTimer.cancel();
		App.recRefused();
	}
	
	@Override
	public void requestRefused() {
		disconnected();
	}
	
	@Override
	public void reconnected() {
		connected();
		reconnectToGame();
	}

}