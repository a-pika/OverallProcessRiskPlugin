package org.processmining.plugins.processrisk.input;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.joda.time.DateTime;
import org.processmining.framework.util.ui.widgets.ProMComboBox;
import org.processmining.framework.util.ui.widgets.ProMScrollPane;
import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.DataElement;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PNWDTransition;
import org.processmining.models.graphbased.directed.petrinetwithdata.newImpl.PetriNetWithData;
import org.processmining.plugins.processrisk.analyse.GuardCosts;
import org.processmining.plugins.processrisk.output.UISettings;

import com.fluxicon.slickerbox.components.HeaderBar;
import com.fluxicon.slickerbox.components.SlickerButton;

@SuppressWarnings("serial")
public class GetUserInput extends JDialog{

@SuppressWarnings("rawtypes")
public InputParameters getRiskCosts(XLog log, PetriNetWithData net, final InputParameters ip)	
{
	ConcurrentSkipListSet<String> activities = new ConcurrentSkipListSet<String>();
	
for (XTrace t : log) {
for (XEvent e : t) {
		String activity = XConceptExtension.instance().extractName(e);
		if(activity != null)
		{activities.add(activity);}
}
}

final Vector<String> items = new Vector<String>();
items.addAll(activities);

final HeaderBar mainPane = new HeaderBar("");
setContentPane(mainPane);
mainPane.setLayout(new GridBagLayout());
GridBagConstraints cMain = new GridBagConstraints();
	
//--------------------------------------------------------------------------	

final HeaderBar pane = new HeaderBar("");	
pane.setLayout(new GridBagLayout());
GridBagConstraints c = new GridBagConstraints();
c.fill = GridBagConstraints.CENTER;

final JLabel titleText=new JLabel();
titleText.setForeground(UISettings.TextLight_COLOR);
titleText.setText("<html><h3>Specify risk costs for execution of tasks not aligned with a model: </h3></html>"); 
c.ipadx = 300;      
c.gridwidth = 2;
c.gridx = 0;
c.gridy = 0;
pane.add(titleText, c);
	
final Vector<JLabel> labs = new Vector<JLabel>();
final Vector<ProMTextField> textFields = new Vector<ProMTextField>();

for (int i=0; i<items.size(); i++)
{
	final JLabel lab = new JLabel(items.elementAt(i)); 
	lab.setForeground(UISettings.TextLight_COLOR);
	labs.add(lab);
	c.ipadx = 200; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = i+1;
	pane.add(labs.elementAt(i), c);
		
	final ProMTextField tf = new ProMTextField("1.0");
	c.gridx = 1;
	c.gridy = i+1;
	c.ipadx = 100;
	c.gridwidth = 1;
	textFields.add(tf);
	pane.add(textFields.elementAt(i), c);
}

//move on model costs

final Vector<String> itemsModel = new Vector<String>();

Collection <Transition> trans = net.getTransitions();
Iterator it = trans.iterator();
while(it.hasNext())
{
	DirectedGraphNode node = (DirectedGraphNode) it.next();
	itemsModel.add(node.toString());
}

final JLabel titleTextModel=new JLabel();
titleTextModel.setText("<html><h3>Specify risk costs for skipping of tasks in a model: </h3></html>"); 
titleTextModel.setForeground(UISettings.TextLight_COLOR);
c.ipadx = 300;      
c.gridwidth = 2;
c.gridx = 0;
c.gridy = items.size()+1;
pane.add(titleTextModel, c);

final Vector<JLabel> labs2 = new Vector<JLabel>();
final Vector<ProMTextField> textFields2 = new Vector<ProMTextField>();

for (int i=0; i<itemsModel.size(); i++)
{
final JLabel lab2 = new JLabel(itemsModel.elementAt(i));  
lab2.setForeground(UISettings.TextLight_COLOR);
labs2.add(lab2);
c.ipadx = 200; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = items.size()+1+i+1;
pane.add(labs2.elementAt(i), c);
	
final ProMTextField tf = new ProMTextField("1.0");
c.gridx = 1;
c.gridy = items.size()+1+i+1;
c.ipadx = 100;
c.gridwidth = 1;
textFields2.add(tf);
pane.add(textFields2.elementAt(i), c);
}

//vars not written costs---------------

final Vector<String> itemsVar = new Vector<String>();

Collection <Transition> trans2 = net.getTransitions();
Iterator it2 = trans2.iterator();
while(it2.hasNext())
{
	DirectedGraphNode node = (DirectedGraphNode) it2.next();
	PNWDTransition transition = (PNWDTransition) node;
		
	String task = transition.getLabel();
	Set<DataElement> wo = transition.getWriteOperations();
	Iterator it3 = wo.iterator();
	
	while(it3.hasNext())
	{
		DataElement curVar = (DataElement) it3.next();
		String varName = curVar.getVarName();
		itemsVar.add(task+"-"+varName);
		//varNotWrittenCosts.put(task+"-"+varName, 1.0);
	}
}
	
	
	final JLabel titleText3=new JLabel();
	titleText3.setText("<html><h3>Specify risk costs for non-writing of variables by tasks: [task]-[variable] </h3></html>"); // title of the form
	titleText3.setForeground(UISettings.TextLight_COLOR);
	c.ipadx = 300;      
	c.gridwidth = 2;
	c.gridx = 0;
	c.gridy = items.size()+itemsModel.size()+2;
	if(itemsVar.size()>0)
	{pane.add(titleText3, c);}
		
	final Vector<JLabel> labs3 = new Vector<JLabel>();
	final Vector<JTextField> textFields3 = new Vector<JTextField>();

	for (int i=0; i<itemsVar.size(); i++)
	{
		final JLabel lab = new JLabel(itemsVar.elementAt(i));  
		lab.setForeground(UISettings.TextLight_COLOR);
		labs3.add(lab);
		c.ipadx = 200; 
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = items.size()+itemsModel.size()+2+i+1;
		pane.add(labs3.elementAt(i), c);
			
		final JTextField tf = new JTextField("1.0");
		c.gridx = 1;
		c.gridy = items.size()+itemsModel.size()+2+i+1;
		c.ipadx = 100;
		c.gridwidth = 1;
		textFields3.add(tf);
		pane.add(textFields3.elementAt(i), c);
	}
		


//-------------------
	
SlickerButton but=new SlickerButton();
but.setText("Next");
c.gridx = 0;
c.gridy = items.size()+itemsModel.size()+itemsVar.size()+4;
c.gridwidth = 2;
pane.add(but, c);

//----------

int rows = items.size()+itemsModel.size()+itemsVar.size()+4;
pane.setPreferredSize(new Dimension(830, rows*30+40));//
ProMScrollPane scrollPane = new ProMScrollPane(pane);
cMain.ipadx = 860;
cMain.ipady = 590;
cMain.gridx = 0;
cMain.gridy = 0;
mainPane.add(scrollPane,cMain);


//----------
  
 	
   but.addActionListener(
           new ActionListener(){
               public void actionPerformed(
                       ActionEvent e) {
            	   
            	   for(int j=0; j<items.size(); j++)
            	   {ip.moveOnLogCosts.put(items.elementAt(j), Double.parseDouble(textFields.elementAt(j).getText()));}
            	   
            	   for(int j=0; j<itemsModel.size(); j++)
            	   {ip.moveOnModelCosts.put(itemsModel.elementAt(j), Double.parseDouble(textFields2.elementAt(j).getText()));}
             	   
            	   for(int j=0; j<itemsVar.size(); j++)
            	   {ip.varNotWrittenCosts.put(itemsVar.elementAt(j), Double.parseDouble(textFields3.elementAt(j).getText()));}
            	            	   
            	   dispose(); }
                           }
                       );
   
   setSize(900,660);
   setModal(true);
   setLocationRelativeTo(null);
   setVisible(true);
   setDefaultCloseOperation(DISPOSE_ON_CLOSE);

System.out.println(ip.moveOnLogCosts);
return ip;
}


	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public InputParameters defineTSParams(final InputParameters ip, final XLog log) throws Exception
   {
	
	final HeaderBar pane = new HeaderBar("");	
	setContentPane(pane);
		
	pane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.CENTER;
		
	final JLabel defVarsText=new JLabel();
	defVarsText.setForeground(UISettings.TextLight_COLOR);
	defVarsText.setText("<html><h3>Specify time series settings: </h3></html>");
	c.ipadx = 300;      
	c.gridwidth = 14;
	c.gridx = 0;
	c.gridy = 0;
	pane.add(defVarsText, c);
		
	final JLabel lab0 = new JLabel("Start time:");  
	lab0.setForeground(UISettings.TextLight_COLOR);
	c.ipadx = 30; 
	c.gridwidth = 3;
	c.gridx = 0;
	c.gridy = 1;
	pane.add(lab0, c);
	
	//getting log start and end time:
	String string_date = "12-December-2100";

	SimpleDateFormat f = new SimpleDateFormat("dd-MMM-yyyy");
	Date d = f.parse(string_date);
	long milliseconds = d.getTime();
	
	Timestamp starttime = new Timestamp(milliseconds);
	Timestamp endtime = new Timestamp(0);
	
	
	for (XTrace t : log) 
	{
		XEvent start = t.get(0);

		XAttributeTimestamp nexteventattr = (XAttributeTimestamp)start.getAttributes().get("time:timestamp");
		Date nexte = nexteventattr.getValue();
		long nexttime = nexte.getTime();
		Timestamp curstarttime = new Timestamp(nexttime);
		
		if(curstarttime.before(starttime)){starttime = curstarttime;}
	
		XEvent end = t.get(t.size()-1);		
		nexteventattr = (XAttributeTimestamp)end.getAttributes().get("time:timestamp");
		nexte = nexteventattr.getValue();
		nexttime = nexte.getTime();
		Timestamp curendtime = new Timestamp(nexttime);
	
		if(curendtime.after(endtime)){endtime = curendtime;}
		
	}
	

		long timestamp = starttime.getTime();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		Integer syear = cal.get(Calendar.YEAR);
		Integer smonth = cal.get(Calendar.MONTH)+1;
		Integer sday = cal.get(Calendar.DATE);
		Integer shour = cal.get(Calendar.HOUR_OF_DAY);
		Integer smin = cal.get(Calendar.MINUTE);
		Integer ssec = cal.get(Calendar.SECOND);
		
		String iyear = syear.toString(); 
		String imonth = smonth.toString(); 
		String iday = sday.toString(); 
		String ihour = shour.toString(); 
		String imin = smin.toString(); 
		String isec = ssec.toString(); 
		
		final Long log_duration = endtime.getTime() - starttime.getTime();
		
		double numWeeks = log_duration/1000/60/60/24/7;
		Long weeks = (long) numWeeks+1;
	
	//--------------------------
	
	final JTextField year=new JTextField(iyear,30);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 3;
	c.gridy = 1;
	pane.add(year, c);
	
	final JLabel lab1 = new JLabel("<html><h3>&nbsp;&nbsp;&nbsp;&nbsp;/</h3></html>");  
	lab1.setForeground(UISettings.TextLight_COLOR);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 4;
	c.gridy = 1;
	pane.add(lab1, c);
		
	final JTextField month=new JTextField(imonth,30);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 5;
	c.gridy = 1;
	pane.add(month, c);
	
	final JLabel lab2 = new JLabel("<html><h3>&nbsp;&nbsp;&nbsp;&nbsp;/</h3></html>");  
	lab2.setForeground(UISettings.TextLight_COLOR);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 6;
	c.gridy = 1;
	pane.add(lab2, c);
			
	final JTextField day=new JTextField(iday,50);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 7;
	c.gridy = 1;
	pane.add(day, c);
	
	final JLabel lab3 = new JLabel("<html><h3>&nbsp;&nbsp;.&nbsp;&nbsp;</h3></html>"); 
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 8;
	c.gridy = 1;
	pane.add(lab3, c);

	final JTextField hour=new JTextField(ihour,30);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 9;
	c.gridy = 1;
	pane.add(hour, c);
	
	final JLabel lab5 = new JLabel("<html><h3>&nbsp;&nbsp;&nbsp;&nbsp;:</h3></html>");
	lab5.setForeground(UISettings.TextLight_COLOR);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 10;
	c.gridy = 1;
	pane.add(lab5, c);
		
	final JTextField minute=new JTextField(imin,30);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 11;
	c.gridy = 1;
	pane.add(minute, c);
	
	final JLabel lab6 = new JLabel("<html><h3>&nbsp;&nbsp;&nbsp;&nbsp;:</h3></html>");  
	lab6.setForeground(UISettings.TextLight_COLOR);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 12;
	c.gridy = 1;
	pane.add(lab6, c);

	final JTextField second=new JTextField(isec,30);
	c.ipadx = 30; 
	c.gridwidth = 1;
	c.gridx = 13;
	c.gridy = 1;
	pane.add(second, c);
	
			
	final JLabel lab7 = new JLabel("Slot size:");  
	lab7.setForeground(UISettings.TextLight_COLOR);
	c.ipadx = 30; 
	c.gridwidth = 3;
	c.gridx = 0;
	c.gridy = 2;
	pane.add(lab7, c);
		
	final ProMTextField tslotnum = new ProMTextField("1");
	c.ipadx = 30;
	c.gridwidth = 5;
	c.gridx = 3;
	c.gridy = 2;
	pane.add(tslotnum, c);
			
		
	DefaultComboBoxModel model = new DefaultComboBoxModel();
	
	model.addElement("week");
	model.addElement("month");
	model.addElement("year");
	model.addElement("day");
	model.addElement("hour");
	model.addElement("minute");
	model.addElement("second");
	   
	final ProMComboBox tslotunit = new ProMComboBox(model);
	c.ipadx = 30;
	c.gridwidth = 6;
	c.gridx = 8;
	c.gridy = 2;
	pane.add(tslotunit, c);

	final JLabel lab8 = new JLabel("Number of time slots:"); 
	lab8.setForeground(UISettings.TextLight_COLOR);
	c.ipadx = 30; 
	c.gridwidth = 3;
	c.gridx = 0;
	c.gridy = 3;
	pane.add(lab8, c);
		
	final ProMTextField numslots = new ProMTextField(weeks.toString());
	c.ipadx = 30;
	c.gridwidth = 6;
	c.gridx = 5;
	c.gridy = 3;
	pane.add(numslots, c);
	
	tslotnum.getDocument().addDocumentListener(new DocumentListener()
    {
			   public void changedUpdate(DocumentEvent e) {
        	   long unit = 0;
        	   int slotsize = Integer.parseInt(tslotnum.getText());
        	   String slotunit = (String) tslotunit.getSelectedItem();
        	   if(slotunit.equals("year")){unit = (604800000l/7)*365;}//365 days 
        	   else if(slotunit.equals("month")){unit = (604800000l/7)*30;}//30 days 
        	   else if(slotunit.equals("week")){unit = 604800000l;}
        	   else if(slotunit.equals("day")){unit = 604800000l/7;}
        	   else if(slotunit.equals("hour")){unit = 604800000l/(7*24);}
        	   else if(slotunit.equals("minute")){unit = 604800000l/(7*24*60);}
        	   else if(slotunit.equals("second")){unit = 604800000l/(7*24*60*60);}
        	   
        	   double numWeeks = log_duration/(unit*slotsize);
        	   Long weeks = (long) numWeeks+1;

        	   numslots.setText(weeks.toString());
        	   pane.revalidate();
        	   pane.repaint();
       	
        }
			   public void insertUpdate(DocumentEvent e) {
	        	   long unit = 0;
            	   int slotsize = Integer.parseInt(tslotnum.getText());
            	   String slotunit = (String) tslotunit.getSelectedItem();
            	   if(slotunit.equals("year")){unit = (604800000l/7)*365;}//365 days 
            	   else if(slotunit.equals("month")){unit = (604800000l/7)*30;}//30 days 
            	   else if(slotunit.equals("week")){unit = 604800000l;}
            	   else if(slotunit.equals("day")){unit = 604800000l/7;}
            	   else if(slotunit.equals("hour")){unit = 604800000l/(7*24);}
            	   else if(slotunit.equals("minute")){unit = 604800000l/(7*24*60);}
            	   else if(slotunit.equals("second")){unit = 604800000l/(7*24*60*60);}
            	   
            	   double numWeeks = log_duration/(unit*slotsize);
            	   Long weeks = (long) numWeeks+1;

            	   numslots.setText(weeks.toString());
            	   pane.revalidate();
            	   pane.repaint();
           	
            }
        
			   public void removeUpdate(DocumentEvent e) {
				   
				   if(!tslotnum.getText().equals(""))
			   	   {long unit = 0;
            	   int slotsize = Integer.parseInt(tslotnum.getText());
            	   String slotunit = (String) tslotunit.getSelectedItem();
            	   if(slotunit.equals("year")){unit = (604800000l/7)*365;}//365 days 
            	   else if(slotunit.equals("month")){unit = (604800000l/7)*30;}//30 days 
            	   else if(slotunit.equals("week")){unit = 604800000l;}
            	   else if(slotunit.equals("day")){unit = 604800000l/7;}
            	   else if(slotunit.equals("hour")){unit = 604800000l/(7*24);}
            	   else if(slotunit.equals("minute")){unit = 604800000l/(7*24*60);}
            	   else if(slotunit.equals("second")){unit = 604800000l/(7*24*60*60);}
            	   
            	   double numWeeks = log_duration/(unit*slotsize);
            	   Long weeks = (long) numWeeks+1;

            	   numslots.setText(weeks.toString());
            	   pane.revalidate();
            	   pane.repaint();
			   	   }
					   
            }
     
    });

tslotunit.addActionListener(
    new ActionListener(){
        public void actionPerformed(
                ActionEvent e) {
        	
       	   long unit = 0;
    	   int slotsize = Integer.parseInt(tslotnum.getText());
    	   String slotunit = (String) tslotunit.getSelectedItem();
    	   if(slotunit.equals("year")){unit = (604800000l/7)*365;}//365 days 
    	   else if(slotunit.equals("month")){unit = (604800000l/7)*30;}//30 days 
    	   else if(slotunit.equals("week")){unit = 604800000l;}
    	   else if(slotunit.equals("day")){unit = 604800000l/7;}
    	   else if(slotunit.equals("hour")){unit = 604800000l/(7*24);}
    	   else if(slotunit.equals("minute")){unit = 604800000l/(7*24*60);}
    	   else if(slotunit.equals("second")){unit = 604800000l/(7*24*60*60);}
    	   
    	   double numWeeks = log_duration/(unit*slotsize);
    	   Long weeks = (long) numWeeks+1;

    	   numslots.setText(weeks.toString());
    	   pane.revalidate();
    	   pane.repaint();

        	
        }
        
    });

		
	SlickerButton but=new SlickerButton();
	c.ipadx = 600;
	but.setText("Next");
	c.gridwidth = 14;
	c.gridx = 0;
	c.gridy = 4;
	pane.add(but, c);
      
     	
       but.addActionListener(
               new ActionListener(){
                   public void actionPerformed(
                           ActionEvent e) {
                	   
                		DateTime dt = new DateTime(Integer.parseInt(year.getText()), Integer.parseInt(month.getText()),Integer.parseInt(day.getText()), Integer.parseInt(hour.getText()), Integer.parseInt(minute.getText()), Integer.parseInt(second.getText())); // 
                		ip.startTime = dt.getMillis();
                		ip.numberOfSlots = Integer.parseInt(numslots.getText());// 	
                		ip.decNum = 3;// 		
    	  	
                	   long unit = 0;
                	   int slotsize = Integer.parseInt(tslotnum.getText());
                	   String slotunit = (String) tslotunit.getSelectedItem();
                	   if(slotunit.equals("year")){unit = (604800000l/7)*365;}//365 days 
                	   else if(slotunit.equals("month")){unit = (604800000l/7)*30;}//30 days 
                	   else if(slotunit.equals("week")){unit = 604800000l;}
                	   else if(slotunit.equals("day")){unit = 604800000l/7;}
                	   else if(slotunit.equals("hour")){unit = 604800000l/(7*24);}
                	   else if(slotunit.equals("minute")){unit = 604800000l/(7*24*60);}
                	   else if(slotunit.equals("second")){unit = 604800000l/(7*24*60*60);}
                	   
                	   ip.slotSize = unit*slotsize;
                	   System.out.println(dt+"---"+ip.numberOfSlots+"---"+ ip.slotSize);
                	              dispose(); }
                                   }
                           );
       
       setSize(700,300);
       setModal(true);
       setLocationRelativeTo(null);
       setVisible(true);
       setDefaultCloseOperation(DISPOSE_ON_CLOSE);
       
       return ip;
   } 


@SuppressWarnings("rawtypes")
public Vector<GuardCosts> getGuardCosts (PetriNetWithData net, final Vector<GuardCosts> guardCosts)
{

Vector<String> tasks = new Vector<String>();
Vector<String> guards = new Vector<String>();
Vector<Integer> numAnds = new Vector<Integer>();

Collection <Transition> trans = net.getTransitions();
Iterator it = trans.iterator();
while(it.hasNext())
{
	DirectedGraphNode node = (DirectedGraphNode) it.next();
	PNWDTransition transition = (PNWDTransition) node;
	if(transition.getGuard()!=null)
		{
		String mainGuard = transition.getGuard().toString();
		//guardCosts.add(new GuardCosts(node.toString(),mainGuard,1.0));
		tasks.add(node.toString());
		guards.add(mainGuard);
		
		Pattern p = Pattern.compile("&&");
		Matcher m = p.matcher(mainGuard);
		int count = 0;
		while (m.find())
		{
		count +=1;
		}
		
		numAnds.add(count);
		}
	else
		{
		//guardCosts.add(new GuardCosts(node.toString(),"NO-GUARD",0.0));
		tasks.add(node.toString());
		guards.add("NO-GUARD");
		numAnds.add(0);
	
		}
}

//System.out.println(tasks);
//System.out.println(guards);
//System.out.println(numAnds);


for (int i=0; i<tasks.size(); i++)
{
final String curTask = tasks.elementAt(i);
final String curGuard = guards.elementAt(i);
final Integer curNumAnds = numAnds.elementAt(i);

if (curGuard.equals("NO-GUARD"))
{
	guardCosts.add(new GuardCosts(curTask,"NO-GUARD",0.0));
	
}else
{
	GetGuardInput ggi = new GetGuardInput();	
	guardCosts.add(ggi.getOneGuardCost(curTask, curGuard, curNumAnds));
	
}

}


return guardCosts;
}


@SuppressWarnings({ "rawtypes", "unchecked" })
public InputParameters defineRiskParams(final InputParameters ip) throws Exception
{

	final HeaderBar pane = new HeaderBar("");	
	setContentPane(pane);
		
	pane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	c.fill = GridBagConstraints.CENTER;
	
final JLabel defVarsText=new JLabel();
defVarsText.setForeground(UISettings.TextLight_COLOR);
defVarsText.setText("<html><h3>Process risk evaluation parameters</h3></html>");
c.ipadx = 200;      
c.gridwidth = 2;
c.gridx = 0;
c.gridy = 0;
pane.add(defVarsText, c);

//--------timeUnit--------------------------------------------------
final JLabel timeunitlab=new JLabel();
timeunitlab.setForeground(UISettings.TextLight_COLOR);
timeunitlab.setHorizontalAlignment(JLabel.CENTER);
timeunitlab.setText("<html><h3>Time unit: </h3></html>");
c.ipadx = 100;      
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 1;
pane.add(timeunitlab, c);	
		
	
	 DefaultComboBoxModel model = new DefaultComboBoxModel();
	 model.addElement("hours");
	 model.addElement("years");
     model.addElement("months");
     model.addElement("weeks");
     model.addElement("days");
     model.addElement("minutes");
     model.addElement("seconds");
   
     final ProMComboBox tslotunit = new ProMComboBox(model);
    c.gridwidth = 1;
	c.gridx = 1;
	c.gridy = 1;
	c.ipadx = 100;
	pane.add(tslotunit, c);

	//----------------TSCaseMethod---------------------------------------------------
	final JLabel tscasemethlab=new JLabel();
	tscasemethlab.setForeground(UISettings.TextLight_COLOR);
	tscasemethlab.setHorizontalAlignment(JLabel.CENTER);
	tscasemethlab.setText("<html><h3>Time slot cases: </h3></html>");
	c.ipadx = 100;      
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 2;
	pane.add(tscasemethlab, c);	
			
		
		 DefaultComboBoxModel model1 = new DefaultComboBoxModel();
	     model1.addElement("Started in the slot");
	     model1.addElement("Completed in the slot");
	     model1.addElement("Started before and completed after the slot");
	     model1.addElement("Active during the slot");
	     model1.addElement("Started before and completed after the end of slot");

	   
	    final ProMComboBox tscasemeth = new ProMComboBox(model1);
	    c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 2;
		c.ipadx = 100;
		pane.add(tscasemeth, c);

//------------------truncate---------------------------------------
		final JLabel truncatelab=new JLabel();
		truncatelab.setForeground(UISettings.TextLight_COLOR);
		truncatelab.setHorizontalAlignment(JLabel.CENTER);
		truncatelab.setText("<html><h3>Truncate cases at the end of time slot? </h3></html>");
		c.ipadx = 100;      
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 3;
		pane.add(truncatelab, c);	
				
			
			 DefaultComboBoxModel model2 = new DefaultComboBoxModel();
		     model2.addElement("Yes");
		     model2.addElement("No");
		    		   
		    final ProMComboBox truncate = new ProMComboBox(model2);
		    c.gridwidth = 1;
			c.gridx = 1;
			c.gridy = 3;
			c.ipadx = 100;
			pane.add(truncate, c);
//-------------------pre-process-----------
			
			final JLabel preprocesslab=new JLabel();
			preprocesslab.setForeground(UISettings.TextLight_COLOR);
			preprocesslab.setHorizontalAlignment(JLabel.CENTER);
			preprocesslab.setText("<html><h3>Pre-process log? </h3></html>");
			c.ipadx = 100;      
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 4;
			pane.add(preprocesslab, c);	
					
				
				 DefaultComboBoxModel model3 = new DefaultComboBoxModel();
			     model3.addElement("Yes");
			     model3.addElement("No");
			    		   
			    final ProMComboBox preprocess = new ProMComboBox(model3);
			    c.gridwidth = 1;
				c.gridx = 1;
				c.gridy = 4;
				c.ipadx = 100;
				pane.add(preprocess, c);

			
//-----------------------Risk aggregation method ------------------------
				final JLabel riskmethodlab=new JLabel();
				riskmethodlab.setForeground(UISettings.TextLight_COLOR);
				riskmethodlab.setHorizontalAlignment(JLabel.CENTER);
				riskmethodlab.setText("<html><h3>Risk aggregation method:</h3></html>");
				c.ipadx = 100;      
				c.gridwidth = 1;
				c.gridx = 0;
				c.gridy = 5;
				pane.add(riskmethodlab, c);	
						
					
					 DefaultComboBoxModel model4 = new DefaultComboBoxModel();
				     model4.addElement("total");
				     model4.addElement("mean");
				    		   
				    final ProMComboBox riskmethod = new ProMComboBox(model4);
				    c.gridwidth = 1;
					c.gridx = 1;
					c.gridy = 5;
					c.ipadx = 100;
					pane.add(riskmethod, c);
			
//--------------------Mean method - prev. version----------------------------------------------
/*					final JLabel meanmethodlab=new JLabel();
					meanmethodlab.setForeground(UISettings.TextLight_COLOR);
					meanmethodlab.setHorizontalAlignment(JLabel.CENTER);
					meanmethodlab.setText("<html><h3>Mean method:</h3></html>");
					c.ipadx = 100;      
					c.gridwidth = 1;
					c.gridx = 0;
					c.gridy = 6;
					pane.add(meanmethodlab, c);	
							
						
						 DefaultComboBoxModel model5 = new DefaultComboBoxModel();
					     model5.addElement("average");
					     model5.addElement("median");
					    		   
					    final ProMComboBox meanmethod = new ProMComboBox(model5);
					    c.gridwidth = 1;
						c.gridx = 1;
						c.gridy = 6;
						c.ipadx = 100;
						pane.add(meanmethod, c);				
*///-----------------------------------------------------------------------------
			
SlickerButton but=new SlickerButton();
but.setText("Next");
c.gridx = 0;
c.gridy = 6;
c.gridwidth = 2;
pane.add(but, c);
  
 	
   but.addActionListener(
           new ActionListener(){
               public void actionPerformed(
                       ActionEvent e) {
            	   
            	
            	   long unit = 0;
            	   String slotunit = (String) tslotunit.getSelectedItem();
            	   if(slotunit.equals("years")){unit = (604800000l/7)*365;}//365 days 
            	   else if(slotunit.equals("months")){unit = (604800000l/7)*30;}//30 days 
            	   else if(slotunit.equals("weeks")){unit = 604800000l;}
            	   else if(slotunit.equals("days")){unit = 604800000l/7;}
            	   else if(slotunit.equals("hours")){unit = 604800000l/(7*24);}
            	   else if(slotunit.equals("minutes")){unit = 604800000l/(7*24*60);}
            	   else if(slotunit.equals("seconds")){unit = 604800000l/(7*24*60*60);}
            	   ip.timeUnit = unit;
            	 
            	   String caseTSmethod = (String) tscasemeth.getSelectedItem();
               	
            	   if(caseTSmethod.equals("Started in the slot")){ip.tsCaseMethod = "start";}
            	   else if(caseTSmethod.equals("Completed in the slot")){ip.tsCaseMethod = "complete";}
            	   else if(caseTSmethod.equals("Started before and completed after the slot")){ip.tsCaseMethod = "activeInTS";}
            	   else if(caseTSmethod.equals("Active during the slot")){ip.tsCaseMethod = "active";}
            	   else if(caseTSmethod.equals("Started before and completed after the end of slot")){ip.tsCaseMethod = "activeEndTS";}
            	   
            	   
             	   String truncateitem = (String) truncate.getSelectedItem();
            	   if(truncateitem.equals("Yes")){ip.truncate = true;}
            	   else{ip.truncate = false;}
            	   
            	   String preprocessitem = (String) preprocess.getSelectedItem();
            	   if(preprocessitem.equals("Yes")){ip.preprocess = true;}
            	   else{ip.preprocess = false;}
            	 
            	   ip.riskMethod = (String) riskmethod.getSelectedItem();
            	   ip.meanMethod = "average";
            	  // ip.meanMethod = (String) meanmethod.getSelectedItem(); // prev. version
               	
            	   
            	   // System.out.println(dt+"---"+ip.numberOfSlots+"---"+ ip.slotSize);
            	              dispose(); }
                               }
                       );
   
   setSize(700,400);
   setModal(true);
   setLocationRelativeTo(null);
   setVisible(true);
   setDefaultCloseOperation(DISPOSE_ON_CLOSE);
   
   return ip;
} 


@SuppressWarnings({ "rawtypes", "unchecked" })
public InputParameters defineOutcomeParams(final InputParameters ip, XLog log) throws Exception
{

final HeaderBar pane = new HeaderBar("");
setContentPane(pane);
pane.setLayout(new GridBagLayout());
GridBagConstraints c = new GridBagConstraints();


final JLabel defVarsText=new JLabel();
defVarsText.setForeground(UISettings.TextLight_COLOR);
defVarsText.setText("<html><h3>Aggregate process outcomes parameters: </h3></html>");
c.ipadx = 200;      
c.gridwidth = 2;
c.gridx = 0;
c.gridy = 0;
pane.add(defVarsText, c);


	//----------------OutcomeMethod---------------------------------------------------
	final JLabel tsoutmethlab=new JLabel();
	tsoutmethlab.setForeground(UISettings.TextLight_COLOR);
	tsoutmethlab.setText("<html><h3>Aggregate process outcome method: </h3></html>");
	c.ipadx = 100;      
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 1;
	pane.add(tsoutmethlab, c);	
			
		
		 DefaultComboBoxModel model1 = new DefaultComboBoxModel();
	     model1.addElement("Total");
	     model1.addElement("Fraction");
	     model1.addElement("Mean");
	    	   
	    final ProMComboBox tsoutmeth = new ProMComboBox(model1);
	    c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 1;
		c.ipadx = 100;
		pane.add(tsoutmeth, c);

//---------------------------------------------------------
		
		List<XAttribute> eal = log.getGlobalEventAttributes(); 
		List<XAttribute> cal = log.getGlobalTraceAttributes();
		ConcurrentSkipListSet <String> attributes = new ConcurrentSkipListSet <String>();
		
		
		for (int i=0; i<eal.size();i++)
		{attributes.add(eal.get(i).getKey());}
		
		for (int i=0; i<cal.size();i++)
		{attributes.add(cal.get(i).getKey());}
		
		DefaultComboBoxModel model2 = new DefaultComboBoxModel();
		Iterator it = attributes.iterator();
		
		for(int i=0;i<attributes.size();i++)
		{
			
			String next = (String)it.next();
			model2.addElement(next);
		}

		final JLabel lab1 = new JLabel("Outcome attribute: ");  
		lab1.setForeground(UISettings.TextLight_COLOR);
		c.ipadx = 100; 
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 2;
		pane.add(lab1, c);
		
		final ProMComboBox outattr = new ProMComboBox(model2);
		c.gridx = 1;
		c.gridy = 2;
		c.ipadx = 100;
		pane.add(outattr, c);
		
//---------------------------------------------------------
		
		 DefaultComboBoxModel model3 = new DefaultComboBoxModel();
		 model3.addElement("case_attribute");
		 
		ConcurrentSkipListSet <String> tasks = new ConcurrentSkipListSet <String>();
		
		//get tasks from log
		for (XTrace t: log)
		{for (XEvent e : t) {
			String eventName = XConceptExtension.instance().extractName(e);
			tasks.add(eventName);
			}}
		
		//------------------
		Iterator it2 = tasks.iterator();
		for(int i=0;i<tasks.size();i++)
		{
			
			String next = (String)it2.next();
			model3.addElement(next);
		}
	
		 
				final JLabel lab2 = new JLabel("Task/case containing case outcome: ");  
				lab2.setForeground(UISettings.TextLight_COLOR);
				
				c.ipadx = 100; 
				c.gridwidth = 1;
				c.gridx = 0;
				c.gridy = 3;
				pane.add(lab2, c);
				
				final ProMComboBox outtask = new ProMComboBox(model3);
				c.gridx = 1;
				c.gridy = 3;
				c.ipadx = 100;
				pane.add(outtask, c);			

//-----------------------------------------------------------------------------
			
				final JLabel lab3 = new JLabel("Case outcome value: ");  
				lab3.setForeground(UISettings.TextLight_COLOR);
				
				c.ipadx = 100; 
				c.gridwidth = 1;
				c.gridx = 0;
				c.gridy = 4;
				pane.add(lab3, c);
				
				final ProMTextField outval = new ProMTextField("0.0");
				c.gridx = 1;
				c.gridy = 4;
				c.ipadx = 100;
				pane.add(outval, c);			

//-----------------------------------------------------------------------------

				
SlickerButton but=new SlickerButton();
but.setText("Next");
c.gridx = 0;
c.gridy = 5;
c.gridwidth = 2;
pane.add(but, c);
  
 	
   but.addActionListener(
           new ActionListener(){
               public void actionPerformed(
                       ActionEvent e) {
            	   
            	  ip.taskPrevCaseAttr = (String) outtask.getSelectedItem();
            	  ip.outcomeattribute = (String) outattr.getSelectedItem();
            	  ip.outcomevalue = Double.parseDouble(outval.getText());
            	  ip.outcomeAggregateMethod = (String) tsoutmeth.getSelectedItem();
            	 	
            	   
            	   // System.out.println(dt+"---"+ip.numberOfSlots+"---"+ ip.slotSize);
            	   dispose(); }
                               }
                       );
   
   setSize(700,400);
   setModal(true);
   setLocationRelativeTo(null);
   setVisible(true);
   setDefaultCloseOperation(DISPOSE_ON_CLOSE);
   
   return ip;
} 

	
	
}


