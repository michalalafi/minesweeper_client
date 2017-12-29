package vastja.minesweeper_client.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import vastja.minesweeper_client.App;
import vastja.minesweeper_client.controllers.GameController;
import vastja.minesweeper_client.utils.Request;

public class Client implements Runnable {

	/* Start game */
	public static final char START_GAME = 4;
	public static final char START_GAME_ACCEPTED = 5;
	public static final char START_GAME_REFUSED = 6;
	/* Reveal */
	public static final char REVEAL = 7;
	public static final char REVEAL_REFUSED = 8;
	public static final char WIN = 9;
	public static final char LOSE = 10;
	public static final char DRAW = 11;
	/* Surrender */
	public static final char SURRENDER = 12;
	public static final char SURRENDER_REFUSED = 13;
	public static final char SURRENDER_WIN = 14;
	public static final char SURRENDER_LOSE = 15;
	/* Reconnect */
	public static final char RECONNECT = 16;
	public static final char RECONNECT_REFUSED = 17;
	/* End game */
	public static final char END_GAME = 18;
	public static final char END_GAME_REFUSED = 19;
	public static final char END_GAME_REVEAL = 20;
	/* Support functions */
	public static final char SEND_ID = 21;
	public static final char START_TURN = 22;
	public static final char TIMEOUT_WIN = 23;
	public static final char TIMEOUT_LOSE = 24;
	public static final char ALIVE = 25;
	public static final char MESSAGE_BAD_FORMAT = 26;
	
	public static final String D = "death";
	public static final String T = "timeout";
	public static final String S = "surrender";
	
	public static final char ETX = 3;
	public static final char STX = 2;
	public static final char ESCAPE_CHAR = '/';
	public static final char DIV_CHAR = ';';
	
	private static final int BUFFER_SIZE = 1024;
	
	private static String ipAdress;
	private static int port;
	
	private Timer connectTimer;
	
	private static int failedToConnect = 0;
	private static final int MAX_CONNECT_FAILURE_COUNT = 15;
	private static final int TRY_CONNECT_DELAY = 500;
	
	private static int failedToSend = 0;
	private static final int MAX_SEND_FAILURE_COUNT = 5;
	
	private Timer comunicationTimer;
	private static final int CON_TIMER_INTERVAL = 2000;
	private ComunicationManager comunicationManager;
	
	private static Client connection = null;
	private OutputStream os ;
	private InputStream is;
	private Socket socket;
	
	private int corruptedMessagesCount = 0;
	private static final int MAX_CORRUPTED_MESSAGES_IN_ROW = 5;
	private int id;
	private static boolean read;
	private static boolean wasDisconnected;
	
	public Client() {
		comunicationManager = new ComunicationManager();
		wasDisconnected = false;
	}
	
	public static Client getConnection() {
		
		if (connection ==  null) {
			connection = new Client();
		}
		
		return connection;
		
		
	}
	
	public void disconnect() {
		
		wasDisconnected = true;
		
		if (comunicationTimer != null) {
			comunicationTimer.cancel();
			comunicationTimer = null;
		}
		
		if (connectTimer != null) {
			connectTimer.cancel();
			connectTimer = null;
		}
		
		read = false;
		
		if (socket != null  && !socket.isClosed()) {
			try {
				socket.close();
			} catch (IOException e) {
				System.err.println("Closing socket failed.");
			}
		}
		
		socket = null;
		is = null;
		os = null;
		
	}
	
