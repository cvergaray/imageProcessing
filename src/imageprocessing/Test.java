
import imageprocessing.deskew.Deskewer;
import imageprocessing.despeckle.Despeckler;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Test extends JPanel
{

   public void paint(Graphics g)
   {
      Image img = createImageWithText();
      g.drawImage(img, 20, 20, this);
   }

   private Image createImageWithText()
   {
      BufferedImage bufferedImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
      Graphics g = bufferedImage.getGraphics();

      g.drawString("www.tutorialspoint.com", 20, 20);
      g.drawString("www.tutorialspoint.com", 20, 40);
      g.drawString("www.tutorialspoint.com", 20, 60);
      g.drawString("www.tutorialspoint.com", 20, 80);
      g.drawString("www.tutorialspoint.com", 20, 100);
      return bufferedImage;
   }

   public static void main(String[] args)
   {
      /*
       JFrame frame = new JFrame();
       frame.getContentPane().add(new Test());

       frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       frame.setSize(200, 200);
       frame.setVisible(true);
       */

      Deskewer DesQ = new Deskewer("gjvg.png");

      BufferedImage image = DesQ.getImage();

      image = Despeckler.threshold(image, .5);

      Deskewer.writeImage("MononiBW.png", image);

      DesQ.initWithImage(image);

//      System.out.println("Black: " + Color.BLACK.getRGB());
//      System.out.println("Brightness: " + Color.BLACK.getRGB());
      int top = 0;
      List<Point> myPoints = new ArrayList<Point>();
      for (int x = 0; x < image.getWidth(); x++)
      {
         for (int y = 0; y < image.getHeight(); y++)
         {
            //System.out.println("(" + x + "," + y + ") : " + image.getRGB(x, y));
            if (Deskewer.isDark(new Color(image.getRGB(x, y))))
            {
               top = y;
               while (y < image.getHeight() && Deskewer.isDark(new Color(image.getRGB(x, y))))
               {
                  y++;
               }
               myPoints.add(new Point(x + 1, top, y));
            }
         }
      }
      /*int choice = 0;
      int lastPage = 0;
      for (int i = 0; i < myPoints.size() - 1; i++)
      {
         if (myPoints.get(i).getPageNum() == myPoints.get(i + 1).getPageNum())
         {
            int count = 1;
            int j = i + 1;
            while (myPoints.get(i).getPageNum() == myPoints.get(j).getPageNum())
            {
               count++;
               j++;
            }
            choice = (choice + 1) % count;            
         }
         i += choice;
               System.out.println(myPoints.get(i).toString());

              */
      for(Point myPoint : myPoints)
      {
         System.out.println(myPoint.toString());
      }
   }

}

class Point
{

   int columnNumber;
   float start;
   float end;

   public Point(int colNum, int start, int end)
   {
      columnNumber = colNum;
      setStart(start);
      setEnd(end);
   }

   public void setStart(int startPixel)
   {
      start = (float) startPixel / 100;
   }

   public void setEnd(int endPixel)
   {
      end = (float) endPixel / 100;
   }

   public int getPageNum()
   {
      return (2 * columnNumber) - 1;
   }

   @Override
   public String toString()
   {
      return String.format("[%4d] : %5.2f cm | %5.2f cm", getPageNum(), start, end);
   }
}
