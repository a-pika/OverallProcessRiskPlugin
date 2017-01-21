package org.processmining.plugins.processrisk.output;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Date;
import java.util.Vector;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.util.ShapeUtilities;
import org.processmining.plugins.processrisk.input.InputParameters;



public class VisualiseResults extends ApplicationFrame {
	
	public VisualiseResults(){super("");};
	
	public VisualiseResults(String title, InputParameters ip, TimeSeries ts1) throws Exception {
		super(title);
	    setContentPane(displayOneTS(ip, ts1, title));
	
	}
	
	public VisualiseResults(String title, InputParameters ip, TimeSeries ts1, TimeSeries ts2) throws Exception {
		super(title);
	    setContentPane(displayTwoTS(ip, ts1, ts2, title));
	
	}


   private static final long serialVersionUID = 1L;

   public void showChart(ChartPanel chartPanel, String title)
	{JFrame f = new JFrame(title);
    f.setTitle(title);
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    f.setLayout(new BorderLayout());
    f.setSize(1100, 600);
    f.add(chartPanel, BorderLayout.CENTER);
    f.setLocationRelativeTo(null);
    f.setVisible(true);
	}
	
	public ChartPanel displayOneTS(InputParameters ip, TimeSeries ts1, String title) throws Exception
	{
		
		TimeSeriesCollection ts_dataset1 = new TimeSeriesCollection();
		ts_dataset1.addSeries(ts1);
		
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				title, // Title
				"Date", // x-axis Label
				"Value", // y-axis Label
				ts_dataset1, // Dataset
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		
		return new ChartPanel( chart ); 
		
}

	
	@SuppressWarnings("deprecation")
	public ChartPanel displayTwoTS(InputParameters ip, TimeSeries ts1, TimeSeries ts2, String title) throws Exception
	{
		
			
		TimeSeriesCollection ts_dataset1 = new TimeSeriesCollection();
		ts_dataset1.addSeries(ts1);
		
		
		TimeSeriesCollection ts_dataset2 = new TimeSeriesCollection();
		ts_dataset2.addSeries(ts2);
	
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				title, // Title
				"Date", // x-axis Label
				"Value", // y-axis Label
				ts_dataset1, // Dataset
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		
		
		XYPlot xyplot = chart.getXYPlot();
		
		
		xyplot.setDataset(2,ts_dataset2);
		XYLineAndShapeRenderer rr2 = new XYLineAndShapeRenderer();
		rr2.setSeriesLinesVisible(0, true);
		rr2.setSeriesShapesVisible(0, false);
		rr2.setPaint(Color.blue);
		chart.getXYPlot().setRenderer(2,rr2);

		
		return new ChartPanel(chart);
		
	}

	
public TimeSeries getTS(InputParameters ip, String tsname, String plotTS) throws Exception
{
	
	TimeSeries ts = new TimeSeries(tsname);	
	
	
	String outts = "";
	outts = plotTS;
	String ts_array[] = outts.split(",");
	Vector<Double> ts_values = new Vector<Double>();
	for(int i=0; i<ts_array.length; i++)
	{
		ts_values.add(Double.valueOf(ts_array[i]));
		//System.out.println(ts_values.elementAt(i).toString());
	}
	
	Vector<Date> ts_times = new Vector<Date>();	
	long tsPointTime=ip.startTime;
	
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		tsPointTime+=ip.slotSize;
		ts_times.add(new Date (tsPointTime));
	}
	
		
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		ts.add(new Day(ts_times.elementAt(i)),ts_values.elementAt(i));
		
		//System.out.println(ts.getDataItem(i).getPeriod()+" - "+ts.getDataItem(i).getValue());
		
	}
	
	
	
	return ts;
	
}



public TimeSeries getTSCP(InputParameters ip, String plotTS, String change_points) throws Exception
{
	
	
	String outts = "";
	outts = plotTS;
	String ts_array[] = outts.split(",");
	Vector<Double> ts_values = new Vector<Double>();
	for(int i=0; i<ts_array.length; i++)
	{
		ts_values.add(Double.valueOf(ts_array[i]));
		//System.out.println(ts_values.elementAt(i).toString());
	}
	
	Vector<Date> ts_times = new Vector<Date>();	
	long tsPointTime=ip.startTime;
	
	for(int i=0; i<ip.numberOfSlots; i++)
	{
		tsPointTime+=ip.slotSize;
		ts_times.add(new Date (tsPointTime));
	}
	
	
	//---------------------------------------------
	TimeSeries ts = new TimeSeries("Change Points");	
	//String times = "";
	
	String tscp = change_points;
	String cp_array[] = tscp.split(",");
	Vector<Integer> cp_times = new Vector<Integer>();
	
if(cp_array.length > 0 && !cp_array[0].equals(""))
{	
	for(int i=0; i<cp_array.length; i++)
	{
		cp_times.add(Integer.valueOf(cp_array[i]));
		//System.out.println(ts_values.elementAt(i).toString());
	}
	
	for(int i=0; i<cp_times.size(); i++)
	{
		ts.add(new Day(ts_times.elementAt(cp_times.elementAt(i)-1)),ts_values.elementAt(cp_times.elementAt(i)-1));
		
		//System.out.println(ts.getDataItem(i).getPeriod()+" - "+ts.getDataItem(i).getValue());
		
	}
}	
	
	return ts;
	
}




