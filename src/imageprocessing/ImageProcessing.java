/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing;

import imageprocessing.CharacterExtractor.*;
import imageprocessing.deskew.Deskewer;
import static imageprocessing.deskew.Deskewer.writeImage;
import imageprocessing.despeckle.Despeckler;
import imageprocessing.rotate.ImageRotator;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;



/**
 *
 * @author cave
 */
public class ImageProcessing
{
   // SimpleFileChooser fileChooser = new SimpleFileChooser();

   // static Deskewer DesQ = new Deskewer("skewedImages/bcnotdetected.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/p16.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/Sample25Degrees.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Sample355Degrees.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/zjd2b.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/Image_003.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/Image_005.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/text4991.jpg"); //This one is still unhappy
   // static Deskewer DesQ = new Deskewer("skewedImages/skew.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/pg38-39.gif"); //The black border causes problems
   // static Deskewer DesQ = new Deskewer("ar3.jpg"); //Not text, but edge detected anyway
   // static Deskewer DesQ = new Deskewer("learnedFont.png"); //Baseline super accurately detected. :D
   // static Deskewer DesQ = new Deskewer("skewedImages/Courier Sample A.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Courier Sample B.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Courier Sample C.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Courier Sample D.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Courier Sample E.png");

   // static String imageName = "Talk A-1.png";
   // static String imageName = "Talk A-2.png";
   // static String imageName = "Talk A-3.png";
   // static String imageName = "Talk A-4.png";
   // static String imageName = "Talk A-5.png";
   // static String imageName = "Talk A-6.png";
   // static String imageName = "Talk A-7.png";
   // static String imageName = "Talk A-8.png";
   // static String imageName = "Talk A-9.png";
   // static String imageName = "Talk A-10.png";
   // static String imageName = "Talk A-11.png";
   // static String imageName = "Talk A-12.png";
   // static String imageName = "alphabet.png";
   // static String imageName = "alphabet2.png";
   // static String imageName = "0123456789.png";

   // static String imageName = "fox.png";
    static String imageName = "lorem.png";

    
// static Deskewer DesQ = new Deskewer("skewedImages/Talk A-2.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Talk A-3.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Talk A-4.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Talk A-5.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Talk A-6.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Talk A-7.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Talk A-8.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Talk A-9.png");
   // static String imageName = "skewedImages/Talk A-10.png";
   // static Deskewer DesQ = new Deskewer("skewedImages/Talk A-11.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Talk A-12.png");

    static String imageFolder = "skewedImages/";
    static String textFolder = "expectedText/";
    
    /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      
      Deskewer DesQ = new Deskewer(imageFolder + imageName);
      
      BufferedImage image = DesQ.getImage();
      
      image = Despeckler.threshold(image, .65);

      Deskewer.writeImage("MyDespeckled.png", image);
      
      DesQ.initWithImage(image);

      Double angle = -300.0;
      //while (angle == -300.0 && DesQ.getThreshold() > 0)
      //{
      //   DesQ.setThreshold(DesQ.getThreshold() - 1);
         angle = DesQ.GetHoughAngle();
      //}
      if(angle == -300.0) angle = 0.0;

      System.out.println("angle == " + angle);

      image = ImageRotator.rotateRad(image, -angle);

      Deskewer.writeImage("MyRotated.png", image);

      image = Despeckler.threshold(image, .5);

      CharacterExtractor.learnFont("COURIER.ttf", "Monospace", angle);
      //CharacterExtractor.learnFont("OCRA.ttf", "Monospace");
      
      
      /*
      List<List<ProcessedCharacter>> lines = CharacterExtractor.currentLibrary.characters;    

//      Deskewer temp;
      
      for (List<ProcessedCharacter> currentLine : lines)
      {
         for (ProcessedCharacter current : currentLine)
         {
            Deskewer.writeImage("Libraries/learned/LC" + "-" + current.getID() + ".png", current.getImageSegment());
         }
      }

*/
//      CharacterExtractor.learnFont("Times New Roman.ttf", "I Pretend");

      String interpreted = CharacterExtractor.identifyCharacters(image);
      
      System.out.println(interpreted);
      String expected = "!";
      for(int i = 35; i < 127; i++)
      {
         expected += (char) i;
      }
      System.out.println(expected);

      int correct = 0;
      //expected = "This is a section of sample text. Please detect it, \nalgorithm. I need you to work.";
      expected = ""; 
       try
       {
          Scanner in = new Scanner(new FileReader(textFolder + imageName + ".txt"));
          while(in.hasNextLine())
             expected += in.nextLine() + "\n";
       } catch (FileNotFoundException ex)
       {
          Logger.getLogger(ImageProcessing.class.getName()).log(Level.SEVERE, null, ex);
       }
      
      int length = interpreted.length() < expected.length() ? interpreted.length() : expected.length();
      int i, e;
      i = e = 0;
      while(i < length && e < length)
      {
         String debugString = "" + expected.charAt(e) + " == " + interpreted.charAt(i) + " ? " + 
                 ((expected.charAt(e) == interpreted.charAt(i)) ? "TRUE" : "FALSE");
         System.out.println(unEscapeString(debugString));
         correct += (expected.charAt(e) == interpreted.charAt(i)) ? 1 : 0;
 
         //Reset at each line, so if one reached the EOL first, then wait for the other to catch up.
         //If they are the same, both can move ahead.
         if (!((expected.charAt(e) == '\n') ^ (interpreted.charAt(i) == '\n'))) {
            i++;
            e++;
         } else if (expected.charAt(e) == '\n') {
            //But if the expected hit EOL, then keep going through interpreted.
            i++;
         } else {
            e++;
         }         
      }
      
      System.out.println("Number of correct characters: " + correct + " / " + length);
      System.out.println("Percent correct: " + Math.round((double) correct / length * 100.0) + "%");
              
//      CharacterExtractor.learnFont("Pretendo.ttf", "I Pretend");
   }

   //As found on StackOverflow:
   //http://stackoverflow.com/questions/7888004/how-do-i-print-escape-characters-in-java
   public static String unEscapeString(String s){
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<s.length(); i++)
        switch (s.charAt(i)){
            case '\n': sb.append("\\n"); break;
            case '\t': sb.append("\\t"); break;
            // ... rest of escape characters
            default: sb.append(s.charAt(i));
        }
    return sb.toString();
}
}
