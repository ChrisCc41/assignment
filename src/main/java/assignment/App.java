package assignment;

import java.io.IOException;
import java.security.GeneralSecurityException;

/* class to demonstarte use of Drive files list API */
public class App {
  public static void main(String... args) throws IOException, GeneralSecurityException{
    GoogleDrive google = new GoogleDrive();
    TextHandler imageToText = new TextHandler();
    String imagePath = "src/resources/image";
    String docPath = "src/resources/files";
    /*To Upload Image*/
    google.upload(imagePath);

    /*To Convert Image to Text with Blue Font if Contains 'o'*/
    imageToText.analyzeImageText(imagePath);

    /*To Upload Document*/
    google.upload(docPath);
  }
}