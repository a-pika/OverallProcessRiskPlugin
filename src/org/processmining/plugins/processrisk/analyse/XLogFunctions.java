package org.processmining.plugins.processrisk.analyse;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.mvel2.MVEL;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.plugins.DataConformance.Alignment;
import org.processmining.plugins.DataConformance.DataAlignment.PetriNet.DataAlignment;
import org.processmining.plugins.DataConformance.DataAlignment.PetriNet.ResultReplayPetriNetWithData;
import org.processmining.plugins.DataConformance.framework.ExecutionStep;
import org.processmining.plugins.processrisk.input.InputParameters;

public class XLogFunctions{


//getting variable types
@SuppressWarnings("rawtypes")
public Hashtable <String,String> getVarTypes(PetriNetWithData net, Hashtable<String,String> varTypes)	
{
	
	Collection <Transition> trans = net.getTransitions();
	Iterator it = trans.iterator();
	while(it.hasNext())
	{
		DirectedGraphNode node = (DirectedGraphNode) it.next();
		PNWDTransition transition = (PNWDTransition) node;
		Set<DataElement> wo = transition.getWriteOperations();
		Iterator it3 = wo.iterator();
		
		while(it3.hasNext())
		{
			DataElement curVar = (DataElement) it3.next();
			String varName = curVar.getVarName();
			String varType = curVar.getType().getCanonicalName();
			varTypes.put(varName, varType);
		
		}
			
	}

	//System.out.println(varTypes);

return varTypes;
}


public Vector<TSslot> replayLogNew(UIPluginContext context, PetriNetWithData net, XLog log, Vector<TSslot> ts, Hashtable<String, Double> MoM, Hashtable<String, Double> MoL, Double MB, Vector<GuardCosts> guardCosts, Hashtable<String, Double> VnW,Hashtable<String, String> VT, InputParameters ip)
{
	
	DataAlignment da = new DataAlignment();
	
	ResultReplayPetriNetWithData rp = da.plugin(context, net, log);
	
	Iterator<Alignment> iterator = rp.labelStepArray.iterator();
	//System.out.println("Variable Mapping:" + rp.getVariableMapping().toString());
	//System.out.println("labelStepArray:" + rp.labelStepArray.toString());
	
	while (iterator.hasNext()) 	
	{
	Alignment nextAlignment = iterator.next();	
	
	//System.out.println("Process trace:" + nextAlignment.getProcessTrace().toString());
	//System.out.println("Log trace:" + nextAlignment.getLogTrace().toString());
	//System.out.println("Step labels:" +nextAlignment.getStepLabels().toString());
	//System.out.println("Step types:" +nextAlignment.getStepTypes().toString());
	//System.out.println("Fitness:" +nextAlignment.getFitness());
			
	String nextTScaseID = nextAlignment.getTraceName();
	//System.out.println(nextTScaseID);
	//Double nextCaseFitness = (double) nextAlignment.getFitness(); //prev.
	Double nextCaseFitness = getCaseRisk(nextAlignment, MoM, MoL, MB, guardCosts,VnW,VT);
	//System.out.println("Trace---risk:" +nextAlignment.getTraceName()+"---"+nextCaseFitness);

	//System.out.println(nextCaseFitness);
	int tsID = Integer.parseInt((nextTScaseID.split("_"))[0]);
	//System.out.println(tsID);
	int index = ts.elementAt(tsID).tsCases.indexOf(nextTScaseID);
	//System.out.println(index);
	ts.elementAt(tsID).fitnesses.set(index, nextCaseFitness);
	}

	//get mean fitness for each slot
	
	for (int i=0; i<ts.size(); i++)
	{
		Double tsPointMean = getMean(ts.elementAt(i).fitnesses, ip);
		//tsPointMean = (double)Math.round(tsPointMean * ip.decNum) / ip.decNum;
		ts.elementAt(i).meanFitness = tsPointMean;
	}
	
	return ts;
}

public Double getCaseRisk(Alignment al, Hashtable<String, Double> MoM, Hashtable<String, Double> MoL, Double MB, Vector<GuardCosts> guardCosts, Hashtable<String, Double> VnW, Hashtable<String, String> VT)
{
	Double risk = 0.0;
	Double VarNotWrittenRisk = 0.0;
	Double guardFalseRisk = 0.0;
	
	List<ExecutionStep> prSteps = al.getProcessTrace();
	List<ExecutionStep> logSteps = al.getLogTrace();
	Vector<String> prTasks = new Vector<String>();
	Vector<String> logTasks = new Vector<String>();
	Hashtable<String, Object> varValuesBefore = new Hashtable<String, Object>();
	
	
	for(int i=0; i<prSteps.size(); i++)
	{
		String prTask = prSteps.get(i).getActivity();
		if(prTask!=null) {prTasks.add(prTask);} else {prTasks.add("BOTTOM-STEP");};
		String logTask = logSteps.get(i).getActivity();
		if(logTask!=null) {logTasks.add(logTask);} else {logTasks.add("BOTTOM-STEP");};
				
		//System.out.println("Process task: "+prTask);
		//System.out.println("Log task: "+logTask);
		
		
		Vector<String> prVars = new Vector <String>();
		prVars.addAll(prSteps.get(i).keySet());		
		//System.out.println(prVars);
		Vector<Object> prValues = new Vector <Object>();
		prValues.addAll(prSteps.get(i).values());		
		//System.out.println(prValues);
		Hashtable<String, Object> prVarValues = new Hashtable<String, Object>();
	
		for(int z=0; z<prVars.size(); z++)
		{
			prVarValues.put(prVars.elementAt(z), prValues.elementAt(z));
		}
				
		Vector<String> logVars = new Vector <String>();
		logVars.addAll(logSteps.get(i).keySet());		
		//System.out.println(logVars);
		Vector<Object> logValues = new Vector <Object>();
		logValues.addAll(logSteps.get(i).values());		
		//System.out.println(logValues);
		Hashtable<String, Object> logVarValues = new Hashtable<String, Object>();
		
		for(int z=0; z<logVars.size(); z++)
		{
			logVarValues.put(logVars.elementAt(z), logValues.elementAt(z));
		}
	
		
		//checking if all vars are written
		
		for(int j=0; j<prVars.size(); j++)
		{
			String nextVar = prVars.elementAt(j);
			String taskVar = prTask+"-"+nextVar;
			Double taskVarCost = VnW.get(taskVar);
			
			if (logTask!=null)
			{
				if(!(logVars.contains(nextVar)))
				{
				VarNotWrittenRisk += taskVarCost;
				}	
			}
		}
		
	
		
		//Evaluating transition guard
		
		if(prTask != null && logTask != null)
		{
		int index = -1;
		for(int z=0; z<guardCosts.size(); z++)
		{
			if(guardCosts.elementAt(z).task.equals(prTask))
			{
				index = z;
			}
		}
		//in prev version - getting main guard from guards Hashtable	
		String theGuard = guardCosts.elementAt(index).mainGuard;

		
//getting guard false cost if there is only main guard 
if((index > -1) && (guardCosts.elementAt(index).guards.isEmpty()) && (!(theGuard.equals("NO-GUARD"))))
{
				
				Double mgc =  guardCosts.elementAt(index).mainGuardCost;
				Vector <String> usedVars = new Vector<String>();
				Vector <String> usedCurVars = new Vector<String>();
				Vector <String> usedPrevVars = new Vector<String>();
				
				Vector<String> allvars = new Vector<String>();
				allvars.addAll(VT.keySet());
				
			//getting used vars and used current vars
				for(int z=0; z<allvars.size(); z++)
				{
					if(theGuard.contains(allvars.elementAt(z)))
					{
						usedVars.add(allvars.elementAt(z));
					}
					
					if(theGuard.contains(allvars.elementAt(z)+"'"))
					{
						usedCurVars.add(allvars.elementAt(z));
					}
				}
			
				
			for (int z=0; z<usedVars.size(); z++)
			{
				if(!(usedCurVars.contains(usedVars.elementAt(z))))
				{
					usedPrevVars.add(usedVars.elementAt(z));
				}
			}
			
			//System.out.println("usedVars: "+usedVars);
			//System.out.println("usedCurVars: "+usedCurVars);
			//System.out.println("usedPrevVars: "+usedPrevVars);
			
			
			for (int z=0; z<usedPrevVars.size(); z++)
			{
				String curVar = usedPrevVars.elementAt(z);
				String curVarType = VT.get(curVar);
				String curVarValue = varValuesBefore.get(curVar).toString();
				//System.out.println("curVarValue before: "+curVarValue);
				if(curVarType.equals("java.lang.String"))
				{
					curVarValue = "\""+curVarValue+"\"";
				}
				//System.out.println("curVarValue after: "+curVarValue);
				//System.out.println("guardBefore: "+theGuard);
				theGuard = theGuard.replaceAll(curVar, curVarValue);
				
				//System.out.println("guardAfter: "+theGuard);
				
				
			}
			
			for (int z=0; z<usedCurVars.size(); z++)
			{
				String curVar = usedCurVars.elementAt(z);
				String curVarType = VT.get(curVar);
				String curVarValue = logVarValues.get(curVar).toString();
				//System.out.println("curVarValue before (current): "+curVarValue);
				if(curVarType.equals("java.lang.String"))
				{
					curVarValue = "\""+curVarValue+"\"";
				}
				//System.out.println("curVarValue after (current): "+curVarValue);
				//System.out.println("guardBefore (current): "+theGuard);
				
				theGuard = theGuard.replaceAll(curVar+"'", curVarValue);
				
				//System.out.println("guardAfter (current): "+theGuard);
				
				
			}
			
			//evaluating the guard
			
			String guardEval = MVEL.eval(theGuard).toString();
			//System.out.println("guardEval: "+guardEval);
			if(guardEval.equals("false"))
			{
				guardFalseRisk += mgc;
			}

}


//getting guard false costs if a few guards are defined for a transition
if((index > -1) && (!(guardCosts.elementAt(index).guards.isEmpty())) && (!(theGuard.equals("NO-GUARD"))))
{
	for(int y=0; y<guardCosts.elementAt(index).guards.size(); y++)
	{			
				theGuard = guardCosts.elementAt(index).guards.elementAt(y);
				Double gc =  guardCosts.elementAt(index).costs.elementAt(y);
				Vector <String> usedVars = new Vector<String>();
				Vector <String> usedCurVars = new Vector<String>();
				Vector <String> usedPrevVars = new Vector<String>();
				
				Vector<String> allvars = new Vector<String>();
				allvars.addAll(VT.keySet());
				
			//getting used vars and used current vars
				for(int z=0; z<allvars.size(); z++)
				{
					if(theGuard.contains(allvars.elementAt(z)))
					{
						usedVars.add(allvars.elementAt(z));
					}
					
					if(theGuard.contains(allvars.elementAt(z)+"'"))
					{
						usedCurVars.add(allvars.elementAt(z));
					}
				}
			
				
			for (int z=0; z<usedVars.size(); z++)
			{
				if(!(usedCurVars.contains(usedVars.elementAt(z))))
				{
					usedPrevVars.add(usedVars.elementAt(z));
				}
			}
			
		//	System.out.println("usedVars: "+usedVars);
		//	System.out.println("usedCurVars: "+usedCurVars);
		//	System.out.println("usedPrevVars: "+usedPrevVars);
			
			
			for (int z=0; z<usedPrevVars.size(); z++)
			{
				String curVar = usedPrevVars.elementAt(z);
				String curVarType = VT.get(curVar);
				String curVarValue = varValuesBefore.get(curVar).toString();
			//	System.out.println("curVarValue before: "+curVarValue);
				if(curVarType.equals("java.lang.String"))
				{
					curVarValue = "\""+curVarValue+"\"";
				}
		//		System.out.println("curVarValue after: "+curVarValue);
		//		System.out.println("guardBefore: "+theGuard);
				theGuard = theGuard.replaceAll(curVar, curVarValue);
				
		//		System.out.println("guardAfter: "+theGuard);
				
				
			}
			
			for (int z=0; z<usedCurVars.size(); z++)
			{
				String curVar = usedCurVars.elementAt(z);
				String curVarType = VT.get(curVar);
				String curVarValue = logVarValues.get(curVar).toString();
		//		System.out.println("curVarValue before (current): "+curVarValue);
				if(curVarType.equals("java.lang.String"))
				{
					curVarValue = "\""+curVarValue+"\"";
				}
		//		System.out.println("curVarValue after (current): "+curVarValue);
		//		System.out.println("guardBefore (current): "+theGuard);
				
				theGuard = theGuard.replaceAll(curVar+"'", curVarValue);
				
		//		System.out.println("guardAfter (current): "+theGuard);
				
				
			}
			
			//evaluating the guard
			
			String guardEval = MVEL.eval(theGuard).toString();
		//	System.out.println("guardEval: "+guardEval);
			if(guardEval.equals("false"))
			{
				guardFalseRisk += gc;
			}

}
}

		
		}
		
		
		
		
		
		//updating the values of variables written
		
	for(int z=0; z<logVars.size(); z++)
	{
		String curLogVar = logVars.elementAt(z);
		//if(varValuesBefore.containsKey(curLogVar))
		//{
			varValuesBefore.put(curLogVar, logVarValues.get(curLogVar));
		//}
	}
		
		//System.out.println("varValuesBefore: "+varValuesBefore);
		//System.out.println(logSteps.get(i).values());
	}
	
	//System.out.println("Process tasks:" + prTasks);
	//System.out.println("Log tasks:" +logTasks);
	
	//getting control-flow risk
	for(int i=0; i<prTasks.size(); i++)
	{
		String modelMove = prTasks.elementAt(i);
		String logMove = logTasks.elementAt(i);
		if(modelMove.equals(logMove)){risk+=MB;}
		else 
		{
			if (modelMove.equals("BOTTOM-STEP")){risk+=MoL.get(logMove);}
			else
			{
				if (logMove.equals("BOTTOM-STEP")){risk+=MoM.get(modelMove);}
				
			};
		}
	}
	//System.out.println("VarNotWrittenRisk: "+VarNotWrittenRisk);	
	
	//System.out.println("guardFalseRisk: "+guardFalseRisk);	
	
	//System.out.println("controlFlowRisk: "+risk);	
	
	risk += (VarNotWrittenRisk + guardFalseRisk);
	
	//System.out.println("totalRisk: "+risk);	
	
	return risk;
	
}


//0. Get process outcomes

public Vector<TSslot> getLastTaskOutcomes(Vector<TSslot> activeTS, XLog log, InputParameters ip) throws Exception
{
	for (int i=0; i<activeTS.size(); i++)
	{
		
		Vector<String> activeSlotCases = new Vector<String>();
		activeSlotCases.addAll(activeTS.elementAt(i).cases);
		String outcomeattr = ip.outcomeattribute;
		
		for (XTrace t : log) 
		{	
			String caseID = XConceptExtension.instance().extractName(t);
			XEvent first = t.get(0);			 
			
		
			if (activeSlotCases.contains(caseID))
			{
				Date slotEndtime = activeTS.elementAt(i).slotEnd;
				Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
				long duration = slotEndtime.getTime()-caseStartTime.getTime();
				double timeFromCaseStart = (double)(duration)/ip.timeUnit;
			
				for (XEvent e : t)
				{
					String task = XConceptExtension.instance().extractName(e);
					if (task.equalsIgnoreCase(ip.taskPrevCaseAttr))
					{
						String caseOut = ((XAttributeLiteralImpl)e.getAttributes().get(outcomeattr)).getValue();
						//System.out.println("caseid: "+caseID);
						//System.out.println("caseOut: "+caseOut);
						Double caseoutcome = Double.parseDouble(caseOut);
						//System.out.println("caseOutDouble: "+caseoutcome);
						activeTS.elementAt(i).outcomes.add(caseoutcome);
						activeTS.elementAt(i).caseDurationsEndOfSlot.add(timeFromCaseStart);
					}
					
					
			}
			
		}
				
	}
}
	
return activeTS;}


public Vector<TSslot> getCaseOutcomes(Vector<TSslot> activeTS, XLog log, InputParameters ip) throws Exception
{
	for (int i=0; i<activeTS.size(); i++)
	{
		
		Vector<String> activeSlotCases = new Vector<String>();
		activeSlotCases.addAll(activeTS.elementAt(i).cases);
		String outcomeattr = ip.outcomeattribute;
		
		for (XTrace t : log) 
		{	
			String caseID = XConceptExtension.instance().extractName(t);
			XEvent first = t.get(0);			 
			
			
			if (activeSlotCases.contains(caseID))
			{
				Date slotEndtime = activeTS.elementAt(i).slotEnd;
				Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
				long duration = slotEndtime.getTime()-caseStartTime.getTime();
				double timeFromCaseStart = (double)(duration)/ip.timeUnit;
							
				XAttributeMap am = t.getAttributes();
				String caseOut = am.get(outcomeattr).toString();
				//System.out.println("caseid: "+caseID);
				//System.out.println("caseOut: "+caseOut);
				Double caseoutcome = Double.parseDouble(caseOut);
				//System.out.println("caseOutDouble: "+caseoutcome);
				
				//caseDuration = ((Double)(casedur/1000/60/60/24)).toString();
				activeTS.elementAt(i).outcomes.add(caseoutcome);
				activeTS.elementAt(i).caseDurationsEndOfSlot.add(timeFromCaseStart);
				
			}
			
		}
				
	}
	
return activeTS;}

//00 Get aggregate process outcomes

//total
public Vector<TSslot> getAggregateOutcome(Vector<TSslot> activeTS, InputParameters ip) throws Exception
{
	for (int i=0; i<activeTS.size(); i++)
	{
		int counter = 0;
		Vector<Double> caseOutcomes = new Vector<Double>();
		caseOutcomes.addAll(activeTS.elementAt(i).outcomes);
		
		for (int j=0; j<caseOutcomes.size(); j++)
		{
			
			double a = ip.outcomevalue;
			double b = caseOutcomes.elementAt(j);
			double dif = a-b;
			if (Math.abs(dif) <= 0.0000000001)
			counter++;
			
		}
		activeTS.elementAt(i).aggregateOutcome = (double) counter;
		//System.out.println("counter: "+counter);			
	}
	
return activeTS;
}

//fraction
public Vector<TSslot> getAggregateOutcomeFraction(Vector<TSslot> activeTS, InputParameters ip) throws Exception
{
	
	for (int i=0; i<activeTS.size(); i++)
	{
		int counter = 0;
		Vector<Double> caseOutcomes = new Vector<Double>();
		caseOutcomes.addAll(activeTS.elementAt(i).outcomes);
		
		for (int j=0; j<caseOutcomes.size(); j++)
		{
			
			double a = ip.outcomevalue;
			double b = caseOutcomes.elementAt(j);
			double dif = a-b;
			if (Math.abs(dif) <= 0.0000000001)
			counter++;
			
		}
		
		Double fraction = (double) (counter/caseOutcomes.size());
		activeTS.elementAt(i).aggregateOutcome = fraction;
		//System.out.println("fraction: "+fraction);			
	}
	
return activeTS;
}

//mean
public Vector<TSslot> getAggregateOutcomeMean(Vector<TSslot> activeTS, InputParameters ip) throws Exception
{
		
	for (int i=0; i<activeTS.size(); i++)
	{
		//int counter = 0;
		Vector<Double> caseOutcomes = new Vector<Double>();
		caseOutcomes.addAll(activeTS.elementAt(i).outcomes);
		Double meanOutcome = DoubleAverage(caseOutcomes);
		
		activeTS.elementAt(i).aggregateOutcome = meanOutcome;
		//System.out.println("meanOutcome: "+meanOutcome);			
	}
	
return activeTS;
}


//Get aggregate outcomes TS

public String getAggregateOutcomeTS(Vector<TSslot> ts)
{ 
	String ret ="";
	
	
	for (int i=0; i<ts.size(); i++)
	{	
		Double slotOutcome = ts.elementAt(i).aggregateOutcome;
				
		ret+=slotOutcome.toString()+",";
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}

//1. Log pre-processing--------------------------------------------------------------------------------
	
public XLog preprocessLog (XLog inputlog, InputParameters ip)
{
	// getting input log and creating for each task 3 new attributes:
	// taskname_time (in given units from case start), taskname_type, taskname_resource
	// adding these new attributes to the log and returning the log
	
	
	
	XLog log = XFactoryRegistry.instance().currentDefault().createLog(inputlog.getAttributes());
	for (XTrace t : inputlog) {
	XTrace trace = XFactoryRegistry.instance().currentDefault().createTrace(t.getAttributes());
	log.add(trace);
	XEvent first = t.get(0);			 
	for (XEvent e : t) 
	{
		
		XEvent event = XFactoryRegistry.instance().currentDefault().createEvent(e.getAttributes());
		String task = XConceptExtension.instance().extractName(e);
		String type = XLifecycleExtension.instance().extractTransition(e);
		String resource = XOrganizationalExtension.instance().extractResource(e);
		Date curtime = XTimeExtension.instance().extractTimestamp(e);
		Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
		long duration = curtime.getTime()-caseStartTime.getTime();
		double timeFromCaseStart = (double)(duration)/ip.timeUnit;
		//timeFromCaseStart = (double)Math.round(timeFromCaseStart * ip.decNum) / ip.decNum;
		
		event.getAttributes().put(task+"_type",new XAttributeLiteralImpl(task+"_type",type));
		event.getAttributes().put(task+"_resource",new XAttributeLiteralImpl(task+"_resource",resource));
		//event.getAttributes().put(task+"_time",new XAttributeTimestampImpl(task+"_time",curtime));
		event.getAttributes().put(task+"_time",new XAttributeContinuousImpl(task+"_time",timeFromCaseStart));
		
		
		trace.add(event);
	}
}

return log;
}

//2. Creating ts with cases - for risky situations-----------------------------------------------------

//2a. cases completed during time slot

public Vector<TSslot> populateTSwithCasesCompleted(Vector<TSslot> ts, XLog log, InputParameters ip) throws Exception
{

long tsPointTime=ip.startTime;
	
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		Date slotStart = getSlotStart(ip, (tsPointTime+i*ip.slotSize));
		Date slotEnd = getSlotEnd(ip, (tsPointTime+i*ip.slotSize));
		TSslot tsslot = new TSslot(slotStart, slotEnd);
		
		for (XTrace t : log) {
			
			String caseID = XConceptExtension.instance().extractName(t);
			//System.out.println(caseID);
			//XEvent first = t.get(0);
			XEvent last = t.get(t.size()-1);
			//Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
			Date caseEndTime = XTimeExtension.instance().extractTimestamp(last);
			
			if(caseEndTime.before(slotEnd) && caseEndTime.after(slotStart)) 
			{
				tsslot.cases.add(caseID);
				tsslot.tsCases.add(i+"_"+caseID);
				tsslot.fitnesses.add(0.0);
			}
			}
		ts.add(tsslot);
	}			
	
return ts;}

//2b. cases started during timeslot

public Vector<TSslot> populateTSwithCasesStarted(Vector<TSslot> ts, XLog log, InputParameters ip) throws Exception
{

long tsPointTime=ip.startTime;
	
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		Date slotStart = getSlotStart(ip, (tsPointTime+i*ip.slotSize));
		Date slotEnd = getSlotEnd(ip, (tsPointTime+i*ip.slotSize));
		TSslot tsslot = new TSslot(slotStart, slotEnd);
		
		for (XTrace t : log) {
			
			String caseID = XConceptExtension.instance().extractName(t);
			//System.out.println(caseID);
			XEvent first = t.get(0);
			//XEvent last = t.get(t.size()-1);
			Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
			//Date caseEndTime = XTimeExtension.instance().extractTimestamp(last);
			
			if(caseStartTime.before(slotEnd) && caseStartTime.after(slotStart)) 
			{
				tsslot.cases.add(caseID);
				tsslot.tsCases.add(i+"_"+caseID);
				tsslot.fitnesses.add(1.0);
			}
			}
		ts.add(tsslot);
	}			
	
return ts;}

