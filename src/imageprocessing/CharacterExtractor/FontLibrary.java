/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.CharacterExtractor;

import imageprocessing.SpellCheckerManager;
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

   private final List<List<ProcessedCharacter>> characters;
   private final String fontName;
   private int countCharacters;
   public double typicalAR;

   static final long serialVersionUID = -687991492150864067L;

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
      //Double zResult =  input.compareZonedHistogram(compared, 6);//input.getVHistogram().length);
      //return hResult < zResult ? hResult : zResult;

      return input.compareHistograms(compared);

      //Double hResult =  input.compareHHistogram(compared);
      //Double vResult =  input.compareVHistogram(compared);
      //return hResult < vResult ? hResult : vResult;
   }

   public ProcessedCharacter getIntersectionSuggestion(ProcessedCharacter input)
   {
      ProcessedCharacter lowestMatch = input;
      int lowestFound = Integer.MAX_VALUE;
      
      if (characters != null)
      {
         for (List<ProcessedCharacter> currentLine : characters)
         {
            for (ProcessedCharacter current : currentLine)
            {
               int intersectConfidence = input.compareIntersectStrings(current);
//               System.out.println("difference: " + histogramConfidence);
               if (intersectConfidence < lowestFound)
               {
                  lowestFound = intersectConfidence;
                  lowestMatch = current;
                  if (intersectConfidence == 0)
                  {
                     break;
                  }
               }
            }
         }
         
         input.value = lowestMatch.value;
         input.confidence = lowestFound / 1000.0;

         //System.out.println("Selected: " + input.value);

      }
      
      return input;
   }
   
   public ProcessedCharacter findClosestMatch(ProcessedCharacter input)
   {
      ProcessedCharacter lowestHistMatch = input;
      ProcessedCharacter lowestFeatMatch = input;
      double lowestHistFound = input.confidence;
      double lowestFeatFound = input.confidence;

      if (characters != null)
      {
         for (List<ProcessedCharacter> currentLine : characters)
         {
            for (ProcessedCharacter current : currentLine)
            {
               double histogramConfidence = compareHistograms(input, current);
               double featureConfidence = input.compareFeatures(current);
//               System.out.println("difference: " + histogramConfidence);
               if (histogramConfidence < lowestHistFound)
               {
                  lowestHistFound = histogramConfidence;
                  lowestHistMatch = current;
                  if (lowestHistFound == 0)
                  {
                     break;
                  }
               }
               /*
               if (featureConfidence < lowestFeatFound)
               {
                  lowestFeatFound = featureConfidence;
                  lowestFeatMatch = current;
                  if (lowestFeatFound == 0)
                  {
                     break;
                  }
               }
               */
            }
         }
         
/*         if(lowestHistMatch.equals(lowestFeatMatch))
         {
            input.confidence = (lowestHistFound + lowestFeatFound) / 2;
            input.value = lowestHistMatch.value;
            //System.out.println("Same! " + input.value);
         }
         else
         {
            /*
            System.out.println("They were different:");
            System.out.println(lowestHistMatch.value + " : " + lowestHistFound);
            System.out.println(lowestFeatMatch.value + " : " + lowestFeatFound);            
            //*/
            
/*            
            ProcessedCharacter lowerChar;
            double lowerConf;
            if(lowestHistFound < lowestFeatFound)
            {
               lowerChar = lowestHistMatch;
               lowerConf = lowestHistFound;
            }
            else
            {
                lowerChar = lowestFeatMatch;
               lowerConf = lowestFeatFound;
            }
            
            input.value = lowerChar.value;
            input.confidence = lowerConf;
            
         }
*/
         
         input.value = lowestHistMatch.value;
         input.confidence = lowestHistMatch.confidence;
         
         System.out.println("Selected: " + input.value);
         
      }

      return input;
   }

   public String matchAll(List<List<ProcessedCharacter>> input)
   {
      String processed = "";
      String word = "";

      for (List<ProcessedCharacter> currentLine : input)
      {
         for (ProcessedCharacter current : currentLine)
         {
//            this.getIntersectionSuggestion(current);
            findClosestMatch(current);
            word += current.value;
            if (current.followedBySpace)
            {
//               List<com.swabunga.spell.engine.Word> suggestion = SpellCheckerManager.getSuggestions(word, 1);
//               if(suggestion != null && !suggestion.isEmpty())
//               word = suggestion.get(0).toString();
               processed += word;
               processed += " ";
               word = "";
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
         saveFile = new FileOutputStream("Libraries/" + libToSave.fontName + ".sav");

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
         loadFile = new FileInputStream("Libraries/" + libName + ".sav");

         // Create an ObjectOutputStream to put objects into load file.
         ObjectInputStream load = new ObjectInputStream(loadFile);

         loadedLibrary = (FontLibrary) load.readObject();

         load.close(); // This also closes loadFile.

         System.err.println("Loaded Font library " + loadedLibrary.fontName);

      } catch (Exception e)
      {
         System.out.println("Library could not be loaded. Attempting to generate..");
         //e.printStackTrace(); // If there was an error, print the info.
         return null;
      }
      return loadedLibrary;
   }

}