//---------------------------------------PREV. VERSIONS---------------------------------------------------------
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/*	
//Getting costs from users ---------------- Template ------------------------------------------------

public Hashtable <String,Double> getCost(final Hashtable<String,Double> costs) throws Exception	
{ConcurrentSkipListSet<String> activities = new ConcurrentSkipListSet<String>();
	
activities.add("Task1             uuuuuu              uuuu");
activities.add("Task2");
activities.add("Task3");
activities.add("Task4");
activities.add("Task5");
activities.add("Task6");
activities.add("Task7");

final Vector<String> items = new Vector<String>();
items.addAll(activities);

//-------------------------------------

final Container pane = getContentPane();
pane.setLayout(new GridBagLayout());
GridBagConstraints c = new GridBagConstraints();
c.fill = GridBagConstraints.CENTER;

final JLabel titleText=new JLabel();
titleText.setText("<html><h3>Specify costs: </h3></html>"); // title of the form
c.ipadx = 300;      
c.gridwidth = 2;
c.gridx = 0;
c.gridy = 0;
pane.add(titleText, c);
	
final Vector<JLabel> labs = new Vector<JLabel>();
final Vector<JTextField> textFields = new Vector<JTextField>();

for (int i=0; i<items.size(); i++)
{
	final JLabel lab = new JLabel(items.elementAt(i));  
	labs.add(lab);
	c.ipadx = 200; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = i+1;
	//c.fill = GridBagConstraints.EAST;
	pane.add(labs.elementAt(i), c);
		
	final JTextField tf = new JTextField("1.0");
	c.gridx = 1;
	c.gridy = i+1;
	c.ipadx = 100;
	c.gridwidth = 1;
	textFields.add(tf);
	pane.add(textFields.elementAt(i), c);
}
	
JButton but=new JButton();
but.setText("Submit");
c.gridx = 0;
c.gridy = items.size()+1;
c.gridwidth = 2;
pane.add(but, c);

	
 but.addActionListener(
         new ActionListener(){
             public void actionPerformed(
                     ActionEvent e) {
          	   
          	   for(int j=0; j<items.size(); j++)
          	   {costs.put(items.elementAt(j), Double.parseDouble(textFields.elementAt(j).getText()));}
          	   dispose(); }
                             }
                     );
 
 setSize(800,400);
 setModal(true);
 setVisible(true);
 setDefaultCloseOperation(DISPOSE_ON_CLOSE);

System.out.println(costs);
return costs;
}
*/