//2c. cases active during time slot: completed during, started before and completed after, started during ts

public Vector<TSslot> populateTSwithCasesActive(Vector<TSslot> ts, XLog log, InputParameters ip) throws Exception
{

	long tsPointTime=ip.startTime;
	
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		Date slotStart = getSlotStart(ip, (tsPointTime+i*ip.slotSize));
		Date slotEnd = getSlotEnd(ip, (tsPointTime+i*ip.slotSize));
		TSslot tsslot = new TSslot(slotStart, slotEnd);
		
		for (XTrace t : log) {
			
			String caseID = XConceptExtension.instance().extractName(t);
			XEvent first = t.get(0);
			XEvent last = t.get(t.size()-1);
			Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
			Date caseEndTime = XTimeExtension.instance().extractTimestamp(last);
			
			if((caseEndTime.after(slotStart) && caseEndTime.before(slotEnd)) || (caseStartTime.before(slotStart) && caseEndTime.after(slotEnd)) || (caseStartTime.after(slotStart) && caseStartTime.before(slotEnd))) 
			{
				tsslot.cases.add(caseID);
				tsslot.tsCases.add(i+"_"+caseID);
				tsslot.fitnesses.add(1.0);
			}
			}
		ts.add(tsslot);
	}			
	
return ts;}

