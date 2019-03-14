package no.simula.depict.ui;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.simula.depict.Activator;
import no.simula.depict.Application;
import no.simula.depict.GlobalSymbols;
import no.simula.depict.data.CteConnectionInfo;
import no.simula.depict.data.DataManager;
import no.simula.depict.manager.CteModelManager;
import no.simula.depict.manager.SqlExpression;
import no.simula.depict.model.Cargo;
import no.simula.depict.model.CteObject;
import no.simula.depict.model.TestCaseInteraction;
import no.simula.depict.project.DepictProjectSupport;
import no.simula.rcp.utility.ProjectUtility;
import no.simula.rcp.utility.ResourceUtility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class InteractionCoveragePublisher implements ISelectionListener 
{
	private static InteractionCoveragePublisher me;
	private IResource lastSelectedResource;
	private String filterExpression;
	private CteObject cteo;
	private Map<String, List<TestCaseInteraction>> dataSetsCache;
	
	private InteractionCoveragePublisher() 
	{
        dataSetsCache = new HashMap<String, List<TestCaseInteraction>>();
	}

	public static InteractionCoveragePublisher getInstance()
	{
		if (me == null)
		{
			me = new InteractionCoveragePublisher();
			IWorkbench workbench = PlatformUI.getWorkbench();
	        workbench.getActiveWorkbenchWindow().getActivePage().addSelectionListener(GlobalSymbols.VIEW_ID_NAVIGATOR, (ISelectionListener) me);
		}
		
		return me;
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{
		IResource r = ProjectUtility.toResource((IStructuredSelection) selection);
		
		if (r == null)
		{
			notifySubscribers(null);
			return;
		}
		
		if (!ResourceUtility.isFile(r))
		{
			notifySubscribers(null);
			return;
		}
		
		if (!r.getFileExtension().equals(GlobalSymbols.FILE_EXT_TESTONA))
		{
			notifySubscribers(null);
			return;
		}
		
		filterExpression = null;
		lastSelectedResource = r;
		loadDataModel(r);
		onDataInteractionModelSelection(r.getFullPath().toString());
		
		/*
		String s = r.getFullPath().toString();
		if (lastSelectedResource != r)
			lastSelectedResource = r;
		else
			return;
		
		CteModelManager ctemh = new CteModelManager();
		IFile f = DepictProjectSupport.getGraphModelFile((IFile) r);
		cteo = ctemh.toCteObject(ResourceUtility.toFile(r), ResourceUtility.toFile(f));
		filterExpression = null;
		
		Job job = new Job("Depict views update ...") {
		    @Override
		    protected IStatus run(IProgressMonitor monitor) {
		        refresh(cteo, monitor);
		        return Status.OK_STATUS;
		    }
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
		*/
	}
	
	private void loadDataModel(IResource r)
	{
		CteModelManager ctemh = new CteModelManager();
		IFile f = DepictProjectSupport.getGraphModelFile((IFile) r);
		cteo = ctemh.toCteObject(ResourceUtility.toFile(r), ResourceUtility.toFile(f));
	}
	
	private void onDataInteractionModelSelection(String modelName)
	{
		//
		// If a cache exist then show cached data
		if (dataSetsCache.containsKey(modelName))
		{
			List<TestCaseInteraction> tcis = dataSetsCache.get(modelName);
			notifySubscribers(tcis);
		}
		else
			notifySubscribers(null);
	}
	
	private void setCacheData(List<TestCaseInteraction> tcis)
	{
		String key = lastSelectedResource.getFullPath().toString();
		dataSetsCache.put(key, tcis);
	}
	
	public List<TestCaseInteraction> getLatestTestCaseIteraction()
	{
		String key = lastSelectedResource.getFullPath().toString();
		return dataSetsCache.get(key);
	}
	
	private void refresh(CteObject cteo, IProgressMonitor monitor)
	{
		long t1 = System.currentTimeMillis();
		//interactionCoverages = new ArrayList<InteractionCoverage>();
		CteModelManager ctemh = new CteModelManager();
		try 
		{
			int n = 0;
			CteConnectionInfo ci = ctemh.toConnectionInfo(cteo);
			DataManager dm = new DataManager();
			dm.setup(ci);
	        
			Cargo c = ctemh.unserializeModel(cteo);
			List<TestCaseInteraction> tcis = c.getTestCaseInteractions();

	        monitor.beginTask("Refreshing interaction coverage data ...", tcis.size());
			for (TestCaseInteraction tci : tcis)
			{
				++n;
				if (tci.getExpressions().size() > 0)
				{
					long start = System.currentTimeMillis();
					dm.open();
					
					String sql = "";
					if (getFilterExpression() != null)
						sql = SqlExpression.addFilter(tci.getSqlStatement(), getFilterExpression());
					else
						sql = tci.getSqlStatement();
					
					ResultSet rs = dm.execute(SqlExpression.toWrappedSQLCount(sql));
					long elapsed = System.currentTimeMillis() - start;
					
					if (rs.next())
					{
						tci.setInteractionsCount(rs.getInt(1));
						tci.setElapsedTimeMillis(elapsed);
					}
					else
					{
						tci.setInteractionsCount(0);
						tci.setElapsedTimeMillis(0);
					}
					
					dm.close();
					System.out.println(String.format("%d) [%s] elapsed:%d msecs. \n\t%s", n, tci.getTestCase().getName(), elapsed, sql)); // n + ") [" + tci.getTestCase().getName() + "] " + tci.getSqlCountStatement());
				}
				else
					tci.setInteractionsCount(0);
				
				if (monitor.isCanceled())
					throw new OperationCanceledException();

				monitor.worked(1);
			}
			
			//testCaseInteractions = tcis;
			
	        monitor.done();
			
	        setCacheData(tcis);
			notifySubscribers(tcis);
			IStatus status = new Status(IStatus.INFO, Activator.PLUGIN_ID, String.format("Model: %s. Generated %d queries in %d msec.", lastSelectedResource.getName(), n, System.currentTimeMillis() - t1), null);
			Activator.getDefault().getLog().log(status);
		} 
		catch (final Exception e) 
		{
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(status);

			final Display d = PlatformUI.getWorkbench().getDisplay();
			d.asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(d.getActiveShell(), 
							"Refreshing interaction model coverage", 
							"Error: " + e.getMessage());
				}
			});
		}
	}

	private void notifySubscribers(List<TestCaseInteraction> tcis)
	{
		//
		// Send and asynchronous event to all the subscribers in order to notify that a new model interactions have been computed
        BundleContext ctx = FrameworkUtil.getBundle(Application.class).getBundleContext();
        ServiceReference<?> ref = ctx.getServiceReference(EventAdmin.class.getName());
        EventAdmin eventAdmin = (EventAdmin) ctx.getService(ref);
        
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(no.simula.depict.GlobalSymbols.EVENT_PARAM_INTERACTION_COVERAGE_OBJECT, tcis);
        properties.put(no.simula.depict.GlobalSymbols.EVENT_PARAM_CTE_RESOURCE, lastSelectedResource);
        
        Event ev = new Event(no.simula.depict.GlobalSymbols.EVENT_ID_INTERACTION_COVERAGE_REFRESHED, properties);
        eventAdmin.postEvent(ev);
	}

	public String getFilterExpression() {
		return filterExpression;
	}

	public void setFilterExpression(String filterExpression) {
		this.filterExpression = filterExpression;

		Job job = new Job("Depict views update ...") {
		    @Override
		    protected IStatus run(IProgressMonitor monitor) {
		        refresh(cteo, monitor);
		        return Status.OK_STATUS;
		    }
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
	}

	public void refresh() {
		Job job = new Job("Depict views refresh ...") {
		    @Override
		    protected IStatus run(IProgressMonitor monitor) {
		        refresh(cteo, monitor);
		        return Status.OK_STATUS;
		    }
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule();
	}

	/*
	public List<TestCaseInteraction> getTestCaseInteractions() {
		return testCaseInteractions;
	}
	*/
}
