

/*
* Copyright (c) 2010 Google Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License
* is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
* or implied. See the License for the specific language governing permissions and limitations under
* the License.
*/

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Builder;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.client.googleapis.media.MediaHttpDownloader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
* Command-line sample for the Google OAuth2 API described at <a
* href="http://code.google.com/apis/accounts/docs/OAuth2Login.html">Using OAuth 2.0 for Login
* (Experimental)</a>.
*
* @author Yaniv Inbar
*/
@SuppressWarnings("unused")
public class OAuth2Sample {

 /** Global instance of the HTTP transport. */
 private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

 /** Global instance of the JSON factory. */
 private static final JsonFactory JSON_FACTORY = new JacksonFactory();

 /** OAuth 2.0 scopes. */
 private static final List<String> SCOPES = Arrays.asList(
     "https://www.googleapis.com/auth/userinfo.profile",
     "https://www.googleapis.com/auth/userinfo.email",
     "https://www.googleapis.com/auth/drive"
     );

 private static Oauth2 oauth2;
 private static Drive service;
 private static Permission perm = new Permission().setRole("writer").setType("user");

 public static void main(String[] args) {
	 // args[0] = command (insert or get)
	 // args[1] = file(s) to interact with
	 // args[2] = received email from galaxy
	 // args[3] = format of the file
	 // args[4] = name of workflow
	 // args[5] = location (local or remote)
	 // args[6] = location of credential file
	 String command = args[0];
	 String[] filePairs = args[1].split("\\,");
	 String raw_email = args[2];
	 String format = args[3];
	 String workflow = args[4];
	 String location = args[5];
	 String credPath = args[6];
	 
	 String email = null;
	 if (raw_email.contains("__at__")) {
		 String[] email_parts = raw_email.split("\\__at__");
	 	 email = email_parts[0] + "@" + email_parts[1];
	 } else {
		 email = raw_email;
	 }
	 
	 if (format.equals("-")) {
		 format = null;
	 }
	 
	// authorization
	 java.io.File cred = new java.io.File(credPath);
	 
    Credential credential = null;
	try {
		credential = OAuth2Native.authorize(HTTP_TRANSPORT, JSON_FACTORY, new LocalServerReceiver(), SCOPES, cred);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
     // set up global Oauth2 instance
     oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
         "Google-OAuth2Sample/1.0").build();
     // run commands
     service = new Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
	 
     perm.setValue(email);
     String parentFolder = determineParent(email, workflow);
	 for (String fileID : filePairs) {
		 String[] fileName = fileID.split("\\:");
		 
		 String path = new String(fileName[0]);
		 
		 String[] s = fileName[0].split("/");
		 String file = s[s.length-1];
		 String id = null;
		 
		 if (!file.equals("None")) {
			 id = uploadDriveFile(fileName[1], "No description",
					 parentFolder, "text/plain", file, path, email);
		 }
		 // System.out.println(id);
	 }
 }
 
public static String uploadDriveFile(String title, String description,
	      String parentId, String mimeType, String filename, String path, String shareTo) {
		
	    // File's metadata.
	    File body = new File();
	    body.setTitle(title);
	    body.setDescription(description);
	    body.setMimeType(mimeType);
	    
	    body.setParents(Arrays.asList(new ParentReference().setId(parentId)));
	    // File's content.
	    java.io.File fileContent = new java.io.File(path);
	    FileContent mediaContent = new FileContent(mimeType, fileContent);
	    try {
	      File file = service.files().insert(body, mediaContent).execute();
	      service.permissions().insert(file.getId(), perm).execute();

	      // Uncomment the following line to print the File ID.
	      // System.out.println("File ID: " + file.getId());
	      
	      return file.getId();
	    } catch (IOException e) {
	      System.out.println("An error occured: " + e);
	      return null;
	    }
	  }

private static String determineParent(String email, String workflow) {
	String root = "0B7Jfx3RRVE5YenZEY1N5cE5pRms";
	String parentFolder = null;
	
	String level = root;
	String parent = root;
	boolean lowest = false;
	
	// Checks for user
	level = folderContains(email, level);
	if (level != null) {
		
		DateFormat today = new SimpleDateFormat("E, MM/dd/yyyy");
		Date now = new Date();
		String nowStr = today.format(now);
		
		// Checks for today's date
		parent = level;
		level = folderContains(nowStr, level);
		if (level != null) {
			
			// Checks for a workflowID folder
			parent = level;
			level = folderContains(workflow, level);
			//System.out.println("level: "+level);
			if (level != null) {
				
				// Finds the highest folder number; add 1 and creates it.
				try {
					File child = null;
					int lastRun = 0;
					int tmp = lastRun;
					ChildList children = service.children().list(level).execute();
					for (ChildReference element : children.getItems()) {
						child = service.files().get(element.getId()).execute();
						try {
							tmp = Integer.parseInt(child.getTitle());
							if (tmp>lastRun) {
								lastRun = tmp;
							}
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					lastRun += 1;
					String next = new Integer(lastRun).toString();
					//System.out.println("level: "+level);
					//System.out.println("next: "+next);
					parentFolder = createFolderWithParentAndTitle(level, next);
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				} finally {
					lowest = true;
				}
				
			} else {
				parentFolder = createFolderWithParentAndTitle(parent, workflow);
			}
		} else {
			parentFolder = createFolderWithParentAndTitle(parent, nowStr);
		}
	} else {
		parentFolder = createFolderWithParentAndTitle(parent, email);
	}
	
	try {
		File file = service.files().get(parentFolder).execute();
		service.permissions().insert(file.getId(), perm).execute();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	if (!lowest)
		parentFolder = determineParent(email, workflow);
	
	return parentFolder;
}

private static String folderContains(String filename, String folderID) {
	
	// Returns the ID of a specified filename in a folder or null if it does not exist
	
	File child = null;
	ChildList children = null;
	try {
		children = service.children().list(folderID).execute(); 
		for (ChildReference element : children.getItems()) {
			child = service.files().get(element.getId()).execute();
			if (child.getTitle().equals(filename)) {
				break;
			}
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		//System.out.println("children: "+children.toPrettyString());
		//System.out.println("child: "+child.toPrettyString());
	}
	
	if (child != null && child.getTitle().equals(filename)) {
		return child.getId();
	}
	
	return null;
}

private static Permission insertPermission(Drive service, String fileId, 
		String value, String type, String role) {
	
    Permission newPermission = new Permission();

    newPermission.setValue(value);
    newPermission.setType(type);
    newPermission.setRole(role);
    try {
      return service.permissions().insert(fileId, newPermission).execute();
    } catch (IOException e) {
      System.out.println("An error occurred: " + e);
    }
    return null;
}

private static String createFolderWithParentAndTitle(String parentID, String title) {
	String folderMIME = "application/vnd.google-apps.folder";

	File newFolder = new File()
	.setTitle(title)
	.setParents(
			Arrays.asList(new ParentReference()
			.setId(parentID)))
			.setMimeType(folderMIME);
	
	File returned = null;
	try {
		returned = service.files().insert(newFolder).execute();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	if (returned!=null)
		return returned.getId();
	
	return null;
}
/*
private static void tokenInfo(String accessToken) throws IOException {
   header("Validating a token");
   Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(accessToken).execute();
   System.out.println(tokeninfo.toPrettyString());
   if (!tokeninfo.getAudience()
       .equals(OAuth2Native.getClientSecrets().getDetails().getClientId())) {
     System.err.println("ERROR: audience does not match our client ID!");
   }
 }

private static void userInfo() throws IOException {
   header("Obtaining User Profile Information");
   Userinfo userinfo = oauth2.userinfo().get().execute();
   System.out.println(userinfo.toPrettyString());
 }
*/
static void print(String s) {
   System.out.println("================== " + s + " ==================");
   System.out.println();
}

static void header(String s) {
	System.out.println();
	System.out.println("================== " + s + " ==================");
	System.out.println();
}
}