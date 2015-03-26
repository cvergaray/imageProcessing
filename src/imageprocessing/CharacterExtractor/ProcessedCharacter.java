/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.CharacterExtractor;

import imageprocessing.deskew.Deskewer;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.imageio.ImageIO;

/**
 * Processed Character Class. This class stores all relevant data about
 * characters for OCR as well as providing some histogram related computation
 * and comparison.
 *
 * @author Chris Vergaray
 */
public class ProcessedCharacter implements Serializable
{

   private transient BufferedImage imageSegment;
   private final int characterID;
   private final int lineNumber;
   char value;
   double confidence;
   private int[] hHistogram;
   private int[] vHistogram;
   public Boolean followedBySpace;
   public int[] features;
   public String intersectionStringH;
   public String intersectionStringV;

   /**
    * Complete Constructor. Accepts and assigns all values within the object.
    * This constructor is especially suited for generating libraries.
    *
    * @param pImage a BufferedImage of the segment of the main image that this
    * character comes from.
    *
    * @param id The place in line where the character belongs.
    *
    * @param lineNum the line number to which the character belongs. Combined
    * with the id, this can provide a coordinate of sorts for the character.
    *
    * @param pValue The character that this image represents.
    */
   public ProcessedCharacter(BufferedImage pImage, int id, int lineNum, char pValue)
   {
      imageSegment = pImage;
      characterID = id;
      lineNumber = lineNum;
      value = pValue;
      confidence = 0;
      features = new int[256];

      /*
       * Since we know what the character is, we might as well calculate all the
       * histograms for this instance. Especially useful when generating a 
       * library
       */
      calculateHistograms();
   }

   /**
    * Constructor for extracting within lines. This constructor is specifically
    * for storing a single character while retaining information about where in
    * the image it comes from; specifically the line number and place within the
    * line.
    *
    * @param pImage a BufferedImage of the segment of the main image that this
    * character comes from.
    *
    * @param id The place in line where the character belongs.
    *
    * @param lineNum the line number to which the character belongs. Combined
    * with the id, this can provide a coordinate of sorts for the character.
    */
   public ProcessedCharacter(BufferedImage pImage, int id, int lineNum)
   {
      imageSegment = pImage;
      characterID = id;
      lineNumber = lineNum;
      confidence = Double.MAX_VALUE;
      features = new int[256];

   }

   /**
    * Basic Constructor. Allows for basic characters to be created from an image
    * and an ID. This allows processedCharacter objects to hold lines of text.
    *
    * @param pImage a BufferedImage of the segment of the main image that this
    * character comes from.
    *
    * @param id The place in line where the character belongs.
    */
   public ProcessedCharacter(BufferedImage pImage, int id)
   {
      imageSegment = pImage;
      characterID = id;
      lineNumber = -1;
      confidence = Double.MAX_VALUE;
//      features = new int[256];

   }

   /*
    * Accessors 
    */
   public BufferedImage getImageSegment()
   {
      return imageSegment;
   }

   public int getID()
   {
      return characterID;
   }

   public int getLineNum()
   {
      return lineNumber;
   }

   public int[] getVHistogram()
   {
      if (vHistogram == null)
      {
         calculateHistograms();
      }
      return vHistogram;
   }

   public int[] getHHistogram()
   {
      if (hHistogram == null)
      {
         calculateHistograms();
      }
      return hHistogram;
   }

   public void setFollowedBySpace(Boolean hasSpace)
   {
      if (followedBySpace == null)
      {
         followedBySpace = hasSpace;
      }
   }

   public double[] getVRHistogram()
   {
      double[] VRH = new double[vHistogram.length];
      for (int i = 0; i < VRH.length; i++)
      {
         VRH[i] = (double) vHistogram[i] / (double) imageSegment.getHeight();
      }
      return VRH;
   }

   public double[] getHRHistogram()
   {
      double[] HRH = new double[hHistogram.length];
      for (int i = 0; i < HRH.length; i++)
      {
         HRH[i] = (double) hHistogram[i] / (double) imageSegment.getWidth();
      }
      return HRH;
   }

   public double getAspectRatio()
   {
      return (double) imageSegment.getHeight()
              / (double) imageSegment.getWidth();
   }