public Vector<TSslot> populateTSwithCasesActiveBA(Vector<TSslot> ts, XLog log, InputParameters ip) throws Exception
{

	long tsPointTime=ip.startTime;
	
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		Date slotStart = getSlotStart(ip, (tsPointTime+i*ip.slotSize));
		Date slotEnd = getSlotEnd(ip, (tsPointTime+i*ip.slotSize));
		TSslot tsslot = new TSslot(slotStart, slotEnd);
		
		for (XTrace t : log) {
			
			String caseID = XConceptExtension.instance().extractName(t);
			XEvent first = t.get(0);
			XEvent last = t.get(t.size()-1);
			Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
			Date caseEndTime = XTimeExtension.instance().extractTimestamp(last);
			
			if(caseStartTime.before(slotStart) && caseEndTime.after(slotEnd)) 
			{
				tsslot.cases.add(caseID);
				tsslot.tsCases.add(i+"_"+caseID);
				tsslot.fitnesses.add(1.0);
			}
			}
		ts.add(tsslot);
	}			
	
return ts;}

//2d. Prepare log for replay when truncating cases 

public XLog truncateCases(XLog inputlog, Vector<TSslot> ts)
{

	// pre-processing eventlog for replay - replacing case IDs with ts IDs plus case IDs, truncating cases that were completed after end of slot
	
	XLog newlog = new XLogImpl(inputlog.getAttributes());
	newlog.removeAll(newlog);


	for(int i=0; i<ts.size();i++)
	{
		Date slotEnd = ts.elementAt(i).slotEnd;
		Vector<String> slotCases = ts.elementAt(i).cases;
		Vector<String> slotTSCases = ts.elementAt(i).tsCases;
				
		for (XTrace t : inputlog) {
			String caseID = XConceptExtension.instance().extractName(t);
						
			if(slotCases.contains(caseID))
			{ 	
				XTrace newtrace = new XTraceImpl((XAttributeMap) t.getAttributes().clone());			
					
				newtrace.getAttributes().put("originalCaseID",new XAttributeLiteralImpl("originalCaseID",caseID));
				
				int index = slotCases.indexOf(caseID);
				String tsCaseID = slotTSCases.elementAt(index);
				newtrace.getAttributes().put("concept:name",new XAttributeLiteralImpl("concept:name",tsCaseID));
						
			for (XEvent e : t) 
			{
				
				Date curtime = XTimeExtension.instance().extractTimestamp(e);
				if (curtime.before(slotEnd))
				{newtrace.add(new XEventImpl(e.getAttributes()));}
			}
			
			newlog.add(newtrace);
			}
		}
		
	}
	
	return newlog;	
}

