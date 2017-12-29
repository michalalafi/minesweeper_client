package vastja.minesweeper_client.controllers;

public interface IController {
	
	public void goToDisconnectPage();
	
	public void disconnected();
	
	public void connected();
	
	public void reconnecting();
	
	public void responseTimeout();
	
	public void corruptedMessages();

	public void requestRefused();
	
	public void reconnected();
}
