/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.despeckle;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author cave
 */
public class Despeckler
{

   private static BufferedImage image;

   public Despeckler()
   {
   }

   private static void setImage(BufferedImage pImage)
   {
      image = pImage;
   }

   /**
    * A class to detect similar colors. Similar colors are calculated by taking
    * the difference between the RGB values, (one at a time,) and comparing the
    * absolute value of the difference with the similarity factor multiplied by
    * 255 (Maximum value). If the differences for all three RGB values is less,
    * the colors are similar.
    *
    * @param c1 The first color to be compared
    * @param c2 The second color to be compared
    * @param similarity The similarity factor. This number is assumed to be
    * between 0 and 1 and represents the percentage of similarity between the
    * two colors. For example, a .05 means that the two numbers are 5% similar,
    * or, that is to say, the RGB values are within 5% of each other.
    * @return True if the colors are within the similarity index, false
    * otherwise.
    */
   public static boolean isSimilar(Color c1, Color c2, float similarity)
   {
      return (Math.abs(c1.getRed() - c2.getRed()) <= (similarity * 255)
              && Math.abs(c1.getGreen() - c2.getGreen()) <= (similarity * 255)
              && Math.abs(c1.getBlue() - c2.getBlue()) <= (similarity * 255));
   }
   
   public static boolean isSimilar(int c1, int c2, float similarity)
   {
      return isSimilar(new Color(c1), new Color (c2), similarity);
   }

   public static Boolean isDark(Color c)
   {
      //return ((c.getRGB() & 0x000000ff) == 0);

      float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
      float brightness = hsb[2];
      return (brightness < 0.5);
   }

   public static Boolean isNoise(int x, int y)
   {
      int dark = 0;
      int similar = 0;
      if (y < 1
              || (x < 1)
              || x + 1 < image.getWidth()
              || y + 1 < image.getHeight())
      {
         return false;
      }

      Color pixel = new Color(image.getRGB(x, y));

      for (int i = x - 1; i < x + 1; i++)
      {
         for (int j = y - 1; j < y + 1; j++)
         {
            if (j == 0 && i == 0)
            {
               continue;
            }
            Color temp = new Color(image.getRGB(i, j));
            if (!isDark(temp))
            {
               System.err.println("Pixel [" + i + "][" + j + "] is dark");
               dark++;
            }
            if (isSimilar(pixel, temp, (float) .05))
            {
               System.err.println("Pixel [" + i + "][" + j + "] is similar");

               similar++;
            }
         }
      }

      return ((dark >= 5) && (similar >= 5));
   }

   public static BufferedImage despeckle(BufferedImage pImage)
   {
      setImage(pImage);
      return despeckle();
   }

   private static BufferedImage despeckle()
   {
      int x = image.getWidth();
      int y = image.getHeight();

      Boolean[][] noise = new Boolean[x][y];

      for (int i = 0; i < x; i++)
      {
         for (int j = 0; j < y; j++)
         {
            noise[i][j] = isNoise(i, j);
         }
      }
      for (int i = 0; i < x; i++)
      {
         for (int j = 0; j < y; j++)
         {
            if (noise[i][j])
            {
               System.err.println("Pixel [" + i + "][" + j + "] is noise");
               image.setRGB(x, y, Color.WHITE.getRGB());
            }
         }
      }
      return image;
   }

   public static BufferedImage threshold(BufferedImage pImage, double pThresh)
   {
      setImage(pImage);

      for (int x = 0; x < image.getWidth(); x++)
      {
         for (int y = 0; y < image.getHeight(); y++)
         {
            Color c = new Color(image.getRGB(x, y));
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            float brightness = hsb[2];
            if (brightness < pThresh)
            {
               image.setRGB(x, y, Color.BLACK.getRGB());
            } else
            {
               image.setRGB(x, y, Color.WHITE.getRGB());
            }

         }
      }

      return image;
   }

}
