/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.deskew;

import java.awt.image.BufferedImage;

/**
 *
 * @author cave
 */
public class HoughCoordinates
{

   public HoughCoordinates(int pVotes, int pAngle, int pDistance)
   {
      this.votes = pVotes;
      this.mAngleRef = pAngle;
      this.mDistance = pDistance;
   }

   public HoughCoordinates(int pVotes, double pAngle, int pDistance)
   {
      this.votes = pVotes;
      this.mAngle = pAngle;
      this.mDistance = pDistance;
   }

   public HoughCoordinates()
   {
      this.votes = -1;
      this.mAngle = -1;
      this.mDistance = -1;
      this.mAngleRef = 0;
   }

   public void setAngle(double pAngle)
   {
      mAngle = pAngle;
   }

   public void setAngleRef(int pAngle)
   {
      mAngleRef = pAngle;
   }

   public void setDistance(int pDistance)
   {
      mDistance = pDistance;
   }

   public double getDistance()
   {
      return mDistance;
   }

   public double getAngle()
   {
      return mAngle;
   }

   public double getAR()
   {
      return mAngleRef;
   }

   public int votes;
   private double mAngle;
   private double mDistance;
   private int mAngleRef;

   public boolean equals(Object other)
   {
      if (other == null)
      {
         return false;
      }

      if (this.getClass() != other.getClass())
      {
         return false;
      }

      if (this.mAngle != ((HoughCoordinates) other).mAngle)
      {
         return false;
      }

      if (this.mAngleRef != ((HoughCoordinates) other).mAngleRef)
      {
         return false;
      }
      if (this.mDistance == ((HoughCoordinates) other).mDistance)
      {
         return true;
      }
      return false;
   }
   
       /** 
     * Draws the line on the image of your choice with the RGB color of your choice. 
     */ 
    public void draw(BufferedImage image, int color) { 
 
        int height = image.getHeight(); 
        int width = image.getWidth(); 
 
        // During processing h_h is doubled so that -ve r values 
        int houghHeight = (int) (Math.sqrt(2) * Math.max(height, width)) / 2; 
 
        // Find edge points and vote in array 
        float centerX = width / 2; 
        float centerY = height / 2; 
 
        // Draw edges in output array 
        double tsin = Math.sin(getAngle()); 
        double tcos = Math.cos(getAngle()); 
 
        if (getAngle() < Math.PI * 0.25 || getAngle() > Math.PI * 0.75) { 
            // Draw vertical-ish lines 
            for (int y = 0; y < height; y++) { 
                int x = (int) ((((mDistance - houghHeight) - ((y - centerY) * tsin)) / tcos) + centerX); 
                if (x < width && x >= 0) { 
                    image.setRGB(x, y, color); 
                } 
            } 
        } else { 
            // Draw horizontal-sh lines 
            for (int x = 0; x < width; x++) { 
                int y = (int) ((((mDistance - houghHeight) - ((x - centerX) * tcos)) / tsin) + centerY); 
                if (y < height && y >= 0) { 
                    image.setRGB(x, y, color); 
                } 
            } 
        } 
    }
}
