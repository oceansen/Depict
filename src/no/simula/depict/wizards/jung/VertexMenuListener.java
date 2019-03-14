package no.simula.depict.wizards.jung;

import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public interface VertexMenuListener 
{
	void setVertexAndView(DepictVertex v, VisualizationViewer<DepictVertex, DepictEdge> vv);    
}
