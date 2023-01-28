package assignment;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;

public class GoogleDrive {
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES =
        Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "src/resources/configs/client_secret.json";

    /*Get Credentials to Connect with Google Services */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
        throws IOException {
      // Load client secrets.
      InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
      if (in == null) {
        throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
      }
      GoogleClientSecrets clientSecrets =
          GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
  
      // Build flow and trigger user authorization request.
      GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
          HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
          .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
          .setAccessType("offline")
          .build();
      LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
      Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
      //returns an authorized Credential object.
      return credential;
    }
  
    public static void upload(String pathName)  throws IOException, GeneralSecurityException{
      
      final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      java.io.File folder = new java.io.File(pathName);
      java.io.File[] listOfFiles = folder.listFiles();
      File fileMetadata = new File();
      fileMetadata.setParents(Collections.singletonList("14KWSTeRGUFE6sV2DCtTTo43mDbCOm70j"));
      Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
        .setApplicationName(APPLICATION_NAME)
        .build();

      /*Upload all the files inside the directory */
      for (int i = 0; i < listOfFiles.length; i++) {
        fileMetadata.setName(listOfFiles[i].getName());
        String filePath = listOfFiles[i].getPath();
        Path path = Paths.get(filePath);
        FileContent mediaContent = new FileContent(java.nio.file.Files.probeContentType(path), listOfFiles[i]);
        try {
          File file = service.files().create(fileMetadata, mediaContent)
              .setFields("id")
              .execute();
          System.out.println("File ID: " + file.getId());
        } catch (GoogleJsonResponseException e) {
          System.err.println("Unable to upload file: " + e.getDetails());
          throw e;
        }
      }
    }  
}
