package assignment;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class TextHandler {
  /*To Check whether String contains Chinese Character*/
  public static boolean isCJK(String str){
    int length = str.length();
    for (int i = 0; i < length; i++){
        char ch = str.charAt(i);
        Character.UnicodeBlock block = Character.UnicodeBlock.of(ch);
        if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(block)|| 
            Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS.equals(block)|| 
            Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A.equals(block)){
            return true;
        }
    }
    return false;
  }

  /*To Detect String from Image */
  public static String detectText(String filePath) throws IOException {
    /* Builds the image annotation request */
    List<AnnotateImageRequest> requests = new ArrayList<>();
    List <String> texts = new ArrayList<>();
    ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));
    Image img = Image.newBuilder().setContent(imgBytes).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    /*Performs Label Detection in the Image */
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          System.out.format("Error: %s%n", res.getError().getMessage());
        }

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
          texts.add(annotation.getDescription());
        }
      }
    }
    /*Some Data Pre Processing */
    String words = texts.toString();
    words = words.replaceAll("[^a-zA-Z0-9\\p{script=Han}]", " ");
    words = words.replaceAll( "[ ]+"," ");
    return words;
  }

  /*Create Word File */
  public static void createWordFile(String texts, String fileName){
    XWPFDocument doc = new XWPFDocument();
    XWPFParagraph para = doc.createParagraph();
    String[] words = texts.split(" ");
    for (String word : words) {
      XWPFRun run = para.createRun();
      run.setText(word+" ");
      /*Change the color for words containt 'o' */
      if (word.contains("o")) {
          run.setColor("0000FF");
      }
    }
    try {
      FileOutputStream out = new FileOutputStream("src/resources/files/"+fileName+".docx");
      doc.write(out);
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /*Load Image to Detect the Text */
  public static void analyzeImageText (String pathName) throws IOException{
    java.io.File folder = new java.io.File(pathName);
    java.io.File[] listOfFiles = folder.listFiles();
    String englishText ="";
    String chineseText = "";
    for (int i = 0; i < listOfFiles.length; i++) {
      String filePath = listOfFiles[i].getPath();
      String text = detectText(filePath);
        if(isCJK(text)==true){
          chineseText+=text;
        }else {
          englishText+=text;
        }
    }
    createWordFile(englishText, "English Text");
    createWordFile(chineseText, "Chinese Text");
  }
}