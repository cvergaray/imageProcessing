/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.CharacterExtractor;

import java.awt.image.BufferedImage;

/**
 * Processed Character Class.
 * This class stores all relevant data about characters for OCR as well as 
 * providing some histogram related computation and comparison.
 * @author Chris Vergaray
 */
public class ProcessedCharacter
{

   private final BufferedImage imageSegment;
   private final int characterID;
   private final int lineNumber;
   char value;
   double confidence;
   private int[] hHistogram;
   private int[] vHistogram;

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
      return vHistogram;
   }

   public int[] getHHistogram()
   {
      return hHistogram;
   }
   
   public double getAspectRatio()
   {
      return (double) imageSegment.getHeight()
              / (double) imageSegment.getWidth();
   }
   
   /**
    * Calculate Histograms.
    * Calculates the vertical and horizontal projections (Histograms) by 
    * calling the static method of the character extractor. This function only
    * calculates if the histograms are not set yet.
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
    * RecalculateHistograms.
    * A public function that allows the forced recalculation of histograms.
    * Sets the variables to null and then calls calculateHistograms();
    * This function only needs to be called if the image segment is changed.
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
   double compareHistogram(ProcessedCharacter input)
   {

      //First check the aspect ratios
      //If they are very different, one of the projections is probably limited
      //and histogram comparisons would probably be difficult.
      if (Math.abs(input.getAspectRatio() - this.getAspectRatio()) > 10)
      {
         //System.out.println("Difference is: " + Math.abs(input.getAspectRatio() - this.getAspectRatio()));
         return Double.MAX_VALUE;
      }

      //Just in case the histograms have not been calculated, calculate now
      calculateHistograms();
      
      //Select the smaller of the histograms so we stay within the limits of 
      //what can be compared.
      //TODO: Find a way to normalize the histograms so they are closer to 
      //the same
      int[] smallerHHistogram = hHistogram.length > input.getHHistogram().length ? input.getHHistogram() : hHistogram;
      int[] smallerVHistogram = vHistogram.length > input.getVHistogram().length ? input.getVHistogram() : vHistogram;

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
         differenceFactor += Math.abs(hHistogram[i] - input.getHHistogram()[i]);
      }
      for (int i = 0; i < smallerVHistogram.length; i++)
      {
         differenceFactor += Math.abs(vHistogram[i] - input.getVHistogram()[i]);
      }

      //The difference factor is the average of how many pixels are different 
      //in each projection (horizontal and vertical combined)
      differenceFactor = differenceFactor / (double) (smallerHHistogram.length + smallerVHistogram.length);

      return differenceFactor;
   }

}