/*

//getting move on log costs

public Hashtable <String,Double> getMoveOnLogCosts(XLog log, final Hashtable<String,Double> moveOnLogCosts)	
{ConcurrentSkipListSet<String> activities = new ConcurrentSkipListSet<String>();
	
for (XTrace t : log) {
for (XEvent e : t) {
		String activity = XConceptExtension.instance().extractName(e);
		if(activity != null)
		{activities.add(activity);}
}
}

//new

final Vector<String> items = new Vector<String>();
items.addAll(activities);

//-------------------------------------

final Container pane = getContentPane();
pane.setLayout(new GridBagLayout());
GridBagConstraints c = new GridBagConstraints();
c.fill = GridBagConstraints.CENTER;

final JLabel titleText=new JLabel();
titleText.setText("<html><h3>Specify risk costs for execution of tasks not aligned with a model: </h3></html>"); // title of the form
c.ipadx = 300;      
c.gridwidth = 2;
c.gridx = 0;
c.gridy = 0;
pane.add(titleText, c);
	
final Vector<JLabel> labs = new Vector<JLabel>();
final Vector<JTextField> textFields = new Vector<JTextField>();

for (int i=0; i<items.size(); i++)
{
	final JLabel lab = new JLabel(items.elementAt(i));  
	labs.add(lab);
	c.ipadx = 200; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = i+1;
	//c.fill = GridBagConstraints.EAST;
	pane.add(labs.elementAt(i), c);
		
	final JTextField tf = new JTextField("1.0");
	c.gridx = 1;
	c.gridy = i+1;
	c.ipadx = 100;
	c.gridwidth = 1;
	textFields.add(tf);
	pane.add(textFields.elementAt(i), c);
}
	
JButton but=new JButton();
but.setText("Submit");
c.gridx = 0;
c.gridy = items.size()+1;
c.gridwidth = 2;
pane.add(but, c);

	
 but.addActionListener(
         new ActionListener(){
             public void actionPerformed(
                     ActionEvent e) {
          	   
          	   for(int j=0; j<items.size(); j++)
          	   {moveOnLogCosts.put(items.elementAt(j), Double.parseDouble(textFields.elementAt(j).getText()));}
          	   dispose(); }
                             }
                     );
 
 setSize(800,400);
 setModal(true);
 setVisible(true);
 setDefaultCloseOperation(DISPOSE_ON_CLOSE);

System.out.println(moveOnLogCosts);
return moveOnLogCosts;
}
*/