@SuppressWarnings("deprecation")
public ChartPanel plotTSDisplayNew(TimeSeries ts, TimeSeries cp) throws Exception
{
	
	TimeSeriesCollection ts_dataset = new TimeSeriesCollection();
	ts_dataset.addSeries(ts);
	
	TimeSeriesCollection cp_dataset = new TimeSeriesCollection();
	cp_dataset.addSeries(cp);
	
	String title = "Overall Process Risk";
	
	JFreeChart chart = ChartFactory.createTimeSeriesChart(
			title, // Title
			"Date", // x-axis Label
			"OPR", // y-axis Label
			ts_dataset, // Dataset
			true, // Show Legend
			true, // Use tooltips
			false // Configure chart to generate URLs?
			);
	
	
	XYPlot xyplot = chart.getXYPlot();
	
	xyplot.setDataset(0,cp_dataset);
	XYLineAndShapeRenderer rr0 = new XYLineAndShapeRenderer();
	rr0.setSeriesLinesVisible(0, false);
	rr0.setSeriesShapesVisible(0, true);
	rr0.setPaint(Color.MAGENTA);//yellow
	rr0.setSeriesShape(0,ShapeUtilities.createUpTriangle(6.0f));
	chart.getXYPlot().setRenderer(0,rr0);
			
	
	xyplot.setDataset(2,ts_dataset);
	XYLineAndShapeRenderer rr2 = new XYLineAndShapeRenderer();
	rr2.setSeriesLinesVisible(0, true);
	rr2.setSeriesShapesVisible(0, false);
	rr2.setPaint(Color.blue);
	//rr2.setSeriesOutlineStroke(0, new BasicStroke(10));
	chart.getXYPlot().setRenderer(2,rr2);
	
	return(new ChartPanel(chart));
}



	
}


//----------------------------------------------PREV. VERSIONS-------------------------------------------------
//////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*	public void plotOneTS(InputParameters ip, TimeSeries ts1, String title) throws Exception
{
	
		
	TimeSeriesCollection ts_dataset1 = new TimeSeriesCollection();
	ts_dataset1.addSeries(ts1);
	
	
	JFreeChart chart = ChartFactory.createTimeSeriesChart(
			title, // Title
			"Date", // x-axis Label
			"Value", // y-axis Label
			ts_dataset1, // Dataset
			true, // Show Legend
			true, // Use tooltips
			false // Configure chart to generate URLs?
			);
	
	
	XYPlot xyplot = chart.getXYPlot();
	
	try {	
	ChartUtilities.saveChartAsJPEG(new File("C:\\temp\\"+title+".jpg"), chart, 1200, 400);
	} catch (IOException e) {
	System.err.println("Problem occurred creating chart.");
	}

}
*/


/*	
	
	public void plotTwoTS(InputParameters ip, TimeSeries ts1, TimeSeries ts2, String title) throws Exception
	{
		
			
		TimeSeriesCollection ts_dataset1 = new TimeSeriesCollection();
		ts_dataset1.addSeries(ts1);
		
		
		TimeSeriesCollection ts_dataset2 = new TimeSeriesCollection();
		ts_dataset2.addSeries(ts2);
	
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				title, // Title
				"Date", // x-axis Label
				"Value", // y-axis Label
				ts_dataset1, // Dataset
				true, // Show Legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
				);
		
		
		XYPlot xyplot = chart.getXYPlot();
		
		
		xyplot.setDataset(1,ts_dataset1);
		XYLineAndShapeRenderer rr1 = new XYLineAndShapeRenderer();
		rr1.setSeriesLinesVisible(0, true);
		rr1.setSeriesShapesVisible(0, false);
		rr1.setPaint(Color.red);
		//rr2.setSeriesOutlineStroke(0, new BasicStroke(10));
		chart.getXYPlot().setRenderer(1,rr1);
		
		
		xyplot.setDataset(2,ts_dataset2);
		XYLineAndShapeRenderer rr2 = new XYLineAndShapeRenderer();
		rr2.setSeriesLinesVisible(0, true);
		rr2.setSeriesShapesVisible(0, false);
		rr2.setPaint(Color.blue);
		chart.getXYPlot().setRenderer(2,rr2);
	
		try {	
		ChartUtilities.saveChartAsJPEG(new File("C:\\temp\\"+title+".jpg"), chart, 1200, 400);
		} catch (IOException e) {
		System.err.println("Problem occurred creating chart.");
		}

	}*/

