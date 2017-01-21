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
import org.processmining.plugins.processrisk.output.VisualiseResults;
import org.rosuda.JRI.Rengine;


@Plugin(
		name = "Process Risk Evaluation: predicting outcomes (1 process)", 
		parameterLabels = { "Event Log - training", "Event Log - test", "Process Model"}, 
		returnLabels = {"Aggregate process outcome prediction"},
		returnTypes = { String.class }, 
		userAccessible = true
		)

public class ProcessRiskPredictionPluginOneProcess {
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
	
	@PluginVariant(variantLabel = "Process Risk Evaluation: predicting outcomes (1 process)", requiredParameterLabels = { 0, 1, 2})		

	@UITopiaVariant(uiLabel = "Process Risk Evaluation: predicting outcomes (1 process)",
			affiliation = "QUT", 
			author = "A.Pika", 
			email = "a.pika@qut.edu.au"
)

public String  mainTwo(UIPluginContext context, final XLog log1, final XLog testlog1, PetriNetWithData risknet1) throws Exception  
		{
			
			Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
			
			InputParameters ip = new InputParameters();
			ip = gui5.defineTSParams(ip,log1); 
        	ip = gui6.defineRiskParams(ip);
        	ip = gui7.defineOutcomeParams(ip,log1);
			
			// 0. Getting risk costs
        	
        	ip = gui1.getRiskCosts(log1, risknet1, ip);
    		ip.varTypes = logFun.getVarTypes(risknet1, ip.varTypes);
			ip.guardCosts = gui4.getGuardCosts(risknet1, ip.guardCosts);
		
			//prev. versions
			/*
			//getting move on log costs
			Hashtable<String,Double> moveOnLogCostsLog1 = new Hashtable<String,Double>();  
			//Hashtable<String,Double> moveOnLogCostsLog2 = new Hashtable<String,Double>();  
			moveOnLogCostsLog1 = gui1.getMoveOnLogCosts(log1, moveOnLogCostsLog1);
			//moveOnLogCostsLog2 = gui.getMoveOnLogCosts(log2, moveOnLogCostsLog2);
			
			//getting move on model costs
			Hashtable<String,Double> moveOnModelCostsNet1 = new Hashtable<String,Double>(); 
			//Hashtable<String,Double> moveOnModelCostsNet2 = new Hashtable<String,Double>(); 
			moveOnModelCostsNet1 = gui2.getMoveOnModelCosts(risknet1, moveOnModelCostsNet1);
			//moveOnModelCostsNet2 = gui.getMoveOnModelCosts(risknet2, moveOnModelCostsNet2);
			
			//getting var not written costs
			Hashtable<String, Double> varNotWrittenCostsNet1 = new Hashtable<String, Double>();
			//Hashtable<String, Double> varNotWrittenCostsNet2 = new Hashtable<String, Double>();
			varNotWrittenCostsNet1 = gui3.getVarNotWrittenCosts(risknet1, varNotWrittenCostsNet1);
			//varNotWrittenCostsNet2 = gui.getVarNotWrittenCosts(risknet2, varNotWrittenCostsNet2);
			
			//getting var types
			Hashtable<String,String> varTypesNet1 = new Hashtable<String,String>();
			//Hashtable<String,String> varTypesNet2 = new Hashtable<String,String>();
			varTypesNet1 = logFun.getVarTypes(risknet1, varTypesNet1);
			//varTypesNet2 = logFun.getVarTypes(risknet2, varTypesNet2);
			
			//getting guard costs
			Vector<GuardCosts> guardCostsNet1 = new Vector<GuardCosts>();
			//Vector<GuardCosts> guardCostsNet2 = new Vector<GuardCosts>();
			guardCostsNet1 = gui4.getGuardCosts(risknet1, guardCostsNet1);
			//guardCostsNet2 = gui.getGuardCosts(risknet2, guardCostsNet2);
			*/
			
			// 1. Log pre-processing - adding task_time, resource, type attribute
			
			XLog processedLog1;
			XLog testprocessedLog1;
			context.log("Preparing logs");
			
			if(ip.preprocess)
			{
				processedLog1 = logFun.preprocessLog(log1, ip);
				testprocessedLog1 = logFun.preprocessLog(testlog1, ip);
			}
			else
			{
				processedLog1 = log1;
				testprocessedLog1 = testlog1;
			}
		
			
			
			// 2. Populating TSslot Vectors - cases that were active during the end of time slot
			
			Vector <TSslot> cases1 = new Vector <TSslot>();
			Vector <TSslot> casesOutcome = new Vector <TSslot>();
			Vector <TSslot> testcases1 = new Vector <TSslot>();
			Vector <TSslot> testcasesOutcome = new Vector <TSslot>();
		
			if(ip.tsCaseMethod.equals("start"))
			{
				cases1 = logFun.populateTSwithCasesStarted(cases1, processedLog1,ip);
				casesOutcome = logFun.populateTSwithCasesStarted(casesOutcome, processedLog1,ip);
				testcases1 = logFun.populateTSwithCasesStarted(testcases1, testprocessedLog1,ip);
				testcasesOutcome = logFun.populateTSwithCasesStarted(testcasesOutcome, testprocessedLog1,ip);
		
				
			}	
			
			if(ip.tsCaseMethod.equals("complete"))
			{
				cases1 = logFun.populateTSwithCasesCompleted(cases1, processedLog1,ip);
				casesOutcome = logFun.populateTSwithCasesCompleted(casesOutcome, processedLog1,ip);
				testcases1 = logFun.populateTSwithCasesCompleted(testcases1, testprocessedLog1,ip);
				testcasesOutcome = logFun.populateTSwithCasesCompleted(testcasesOutcome, testprocessedLog1,ip);
	
				
			}
			
			if(ip.tsCaseMethod.equals("activeInTS"))
			{
				cases1 = logFun.populateTSwithCasesActiveBA(cases1, processedLog1,ip);
				casesOutcome = logFun.populateTSwithCasesActiveBA(casesOutcome, processedLog1,ip);
				testcases1 = logFun.populateTSwithCasesActiveBA(testcases1, testprocessedLog1,ip);
				testcasesOutcome = logFun.populateTSwithCasesActiveBA(testcasesOutcome, testprocessedLog1,ip);
		
			}
			
			if(ip.tsCaseMethod.equals("active"))
			{
				cases1 = logFun.populateTSwithCasesActive(cases1, processedLog1,ip);
				casesOutcome = logFun.populateTSwithCasesActive(casesOutcome, processedLog1,ip);
				testcases1 = logFun.populateTSwithCasesActive(testcases1, testprocessedLog1,ip);
				testcasesOutcome = logFun.populateTSwithCasesActive(testcasesOutcome, testprocessedLog1,ip);
	
				
			}
			
			if(ip.tsCaseMethod.equals("activeEndTS"))
			{
				cases1 = logFun.populateTSwithCasesActiveEndOfSlot(cases1, processedLog1,ip);
				casesOutcome = logFun.populateTSwithCasesActiveEndOfSlot(casesOutcome, processedLog1,ip);
				testcases1 = logFun.populateTSwithCasesActiveEndOfSlot(testcases1, testprocessedLog1,ip);
				testcasesOutcome = logFun.populateTSwithCasesActiveEndOfSlot(testcasesOutcome, testprocessedLog1,ip);
	
			}
			
			
			//prev. version
			//cases1 = logFun.populateTSwithCasesActiveEndOfSlot(cases1, processedLog1, ip); 
			//casesOutcome = logFun.populateTSwithCasesActiveEndOfSlot(casesOutcome, processedLog1, ip);	
			//testcases1 = logFun.populateTSwithCasesActiveEndOfSlot(testcases1, testprocessedLog1, ip); 
			//testcasesOutcome = logFun.populateTSwithCasesActiveEndOfSlot(testcasesOutcome, testprocessedLog1, ip);	
		
			// 3. Processing logs for replay - truncating traces at a slot end point in risk logs
			
			// truncating cases and/or replacing caseID with caseID+tsID
			XLog truncatedCasesLog1;
			XLog testtruncatedCasesLog1;		
			
			if(ip.truncate)		
			{
				truncatedCasesLog1 = logFun.truncateCases(processedLog1, cases1); // truncates cases at end of slot
				testtruncatedCasesLog1 = logFun.truncateCases(testprocessedLog1, testcases1); // truncates cases at end of slot
					
			}	
			else
			{
				truncatedCasesLog1 = logFun.processTSCases(processedLog1, cases1); // replacing caseID with caseID+tsID - cases not truncated
				testtruncatedCasesLog1 = logFun.processTSCases(testprocessedLog1, testcases1); // replacing caseID with caseID+tsID - cases not truncated
			
			}

			
			//prev. version
			//XLog truncatedCasesLog1 = logFun.truncateCases(processedLog1, cases1);
			//XLog truncatedCasesLog2 = logFun.truncateCases(processedLog2, cases2);
			//			
			//XLog testtruncatedCasesLog1 = logFun.truncateCases(testprocessedLog1, testcases1);
			//XLog testtruncatedCasesLog2 = logFun.truncateCases(testprocessedLog2, testcases2);
				
			
		   // 4. Replaying logs 
			
			cases1 = logFun.replayLogNew(context, risknet1, truncatedCasesLog1, cases1, ip.moveOnModelCosts, ip.moveOnLogCosts, ip.moveBothCost, ip.guardCosts,ip.varNotWrittenCosts,ip.varTypes, ip);
			testcases1 = logFun.replayLogNew(context, risknet1, testtruncatedCasesLog1, testcases1, ip.moveOnModelCosts, ip.moveOnLogCosts, ip.moveBothCost, ip.guardCosts,ip.varNotWrittenCosts,ip.varTypes, ip);
			
			//prev. version
			//cases2 = logFun.replayLogNew(context, risknet2, truncatedCasesLog2, cases2, moveOnModelCostsNet2, moveOnLogCostsLog2, ip.moveBothCost, guardCostsNet2,varNotWrittenCostsNet2,varTypesNet2, ip);
			//testcases2 = logFun.replayLogNew(context, risknet2, testtruncatedCasesLog2, testcases2, moveOnModelCostsNet2, moveOnLogCostsLog2, ip.moveBothCost, guardCostsNet2,varNotWrittenCostsNet2,varTypesNet2, ip);
		
			context.log("Evaluating Overall Process Risk");
			
			// 5.1 Getting case outcomes
			
			if(ip.taskPrevCaseAttr.equals("case_attribute"))
			{
			casesOutcome = logFun.getCaseOutcomes(casesOutcome, processedLog1, ip); 
			testcasesOutcome = logFun.getCaseOutcomes(testcasesOutcome, testprocessedLog1, ip);
			}else
			{
			casesOutcome = logFun.getLastTaskOutcomes(casesOutcome, processedLog1, ip); //In 'last task outcome' version 
			testcasesOutcome = logFun.getLastTaskOutcomes(testcasesOutcome, testprocessedLog1, ip); 
			}

			// 5.2 Getting aggregated case outcome
			if(ip.outcomeAggregateMethod.equals("Total"))
			{
			casesOutcome = logFun.getAggregateOutcome(casesOutcome, ip); 
			testcasesOutcome = logFun.getAggregateOutcome(testcasesOutcome, ip); 
			}
			
			if(ip.outcomeAggregateMethod.equals("Fraction"))
			{
			casesOutcome = logFun.getAggregateOutcomeFraction(casesOutcome, ip); 
			testcasesOutcome = logFun.getAggregateOutcomeFraction(testcasesOutcome, ip); 
			}
		
			if(ip.outcomeAggregateMethod.equals("Mean"))
			{
			casesOutcome = logFun.getAggregateOutcomeMean(casesOutcome, ip); 
			testcasesOutcome = logFun.getAggregateOutcomeMean(testcasesOutcome, ip); 
			}
		
			//prev. version
			//casesOutcome = logFun.getAggregateOutcomeNotDelayed(casesOutcome); 
			//testcasesOutcome = logFun.getAggregateOutcomeNotDelayed(testcasesOutcome); 
		
		/*	for(int i=0; i<casesOutcome.size(); i++)
			{System.out.println(casesOutcome.elementAt(i).slotStart);
			System.out.println(casesOutcome.elementAt(i).slotEnd);
			System.out.println(casesOutcome.elementAt(i).cases);
			System.out.println(casesOutcome.elementAt(i).outcomes);
			System.out.println(casesOutcome.elementAt(i).aggregateOutcome);
			}
	*/	
			// 6. Aggregated time series of risks:
		
			String overallrisk = "";
			String testoverallrisk = "";
			
			if (ip.riskMethod.equals("total"))
			{
			overallrisk = logFun.getTotalRiskTSNew(cases1);
			testoverallrisk = logFun.getTotalRiskTSNew(testcases1);
			
			//System.out.println("overallrisk: "+overallrisk);
			//System.out.println("testoverallrisk: "+testoverallrisk);
			
			//TimeSeries totalriskts = vis.getTS( ip, "Total Risk", overallrisk);
			//vis.plotOneTS(ip, totalriskts, "Total Risk");
			//TimeSeries testtotalriskts = vis.getTS( ip, "Total Risk - test", testoverallrisk);
			//vis.plotOneTS(ip, testtotalriskts, "Total Risk - test");
		
			}
			
			if (ip.riskMethod.equals("mean"))
			{
			
				overallrisk = logFun.getMeanRiskTSNew(cases1);
				testoverallrisk = logFun.getMeanRiskTSNew(testcases1);
				
				//System.out.println("overallrisk: "+overallrisk);
				//System.out.println("testoverallrisk: "+testoverallrisk);
				
			//	TimeSeries totalriskts = vis.getTS( ip, "Mean Risk", overallrisk);
			//	vis.plotOneTS(ip, totalriskts, "Mean Risk");
			//	TimeSeries testtotalriskts = vis.getTS( ip, "Mean Risk - test", testoverallrisk);
			//	vis.plotOneTS(ip, testtotalriskts, "Mean Risk - test");
			
			}
		
			
			
			//System.out.println("Training data results:");
			
				//String totalRisk1 = logFun.getTotalRiskTSNew(cases1);
				//System.out.println("Total risk 1: "+totalRisk1);
						
				//String meanRisk1 = logFun.getMeanRiskTSNew(cases1);
				//System.out.println("Mean risk 1: "+meanRisk1);
					
			//	String totalRisk2 = logFun.getTotalRiskTSNew(cases2);
			//	System.out.println("Total risk 2: "+totalRisk2);
						
			//	String meanRisk2 = logFun.getMeanRiskTSNew(cases2);
			//	System.out.println("Mean risk 2: "+meanRisk2);
				
		//System.out.println("Test data results:");
				
				
				//String testtotalRisk1 = logFun.getTotalRiskTSNew(testcases1);
				//System.out.println("Total risk 1 test: "+testtotalRisk1);
						
				//String testmeanRisk1 = logFun.getMeanRiskTSNew(testcases1);
				//System.out.println("Mean risk 1 test: "+testmeanRisk1);
					
			//	String testtotalRisk2 = logFun.getTotalRiskTSNew(testcases2);
			//	System.out.println("Total risk 2 test: "+testtotalRisk2);
						
			//	String testmeanRisk2 = logFun.getMeanRiskTSNew(testcases2);
			//	System.out.println("Mean risk 2 test: "+testmeanRisk2);
	
	// 7. Aggregate TS for Outcomes:
				
				String aggrOut = logFun.getAggregateOutcomeTS(casesOutcome);
				//System.out.println("Aggregate Outcome TS: "+aggrOut);
		
				String aggrOutTest = logFun.getAggregateOutcomeTS(testcasesOutcome);
				//System.out.println("Aggregate Outcome TS - Test: "+aggrOutTest);
			
	//8. getting R predictions 
					
					context.log("Getting predictions");
					Rengine re=new Rengine (new String [] {"--vanilla"}, false, null);	
					
					//predicting totals:
					//System.out.println("Totals:");
					
					Vector<String> ExpVars = new Vector<String>();
					ExpVars.add(overallrisk);
					//ExpVars.add(totalRisk2);
					
					Vector<String> testVars = new Vector<String>();
					testVars.add(testoverallrisk);
					//testVars.add(testtotalRisk2);
				
					String pred = RFun.getPrediction(ip, re,  aggrOut, ExpVars, testVars);
					//System.out.println("Predicted - total: "+pred);
					String mae = RFun.getMAE(ip, re,  aggrOutTest, pred);
					//System.out.println("MAE - total: "+mae);
					String r2 = RFun.getRSquared(ip, re,  aggrOutTest, pred);
					//System.out.println("Real R2 - total: "+r2);
					
					
			//9. visualising totals
					
					//TimeSeries risk1 = vis.getTS( ip, "Overall Risk - Training Set", overallrisk);
					//vis.showChart(vis.displayOneTS(ip, risk1, "Overall Risk - Training Set"), "Overall Risk - Training Set");
					//TimeSeries testtotalrisk1TS = vis.getTS( ip, "Overall Risk - Test Set", testoverallrisk);
					//vis.showChart(vis.displayOneTS(ip, testtotalrisk1TS, "Overall Risk - Test Set"), "Overall Risk - Test Set");
					
					TimeSeries totalreal = vis.getTS( ip, "Real Outcomes", aggrOutTest);
					TimeSeries totalpred = vis.getTS( ip, "Predicted Outcomes", pred);
				    
					Double rs = Double.parseDouble(r2);
					Double maes = Double.parseDouble(mae);
					
					r2 = String.format("%.2f", rs);
					mae = String.format("%.2f", maes);
					re.end();
					
					//vis.showChart(vis.displayTwoTS(ip, totalreal, totalpred, "Predicted VS Real Outcomes, R2 is "+r2), "Predicted VS Real Outcomes, R2 is "+r2);
					vis.showChart(vis.displayTwoTS(ip, totalreal, totalpred, "Predicted VS Real Outcomes, R2: "+r2+", MAE: "+mae), "Predicted VS Real Outcomes, R2: "+r2+", MAE: "+mae);
					
											
					return "";
		}
}





