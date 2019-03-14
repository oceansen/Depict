package no.simula.depict.ui;

import java.awt.Dimension;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import no.simula.depict.Activator;
import no.simula.depict.GlobalSymbols;
import no.simula.depict.model.TestCaseInteraction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class InteractionCoverageGraphView extends ViewPart
{
	private static final int GRAPHS_PER_ROW = 1;
	
	private Composite composite;
	private ScrolledComposite scrolledComposite;
	private ServiceRegistration<?> serviceRegistration;
	private Composite parent;
	
	public InteractionCoverageGraphView() 
	{
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		this.parent = parent;
		init(parent);
		createView(parent);
		
		refreshView(InteractionCoveragePublisher.getInstance().getLatestTestCaseIteraction());
	}

	@Override
	public void setFocus() 
	{
	}
	
	@Override
	public void dispose() 
	{
		super.dispose();
		scrolledComposite.dispose();
		composite.dispose();
		serviceRegistration.unregister();
	}
	
	@Override
	public void showBusy(boolean busy) 
	{
	    super.showBusy(busy);
	    if(busy)
	        setPartName("I'm doing a job right now...");
	    else
	        setPartName("Sample View");
	}
	
	private void init(Composite parent)
	{
		subscribeRefreshEvent();
	}
	
	private void subscribeRefreshEvent()
	{
		BundleContext ctx = FrameworkUtil.getBundle(Activator.class).getBundleContext();
	    EventHandler handler = new EventHandler() 
	    {
	    	public void handleEvent(final Event event)
	    	{
	    		@SuppressWarnings("unchecked")
				final List<TestCaseInteraction> tcis = (List<TestCaseInteraction>) event.getProperty(GlobalSymbols.EVENT_PARAM_INTERACTION_COVERAGE_OBJECT);

	    		if (parent.getDisplay().getThread() == Thread.currentThread()) 
	    		{
					refreshView(tcis);
	    		}
	    		else
	    		{
	    			parent.getDisplay().syncExec(new Runnable() {
	    				public void run() {
	    					refreshView(tcis);
	    				}
	    			});
	    		}
    		}
	    };
	    
	    //
	    // Subscribe the event related to family model refresh
	    Dictionary<String, String> properties = new Hashtable<String, String>();
	    properties.put(EventConstants.EVENT_TOPIC, GlobalSymbols.EVENT_ID_INTERACTION_COVERAGE_REFRESHED);
	    serviceRegistration = ctx.registerService(EventHandler.class.getName(), handler, properties);
	}
	
	private void createView(Composite parent)
	{
		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		composite = new Composite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout(GRAPHS_PER_ROW, true));
	}
	
	private void createGraph(List<TestCaseInteraction> tcis)
	{
		final JFreeChart chart = InteractionBarchart.createGraph(tcis);
		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(500, 500));
		
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint = 300;
		gridData.widthHint = 150;
		composite.setSize(1500, 1000);
		ChartComposite cc = new ChartComposite(composite, SWT.NONE, chart, true);
		cc.setLayoutData(gridData);
		
        scrolledComposite.setContent(composite);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void refreshView(final List<TestCaseInteraction> tcis)
	{
		for (Control control : composite.getChildren()) {
	        control.dispose();
	    }
		
		//if (tcis == null)
		//	return;
		
		createGraph(tcis);
	}
}
