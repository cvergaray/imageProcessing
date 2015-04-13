package imageprocessing;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cave
 */
public class Stopwatch
{
   private long elapsedExecutionTime = 0;
   private int countExecutions = 0;
   private long executionTime;
   private long startTime;
   private long endTime;
   
   public void resetTimer(){
      executionTime = 0;
   };
   
   public void start(){
      startTime = System.currentTimeMillis();
   }
   
   public long stop(){
      endTime = System.currentTimeMillis();
      executionTime = endTime - startTime;
      elapsedExecutionTime += executionTime;
      countExecutions++;
      return executionTime;
   }
   
   public void displayStats(){
      
      System.out.print("Last Run Time: ");
      System.out.println(this.getTotalTimeString());
      
   }
   
   public String getTotalTimeString(){
      return getTotalTimeString(0);
   }
           
   
   public String getTotalTimeString(int code)
   {
      long timeInQuestion = 0;
      switch(code){
         case 1:
            timeInQuestion = elapsedExecutionTime;
            break;   
         case 2:
            timeInQuestion = elapsedExecutionTime / countExecutions;
            break;         
         default:
            timeInQuestion = executionTime;
            break;
      }
      
      String results = "";
      long minutes = timeInQuestion / 60000;
      long seconds = timeInQuestion / 1000;
      seconds %= 60;
      long milliseconds = timeInQuestion % 1000;
      if(minutes > 0)
         results += (minutes + "(m) ");
      if(seconds > 0)
         results += (seconds + "(s) ");
      if(milliseconds > 0) 
         results += (milliseconds + "(ms) ");

      return results;
   }
   
   public void stopAndDisplay(){
      this.stop();
      this.displayStats();
   }
   
}