   public double getPixelDensity()
   {
      int a, b, y, z;
      for (a = 0; a < this.getHHistogram().length && this.getHHistogram()[a] == 0; a++);
      for (y = 0; y < this.getVHistogram().length && this.getVHistogram()[y] == 0; y++);

      for (b = this.getHHistogram().length - 1; b >= 0 && this.getHHistogram()[b] == 0; b--);
      for (z = this.getVHistogram().length - 1; z >= 0 && this.getVHistogram()[z] == 0; z--);

      double darkPixels = 0;
      int[] hist = this.getHHistogram();
      
      //Only one direction need be counted, since they should be exactly the same.
      for (int current : hist)
      {
         darkPixels += current;
      }
      
      //Pixel density is the number of dark pixels multiplied by the nubmer of total pixels (length * width)
      return darkPixels / (double) ((b - a) * (z - y));
   }
   /**
    * Calculate Histograms. Calculates the vertical and horizontal projections
    * (Histograms) by calling the static method of the character extractor. This
    * function only calculates if the histograms are not set yet.
    */
   public void calculateHistograms()
   {
      if (hHistogram == null)
      {
         hHistogram = CharacterExtractor.getHorizontalProjections(imageSegment);
      }
      if (vHistogram == null)
      {
         vHistogram = CharacterExtractor.getVerticalProjections(imageSegment);
      }
   }

   /**
    * RecalculateHistograms. A public function that allows the forced
    * recalculation of histograms. Sets the variables to null and then calls
    * calculateHistograms(); This function only needs to be called if the image
    * segment is changed.
    */
   public void recalculateHistograms()
   {
      hHistogram = null;
      vHistogram = null;
      calculateHistograms();
   }

   /**
    * CompareHistogram
    *
    * @param input a ProcessedCharacter that is to be compared.
    * @return
    */
   double compareHistograms(ProcessedCharacter input)
   {
      //First check the aspect ratios
      //If they are very different, one of the projections is probably limited
      //and histogram comparisons would probably be difficult.
//      if (Math.abs(input.getAspectRatio() - this.getAspectRatio()) > .1 * this.getAspectRatio())
      if (input.getAspectRatio() < .5 * this.getAspectRatio()
              || this.getAspectRatio() < .5 * input.getAspectRatio())
      {
         //System.out.println("Difference is: " + Math.abs(input.getAspectRatio() - this.getAspectRatio()));
         return Double.MAX_VALUE;
      }

      //Just in case the histograms have not been calculated, calculate now
      calculateHistograms();

      //Select the smaller of the histograms so we stay within the limits of 
      //what can be compared.
      //TODO: Find a way to normalize the histograms so they are closer to 
      //the same size
      double[] smallerHHistogram = hHistogram.length > input.getHHistogram().length ? input.getHRHistogram() : getHRHistogram();
      double[] smallerVHistogram = vHistogram.length > input.getVHistogram().length ? input.getVRHistogram() : getVRHistogram();
      double[] largerHHistogram = hHistogram.length > input.getHHistogram().length ? getHRHistogram() : input.getHRHistogram();
      double[] largerVHistogram = vHistogram.length > input.getVHistogram().length ? getVRHistogram() : input.getVRHistogram();
      //This will keep track of how different the images are.
      double differenceFactor = 0;

      /*
       The differences are calculated and tallied in the differenceFactor 
       variable.
       The calculations are made by subtracting one projection from the other 
       and adding the absolute value of the difference. This will allow 
       commutativity between characters when comparing.
       */
      for (int i = 0; i < smallerHHistogram.length; i++)
      {
         differenceFactor += Math.abs(smallerHHistogram[i] - largerHHistogram[i]);
      }
      double ar = input.getAspectRatio();
      if (ar < 7.0)
      {
         for (int i = 0; i < smallerVHistogram.length; i++)
         {
            differenceFactor += Math.abs(smallerVHistogram[i] - largerVHistogram[i]);
         }
      }

      //The difference factor is the average of how many pixels are different 
      //in each projection (horizontal and vertical combined)
      differenceFactor = differenceFactor / (double) (smallerHHistogram.length + smallerVHistogram.length);

      return differenceFactor;
   }

