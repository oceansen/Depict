package no.simula.depict.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import no.simula.depict.Activator;
import no.simula.depict.GlobalSymbols;
import no.simula.depict.export.HtmlTestcaseExport;
import no.simula.depict.model.TestCaseInteraction;
import no.simula.rcp.utility.ResourceUtility;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class InteractionCoverageTableView extends ViewPart
{
	private static final int GRID_LAYOUT_COLUMNS = 3;
	private TableViewer tableViewer;
	private ServiceRegistration<?> serviceRegistration;
	private Composite parent;
	private Button optStandardView;
	private Button optDataHoleView;
	private Button optDataCoveredView;
	private Action actionExportResult;
	//private Action actionRefresh;
	private IResource lastCteResource;
	private InteractionCoverageTableSort comparator;
	private List<TestCaseInteraction> testCaseInteractions;
	private List<TestCaseInteraction> lastTestCaseInteractions;
	
	public InteractionCoverageTableView() 
	{
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		this.parent = parent;
		testCaseInteractions = new ArrayList<TestCaseInteraction>();
		lastTestCaseInteractions = new ArrayList<TestCaseInteraction>();
		
		subscribeRefreshEvent();
		createView(parent);
	    // Set the sorter for the table
	    comparator = new InteractionCoverageTableSort();
	    tableViewer.setComparator(comparator);

		optStandardView.setSelection(true);

		createActions();
		createToolbar();
		
		actionExportResult.setEnabled(false);
		//actionRefresh.setEnabled(false);
	}

	@Override
	public void setFocus() 
	{
	}
	
	@Override
	public void dispose() 
	{
		super.dispose();
		serviceRegistration.unregister();
	}

	private void createView(Composite parent)
	{
		GridLayout gl = new GridLayout(GRID_LAYOUT_COLUMNS, false);
	    parent.setLayout(gl);
		
		optStandardView = new Button(parent, SWT.RADIO);
		optStandardView.setText("Standard view");
		optStandardView.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				populateTable();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		optDataHoleView = new Button(parent, SWT.RADIO);
		optDataHoleView.setText("Holes in data");
		optDataHoleView.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				populateTable();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		optDataCoveredView = new Button(parent, SWT.RADIO);
		optDataCoveredView.setText("Covered data");
		optDataCoveredView.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				populateTable();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		createColumns(table);
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setInput(testCaseInteractions);
		
		// Layout the viewer
		GridData gd = null;
		gd = new GridData();
		gd.horizontalSpan = GRID_LAYOUT_COLUMNS;
		gd.heightHint = 250;
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		table.setLayoutData(gd);
	}

    private void createToolbar() 
    {
    	IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
    	//tm.add(actionRefresh);
    	tm.add(actionExportResult);
	}

	private void createActions() 
	{
		/*
		actionRefresh = new Action("Dataset refresh") {
			public void run() {
				InteractionCoveragePublisher.getInstance().refresh();
			}
		};
		actionRefresh.setImageDescriptor(ResourceUtility.getImageDescriptor("1375808581_database_refresh.png"));
		*/
		
		actionExportResult = new Action("Export current test case result") {
			public void run() {
				FileDialog dialog = new FileDialog(getViewSite().getShell(), SWT.SAVE);
				dialog.setFilterExtensions(new String[] {"*.html", "*.*"});
				dialog.setFilterNames(new String[] {"HTML file", "All Files"});
				String fileSelected = dialog.open();
				if (fileSelected != null) 
					exportResultToFile(fileSelected);
			}
		};
		actionExportResult.setImageDescriptor(ResourceUtility.getImageDescriptor("1375879317_table_export.png"));
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
	    		lastCteResource = (IResource) event.getProperty(GlobalSymbols.EVENT_PARAM_CTE_RESOURCE);
	    		
	    		if (parent.getDisplay().getThread() == Thread.currentThread()) 
	    		{
					refreshView(tcis);
					actionExportResult.setEnabled(true);
					//actionRefresh.setEnabled(true);
	    		}
	    		else
	    		{
	    			parent.getDisplay().syncExec(new Runnable() {
	    				public void run() {
	    					refreshView(tcis);
	    					actionExportResult.setEnabled(true);
	    					//actionRefresh.setEnabled(true);
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
	
	private void exportResultToFile(String fileName)
	{
		if (testCaseInteractions == null)
			return;
		
		if (lastCteResource == null)
			return;
		
		HtmlTestcaseExport htmlExp = new HtmlTestcaseExport(lastCteResource.getName(), testCaseInteractions, optDataHoleView.getSelection());
		try 
		{
			htmlExp.export(fileName);
			MessageDialog.openInformation(parent.getShell(), "Interaction coverage export", "Data interaction coverage export completed!");
		} 
		catch (IOException e) 
		{
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(status);
			MessageDialog.openError(parent.getShell(), "Interaction coverage export", e.getMessage());
		}
	}
	
	private void refreshView(List<TestCaseInteraction> tcis)
	{
		tableViewer.getTable().removeAll();
		lastTestCaseInteractions.clear();
		if (tcis == null)
			return;

		lastTestCaseInteractions.addAll(tcis);
		//testCaseInteractions = tcis;
		
		populateTable();
	}
	
	private void populateTable()
	{
		boolean holeView = optDataHoleView.getSelection() ? true : false;
		boolean coveredView = optDataCoveredView.getSelection() ? true : false;
		
		testCaseInteractions.clear();
		for (TestCaseInteraction tci : lastTestCaseInteractions)
		{
			if (holeView)
			{
				if (tci.getInteractionsCount() == 0)
					testCaseInteractions.add(tci);
			}
			else if (coveredView)
			{
				if (tci.getInteractionsCount() != 0)
					testCaseInteractions.add(tci);
			}
			else
				testCaseInteractions.add(tci);
	    }

		tableViewer.refresh();
	}
	
	private void createColumns(Table table)
	{
		TableViewerColumn tvCol = createTableViewerColumn("Id", 50, SWT.RIGHT); 
		tvCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) 
			{
				TestCaseInteraction tci = (TestCaseInteraction) element;
				return String.valueOf(tci.getInteractionsId());
			}
	    });
	    TableColumn col = tvCol.getColumn(); 
	    col.addSelectionListener(getSelectionAdapter(col, 0));

		tvCol = createTableViewerColumn("Test Case", 150, SWT.LEFT); 
		tvCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) 
			{
				TestCaseInteraction tci = (TestCaseInteraction) element;
				return tci.getTestCase().getFullName();
			}
	    });

		col = tvCol.getColumn(); 
	    col.addSelectionListener(getSelectionAdapter(col, 1));
		

		tvCol = createTableViewerColumn("Count", 50, SWT.RIGHT); 
		tvCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) 
			{
				TestCaseInteraction tci = (TestCaseInteraction) element;
				return String.valueOf(tci.getInteractionsCount());
			}
	    });
		
	    col = tvCol.getColumn(); 
	    col.addSelectionListener(getSelectionAdapter(col, 2));

		tvCol = createTableViewerColumn("Expression", 250, SWT.LEFT); 
		tvCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) 
			{
				TestCaseInteraction tci = (TestCaseInteraction) element;
				return tci.getExpressions().toString();
			}
	    });
		
		tvCol = createTableViewerColumn("Elapsed [msec.]", 100, SWT.RIGHT); 
		tvCol.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) 
			{
				TestCaseInteraction tci = (TestCaseInteraction) element;
				return String.valueOf(tci.getElapsedTimeMillis());
			}
	    });
	}
	
	private TableViewerColumn createTableViewerColumn(String title, int bound, int style) 
	{
		final TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, style);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);		    
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}


	private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) 
	{
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
		        int dir = comparator.getDirection();
		        tableViewer.getTable().setSortDirection(dir);
		        tableViewer.getTable().setSortColumn(column);
		        tableViewer.refresh();
	        }
		};
		
		return selectionAdapter;
	}
}
