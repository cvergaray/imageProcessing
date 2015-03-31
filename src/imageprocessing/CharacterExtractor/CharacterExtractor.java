/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.CharacterExtractor;

import imageprocessing.deskew.Deskewer;
import imageprocessing.rotate.ImageRotator;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * CharacterExtractor. A class dedicated to processing images and finding
 * individual characters. This is generally accomplished by using the vertical
 * and horizontal projections, or histograms. That is to say, counts of how many
 * pixels are dark within rows and columns of pixels.
 *
 * @author Chris Vergaray
 */
public class CharacterExtractor
{

   public static int characterID = 0;
   public static int lineID = 0;
   private static FontLibrary currentLibrary;

   public static int[] getVerticalProjections(BufferedImage image)
   {
      int[] projections;
      projections = new int[image.getWidth()];

      for (int x = 0; x < image.getWidth(); x++)
      {
         for (int y = 0; y < image.getHeight(); y++)
         {
            projections[x] += (Deskewer.isDark(new Color(image.getRGB(x, y))) ? 1 : 0);
         }
      }

      return projections;
   }

   /**
    * A function to get the projections across the rows. A projection is the sum
    * of all the dark characters across a single row. The resultant array will
    * be the same size as the height of the image. Thus, the resultant array
    * will contain the
    *
    * @param image
    * @return
    */
   public static int[] getHorizontalProjections(BufferedImage image)
   {
      int[] projections;
      projections = new int[image.getHeight()];

      for (int y = 0; y < image.getHeight(); y++)
      {
         for (int x = 0; x < image.getWidth(); x++)
         {
            projections[y] += (Deskewer.isDark(new Color(image.getRGB(x, y))) ? 1 : 0);
         }
      }

      return projections;
   }

   private static List<ProcessedCharacter> getLines(BufferedImage image)
   {
      List<ProcessedCharacter> lines = new ArrayList<ProcessedCharacter>();
      int[] projections;

      projections = getHorizontalProjections(image);

      int top = 0;
      int count = 1;
      for (int y = 0; y < image.getHeight(); y++)
      {
         if (projections[y] > 0)
         {
            top = y;
            while (y < image.getHeight() && projections[y] > 0)
            {
               y++;
            }
            ProcessedCharacter temp = new ProcessedCharacter(image.getSubimage(0, top, image.getWidth(), y - top), count);
            temp.trimImage();
            if(temp.getImageSegment().getHeight() < 30)
               lines.add(temp);
            else{
               System.out.println("Line " + temp.getID() + " of invalid height - Assuming it's a picture and skipping");
               Deskewer.writeImage("output/line-" + temp.getID() + ".png", temp.getImageSegment());
            }
            count++;
            //y--;
         }
      }
      
      for( ProcessedCharacter line : lines){
      Deskewer.writeImage("output/line-" + line.getID() + ".png", line.getImageSegment());
      }
      
      return lines;
   }

   private static List<List<ProcessedCharacter>> getCharacters(List<ProcessedCharacter> lines)
   {
      characterID = 0;
      lineID = 0;

      List<List<ProcessedCharacter>> document = new ArrayList<List<ProcessedCharacter>>();
      for (ProcessedCharacter current : lines)
      {
         document.add(getCharacters(current.getImageSegment()));
         lineID++;
      }

      return document;
   }