   /**
    * CompareHistogram
    *
    * @param input a ProcessedCharacter that is to be compared.
    * @return
    */
   double compareVHistogram(ProcessedCharacter input)
   {

      //First check the aspect ratios
      //If they are very different, one of the projections is probably limited
      //and histogram comparisons would probably be difficult.
//      if (Math.abs(input.getAspectRatio() - this.getAspectRatio()) > .1 * this.getAspectRatio())
      if (input.getAspectRatio() < .5 * this.getAspectRatio()
              || this.getAspectRatio() < .5 * input.getAspectRatio())
      {
         //System.out.println("Difference is: " + Math.abs(input.getAspectRatio() - this.getAspectRatio()));
         return Double.MAX_VALUE;
      }

      //Just in case the histograms have not been calculated, calculate now
      calculateHistograms();

      //Select the smaller of the histograms so we stay within the limits of 
      //what can be compared.
      //TODO: Find a way to normalize the histograms so they are closer to 
      //the same size
      double[] smallerVHistogram = vHistogram.length > input.getVHistogram().length ? input.getVRHistogram() : getVRHistogram();
      double[] largerVHistogram = vHistogram.length > input.getVHistogram().length ? getVRHistogram() : input.getVRHistogram();
      //This will keep track of how different the images are.
      double differenceFactor = 0;

      /*
       The differences are calculated and tallied in the differenceFactor 
       variable.
       The calculations are made by subtracting one projection from the other 
       and adding the absolute value of the difference. This will allow 
       commutativity between characters when comparing.
       */
      double ar = input.getAspectRatio();
      //if(ar < 7.0)
      for (int i = 0; i < smallerVHistogram.length; i++)
      {
         differenceFactor += Math.abs(smallerVHistogram[i] - largerVHistogram[i]);
      }

      //The difference factor is the average of how many pixels are different 
      //in each projection (horizontal and vertical combined)
      differenceFactor = differenceFactor / (double) smallerVHistogram.length;

      return differenceFactor;
   }

   /**
    * CompareFeatures. This works by comparing the features array of two
    * characters. In this case, there are always 256 items in both arrays, so
    * the checks for size used in the other compare functions are not necessary.
    * Instead we can directly compare the two arrays. The reason there are 256
    * items is because we can treat all pixels surrounding a dark pixel as
    * binary digits of which the value is determined by the darkness of the
    * pixel. Therefore, there are 2^8 = 256 possibilities and they can be
    * indexed by setting the corresponding bits and using the resultant number
    * as an index into the array. The bits are assigned starting at the LSB from
    * the top left pixel and moving the same way we read in English, skipping
    * the middle pixel. Thus, the (decimal) numbers used as masks for each pixel
    * are represented in the following grid: 
    * [001 : 002 : 004] 
    * [008 : 000 : 016]
    * [032 : 064 : 128]
    *
    * @param input a ProcessedCharacter that is to be compared.
    * @return
    */
   double compareFeatures(ProcessedCharacter input)
   {
      if (input.features == null || features == null)
      {
         return Double.MAX_VALUE;
      }

      //This will keep track of how different the images are.
      double differenceFactor = 0;

      /*
       The differences are calculated and tallied in the differenceFactor 
       variable.
       The calculations are made by subtracting one projection from the other 
       and adding the absolute value of the difference. This will allow 
       commutativity between characters when comparing.
       */
      for (int i = 0; i < 256; i++)
      {
         differenceFactor += Math.abs(input.features[i] - features[i]);
      }

//      countValue = countValue > 0 ? countValue : 1;
      //The difference factor is the average of how many features are different 
      differenceFactor = differenceFactor / 256;

      return differenceFactor;
   }

   public int compareIntersectStrings(ProcessedCharacter other)
   {
      if(other == null)
         return Integer.MAX_VALUE;
      this.getIntersectionStrings();
      other.getIntersectionStrings();
      int horizontal = this.intersectionStringH.compareTo(other.intersectionStringH);
      int vertical = this.intersectionStringV.compareTo(other.intersectionStringV);
      
      return Math.abs(horizontal) + Math.abs(vertical);
   }
   
