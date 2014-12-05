/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.CharacterExtractor;

import imageprocessing.deskew.Deskewer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

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
            lines.add(new ProcessedCharacter(image.getSubimage(0, top, image.getWidth(), y - top), count));
            count++;
            //y--;
         }
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
         if (projections[x] > 0)
         {
            left = x;
            while (x < image.getWidth() && x < projections.length && projections[x] > 0)
            {
               if (projections[x] == 1 && x > 0)
               {
                  Boolean combined = false;

                  for (int i = 0; i < image.getHeight(); i++)
                  {
                     
                  }
               }
               x++;
            }
            if (x != left)
            {
               characters.add(new ProcessedCharacter(image.getSubimage(left, 0, x - left, image.getHeight()), characterID, lineID));
            }

            int tempLeft = x;
            int temp = x;
            for (; temp < image.getWidth() /*&& temp - x < x - left */ && projections[temp] == 0; temp++);
            if (characters.size() > 0)
            {
               characters.get(characters.size() - 1).followedBySpace = (temp - x < x - left && (double) (temp - x) / (double) (x - left) > .6);
            }
            characterID++;
            //x--;
         }
      }
      return characters;
   }

   public static List<List<ProcessedCharacter>> extractAll(BufferedImage image)
   {
      return getCharacters(getLines(image));
   }

   public static void learnFont(String FileName, String FontName)
   {
      Font font = null;// = Font.getFont("Times New Roman");

      //If we have specified a font, then we can try to get that one and learn it
      if (FileName != "" && FontName != "")
      {
         try
         {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            font = Font.createFont(Font.TRUETYPE_FONT, new File(FileName)).deriveFont(36F);
            ge.registerFont(font);
            System.out.println("Created font " + font.getName());
         } catch (Exception e)
         {
            e.printStackTrace();
         }
      } else
      {
         //If we couldn't learn the font, then Bail out!
         return;
      }

      currentLibrary = FontLibrary.LoadLibrary(font.getName());

      if (currentLibrary != null)
      {
         System.err.println("FontLibrary Loaded: " + font.getName());
         return;
      }

      int x = 7000;
      int y = 200;

      BufferedImage bufferedImage = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
      Graphics g = bufferedImage.getGraphics();
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, x, y);
      g.setFont(font);
      g.setColor(Color.BLACK);
      String test = new String();
      String test2 = new String();
      for (int i = 33; i < 127; i++)
      {
         if (i != 34)
         {
            test += (char) i;
            test2 += (char) i;
            test += " ";
         }
      }

      g.drawString(test, 50, 50);
      g.drawString(test2, 50, 150);

      List<List<ProcessedCharacter>> all = extractAll(bufferedImage);

      currentLibrary = new FontLibrary(all, font.getFontName());
      int i = 33;
      for (List<ProcessedCharacter> currentLine : all)
      {
         for (ProcessedCharacter current : currentLine)
         {
            if (i == 34)
            {
               i++;
            }

            //System.out.println("AR for " + (char) i + ": " + current.getAspectRatio());
            current = currentLibrary.findClosestMatch(current);
            //System.out.println("Closest match for " + (char) i + " is: " + current.value + " With a confidence of: " + current.confidence);
            /*
             current.value = (char) i;
             current.calculateHistograms();
             Deskewer.writeImage("output/character" + current.getLineNum() + "-" + i + ".png", current.getImageSegment());
             */
            i++;

         }
      }

      FontLibrary.SaveLibrary(currentLibrary);

      Deskewer.writeImage("LearnedFont.png", bufferedImage);

      System.err.println("FontLibrary Generated: " + currentLibrary.name());

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
            Deskewer.writeImage("output/character" + current.getLineNum() + "-" + current.getID() + ".png", current.getImageSegment());

         }
      }
      int roundedConfidence = (int) ((1.0 - (confidence / count)) * 10000);
      System.out.println("Total confidence : " + (double) roundedConfidence / 100 + "%");
      return identifiedString;
   }

}
