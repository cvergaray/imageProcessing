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
import java.util.Iterator;
import java.util.List;
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
    static Deskewer DesQ = new Deskewer("skewedImages/Courier Sample B.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Courier Sample C.png");

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {
      
      
      BufferedImage image = DesQ.getImage();
      
      image = Despeckler.threshold(image, .55);

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

      CharacterExtractor.learnFont("COURIER.ttf", "Monospace");
      
      
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
      expected = "This is a section of sample text. Please detect it,\nalgorithm. I need you to work.";
      int length = interpreted.length() < expected.length() ? interpreted.length() : expected.length();
      for(int i = 0; i < length; i++)
      {
         correct += (expected.charAt(i) == interpreted.charAt(i)) ? 1 : 0;
      }
      
      System.out.println("Number of correct characters: " + correct + " / " + length);
      System.out.println("Percent correct: " + Math.round((double) correct / length * 100.0) + "%");
              
//      CharacterExtractor.learnFont("Pretendo.ttf", "I Pretend");
   }

}
