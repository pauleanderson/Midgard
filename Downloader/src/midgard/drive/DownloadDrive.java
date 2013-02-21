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

package midgard.drive;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.common.io.Files;
import com.google.api.services.drive.Drive.Builder;
import com.google.api.services.drive.model.File;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yaniv Inbar
 */
public class DownloadDrive {

  /** E-mail address of the service account. */
  private static final String SERVICE_ACCOUNT_EMAIL = "1078037152741@developer.gserviceaccount.com";

  /** Global instance of the HTTP transport. */
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  protected static final HttpTransport TRANSPORT = new NetHttpTransport();

  /**
   * Scopes for which to request access from the user.
   */
  public static final List<String> SCOPES = Arrays.asList(
      // Required to access and manipulate files.
      "https://www.googleapis.com/auth/drive.file",
      // Required to identify the user in our data store.
      "https://www.googleapis.com/auth/userinfo.email",
      "https://www.googleapis.com/auth/userinfo.profile");
  
  
  public static void main(String[] args) {
    try {
      try {
        // check for valid setup

        String p12Content = Files.readFirstLine(new java.io.File("key.p12"), Charset.defaultCharset());
        // service account credential (uncomment setServiceAccountUser for domain-wide delegation)
        GoogleCredential credential = new GoogleCredential.Builder().setTransport(HTTP_TRANSPORT)
            .setJsonFactory(JSON_FACTORY)
            .setServiceAccountId(SERVICE_ACCOUNT_EMAIL)
            .setServiceAccountScopes(SCOPES)
            .setServiceAccountPrivateKeyFromP12File(new java.io.File("key.p12"))
            .build();
        Drive service = new Builder(TRANSPORT, JSON_FACTORY, credential).build();
        System.out.println("Before");
        File file = service.files().get("0B_4L9UB-A6C3dkJveW04MnJHQzg").execute();
        GenericUrl u = new GenericUrl(file.getDownloadUrl());
        Get request = service.files().get(file.getId());
        System.out.println(file.getTitle());
        MediaHttpDownloader mhd = request.getMediaHttpDownloader();
        mhd.setChunkSize(10*0x100000);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("test.txt"));
        System.out.println(bos.toString());
        System.out.println(u.toString());
        mhd.download(u, bos);
        //HttpResponse response = service.getRequestFactory().buildGetRequest(u).execute();
        //response.getContent();
        
        System.out.println("After");

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
  }