//----------------------------- prev. version-----------------------------------------------------------------------------------	
					
			/*	String totalOutcome = logFun.getTotalRiskTS(casesOutcome);
				System.out.println("Total outcome - duration: "+totalOutcome);
							
				String meanOutcome = logFun.getMeanRiskTS(casesOutcome);
				System.out.println("Mean outcome - duration: "+meanOutcome);
						
				String NumberRestarted = logFun.getNumberRestartedTS(casesRestarted);
				System.out.println("Number Restarted: "+NumberRestarted);

				String PercentageRestarted = logFun.getPercentageRestartedTS(casesOutcome,casesRestarted);
				System.out.println("Percentage Restarted: "+PercentageRestarted);

				String Numberdelayed = logFun.getNumberDelayedTS(casesOutcome);
				System.out.println("Number Delayed: "+Numberdelayed);
						
				String PercentageDelayed = logFun.getPercentageDelayedTS(casesOutcome);
				System.out.println("Percentage Delayed: "+PercentageDelayed);

				String NumberDelayedRestarted = logFun.getNumberDelayedRestartedTS(casesOutcome,casesRestarted);
				System.out.println("NumberDelayedRestarted: "+NumberDelayedRestarted);
				
				String PercentageDelayedRestarted = logFun.getPercentageDelayedRestartedTS(casesOutcome,casesRestarted);
				System.out.println("PercentageDelayedRestarted: "+PercentageDelayedRestarted);
		*/
				
						
			/*	String testtotalOutcome = logFun.getTotalRiskTS(testcasesOutcome);
				System.out.println("Total outcome - duration test: "+testtotalOutcome);
							
				String testmeanOutcome = logFun.getMeanRiskTS(testcasesOutcome);
				System.out.println("Mean outcome - duration test: "+ testmeanOutcome);
						
				String testNumberRestarted = logFun.getNumberRestartedTS(testcasesRestarted);
				System.out.println("Number Restarted test: "+testNumberRestarted);

				String testPercentageRestarted = logFun.getPercentageRestartedTS(testcasesOutcome,testcasesRestarted);
				System.out.println("Percentage Restarted test: "+testPercentageRestarted);

				String testNumberdelayed = logFun.getNumberDelayedTS(testcasesOutcome);
				System.out.println("Number Delayed test: "+testNumberdelayed);
						
				String testPercentageDelayed = logFun.getPercentageDelayedTS(testcasesOutcome);
				System.out.println("Percentage Delayed test: "+testPercentageDelayed);

				String testNumberDelayedRestarted = logFun.getNumberDelayedRestartedTS(testcasesOutcome,testcasesRestarted);
				System.out.println("NumberDelayedRestarted test: "+testNumberDelayedRestarted);
				
				String testPercentageDelayedRestarted = logFun.getPercentageDelayedRestartedTS(testcasesOutcome,testcasesRestarted);
				System.out.println("PercentageDelayedRestarted test: "+testPercentageDelayedRestarted);
	*/			
				
				//getting R predictions - use this
				
			/*	Rengine re=new Rengine (new String [] {"--vanilla"}, false, null);	
				
				//predicting totals:
				System.out.println("Totals:");
				
				Vector<String> ExpVars = new Vector<String>();
				ExpVars.add(totalRisk1);
				ExpVars.add(totalRisk2);
				
				Vector<String> testVars = new Vector<String>();
				testVars.add(testtotalRisk1);
				testVars.add(testtotalRisk2);
			
				String pred = RFun.getPrediction(ip, re,  NumberDelayedRestarted, ExpVars, testVars);
				System.out.println("Predicted - total: "+pred);
				String mae = RFun.getMAE(ip, re,  testNumberDelayedRestarted, pred);
				System.out.println("MAE - total: "+mae);
				String r2 = RFun.getRSquared(ip, re,  testNumberDelayedRestarted, pred);
				System.out.println("Real R2 - total: "+r2);
	*/			
				
				//visualising totals
				
				//TimeSeries risk1 = vis.getTS( ip, "Total Risk 1 - Training", totalRisk1);
				//TimeSeries risk2 = vis.getTS( ip, "Total Risk 2 - Training", totalRisk2);
				
				//vis.plotOneTS(ip, risk1, "Total Risk 1 - Training");
				//vis.plotOneTS(ip, risk2, "Total Risk 2 - Training");
				
				//TimeSeries testtotalrisk1TS = vis.getTS( ip, "Total Risk 1", testtotalRisk1);
				//TimeSeries testtotalrisk2TS = vis.getTS( ip, "Total Risk 2", testtotalRisk2);
				
				//vis.plotOneTS(ip, testtotalrisk1TS, "Total Risk 1");
				//vis.plotOneTS(ip, testtotalrisk2TS, "Total Risk 2");
			
				//TimeSeries totalreal = vis.getTS( ip, "Real Outcomes - Total", testNumberDelayedRestarted);
				//TimeSeries totalpred = vis.getTS( ip, "Predicted Outcomes - Total", pred);
							
				//vis.plotTwoTS(ip, totalreal, totalpred, "PredictedVSRealOutcomes - Total");
				
				
				//predicting percentages:
			/*	System.out.println("Percentages:");
				
				Vector<String> PercExpVars = new Vector<String>();
				PercExpVars.add(meanRisk1);
				PercExpVars.add(meanRisk2);
				
				Vector<String> PerctestVars = new Vector<String>();
				PerctestVars.add(testmeanRisk1);
				PerctestVars.add(testmeanRisk2);
			
				String predPerc = RFun.getPrediction(ip, re,  PercentageDelayedRestarted, PercExpVars, PerctestVars);
				System.out.println("Predicted - %: "+predPerc);
				String maePerc = RFun.getMAE(ip, re,  testPercentageDelayedRestarted, predPerc);
				System.out.println("MAE - %: "+maePerc);
				String r2Perc = RFun.getRSquared(ip, re,  testPercentageDelayedRestarted, predPerc);
				System.out.println("Real R2 - %: "+r2Perc);
*/
				//visualising percentages
				
				//TimeSeries meantestrisk1 = vis.getTS( ip, "Mean Risk 1", testmeanRisk1);
				//TimeSeries meantestrisk2 = vis.getTS( ip, "Mean Risk 2", testmeanRisk2);
				
				//vis.plotOneTS(ip, meantestrisk1, "Mean Risk 1");
				//vis.plotOneTS(ip, meantestrisk2, "Mean Risk 2");
			
				//TimeSeries meanreal = vis.getTS( ip, "Real Outcomes - Percentage", testPercentageDelayedRestarted);
				//TimeSeries meanpred = vis.getTS( ip, "Predicted Outcomes - Percentage", predPerc);
							
				//vis.plotTwoTS(ip, meanreal, meanpred, "PredictedVSRealOutcomes - Percentage");
			

				
			
		/*		System.out.println("Terminal risks:");
			
			for(int i=0; i<cases1.size(); i++)
			{
				System.out.println(cases1.elementAt(i).slotStart.toString());
				System.out.println(cases1.elementAt(i).slotEnd.toString());
				System.out.println(cases1.elementAt(i).cases.toString());
				System.out.println(cases1.elementAt(i).tsCases.toString());
				System.out.println(cases1.elementAt(i).fitnesses.toString());
				System.out.println(cases1.elementAt(i).meanFitness.toString());
			}
			
			System.out.println("Incident risks:");
			
			for(int i=0; i<cases2.size(); i++)
			{
				System.out.println(cases2.elementAt(i).slotStart.toString());
				System.out.println(cases2.elementAt(i).slotEnd.toString());
				System.out.println(cases2.elementAt(i).cases.toString());
				System.out.println(cases2.elementAt(i).tsCases.toString());
				System.out.println(cases2.elementAt(i).fitnesses.toString());
				System.out.println(cases2.elementAt(i).meanFitness.toString());
			}
	
		System.out.println("Terminal durations:");
			
			for(int i=0; i<casesOutcomeActive.size(); i++)
			{
				System.out.println(casesOutcomeActive.elementAt(i).slotStart.toString());
				System.out.println(casesOutcomeActive.elementAt(i).slotEnd.toString());
				System.out.println(casesOutcomeActive.elementAt(i).cases.toString());
				System.out.println(casesOutcomeActive.elementAt(i).tsCases.toString());
				System.out.println(casesOutcomeActive.elementAt(i).fitnesses.toString());
				System.out.println(casesOutcomeActive.elementAt(i).meanFitness.toString());
			}
*/			
			
					
			//XLog processedCasesLogRestarted = logFun.processTSCases(processedLog2, casesOutcomeRestarted);
			//casesOutcomeRestarted = logFun.replayLog(context, net2, processedCasesLogRestarted, casesOutcomeRestarted);
			
		/*	System.out.println("Terminals restarted:");
			
			for(int i=0; i<casesOutcomeRestarted.size(); i++)
			{
				System.out.println(casesOutcomeRestarted.elementAt(i).slotStart.toString());
				System.out.println(casesOutcomeRestarted.elementAt(i).slotEnd.toString());
				System.out.println(casesOutcomeRestarted.elementAt(i).cases.toString());
				System.out.println(casesOutcomeRestarted.elementAt(i).tsCases.toString());
				System.out.println(casesOutcomeRestarted.elementAt(i).fitnesses.toString());
				System.out.println(casesOutcomeRestarted.elementAt(i).meanFitness.toString());
			}
	*/		
			

			
