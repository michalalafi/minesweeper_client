package vastja.minesweeper_client.utils;

import java.nio.charset.StandardCharsets;

public class Request {
		
	public final char reqId;
	private String message;
	
	private static final int BUFFER_SIZE = 1024;
	
	public Request(char reqId) {
		this.reqId = reqId;
		// Because of C server
		this.message = null;
	}
	
	public byte[] getMessageToSend(int id) {
		
		byte message[];
		if (this.message == null) {
			message = new byte[5];
		}
		else {
			message = new byte[5 + this.message.length()];
		}
		
		// 16bit int
		message[0] = Client.STX;
		message[1] = (byte) (id >> 8 & 0xFF);
		message[2] = (byte) (id & 0xFF);
		message[3] = (byte) reqId;	
		message[message.length - 1] =  Client.ETX;
		
		if (this.message != null) {
			byte messageBytes[] = this.message.getBytes(StandardCharsets.UTF_8);
			System.arraycopy(messageBytes, 0, message, 4, messageBytes.length);
		}
		
		return message;
		
	}
	
	public void addDataSeg(String dataSeg) {
		
		StringBuilder sb = new StringBuilder();
		
		char ch;
		for (int i = 0; i < dataSeg.length(); i++) {
			ch = dataSeg.charAt(i);
			if (ch == '\\') {
				sb.append('\\' + ch);
			}
			else if (ch == ';') {
				sb.append('\\' + ch);
			}
			else {
				sb.append(ch);
			}			
		}
	
		sb.append(';');
		
		if (message == null) {
			message = sb.toString();
		}
		else {
			message += sb.toString();
		}
		
			
	}
	

}