   /**
    * CompareHistogram
    *
    * @param input a ProcessedCharacter that is to be compared.
    * @return
    */
   double compareHHistogram(ProcessedCharacter input)
   {

      //First check the aspect ratios
      //If they are very different, one of the projections is probably limited
      //and histogram comparisons would probably be difficult.
//      if (Math.abs(input.getAspectRatio() - this.getAspectRatio()) > .1 * this.getAspectRatio())
      if (input.getAspectRatio() < .5 * this.getAspectRatio()
              || this.getAspectRatio() < .5 * input.getAspectRatio())
      {
         //System.out.println("Difference is: " + Math.abs(input.getAspectRatio() - this.getAspectRatio()));
         return Double.MAX_VALUE;
      }

      //Just in case the histograms have not been calculated, calculate now
      calculateHistograms();

      //Select the smaller of the histograms so we stay within the limits of 
      //what can be compared.
      //TODO: Find a way to normalize the histograms so they are closer to 
      //the same size
      double[] smallerHHistogram = hHistogram.length > input.getHHistogram().length ? input.getHRHistogram() : getHRHistogram();
      double[] largerHHistogram = hHistogram.length > input.getHHistogram().length ? getHRHistogram() : input.getHRHistogram();
      //This will keep track of how different the images are.
      double differenceFactor = 0;

      /*
       The differences are calculated and tallied in the differenceFactor 
       variable.
       The calculations are made by subtracting one projection from the other 
       and adding the absolute value of the difference. This will allow 
       commutativity between characters when comparing.
       */
      for (int i = 0; i < smallerHHistogram.length; i++)
      {
         differenceFactor += Math.abs(smallerHHistogram[i] - largerHHistogram[i]);
      }
      double ar = input.getAspectRatio();

      //The difference factor is the average of how many pixels are different 
      //in each projection (horizontal and vertical combined)
      differenceFactor = differenceFactor / (double) smallerHHistogram.length;

      return differenceFactor;
   }

   public double compareZonedHistogram(ProcessedCharacter input, int zones)
   {
      //First check the aspect ratios
      //If they are very different, one of the projections is probably limited
      //and histogram comparisons would probably be difficult.
      if (Math.abs(input.getAspectRatio() - this.getAspectRatio()) > 10)
      {
         //System.out.println("Difference is: " + Math.abs(input.getAspectRatio() - this.getAspectRatio()));
         return Double.MAX_VALUE;
      }

      //I need to get a pair of zoned histograms from ProcessedCharacter
      double[] myHH = getZonedHHistogram(zones);
      double[] myVH = getZonedHHistogram(zones);
      double[] otherHH = input.getZonedHHistogram(zones);
      double[] otherVH = input.getZonedHHistogram(zones);

      //This will keep track of how different the images are.
      double differenceFactor = 0;

      /*
       The differences are calculated and tallied in the differenceFactor 
       variable.
       The calculations are made by subtracting one projection from the other 
       and adding the absolute value of the difference. This will allow 
       commutativity between characters when comparing.
       */
      for (int i = 0; i < zones; i++)
      {
         differenceFactor += Math.abs(myHH[i] - otherHH[i]);
      }
      for (int i = 0; i < zones; i++)
      {
         differenceFactor += Math.abs(myVH[i] - otherVH[i]);
      }

      //The difference factor is the average of how many pixels are different 
      //in each projection (horizontal and vertical combined)
      differenceFactor = differenceFactor / (double) (2 * zones);

      return differenceFactor;
   }

   double[] getZonedVHistogram(int zones)
   {
      calculateHistograms();
      int gap = (int) Math.ceil((double) vHistogram.length / (double) zones);
      double[] zonedHistogram = new double[zones];

      for (int i = 0; (i * gap) < vHistogram.length; i++)
      {
         zonedHistogram[i] = (double) vHistogram[i * gap] / vHistogram.length;
      }

      return zonedHistogram;
   }

   double[] getZonedHHistogram(int zones)
   {
      calculateHistograms();

      int gap = (int) Math.ceil((double) hHistogram.length / (double) zones);
      double[] zonedHistogram = new double[zones];

      for (int i = 0; (i * gap) < hHistogram.length; i++)
      {
         zonedHistogram[i] = (double) hHistogram[i * gap] / hHistogram.length;
      }

      return zonedHistogram;
   }

   public void trimImage()
   {
      int nTop = 0;
      int nBottom = getHHistogram().length - 1;
      Boolean notDone = true;
      while (nTop < nBottom && notDone)
      {
         if (this.getHHistogram()[nTop] == 0)
         {
            nTop++;
         } else 
         {
            notDone = false;
         }
         if (this.getHHistogram()[nBottom] == 0)
         {
            nBottom--;
         } else
         {
            notDone = false;
         }
      }

      if (nTop < nBottom)
      {
         imageSegment = imageSegment.getSubimage(0, nTop, imageSegment.getWidth(), nBottom - nTop);
      }
   }

   /**
    * Always treat de-serialization as a full-blown constructor, by validating
    * the final state of the de-serialized object.
    */
   private void readObject(
           ObjectInputStream aInputStream
   ) throws ClassNotFoundException, IOException
   {
      //always perform the default de-serialization first
      aInputStream.defaultReadObject();
      imageSegment = ImageIO.read(aInputStream);
     //ensure that object state has not been corrupted or tampered with maliciously
      // validateState();
   }

