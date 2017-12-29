package vastja.minesweeper_client.utils;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class BufferParser {
	
	public static int badFormatMessageCount = 0;
	
	public static int getId(byte f, byte s) {
		
		int id = (f & 0xFF) << 8;
		id = id | (s & 0xFF);
		return id;
		
	}
	
	
	public static List<Response> getResponses(byte buffer[]) {
		
		badFormatMessageCount = 0;
		
		List<Response> responses = new ArrayList<Response>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
	    boolean escape = false;
	    boolean start = false;
	    
	    for (int i = 0; i < buffer.length; i++) {
	        if (escape) {
	            escape = false;
	            if (start) {
	                baos.write(buffer[i]);
	            }     
	        }
	        else if (buffer[i] == Client.ESCAPE_CHAR) {
	            escape = true;
	            if (start) {
	            	baos.write(buffer[i]);
	            }
	        }
	        else if (buffer[i] == Client.STX) {
	            start = true;
	            baos = new ByteArrayOutputStream();
	        }
	        else if (buffer[i] == Client.ETX) {
	            start = false;
	            try {
					Response response = new Response(baos.toByteArray());
					responses.add(response);
				} catch (ParseException e) {
					badFormatMessageCount++;
				}
	        } 
	        else {
	            if (start) {
	            	baos.write(buffer[i]);
	            }
	        }
	        buffer[i] = 0;
	    }
		
		return responses;
		
	}

}
