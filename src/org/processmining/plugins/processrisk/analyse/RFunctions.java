package org.processmining.plugins.processrisk.analyse;

import java.util.Vector;

import org.processmining.plugins.processrisk.input.InputParameters;
import org.rosuda.JRI.Rengine;

public class RFunctions{
	

	public String getRSquared(InputParameters ip, Rengine re, String real, String predicted) throws Exception
{
	String out = "";	
	re.eval("real <- c("+real+")");
	re.eval("predicted <- c("+predicted+")");
	re.eval("realmean <- mean(real)");
	re.eval("sstot <- sum((real-realmean)^2)");
	re.eval("ssres <- sum((real-predicted)^2)");
	re.eval("r2 <- 1-ssres/sstot");
	
		
	Double r2 = re.eval("r2").asDouble();
	out = r2.toString();
	
	return out;
}

	public String getMAE(InputParameters ip, Rengine re, String real, String predicted) throws Exception
{
	String out = "";	
	re.eval("real <- c("+real+")");
	re.eval("predicted <- c("+predicted+")");
	
	re.eval("mae <-  mean(abs(predicted-real))");
	
	Double mae = re.eval("mae").asDouble();
	out = mae.toString();
	
	return out;
}

	public String getPrediction(InputParameters ip, Rengine re, String depVar, Vector<String> ExpVars, Vector<String> testVars) throws Exception
{
	String expvars = "";
	String testvars = "";
	
	re.eval("y <- c("+depVar+")");
	
	for(int i=0;i<ExpVars.size();i++)
	{
	re.eval("x"+i+" <- c("+ExpVars.elementAt(i)+")");
	expvars+="x"+i+"+";
	}

	if(!expvars.isEmpty())
	{expvars = expvars.substring(0, expvars.length()-1);} 
	
	for(int i=0;i<testVars.size();i++)
	{
	testvars+="x"+i+"=c("+testVars.elementAt(i)+"),";
	}
	if(!testvars.isEmpty())
	{testvars = testvars.substring(0, testvars.length()-1);} 

	//System.out.println("expvars: "+expvars);
	//System.out.println("testvars: "+testvars);

	
	re.eval("new <- data.frame("+testvars+")");
	re.eval("library(np)");
	re.eval("res <- npreg(y~" + expvars +")");
	re.eval("predicted <- predict(res, newdata=new)");
	double[] predicted = re.eval("predicted").asDoubleArray();
	String out = "";
	for (int i=0; i<predicted.length; i++)
	{
	
	double next = (double)Math.round(predicted[i] * ip.decNum) / ip.decNum;
	out += next +",";
	}
	
	if(!out.isEmpty())
	{out = out.substring(0, out.length()-1);} 

	
	//double rsquared = re.eval("res$R2").asDouble();
	//Double rsquaredrounded = (double)Math.round(rsquared * ip.decNum) / ip.decNum;
	//System.out.println("Fitted R2: "+rsquared);
	
	//double mae = re.eval("res$MAE").asDouble();
	//Double rsquaredrounded = (double)Math.round(rsquared * ip.decNum) / ip.decNum;
	//System.out.println("MAE from prediction f-n: "+mae);
	
	//System.out.println("out (predicted from f-n): "+out);

	return out;
}


