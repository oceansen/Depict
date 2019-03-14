package no.simula.depict.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Paint;
import java.awt.Stroke;

import javax.swing.JApplet;

import no.simula.depict.Activator;
import no.simula.depict.GlobalSymbols;
import no.simula.depict.manager.CteModelManager;
import no.simula.depict.model.Cargo;
import no.simula.depict.model.CteObject;
import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;
import no.simula.depict.project.DepictProjectSupport;
import no.simula.rcp.utility.ProjectUtility;
import no.simula.rcp.utility.ResourceUtility;

import org.apache.commons.collections15.Transformer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class ModelGraphView extends ViewPart implements ISelectionListener
{
	private JApplet japplet;
	private Frame frame;
	private Composite composite;
	private Component lastComponent = null;
	
	public ModelGraphView() 
	{
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		composite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		
		frame = SWT_AWT.new_Frame(composite);
		japplet = new JApplet();
		frame.add(japplet);
		frame.pack();
		frame.setVisible(true);
		
		getViewSite().getPage().addSelectionListener(GlobalSymbols.VIEW_ID_NAVIGATOR, this);	
	}

	@Override
	public void setFocus() 
	{
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
		
		//Layout<DepictVertex, DepictEdge> layout = new CircleLayout<DepictVertex, DepictEdge>(graph);
		//Layout<DepictVertex, DepictEdge> layout = new ISOMLayout<DepictVertex, DepictEdge>(graph);
		Layout<DepictVertex, DepictEdge> layout = new KKLayout<DepictVertex, DepictEdge>(graph);
		Point p = composite.getSize();
		layout.setSize(new Dimension(p.x, p.y));
		
		//BasicVisualizationServer<DepictVertex, DepictEdge> vv = new BasicVisualizationServer<DepictVertex, DepictEdge>(layout);
		VisualizationViewer<DepictVertex, DepictEdge> vv = new VisualizationViewer<DepictVertex, DepictEdge>(layout);
		//vv.setPreferredSize(new Dimension(450, 450));

		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
        //vv.getRenderer().setVertexRenderer(new GradientVertexRenderer<DepictVertex, DepictEdge>(Color.white, Color.red,	Color.white, Color.blue, vv.getPickedVertexState(),	false));
        
        PickedState<DepictVertex> ps = vv.getPickedVertexState();
	    VertexStrokeHighlight<DepictVertex, DepictEdge> vsh = new VertexStrokeHighlight<DepictVertex, DepictEdge>(graph, ps);

        vv.getRenderContext().setVertexStrokeTransformer(vsh);
		vv.getRenderContext().setVertexLabelTransformer(vertexLabel);
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.S);
		vv.getRenderContext().setEdgeDrawPaintTransformer(edgePaint);

		DefaultModalGraphMouse<DepictVertex, DepictEdge> gm = new DefaultModalGraphMouse<DepictVertex, DepictEdge>();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		//gm.setMode(ModalGraphMouse.Mode.PICKING);
		vv.setGraphMouse(gm);
		vv.addKeyListener(gm.getModeKeyListener());
		vv.setEdgeToolTipTransformer(edgeTooltip);

		return vv;
	}

	@Override
	public void dispose() 
	{
		super.dispose();
		//serviceRegistration.unregister();
		getViewSite().getPage().removeSelectionListener(GlobalSymbols.VIEW_ID_NAVIGATOR, this);
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{
		IResource r = ProjectUtility.toResource((IStructuredSelection) selection);
		if (r == null)
			return;

		if (!ResourceUtility.isFile(r))
			return;
		
		if (!r.getFileExtension().equals(GlobalSymbols.FILE_EXT_TESTONA))
			return;

		CteModelManager ctemh = new CteModelManager();
		IFile f = DepictProjectSupport.getGraphModelFile((IFile) r);
		CteObject cteo = ctemh.toCteObject(ResourceUtility.toFile(r), ResourceUtility.toFile(f));
		try 
		{
			Cargo c = ctemh.unserializeModel(cteo);
			refresh(c.getGraph());
		} 
		catch (Exception e) 
		{
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(status);
		}
	}
	
	private void refresh(Graph<DepictVertex, DepictEdge> graph)
	{
		if (lastComponent != null)
			japplet.remove(lastComponent);
		lastComponent = createGraph(graph);
		japplet.add(lastComponent);

		Point p = composite.getSize();
		++p.x;
		composite.setSize(p);
		--p.x;
		composite.setSize(p);
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
}