// --------OLD BELOW--------------------------------------------------------------------------------			
//--------------------Testing functions from the first version of implementation-----------------	
			
			
		/*	Vector <TraceReplay> cases = new Vector <TraceReplay>();
			
			// 1 - parsing log and populating the vector with case IDs, start and end dates
			
			cases = logFun.getCaseIDDate(log, cases);	
						
			// 2 - calling function that populates the vector with replay fitnesses
			
			cases = logFun.getCaseFitness(context, net, log, cases);	
			
			for(int i=0; i<cases.size(); i++)
			{
				System.out.println(cases.elementAt(i).caseID);
				System.out.println(cases.elementAt(i).caseStart.toString());
				System.out.println(cases.elementAt(i).caseEnd.toString());
				System.out.println(cases.elementAt(i).fitness.toString());
			}
	*/	
			
			// 3 - calling function that creates mean time series of process risk
			
			//ret = logFun.getTS(ip, cases);
			
			// TS analysis with R
			
			//Rengine re=new Rengine (new String [] {"--vanilla"}, false, null);	
			
			//String testts = "0.8,0.9,0.8,0.9,0.8,0.9,0.8,0.9,0.8,1.9,0.8,0.9,0.8,0.9,0.8,0.9,0.8,0.9,0.8,0.6,0.5,0.4,0.3,0.2,0.2";
			//String testts = "1,2,3,4,5,6,6,6,6,6,7,7,7,7,7,8,8,8,8,9,9,9,9";
			
			//ret = RFun.getCP(ip, re, testts);
			//ret = RFun.getOutliers(ip, re, testts);
			//ret = RFun.getTrend(ip, re, testts, 19);
			//RFun.getARIMAForecast(ip, re, testts);
					
			//re.end();
			//System.out.println(ret);
			
			//ret = "Total risk: "+totalRisk + "--- Outcomes: " + meanOutcome;
			
		
		//}