	private void connect() {
		
		
		if (isConnected()) {
			return;
		}
		
		try {
			socket = new Socket(ipAdress, port);
			
			InetAddress adresa = socket.getInetAddress();
			System.out.println("Connecting to : " + adresa.getHostAddress() + " ["+ adresa.getHostName() + "]");
			  
			this.os = socket.getOutputStream();
			this.is = socket.getInputStream();
			
			failedToConnect = 0;
		
			if (wasDisconnected) {
				Platform.runLater(() -> App.getController().reconnected());
			}
			else {
				Platform.runLater(() -> App.getController().connected());
			}
	
			listen();
			
		}
		catch (IOException e) {
			
			wasDisconnected = true;
			Platform.runLater(() -> App.getController().reconnecting());
			
			failedToConnect++;
			if (failedToConnect > MAX_CONNECT_FAILURE_COUNT) {
				Platform.runLater(() -> App.getController().goToDisconnectPage());
			}
			else {
				
				if (connectTimer == null) {
					connectTimer = new Timer();
				}
				
				connectTimer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						Platform.runLater(() -> App.getController().reconnecting());
						connect();		
					}
					
				}, TRY_CONNECT_DELAY);
				
			}
		}
		
	}
	
	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}
	
	public void testConnection() {
		send(new Request(ALIVE));
	}
	
	public void send(Request request) {
		
		if (!comunicationManager.canBeSend(request)) {
			return;
		}
		
		if (os != null) {
			
			try {
				os.write(request.getMessageToSend(id));
				comunicationManager.addRequest(request);
				failedToSend = 0;
			
				Platform.runLater(() -> App.getController().connected()); 
				
			} catch (IOException e) {
				
				wasDisconnected = true;
				Platform.runLater(() -> App.getController().reconnecting());
				
				failedToSend++;
				if (failedToSend > MAX_SEND_FAILURE_COUNT) {
					Platform.runLater(() -> App.getController().disconnected());
				}
			}
		}
	}

	@Override
	public void run() {
		
		
		disconnect();
	
		
		comunicationTimer = new Timer();
		
		comunicationTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				if(!comunicationManager.check()) {
					comunicationManager.reset();
					Platform.runLater(() -> App.getController().responseTimeout());	
				}
			}
			
		}, 0, CON_TIMER_INTERVAL);
		
		connectTimer = new Timer();
		connect();
		
	}
	
	private void listen() {
		
		byte buffer[] = new byte[BUFFER_SIZE];
		  	
		read = true;
		while (read) {		
			
			try {
				
				if (is == null) {
					continue;
				}
				
				int i = is.read(buffer);	
				
				if (i == -1) {
					wasDisconnected = true;
					socket.close();
					Platform.runLater(() -> App.getController().disconnected());
					break;
				}
				
				for (int j = 0; j < BUFFER_SIZE; j++) {
					System.out.print(buffer[j] + "-");
				}
				System.out.println();
					
				List<Response> responses = BufferParser.getResponses(buffer);
				corruptedMessagesCount += BufferParser.badFormatMessageCount;
				if (responses.size() == 0) {
					corruptedMessagesCount++;
				}
				if (corruptedMessagesCount >= MAX_CORRUPTED_MESSAGES_IN_ROW) {
					corruptedMessagesCount = 0;
					Platform.runLater(() -> App.getController().corruptedMessages());
				}
				
				for (Response response : responses) {
					comunicationManager.update(response);
					executeResponse(response);
				}
						
			} catch (IOException e) {
				Platform.runLater(() -> App.getController().reconnecting());
			}
					
		}
	}
	
	private void executeResponse(Response response) {
		
		switch (response.reqId) {
			case SEND_ID:
				executeIdResponse(response);
				break;
			case REVEAL:
				executeRevealResponse(response);
				break;
			case START_GAME:
				executeStartGameResponse(response);
				break;
			case WIN:
				executeWinResponse(response, D);
				break;
			case LOSE:
				executeLoseResponse(response, D);
				break;
			case DRAW:
				executeDrawResponse(response);
				break;
			case TIMEOUT_WIN:
				executeWinResponse(response, T);
				break;
			case TIMEOUT_LOSE:
				executeLoseResponse(response, T);
				break;
			case SURRENDER_WIN:
				executeWinResponse(response, S);
				break;
			case SURRENDER_LOSE:
				executeLoseResponse(response, S);
				break;
			case START_TURN:
				executeStartTurnResponse(response);
				break;
			case END_GAME_REVEAL:
				executeEndGameRevealResponse(response);
				break;		
			case END_GAME:
				executeEndGameResponse(response);
				break;
			case RECONNECT_REFUSED:
				executeReconnectedRefuse(response);
				break;
			case RECONNECT:
				executeReconnectToGame();
				break;
			// TODO
			case START_GAME_REFUSED:
			case REVEAL_REFUSED:
			case END_GAME_REFUSED:
			case SURRENDER_REFUSED:
				executeResponseRefused();
				break;
			case ALIVE:
			case START_GAME_ACCEPTED:
			case MESSAGE_BAD_FORMAT:
				break;
			default:
				corruptedMessagesCount++;
				System.out.println("No response with request ID: " + response.reqId);
				
		}
		
	}
	
	private void executeIdResponse(Response response) {
		id = response.id;
		System.out.println("Got id: " + response.id + " from server [Request ID: " + (int) response.reqId + "]");
	}
	
	private void executeRevealResponse(Response response) {
		String parsed[] = parseMessage(response.message);
		
		if (parsed.length == 3) {
			try {
				int i = Integer.parseInt(parsed[0]);
				int j = Integer.parseInt(parsed[1]);
				int result = Integer.parseInt(parsed[2]);
				
				Platform.runLater(() -> {
					GameController gameController = App.getGameController();
					if (gameController != null) {
						gameController.solve(i, j, result);
					}
				});
				
				System.out.println("Got REVEAL response i: " + i + " j: " + j + " number: " + result + "[Request ID: " + (int) response.reqId + "]");
				
			} catch(NumberFormatException e) {
				corruptedMessagesCount++;
				System.err.println("REVEAL message in bad format");
				return;
			}
			
		}
		else {
			corruptedMessagesCount++;
		}
		
	}
	
	private String[] parseMessage(String message) {
		
		List<String> result = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		
		boolean escape = false;
	    for (int i = 0; i < message.length(); i++) {

	        if (escape) {
	            escape = false;
	            sb.append(message.charAt(i));
	        }
	        if (message.charAt(i) == ESCAPE_CHAR) {
	            escape = true;
	        }
	        else if (message.charAt(i)  == Client.DIV_CHAR) {
	            result.add(sb.toString());
	            sb = new StringBuilder();
	        }
	        else {
	        	sb.append(message.charAt(i));
	        }
	    }
	    
	    return result.toArray(new String[result.size()]);
	}
	
	private void executeStartGameResponse(Response response) {
		String parsed[] = parseMessage(response.message);
		
		if (parsed.length == 1) {
			Platform.runLater(() -> App.game(parsed[0], id));
		}
		else {
			corruptedMessagesCount++;
		}
		
		System.out.println("Got START GAME response [Request ID: " + (int) response.reqId + "]");
	}
	
	private void executeWinResponse(Response response, String reason) {
		Platform.runLater(() -> {
			GameController gameController = App.getGameController();
			if (gameController != null) {
				gameController.endGameReveal();
				gameController.win(reason);
			}
		});
		System.out.println("Got WIN response [Request ID: " + (int) response.reqId + "]");
	}
	
	private void executeLoseResponse(Response response, String reason) {
		Platform.runLater(() -> {
			GameController gameController = App.getGameController();
			if (gameController != null) {
				gameController.endGameReveal();
				gameController.lose(reason);
			}
		});
		System.out.println("Got LOSE response [Request ID: " + (int) response.reqId + "]");
	}
	
	private void executeDrawResponse(Response response) {
		Platform.runLater(() -> {
			GameController gameController = App.getGameController();
			if (gameController != null) {
				//TODO change all flagged
				gameController.draw();
			}
		});
		System.out.println("Got DRAW response [Request ID: " + (int) response.reqId + "]");
	}
	
	private void executeStartTurnResponse(Response response) {
		
		Platform.runLater(() -> {
			GameController gameController = App.getGameController();
			if (gameController != null) {
				gameController.setMyTurn();
			}
		});
			
		
		System.out.println("Got START_TURN response [Request ID: " + (int) response.reqId + "]");
	}
	
	private void executeEndGameRevealResponse(Response response) {
		String parsed[] = parseMessage(response.message);
		
		if (parsed.length == 3) {
			try {
				int i = Integer.parseInt(parsed[0]);
				int j = Integer.parseInt(parsed[1]);
				int result = Integer.parseInt(parsed[2]);
				
				Platform.runLater(() -> {
					
					GameController gameController = App.getGameController();
					if (gameController != null) {
						gameController.endGameSolve(i, j, result);
					}
						
				});
				System.out.println("Got END_GAME_REVEAL response [Request ID: " + (int) response.reqId + "]");
			}
			catch(NumberFormatException e) {
				corruptedMessagesCount++;
				System.err.println("END_GAME_REVEAL message in bad format");
				return;
			}
		}
		else {
			corruptedMessagesCount++;
		}
	
	}
	
	private void executeEndGameResponse(Response response) {
		System.out.println("Got END_GAME response [Request ID: " + (int) response.reqId + "]");
	}
	
	
	private void executeReconnectedRefuse(Response response) {
		Platform.runLater(() -> {
			
			GameController gameController = App.getGameController();
			if (gameController != null) {
				gameController.reconRefused();
			}
				
		});
		
		System.out.println("Got RECONNECT_REFUSED response [Request ID: " + (int) response.reqId + "]");
	}
	
	private void executeReconnected(Response response) {
		Platform.runLater(() -> {
					
			GameController gameController = App.getGameController();
			if (gameController != null) {
				gameController.setGameId(id);
			}
						
		});
	}
	
	private void executeReconnectToGame() {
		Platform.runLater(() -> {
			
			GameController gameController = App.getGameController();
			if (gameController != null) {
				gameController.reconnectedToGame();
			}
						
		});
	}
	
	private void executeResponseRefused() {
		Platform.runLater(() -> App.getController().requestRefused());
	}
	
	public int getId() {
		return id;
	}

	public static String getIpAdress() {
		return ipAdress;
	}

	public static void setIpAdress(String ipAdress) {
		Client.ipAdress = ipAdress;
	}

	public static int getPort() {
		return port;
	}

	public static void setPort(int port) {
		Client.port = port;
	}
	
	
}
