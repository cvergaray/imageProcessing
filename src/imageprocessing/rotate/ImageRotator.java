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

   static int newX;
   static int newY;

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
      
      image = rotateRad(image, 1.8);
      
      Deskewer.writeImage("rotateTest.jpg", image);
   }

   public static BufferedImage rotateRad(BufferedImage pImage, Double pRad)
   {

      calcNewCanvasSize(pImage.getWidth(), pImage.getHeight(), pRad);

      
      BufferedImage newImage = new BufferedImage(newX, newY, pImage.getType());
      Graphics2D g2 = newImage.createGraphics();
      g2.setColor(Color.WHITE);
      g2.fillRect(0, 0, newX, newY);
      g2.rotate(-pRad + (.5 * Math.PI), newX / 2, newY / 2);
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

   private static void calcNewCanvasSize(int oldX, int oldY, double angle)
   {

      double cosAngle = Math.cos(angle);
      double sinAngle = Math.sin(angle);
      newY = (int) (Math.ceil(Math.abs(oldX * cosAngle)) + Math.ceil(Math.abs(oldY * sinAngle)));
      newX = (int) (Math.ceil(Math.abs(oldX * sinAngle)) + Math.ceil(Math.abs(oldY * cosAngle)));
   }
}