//2e. creating a log ready for replay - replacing caseID with ts ID and caseID, no truncation

public XLog processTSCases(XLog inputlog, Vector<TSslot> ts)
{

	// pre-processing eventlog for replay - replacing case IDs with ts IDs plus case IDs - no truncation
	
	XLog newlog = new XLogImpl(inputlog.getAttributes());
	newlog.removeAll(newlog);


	for(int i=0; i<ts.size();i++)
	{
		Vector<String> slotCases = ts.elementAt(i).cases;
		Vector<String> slotTSCases = ts.elementAt(i).tsCases;
				
		for (XTrace t : inputlog) {
			String caseID = XConceptExtension.instance().extractName(t);
						
			if(slotCases.contains(caseID))
			{ 	
				XTrace newtrace = new XTraceImpl((XAttributeMap) t.getAttributes().clone());			
					
				newtrace.getAttributes().put("originalCaseID",new XAttributeLiteralImpl("originalCaseID",caseID));
				
				int index = slotCases.indexOf(caseID);
				String tsCaseID = slotTSCases.elementAt(index);
				newtrace.getAttributes().put("concept:name",new XAttributeLiteralImpl("concept:name",tsCaseID));
						
			for (XEvent e : t) 
			{
				
				newtrace.add(new XEventImpl(e.getAttributes()));
			}
			
			newlog.add(newtrace);
			}
		}
		
	}
	
	return newlog;	
}
	


