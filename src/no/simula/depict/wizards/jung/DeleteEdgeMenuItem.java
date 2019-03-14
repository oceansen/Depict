package no.simula.depict.wizards.jung;

import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;

public class DeleteEdgeMenuItem extends JMenuItem implements EdgeMenuListener 
{
	private static final long serialVersionUID = 1L;
	private DepictEdge edge;
    private VisualizationViewer<DepictVertex, DepictEdge> vv;
    
    public DeleteEdgeMenuItem() 
    {
        super("Delete Edge");
        this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) 
            {
                int dialogButton = JOptionPane.YES_NO_OPTION;
                if (JOptionPane.showConfirmDialog (null, "You are going to remove the selected edge.\nAre you sure?", "Delete edge", dialogButton) == JOptionPane.YES_OPTION)
    			{
	                vv.getPickedEdgeState().pick(edge, false);
	                vv.getGraphLayout().getGraph().removeEdge(edge);
	                vv.repaint();
    			}
            }
        });
    }

    public void setEdgeAndView(DepictEdge edge, VisualizationViewer<DepictVertex, DepictEdge> vv) 
    {
        this.edge = edge;
        this.vv = vv;

        this.setEnabled(edge.isSurrogate() ? true : false);
        this.setText("Delete Edge " + edge.getShortName());
    }
}
