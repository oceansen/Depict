package no.simula.depict.wizards;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.xml.bind.JAXBException;

import no.simula.depict.data.CteConnectionInfo;
import no.simula.depict.data.DataManager;
import no.simula.depict.data.TableColumnMetadata;
import no.simula.depict.data.TableMetadata;
import no.simula.depict.manager.CteModelManager;
import no.simula.depict.manager.EntityRelationshipModelManager;
import no.simula.depict.model.CteClassification;
import no.simula.depict.model.CteComposition;
import no.simula.depict.model.CteObject;
import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;
import no.simula.depict.wizards.ImportCTEModelWizard.ModelImportCargo;
import no.simula.depict.wizards.jung.EditRelationshipMenu;
import no.simula.depict.wizards.jung.PopupMouseMenu;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class CreateDbRelationshipsPage extends WizardPage 
{
	private static final String WRONG_CATALOG_NAME = "Wrong catalog name [%s]";
	private static final String WRONG_SCHEMA_NAME = "Wrong schema name [%s]";
	private static final String WRONG_TABLE_NAME = "Wrong table name [%s]";
	private static final String WRONG_COLUMN_NAME = "Wrong column name [%s.%s]";
	private static final String MISSING_COLUMNS = "None columns defined for table: [%s]";
	
	private JApplet japplet;
	private Frame frame;
	private Composite container;
	private JPanel pnlGraph;
	private JRadioButton rdoTransform;
	private JRadioButton rdoPick;
	private JRadioButton rdoEdit;
	private ModelImportCargo modelImportCargo;
	private Graph<DepictVertex, DepictEdge> graph;
	private Graph<DepictVertex, DepictEdge> subGraph;
	private EditingModalGraphMouse<DepictVertex, DepictEdge> gm;
	
	public CreateDbRelationshipsPage(String pageName, IProject selectedProject, ModelImportCargo modelImportCargo) 
	{
		super(pageName);
		//this.selection = selection;
		//this.selectedProject = selectedProject;
		setTitle("Select the right relationships according to the imported data interaction");
		setDescription("Create a path connecting all the yellow nodes of the graph. When you've done with the selection click 'Check'");
		this.modelImportCargo = modelImportCargo;
	}

	public void createControl(Composite parent) 
	{
		container = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);

		frame = SWT_AWT.new_Frame(container);
		japplet = new JApplet();
	    //content.setBackground(Color.white);
	    //content.setLayout(new GridBagLayout()); 
	    
	    JButton cmdCheck = new JButton("Check model");
        //cmdCheck.setPreferredSize(new Dimension(80, 25));
	    cmdCheck.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final boolean ret = checkGraph();
    			container.getDisplay().syncExec(new Runnable() {
    				public void run() {
    					if (ret)
    						MessageDialog.openInformation(Display.getDefault().getActiveShell(), 
    								"Check data interaction definition", 
    								"The defined path is formally correct.\nYou can complete the import procedure by clicking 'Finish'.");
    					else
    						MessageDialog.openWarning(Display.getDefault().getActiveShell(), 
    								"Check data interaction definition", 
    								"The path you defined is not correct.\nPlease ensure that:\n 1) All the entities coming from the model (the yellow ones) are along the path.\n 2) All the surrogate relationships have been properly defined");
    					setPageComplete(ret);
					}
    			});							
			}});
	    
	    rdoTransform = new JRadioButton("Transform");
	    rdoTransform.setSelected(true);
	    rdoTransform.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			    gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
			}});
	    
	    rdoPick = new JRadioButton("Pick");
	    rdoPick.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gm.setMode(ModalGraphMouse.Mode.PICKING);
			}});

	    rdoEdit = new JRadioButton("Edit");
	    rdoEdit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gm.setMode(ModalGraphMouse.Mode.EDITING);
			}});

	    ButtonGroup group = new ButtonGroup();
	    group.add(rdoTransform);
	    group.add(rdoPick);
	    group.add(rdoEdit);

	    JPanel pnlToolbar = new JPanel();
	    pnlToolbar.setLayout(new BoxLayout(pnlToolbar, BoxLayout.LINE_AXIS));
	    pnlToolbar.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
	    pnlToolbar.add(cmdCheck);
	    pnlToolbar.add(Box.createRigidArea(new Dimension(10, 0)));
	    pnlToolbar.add(rdoTransform);
	    pnlToolbar.add(Box.createRigidArea(new Dimension(10, 0)));
	    pnlToolbar.add(rdoPick);
	    pnlToolbar.add(Box.createRigidArea(new Dimension(10, 0)));
	    pnlToolbar.add(rdoEdit);
	    pnlToolbar.add(Box.createHorizontalGlue());

	    pnlGraph = new JPanel();
	    pnlGraph.setLayout(new BoxLayout(pnlGraph, BoxLayout.PAGE_AXIS));

	    Container content = japplet.getContentPane();
	    //content.add(pnlToolbar, BorderLayout.PAGE_START);
	    content.add(pnlGraph, BorderLayout.CENTER);
	    content.add(pnlToolbar, BorderLayout.PAGE_END);
		frame.add(japplet);
		
		frame.pack();
		frame.setVisible(true);

		setControl(container);
		setPageComplete(false);		
	}
	
	private boolean checkGraph()
	{
		//
		// 1) Check for special case: only one entity selected
		// 2) Add all selected arcs and corresponding vertices into a new Graph<.,.> object
		// 3) Check:
		//		a) If the newly created Graph<.,.> contains all data interaction model entities
		//		b) If Graph<.,.> is connected
		//	if both conditions are true then the graph represents a feasible SQL statement
		
		//
		// Copy selected elements into a temporary graph
		subGraph = new DirectedSparseGraph<DepictVertex, DepictEdge>();
		
		/*
		if (getDataInteractionEntitiesCount(graph) == 1)
		{
			List<DepictVertex> vs = getDataInteractionSelection(graph);
			for (DepictVertex v : vs)
			{
				subGraph.addVertex(v);
				//
				// Any self relationship?
				Collection<DepictEdge> es = graph.getIncidentEdges(v);
				for (DepictEdge e : es)
				{
					if (e.isSelected())
					{
						Collection<DepictVertex> c = graph.getIncidentVertices(e);
						subGraph.addEdge(e, c);
					}
				}
			}			
			return true;
		}
		*/
		
		for (DepictEdge e : graph.getEdges())
		{
			if (e.isSurrogate())
			{
				//
				// Just check if the following list is empty because all the other checks have been accomplished in the editor
				if (e.getForeignKeyColumns().size() == 0)
					return false;
			}
			
			if (e.isSelected())
			{
				Collection<DepictVertex> c = graph.getIncidentVertices(e);
				subGraph.addEdge(e, c);
			}
		}
		
		//System.out.println(subGraph);
		
		//
		// Collect all data interaction entities selected into the edited model
		//Collection<DepictVertex> c = subGraph.getVertices();
		/*
		List<DepictVertex> vs = new ArrayList<DepictVertex>();
		for (DepictVertex v : c)
			if (v.isOutputVertex())
				vs.add(v);
		*/
		
		Collection<DepictVertex> c = graph.getVertices();
		for (DepictVertex v : c)
			if (v.isOutputVertex())
				if (!subGraph.containsVertex(v))
					subGraph.addVertex(v);
		
		//
		// Does edited model contain all "output" vertices?
		/*
		Collection<DepictVertex> c = graph.getVertices();
		for (DepictVertex v : c)
			if (v.isOutputVertex())
				if (!subGraph.containsVertex(v))
					return false;
		*/
		
		if (getWeakComponentsCount(subGraph) > 1)
			return false;
		
		//
		// Any self relationship?
		// TODO: eliminare attributo recursive in DepictVertex
		for (DepictEdge e : subGraph.getEdges())
		{
			Pair<DepictVertex> p = subGraph.getEndpoints(e);
			if (p.getFirst().equals(p.getSecond()))
			{
				p.getFirst().setRecursiveRelationship(true);
				e.setRecursiveRelationship(true);
			}
		}

		return true;
	}
	
	//
	// Return the number of weak connected components
	private int getWeakComponentsCount(Graph<DepictVertex, DepictEdge> g)
	{
		WeakComponentClusterer<DepictVertex, DepictEdge> wcc = new WeakComponentClusterer<DepictVertex, DepictEdge>();
		Set<Set<DepictVertex>> vertexSets = wcc.transform(g);
		
		//System.out.println("---------------");
		//System.out.println("Weakly connected components");
		//for (Set<DepictVertex> vs : vertexSets)
		//	System.out.println(vs.toString());
		return vertexSets.size();
	}

	/*
	private int getDataInteractionEntitiesCount(Graph<DepictVertex, DepictEdge> g)
	{
		int n = 0;
		Collection<DepictVertex> c = graph.getVertices();
		for (DepictVertex v : c)
			if (v.isOutputVertex())
				++n;
		
		return n;
	}
	
	private List<DepictVertex> getDataInteractionSelection(Graph<DepictVertex, DepictEdge> g)
	{
		List<DepictVertex> vs = new ArrayList<DepictVertex>();
		Collection<DepictVertex> c = g.getVertices();
		for (DepictVertex v : c)
			if (v.isOutputVertex())
				vs.add(v);
		
		return vs;
	}
*/
	
	public void onEnterPage() throws ClassNotFoundException, InstantiationException, IllegalAccessException, JAXBException, CteModelValidationException, SQLException, IOException
	{
		EntityRelationshipModelManager erg = new EntityRelationshipModelManager(modelImportCargo.getCteObject(), modelImportCargo.getConnectionInfo());
		graph = erg.extract();
		refresh();
	}
	
	private void refresh()
	{
		pnlGraph.removeAll();
		pnlGraph.add(createGraph(graph));
		
		Point p = container.getSize();
		++p.x;
		container.setSize(p);
		--p.x;
		container.setSize(p);
	}

	private BasicVisualizationServer<DepictVertex, DepictEdge> createGraph(Graph<DepictVertex, DepictEdge> graph)
	{
		Transformer<DepictVertex, String> vertexLabel = new Transformer<DepictVertex, String>() {
			@Override
			public String transform(DepictVertex v) 
			{
				return v.getName();
			}
		};
		
		Transformer<DepictVertex, Paint> vertexPaint = new Transformer<DepictVertex, Paint>() {
			@Override
			public Paint transform(DepictVertex v) 
			{
				if (v.isOutputVertex())
					return Color.YELLOW;
				
				return Color.RED;
			}
		};
		
		float dash[] = {10.0f};
		final Stroke edgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		
		Transformer<DepictEdge, Stroke> edgeStrokeTransformer = new Transformer<DepictEdge, Stroke>() {
			@Override
			public Stroke transform(DepictEdge e) 
			{
				if (!e.isSelected())
					return edgeStroke;

				return null;
			}
		};
		
		Transformer<DepictEdge, String> edgeTooltip = new Transformer<DepictEdge, String>() {
			@Override
			public String transform(DepictEdge e) 
			{
				return e.getName();
			}
		};

		Transformer<DepictEdge, Paint> edgePaint = new Transformer<DepictEdge, Paint>() {
			@Override
			public Paint transform(DepictEdge e) 
			{
				if (e.isSurrogate())
					return Color.BLUE;
				
				return Color.BLACK;
			}
		};
		
		/*
		Transformer<DepictEdge, String> edgeLabel = new Transformer<DepictEdge, String>() {
			@Override
			public String transform(DepictEdge e) 
			{
				return e.getName();
			}
		};
		*/
		
		//Layout<DepictVertex, DepictEdge> layout = new CircleLayout<DepictVertex, DepictEdge>(graph);
		//Layout<DepictVertex, DepictEdge> layout = new ISOMLayout<DepictVertex, DepictEdge>(graph);
		Layout<DepictVertex, DepictEdge> layout = new KKLayout<DepictVertex, DepictEdge>(graph);
		//Layout<DepictVertex, DepictEdge> layout = new FRLayout<DepictVertex, DepictEdge>(graph);
		//Layout<DepictVertex, DepictEdge> layout = new SpringLayout2<DepictVertex, DepictEdge>(graph);
		//Point p = container.getSize();
		//layout.setSize(new Dimension(p.x, p.y));
		
		//BasicVisualizationServer<DepictVertex, DepictEdge> vv = new BasicVisualizationServer<DepictVertex, DepictEdge>(layout);
		final VisualizationViewer<DepictVertex, DepictEdge> vv = new VisualizationViewer<DepictVertex, DepictEdge>(layout);
		//vv.setPreferredSize(new Dimension(450, 450));

		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setVertexLabelTransformer(vertexLabel);
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.S);

		PickedState<DepictVertex> ps = vv.getPickedVertexState();
	    VertexStrokeHighlight<DepictVertex, DepictEdge> vsh = new VertexStrokeHighlight<DepictVertex, DepictEdge>(graph, ps);
        vv.getRenderContext().setVertexStrokeTransformer(vsh);

		vv.setEdgeToolTipTransformer(edgeTooltip);
		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);
		
        Factory<DepictVertex> vertexFactory = new VertexFactory();
        Factory<DepictEdge> edgeFactory = new DepictEdgeFactory(vv);

		//DefaultModalGraphMouse<DepictVertex, DepictEdge> gm = new DefaultModalGraphMouse<DepictVertex, DepictEdge>();
		gm = new EditingModalGraphMouse<DepictVertex, DepictEdge>(vv.getRenderContext(), vertexFactory, edgeFactory);
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		
		//
		// Custom context menu
        PopupMouseMenu mnu = new PopupMouseMenu();
        // Add some popup menus for the edges and vertices to our mouse plugin.
        JPopupMenu edgeMenu = new EditRelationshipMenu.EdgeMenu(frame, graph, modelImportCargo);
        //JPopupMenu vertexMenu = new EditRelationshipMenu.VertexMenu();
        mnu.setEdgePopup(edgeMenu);
        //mnu.setVertexPopup(vertexMenu);
        gm.remove(gm.getPopupEditingPlugin());  // Removes the existing popup editing plugin
        gm.add(mnu);   // Add our new plugin to the mouse
        
        vv.setGraphMouse(gm);

        vv.addKeyListener(gm.getModeKeyListener());
		vv.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent ke) {
				//System.out.println(ke);
				switch (ke.getKeyChar())
				{
					case 'p':
					case 'P':
					rdoPick.setSelected(true);
					break;
					
					case 't':
					case 'T':
					rdoTransform.setSelected(true);
					break;

					case 'e':
					case 'E':
					rdoEdit.setSelected(true);
					break;
				}
			}

			@Override
			public void keyReleased(KeyEvent arg0) {
			}

			@Override
			public void keyTyped(KeyEvent arg0) {
			}
		});
		
		vv.getPickedEdgeState().addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ie) {
				//System.out.println(ie);
				DepictEdge e = (DepictEdge) ie.getItem();
				if (ie.getStateChange() == ItemEvent.SELECTED)
					e.setSelected(!e.isSelected());
			}
		});
		
		//final PickedState<DepictVertex> pse = vv.getPickedEdgeState();
        final GraphElementAccessor<DepictVertex, DepictEdge> pickSupport = vv.getPickSupport();

		vv.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				Point2D p = e.getPoint();
				DepictVertex v = pickSupport.getVertex(vv.getGraphLayout(), p.getX(), p.getY());
				DepictEdgeFactory.DepictEdgeFactoryCargo.getInstance().setStartVertex(v);
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		
		return vv;
	}
	
	public boolean validate() throws JAXBException, CteModelValidationException, SQLException, ClassNotFoundException, IOException, InstantiationException, IllegalAccessException, no.simula.depict.manager.CteModelManager.CteModelValidationException
	{
		CteObject cteo = modelImportCargo.getCteObject();
		modelImportCargo.getTables().clear();
		DataManager dm = new DataManager();
		
		try
		{
			CteConnectionInfo dbci = modelImportCargo.getConnectionInfo();
			dm.setup(dbci);
			dm.open();
			//
			// Check DB name
			CteModelManager ctem = new CteModelManager();
			CteComposition catalog = ctem.getCatalog(cteo);
			String catalogName = catalog != null ? catalog.getName() : null;

			CteComposition cteSchema = ctem.getSchema(cteo);;
			String schemaName = cteSchema != null ? cteSchema.getName() : null;
			
			if (catalog != null)
			{
				if (!dm.databaseContainsCatalog(catalog.getName()))
					throw new CteModelValidationException(String.format(WRONG_CATALOG_NAME, catalog.getName()));
			}

			if (cteSchema != null)
			{
				//cteSchema = CteObject.getSchema(catalog);
				if (!dm.databaseContainsSchema(catalogName, schemaName))
					throw new CteModelValidationException(String.format(WRONG_SCHEMA_NAME, schemaName));
			}
			
			//
			// Check table names
			List<CteComposition> tables = cteSchema.getCompositions(); //cteo.isSchemaMissing() ? catalog.getCompositions() : cteSchema.getCompositions();
			for (CteComposition table : tables)
			{
				//System.out.println("Table name = " + table.getName());
				if (!dm.databaseContainsTable(catalogName, schemaName, table.getName()))
					throw new CteModelValidationException(String.format(WRONG_TABLE_NAME, table.getName()));
				
				//
				// Check column names
				if (table.getClassifications() == null)
					throw new CteModelValidationException(String.format(MISSING_COLUMNS, table.getName()));

				TableMetadata tmd = new TableMetadata(catalogName, schemaName, table.getName());
				List<TableColumnMetadata> columns = dm.getColumns(catalogName, schemaName, table.getName());
				//
				// Are all classification names (DB column names) correct?
				for (CteClassification c : table.getClassifications())
				{
					int i = columns.indexOf(new TableColumnMetadata(c.getName()));
					if (i == -1)
						throw new CteModelValidationException(String.format(WRONG_COLUMN_NAME, table.getName(), c.getName()));
				}
				
				for (TableColumnMetadata cmd : columns)
					tmd.getColumns().add(cmd);
				
				modelImportCargo.getTables().add(tmd);
				
				//
				// C.I. 23/01/2014 Added check for uniqueness of test case names
				CteModelManager cmm = new CteModelManager();
				cmm.checkNamesValidity(cteo);
			}
		}
		finally
		{
			dm.close();
		}

		return true;
	}

	private class CteModelValidationException extends Exception
	{
		private static final long serialVersionUID = -3568084747310171781L;
		
		public CteModelValidationException(String message)
		{
			super(message);
		}
	}

	public Graph<DepictVertex, DepictEdge> getSubGraph() {
		return subGraph;
	}
	
	private final static class VertexStrokeHighlight<V, E> implements Transformer<V, Stroke>
    {
        //protected boolean highlight = false;
        protected Stroke heavy = new BasicStroke(5);
        protected Stroke medium = new BasicStroke(3);
        protected Stroke light = new BasicStroke(1);
        protected PickedInfo<V> pi;
        protected Graph<V,E> graph;
        
        public VertexStrokeHighlight(Graph<V,E> graph, PickedInfo<V> pi)
        {
        	this.graph = graph;
            this.pi = pi;
        }

        /*
        public void setHighlight(boolean highlight)
        {
            this.highlight = highlight;
        }
        */
        
        public Stroke transform(V v)
        {
            //if (highlight)
            //{
            if (pi.isPicked(v))
                return heavy;
            else
            {
            	for(V w : graph.getNeighbors(v)) {
                    if (pi.isPicked(w))
                        return medium;
                }
                return light;
            }
            //}
            //else
            //    return light; 
        }

    }
	
    class VertexFactory implements Factory<DepictVertex> 
    {
		public DepictVertex create() 
		{
			return null;
		}
    }
}

