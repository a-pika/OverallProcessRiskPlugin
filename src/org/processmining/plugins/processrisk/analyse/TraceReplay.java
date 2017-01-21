package org.processmining.plugins.processrisk.analyse;

import java.util.Date;





public class TraceReplay{
	 
	public String caseID;
	public Date caseStart;
	public Date caseEnd;
	public Double fitness; // combined fitness from data-aware replay
	
	
	 
	public TraceReplay(String cid, Date start, Date end) {
		
		caseID = cid;
		caseStart = start;
		caseEnd = end;
		fitness = 1.00; // default fitness
					       
	    } 
	
	public void setFitness (Double fit)
	{
		fitness = fit;	
	}


	
}




