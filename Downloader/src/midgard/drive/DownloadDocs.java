package midgard.drive;

import com.google.gdata.util.*;

import java.io.IOException;
import java.net.*;

public class DownloadDocs {

	public static String CLIENT_ID = "1078037152741-uolbuufkamgdm0uudprnmv5laiga5ftc.apps.googleusercontent.com";
	
	public static String CLIENT_SECRET = "7w21kDWnsiXqyGkK3QptJR8T";
	
	public static String SCOPE = "https://docs.google.com/feeds/ https://docs.googleusercontent.com/ https://spreadsheets.google.com/feeds/";
	
	public static String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	
	public static void main(String[] args)
			throws AuthenticationException, MalformedURLException, IOException, ServiceException {
		
		DownloadDocs dd = new DownloadDocs();
		
		System.out.println("Hello!");

	}

}
