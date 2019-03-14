package no.simula.depict.wizards;

import java.awt.geom.Point2D;
import java.util.UUID;

import org.apache.commons.collections15.Factory;

import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public class DepictEdgeFactory implements Factory<DepictEdge>
{
	private VisualizationViewer<DepictVertex, DepictEdge> vv;

	public DepictEdgeFactory(VisualizationViewer<DepictVertex, DepictEdge> vv)
	{
		this.vv = vv;
	}
	
	public DepictEdge create() 
	{
		GraphElementAccessor<DepictVertex, DepictEdge> pickSupport = vv.getPickSupport();
		Point2D p = vv.getMousePosition();
		DepictVertex endVertex = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
		
        DepictEdge e = new DepictEdge();
        UUID id = UUID.randomUUID();
        e.setId(id.toString());
		e.setSurrogate(true);
		DepictVertex startVertex = DepictEdgeFactoryCargo.getInstance().getStartVertex(); 
		e.setPrimaryTableName(startVertex.getName());
		e.setForeignTableName(endVertex.getName());
		e.setSchemaName(startVertex.getSchemaName());
		return e;
	}

	public static class DepictEdgeFactoryCargo
	{
		private static DepictEdgeFactoryCargo me;
    	private DepictVertex startVertex;

		private DepictEdgeFactoryCargo() {}
		public static DepictEdgeFactoryCargo getInstance()
		{
			if (me != null)
				return me;
			
			me = new DepictEdgeFactoryCargo();
			return me;
		}

		public DepictVertex getStartVertex() {
			return startVertex;
		}

		public void setStartVertex(DepictVertex startVertex) {
			this.startVertex = startVertex;
		}
	}
}