/*
//getting move on model costs
public Hashtable <String,Double> getMoveOnModelCosts(PetriNetWithData net, final Hashtable<String,Double> moveOnModelCosts)	
{
	final Vector<String> itemsModel = new Vector<String>();
	
	Collection <Transition> trans = net.getTransitions();
	Iterator it = trans.iterator();
	while(it.hasNext())
	{
		DirectedGraphNode node = (DirectedGraphNode) it.next();
		itemsModel.add(node.toString());
	}
	

final Container pane = getContentPane();
pane.setLayout(new GridBagLayout());
GridBagConstraints c = new GridBagConstraints();
c.fill = GridBagConstraints.CENTER;

final JLabel titleText=new JLabel();
titleText.setText("<html><h3>Specify risk costs for skipping of tasks in a model: </h3></html>"); // title of the form
c.ipadx = 300;      
c.gridwidth = 2;
c.gridx = 0;
c.gridy = 0;
pane.add(titleText, c);
	
final Vector<JLabel> labs = new Vector<JLabel>();
final Vector<JTextField> textFields = new Vector<JTextField>();

for (int i=0; i<itemsModel.size(); i++)
{
	final JLabel lab = new JLabel(itemsModel.elementAt(i));  
	labs.add(lab);
	c.ipadx = 200; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = i+1;
	//c.fill = GridBagConstraints.EAST;
	pane.add(labs.elementAt(i), c);
		
	final JTextField tf = new JTextField("1.0");
	c.gridx = 1;
	c.gridy = i+1;
	c.ipadx = 100;
	c.gridwidth = 1;
	textFields.add(tf);
	pane.add(textFields.elementAt(i), c);
}
	
JButton but=new JButton();
but.setText("Submit");
c.gridx = 0;
c.gridy = itemsModel.size()+1;
c.gridwidth = 2;
pane.add(but, c);

	
 but.addActionListener(
         new ActionListener(){
             public void actionPerformed(
                     ActionEvent e) {
          	   
          	   for(int j=0; j<itemsModel.size(); j++)
          	   {moveOnModelCosts.put(itemsModel.elementAt(j), Double.parseDouble(textFields.elementAt(j).getText()));}
          	   dispose(); }
                             }
                     );
 
 setSize(800,400);
 setModal(true);
 setVisible(true);
 setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	
	
	System.out.println(moveOnModelCosts);

return moveOnModelCosts;
}

*/

