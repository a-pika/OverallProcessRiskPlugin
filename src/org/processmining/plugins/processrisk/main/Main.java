package org.processmining.plugins.processrisk.main;

import org.processmining.plugins.processrisk.analyse.RFunctions;
import org.processmining.plugins.processrisk.analyse.XLogFunctions;
import org.processmining.plugins.processrisk.input.GetUserInput;
import org.processmining.plugins.processrisk.input.InputParameters;
import org.processmining.plugins.processrisk.output.VisualiseResults;


public class Main {
	
	static XLogFunctions logFun = new XLogFunctions();
	static InputParameters ip = new InputParameters();
	static RFunctions RFun = new RFunctions();
	static VisualiseResults vis = new VisualiseResults();
	static GetUserInput gui = new GetUserInput();
		
	public static void main (String args[]) throws Exception {
		
		//for testing
		
		//Hashtable<String,Double> costs = new Hashtable<String,Double>(); 
		//costs = gui.getCost(costs);
		
		//System.out.println(MVEL.eval("(\"High\" != \"High1\") && ((3.2 < (0.0 + 4.0)) || (false && true ))"));
		//System.out.println(MVEL.eval("(\"High\" != \"High1\")"));
		//System.out.println(MVEL.eval("(\"High\" == \"High\")"));
		//System.out.println(MVEL.eval("(High == High)"));
		
	}


	
}
