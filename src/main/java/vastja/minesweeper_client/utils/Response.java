package vastja.minesweeper_client.utils;

import java.lang.StringBuilder;
import java.text.ParseException;

public class Response {
	
	public final int id;
	public final char reqId;
	public final String message;
	
	public Response(byte[] message) throws ParseException {
		
		if (message.length > 2) {
			this.id = BufferParser.getId(message[0], message[1]);
			this.reqId = (char) message[2];
		}
		else {
			throw new ParseException("Invalid message format", 0);
		}
		
		StringBuilder sb = new StringBuilder();
		for (int i = 3; i < message.length; i++) {
			sb.append((char) (message[i] & 0xFF));
		}
		this.message = sb.toString();
	}
	
	
}
