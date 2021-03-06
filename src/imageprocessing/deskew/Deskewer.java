/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.deskew;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import javax.imageio.ImageIO;

/**
 *
 * @author cave
 */
public class Deskewer
{

   BufferedImage image;
   HoughCoordinates[][] HoughArray;
   HoughCoordinates[] top20;

   //The minimum number of votes needed to count
   protected int threshold;

   protected int mWidth;
   protected int mHeight;
   protected int[][] mAccumulatorMatrix;
   protected int m_CountSteps = 180;
   protected double m_AlphaStep = Math.PI / m_CountSteps;
   
   protected int mNeighbourhoodSize = 4;

   // the height of the accumulator array 
   protected int mAccumHeight;

   // double the hough height (allows for negative numbers) 
   protected int mDoubleHeight;

   // the number of points that have been added 
   protected int mNumPoints;

   // cache of values of sin and cos for different theta values. 
   //It's best to caclulate once and then use the values over and over 
   //because sin and cos are computationally expensive. 
   private double[] mSinCache;
   private double[] mCosCache;

   // the coordinates of the centre of the image 
   protected float centerX, centerY;

   public BufferedImage getImage()
   {
      return image;
   }

   public Deskewer(String pFilename)
   {
      File input;

      try
      {
         input = new File(pFilename);
         image = ImageIO.read(input);
         initWithImage(image);
      } catch (IOException e)
      {
         System.out.println(e.getMessage() + "Failed to open file");
         System.exit(-1);
      }
   }

   public Deskewer(BufferedImage pImage)
   {
      initWithImage(pImage);
   }
   
   public void initWithImage(BufferedImage pImage)
   {
//      System.err.println("Initializing...");

      //Default threshold. This should work for clean pictures.
      threshold = 1000;
      
      mNumPoints = 0;

      image = pImage;
      mWidth = image.getWidth();
      mHeight = image.getHeight();
//      System.err.println("Done Initializing...");

      // Calculate the maximum height the hough array needs to have 
      mAccumHeight = (int) (Math.sqrt(2) * Math.max(mHeight, mWidth)) / 2;
      // Double the height of the hough array to cope with negative r values 
      mDoubleHeight = 2 * mAccumHeight;
      mAccumulatorMatrix = new int[m_CountSteps][mDoubleHeight];

      // Find edge points and vote in array 
      centerX = mWidth / 2;
      centerY = mHeight / 2;

      mSinCache = new double[m_CountSteps];
      mCosCache = new double[m_CountSteps];

      for (int i = 0; i < m_CountSteps; i++)
      {
         //Save this to calculate it only once.
         double current = m_AlphaStep * (double) i;
         //Calculate each sin and cos once since it's so expensive
         mSinCache[i] = Math.sin(current);
         mCosCache[i] = Math.cos(current);
      }
   }

   public void setThreshold(int pThresh)
   {
      threshold = pThresh;
   }

   public int getThreshold()
   {
      return threshold;
   }

   public void GrayScale()
   {
      try
      {
         for (int i = 0; i < mHeight; i++)
         {
            for (int j = 0; j < mWidth; j++)
            {
               Color c = new Color(image.getRGB(j, i));
               int red = (int) (c.getRed() * 0.299);
               int green = (int) (c.getGreen() * 0.587);
               int blue = (int) (c.getBlue() * 0.114);
               Color newColor = new Color(red + green + blue,
                       red + green + blue, red + green + blue);
               image.setRGB(j, i, newColor.getRGB());
            }
         }
         File ouptut = new File("grayscale.gif");
         ImageIO.write(image, "gif", ouptut);
      } catch (IOException e)
      {
         System.out.println(e.getMessage() + "Failed to write greyscale");
      }
   }

   public double GetHoughAngle()
   {
      try
      {
//         System.err.println("Starting loop...");

         for (int y = 1; y < mHeight - 1; y++)
         {
            //System.out.println("Starting row #" + y + " of " + mHeight);
            for (int x = 0; x < mWidth; x++)
            {
               //Theoretically, a pixel whose lower or upper neighbor is dark  
               //is much more likely to be a baseline pixel than any random 
               //dark one.
               Color c1 = new Color(image.getRGB(x, y));
               Color c2 = new Color(image.getRGB(x, y + 1));
               Color c3 = new Color(image.getRGB(x, y - 1));

               if (isDark(c1) && (!isDark(c2) ^ !isDark(c3)))
               {
                  //System.out.println("Pixel (" + x + "," + y + ") is dark.");
                  for (int t = 0; t < m_CountSteps; t++)
                  {
                     int r = (int) (((x - centerX) * mCosCache[t]) + ((y - centerY) * mSinCache[t]));

                     // this copes with negative values of r 
                     r += mAccumHeight;

                     //System.out.println("Now incrementing matrix [" + t + "][" + r + "]");
                     if (r >= 0 && r < mDoubleHeight)
                     {
                        mAccumulatorMatrix[t][r]++;
                     }
                  }
                  mNumPoints++;
               }
            }
         }

//         System.err.println("Getting top 20");

         
         getTop20V1(20);
 //        getLines(20);
//         System.err.println("Got top 20");

         int count = 0;
         double skewAngle = 0.0;
         HashMap<Double,Integer> angleVotes = new HashMap<Double,Integer>();          

         while (count == 0 && threshold > 0)
         {            
            skewAngle = 0.0;
            count = 0;
            for (HoughCoordinates current : top20)
            {
               if (current.votes > 0 && current.votes > threshold)
               {  
                  /* //This doesn't work because a single erroneous point would throw the whole thing off
                  System.err.println("Adding " + current.getAngle() + " with " + current.votes + " votes to running total");
                  skewAngle += current.getAngle();
                  */
                  count++;
//                  current.draw(image, Color.CYAN.getRGB());
                  
                  if(angleVotes.containsKey(current.getAngle()))
                     angleVotes.put(current.getAngle(), 
                             (int) angleVotes.get(current.getAngle()) + 1);
                  else
                     angleVotes.put(current.getAngle(), 1);
               }
            }
            if (count == 0)
            {
               threshold = threshold - 70;
            }
            
         }

         writeImage("MyTransformed.png", image);
//         File ouptut = new File("MyTransformed.jpg");
//         ImageIO.write(image, "jpg", ouptut);

         if (count > 0)
         {
            Map.Entry<Double, Integer> highestVotedAngle = null;
            for(Map.Entry<Double, Integer> entry : angleVotes.entrySet()) {
               if(highestVotedAngle == null || entry.getValue() > highestVotedAngle.getValue())
                  highestVotedAngle = entry;
            }
            System.out.println("Angle == " + skewAngle / (double) count);
            System.out.println("Higest voted angle = " +
                    highestVotedAngle.getKey() + " With " + 
                    highestVotedAngle.getValue() + " votes.");
//            return ((skewAngle) / (double) count) - (.5 * Math.PI);
            return (highestVotedAngle.getKey() - (.5 * Math.PI));
         }

      } catch (Exception e)
      {
         System.out.println(e.getMessage() + " FailedAngle " + e.getLocalizedMessage());
      }
      return -300.0;
   }

