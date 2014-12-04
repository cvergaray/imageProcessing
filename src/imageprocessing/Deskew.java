/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package imageprocessing;
import java.util.ArrayList;
import java.lang.Math;

/**
 *
 * @author cave
 *
public class Deskew
{
   private int m_width;
   private int m_height;
   private int m_buf;
   private int m_cDMin;
   private double m_cAlphaStart;
   private double m_cAlphaStep;
   private int m_cSteps;
   private double m_cDStep;
   private int [] m_cHMatrix;
   private int m_cHMatrixSize;
   
   public Deskew() {       
      m_cAlphaStart = -20.0;
      m_cAlphaStep = 0.2;
      m_cSteps = (40 * 5);
      m_cDStep = 1.0;
   }
   
   public double GetSkewAngle() {
      int count = 20;
      double sum = 0.0;
      ArrayList<HoughLine> top;

      // Calculate Hough transform

      Calc();

      // Get the best 20 results

      top = GetTop(count);

      if (top.isEmpty()) {
         return 0.0;
      }

   // Return the average of the best

   for (int i = 0; i < count; i++) {
      sum += top.get(i).GetAlpha();
      System.out.println("Best " + i + " is " + top.get(i).GetAlpha());
   }
  
   System.out.println("Sum is " + sum);
   top.clear();
   double ret = (sum / count);
   System.out.println("ret is " + ret);
   return ret;
}

public ArrayList<HoughLine> GetTop(int count)
{
   HoughLine[] hl;
   HoughLine[] tmp;
   int dIndex;
   int alphaIndex;


   for (int i = 0; i < m_cHMatrixSize; i++) {
      if (m_cHMatrix[i] > hl[count - 1]->GetCount()) {
         hl[count - 1]->SetCount(m_cHMatrix[i]);
         hl[count - 1]->SetIndex(i);
         int j = count - 1;
         while (j > 0 && hl[j]->GetCount() > hl[j - 1]->GetCount()) {
            tmp = hl[j];
            hl[j] = hl[j - 1];
            hl[j - 1] = tmp;
            j -= 1;
         }
      }
   }

   for (int i = 0; i < count; i++) {
      dIndex = hl[i]->GetIndex() / m_cSteps;
      alphaIndex = hl[i]->GetIndex() - dIndex * m_cSteps;
      hl[i]->SetAlpha(GetAlpha(alphaIndex));
      hl[i]->SetD(dIndex + m_cDMin);
   }
   return hl;
}

   private void GetAlpha()
   {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
   }

public void Calc()
{
   int hMin = m_height / 4;
   int hMax = m_height * 3 / 4;

   for (int y = hMin; y <= hMax; y++) {
      for (int x = 1; x < m_width - 1; x++) {
         if (IsBlack(x, y)) {
            if (!IsBlack(x, y + 1)) {
               Calc(x, y);
            }
         }
      }
   }
}

public void Calc(int x, int y)
{
   double d;
   int dIndex;
   int index;

   for (int alpha = 0; alpha < m_cSteps; alpha++) {
      double rads = GetAlpha(alpha) * Math.PI / 180.0;
      d = y * Math.cos(rads) - x * Math.sin(rads);
      dIndex = CalcDIndex(d);
      index = dIndex * m_cSteps + alpha;
      m_cHMatrix[index]++; 
   }
}

public boolean IsBlack(int x, int y)
{
   //luminance = (c.R * 0.299) + (c.G * 0.587) + (c.B * 0.114)
   if((m_buf + y * m_width + x) < 140)
   return true;
   else
      return false;
}

public double GetAlpha(int index)
{
   return m_cAlphaStart + index * m_cAlphaStep;
}

public int CalcDIndex(double d)
{
   return (int) (d - m_cDMin);
}
   
}
*/