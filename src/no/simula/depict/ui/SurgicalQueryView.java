package no.simula.depict.ui;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import no.simula.depict.GlobalSymbols;
import no.simula.depict.data.DataManager;
import no.simula.depict.manager.CteModelManager;
import no.simula.depict.model.CteObject;
import no.simula.depict.model.CteTestCase;
import no.simula.depict.model.CteTestGroup;
import no.simula.depict.project.DepictProjectSupport;
import no.simula.rcp.utility.ProjectUtility;
import no.simula.rcp.utility.ResourceUtility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class SurgicalQueryView extends ViewPart implements ISelectionListener 
{
	private static final String VIEW_CAPTION = "Retrieved %d rows from current test cases selection";
	private static final String TREE_ITEM_ROOT = "Test cases";
	
	private static final int PAGE_SIZE = 100;
	private static final int DEFAULT_ROW_COUNT = 10;
	private static final int GRID_LAYOUT_COLUMNS = 3;
	
	private TableViewer viewer;
	private DataManager dm;
	private CteObject lastSelectedCteobject;
	private Text txtMaxRows;
	private Button chkPickRandom;
	private Tree treeTestcases;
	private Text txtSelectionSummary;
	private Action actionRefresh;
	private Action actionSQLExport;
	private int testCaseSelectionSignature;
	private Image imgNotSyncWarning; 
	private CLabel lblWarningMessage;
	private Text txtDatabaseInfo;
	
	public SurgicalQueryView() 
	{
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		GridLayout layout = new GridLayout(GRID_LAYOUT_COLUMNS, false);
	    parent.setLayout(layout);
	    
		dm = new DataManager();
		getViewSite().getPage().addSelectionListener(GlobalSymbols.VIEW_ID_NAVIGATOR, this);	

		imgNotSyncWarning = ResourceUtility.getImageDescriptor("1375812270_Warning.png").createImage();
		lblWarningMessage = new CLabel(parent, SWT.NONE);
		
		Group grpSearchParams = new Group(parent, SWT.NONE);
		grpSearchParams.setText("Display parameters");
		
	    Label lblMaxRows = new Label(grpSearchParams, SWT.NONE);
	    lblMaxRows.setText("Number of displayed rows: ");
	    txtMaxRows = new Text(grpSearchParams, SWT.BORDER);
	    txtMaxRows.setText(String.valueOf(DEFAULT_ROW_COUNT));
	    txtMaxRows.addListener (SWT.Verify, new Listener () {
			public void handleEvent (Event e) {
				String string = e.text;
				char [] chars = new char [string.length ()];
				string.getChars (0, chars.length, chars, 0);
				for (int i=0; i<chars.length; i++) {
					if (!('0' <= chars [i] && chars [i] <= '9')) {
						e.doit = false;
						return;
					}
				}
			}
		});

	    chkPickRandom = new Button(grpSearchParams, SWT.CHECK);
	    chkPickRandom.setText("Pick rows randomly");
	    
		Group grpInfo = new Group(parent, SWT.NONE);
		grpInfo.setText("Connected Database Info");
		txtDatabaseInfo = new Text(grpInfo, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		
		Label lblTestSelection = new Label(parent, SWT.NONE);
	    lblTestSelection.setText("Test cases selection:");

		treeTestcases = new Tree(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		treeTestcases.setSize(290, 100);

		treeTestcases.addListener (SWT.Selection, new Listener () {
			public void handleEvent (Event event) {
	            if (event.detail == SWT.CHECK) 
	            {
	                TreeItem item = (TreeItem) event.item;
	                boolean checked = item.getChecked();
	                checkItems(item, checked);
	                checkPath(item.getParentItem(), checked, false);
	            }
	            refreshCurrentSelection();
			}
		});

		txtSelectionSummary = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);

	    viewer = new TableViewer(parent, SWT.VIRTUAL | SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.addListener(SWT.SetData, new Listener() {
			@Override
			public void handleEvent(Event event) 
			{
				TableItem item = (TableItem) event.item;
				int index = event.index;
				int page = index / PAGE_SIZE;
				int start = page * PAGE_SIZE;
				int end = start + PAGE_SIZE;
				end = Math.min(end, table.getItemCount());
				for (int i = start; i < end; i++)
				{
					item = table.getItem(i);
					item.setText(0, String.valueOf(i + 1));
					try 
					{
						int k = i;
						if (chkPickRandom.getSelection())
							k = (int)(Math.random() * (dm.getRowCount()));
						
						ResultSet rs = dm.getRow(k + 1); // Resultset absolute positioning is 1-based
						if (rs != null)
						{
							for (int j = 1; j <= dm.getColumnCount(); ++j)
								item.setText(j, rs.getString(j) != null ? rs.getString(j) : "");
						}
					} 
					catch (SQLException e) 
					{
						e.printStackTrace();
					}
				}
			}
		});

		// Layout the viewer
		GridData gridData = null;

		gridData = new GridData();
		//gridData.heightHint = 32;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = GRID_LAYOUT_COLUMNS;
		//gridData.grabExcessVerticalSpace = true;
		lblWarningMessage.setLayoutData(gridData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		grpSearchParams.setLayout(gridLayout);

		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.heightHint = 50;
		gridData.horizontalSpan = 2; //GRID_LAYOUT_COLUMNS;
		grpSearchParams.setLayoutData(gridData);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		grpInfo.setLayout(gridLayout);
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		//gridData.heightHint = 150;
		gridData.minimumHeight = 75;
		gridData.horizontalSpan = 1; //GRID_LAYOUT_COLUMNS;
		grpInfo.setLayoutData(gridData);

		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		txtDatabaseInfo.setLayoutData(gridData);
		
		gridData = new GridData();
		gridData.widthHint = 75;
		txtMaxRows.setLayoutData(gridData);

		gridData = new GridData();
		gridData.horizontalSpan = 2;
		chkPickRandom.setLayoutData(gridData);
		
		gridData = new GridData();
		gridData.horizontalSpan = GRID_LAYOUT_COLUMNS;
		lblTestSelection.setLayoutData(gridData);
		
		//
		// Positioning tree
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.widthHint = 200;
		gridData.heightHint = 75;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		//gridData.grabExcessHorizontalSpace = true;
		//gridData.grabExcessVerticalSpace = true;
		treeTestcases.setLayoutData(gridData);

		gridData = new GridData();
		gridData.heightHint = 75;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		//gridData.grabExcessHorizontalSpace = true;
		//gridData.grabExcessVerticalSpace = true;
		txtSelectionSummary.setLayoutData(gridData);

		//
		// Positioning table
		gridData = new GridData();
		gridData.heightHint = 250;
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = GRID_LAYOUT_COLUMNS;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		//gridData.minimumHeight = 150;
		
		viewer.getControl().setLayoutData(gridData);

		createActions();
		createToolbar();
		try 
		{
			setContentDescription(String.format(VIEW_CAPTION, dm.getRowCount()));
		} 
		catch (SQLException e1) 
		{
			setContentDescription(String.format(VIEW_CAPTION, 0));
			e1.printStackTrace();
		}

		List<CteTestCase> testCases = getCurrentSelection();
		testCaseSelectionSignature = testCases.hashCode(); 
	}	
	
    private void createToolbar() 
    {
    	IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
    	tm.add(actionRefresh);
    	tm.add(actionSQLExport);
	}

	private void createActions() 
	{
		actionRefresh = new Action("Dataset refresh") {
			public void run() {
				List<CteTestCase> tcs = getCurrentSelection(); 
				refreshView(tcs);
				testCaseSelectionSignature = tcs.hashCode();
				clearChangesStatus();
			}
		};
		actionRefresh.setImageDescriptor(ResourceUtility.getImageDescriptor("1375808581_database_refresh.png"));
		//Shell shell = this.getViewSite().getShell();
		actionSQLExport = new Action("Export current test case selection to a SQL file") {
			public void run() {
				FileDialog dialog = new FileDialog(getViewSite().getShell(), SWT.SAVE);
				dialog.setFilterExtensions(new String[] {"*.sql", "*.*"});
				dialog.setFilterNames(new String[] {"SQL file", "All Files"});
				String fileSelected = dialog.open();
				if (fileSelected != null) 
					exportSQLToFile(fileSelected);
			}
		};
		actionSQLExport.setImageDescriptor(ResourceUtility.getImageDescriptor("1375879317_table_export.png"));
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionEnablement();
			}
		});
    }	

	private void updateActionEnablement() 
	{
		//IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
		//deleteItemAction.setEnabled(sel.size() > 0);
	}

	private void checkPath(TreeItem item, boolean checked, boolean grayed) 
	{
	    if (item == null) 
	    	return;
	    
	    if (grayed) 
	    {
	        checked = true;
	    } 
	    else 
	    {
	        int index = 0;
	        TreeItem[] items = item.getItems();
	        while (index < items.length) 
	        {
	            TreeItem child = items[index];
	            if (child.getGrayed() || checked != child.getChecked()) 
	            {
	                checked = grayed = true;
	                break;
	            }
	            index++;
	        }
	    }
	    item.setChecked(checked);
	    item.setGrayed(grayed);
	    checkPath(item.getParentItem(), checked, grayed);
	}

	private void checkItems(TreeItem item, boolean checked) 
	{
	    item.setGrayed(false);
	    item.setChecked(checked);
	    TreeItem[] items = item.getItems();
	
	    for (int i = 0; i < items.length; i++) 
	    {
	        checkItems(items[i], checked);
	    }
	}
	
	private void clearChangesStatus()
	{
		lblWarningMessage.setImage(null);
		lblWarningMessage.setText("");
	}
	
	private void refreshCurrentSelection()
	{
		txtSelectionSummary.setText("");
		
		List<CteTestCase> testCases = getCurrentSelection();
		if (testCases.hashCode() != testCaseSelectionSignature)
		{
			lblWarningMessage.setImage(imgNotSyncWarning);
			lblWarningMessage.setText("Click 'Refresh' button in order to update the data set with the new test case selection");
		}
		else
			clearChangesStatus();
		
		StringBuffer sb = new StringBuffer();
		CteModelManager ctemh = new CteModelManager();
		for (CteTestCase tc : testCases)
		{
			if (sb.length() > 0)
				sb.append(" OR \n");
			sb.append(ctemh.getReadableCteExpressionFromTestcase(lastSelectedCteobject, tc));
		}
		txtSelectionSummary.setText(sb.toString());
	}
	
	private List<CteTestCase> getCurrentSelection()
	{
		List<CteTestCase> testCases = new ArrayList<CteTestCase>();
		TreeItem[] tis = treeTestcases.getItems();
		
		getCurrentSelection(testCases, tis);
		return testCases;
	}
	
	private void getCurrentSelection(List<CteTestCase> testCases, TreeItem[] tis)
	{
		if (testCases == null)
		{
			testCases = new ArrayList<CteTestCase>();
			tis = treeTestcases.getItems();
		}
		
		for (TreeItem ti : tis)
		{
			if (ti.getChecked() || ti.getGrayed())
			{
				Object o = ti.getData();
				if (o != null)
				{
					if (o instanceof CteTestCase)
						testCases.add((CteTestCase) o);
					else if (o instanceof CteTestGroup)
						getCurrentSelection(testCases, ti.getItems());
				}
				else
					getCurrentSelection(testCases, ti.getItems());
			}
		}
	}

	private void populateFilter(CteObject cteo)
	{
		treeTestcases.removeAll();
		txtSelectionSummary.setText("");

		CteTestGroup tg = cteo.getTestGroup();
		TreeItem root = new TreeItem(treeTestcases, 0);
		root.setText(TREE_ITEM_ROOT);
		populateFilter(root, tg);
	}

	private void populateFilter(TreeItem parent, CteTestGroup tg)
	{
		if (tg == null)
			return;
		
		if (tg.getTestGroups() != null)
		{
			for (CteTestGroup tgi : tg.getTestGroups())
			{
				TreeItem ti = new TreeItem(parent, 0);
				ti.setText(tgi.getName());
				ti.setData(tgi);
				populateFilter(ti, tgi);
			}
		}
		
		if (tg.getTestCases() != null)
		{
			for (CteTestCase tc : tg.getTestCases())
			{
				TreeItem tj = new TreeItem(parent, 0);
				tj.setText(tc.getName());
				tj.setData(tc);
			}
		}	
	}
	
	@Override
	public void setFocus() 
	{
	}
	
	@Override
	public void dispose() 
	{
		super.dispose();
		try 
		{
			dm.close();
			imgNotSyncWarning.dispose();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
		getViewSite().getPage().removeSelectionListener(GlobalSymbols.VIEW_ID_NAVIGATOR, this);
	}
	
	/*
	private void createViewer() throws SQLException 
	{
		Table table = viewer.getTable();
		table.removeAll();
		createColumns(table);

		int n = Math.min(dm.getRowCount(), Integer.valueOf(txtMaxRows.getText()));
		table.setItemCount(n);
		table.setRedraw(true);
		table.redraw();
		
		txtDatabaseInfo.setText(dm.getDatabaseInfo().toString());
	}
	
	private void createColumns(Table table) throws SQLException 
	{
		table.setRedraw(false);
		while (table.getColumnCount() > 0) 
		    table.getColumns()[0].dispose();
		
		TableColumn column = new TableColumn(table, SWT.LEFT);
		column.setText("#");
		column.setWidth(50);
		
		for (String h : dm.getColumnNames())
		{
			column = new TableColumn(table, SWT.LEFT);
			column.setText(h);
			column.setWidth(100);
		}
	}
	*/

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{
		System.out.println("SurgicalQueryView.selectionChanged: " + selection);
		IResource r = ProjectUtility.toResource((IStructuredSelection) selection);
		clearChangesStatus();
		
		if (r == null)
			return;

		if (!ResourceUtility.isFile(r))
			return;
		
		if (!r.getFileExtension().equals(GlobalSymbols.FILE_EXT_TESTONA))
			return;

		CteModelManager ctemh = new CteModelManager();
		IFile f = DepictProjectSupport.getGraphModelFile((IFile) r);
		CteObject cteo = ctemh.toCteObject(ResourceUtility.toFile(r), ResourceUtility.toFile(f));
		lastSelectedCteobject = cteo;
		refreshView(null);
		populateFilter(lastSelectedCteobject);
	}
	
	private void refreshView(List<CteTestCase> selectedTestCases)
	{
		/*
		if (lastSelectedCteobject == null)
			return;
		
		CteModelHelper ctemh = new CteModelHelper();

		try 
		{
			String sql = ctemh.toSQL(lastSelectedCteobject, selectedTestCases);
			IStatus status = new Status(IStatus.INFO, Activator.PLUGIN_ID, String.format("SQL>%s", sql), null);
			Activator.getDefault().getLog().log(status);

			CteConnectionInfo ci = ctemh.toConnectionInfo(lastSelectedCteobject);
			if (dm.isOpened())
				dm.close();
			
			dm.setup(ci);
			dm.open();
			dm.execute(sql);
			createViewer();
			
			setContentDescription(String.format(VIEW_CAPTION, dm.getRowCount()));
		} 
		catch (Exception e) 
		{
			lastSelectedCteobject = null;
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(status);
		}
		*/
	}
	
	private void exportSQLToFile(String fileName)
	{
		/*
		if (lastSelectedCteobject == null)
			return;
		
		try 
		{
			Path path = FileSystems.getDefault().getPath(fileName);
			StringBuffer sb = new StringBuffer();

			sb.append("-- *****************************************\n");
			sb.append(String.format("-- Model name:%s\n", lastSelectedCteobject.getModelName()));

			String dbInfo = dm.getDatabaseInfo().toString();
			String[] infos = dbInfo.split("\n");
			sb.append("-- *****************************************\n");
			sb.append("-- Depict environment database informations:\n");
			sb.append("-- *****************************************");

			for (String s : infos)
			{
				if (sb.length() > 0)
					sb.append("\n");
				sb.append("-- " + s);
			}
			
			CteModelHelper ctemh = new CteModelHelper();
			List<CteTestCase> testCases = getCurrentSelection();
			if (testCases.size() > 0)
			{
				sb.append("\n\n\n-- *****************************************\n");
				sb.append("-- Test cases selection:\n");
				sb.append("-- *****************************************");
			}
			
			for (CteTestCase tc : testCases)
			{
				if (sb.length() > 0)
					sb.append("\n");
				sb.append("-- " + ctemh.getReadableCteExpressionFromTestcase(lastSelectedCteobject, tc));
			}
			
			String sql = ctemh.toSQL(lastSelectedCteobject, testCases);
			if (sb.length() > 0)
				sb.append("\n");
			sb.append(sql);
			Files.deleteIfExists(path);
			Files.write(path, sb.toString().getBytes(), StandardOpenOption.CREATE);
		} 
		catch (Exception e) 
		{
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(status);
		}
	*/
	}
}
