/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.CharacterExtractor;

//import imageprocessing.SpellCheckerManager;
import java.io.*;
import java.util.ArrayList;
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
   public double typicalWidth;

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
         
         ProcessedCharacter differentiated;
         switch (lowestHistMatch.value)
         {
            case 'q':
               differentiated = differentiateChars("gpq", input);
               lowestHistMatch = differentiated == null ? lowestHistMatch : differentiated;
               break;
            case 'k':
               differentiated = differentiateChars("hk", input);
               lowestHistMatch = differentiated == null ? lowestHistMatch : differentiated;
               break;
            case 'u':
               differentiated = differentiateChars("un", input);
               lowestHistMatch = differentiated == null ? lowestHistMatch : differentiated;
               break;
            case 'a':
            case 's':
            case 'e':
               differentiated = differentiateChars("ase", input);
               lowestHistMatch = differentiated == null ? lowestHistMatch : differentiated;
               break;
            case 'n':
               differentiated = differentiateChars("wn", input);
               lowestHistMatch = differentiated == null ? lowestHistMatch : differentiated;
               break;
            case 'c':
               differentiated = differentiateChars("oc", input);
               lowestHistMatch = differentiated == null ? lowestHistMatch : differentiated;
               break;
            default:
               break;
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
            if(current == null)
               System.out.println("Something is very wrong!");
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
   
   private ProcessedCharacter differentiateChars(
           String candidates, ProcessedCharacter testSubject){
      return differentiateChars(getLibrarySubset(candidates), testSubject);
   }
   
   private ProcessedCharacter differentiateChars(
           List<ProcessedCharacter> candidates, ProcessedCharacter testSubject)
   {
      System.out.println("Looking for best match to H: " + 
              testSubject.intersectionStringH + " V: " + 
              testSubject.intersectionStringV);
      
      List<ProcessedCharacter> goodMatches = new ArrayList<ProcessedCharacter>();

      int longestMatch = -1;
      for (ProcessedCharacter current : candidates)
      {
         String lcs = LCS(current.getIntersectionStringH(), testSubject.getIntersectionStringV());
         if(lcs.length() == longestMatch){
            goodMatches.add(current);
         } else if (lcs.length() > longestMatch) {
            longestMatch = lcs.length();
            goodMatches.clear();
            goodMatches.add(current);
         }
      }

      longestMatch = -1;
      List<ProcessedCharacter> reallyGoodMatches = new ArrayList<ProcessedCharacter>();
      for (ProcessedCharacter current : goodMatches) {
         String lcs = LCS(current.intersectionStringV, testSubject.intersectionStringV);
         if(lcs.length() == longestMatch){
            reallyGoodMatches.add(current);
         } else if (lcs.length() > longestMatch) {
            longestMatch = lcs.length();
            reallyGoodMatches.clear();
            reallyGoodMatches.add(current);
         } 
      }
      
      ProcessedCharacter bestAvailableMatch = null;
      if(!reallyGoodMatches.isEmpty())
         if(reallyGoodMatches.size() == 1)
            bestAvailableMatch = reallyGoodMatches.get(0);
         else
            bestAvailableMatch = breakTies(reallyGoodMatches, testSubject);
      
      for (ProcessedCharacter CurrentBestAvailableMatch : reallyGoodMatches)
      System.out.println(
              "Candidate match: " + CurrentBestAvailableMatch.value 
              + " With H string: " + CurrentBestAvailableMatch.intersectionStringH
                      + " and V String: " + CurrentBestAvailableMatch.intersectionStringV);
      
      
      return bestAvailableMatch;
   }
   
   public ProcessedCharacter breakTies(List<ProcessedCharacter> tied, ProcessedCharacter testSubject) {
      System.out.println("Breaking a tie");
      ProcessedCharacter winner = null;
      double testSubjectDensity = testSubject.getPixelDensity();
      double lowestDifference = Double.MAX_VALUE;
      
      System.out.println("Test subject Density: " + testSubjectDensity);
      
      for(ProcessedCharacter candidate : tied){
         double candidateScore = Math.abs(testSubjectDensity - candidate.getPixelDensity());
         System.out.println("Candidate Score: " + candidateScore);
//int candidateScore = Math.abs(candidate.intersectionStringH.length() - testSubject.intersectionStringH.length());
//candidateScore += Math.abs(candidate.intersectionStringV.length() - testSubject.intersectionStringV.length());
         if(candidateScore > lowestDifference)
         {
            winner = candidate;
            lowestDifference = candidateScore;
         }
      }

      return winner;
   }
   

 /*************************************************************************
 *  Accepts two strings and computes their longest common subsequence.
 * 
 * Copyright © 2000–2011, Robert Sedgewick and Kevin Wayne. 
 * Last updated: Wed Feb 9 09:20:16 EST 2011.
 * 
 * Adapted from: http://introcs.cs.princeton.edu/java/96optimization/LCS.java.html
 *************************************************************************/
   private String LCS(String x, String y)
   {
      String output = "";
      int M = x.length();
      int N = y.length();

      // opt[i][j] = length of LCS of x[i..M] and y[j..N]
      int[][] opt = new int[M + 1][N + 1];

      // compute length of LCS and all subproblems via dynamic programming
      for (int i = M - 1; i >= 0; i--)
      {
         for (int j = N - 1; j >= 0; j--)
         {
            if (x.charAt(i) == y.charAt(j))
            {
               opt[i][j] = opt[i + 1][j + 1] + 1;
            } else
            {
               opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
            }
         }
      }

      // recover LCS itself and print it to standard output
      int i = 0, j = 0;
      while (i < M && j < N)
      {
         if (x.charAt(i) == y.charAt(j))
         {
            output += x.charAt(i);
            i++;
            j++;
         } else if (opt[i + 1][j] >= opt[i][j + 1])
         {
            i++;
         } else
         {
            j++;
         }
      }
      return output;
   }
   
   public List<ProcessedCharacter> getLibrarySubset(String desiredCharacters)
   {
      List<ProcessedCharacter> subset = new ArrayList<ProcessedCharacter>();

      for (List<ProcessedCharacter> currentList : characters)
         for (ProcessedCharacter current : currentList)
            if (desiredCharacters.indexOf(current.value) != -1)            
               subset.add(current);
      return subset;
   }
   
}
