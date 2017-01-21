package org.processmining.plugins.processrisk.main;

import java.util.Vector;

import org.deckfour.xes.model.XLog;
import org.jfree.data.time.TimeSeries;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.plugins.processrisk.analyse.RFunctions;
import org.processmining.plugins.processrisk.analyse.TSslot;
import org.processmining.plugins.processrisk.analyse.XLogFunctions;
import org.processmining.plugins.processrisk.input.GetUserInput;
import org.processmining.plugins.processrisk.input.InputParameters;
import org.processmining.plugins.processrisk.output.PaneOut;
import org.processmining.plugins.processrisk.output.VisualiseResults;
import org.rosuda.JRI.Rengine;



public class ProcessRiskEvaluationPlugin {
	
	static XLogFunctions logFun = new XLogFunctions();
	static RFunctions RFun = new RFunctions();
	static VisualiseResults vis = new VisualiseResults();
	static GetUserInput gui1 = new GetUserInput();
	static GetUserInput gui2 = new GetUserInput();
	static GetUserInput gui3 = new GetUserInput();
	static GetUserInput gui4 = new GetUserInput();
	static GetUserInput gui5 = new GetUserInput();
	static GetUserInput gui6 = new GetUserInput();
	static GetUserInput gui7 = new GetUserInput();
	static PaneOut out = new PaneOut();

		
@Plugin(
			name = "Process Risk Evaluation", 
			parameterLabels = { "Event Log", "Process Model" }, 
			returnLabels = {"Overall Process Risk"},
			returnTypes = {String.class},
			//returnTypes = {HeaderBar.class},
			userAccessible = true,
			help = ""
		)
@PluginVariant(variantLabel = "Process Risk Evaluation", requiredParameterLabels = { 0, 1 })
@UITopiaVariant(
		affiliation = "QUT", 
		author = "A.Pika", 
		email = "a.pika@qut.edu.au"
			)


public /*HeaderBar*/ String  main(UIPluginContext context, final XLog log, PetriNetWithData net) throws Exception  
		{
	
	//template for panel output
	/*
	final HeaderBar pane = new HeaderBar("");
	pane.setLayout(new GridBagLayout());
	final GridBagConstraints c = new GridBagConstraints();
	JLabel importlab = new JLabel("<html><h1>Test</h1></html>");
		
	c.gridwidth = 1;
	c.gridheight = 1;
	c.gridx = 0;
	c.gridy = 0;
	pane.add(importlab,c);	

	return pane;
	*/
	
			Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
				
			// getting costs - prev. versions
			//Hashtable<String,Double> moveOnModelCosts = new Hashtable<String,Double>(); 
			//Hashtable<String,Double> moveOnLogCosts = new Hashtable<String,Double>();  
			//Vector<GuardCosts> guardCosts = new Vector<GuardCosts>();
			//Hashtable<String, Double> varNotWrittenCosts = new Hashtable<String, Double>();
			//Hashtable<String,String> varTypes = new Hashtable<String,String>();
         	//ip.moveOnLogCosts = gui1.getMoveOnLogCosts(log, ip.moveOnLogCosts);
			//ip.moveOnModelCosts = gui2.getMoveOnModelCosts(net, ip.moveOnModelCosts);
			//ip.varNotWrittenCosts = gui3.getVarNotWrittenCosts(net, ip.varNotWrittenCosts);
		 	
        	
			InputParameters ip = new InputParameters();
			ip = gui5.defineTSParams(ip,log); 
        	ip = gui6.defineRiskParams(ip);
        	ip = gui1.getRiskCosts(log, net, ip);
    		ip.varTypes = logFun.getVarTypes(net, ip.varTypes);
			ip.guardCosts = gui4.getGuardCosts(net, ip.guardCosts);
		
        	//System.out.println("move-on-log-costs:"+ip.moveOnLogCosts);
        	//System.out.println("var-not-written-costs:"+ip.varNotWrittenCosts);
        	//System.out.println("move-on-model-costs:"+ip.moveOnModelCosts);
 			
			context.log("Log pre-processing");
			
			// adding combinations of task with time, type or resource attributes to each event 	
			XLog processedLog = null;
			
			if(ip.preprocess)
				{processedLog = logFun.preprocessLog(log,ip);}
			else
				{processedLog = log;}
			
			// populate time slots with case IDs
			Vector <TSslot> cases = new Vector <TSslot>();
			if(ip.tsCaseMethod.equals("start"))
			{
				cases = logFun.populateTSwithCasesStarted(cases, processedLog,ip);
				
			}	
			
			if(ip.tsCaseMethod.equals("complete"))
			{
				cases = logFun.populateTSwithCasesCompleted(cases, processedLog,ip);
			}
			
			if(ip.tsCaseMethod.equals("activeInTS"))
			{
				cases = logFun.populateTSwithCasesActiveBA(cases, processedLog,ip);
			}
			
			if(ip.tsCaseMethod.equals("active"))
			{
				cases = logFun.populateTSwithCasesActive(cases, processedLog,ip);
				
			}
			
			if(ip.tsCaseMethod.equals("activeEndTS"))
			{
				cases = logFun.populateTSwithCasesActiveEndOfSlot(cases, processedLog,ip);
			}
			
			
			// truncating cases and/or replacing caseID with caseID+tsID
			XLog processedCasesLog = null;		
			
			if(ip.truncate)		
			{
				processedCasesLog = logFun.truncateCases(processedLog, cases); // truncates cases at the end of slot
					
			}	
			else
			{
				processedCasesLog = logFun.processTSCases(processedLog, cases); //replacing caseID with caseID+tsID - cases not truncated
				
			}
			
			//replaying event log on the Petri net
			//System.out.println(ip.startTime +"--"+ ip.numberOfSlots +"--"+ ip.slotSize + "--"+ ip.riskMethod + "--- "+ip.moveBothCost);
			
			context.log("Evaluating Overall Process Risk");
			
			cases = logFun.replayLogNew(context, net, processedCasesLog, cases, ip.moveOnModelCosts, ip.moveOnLogCosts, ip.moveBothCost, ip.guardCosts,ip.varNotWrittenCosts,ip.varTypes,ip);
			
			//getting overall process risk
			
			//TimeSeries overallRisk;
			Rengine re=new Rengine (new String [] {"--vanilla"}, false, null);	
			
			if (ip.riskMethod.equals("total"))
			{
				//String totalRisk = logFun.getTotalRiskTS(cases); // prev. version that uses case fitness to evaluate case risk
				String totalRisk = logFun.getTotalRiskTSNew(cases);
				//System.out.println("TR: "+totalRisk);
			
				String change_points = RFun.getCP(re, totalRisk);
				//System.out.println("CP: "+change_points);
				
				TimeSeries totalriskts = vis.getTS( ip, "Total Risk", totalRisk);
				//vis.showChart(vis.displayOneTS(ip, totalriskts, "Total Risk"), "Total Risk"); //prev. version
			
				vis.showChart(vis.plotTSDisplayNew(totalriskts, vis.getTSCP(ip, totalRisk, change_points)), "Total Risk");
			}
			
			if (ip.riskMethod.equals("mean"))
			{
				//String meanRisk = logFun.getMeanFitnessTS(cases); // mean fitness - prev. version
				//String meanRisk = logFun.getMeanRiskTS(cases); // prev. version with 1 - fitness as mean risk
				String meanRisk = logFun.getMeanRiskTSNew(cases);
				//System.out.println("MR: "+meanRisk);
			
				String change_points = RFun.getCP(re, meanRisk);
				//System.out.println("CP: "+change_points);
		
				TimeSeries meanriskts = vis.getTS( ip, "Mean Risk", meanRisk);
				//vis.showChart(vis.displayOneTS(ip, meanriskts, "Mean Risk"), "Mean Risk"); //prev. version
			
				vis.showChart(vis.plotTSDisplayNew(meanriskts, vis.getTSCP(ip, meanRisk, change_points)), "Mean Risk");
			
			}
			
	return "";
			
		
		}
		
	
	
}