// 3. Creating ts with cases for outcomes--------------------------------------------------------------

public Vector<TSslot> populateTSwithCasesActiveEndOfSlot(Vector<TSslot> ts, XLog log, InputParameters ip) throws Exception
{

	long tsPointTime=ip.startTime;
	
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		Date slotStart = getSlotStart(ip, (tsPointTime+i*ip.slotSize));
		Date slotEnd = getSlotEnd(ip, (tsPointTime+i*ip.slotSize));
		TSslot tsslot = new TSslot(slotStart, slotEnd);
		
		for (XTrace t : log) {
			
			String caseID = XConceptExtension.instance().extractName(t);
			XEvent first = t.get(0);
			XEvent last = t.get(t.size()-1);
			Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
			Date caseEndTime = XTimeExtension.instance().extractTimestamp(last);
			
			if((caseEndTime.after(slotEnd) && caseStartTime.before(slotEnd))) 
			{
				tsslot.cases.add(caseID);
				tsslot.tsCases.add(i+"_"+caseID);
				tsslot.fitnesses.add(1.0);
			}
			}
		ts.add(tsslot);
	}			
	
return ts;}

public String getTotalRiskTSNew(Vector<TSslot> ts)
{ 
	String ret ="";
	Double slotRisk;
	
	for (int i=0; i<ts.size(); i++)
	{	
		slotRisk = 0.0;
				
		for (int j=0; j<ts.elementAt(i).fitnesses.size(); j++)
		{
			
			Double curUnFitness = ts.elementAt(i).fitnesses.elementAt(j);
			slotRisk+= curUnFitness;
		}
		
		//slotRisk = (double)Math.round(slotRisk * ip.decNum) / ip.decNum;
		
		ret+=slotRisk.toString()+",";
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}

//mean risk ts

public String getMeanRiskTSNew(Vector<TSslot> ts)
{ 
	String ret ="";
	Double slotRisk;
	Double meanSlotRisk;
	int slotCases;
	
	for (int i=0; i<ts.size(); i++)
	{	
		slotRisk = 0.0;
		meanSlotRisk = 0.0;
		slotCases = ts.elementAt(i).fitnesses.size();
		
		//System.out.println("Slot: " + i + ", fitnesses: " + ts.elementAt(i).fitnesses);
				
		for (int j=0; j<slotCases; j++)
		{
			
			Double curUnFitness = ts.elementAt(i).fitnesses.elementAt(j);
			slotRisk+= curUnFitness;
		}
		
		//System.out.println("slotCases: "+slotCases);
		meanSlotRisk = (double)(slotRisk/slotCases);
		//meanSlotRisk = (double)Math.round(meanSlotRisk * ip.decNum) / ip.decNum;
		
		ret+=meanSlotRisk.toString()+",";
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}

public Date getSlotStart(InputParameters ip, long t) throws Exception
{
	Date start = new Date(t);
	
	return start;
	
}

public Date getSlotEnd(InputParameters ip, long t) throws Exception
{
	Date end = new Date(t+ip.slotSize);
	
	return end;
	
}

public Double getMean(Vector<Double> oneTSDouble, InputParameters ip)
{
Double ret = 0.0;
String meanMethod = ip.meanMethod;

if(meanMethod.equalsIgnoreCase("median"))
{
	ret = DoubleMedian(oneTSDouble);	
}else
{
	ret = DoubleAverage(oneTSDouble);	
}

return ret;
}

public Double DoubleMedian(Vector<Double> numbers)
{
	Collections.sort(numbers);
	double median = 1.0;
	int size = numbers.size();
	if (size!=0)
	{if( (size % 2) != 0)
	{median = numbers.elementAt((size+1)/2-1);}
	else
	{median = 0.5*numbers.elementAt(size/2-1)+0.5*numbers.elementAt(size/2);}}
	return median;
}

public Double DoubleAverage(Vector<Double> numbers)
{
		double average = 1.0;
		int size = numbers.size();
		double sum = 0.00;
		
		for (int i=0; i<size; i++)
		{
			sum+=numbers.elementAt(i);
		}

		average = sum/size;
		
return average;
}
	
}


//----------------------------------------------------PREV. VERSIONS----------------------------------------------------
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


//static InputParameters ip = new InputParameters();	

//Getting costs from users-------------------------------------------------------------------------------

//getting move on log costs

/*Hashtable <String,Double> getMoveOnLogCosts(XLog log, Hashtable<String,Double> moveOnLogCosts)	
{ConcurrentSkipListSet<String> activities = new ConcurrentSkipListSet<String>();
	
for (XTrace t : log) {
for (XEvent e : t) {
		String activity = XConceptExtension.instance().extractName(e);
		if(activity != null)
		{activities.add(activity);}
}
}

Iterator it2 = activities.iterator();
while(it2.hasNext())
{moveOnLogCosts.put((String) it2.next(), 1.0);}//original
//{moveOnLogCosts.put((String) it2.next(), 1.0);}//temp
//moveOnLogCosts.put("Load ATM", 0.0);//temp

System.out.println(moveOnLogCosts);
return moveOnLogCosts;
}*/

//getting move on model costs
/*Hashtable <String,Double> getMoveOnModelCosts(PetriNetWithData net, Hashtable<String,Double> moveOnModelCosts)	
{
	Vector<String> transitions = new Vector<String>();
	
	Collection <Transition> trans = net.getTransitions();
	Iterator it = trans.iterator();
	while(it.hasNext())
	{
		DirectedGraphNode node = (DirectedGraphNode) it.next();
		transitions.add(node.toString());
	}
	
	for(int i=0; i<transitions.size(); i++)
	{moveOnModelCosts.put(transitions.elementAt(i), 1.0);}
	
	moveOnModelCosts.put("tr1", 0.0);
	moveOnModelCosts.put("tr2", 0.0);
	moveOnModelCosts.put("tr3", 0.0);
	moveOnModelCosts.put("tr4", 0.0);
	moveOnModelCosts.put("tr5", 0.0);
	moveOnModelCosts.put("tr6", 0.0);
	//moveOnModelCosts.put("Load ATM", 0.0);//temp

	System.out.println(moveOnModelCosts);

return moveOnModelCosts;
}*/

//getting variable not written costs
/*Hashtable <String,Double> getVarNotWrittenCosts(PetriNetWithData net, Hashtable<String,Double> varNotWrittenCosts)	
{
	
	//Vector<String> transitions = new Vector<String>();
	
	Collection <Transition> trans = net.getTransitions();
	Iterator it = trans.iterator();
	while(it.hasNext())
	{
		DirectedGraphNode node = (DirectedGraphNode) it.next();
		//transitions.add(node.toString());
		PNWDTransition transition = (PNWDTransition) node;
			
		String task = transition.getLabel();
		Set<DataElement> wo = transition.getWriteOperations();
		Iterator it3 = wo.iterator();
		
		while(it3.hasNext())
		{
			DataElement curVar = (DataElement) it3.next();
			String varName = curVar.getVarName();
			varNotWrittenCosts.put(task+"-"+varName, 1.0);
		}
		
	}

	System.out.println(varNotWrittenCosts);

return varNotWrittenCosts;
}*/

/*Vector<GuardCosts> getGuardCosts (PetriNetWithData net, Vector<GuardCosts> guardCosts)
{

//Hashtable<String, String> guards = new Hashtable<String, String>();

Collection <Transition> trans = net.getTransitions();
Iterator it = trans.iterator();
while(it.hasNext())
{
	DirectedGraphNode node = (DirectedGraphNode) it.next();
	PNWDTransition transition = (PNWDTransition) node;
	if(transition.getGuard()!=null)
		{
		String mainGuard = transition.getGuard().toString();
		//guards.put(node.toString(),mainGuard);
		guardCosts.add(new GuardCosts(node.toString(),mainGuard,1.0));
		}
	else
		{
		//guards.put(node.toString(),"NO-GUARD");
		guardCosts.add(new GuardCosts(node.toString(),"NO-GUARD",0.0));
		}
		
}

//adding separate guards to first task for terminals; - needed for prediction in simulated log

String task = "Lodge Request";
int index = -1;
for (int i=0; i<guardCosts.size(); i++)
{
	if(guardCosts.elementAt(i).task.equals(task))
	{
		index = i;
	}
		
}
if(index > -1)
{guardCosts.elementAt(index).guards.add("terminal' != \"ATM\"");
guardCosts.elementAt(index).costs.add(1.0);

guardCosts.elementAt(index).guards.add("urgency' != \"High\"");
guardCosts.elementAt(index).costs.add(1.0);
}



//System.out.println("index: " + index);
//System.out.println("task: " + task);
//System.out.println(guards);
//System.out.println(guardCosts);

return guardCosts;
}*/

//System.out.println("Transitions: "+net.getTransitions().toString());
//System.out.println("Transition 1: "+net.getTransitions().iterator().next().toString());
//DirectedGraphNode node = net.getTransitions().iterator().next();
//PNWDTransition transition = (PNWDTransition) node;
//String guard = transition.getGuard().toString();
//String variables = transition.getWriteOperations().toString();
//String variabletype = transition.getWriteOperations().iterator().next().getType().toString();
//System.out.println("node guard: "+guard);
//System.out.println("node vars: "+variables);
//System.out.println("node var 1 type: "+variabletype);
//System.out.println(MVEL.eval("(\"High\" != \"High1\") && ((3.2 < (0.0 + 4.0)) || (false && true ))"));

//System.out.println(MVEL.eval("(\"High\" != \"High1\") && ((3.2 < (0.0 + 4.0)) || (false && true ))"));
//End of getting input costs from users----------------------------------------------------------------------------------

//00 Get aggregate process outcomes - only not delayed cases
/*public Vector<TSslot> getAggregateOutcomeNotDelayed(Vector<TSslot> activeTS, InputParameters ip) throws Exception
{
	Double outcomevalue = ip.outcomevalue;
	Double normCaseDur = ip.normalCaseDuration;
	
	for (int i=0; i<activeTS.size(); i++)
	{
		int counter = 0;
		Vector<Double> caseOutcomes = new Vector<Double>();
		caseOutcomes.addAll(activeTS.elementAt(i).outcomes);
		Vector<Double> caseDurations = new Vector<Double>();
		caseDurations.addAll(activeTS.elementAt(i).caseDurationsEndOfSlot);
	
		
		for (int j=0; j<caseOutcomes.size(); j++)
		{
			if(caseOutcomes.elementAt(j)>outcomevalue && caseDurations.elementAt(j)<normCaseDur)
				counter++;
		}
		activeTS.elementAt(i).aggregateOutcome = (double) counter;
					
	}
	
return activeTS;
}
*/

/*public Vector<TSslot> populateTSwithCasesActiveEndOfSlot(Vector<TSslot> ts, XLog log) throws Exception
{

	long tsPointTime=ip.startTime;
	
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		Date slotStart = getSlotStart(ip, (tsPointTime+i*ip.slotSize));
		Date slotEnd = getSlotEnd(ip, (tsPointTime+i*ip.slotSize));
		TSslot tsslot = new TSslot(slotStart, slotEnd);
		
		for (XTrace t : log) {
			
			String caseID = XConceptExtension.instance().extractName(t);
			XEvent first = t.get(0);
			XEvent last = t.get(t.size()-1);
			Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
			Date caseEndTime = XTimeExtension.instance().extractTimestamp(last);
			
			if((caseEndTime.after(slotEnd) && caseStartTime.before(slotEnd))) 
			{
				tsslot.cases.add(caseID);
				tsslot.tsCases.add(i+"_"+caseID);
				tsslot.fitnesses.add(1.0);
			}
			}
		ts.add(tsslot);
	}			
	
return ts;}*/

//3-I - Cases related to the same period - started before slot end and ended after
/*
public Vector<TSslot> populateTSwithCasesActiveOutcome(Vector<TSslot> ts, XLog log, InputParameters ip) throws Exception
{

	long tsPointTime=ip.startTime;
	
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		Date slotStart = getSlotStart(ip, (tsPointTime+i*ip.slotSize));
		Date slotEnd = getSlotEnd(ip, (tsPointTime+i*ip.slotSize));
		TSslot tsslot = new TSslot(slotStart, slotEnd);
		
		for (XTrace t : log) {
			
			String caseID = XConceptExtension.instance().extractName(t);
			XEvent first = t.get(0);
			XEvent last = t.get(t.size()-1);
			Date caseStartTime = XTimeExtension.instance().extractTimestamp(first);
			Date caseEndTime = XTimeExtension.instance().extractTimestamp(last);
			
			if(caseStartTime.before(slotEnd) && caseEndTime.after(slotEnd)) 
			{
				tsslot.cases.add(caseID);
				tsslot.tsCases.add(i+"_"+caseID);
				tsslot.fitnesses.add(1.0);
			}
			}
		ts.add(tsslot);
	}			
	
return ts;}*/

/*public Vector<TSslot> populateTSwithCasesActiveRestarted(Vector<TSslot> restartedTS, Vector<TSslot> activeTS, XLog log, InputParameters ip) throws Exception
{
	for (int i=0; i<activeTS.size(); i++)
	{
		
		TSslot newtsslot = new TSslot(activeTS.elementAt(i).slotStart, activeTS.elementAt(i).slotEnd);
		Vector<String> activeSlotCases = new Vector<String>();
		activeSlotCases.addAll(activeTS.elementAt(i).cases);
		
		for (XTrace t : log) 
		{	boolean restarted = false;
			String caseIDRestarted = XConceptExtension.instance().extractName(t);
			String prevCases = ((XAttributeLiteralImpl)t.getAttributes().get(ip.prevCaseAttr)).getValue();
			String[] prevCasesArr = prevCases.split(",");
			
			for(int j=0; j<prevCasesArr.length;j++)
			{
				if (activeSlotCases.contains(prevCasesArr[j]))
				{
					restarted = true;
				}
			}
			
			if(restarted)
			{
			newtsslot.cases.add(caseIDRestarted);
			newtsslot.tsCases.add(i+"_"+caseIDRestarted);
			newtsslot.fitnesses.add(1.0);
			}
			
		}
		
		restartedTS.add(newtsslot);
		
	}
	
	return restartedTS;}*/

//-------------------Restarted in Incidents--------------------------------------
/*	public Vector<TSslot> getRestartedTS(Vector<TSslot> restartedTS, Vector<TSslot> activeTS, XLog log, InputParameters ip) throws Exception
	{
		for (int i=0; i<activeTS.size(); i++)
		{
			
			TSslot newtsslot = new TSslot(activeTS.elementAt(i).slotStart, activeTS.elementAt(i).slotEnd);
			Vector<String> activeSlotCases = new Vector<String>();
			activeSlotCases.addAll(activeTS.elementAt(i).cases);
			
			for (XTrace t : log) 
			{	
				
				for (XEvent e : t)
				{
					String task = XConceptExtension.instance().extractName(e);
					if (task.equalsIgnoreCase(ip.taskPrevCaseAttr))
					{
						String prevCase = ((XAttributeLiteralImpl)e.getAttributes().get(ip.prevCaseAttr)).getValue();
						if (activeSlotCases.contains(prevCase))
						{
							newtsslot.cases.add(prevCase);
							newtsslot.tsCases.add(i+"_"+prevCase);
							newtsslot.fitnesses.add(1.0);
						}
					}
				}
			}
			
			restartedTS.add(newtsslot);
			
		}
		
return restartedTS;}*/
//------------------------------------------------------------------------------------------------------

/*
public Vector<TSslot> replayLog(UIPluginContext context, PetriNetWithData net, XLog log, Vector<TSslot> ts, InputParameters ip)
{
	
	DataAlignment da = new DataAlignment();
	ResultReplayPetriNetWithData rp = da.plugin(context, net, log);
	
	Iterator<Alignment> iterator = rp.labelStepArray.iterator();
	
	while (iterator.hasNext()) 	
	{
	Alignment nextAlignment = iterator.next();	
	String nextTScaseID = nextAlignment.getTraceName();
	System.out.println(nextTScaseID);
	Double nextCaseFitness = (double) nextAlignment.getFitness();
	System.out.println(nextCaseFitness);
	int tsID = Integer.parseInt((nextTScaseID.split("_"))[0]);
	System.out.println(tsID);
	int index = ts.elementAt(tsID).tsCases.indexOf(nextTScaseID);
	System.out.println(index);
	ts.elementAt(tsID).fitnesses.set(index, nextCaseFitness);
	}

	//get mean fitness for each slot
	
	for (int i=0; i<ts.size(); i++)
	{
		Double tsPointMean = getMean(ts.elementAt(i).fitnesses, ip);
		//tsPointMean = (double)Math.round(tsPointMean * ip.decNum) / ip.decNum;
		ts.elementAt(i).meanFitness = tsPointMean;
	}
	
	return ts;
}*/

/*
String getMeanFitnessTS(Vector<TSslot> ts)
{ 
	String ret ="";
	
	for (int i=0; i<ts.size(); i++)
	{
		String nextMeanFitness = ts.elementAt(i).meanFitness.toString();
		ret+=nextMeanFitness+",";
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}*/
/*
public String getPercentageRestartedTS(Vector<TSslot> tsall, Vector<TSslot> restarted, InputParameters ip)
{ 
	String ret ="";
	
	for (int i=0; i<tsall.size(); i++)
	{
		int allCases = tsall.elementAt(i).cases.size();
		int restartedCases = restarted.elementAt(i).cases.size();
		
		if (allCases==0){ret+="0,";}else
		{double curSize = (double)restartedCases/allCases;
		//curSize = (double)Math.round(curSize * ip.decNum) / ip.decNum;
		ret+=curSize+",";}
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}*/
/*
public String getPercentageDelayedRestartedTS(Vector<TSslot> activeDelayed, Vector<TSslot> restarted, InputParameters ip)
{ 
	String ret ="";
	
	for (int i=0; i<activeDelayed.size(); i++)
	{
		int allCases = activeDelayed.elementAt(i).cases.size();
		
		Vector <String> delayedOrRestarted = new Vector <String>();
		Vector <String> delayedCases = new Vector <String>();
		Vector <String> restartedCases = new Vector <String>();
		restartedCases.addAll(restarted.elementAt(i).cases);
		
		for (int j=0; j<activeDelayed.elementAt(i).fitnesses.size();j++)
		{if (activeDelayed.elementAt(i).fitnesses.elementAt(j)<1)
		{delayedCases.add(activeDelayed.elementAt(i).cases.elementAt(j));}
		}
		delayedOrRestarted.addAll(delayedCases);
		for (int z=0;z<restartedCases.size();z++)
		{
			if (!(delayedOrRestarted.contains(restartedCases.elementAt(z))))
			{
				delayedOrRestarted.add(restartedCases.elementAt(z));
			}
		}
		int delayedOrRestartedSize = delayedOrRestarted.size();
		
		if (allCases==0){ret+="0,";}else
		{double curSize = (double)delayedOrRestartedSize/allCases;
		//curSize = (double)Math.round(curSize * ip.decNum) / ip.decNum;
		ret+=curSize+",";}
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}*/

/*public String getNumberDelayedRestartedTS(Vector<TSslot> activeDelayed, Vector<TSslot> restarted, InputParameters ip)
{ 
	String ret ="";
	
	for (int i=0; i<activeDelayed.size(); i++)
	{
		Vector <String> delayedOrRestarted = new Vector <String>();
		Vector <String> delayedCases = new Vector <String>();
		Vector <String> restartedCases = new Vector <String>();
		restartedCases.addAll(restarted.elementAt(i).cases);
		
		for (int j=0; j<activeDelayed.elementAt(i).fitnesses.size();j++)
		{if (activeDelayed.elementAt(i).fitnesses.elementAt(j)<1)
		{delayedCases.add(activeDelayed.elementAt(i).cases.elementAt(j));}
		}
		delayedOrRestarted.addAll(delayedCases);
		for (int z=0;z<restartedCases.size();z++)
		{
			if (!(delayedOrRestarted.contains(restartedCases.elementAt(z))))
			{
				delayedOrRestarted.add(restartedCases.elementAt(z));
			}
		}
		int delayedOrRestartedSize = delayedOrRestarted.size();
		
		
		ret+=delayedOrRestartedSize+",";
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}*/
/*
public String getPercentageDelayedTS(Vector<TSslot> ts, InputParameters ip)
{ 
	String ret ="";
	
	for (int i=0; i<ts.size(); i++)
	{
		int allCases = ts.elementAt(i).cases.size();
		int delayed = 0;
		
		for (int j=0; j<ts.elementAt(i).fitnesses.size();j++)
		{if (ts.elementAt(i).fitnesses.elementAt(j)<1)
		delayed++;}
		
		if (allCases==0){ret+="0,";}else
		{double curSize = (double)delayed/allCases;
		//curSize = (double)Math.round(curSize * ip.decNum) / ip.decNum;
		ret+=curSize+",";}
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}
*/
/*public String getNumberDelayedTS(Vector<TSslot> ts)
{ 
	String ret ="";
	
	for (int i=0; i<ts.size(); i++)
	{
		int delayed = 0;
		
		for (int j=0; j<ts.elementAt(i).fitnesses.size();j++)
		{if (ts.elementAt(i).fitnesses.elementAt(j)<1)
		delayed++;}
		
		ret+=delayed+",";
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}*/

/*public String getNumberRestartedTS(Vector<TSslot> restarted)
{ 
	String ret ="";
	
	for (int i=0; i<restarted.size(); i++)
	{
		int restartedCases = restarted.elementAt(i).cases.size();
		
		ret+=restartedCases+",";
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}*/

/*public String getTotalRiskTS(Vector<TSslot> ts)
{ 
	String ret ="";
	Double slotRisk;
	
	for (int i=0; i<ts.size(); i++)
	{	
		slotRisk = 0.0;
				
		for (int j=0; j<ts.elementAt(i).fitnesses.size(); j++)
		{
			
			Double curUnFitness = 1.0 - ts.elementAt(i).fitnesses.elementAt(j);
			slotRisk+= curUnFitness;
		}
		
		//slotRisk = (double)Math.round(slotRisk * ip.decNum) / ip.decNum;
		
		ret+=slotRisk.toString()+",";
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}*/

//mean risk ts

/*public String getMeanRiskTS(Vector<TSslot> ts)
{ 
	String ret ="";
	Double slotRisk;
	Double meanSlotRisk;
	int slotCases;
	
	for (int i=0; i<ts.size(); i++)
	{	
		slotRisk = 0.0;
		meanSlotRisk = 0.0;
		slotCases = ts.elementAt(i).fitnesses.size();
				
		for (int j=0; j<slotCases; j++)
		{
			
			Double curUnFitness = 1.0 - ts.elementAt(i).fitnesses.elementAt(j);
			slotRisk+= curUnFitness;
		}
		
		//System.out.println("slotCases: "+slotCases);
		meanSlotRisk = slotRisk/slotCases;
		//meanSlotRisk = (double)Math.round(meanSlotRisk * ip.decNum) / ip.decNum;
		
		ret+=meanSlotRisk.toString()+",";
	}
	
	ret = ret.substring(0, ret.length()-1); 
	
	return ret;
}*/

//-------------------------First Variant of Implementation-----------------------------------------------	
/*	 
Vector<TraceReplay> getCaseIDDate(XLog inputlog, Vector<TraceReplay> cases)	
{
	for (XTrace t : inputlog) 
	{
	String caseID = XConceptExtension.instance().extractName(t);
	XEvent first = t.get(0);
	XEvent last = t.get(t.size()-1);
	Date begin = XTimeExtension.instance().extractTimestamp(first);
	Date end = XTimeExtension.instance().extractTimestamp(last);
	
	cases.add(new TraceReplay(caseID,begin,end));
	}
	
	return cases;
}*/

/*Vector<TraceReplay> getCaseFitness(UIPluginContext context, PetriNetWithData net, XLog log, Vector<TraceReplay> cases)
{
	
	DataAlignment da = new DataAlignment();
	ResultReplayPetriNetWithData rp = da.plugin(context, net, log);
	
	Iterator<Alignment> iterator = rp.labelStepArray.iterator();
	while (iterator.hasNext()) {
	Alignment nextAlignment = iterator.next();	
	String nextCaseID = nextAlignment.getTraceName();
	Double nextCaseFitness = (double) nextAlignment.getFitness();
	
	for (int i=0; i<cases.size(); i++)
	{
		if (cases.elementAt(i).caseID.equals(nextCaseID)) 
		{
			cases.elementAt(i).fitness = nextCaseFitness;
		}
	}
	
	}

	return cases;
}*/
/*
public String getTS(InputParameters ip, Vector <TraceReplay> cases) throws Exception
{
	String ts = "";
	Vector <Double> meanvalues = new Vector<Double>(); 
	
	long tsPointTime=ip.startTime;
	
	Vector <Double> tsPointvalues = new Vector<Double>();
	Double tsPointMean;

	for(int i=0; i<ip.numberOfSlots; i++)
	{
		Date t1 = getSlotStart(ip, (tsPointTime+i*ip.slotSize));
		Date t2 = getSlotEnd(ip, (tsPointTime+i*ip.slotSize));
		tsPointvalues.removeAllElements();
		
		for(int j=0; j<cases.size(); j++)
		{
			//case 1
			
			if(ip.tsCaseMethod.equals("start"))
			{
				if(cases.elementAt(j).caseStart.after(t1) && cases.elementAt(j).caseStart.before(t2))
				tsPointvalues.add(cases.elementAt(j).fitness);	
			}
			
			//case 2
			
			if(ip.tsCaseMethod.equals("complete"))
			{
				if(cases.elementAt(j).caseEnd.after(t1) && cases.elementAt(j).caseEnd.before(t2))
				tsPointvalues.add(cases.elementAt(j).fitness);	
			}
			
			//case 3
			
			if(ip.tsCaseMethod.equals("active"))
			{
				if(cases.elementAt(j).caseStart.before(t1) && cases.elementAt(j).caseEnd.after(t2))
				tsPointvalues.add(cases.elementAt(j).fitness);	
			
			}
		}
		
	if(tsPointvalues.isEmpty()){tsPointMean = -1.0;}else
	{tsPointMean = getMean(tsPointvalues, ip);
	//tsPointMean = (double)Math.round(tsPointMean * ip.decNum) / ip.decNum;
	}
	meanvalues.add(tsPointMean);	
	ts+=tsPointMean+",";
	
	}
		
	ts = ts.substring(0, ts.length()-1); 
	//System.out.println(ts);
	return ts;
}*/
