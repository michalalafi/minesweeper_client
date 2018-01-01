package vastja.minesweeper_client.utils;

import java.nio.ByteBuffer;
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
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(Client.STX);
		
		char fByte = (char) (id >> 8 & 0xFF);
		char sByte = (char) (id & 0xFF);
		
		if (fByte == Client.ETX || fByte == Client.STX) {
			sb.append(Client.ESCAPE_CHAR);
		}
		sb.append(fByte);
		
		if (sByte == Client.ETX || sByte == Client.STX) {
			sb.append(Client.ESCAPE_CHAR);
		}
		sb.append(sByte);
		
		sb.append(reqId);	
		
		if (this.message != null) {
			sb.append(this.message);
		}
		
		sb.append(Client.ETX);
		
		byte message[] = sb.toString().getBytes(StandardCharsets.UTF_8);
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
