package org.processmining.plugins.processrisk.main;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;

import com.fluxicon.slickerbox.components.HeaderBar;


public class ProcessRiskEvaluationVisualiser {
	@Plugin(
			name = "Process Risk Evaluation", 
			parameterLabels = { "Event Log", "Process Model" }, 
			returnLabels = {"Overall Process Risk"},
			returnTypes = {HeaderBar.class}, 
			userAccessible = true,
			help = ""
			)
	@Visualizer
	public HeaderBar OPRVisualizer(PluginContext context, HeaderBar result) {
	return result;
	}
	
	
}