	public String getCP(Rengine re, String ts) throws Exception
	{
		String change_points = "";
		
		re.eval("library(cpm)");
		String y = "";
			
		String cpmType = "Mann-Whitney"; 
		String ARL0 = "1000"; 
		String startup = "20"; 
	
		
		y = "y <- c("+ts+")"; 
		re.eval(y);
		re.eval("res <- processStream(y, cpmType = \"" + cpmType +"\", ARL0 = " + ARL0 + ", startup = " + startup +")");
		double[] cp = re.eval("res$changePoints").asDoubleArray();
		
		// returns an index of change points in array
		
		for(int i=0;i<cp.length;i++)
		{
			change_points+=(int)cp[i]+",";
		}
		
		if (cp.length>0) {change_points = change_points.substring(0, change_points.length()-1); }	
		
		System.out.println(change_points);
		
		return change_points;
		
}

	
/*
	public String getRegression(InputParameters ip, Rengine re, String depVar, Vector<String> ExpVars, String filename) throws Exception
{
	String expvars = "";
	re.eval("y <- c("+depVar+")");
	
	for(int i=0;i<ExpVars.size();i++)
	{
	re.eval("x"+i+" <- c("+ExpVars.elementAt(i)+")");
	expvars+="x"+i+"+";
	}

	if(!expvars.isEmpty())
	{expvars = expvars.substring(0, expvars.length()-1);} 
	
	
	re.eval("library(np)");
	re.eval("res <- npreg(y~" + expvars +")");
	
	double rsquared = re.eval("res$R2").asDouble();
		
		Double rsquaredrounded = (double)Math.round(rsquared * ip.decNum) / ip.decNum;
	
	return rsquaredrounded.toString();
}
*/
	
/*
	public String getOutliers(InputParameters ip, Rengine re, String ts) throws Exception
	{
		String outliers_right = "";
		String outliers_left = "";
		
		
		re.eval("library(extremevalues)");
		String y = "";
		String method = ip.method;
			
		y = "y <- c("+ts+")"; 
		re.eval(y);
		re.eval("res <- getOutliers(y, method = \"" + method +"\")");
		double[] outr = re.eval("res$iRight").asDoubleArray();
		if (outr != null) {for (int i=0; i<outr.length;i++)
		{
			outliers_right+=(int)outr[i]+",";
		}
		}
		double[] outl = re.eval("res$iLeft").asDoubleArray();
		if (outl != null) {for (int i=0; i<outl.length;i++)
		{
			outliers_left+=(int)outl[i]+",";
		}
		}
		
		if (outr.length>0) {outliers_right = outliers_right.substring(0, outliers_right.length()-1); }	
		if (outl.length>0) {outliers_left = outliers_left.substring(0, outliers_left.length()-1); }	
		
		System.out.println("right: "+outliers_right);
		System.out.println("left: "+outliers_left);
		
		return "right: "+ outliers_right + " - left: "+outliers_left;

	}
*/	
	
/*
public String getTrend (InputParameters ip, Rengine re, String ts, Integer maxCP) throws Exception
	{
		
		String x = "";
		
if(ip.period.equals("full"))
{
		
		String y = "";
		y = "y <- c("+ts+")"; 
		re.eval(y);
		
		int numberOfSlots = ip.numberOfSlots;	
		x = "x <- c(1:"+numberOfSlots+")";
		re.eval(x);
}
else
{
		
	String ts_array[] = ts.split(",");
	String afterCPTS = "";
	for(int i=maxCP; i<ts_array.length; i++)
	{
		afterCPTS+=ts_array[i]+",";
		
	}
	
	
	afterCPTS = (String) afterCPTS.subSequence(0, afterCPTS.length()-1);
	System.out.println(afterCPTS);
	String y = "";
	y = "y <- c("+afterCPTS+")"; 
	re.eval(y);
	
	int numberOfSlots = ip.numberOfSlots;	
	x = "x <- c("+(maxCP+1)+":"+numberOfSlots+")";
	System.out.println(x);
	re.eval(x);
	


}	
		
	re.eval("res <- lm(y~x)");
	double slope = re.eval("res$coefficients[2]").asDouble();
	double intercept = re.eval("res$coefficients[1]").asDouble();
		
		slope = (double)Math.round(slope * ip.decNum) / ip.decNum;
		intercept = (double)Math.round(intercept * ip.decNum) / ip.decNum;

		//System.out.println(slope +"---"+intercept );
		
		return "slope: "+ slope + " - intercept: " + intercept;
		

	}
*/	
/*public void getARIMAForecast(InputParameters ip, Rengine re, String ts) throws Exception
{
	re.eval("library(forecast)");
	re.eval("x <- c("+ts+")");
	re.eval("xfit <- auto.arima(x)");
	re.eval("fcast <- forecast(xfit)");
	re.eval("pdf(\"C:\\\\temp\\\\mygraph123.pdf\")");
	re.eval("plot(fcast)");
	re.eval("dev.off()");	
}
*/		 
	
}