/*
//getting variable not written costs
public Hashtable <String,Double> getVarNotWrittenCosts(PetriNetWithData net, final Hashtable<String,Double> varNotWrittenCosts)	
{
	
	final Vector<String> itemsVar = new Vector<String>();
	
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
			itemsVar.add(task+"-"+varName);
			//varNotWrittenCosts.put(task+"-"+varName, 1.0);
		}
	}
		
		
		final Container pane = getContentPane();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.CENTER;

		final JLabel titleText=new JLabel();
		titleText.setText("<html><h3>Specify risk costs for non-writing of variables by tasks: [task]-[variable] </h3></html>"); // title of the form
		c.ipadx = 300;      
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(titleText, c);
			
		final Vector<JLabel> labs = new Vector<JLabel>();
		final Vector<JTextField> textFields = new Vector<JTextField>();

		for (int i=0; i<itemsVar.size(); i++)
		{
			final JLabel lab = new JLabel(itemsVar.elementAt(i));  
			labs.add(lab);
			c.ipadx = 200; 
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = i+1;
			//c.fill = GridBagConstraints.EAST;
			pane.add(labs.elementAt(i), c);
				
			final JTextField tf = new JTextField("1.0");
			c.gridx = 1;
			c.gridy = i+1;
			c.ipadx = 100;
			c.gridwidth = 1;
			textFields.add(tf);
			pane.add(textFields.elementAt(i), c);
		}
			
		JButton but=new JButton();
		but.setText("Submit");
		c.gridx = 0;
		c.gridy = itemsVar.size()+1;
		c.gridwidth = 2;
		pane.add(but, c);
		  
		 	
		   but.addActionListener(
		           new ActionListener(){
		               public void actionPerformed(
		                       ActionEvent e) {
		            	   
		            	   for(int j=0; j<itemsVar.size(); j++)
		            	   {varNotWrittenCosts.put(itemsVar.elementAt(j), Double.parseDouble(textFields.elementAt(j).getText()));}
		            	   dispose(); }
		                               }
		                       );
		   
		   setSize(800,400);
		   setModal(true);
		   setVisible(true);
		   setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
		
	

	System.out.println(varNotWrittenCosts);

return varNotWrittenCosts;
}
*/

