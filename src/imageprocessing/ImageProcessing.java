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

    static Deskewer DesQ = new Deskewer("skewedImages/bcnotdetected.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/p16.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/Sample25Degrees.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/Sample355Degrees.png");
   // static Deskewer DesQ = new Deskewer("skewedImages/zjd2b.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/Image_003.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/Image_005.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/text4991.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/skew.jpg");
   // static Deskewer DesQ = new Deskewer("skewedImages/pg38-39.gif");
   // static Deskewer DesQ = new Deskewer("ar3.jpg");
   // static Deskewer DesQ = new Deskewer("learnedFont.png");

   /**
    * @param args the command line arguments
    */
   public static void main(String[] args)
   {

      Double angle = -300.0;
      while (angle == -300.0 && DesQ.getThreshold() > 0)
      {
         DesQ.setThreshold(DesQ.getThreshold() - 1);
         angle = DesQ.GetHoughAngle();
      }
      if(angle == -300.0) angle = 0.0;

      System.out.println("angle == " + angle);

      BufferedImage image = DesQ.getImage();

      image = ImageRotator.rotateRad(image, angle);

      Deskewer.writeImage("MyRotated.png", image);

      image = Despeckler.threshold(image, .5);

      Deskewer.writeImage("MyDespeckled.png", image);

/*      
      List<List<ProcessedCharacter>> lines = CharacterExtractor.extractAll(image);      

      Deskewer temp;
      
      for (List<ProcessedCharacter> currentLine : lines)
      {
         for (ProcessedCharacter current : currentLine)
         {
            temp = new Deskewer(current.getImageSegment());
            temp.setThreshold(2);
            angle = temp.GetHoughAngle();
            Deskewer.writeImage("output/character" + current.getLineNum() + "-" + current.getID() + "-" + angle + ".png", current.getImageSegment());
         }
      }
*/
//      CharacterExtractor.learnFont("COURIER.ttf", "Monospace");

      CharacterExtractor.learnFont("Times New Roman.ttf", "I Pretend");

      
      System.out.println(CharacterExtractor.identifyCharacters(image));
      String expected = "!";
      for(int i = 35; i < 127; i++)
      {
         expected += (char) i;
      }
      System.out.println(expected);
      
//      CharacterExtractor.learnFont("Pretendo.ttf", "I Pretend");
   }

}
