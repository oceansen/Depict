package no.simula.depict.wizards.jung;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JPopupMenu;

import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;

public class PopupMouseMenu extends AbstractPopupGraphMousePlugin 
{
    private JPopupMenu edgePopup, vertexPopup;

    /** Creates a new instance of PopupVertexEdgeMenuMousePlugin */
    public PopupMouseMenu() {
        this(MouseEvent.BUTTON3_MASK);
    }
    
    /**
     * Creates a new instance of PopupVertexEdgeMenuMousePlugin
     * @param modifiers mouse event modifiers see the jung visualization Event class.
     */
    public PopupMouseMenu(int modifiers) {
        super(modifiers);
    }
    
    /**
     * Implementation of the AbstractPopupGraphMousePlugin method. This is where the 
     * work gets done. You shouldn't have to modify unless you really want to...
     * @param e 
     */
    protected void handlePopup(MouseEvent e) 
    {
    	@SuppressWarnings("unchecked")
		final VisualizationViewer<DepictVertex, DepictEdge> vv = (VisualizationViewer<DepictVertex, DepictEdge>) e.getSource();
        Point2D p = e.getPoint();
        
        GraphElementAccessor<DepictVertex, DepictEdge> pickSupport = vv.getPickSupport();
        
        if (pickSupport != null) 
        {
            final DepictVertex v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
            
            if (v != null) 
            {
                updateVertexMenu(v, vv, p);
                vertexPopup.show(vv, e.getX(), e.getY());
            } 
            else
            {
            	final DepictEdge edge = pickSupport.getEdge(vv.getGraphLayout(), p.getX(), p.getY());
            	if (edge != null) 
            	{
                    updateEdgeMenu(edge, vv, p);
                    edgePopup.show(vv, e.getX(), e.getY());
                }
            }
        }
    }
    
    private void updateVertexMenu(DepictVertex v, VisualizationViewer<DepictVertex, DepictEdge> vv, Point2D point) 
    {
    	if (vertexPopup == null) 
    		return;
    	
        Component[] menuComps = vertexPopup.getComponents();
        
        for (Component comp: menuComps) 
        {
            if (comp instanceof VertexMenuListener) 
                ((VertexMenuListener)comp).setVertexAndView(v, vv);
            
            if (comp instanceof MenuPointListener) 
                ((MenuPointListener)comp).setPoint(point);
        }
    }
    
    public JPopupMenu getEdgePopup() {
        return edgePopup;
    }
    
    public void setEdgePopup(JPopupMenu edgePopup) {
        this.edgePopup = edgePopup;
    }
    
    public JPopupMenu getVertexPopup() {
        return vertexPopup;
    }
    
    public void setVertexPopup(JPopupMenu vertexPopup) {
        this.vertexPopup = vertexPopup;
    }
    
    private void updateEdgeMenu(DepictEdge edge, VisualizationViewer<DepictVertex, DepictEdge> vv, Point2D point) 
    {
    	if (edgePopup == null) 
    		return;
    	
        Component[] menuComps = edgePopup.getComponents();
        
        for (Component comp: menuComps) 
        {
            if (comp instanceof EdgeMenuListener) 
                ((EdgeMenuListener)comp).setEdgeAndView(edge, vv);
            
            if (comp instanceof MenuPointListener) 
                ((MenuPointListener)comp).setPoint(point);
        }
    }
}