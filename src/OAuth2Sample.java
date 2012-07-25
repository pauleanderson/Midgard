

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
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Builder;
import com.google.api.services.drive.model.File;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Tokeninfo;
import com.google.api.services.oauth2.model.Userinfo;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.client.googleapis.media.MediaHttpDownloader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
* Command-line sample for the Google OAuth2 API described at <a
* href="http://code.google.com/apis/accounts/docs/OAuth2Login.html">Using OAuth 2.0 for Login
* (Experimental)</a>.
*
* @author Yaniv Inbar
*/
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

 public static void main(String[] args) {
   try {
     try {
       // get file id
       String fileID = args[0];
    	 
       // authorization
       Credential credential =
           OAuth2Native.authorize(HTTP_TRANSPORT, JSON_FACTORY, new LocalServerReceiver(), SCOPES);
       // set up global Oauth2 instance
       oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(
           "Google-OAuth2Sample/1.0").build();
       // run commands
       tokenInfo(credential.getAccessToken());
       userInfo();
       
       Drive service = new Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).build();
       File fileToDownload = service.files().get(fileID).execute();
       String name = fileToDownload.getTitle().split("\\.")[0];
       GenericUrl u = new GenericUrl(fileToDownload.getDownloadUrl());
	   Get request = service.files().get(fileToDownload.getId());
	   FileOutputStream bos = new FileOutputStream("../"+name+".dat");
	   MediaHttpDownloader mhd = request.getMediaHttpDownloader();
	   mhd.setChunkSize(10 * 0x100000);
	   mhd.download(u, bos);
	   bos.close();
		
       // success!
       return;
     } catch (IOException e) {
       System.err.println(e.getMessage());
     }
   } catch (Throwable t) {
     t.printStackTrace();
   }
   System.exit(1);
 }

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

 static void header(String name) {
   System.out.println();
   System.out.println("================== " + name + " ==================");
   System.out.println();
 }
}