   public static void writeImage(String filename, BufferedImage image)
   {
      File ouptut = new File(filename);
      try
      {
         ImageIO.write(image, "png", ouptut);
      } catch (IOException ex)
      {
         Logger.getLogger(Deskewer.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   public static Boolean isDark(Color c)
   {
      //return ((c.getRGB() & 0x000000ff) == 0);
      float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
      float brightness = hsb[2];
      return (brightness < 0.5);

   }

   public static double truncate(double value, int places)
   {
      double multiplier = Math.pow(10, places);
      return Math.floor(multiplier * value) / multiplier;
   }

   private void getTopN(int pNum)
   {

      /*
       HoughCoordinates[] bob = new HoughCoordinates[HoughArray.size()];
       Arrays.sort(HoughArray.toArray(bob), new VotesComparator());      
       top20.clear();
       for (int y = 0; y < pNum && y < bob.length; y++)
       {
       System.err.println("Now adding point with " + bob[y].votes + " Votes.");
       top20.add(bob[y]);
       }
       */
   }

   private Boolean getTop20V1(int pNum)
   {
      
      Boolean foundOne = false;
      
//      System.err.println("Within top 20");

      HoughCoordinates tmp = new HoughCoordinates();

      top20 = new HoughCoordinates[pNum];

      for (int index = 0; index < pNum; index++)
      {
         top20[index] = new HoughCoordinates();
      }

      for (int i = 0; i < m_CountSteps; i++)
      {
         loop:
         for (int j = mNeighbourhoodSize; j < mDoubleHeight; j++)
         {
//            System.err.print("Now testing for larger votes between: ");
//            System.err.print(mAccumulatorMatrix[i][j] + " and ");
//            System.err.println(top20[pNum - 1].votes);
//            System.out.println("Working on (" + i + "," + j + ")");

            if (mAccumulatorMatrix[i][j] > top20[pNum - 1].votes)
            {
               int peak = mAccumulatorMatrix[i][j];
                    // Check that this peak is indeed the local maxima 
                    for (int dx = -mNeighbourhoodSize; dx <= mNeighbourhoodSize; dx++) { 
                        for (int dy = -mNeighbourhoodSize; dy <= mNeighbourhoodSize; dy++) { 
                            int dt = i + dx; 
                            int dr = j + dy; 
                            if (dt < 0) dt = dt + m_CountSteps; 
                            else if (dt >= m_CountSteps) dt = dt - m_CountSteps; 
                            if (mAccumulatorMatrix[dt][dr] > peak) { 
                                // found a bigger point nearby, skip 
                                continue loop; 
                            } 
                        } 
                    } 
                    foundOne = true;
//               System.err.println("Found a better one!");

               top20[pNum - 1].setAngle(m_AlphaStep * i);
               top20[pNum - 1].setAngleRef(i);
               top20[pNum - 1].setDistance(j);
               top20[pNum - 1].votes = mAccumulatorMatrix[i][j];
               int k = pNum - 1;
//               System.err.println("Entering While");

               while (k > 0 && top20[k].votes > top20[k - 1].votes)
               {
                  tmp = top20[k];
                  top20[k] = top20[k - 1];
                  top20[k - 1] = tmp;
                  k -= 1;
//                  System.err.println("Within while: K == " + k);
               }
//               System.out.println("Working on (" + y + "," + x + ")");
            }
         }
      }
      return foundOne;
   }

   /**
    * An implementation of the Comparator interface. Used to sort the couples in
    * order based on their couple ID number
    */
   class VotesComparator
           implements Comparator<HoughCoordinates>
   {

      /**
       * Compares the rank of two couples to determine which had the better
       * (smaller) final placement. A negative number indicates that the first
       * point is better than the other, positive numbers indicate that second
       * point is placed better. Zero means they are the same.
       *
       * @param a The first point to be compared
       * @param b The second point to be compared
       *
       * @return an integer indicating which point is greater
       */
      @Override
      public int compare(HoughCoordinates a, HoughCoordinates b)
      {
         return b.votes - a.votes;
      }
   }
}