   private static List<ProcessedCharacter> getCharacters(BufferedImage image)
   {
      List<ProcessedCharacter> characters = new ArrayList<ProcessedCharacter>();
      int[] projections;

      projections = getVerticalProjections(image);

      int left = 0;
      for (int x = 0; x < image.getWidth(); x++)
      {
         //if this column actually has dark pixels in it, it is of interest
         if (projections[x] > 0)
         {
            //Keep track of where we started, so we know how far we have gone.
            left = x;
            //While there are still pixels to look at
            while (x < image.getWidth() && x < projections.length && projections[x] > 0)
            {
               //Double twoPixel = 2.0 / (double) image.getHeight();
               //If there aren't many set pixels, indicating that we are close 
               //to the end of a letter
               if (projections[x] <= 3 && x > 1 && x < image.getWidth() - 1)
               {
                  Boolean combinedRight = false;
                  Boolean combinedLeft = false;

                  for (int i = 1; i < image.getHeight() - 1; i++)
                  {
                     if (image.getRGB(x, i) == Color.BLACK.getRGB())
                     {
                        //The previous pixel is combined with the next one if it is 
                        //directly connected on either side.
                        combinedRight |= image.getRGB(x, i) == image.getRGB(x + 1, i - 1);
                        combinedRight |= image.getRGB(x, i) == image.getRGB(x + 1, i);
                        combinedRight |= image.getRGB(x, i) == image.getRGB(x + 1, i + 1);
                        combinedLeft |= image.getRGB(x, i) == image.getRGB(x - 1, i - 1);
                        combinedLeft |= image.getRGB(x, i) == image.getRGB(x - 1, i);
                        combinedLeft |= image.getRGB(x, i) == image.getRGB(x - 1, i + 1);
                     }
                  }
                  if (!combinedRight ^ !combinedLeft)
                  {
                     break;
                  }
               }
               x++;
            }
            //Provided we have actually gone forward
            if (x != left)
            {               
               int charWidth = (x - left);
               if (charWidth > 16) //If the width is larger than 16, it's more than one character
               {
                  int [] borders = getVertMinima(image.getSubimage(left, 0, charWidth, image.getHeight()), 2);
                  //int estimatedNumCharacters = borders.length; //(int) Math.round((float) charWidth / 13.0);//(float) currentLibrary.typicalWidth);
                  //charWidth = charWidth / estimatedNumCharacters;
                  //for(int i = 0; i < estimatedNumCharacters; i++)
                  int oldWidth = 0; //The "widths" are actually indexes
                  for (int width : borders)
                  { 
                     width -= oldWidth; //adjust the index so it's actually a width
                     oldWidth += width; //Adjust the old width to include the width of this character
                     characters.add(new ProcessedCharacter(image.getSubimage(left, 0, width, image.getHeight()), characterID, lineID));
                     characters.get(characters.size() - 1).followedBySpace = false;//true;
                     characterID++;
                     left += width;
                  }
//                  characters.add(new ProcessedCharacter(image.getSubimage(left, 0, charWidth, image.getHeight()), characterID, lineID));
               } else
               {
                  characters.add(new ProcessedCharacter(image.getSubimage(left, 0, charWidth, image.getHeight()), characterID, lineID));
               }
            }

            //Look ahead to see if the next block of blank pixels are at least 
            //half the width of the current character. If so, it's probably a 
            //space
            int temp = x + 1;
            for (; temp < image.getWidth() /*&& temp - x < x - left */ && projections[temp] == 0; temp++);

            if (characters.size() > 0)
            {
               characters.set(characters.size() - 1, getSpacing(characters.get(characters.size() - 1), image, x, projections));
               //characters.get(characters.size() - 1).followedBySpace = ((temp - x) > 5);
               //characters.get(characters.size() - 1).followedBySpace = (temp - x < x - left && ((double) (temp - x) / (double) (x - left)) > .5);
               //characters.get(characters.size() - 1).followedBySpace = (temp - x < x - left && (double) (temp - x) / (double) (x - left) > (.5 * currentLibrary.typicalAR));            
               characters.get(characters.size() - 1).extractFeatures();
               characters.get(characters.size() - 1).getIntersectionStrings();
            }

            characterID++;
            //x--;
         }
      }
      return characters;
   }
 
/**
 * Find a list of local isMinima based on the vertical projections.
 * 
 * @param pImage The image to be analyzed
 * @param threshold The minimum number of pixels required to count as a isMinima
 * @return An array of the indexes of the minimum values.
 */   
   public static int[] getVertMinima(BufferedImage pImage, int threshold){

      int[] verticalProjections = getVerticalProjections(pImage);
      boolean[] isMinima;
      isMinima = new boolean[pImage.getWidth()];
      Arrays.fill(isMinima, false);
      for (int i = 6; i < verticalProjections.length - 1; i++)
      {
         //A sector is a isMinima if it's value is lower than both of it's neighbors 
         //and is less than or equal to the specified threshold
         if (verticalProjections[i] <= threshold
                 && verticalProjections[i - 1] >= verticalProjections[i]
                 && verticalProjections[i + 1] >= verticalProjections[i])
         {
            isMinima[i] = true;
         }
      }
      
      //I don't care about the beginning of the segment
      isMinima[0] = false;
      //But, the end better be a local minimum
      isMinima[isMinima.length - 1] = true;
      
      
      //some normalization. I don't want multiple minima next to eachother
      for(int i = 6; i < verticalProjections.length - 1; i++){
         //If there are three in a row, take the middle one
 //        if(isMinima[i - 1] == isMinima[i] == isMinima[i + 1] == true)
 //           isMinima[i - 1] = isMinima[i + 1] = false;
         
         //If just two in a row, take the latter
         if(isMinima[i - 1] && isMinima[i]) isMinima[i - 1] = false;
      }
      
      
      int count = 0;
            for(int i = 0; i < isMinima.length; i++)
               if(isMinima[i]){
                  count++;
                  i += 6;
               }
      
      int index = 0;
      int [] minima = new int[count];      
      for(int i = 0; i < isMinima.length; i++)
         if(isMinima[i]){
            minima[index++] = i;
            i += 6; //We can't have two minima within 6 spaces of eachother
         }
      
      return minima;
   }
   
