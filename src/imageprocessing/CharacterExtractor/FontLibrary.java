/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.CharacterExtractor;

import java.io.*;
import java.util.List;

/**
 * Font Library Class. Stores a collection of known characters. Character sets
 * can be assigned by providing a ttf file of a known font. The
 *
 * @author Chris Vergaray
 */
public class FontLibrary implements Serializable
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
      //Double hResult =  input.compareHistogram(compared);
      //Double zResult =  input.compareZonedHistogram(compared, 6);
      //return hResult < zResult ? hResult : zResult;
      return input.compareHistogram(compared);
   }

   public ProcessedCharacter findClosestMatch(ProcessedCharacter input)
   {
      ProcessedCharacter lowestMatch = input;
      double lowestFound = input.confidence;

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
         //System.out.println("Selected: " + lowestMatch.value);
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
            if (current.followedBySpace)
            {
               processed += " ";
            }
         }
         processed += '\n';
      }

      return processed;
   }

   public static Boolean SaveLibrary(FontLibrary libToSave)
   {
      try
      {
         // Catch errors in I/O if necessary.
         // Open a file to write to, named SavedObj.sav.
         FileOutputStream saveFile;
         saveFile = new FileOutputStream(libToSave.fontName + ".sav");

         // Create an ObjectOutputStream to put objects into load file.
         ObjectOutputStream save = new ObjectOutputStream(saveFile);

         save.writeObject(libToSave);

         save.close(); // This also closes loadFile.

      } catch (Exception e)
      {
         e.printStackTrace(); // If there was an error, print the info.
      }
      return true;
   }
   
   public static FontLibrary LoadLibrary(String libName)
   {
      FontLibrary loadedLibrary = null;
      try
      {
         // Catch errors in I/O if necessary.
         // Open a file to write to, named SavedObj.sav.
         FileInputStream loadFile;
         loadFile = new FileInputStream(libName + ".sav");

         // Create an ObjectOutputStream to put objects into load file.
         ObjectInputStream load = new ObjectInputStream(loadFile);

         loadedLibrary = (FontLibrary) load.readObject();

         load.close(); // This also closes loadFile.

      } catch (Exception e)
      {
         e.printStackTrace(); // If there was an error, print the info.
         return null;
      }
      return loadedLibrary;
   }
   
   
}
