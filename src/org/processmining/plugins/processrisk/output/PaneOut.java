package org.processmining.plugins.processrisk.output;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;

import javax.swing.JLabel;

import com.fluxicon.slickerbox.components.HeaderBar;

public class PaneOut{
	
 
	public HeaderBar displayOut() throws Exception
   {

//template for pane output	
//creating structure of the main output page
	
	final HeaderBar pane = new HeaderBar("");
	pane.setLayout(new GridBagLayout());
	final GridBagConstraints c = new GridBagConstraints();
		
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	double w = screenSize.getWidth();
	double h = screenSize.getHeight();
	final int width = (int) (0.93*w);
	final int height = (int) (0.80*h);
	
	System.out.println(w + "---" + width);
	System.out.println(h + "---" + height);
	
//creating labels
	
	JLabel importlab = new JLabel("<html><h1>Test</h1></html>");
	importlab.setForeground(UISettings.TextLight_COLOR);
	importlab.setHorizontalAlignment(JLabel.CENTER);
	
// add components on panels

c.gridwidth = 1;
c.gridheight = 1;
c.ipadx = width;
c.ipady = height;
c.gridx = 0;
c.gridy = 0;
pane.add(importlab,c);	
  
return pane;
} 

}