   public static ProcessedCharacter getSpacing(ProcessedCharacter charToProcess, 
           BufferedImage image, int x, int[] projections)
   {
      int temp = x + 1;
      for (; temp < image.getWidth() /*&& temp - x < x - left */ && projections[temp] == 0; temp++);

      Deskewer.writeImage("output/test-" + charToProcess.getID() + ".png", charToProcess.getImageSegment());
      charToProcess.setFollowedBySpace((temp - x) > 15);
      /*
       if(currentLibrary == null)
       characters.get(characters.size() - 1).followedBySpace = (temp - x < x - left && (double) (temp - x) / (double) (x - left) > .6);
       else
       characters.get(characters.size() - 1).followedBySpace = (temp - x < x - left && (double) (temp - x) / (double) (x - left) > (.5 * currentLibrary.typicalAR));
       */
      return charToProcess;
   }

   public static List<List<ProcessedCharacter>> extractAll(BufferedImage image)
   {
      return getCharacters(getLines(image));
   }

   public static void learnFont(String FileName, String FontName, double angle)
   {
      Font font = null;// = Font.getFont("Times New Roman");

      //If we have specified a font, then we can try to get that one and learn it
      if (FileName != "" && FontName != "")
      {
         try
         {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            font = Font.createFont(Font.TRUETYPE_FONT, new File(FileName)).deriveFont(24F);
            ge.registerFont(font);
            System.out.println("Loaded font " + font.getName() + "From TTF");
         } catch (FontFormatException | IOException e)
         {
            e.printStackTrace();
            return;
         }
      } else
      {
         //If we couldn't learn the font, then Bail out!
         return;
      }

      currentLibrary = FontLibrary.LoadLibrary(font.getName() + "-" + angle);

      if (currentLibrary != null)
      {
         System.err.println("FontLibrary Loaded from file: " + font.getName() + "-" + angle);
         return;
      }

      int x = 3500;
      int y = 200;

      BufferedImage bufferedImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
      Graphics g = bufferedImage.getGraphics();
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, x, y);
      g.setFont(font);
      g.setColor(Color.BLACK);
      String test = new String();
      String test2 = new String();
      for (int i = 33; i < 123; i++)
      {
         if (i != 34)
         {
            test += (char) i;
            test2 += (char) i;
            test += " ";
         }
      }

      g.drawString(test, 50, 50);
      //g.drawString(test2, 50, 150);
      
      //Here, we rotate the image by the given angle and then rotate it back.
      //This will reproduce any artifacts of rotation in the library font,
      //hopefully improving accuracy
      if(angle != 0.0)
      {
//         bufferedImage = ImageRotator.rotateRad(bufferedImage, angle);
//         bufferedImage = ImageRotator.rotateRad(bufferedImage, -angle);
      }

      Deskewer.writeImage("DebugLearnedImage.png", bufferedImage);
      
      List<List<ProcessedCharacter>> all = extractAll(bufferedImage);

      currentLibrary = new FontLibrary(all, font.getFontName() + "-" + angle);
      int i = 33;
      double averageAR = 0;
      double averageWidth = 0;
      for (List<ProcessedCharacter> currentLine : all)
      {
         for (ProcessedCharacter current : currentLine)
         {
            if (i == 34)
            {
               i++;
            }

//            System.out.println("AR for " + (char) i + ": " + current.getAspectRatio());
            current = currentLibrary.findClosestMatch(current);
//            System.out.println("Closest match for " + (char) i + " is: " + current.value + " With a confidence of: " + current.confidence);
            
             current.value = (char) i;
             current.calculateHistograms();
             

//             Deskewer.writeImage("output/character" /*+ current.getLineNum() */+ "-" + i + ".png", current.getImageSegment());
            i++;

            averageAR += current.getAspectRatio();
            //The length of the vertical histogram is the width of the image
            averageWidth += current.getVHistogram().length;
         }
      }

