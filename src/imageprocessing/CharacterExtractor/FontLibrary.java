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
         
         ProcessedCharacter differentiated = null;
         switch (lowestHistMatch.value)
         {
            case 'q':
               differentiated = differentiateChars("gpq", input);
               break;
            case 'k':
               differentiated = differentiateChars("hk", input);
               break;
            //case 'w':
//            case 'n':
//            case 'u':
//               differentiated = differentiate_un(input);//differentiateChars("un", input);
//               break;
            case 'a':
            case 's':
            case 'e':
            //case '&':
            case '8':               
            case '9':               
            case 'B':               
               differentiated = differentiate_ase8B9(input);//differentiateChars("ase8B9", input);
               break;
            case 'o':
            case 'c':
            case 'u':
            case 'n':
               differentiated =  this.differentiate_ocun(input);//differentiateChars("oc", input);
               break;
/*            case 'r':
            case 'x':
               differentiated = differentiateChars("rx", input);
            case ',':
            case '.':
               differentiated = differentiateChars(",.", input);
               break;
            case '/':
            case '\\':
            case '|':
               differentiated = differentiateChars("\\|/ft", input);
               break;*/
            case 'l':
            case 'i':
               differentiated = differentiate_il(input);//differentiateChars("il", input);
               break;
            default:
               break;
         }
         lowestHistMatch = differentiated == null ? lowestHistMatch : differentiated;

         
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
/*      System.out.println("Looking for best match to H: " + 
              testSubject.getIntersectionStringH() + " V: " + 
              testSubject.getIntersectionStringV());
*/      
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
      
/*      for (ProcessedCharacter CurrentBestAvailableMatch : reallyGoodMatches)
      System.out.println(
              "Candidate match: " + CurrentBestAvailableMatch.value 
              + " With H string: " + CurrentBestAvailableMatch.intersectionStringH
                      + " and V String: " + CurrentBestAvailableMatch.intersectionStringV);
 */     
      
      return bestAvailableMatch;
   }
   
   public ProcessedCharacter groupByPixelDensity(List<ProcessedCharacter> tied, ProcessedCharacter testSubject) {
      System.out.println("Breaking a tie");
      ProcessedCharacter winner = null;
      double testSubjectDensity = testSubject.getPixelDensity();
      double lowestDifference = Double.MAX_VALUE;
      
      //System.out.println("Test subject Density: " + testSubjectDensity);
      
      for(ProcessedCharacter candidate : tied){
         double candidateDensity = candidate.getPixelDensity();
         double candidateScore = Math.abs(testSubjectDensity - candidateDensity);
         //System.out.println("Candidate '" + candidate.value + "' Score: " + candidateScore + " with density: " + candidateDensity);
//int candidateScore = Math.abs(candidate.intersectionStringH.length() - testSubject.intersectionStringH.length());
//candidateScore += Math.abs(candidate.intersectionStringV.length() - testSubject.intersectionStringV.length());
         if(candidateScore < lowestDifference)
         {
            winner = candidate;
            lowestDifference = candidateScore;
         }
      }

      return winner;
   }
   
   
      public ProcessedCharacter breakTies(List<ProcessedCharacter> tied, ProcessedCharacter testSubject) {
         
         //Somehow differentiate by "Open-ness"
         //Which way is the character open?
         return null;
      }
   
      public ProcessedCharacter differentiate_un(ProcessedCharacter testSubject){
         String intersect = testSubject.getFullIntersectionStringH();
         //System.out.println("Full intersection String: " + intersect);
         int index = intersect.indexOf('1');

         String decision;
         if(index > (intersect.length() / 2))
            //The joined part is in the lower half of the image.
            decision = "u";
         else
            decision = "n";
         
         return getLibrarySubset(decision).get(0);

      }
      
      public ProcessedCharacter differentiate_il(ProcessedCharacter testSubject){
         String decision;
         if(isDotted(testSubject))
            decision = "i";
         else
            decision = "l";
         
         return getLibrarySubset(decision).get(0);
      }
      
      public boolean isDotted(ProcessedCharacter input){
         String intersect = input.getFullIntersectionStringH();
         //Any two non zero numbers with any number of zeros between them
         //That means there are two parts to the character, separated by 
         //blank space, eg a dotted i.
         return intersect.matches("0*[1-9]+0+[1-9]+0*");
      }
      
      public ProcessedCharacter differentiate_ocun(ProcessedCharacter testSubject){
         String intersectionString = testSubject.getFullIntersectionStringH();
         int midpoint = testSubject.getImageSegment().getWidth() / 2;
         //If the first time there is only one intersection is in the top
         //and the last time there is only one intersection is near the bottom
         //It's either an o or a c
         if(intersectionString.indexOf("1") < midpoint && intersectionString.lastIndexOf("1") > midpoint)
            return differentiateChars("oc", testSubject);
         //Otherwise there is only one intersection either on the top or 
         //the bottom, making it a u or an n
         else
            return this.differentiate_un(testSubject);
      }
      
      public ProcessedCharacter differentiate_ase8B9(ProcessedCharacter testSubject){
         
         displayRelevantDebugInfo("ase8B9");
         
         return differentiateChars("ase8B9", testSubject);         
      }

      public void displayRelevantDebugInfo(String characters){
         List<ProcessedCharacter> desiredChars = getLibrarySubset(characters);
         System.out.println("\tFull Horizontal Strings");
         for(ProcessedCharacter current : desiredChars)
            System.out.println(current.value + ": " + current.getFullIntersectionStringH());
         System.out.println("\tFull Vertical Strings");

      
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
            //If the character is found in the desired characters list, keep it
            if (desiredCharacters.indexOf(current.value) != -1)            
               subset.add(current);
         
      return subset;
   }
   
}
