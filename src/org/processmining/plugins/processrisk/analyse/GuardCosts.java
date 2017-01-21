package org.processmining.plugins.processrisk.analyse;

import java.util.Vector;


public class GuardCosts{
	 
	public String task;
	public String mainGuard; //from Petri net
	public Double mainGuardCost;
	public Vector<String> guards = new Vector<String>(); //user input if they wish to assign different costs to different parts of guards
	public Vector<Double> costs = new Vector<Double>();
		 
	public GuardCosts() { } 
	
	public GuardCosts(String t, String mg, Double mgc) 
	{
	task = t;
	mainGuard = mg;
	mainGuardCost = mgc;
	} 
	
}




