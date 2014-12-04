/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.CharacterExtractor;

import java.util.List;

/**
 * Font Library Class.
 * Stores a collection of known characters. Character sets can be assigned by
 * providing a ttf file of a known font. The 
 * @author Chris Vergaray
 */
public class FontLibrary
{

   private List<List<ProcessedCharacter>> characters;
   private String fontName;
   private int countCharacters;

   public FontLibrary(List<List<ProcessedCharacter>> pCharacters,
           String pName)
   {
      fontName = pName;
      countCharacters = 0;
      for (List<ProcessedCharacter> currentLine : pCharacters)
      {
         countCharacters += currentLine.size();
      }

      int i = 33;
      for (List<ProcessedCharacter> currentLine : pCharacters)
      {
         for (ProcessedCharacter current : currentLine)
         {
            if (i == 34)
            {
               i++;
            }
            current.calculateHistograms();
            current.value = (char) i;
            i++;

         }
      }
      characters = pCharacters;

   }

   public String name()
   {
      return fontName;
   }

   public int size()
   {
      return countCharacters;
   }

   public double compareHistograms(ProcessedCharacter input, ProcessedCharacter compared)
   {
      return input.compareHistogram(compared);
   }

   public ProcessedCharacter findClosestMatch(ProcessedCharacter input)
   {
      ProcessedCharacter lowestMatch = input;
      double lowestFound = Double.MAX_VALUE;

      if (characters != null)
      {
         for (List<ProcessedCharacter> currentLine : characters)
         {
            for (ProcessedCharacter current : currentLine)
            {
               double histogramConfidence = compareHistograms(input, current);
               //System.out.println("difference: " + histogramConfidence);
               if (histogramConfidence < lowestFound)
               {
                  lowestFound = histogramConfidence;
                  lowestMatch = current;
               }
            }
         }
         input.confidence = lowestFound;
         input.value = lowestMatch.value;
      }
      
      return input;
   }

   public String matchAll(List<List<ProcessedCharacter>> input)
   {
      String processed = "";

      for (List<ProcessedCharacter> currentLine : input)
      {
         for (ProcessedCharacter current : currentLine)
         {
            findClosestMatch(current);
            processed += current.value;
         }
         processed += '\n';
      }

      return processed;
   }
}
