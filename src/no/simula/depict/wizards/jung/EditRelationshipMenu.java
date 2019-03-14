package no.simula.depict.wizards.jung;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import no.simula.depict.data.TableColumnMetadata;
import no.simula.depict.manager.EntityRelationshipModelManager;
import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;
import no.simula.depict.wizards.EditSurrogateRelationshipDialog;
import no.simula.depict.wizards.ImportCTEModelWizard.ModelImportCargo;

public class EditRelationshipMenu 
{
    public static class EdgeMenu extends JPopupMenu 
    {
    	private static final long serialVersionUID = 1L;

		// private JFrame frame; 
        public EdgeMenu(final Frame frame, final Graph<DepictVertex, DepictEdge> graph, final ModelImportCargo modelImportCargo) 
        {
            super("Edge Menu");
            // this.frame = frame;
            this.add(new EdgePropItem(frame, graph, modelImportCargo));           
            //this.addSeparator();
            //this.add(new WeightDisplay());
            //this.add(new CapacityDisplay());
            this.addSeparator();
            this.add(new DeleteEdgeMenuItem());
        }
    }
    
    public static class EdgePropItem extends JMenuItem implements EdgeMenuListener, MenuPointListener 
    {
		private static final long serialVersionUID = 1L;
		
		private DepictEdge edge;
		/*
		private VisualizationViewer<DepictVertex, DepictEdge> vv;
		private Point2D point;
		private List<TableMetadata> tables;
        */
		
        public EdgePropItem(final Frame frame, final Graph<DepictVertex, DepictEdge> graph, final ModelImportCargo modelImportCargo) 
        {
        	super("Edit Edge Properties...");
        	this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
            		EntityRelationshipModelManager erg = new EntityRelationshipModelManager(modelImportCargo.getCteObject(), modelImportCargo.getConnectionInfo());

                	try 
                	{
   						List<TableColumnMetadata> foreignTableCols = erg.getTableColumns(edge.getForeignTableName());
   						List<TableColumnMetadata> primaryTableCols = erg.getTableColumns(edge.getPrimaryTableName());
   						EditSurrogateRelationshipDialog esr = new EditSurrogateRelationshipDialog(frame, edge, foreignTableCols, primaryTableCols, edge.isSurrogate() ? false : true);
   						if (esr.isOk())
   						{
   							edge.getForeignKeyColumns().clear();
   							if (esr.isRightTablePrimary())
   							{
	   							for (TableColumnMetadata t : esr.getForeignKeySelectedColumns())
	   								edge.getForeignKeyColumns().add(t.getName());
   							}
   							else
   							{
	   							for (TableColumnMetadata t : esr.getPrimaryKeySelectedColumns())
	   								edge.getForeignKeyColumns().add(t.getName());
   							}
   							
   							edge.getPrimaryKeyColumns().clear();
   							if (esr.isRightTablePrimary())
   							{
	   							for (TableColumnMetadata t : esr.getPrimaryKeySelectedColumns())
	   								edge.getPrimaryKeyColumns().add(t.getName());
   							}
   							else
   							{
	   							for (TableColumnMetadata t : esr.getForeignKeySelectedColumns())
	   								edge.getPrimaryKeyColumns().add(t.getName());
   							}
   							
   							//
   							// C.I. Feb. 10th 2014
   							// Recursive relationship management
   							
   							/*
   							Pair<DepictVertex> p = graph.getEndpoints(edge);
   							if (p.getFirst().equals(p.getSecond()))
   								p.getFirst().setRecursiveRelationship(true);
   							*/
   						}
					} 
                	catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException e) 
                	{
						e.printStackTrace();
					}

                    //EdgePropertyDialog dialog = new EdgePropertyDialog(frame, edge);
                    //dialog.setLocation((int)point.getX()+ frame.getX(), (int)point.getY()+ frame.getY());
                    //dialog.setVisible(true);
                }
            });
        }

        public void setEdgeAndView(DepictEdge edge, VisualizationViewer<DepictVertex, DepictEdge> vv) 
        {
            this.edge = edge;
            //this.vv = vv;
        }

        public void setPoint(Point2D point) {
            //this.point = point;
        }
    }

    /*
    public static class WeightDisplay extends JMenuItem implements EdgeMenuListener<Samples.MouseMenu.GraphElements.MyEdge> {
        public void setEdgeAndView(GraphElements.MyEdge e, VisualizationViewer visComp) {
            this.setText("Weight " + e + " = " + e.getWeight());
        }
    }
    
    public static class CapacityDisplay extends JMenuItem implements EdgeMenuListener<Samples.MouseMenu.GraphElements.MyEdge> {
        public void setEdgeAndView(GraphElements.MyEdge e, VisualizationViewer visComp) {
            this.setText("Capacity " + e + " = " + e.getCapacity());
        }
    }
	*/
    
    /*
    public static class VertexMenu extends JPopupMenu {
        public VertexMenu() {
            super("Vertex Menu");
            this.add(new DeleteVertexMenuItem<GraphElements.MyVertex>());
            this.addSeparator();
            this.add(new pscCheckBox());
            this.add(new tdmCheckBox());
        }
    }
    
    public static class pscCheckBox extends JCheckBoxMenuItem implements VertexMenuListener<GraphElements.MyVertex> {
        GraphElements.MyVertex v;
        
        public pscCheckBox() {
            super("PSC Capable");
            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    v.setPacketSwitchCapable(isSelected());
                }
                
            });
        }
        public void setVertexAndView(GraphElements.MyVertex v, VisualizationViewer visComp) {
            this.v = v;
            this.setSelected(v.isPacketSwitchCapable());
        }
        
    }
    
        public static class tdmCheckBox extends JCheckBoxMenuItem implements VertexMenuListener<GraphElements.MyVertex> {
        GraphElements.MyVertex v;
        
        public tdmCheckBox() {
            super("TDM Capable");
            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    v.setTdmSwitchCapable(isSelected());
                }
                
            });
        }
        public void setVertexAndView(GraphElements.MyVertex v, VisualizationViewer visComp) {
            this.v = v;
            this.setSelected(v.isTdmSwitchCapable());
        }
        
    }
    */
}
