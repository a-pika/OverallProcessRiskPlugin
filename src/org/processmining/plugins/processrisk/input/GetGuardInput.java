package org.processmining.plugins.processrisk.input;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JLabel;

import org.processmining.framework.util.ui.widgets.ProMTextField;
import org.processmining.plugins.processrisk.analyse.GuardCosts;
import org.processmining.plugins.processrisk.output.UISettings;

import com.fluxicon.slickerbox.components.HeaderBar;
import com.fluxicon.slickerbox.components.SlickerButton;

@SuppressWarnings("serial")
public class GetGuardInput extends JDialog{


GuardCosts getOneGuardCost(final String task, final String guard, final Integer numAnds)
{
	final GuardCosts gc = new GuardCosts();
	
	final HeaderBar pane = new HeaderBar("");
	setContentPane(pane);
	pane.setLayout(new GridBagLayout());
	GridBagConstraints c = new GridBagConstraints();
	

	if(numAnds < 1)
	{
	final JLabel titleText=new JLabel();
	titleText.setForeground(UISettings.TextLight_COLOR);
	titleText.setText("<html><h3>Specify risk cost for the guard evaluating to false: </h3></html>"); // title of the form //or costs for separate constraints
	c.ipadx = 300;      
	c.gridwidth = 2;
	c.gridx = 0;
	c.gridy = 0;
	pane.add(titleText, c);
		
		final JLabel lab = new JLabel(task + ": "+ guard); 
		lab.setForeground(UISettings.TextLight_COLOR);
		c.ipadx = 200; 
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(lab, c);
			
		final ProMTextField tf = new ProMTextField("1.0");
		c.gridx = 1;
		c.gridy = 1;
		c.ipadx = 100;
		c.gridwidth = 1;
		pane.add(tf, c);
	
		
	SlickerButton but=new SlickerButton();
	but.setText("Next");
	c.gridx = 0;
	c.gridy = 2;
	c.gridwidth = 2;
	pane.add(but, c);
	  
	 	
	   but.addActionListener(
	           new ActionListener(){
	               public void actionPerformed(
	                       ActionEvent e) {
	            	   
	            	 Double curCost = Double.parseDouble(tf.getText());
	            	 //gc = new GuardCosts(task,guard,curCost);
	            	 gc.task = task;
	            	 gc.mainGuard = guard;
	            	 gc.mainGuardCost = curCost;
	            	
	            	dispose();
		               
	               }
	                }
	                 );
	   
	   
	   setSize(900,400);
	   setModal(true);
	   setLocationRelativeTo(null);
	   setVisible(true);
	   setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
	}else
	{
		final JLabel titleText=new JLabel();
		titleText.setForeground(UISettings.TextLight_COLOR);
		titleText.setText("<html><h3>Specify risk cost for the guard evaluating to false or specify separate constraints and their costs of violation: </h3></html>"); // title of the form //or costs for separate constraints
		c.ipadx = 300;      
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		pane.add(titleText, c);
			
			final JLabel lab = new JLabel(task + ": "+ guard);  
			lab.setForeground(UISettings.TextLight_COLOR);
			c.ipadx = 200; 
			c.gridwidth = 1;
			c.gridx = 0;
			c.gridy = 1;
			pane.add(lab, c);
				
			final ProMTextField tf = new ProMTextField("1.0");
			c.gridx = 1;
			c.gridy = 1;
			c.ipadx = 100;
			c.gridwidth = 1;
			pane.add(tf, c);
		
		final Vector <ProMTextField> constraints = new Vector<ProMTextField>();
		final Vector <ProMTextField> constraintCosts = new Vector<ProMTextField>();
		
		for (int i=0; i<(numAnds+1);i++)
		{
			final ProMTextField constr = new ProMTextField("Constraint");
			c.gridx = 0;
			c.gridy = i+2;
			c.ipadx = 200;
			c.gridwidth = 1;
			constraints.add(constr);
			pane.add(constraints.elementAt(i), c);
			
			final ProMTextField constrCost = new ProMTextField("0.0");
			c.gridx = 1;
			c.gridy = i+2;
			c.ipadx = 100;
			c.gridwidth = 1;
			constraintCosts.add(constrCost);
			pane.add(constraintCosts.elementAt(i), c);
	
		}
			
		SlickerButton but=new SlickerButton();
		but.setText("Next");
		c.gridx = 0;
		c.gridy = numAnds+3;
		c.gridwidth = 2;
		pane.add(but, c);
		  
		 	
		   but.addActionListener(
		           new ActionListener(){
		               public void actionPerformed(
		                       ActionEvent e) {
		            	   
		            	 Double curCost = Double.parseDouble(tf.getText());
		            	 gc.task = task;
		            	 gc.mainGuard = guard;
		            	 gc.mainGuardCost = curCost;
		            	 
		            	 for (int i=0; i<(numAnds+1);i++)
		            	 {
		            		 String constr = constraints.elementAt(i).getText();
		            		 Double constrCost = Double.parseDouble(constraintCosts.elementAt(i).getText());
		            		if(!constr.equals("Constraint"))
		            		 {gc.guards.add(constr);
		            		 gc.costs.add(constrCost);}
		            	 }
		            	
		            	dispose();
			               
		               }
		                }
		                 );
		   
			   
		   setSize(900,400);
		   setModal(true);
		   setLocationRelativeTo(null);
		   setVisible(true);
		   setDefaultCloseOperation(DISPOSE_ON_CLOSE);	
	}	
	return gc; 
}

	
}