/*
		@UITopiaVariant(uiLabel = "000 - Process Risk Prediction - One process",
				affiliation = "uni", 
				author = "author", 
				email = "email"
			)		
		@PluginVariant(variantLabel = "One process, different training and test data", requiredParameterLabels = { 0, 1, 2 })		
		public String  mainOne(UIPluginContext context, final XLog log1, final XLog testlog1, PetriNetWithData risknet1) throws Exception  
				{
					
					String ret = "test";			
					
					// 1. Log pre-processing - adding task_time, resource, type attribute
					
					XLog processedLog1 = logFun.preprocessLog(log1, ip);
					//XLog processedLog2 = logFun.preprocessLog(log2);
					//XLog processedOutLog = logFun.preprocessLog(outlog);
					
					XLog testprocessedLog1 = logFun.preprocessLog(testlog1, ip);
					//XLog testprocessedLog2 = logFun.preprocessLog(testlog2);
					//XLog testprocessedOutLog = logFun.preprocessLog(testoutlog);
				
					
					
					// 2. Populating TSslot Vectors - cases that were active during the end of time slot
					
					Vector <TSslot> cases1 = new Vector <TSslot>();
					Vector <TSslot> cases2 = new Vector <TSslot>();
					Vector <TSslot> casesOutcome = new Vector <TSslot>();
					
					cases1 = logFun.populateTSwithCasesActiveOutcome(cases1, processedLog1, ip); 
					//cases2 = logFun.populateTSwithCasesActiveOutcome(cases2, processedLog2); 
					//casesOutcome = logFun.populateTSwithCasesActiveOutcome(casesOutcome, processedOutLog);	
					
					Vector <TSslot> testcases1 = new Vector <TSslot>();
					Vector <TSslot> testcases2 = new Vector <TSslot>();
					Vector <TSslot> testcasesOutcome = new Vector <TSslot>();
					
					testcases1 = logFun.populateTSwithCasesActiveOutcome(testcases1, testprocessedLog1, ip); 
					//testcases2 = logFun.populateTSwithCasesActiveOutcome(testcases2, testprocessedLog2); 
					//testcasesOutcome = logFun.populateTSwithCasesActiveOutcome(testcasesOutcome, testprocessedOutLog);	
				
					
					
					
					// 3. Processing logs for replay - truncating traces at a slot end point in risk logs
					
					XLog truncatedCasesLog1 = logFun.truncateCases(processedLog1, cases1);
					//XLog truncatedCasesLog2 = logFun.truncateCases(processedLog2, cases2);
					//XLog processedOutcomeLog = logFun.processTSCases(processedOutLog, casesOutcome);
					
					XLog testtruncatedCasesLog1 = logFun.truncateCases(testprocessedLog1, testcases1);
					//XLog testtruncatedCasesLog2 = logFun.truncateCases(testprocessedLog2, testcases2);
					//XLog testprocessedOutcomeLog = logFun.processTSCases(testprocessedOutLog, testcasesOutcome);
			
					
				   // 4. Replaying logs 
					
					cases1 = logFun.replayLog(context, risknet1, truncatedCasesLog1, cases1, ip);
					//cases2 = logFun.replayLog(context, risknet2, truncatedCasesLog2, cases2);
					//casesOutcome = logFun.replayLog(context, outcomenet, processedOutcomeLog, casesOutcome);
					
					testcases1 = logFun.replayLog(context, risknet1, testtruncatedCasesLog1, testcases1, ip);
					//testcases2 = logFun.replayLog(context, risknet2, testtruncatedCasesLog2, testcases2);
					//testcasesOutcome = logFun.replayLog(context, outcomenet, testprocessedOutcomeLog, testcasesOutcome);
				
					
					// 5 Outcome cases - restarted (incidents opened)
					
					Vector <TSslot> casesRestarted = new Vector <TSslot>();
					Vector <TSslot> testcasesRestarted = new Vector <TSslot>();
					
					//casesRestarted = logFun.getRestartedTS(casesRestarted, casesOutcome, processedLog2); 
					//testcasesRestarted = logFun.getRestartedTS(testcasesRestarted, testcasesOutcome, testprocessedLog2); 

					// 6. Aggregated time series of risks and outcomes:
					
					System.out.println("Training data results:");
					
						String totalRisk1 = logFun.getTotalRiskTS(cases1);
						System.out.println("Total risk 1: "+totalRisk1);
								
						String meanRisk1 = logFun.getMeanRiskTS(cases1);
						System.out.println("Mean risk 1: "+meanRisk1);
							
						String totalRisk2 = logFun.getTotalRiskTS(cases2);
						System.out.println("Total risk 2: "+totalRisk2);
								
						String meanRisk2 = logFun.getMeanRiskTS(cases2);
						System.out.println("Mean risk 2: "+meanRisk2);
							
						String totalOutcome = logFun.getTotalRiskTS(casesOutcome);
						System.out.println("Total outcome - duration: "+totalOutcome);
									
						String meanOutcome = logFun.getMeanRiskTS(casesOutcome);
						System.out.println("Mean outcome - duration: "+meanOutcome);
								
						String NumberRestarted = logFun.getNumberRestartedTS(casesRestarted);
						System.out.println("Number Restarted: "+NumberRestarted);

						String PercentageRestarted = logFun.getPercentageRestartedTS(casesOutcome,casesRestarted, ip);
						System.out.println("Percentage Restarted: "+PercentageRestarted);

						String Numberdelayed = logFun.getNumberDelayedTS(casesOutcome);
						System.out.println("Number Delayed: "+Numberdelayed);
								
						String PercentageDelayed = logFun.getPercentageDelayedTS(casesOutcome, ip);
						System.out.println("Percentage Delayed: "+PercentageDelayed);

						String NumberDelayedRestarted = logFun.getNumberDelayedRestartedTS(casesOutcome,casesRestarted, ip);
						System.out.println("NumberDelayedRestarted: "+NumberDelayedRestarted);
						
						String PercentageDelayedRestarted = logFun.getPercentageDelayedRestartedTS(casesOutcome,casesRestarted, ip);
						System.out.println("PercentageDelayedRestarted: "+PercentageDelayedRestarted);
				
						
						System.out.println("Test data results:");
						
						
						String testtotalRisk1 = logFun.getTotalRiskTS(testcases1);
						System.out.println("Total risk 1 test: "+testtotalRisk1);
								
						String testmeanRisk1 = logFun.getMeanRiskTS(testcases1);
						System.out.println("Mean risk 1 test: "+testmeanRisk1);
							
						String testtotalRisk2 = logFun.getTotalRiskTS(testcases2);
						System.out.println("Total risk 2 test: "+testtotalRisk2);
								
						String testmeanRisk2 = logFun.getMeanRiskTS(testcases2);
						System.out.println("Mean risk 2 test: "+testmeanRisk2);
							
						String testtotalOutcome = logFun.getTotalRiskTS(testcasesOutcome);
						System.out.println("Total outcome - duration test: "+testtotalOutcome);
									
						String testmeanOutcome = logFun.getMeanRiskTS(testcasesOutcome);
						System.out.println("Mean outcome - duration test: "+ testmeanOutcome);
								
						String testNumberRestarted = logFun.getNumberRestartedTS(testcasesRestarted);
						System.out.println("Number Restarted test: "+testNumberRestarted);

						String testPercentageRestarted = logFun.getPercentageRestartedTS(testcasesOutcome,testcasesRestarted, ip);
						System.out.println("Percentage Restarted test: "+testPercentageRestarted);

						String testNumberdelayed = logFun.getNumberDelayedTS(testcasesOutcome);
						System.out.println("Number Delayed test: "+testNumberdelayed);
								
						String testPercentageDelayed = logFun.getPercentageDelayedTS(testcasesOutcome, ip);
						System.out.println("Percentage Delayed test: "+testPercentageDelayed);

						String testNumberDelayedRestarted = logFun.getNumberDelayedRestartedTS(testcasesOutcome,testcasesRestarted, ip);
						System.out.println("NumberDelayedRestarted test: "+testNumberDelayedRestarted);
						
						String testPercentageDelayedRestarted = logFun.getPercentageDelayedRestartedTS(testcasesOutcome,testcasesRestarted, ip);
						System.out.println("PercentageDelayedRestarted test: "+testPercentageDelayedRestarted);
						
						
						//getting R predictions
						
						Rengine re=new Rengine (new String [] {"--vanilla"}, false, null);	
						
						//predicting totals:
						System.out.println("Totals:");
						
						Vector<String> ExpVars = new Vector<String>();
						ExpVars.add(totalRisk1);
						ExpVars.add(totalRisk2);
						
						Vector<String> testVars = new Vector<String>();
						testVars.add(testtotalRisk1);
						testVars.add(testtotalRisk2);
					
						String pred = RFun.getPrediction(ip, re,  NumberDelayedRestarted, ExpVars, testVars);
						System.out.println("Predicted - total: "+pred);
						String mae = RFun.getMAE(ip, re,  testNumberDelayedRestarted, pred);
						System.out.println("MAE - total: "+mae);
						String r2 = RFun.getRSquared(ip, re,  testNumberDelayedRestarted, pred);
						System.out.println("Real R2 - total: "+r2);
						
						
						//visualising totals
						
						//TimeSeries risk1 = vis.getTS( ip, "Total Risk 1 - Training", totalRisk1);
						//TimeSeries risk2 = vis.getTS( ip, "Total Risk 2 - Training", totalRisk2);
						
						//vis.plotOneTS(ip, risk1, "Total Risk 1 - Training");
						//vis.plotOneTS(ip, risk2, "Total Risk 2 - Training");
						
						TimeSeries testtotalrisk1TS = vis.getTS( ip, "Total Risk 1", testtotalRisk1);
						TimeSeries testtotalrisk2TS = vis.getTS( ip, "Total Risk 2", testtotalRisk2);
						
						vis.plotOneTS(ip, testtotalrisk1TS, "Total Risk 1");
						vis.plotOneTS(ip, testtotalrisk2TS, "Total Risk 2");
					
						TimeSeries totalreal = vis.getTS( ip, "Real Outcomes - Total", testNumberDelayedRestarted);
						TimeSeries totalpred = vis.getTS( ip, "Predicted Outcomes - Total", pred);
									
						vis.plotTwoTS(ip, totalreal, totalpred, "PredictedVSRealOutcomes - Total");
						
						
						//predicting percentages:
						System.out.println("Percentages:");
						
						Vector<String> PercExpVars = new Vector<String>();
						PercExpVars.add(meanRisk1);
						PercExpVars.add(meanRisk2);
						
						Vector<String> PerctestVars = new Vector<String>();
						PerctestVars.add(testmeanRisk1);
						PerctestVars.add(testmeanRisk2);
					
						String predPerc = RFun.getPrediction(ip, re,  PercentageDelayedRestarted, PercExpVars, PerctestVars);
						System.out.println("Predicted - %: "+predPerc);
						String maePerc = RFun.getMAE(ip, re,  testPercentageDelayedRestarted, predPerc);
						System.out.println("MAE - %: "+maePerc);
						String r2Perc = RFun.getRSquared(ip, re,  testPercentageDelayedRestarted, predPerc);
						System.out.println("Real R2 - %: "+r2Perc);

						//visualising percentages
						
						TimeSeries meantestrisk1 = vis.getTS( ip, "Mean Risk 1", testmeanRisk1);
						TimeSeries meantestrisk2 = vis.getTS( ip, "Mean Risk 2", testmeanRisk2);
						
						vis.plotOneTS(ip, meantestrisk1, "Mean Risk 1");
						vis.plotOneTS(ip, meantestrisk2, "Mean Risk 2");
					
						TimeSeries meanreal = vis.getTS( ip, "Real Outcomes - Percentage", testPercentageDelayedRestarted);
						TimeSeries meanpred = vis.getTS( ip, "Predicted Outcomes - Percentage", predPerc);
									
						vis.plotTwoTS(ip, meanreal, meanpred, "PredictedVSRealOutcomes - Percentage");
					

						re.end();
						return ret;
					
						System.out.println("Terminal risks:");
					
					for(int i=0; i<cases1.size(); i++)
					{
						System.out.println(cases1.elementAt(i).slotStart.toString());
						System.out.println(cases1.elementAt(i).slotEnd.toString());
						System.out.println(cases1.elementAt(i).cases.toString());
						System.out.println(cases1.elementAt(i).tsCases.toString());
						System.out.println(cases1.elementAt(i).fitnesses.toString());
						System.out.println(cases1.elementAt(i).meanFitness.toString());
					}
					
					System.out.println("Incident risks:");
					
					for(int i=0; i<cases2.size(); i++)
					{
						System.out.println(cases2.elementAt(i).slotStart.toString());
						System.out.println(cases2.elementAt(i).slotEnd.toString());
						System.out.println(cases2.elementAt(i).cases.toString());
						System.out.println(cases2.elementAt(i).tsCases.toString());
						System.out.println(cases2.elementAt(i).fitnesses.toString());
						System.out.println(cases2.elementAt(i).meanFitness.toString());
					}
			
				System.out.println("Terminal durations:");
					
					for(int i=0; i<casesOutcomeActive.size(); i++)
					{
						System.out.println(casesOutcomeActive.elementAt(i).slotStart.toString());
						System.out.println(casesOutcomeActive.elementAt(i).slotEnd.toString());
						System.out.println(casesOutcomeActive.elementAt(i).cases.toString());
						System.out.println(casesOutcomeActive.elementAt(i).tsCases.toString());
						System.out.println(casesOutcomeActive.elementAt(i).fitnesses.toString());
						System.out.println(casesOutcomeActive.elementAt(i).meanFitness.toString());
					}
					
					
							
					//XLog processedCasesLogRestarted = logFun.processTSCases(processedLog2, casesOutcomeRestarted);
					//casesOutcomeRestarted = logFun.replayLog(context, net2, processedCasesLogRestarted, casesOutcomeRestarted);
					
					System.out.println("Terminals restarted:");
					
					for(int i=0; i<casesOutcomeRestarted.size(); i++)
					{
						System.out.println(casesOutcomeRestarted.elementAt(i).slotStart.toString());
						System.out.println(casesOutcomeRestarted.elementAt(i).slotEnd.toString());
						System.out.println(casesOutcomeRestarted.elementAt(i).cases.toString());
						System.out.println(casesOutcomeRestarted.elementAt(i).tsCases.toString());
						System.out.println(casesOutcomeRestarted.elementAt(i).fitnesses.toString());
						System.out.println(casesOutcomeRestarted.elementAt(i).meanFitness.toString());
					}
					
					

					
		// --------OLD BELOW--------------------------------------------------------------------------------			
		//--------------------Testing functions from the first version of implementation-----------------	
					
					
					Vector <TraceReplay> cases = new Vector <TraceReplay>();
					
					// 1 - parsing log and populating the vector with case IDs, start and end dates
					
					cases = logFun.getCaseIDDate(log, cases);	
								
					// 2 - calling function that populates the vector with replay fitnesses
					
					cases = logFun.getCaseFitness(context, net, log, cases);	
					
					for(int i=0; i<cases.size(); i++)
					{
						System.out.println(cases.elementAt(i).caseID);
						System.out.println(cases.elementAt(i).caseStart.toString());
						System.out.println(cases.elementAt(i).caseEnd.toString());
						System.out.println(cases.elementAt(i).fitness.toString());
					}
				
					
					// 3 - calling function that creates mean time series of process risk
					
					//ret = logFun.getTS(ip, cases);
					
					// TS analysis with R
					
					//Rengine re=new Rengine (new String [] {"--vanilla"}, false, null);	
					
					//String testts = "0.8,0.9,0.8,0.9,0.8,0.9,0.8,0.9,0.8,1.9,0.8,0.9,0.8,0.9,0.8,0.9,0.8,0.9,0.8,0.6,0.5,0.4,0.3,0.2,0.2";
					//String testts = "1,2,3,4,5,6,6,6,6,6,7,7,7,7,7,8,8,8,8,9,9,9,9";
					
					//ret = RFun.getCP(ip, re, testts);
					//ret = RFun.getOutliers(ip, re, testts);
					//ret = RFun.getTrend(ip, re, testts, 19);
					//RFun.getARIMAForecast(ip, re, testts);
							
					//re.end();
					//System.out.println(ret);
					
					//ret = "Total risk: "+totalRisk + "--- Outcomes: " + meanOutcome;
					
					
					
					
							
				
				}
				
	*/		
	
//}

