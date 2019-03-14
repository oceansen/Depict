package no.simula.depict.wizards.jung;

import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public interface EdgeMenuListener 
{
	void setEdgeAndView(DepictEdge e, VisualizationViewer<DepictVertex, DepictEdge> visView);
}
