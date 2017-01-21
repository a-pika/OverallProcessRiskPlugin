package org.processmining.plugins.processrisk.analyse;

import java.util.Date;
import java.util.Vector;


public class TSslot{
	 
	
	public Date slotStart;
	public Date slotEnd;
	public Vector<String> cases;
	public Vector<String> tsCases; //tsID+_+caseID
	public Vector<Double> fitnesses;
	public Vector<Double> outcomes;
	public Vector<Double> caseDurationsEndOfSlot;
	public Double meanFitness; // combined fitness from data-aware replay
	public Double aggregateOutcome; 
	
	
	
	 
	public TSslot(Date start, Date end) {
		
		slotStart = start;
		slotEnd = end;
		meanFitness = 1.00; // default fitness
		aggregateOutcome = 0.0;
		cases = new Vector<String>();
		tsCases = new Vector<String>();
		fitnesses = new Vector<Double>();
		outcomes = new Vector<Double>();
		caseDurationsEndOfSlot = new Vector<Double>();	
	    } 
	
}