/*

///

public InputParameters defineTSAnalysisParams(final InputParameters ip) throws Exception
{

final Container pane = getContentPane();
pane.setLayout(new GridBagLayout());
GridBagConstraints c = new GridBagConstraints();
c.fill = GridBagConstraints.CENTER;

final JLabel defVarsText=new JLabel();
defVarsText.setText("<html><h3>Specify time series analysis parameters: </h3></html>");
c.ipadx = 300;      
c.gridwidth = 2;
c.gridx = 0;
c.gridy = 0;
pane.add(defVarsText, c);
	
final JLabel lab1 = new JLabel("Get change points?");  
c.ipadx = 100; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 1;
pane.add(lab1, c);

DefaultComboBoxModel model1 = new DefaultComboBoxModel();
model1.addElement("yes");
model1.addElement("no");
final JComboBox cp = new JComboBox(model1);
c.gridwidth = 1;
c.gridx = 1;
c.gridy = 1;
c.ipadx = 100;
pane.add(cp, c);

	
final JLabel lab2 = new JLabel("Get outliers?");  
c.ipadx = 100; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 2;
pane.add(lab2, c);

DefaultComboBoxModel model2 = new DefaultComboBoxModel();
model2.addElement("yes");
model2.addElement("no");
final JComboBox out = new JComboBox(model2);
c.gridwidth = 1;
c.gridx = 1;
c.gridy = 2;
c.ipadx = 100;
pane.add(out, c);


final JLabel lab3 = new JLabel("Estimate trend?");  
c.ipadx = 100; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 3;
pane.add(lab3, c);

DefaultComboBoxModel model3 = new DefaultComboBoxModel();
model3.addElement("yes");
model3.addElement("no");
final JComboBox trend = new JComboBox(model3);
c.gridwidth = 1;
c.gridx = 1;
c.gridy = 3;
c.ipadx = 100;
pane.add(trend, c);

final JLabel lab4 = new JLabel("Change Point method:");  
c.ipadx = 100; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 4;
pane.add(lab4, c);

DefaultComboBoxModel model4 = new DefaultComboBoxModel();
model4.addElement("Mann-Whitney");
model4.addElement("Mood");
model4.addElement("Lepage");
model4.addElement("Kolmogorov-Smirnov");
model4.addElement("Cramer-von-Mises");
final JComboBox cpmtype = new JComboBox(model4);
c.gridwidth = 1;
c.gridx = 1;
c.gridy = 4;
c.ipadx = 100;
pane.add(cpmtype, c);


final JLabel lab5 = new JLabel("ARL0:");  
c.ipadx = 100; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 5;
pane.add(lab5, c);

DefaultComboBoxModel model5 = new DefaultComboBoxModel();
model5.addElement("1000");
model5.addElement("2000");
model5.addElement("5000");
model5.addElement("10000");
model5.addElement("100");
final JComboBox arl0 = new JComboBox(model5);
c.gridwidth = 1;
c.gridx = 1;
c.gridy = 5;
c.ipadx = 100;
pane.add(arl0, c);


final JLabel lab6 = new JLabel("Startup:");  
c.ipadx = 100; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 6;
pane.add(lab6, c);

DefaultComboBoxModel model6 = new DefaultComboBoxModel();
model6.addElement("20");
model6.addElement("30");
model6.addElement("50");
model6.addElement("100");
model6.addElement("1000");
final JComboBox startup = new JComboBox(model6);
c.gridwidth = 1;
c.gridx = 1;
c.gridy = 6;
c.ipadx = 100;
pane.add(startup, c);

final JLabel lab7 = new JLabel("Outlier identification method:");  
c.ipadx = 100; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 7;
pane.add(lab7, c);

DefaultComboBoxModel model7 = new DefaultComboBoxModel();
model7.addElement("I");
model7.addElement("II");
final JComboBox method = new JComboBox(model7);
c.gridwidth = 1;
c.gridx = 1;
c.gridy = 7;
c.ipadx = 100;
pane.add(method, c);

final JLabel lab8 = new JLabel("Trend estimation period:");  
c.ipadx = 100; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 8;
pane.add(lab8, c);

DefaultComboBoxModel model8 = new DefaultComboBoxModel();
model8.addElement("recent");
model8.addElement("full");
final JComboBox period = new JComboBox(model8);
c.gridwidth = 1;
c.gridx = 1;
c.gridy = 8;
c.ipadx = 100;
pane.add(period, c);

	

JButton but=new JButton();
but.setText("Submit");
c.gridx = 0;
c.gridy = 9;
c.gridwidth = 2;
pane.add(but, c);

	
 but.addActionListener(
         new ActionListener(){
             public void actionPerformed(
                     ActionEvent e) {
          	   
         		ip.cpmType = (String) cpmtype.getSelectedItem();
      		ip.ARL0 = (String) arl0.getSelectedItem();
      		ip.startup = (String) startup.getSelectedItem();
      		ip.method = (String) method.getSelectedItem();
      		ip.period = (String) period.getSelectedItem();

             	String cpa = (String) cp.getSelectedItem();
          	if(cpa.equals("yes")){ip.cp = 1;}else{ip.cp = 0;}
          	   
           	String outa = (String) out.getSelectedItem();
          	if(outa.equals("yes")){ip.out = 1;}else{ip.out = 0;}
          
           	String trenda = (String) trend.getSelectedItem();
          	if(trenda.equals("yes")){ip.trend = 1;}else{ip.trend = 0;}
          
           	dispose(); }
                             }
                     );
 
 setSize(400,400);
 setModal(true);
 setVisible(true);
 setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 return ip;
} 

*/