      currentLibrary.typicalAR = averageAR / (double) (i - 34);
      currentLibrary.typicalWidth = averageWidth / (double) (i - 34);
      
      FontLibrary.SaveLibrary(currentLibrary);

      //Deskewer.writeImage("LearnedFont.png", bufferedImage);
      System.err.println("FontLibrary Generated from TTF: " + currentLibrary.name());

   }

   /**
    * Identify Characters. A public method that accepts an image and
    *
    * @param pImageToProcess
    * @return
    */
   public static String identifyCharacters(BufferedImage pImageToProcess)
   {
      double confidence = 0;
      double count = 0;
      String identifiedString = "";
      List<List<ProcessedCharacter>> all = extractAll(pImageToProcess);
      if (currentLibrary != null)
      {
         identifiedString = currentLibrary.matchAll(all);
      }
      //This code is for debugging and allows confidences to be shown.
      for (List<ProcessedCharacter> temp : all)
      {
         for (ProcessedCharacter current : temp)
         {
            confidence += current.confidence;
            count++;
            //System.out.println(current.value + " : " + current.confidence);
            //Deskewer.writeImage("output/character" + current.getLineNum() + "-" + current.getID() /*+ "-" + current.getAspectRatio() */ + ".png", current.getImageSegment());

         }
      }
      int roundedConfidence = (int) ((1.0 - (confidence / count)) * 10000);
      //System.out.println("Total confidence : " + (double) roundedConfidence / 100 + "%");
      return identifiedString;
   }

   public void extractFeatures(ProcessedCharacter input)
   {
      int featureNum;
      BufferedImage segment = input.getImageSegment();
      for (int x = 1; x < segment.getWidth(); x++)
      {
         for (int y = 1; y < segment.getHeight(); y++)
         {
            if (Deskewer.isDark(new Color(segment.getRGB(x, y))))
            {
               featureNum = 0;
               featureNum &= (Deskewer.isDark(new Color(segment.getRGB(x - 1, y - 1))) ? 1   : 0);
               featureNum &= (Deskewer.isDark(new Color(segment.getRGB(x    , y - 1))) ? 2   : 0);
               featureNum &= (Deskewer.isDark(new Color(segment.getRGB(x + 1, y - 1))) ? 4   : 0);
               featureNum &= (Deskewer.isDark(new Color(segment.getRGB(x - 1, y - 0))) ? 8   : 0);
               featureNum &= (Deskewer.isDark(new Color(segment.getRGB(x + 1, y - 0))) ? 16  : 0);
               featureNum &= (Deskewer.isDark(new Color(segment.getRGB(x - 1, y + 1))) ? 32  : 0);
               featureNum &= (Deskewer.isDark(new Color(segment.getRGB(x    , y + 1))) ? 64  : 0);
               featureNum &= (Deskewer.isDark(new Color(segment.getRGB(x + 1, y + 1))) ? 128 : 0);
               input.features[featureNum]++;
            }
         }
      }
   }

   public static String findIntersectionString(ProcessedCharacter input)
   {
      BufferedImage image = input.getImageSegment();

      int top;
      int[] intersections;
      int lastIntersection = 0;
      intersections = new int[image.getWidth()];
      String intersectionCount = "";
      for (int x = 0; x < image.getWidth(); x++)
      {
         intersections[x] = 0;
         for (int y = 0; y < image.getHeight(); y++)
         {
            //System.out.println("(" + x + "," + y + ") : " + image.getRGB(x, y));
            if (Deskewer.isDark(new Color(image.getRGB(x, y))))
            {
               top = y;
               while (y < image.getHeight() && Deskewer.isDark(new Color(image.getRGB(x, y))))
               {
                  y++;
               }
               intersections[x]++;
               //myPoints.add(new Point(x + 1, top, y));
            }
         }
         if (intersections[x] != 0 && intersections[x] != lastIntersection)
         {
            intersectionCount += intersections[x];
            lastIntersection = intersections[x];
         }
      }
      input.intersectionStringH = intersectionCount;
      return intersectionCount;
   }

}
