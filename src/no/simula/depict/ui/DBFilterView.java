package no.simula.depict.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import no.simula.depict.Activator;
import no.simula.depict.GlobalSymbols;
import no.simula.depict.data.TableColumnMetadata;
import no.simula.depict.data.TableMetadata;
import no.simula.depict.manager.CteModelManager;
import no.simula.depict.manager.TestCaseInteractionManager;
import no.simula.depict.model.Cargo;
import no.simula.depict.model.CteObject;
import no.simula.depict.model.TestCaseInteraction;
import no.simula.depict.project.DepictProjectSupport;
import no.simula.rcp.utility.ProjectUtility;
import no.simula.rcp.utility.ResourceUtility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class DBFilterView extends ViewPart implements ISelectionListener
{
	private static final String NO_ACTIVE_FILTER = "No active filter";
	private static final String ACTIVE_FILTER = "Data filtered: %s";
	
	private enum OPERATORS
	{
		EQU("==", "%s = %s", new boolean [] {true, false}),
		NEQ("!=", "%s <> %s", new boolean [] {true, false}),
		LT("<", "%s < %s", new boolean [] {true, false}),
		LTE("<=", "%s <= %s", new boolean [] {true, false}),
		GT(">", "%s > %s", new boolean [] {true, false}),
		GTE(">=", "%s >= %s", new boolean [] {true, false}),
		RNG("RANGE", "%s BETWEEN %s AND %s", new boolean [] {true, true});
		
		private String symbol;
		private String sqlOperatorFormat;
		private boolean enables[] = {false, false};
		private int valuesCount;
		
		OPERATORS(String symbol, String sqlOperatorFormat, boolean enables[])
		{
			this.symbol = symbol;
			this.sqlOperatorFormat = sqlOperatorFormat;
			this.enables = enables;
			valuesCount = 0;
			for (boolean b : enables)
				if (b)
					++valuesCount;
		}
		
		public String getSymbol() {
			return symbol;
		}
		
		public boolean [] getEnables()	{
			return enables;
		}

		public String getSqlOperatorFormat() {
			return sqlOperatorFormat;
		}

		public int getValuesCount() {
			return valuesCount;
		}
	}
	
	private static final int GRID_LAYOUT_COLUMNS = 3;
	private static final String NONE_TABLE_SELECTED = "Select a table...";
	private static final String NONE_FIELD_SELECTED = "Select a field...";
	private static final String NONE_OP_SELECTED = "Select an operator...";
	private static final String SQL_DATA_TYPE_FMT = "[%s]";
	
	private static final int NONE_SELECTED_IDX = 0;

	private CLabel lblFilterStatus;
	private Image imgWarning;
	private Combo cboTables;
	private Combo cboFields;
	private Combo cboOperators;
	private Label lblDatatype;
	private Text txtValue1;
	private Text txtValue2;
	private Button cmdApply;
	private List<TableMetadata> tables;
	
	public DBFilterView() 
	{
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		getViewSite().getPage().addSelectionListener(GlobalSymbols.VIEW_ID_NAVIGATOR, this);	
		createView(parent);
		initCombos();
	}

	@Override
	public void setFocus() 
	{
	}

	@Override
	public void dispose() 
	{
		if (imgWarning != null)
			imgWarning.dispose();
		
		super.dispose();
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
			List<TestCaseInteraction> tcis = c.getTestCaseInteractions();
			tables = c.getTables();
			
			TestCaseInteractionManager tcim = new TestCaseInteractionManager();
			List<String> tables = tcim.getTableIntersection(tcis);
		    lblFilterStatus.setText(NO_ACTIVE_FILTER);
			populateTableList(tables);
			populateFieldList(NONE_TABLE_SELECTED);
			populateOperatorList();
			onFieldChanged();
		} 
		catch (ClassNotFoundException | IOException e) 
		{
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(status);
		}
	}
	
	private void createView(Composite parent)
	{
		GridLayout gl = new GridLayout(GRID_LAYOUT_COLUMNS, false);
	    parent.setLayout(gl);

	    imgWarning = ResourceUtility.getImageDescriptor("1375812270_Warning.png").createImage();

	    lblFilterStatus = new CLabel(parent, SWT.NULL);
	    lblFilterStatus.setText(NO_ACTIVE_FILTER);
	    
		Label lblTables = new Label(parent, SWT.NULL);
		lblTables.setText("Table:");
		cboTables = new Combo(parent, SWT.READ_ONLY);
		cboTables.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onFieldChanged();
				populateFieldList(cboTables.getText());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		Label lblFields = new Label(parent, SWT.NULL);
		lblFields.setText("Field:");
		cboFields = new Combo(parent, SWT.READ_ONLY);
		cboFields.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onFieldChanged();
				if (cboFields.getSelectionIndex() != NONE_SELECTED_IDX)
				{
					TableColumnMetadata c = getTableColumnMetadata(cboTables.getText(), cboFields.getText());
					lblDatatype.setText(String.format(SQL_DATA_TYPE_FMT, c.getSqlTypeName()));
				}
				else
					lblDatatype.setText("");
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		lblDatatype = new Label(parent, SWT.NULL);
		lblDatatype.setText("");
		
		Label lblOps = new Label(parent, SWT.NULL);
		lblOps.setText("Operator:");
		cboOperators = new Combo(parent, SWT.READ_ONLY);
		cboOperators.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				OPERATORS op = getSelectedOperator();
				if (op != null)
				{
					txtValue1.setEnabled(op.getEnables()[0]);
					txtValue2.setEnabled(op.getEnables()[1]);
					return;
				}
				else
				{
					txtValue1.setEnabled(false);
					txtValue2.setEnabled(false);
				}
				onFieldChanged();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Label lblValues = new Label(parent, SWT.NULL);
		lblValues.setText("Value(s):");

		txtValue1 = new Text(parent, SWT.BORDER);
		txtValue1.setEnabled(false);
		txtValue1.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				onFieldChanged();
			}
		});

		txtValue2 = new Text(parent, SWT.BORDER);
		txtValue2.setEnabled(false);
		txtValue2.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				onFieldChanged();
			}
		});

		cmdApply = new Button(parent, SWT.PUSH);
		cmdApply.setText("Apply");
		cmdApply.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String f = createFilter();
				InteractionCoveragePublisher.getInstance().setFilterExpression(f);
			    lblFilterStatus.setText(String.format(ACTIVE_FILTER, f));
			}			
		});
		cmdApply.setEnabled(false);
		
		Button cmdReset = new Button(parent, SWT.PUSH);
		cmdReset.setText("Reset");
		cmdReset.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				InteractionCoveragePublisher.getInstance().setFilterExpression(null);
			    lblFilterStatus.setText(NO_ACTIVE_FILTER);
			}			
		});

		// Layout the viewer
		GridData gd = null;

		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.widthHint = 250;
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;
		lblFilterStatus.setLayoutData(gd);
		
		gd = new GridData();
		gd.widthHint = 100;
		gd.horizontalSpan = 2;
		cboTables.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 100;
		cboFields.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 100;
		lblDatatype.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 100;
		gd.horizontalSpan = 2;
		cboOperators.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 75;
		txtValue1.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 75;
		txtValue2.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 75;
		cmdApply.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 75;
		gd.horizontalSpan = 2;
		cmdReset.setLayoutData(gd);
	}
	
	private OPERATORS getSelectedOperator()
	{
		String s = cboOperators.getText();
		for (OPERATORS op : OPERATORS.values())
		{
			if (op.getSymbol().equals(s))
			{
				return op;
			}	
		}
		
		return null;
	}
	
	private String createFilter()
	{
		OPERATORS op = getSelectedOperator();
		String t = cboTables.getText();
		String c = cboFields.getText();
		TableColumnMetadata cmd = getTableColumnMetadata(t, c);
		
		String field = t + "." + c;
		
		String filter = "";
		if (op.getValuesCount() == 2)
		{
			filter = String.format(op.getSqlOperatorFormat(), field, encapsulateValue(txtValue1.getText(), cmd), encapsulateValue(txtValue2.getText(), cmd));
		}
		else
			filter = String.format(op.getSqlOperatorFormat(), field, encapsulateValue(txtValue1.getText(), cmd));

		return filter;
	}
	
	private String encapsulateValue(String value, TableColumnMetadata c)
	{
		String r = "";
		
		switch (c.getSqlDataType())
		{
			case java.sql.Types.CHAR: 
			case java.sql.Types.VARCHAR: 
			r = "'" + value + "'";
			break;
	
			default:
			r = value; 
			break;
		}
		
		return r;
	}
	
	private void onFieldChanged()
	{
		boolean b = true;
				
		if (cboTables.getSelectionIndex() != NONE_SELECTED_IDX)
			b &= true;
		else
			b = false;
		
		if (cboFields.getSelectionIndex() != NONE_SELECTED_IDX)
			b &= true;
		else
			b = false;

		if (cboOperators.getSelectionIndex() != NONE_SELECTED_IDX)
			b &= true;
		else
			b = false;

		if (txtValue1.isEnabled())
			if (txtValue1.getText().length() != 0)
				b &= true;
			else
				b = false;
		
		if (txtValue2.isEnabled())
			if (txtValue2.getText().length() != 0)
				b &= true;
			else
				b = false;
		
		cmdApply.setEnabled(b);
	}

	private void populateTableList(List<String> tables)
	{
		cboTables.removeAll();
		
		Arrays.sort(tables.toArray());
		cboTables.add(NONE_TABLE_SELECTED);
		for (String s : tables)
			cboTables.add(s);
		
		cboTables.select(NONE_SELECTED_IDX);
		if (cboTables.getItemCount() == 1)
		{
			lblFilterStatus.setText("None suitable table for filter found");
			lblFilterStatus.setImage(imgWarning);
		}
		else
		{
			lblFilterStatus.setText("");
			lblFilterStatus.setImage(null);
		}
	}

	private void populateOperatorList()
	{
		cboOperators.removeAll();
		cboOperators.add(NONE_OP_SELECTED);
		txtValue1.setEnabled(false);
		txtValue2.setEnabled(false);
		
		for (OPERATORS o : OPERATORS.values())
			cboOperators.add(o.getSymbol());
		
		cboOperators.select(NONE_SELECTED_IDX);
	}

	private void populateFieldList(String tableName)
	{
		lblDatatype.setText("");
		cboFields.removeAll();
		cboFields.add(NONE_FIELD_SELECTED);
		
		TableMetadata t = getTableMetadata(tableName);
		if (t == null)
		{
			cboFields.select(NONE_SELECTED_IDX);
			return;
		}
		
		Collections.sort(t.getColumns(), new TableColumnMetadataNameComparator());
		
		for (TableColumnMetadata c : t.getColumns())
			cboFields.add(c.getName());
		
		cboFields.select(NONE_SELECTED_IDX);
	}
	
	private TableMetadata getTableMetadata(String name)
	{
		for (TableMetadata t : tables)
		{
			if (t.getName().equals(name))
				return t;
		}
		
		return null;
	}
	
	private TableColumnMetadata getTableColumnMetadata(String tableName, String columnName)
	{
		TableMetadata t = getTableMetadata(tableName);

		for (TableColumnMetadata c : t.getColumns())
		{
			if (c.getName().equals(columnName))
				return c;
		}
		
		return null;
	}

	private void initCombos()
	{
		cboTables.add(NONE_TABLE_SELECTED);
		cboTables.select(NONE_SELECTED_IDX);
		cboFields.add(NONE_FIELD_SELECTED);
		cboFields.select(NONE_SELECTED_IDX);
		cboOperators.add(NONE_OP_SELECTED);
		cboOperators.select(NONE_SELECTED_IDX);
	}
	
	private class TableColumnMetadataNameComparator implements Comparator<TableColumnMetadata> 
	{
	    public int compare(TableColumnMetadata c1, TableColumnMetadata c2) 
	    {
	        return c1.getName().compareTo(c2.getName());
	    }
	}
}
