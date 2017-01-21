package org.processmining.plugins.processrisk.main;
import javax.swing.JOptionPane;



public class ExceptionHandler implements Thread.UncaughtExceptionHandler{
	 
	@Override  public void uncaughtException(Thread aThread, Throwable aThrowable) {
	   
	    JOptionPane.showMessageDialog(
	     null, "Error: " + aThrowable.toString(), 
	      "Error", JOptionPane.ERROR_MESSAGE
	    );
	  }
	  
	  
}




