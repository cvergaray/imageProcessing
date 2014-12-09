/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imageprocessing.rotate;

import imageprocessing.deskew.Deskewer;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 *
 * @author cave
 */
public class ImageRotator
{

   private static int newX;
   private static int newY;

   public static void main(String[] args)
   {
      BufferedImage image = null;
      File input;
      try
      {
         input = new File("/Users/cave/Pictures/057.JPG");
         image = ImageIO.read(input);
      } catch (Exception e)
      {
         System.out.println(e.getMessage() + "Failed top open file");
         System.exit(-1);
      }
      
      image = rotateRad(image, .1 * Math.PI);//1.8);
      
      Deskewer.writeImage("rotateTest.jpg", image);
   }

   public static BufferedImage rotateRad(BufferedImage pImage, Double pRad)
   {

      calcNewCanvasSize(pImage.getWidth(), pImage.getHeight(), pRad);

      
      BufferedImage newImage = new BufferedImage(newX, newY, pImage.getType());
      Graphics2D g2 = newImage.createGraphics();
      g2.setColor(Color.WHITE);
      g2.fillRect(0, 0, newX, newY);
      g2.rotate(pRad, newX / 2, newY / 2);
      //The new dimentions include a border, so we will print the image just
      //within the new dimentions.
      g2.drawImage(pImage, null, (newX - pImage.getWidth()) / 2, (newY - pImage.getHeight()) / 2);
      return newImage;

      //The Affine Transformation allows transformation of an image while 
      //preserving affinity between points, such as parallelity.
//      AffineTransform tx = new AffineTransform();
      //A rotation is performed around a fixed point. In this case, the center
      //of the image.
//      tx.rotate(pRad, pImage.getWidth() / 2, pImage.getHeight() / 2);
      //Now we create the operator to actually perform the transformation.
//      AffineTransformOp op = new AffineTransformOp(tx,
//              AffineTransformOp.TYPE_BILINEAR);
      //And then perform the transformation itself.
//      pImage = op.filter(pImage, null);
      //Returning the rotated image.
//      return pImage;
   }

   /**
    * Calculate New Canvas Size.
    * Calculates the size of an image required to still hold an image that is 
    * oldX by oldY pixels large after being rotated by angle radians. This makes
    * it possible to rotate images without losing any data.
    * This function didn't work until I realized that I was calculating the new
    * values backwards. Or, that is to say, I thought that the x was the y and
    * vice-versa. I found that out by trying to rotate a photo and comparing the
    * differences in dimensions. The new dimensions would have held the image
    * if they were flipped, so I tried that and it worked perfectly.
    * Then, I realized I was not doing a true rotation because it would alter
    * the angle when performing the rotation, so when I fixed that, I had to put
    * this back to how it was.
    * @param oldX
    * @param oldY
    * @param angle 
    */
   private static void calcNewCanvasSize(int oldX, int oldY, double angle)
   {

      //By calculating these only once, I can save on some computation.
      double cosAngle = Math.cos(angle);
      double sinAngle = Math.sin(angle);
      newX = (int) (Math.ceil(Math.abs(oldX * cosAngle)) + Math.ceil(Math.abs(oldY * sinAngle)));
      newY = (int) (Math.ceil(Math.abs(oldX * sinAngle)) + Math.ceil(Math.abs(oldY * cosAngle)));
   }
}