   /**
    * This is the default implementation of writeObject. Customize if necessary.
    */
   private void writeObject(
           ObjectOutputStream aOutputStream
   ) throws IOException
   {
      //perform the default serialization for all non-transient, non-static fields
      aOutputStream.defaultWriteObject();
      ImageIO.write(imageSegment, "png", aOutputStream);
   }

   public void extractFeatures()
   {
      int featureNum;
      BufferedImage segment = getImageSegment();
      for (int x = 1; x < segment.getWidth() - 1; x++)
      {
         for (int y = 1; y < segment.getHeight() - 1; y++)
         {
            if (Deskewer.isDark(new Color(segment.getRGB(x, y))))
            {
               featureNum = 0;
               featureNum |= (Deskewer.isDark(new Color(segment.getRGB(x - 1, y - 1))) ? 1   : 0);
               featureNum |= (Deskewer.isDark(new Color(segment.getRGB(x    , y - 1))) ? 2   : 0);
               featureNum |= (Deskewer.isDark(new Color(segment.getRGB(x + 1, y - 1))) ? 4   : 0);
               featureNum |= (Deskewer.isDark(new Color(segment.getRGB(x - 1, y - 0))) ? 8   : 0);
               featureNum |= (Deskewer.isDark(new Color(segment.getRGB(x + 1, y - 0))) ? 16  : 0);
               featureNum |= (Deskewer.isDark(new Color(segment.getRGB(x - 1, y + 1))) ? 32  : 0);
               featureNum |= (Deskewer.isDark(new Color(segment.getRGB(x    , y + 1))) ? 64  : 0);
               featureNum |= (Deskewer.isDark(new Color(segment.getRGB(x + 1, y + 1))) ? 128 : 0);
               features[featureNum]++;
            }
         }
      }

      /* This is debug code. Uncomment this comment block to hide it.
       System.out.println("Feature densities for: " + characterID + value);

       for(int i = 0; i < 256; i++)
       //System.out.format(null, args)
       System.out.println("" + i + " : " + features[i]);
       //*/
   }

   
      public void getIntersectionStrings()
      {
         getIntersectionStringH();
         getIntersectionStringV();
         
         /*
         System.out.println("Character " + getID() + ":");
         System.out.println(intersectionStringH);
         System.out.println(intersectionStringV);
         */
      }

   
      public String getFullIntersectionStringH()
   {
         BufferedImage image = getImageSegment();

         int[] intersections;
         intersections = new int[image.getHeight()];
         String intersectionCount = "";
         for (int y = 0; y < image.getHeight(); y++)
         {
            intersections[y] = 0;
            for (int x = 0; x < image.getWidth(); x++)
            {
               //System.out.println("(" + x + "," + y + ") : " + image.getRGB(x, y));
               if (Deskewer.isDark(new Color(image.getRGB(x, y))))
               {
                  while (x < image.getWidth()&& Deskewer.isDark(new Color(image.getRGB(x, y))))
                  {
                     x++;
                  }
                  intersections[y]++;
                  //myPoints.add(new Point(x + 1, top, y));
               }
            }
           //if(intersections[y] != 0)
               intersectionCount += intersections[y];            
         }
         return intersectionCount;

   }
      
   public String getIntersectionStringH()
   {
      if (this.intersectionStringH == null)
      {
         BufferedImage image = getImageSegment();

         int[] intersections;
         int lastIntersection = 0;
         intersections = new int[image.getHeight()];
         String intersectionCount = "";
         for (int y = 0; y < image.getHeight(); y++)
         {
            intersections[y] = 0;
            for (int x = 0; x < image.getWidth(); x++)
            {
               //System.out.println("(" + x + "," + y + ") : " + image.getRGB(x, y));
               if (Deskewer.isDark(new Color(image.getRGB(x, y))))
               {
                  while (x < image.getWidth()&& Deskewer.isDark(new Color(image.getRGB(x, y))))
                  {
                     x++;
                  }
                  intersections[y]++;
                  //myPoints.add(new Point(x + 1, top, y));
               }
            }
            if (intersections[y] != 0 && intersections[y] != lastIntersection)
            {
               intersectionCount += intersections[y];
               lastIntersection = intersections[y];
            }
         }
         intersectionStringH = intersectionCount;
      }
      return this.intersectionStringH;
   }

   
   public String getIntersectionStringV()
   {
      if (this.intersectionStringV == null)
      {
         BufferedImage image = getImageSegment();

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
         intersectionStringV = intersectionCount;
      }
      return this.intersectionStringV;
   }
}