/*

public InputParameters defineTSParamsOld(final InputParameters ip) throws Exception
{

final Container pane = getContentPane();
pane.setLayout(new GridBagLayout());
GridBagConstraints c = new GridBagConstraints();
c.fill = GridBagConstraints.CENTER;

final JLabel defVarsText=new JLabel();
defVarsText.setText("<html><h3>Specify time series parameters: </h3></html>");
c.ipadx = 300;      
c.gridwidth = 2;
c.gridx = 0;
c.gridy = 0;
pane.add(defVarsText, c);
	
final JLabel lab0 = new JLabel("Specify start date:");  
c.ipadx = 150; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 1;
c.gridwidth = 2;
pane.add(lab0, c);
	
	
final JLabel lab1 = new JLabel("Year:");  
c.ipadx = 100; 
c.gridwidth = 1;
c.gridx = 0;
c.gridy = 2;
pane.add(lab1, c);
	
final JTextField year = new JTextField("1970");
c.gridx = 1;
c.gridy = 2;
c.ipadx = 100;
pane.add(year, c);
	
	final JLabel lab2 = new JLabel("Month:");  
	c.ipadx = 100; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 3;
	pane.add(lab2, c);
	
	final JTextField month = new JTextField("1");
	c.gridx = 1;
	c.gridy = 3;
	c.ipadx = 100;
	pane.add(month, c);

	final JLabel lab3 = new JLabel("Day:");  
	c.ipadx = 100; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 4;
	pane.add(lab3, c);
	
	final JTextField day = new JTextField("1");
	c.gridx = 1;
	c.gridy = 4;
	c.ipadx = 100;
	pane.add(day, c);

	final JLabel lab4 = new JLabel("Hour:");  
	c.ipadx = 100; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 5;
	pane.add(lab4, c);
	
	final JTextField hour = new JTextField("0");
	c.gridx = 1;
	c.gridy = 5;
	c.ipadx = 100;
	pane.add(hour, c);

	final JLabel lab5 = new JLabel("Minute:");  
	c.ipadx = 100; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 6;
	pane.add(lab5, c);
	
	final JTextField minute = new JTextField("0");
	c.gridx = 1;
	c.gridy = 6;
	c.ipadx = 100;
	pane.add(minute, c);

	final JLabel lab6 = new JLabel("Second:");  
	c.ipadx = 100; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 7;
	pane.add(lab6, c);
	
	final JTextField second = new JTextField("0");
	c.gridx = 1;
	c.gridy = 7;
	c.ipadx = 100;
	pane.add(second, c);

	final JLabel lab7 = new JLabel("Time series slot size:");  
	c.ipadx = 100; 
	c.gridwidth = 2;
	c.gridx = 0;
	c.gridy = 8;
	pane.add(lab7, c);
	
	final JTextField tslotnum = new JTextField();
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 9;
	c.ipadx = 100;
	pane.add(tslotnum, c);
	
	
		
	
	 DefaultComboBoxModel model = new DefaultComboBoxModel();
   model.addElement("years");
   model.addElement("months");
   model.addElement("weeks");
   model.addElement("days");
   model.addElement("hours");
   model.addElement("minutes");
   model.addElement("seconds");
 
   final JComboBox tslotunit = new JComboBox(model);
  c.gridwidth = 1;
	c.gridx = 1;
	c.gridy = 9;
	c.ipadx = 100;
	pane.add(tslotunit, c);
	

	
	final JLabel lab8 = new JLabel("Number of slots:");  
	c.ipadx = 100; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 10;
	pane.add(lab8, c);
	
	final JTextField numslots = new JTextField();
	c.gridx = 1;
	c.gridy = 10;
	c.ipadx = 100;
	pane.add(numslots, c);
	
	final JLabel lab9 = new JLabel("Number of decimals after point:");  
	c.ipadx = 100; 
	c.gridwidth = 1;
	c.gridx = 0;
	c.gridy = 11;
	pane.add(lab9, c);
	
	final JTextField decimals = new JTextField();
	c.gridx = 1;
	c.gridy = 11;
	c.ipadx = 100;
	pane.add(decimals, c);

JButton but=new JButton();
but.setText("Submit");
c.gridx = 0;
c.gridy = 11;
c.gridwidth = 2;
pane.add(but, c);

	
 but.addActionListener(
         new ActionListener(){
             public void actionPerformed(
                     ActionEvent e) {
          	   
          		DateTime dt = new DateTime(Integer.parseInt(year.getText()), Integer.parseInt(month.getText()),Integer.parseInt(day.getText()), Integer.parseInt(hour.getText()), Integer.parseInt(minute.getText()), Integer.parseInt(second.getText())); // 
          		ip.startTime = dt.getMillis();
          		ip.numberOfSlots = Integer.parseInt(numslots.getText());// 	
          		//ip.decNum = Integer.parseInt(decimals.getText());// 		
	  	
          	   long unit = 0;
          	   int slotsize = Integer.parseInt(tslotnum.getText());
          	   String slotunit = (String) tslotunit.getSelectedItem();
          	   if(slotunit.equals("years")){unit = (604800000l/7)*365;}//365 days 
          	   else if(slotunit.equals("months")){unit = (604800000l/7)*30;}//30 days 
          	   else if(slotunit.equals("weeks")){unit = 604800000l;}
          	   else if(slotunit.equals("days")){unit = 604800000l/7;}
          	   else if(slotunit.equals("hours")){unit = 604800000l/(7*24);}
          	   else if(slotunit.equals("minutes")){unit = 604800000l/(7*24*60);}
          	   else if(slotunit.equals("seconds")){unit = 604800000l/(7*24*60*60);}
          	   
          	   ip.slotSize = unit*slotsize;
          	   System.out.println(dt+"---"+ip.numberOfSlots+"---"+ ip.slotSize);
          	              dispose(); }
                             }
                     );
 
 setSize(400,400);
 setModal(true);
 setVisible(true);
 setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 return ip;
} 
*/


