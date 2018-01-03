package vastja.minesweeper_client.utils;

public class ComunicationManager {

	private ComunicationItem items[];
	private static final int MAX_TIME_TO_RESPONSE = 2000;
	
	public ComunicationManager() {
		items = new ComunicationItem[6];
	}
	
	public void reset() {
		items = new ComunicationItem[6];
	}
	
	public void addRequest(Request request) {	
		
		int i = findRequestClass(request.reqId);
		if (i == -1) {
			return;
		}
		else {
			items[i] = new ComunicationItem(request);
		}
	}
	
	public void update(Response response) {
		
		int i = findResponseClass(response.reqId);
		if (i == -1) {
			return;
		}
		else {
			items[i] = null;
		}
	}
	
	public boolean check() {
		
		for (int i = 0; i < items.length; i++) {
			if (items[i] != null && System.currentTimeMillis() - items[i].sentTime >= MAX_TIME_TO_RESPONSE) {
				if (items[i].resent) {
					System.out.println(i + " request timeout");
					return false;
				}
				else {
					Request req = items[i].request;
					items[i] = null;
					System.err.println("REQ + [" + (int) req.reqId + "] RESENT");
					Client.getConnection().send(req);
					if (items[i] != null) {
						items[i].resent = true;
					}
				}
			}
		}
		
		return true;
		
	}

	public boolean canBeSend(Request request) {
		
		int i = findRequestClass(request.reqId);
		if (i == -1) {
			return true;
		}
		else if (items[i] == null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private int findRequestClass(char reqId) {
		
		switch (reqId) {
			case Client.START_GAME:
				return 0;
			case Client.REVEAL:
				return 1;
			case Client.SURRENDER:
			case Client.END_GAME:
				return 2;
			case Client.RECONNECT:
				return 3;
			case Client.ALIVE:
				return 4;
			case Client.SEND_ID:
				return 5;
			default:
				return -1;
		}
	}
	
	private int findResponseClass(char reqId) {
		
		switch (reqId) {
			case Client.START_GAME_REFUSED:
			case Client.START_GAME_ACCEPTED:
				return 0;
			case Client.REVEAL:
			case Client.REVEAL_REFUSED:
				return 1;
			case Client.SURRENDER_REFUSED:
			case Client.SURRENDER_LOSE:
			case Client.END_GAME_REFUSED:
			case Client.LOSE:
			case Client.END_GAME:
				return 2;
			case Client.RECONNECT:
			case Client.RECONNECT_REFUSED:
				return 3;
			case Client.ALIVE:
				return 4;
			case Client.SEND_ID:
				return 5;
			default:
				return -1;
		}
	}
}

class ComunicationItem {
	
	public final Request request;
	
	public boolean resent;
	public long sentTime;
	
	public ComunicationItem(Request request) {
		this.request = request;
		resent = false;
		sentTime = System.currentTimeMillis();
	}

}

