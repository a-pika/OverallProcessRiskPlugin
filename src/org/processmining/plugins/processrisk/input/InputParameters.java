package org.processmining.plugins.processrisk.input;

import java.util.Hashtable;
import java.util.Vector;

import org.processmining.plugins.processrisk.analyse.GuardCosts;

public class InputParameters{
	
	//Risk costs
	public Hashtable<String,Double> moveOnModelCosts = new Hashtable<String,Double>(); 
	public Hashtable<String,Double> moveOnLogCosts = new Hashtable<String,Double>();  
	public Vector<GuardCosts> guardCosts = new Vector<GuardCosts>();
	public Hashtable<String, Double> varNotWrittenCosts = new Hashtable<String, Double>();
	public Hashtable<String,String> varTypes = new Hashtable<String,String>();
	
	public Double moveBothCost; // default value
	public int decNum; // number of decimals after point in float
	
	// process outcome parameters
	public String taskPrevCaseAttr;
	public String outcomeattribute;
	public Double outcomevalue;
	public String outcomeAggregateMethod;
	
		
	//time series parameters
	public long startTime;
	public long slotSize;
	public int numberOfSlots;
	
	//process risk parameters
	public long timeUnit; // for event log pre-processing - time from case start
	public String tsCaseMethod; // which cases will be used in TS slot
	public boolean truncate; //true - truncate cases at t2, false - do not truncate
	public boolean preprocess; // true - preprocess  log (add task+time,resource,type), false - do no pre-process
	public String riskMethod; //total vs. mean
	public String meanMethod;
	public boolean getCP;
	
	// change point parameters
	public String cpmType;
	public String ARL0;
	public String startup;
	
	
	//prev. versions
	public String method;
	public String period;
	public boolean excludeTSZeros;
	public Double normalCaseDuration;
	public String prevCaseAttr;
		

	
	public InputParameters() {
	
		moveBothCost = 0.0; //cost of synchronous move between the log and the model
		decNum = 100; //2 decimals after point
		
			
			
	} 
	

	
}




