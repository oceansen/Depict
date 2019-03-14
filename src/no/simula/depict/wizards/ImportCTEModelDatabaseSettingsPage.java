package no.simula.depict.wizards;

import no.simula.depict.Activator;
import no.simula.depict.GlobalSymbols.JDBC_DRIVER_CLASS;
import no.simula.depict.data.CteConnectionInfo;
import no.simula.depict.data.DataManager;
import no.simula.depict.wizards.ImportCTEModelWizard.ModelImportCargo;
import no.simula.rcp.utility.ResourceUtility;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImportCTEModelDatabaseSettingsPage extends WizardPage 
{
	private static final int GRID_LAYOUT_COLUMNS = 2;
	private static final String NONE_DRIVER_SELECTED = "Select a driver...";
	private static final int NONE_DRIVER_SELECTED_IDX = 0;
	
	private Combo cboDriverClass;
	private Text txtUserid;
	private Text txtUserPassword;
	private Text txtDbServer;
	private Text txtDbPort;
	private Text txtDbName;
	private Button cmdPing;
	private ModelImportCargo modelImportCargo;
	private boolean enterPageResult;
	private CteConnectionInfo ci;
	
	protected ImportCTEModelDatabaseSettingsPage(String pageName, ModelImportCargo modelImportCargo, CteConnectionInfo ci) 
	{
		super(pageName);
		setTitle("Define the database connection settings");
		setDescription("Fill out the form and click on 'Ping server' to get to the next step");
		this.ci = ci;
		
		this.modelImportCargo = modelImportCargo;
	}

	@Override
	public void createControl(Composite parent) 
	{
		createView(parent);
		populateDriverList();

		if (ci != null)
		{
			JDBC_DRIVER_CLASS jdbc = JDBC_DRIVER_CLASS.getFromDriverName(ci.getDriverName());
			if (jdbc != null)
			{
				int i = cboDriverClass.indexOf(jdbc.getName());
				cboDriverClass.select(i);
			}
			
			txtUserid.setText(ci.getUser());
			txtUserPassword.setText(ci.getPassword());
			txtDbPort.setText(ci.getPort());
			txtDbName.setText(ci.getDbName());
			txtDbServer.setText(ci.getServer());
			onFieldChanged();
		}

		//
		//XXX Comment in production
		//@@begin
		/*
		cboDriverClass.select(0);
		txtUserid.setText("postgres");
		txtUserPassword.setText("postgres");
		txtDbName.setText("depict");
		onFieldChanged();
		*/
		//@@end
	}

	@Override
	public IWizardPage getNextPage()
	{
		final ImportCTEModelWizard w = (ImportCTEModelWizard) getWizard();
		enterPageResult = false;
		
		BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			public void run() {
				try 
				{
					w.getCreateDbRelationshipsPage().onEnterPage();
					enterPageResult = true;
				} 
				catch (Exception e) 
				{
					IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
					Activator.getDefault().getLog().log(status);
					MessageDialog.openError(Display.getDefault().getActiveShell(), 
							"DB validation error", 
							"Error: " + e.getMessage());
				}
			}});
		
		if (enterPageResult)
			return w.getCreateDbRelationshipsPage();
		
		return null;
	}	
	
	private void createView(Composite parent)
	{
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = GRID_LAYOUT_COLUMNS;
		
		Label lblDriverclass = new Label(container, SWT.NULL);
		lblDriverclass.setText("Driver class:");
		
		cboDriverClass = new Combo(container, SWT.READ_ONLY);
		cboDriverClass.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				onDriverSelected(cboDriverClass.getText());
				onFieldChanged();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Group grpAuth = new Group(container, SWT.NONE);
		grpAuth.setText("Authentication");
		GridLayout gl = new GridLayout();
		gl.numColumns = GRID_LAYOUT_COLUMNS;
		grpAuth.setLayout(gl);

		Label lblUserId = new Label(grpAuth, SWT.NULL);
		lblUserId.setText("Userid:");
		txtUserid = new Text(grpAuth, SWT.BORDER);
		txtUserid.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) 
			{
				onFieldChanged();
			}
		});

		Label lblUserPassword = new Label(grpAuth, SWT.NULL);
		lblUserPassword.setText("Password:");
		txtUserPassword = new Text(grpAuth, SWT.BORDER | SWT.PASSWORD);
		txtUserPassword.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) 
			{
				onFieldChanged();
			}
		});

		Group grpConnection = new Group(container, SWT.NONE);
		grpConnection.setText("Connection");
		gl = new GridLayout();
		gl.numColumns = GRID_LAYOUT_COLUMNS;
		grpConnection.setLayout(gl);

		Label lblDbServer = new Label(grpConnection, SWT.NULL);
		lblDbServer.setText("Database server:");
		txtDbServer = new Text(grpConnection, SWT.BORDER);
		txtDbServer.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) 
			{
				onFieldChanged();
			}
		});

		Label lblDbPort = new Label(grpConnection, SWT.NULL);
		lblDbPort.setText("Database port:");
		txtDbPort = new Text(grpConnection, SWT.BORDER);
		txtDbPort.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) 
			{
				onFieldChanged();
			}
		});

		Label lblDbName = new Label(grpConnection, SWT.NULL);
		lblDbName.setText("Database name:");
		txtDbName = new Text(grpConnection, SWT.BORDER);
		/*
		txtDbName.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				modelImportCargo.setConnectionInfo(getCteConnectionInfo());
			}
		});
		*/
		txtDbName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) 
			{
				onFieldChanged();
			}
		});
		
		cmdPing = new Button(container, SWT.PUSH);
		cmdPing.setText("Ping server");
		cmdPing.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent ev) {
				CteConnectionInfo cci = getCteConnectionInfo();
				if (cci != null)
				{
					//System.out.println(cci.toString());
					DataManager dm = new DataManager();
					try 
					{
						dm.ping(cci);
						modelImportCargo.setConnectionInfo(getCteConnectionInfo());
						
						ImportCTEModelWizard w = (ImportCTEModelWizard) getWizard();
						if (w.getCreateDbRelationshipsPage().validate())
						{
							setPageComplete(true);
							MessageDialog.openInformation(Display.getDefault().getActiveShell(), 
									"Ping database server", 
									"Model validation and database connection succeeded!\nNow you can proceed with the next step.");
						}
					} 
					catch (Exception e) 
					{
						IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
						Activator.getDefault().getLog().log(status);
						setPageComplete(false);
						MessageDialog.openError(Display.getDefault().getActiveShell(), 
								"Ping database server", 
								"Error: " + e.getMessage());
					}
				}
			}			
		});
		cmdPing.setImage(ResourceUtility.getImageDescriptor("1376588659_database_connect.png").createImage());
		//
		// Control's layout
		GridData gd = null;
		gd = new GridData();
		gd.widthHint = 150;
		cboDriverClass.setLayoutData(gd);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = GRID_LAYOUT_COLUMNS;
		grpAuth.setLayoutData(gd);
		
		gd = new GridData();
		gd.widthHint = 150;
		txtUserid.setLayoutData(gd);
		
		gd = new GridData();
		gd.widthHint = 150;
		txtUserPassword.setLayoutData(gd);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = GRID_LAYOUT_COLUMNS;
		grpConnection.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 150;
		txtDbServer.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 150;
		txtDbPort.setLayoutData(gd);

		gd = new GridData();
		gd.widthHint = 150;
		txtDbName.setLayoutData(gd);

		cmdPing.setEnabled(false);
		setControl(container);
		setPageComplete(false);		
	}

	private void onFieldChanged()
	{
		boolean b = true;
				
		if (cboDriverClass.getSelectionIndex() != NONE_DRIVER_SELECTED_IDX)
			b &= true;
		else
			b = false;
		
		if (txtUserid.getText().length() != 0)
			b &= true;
		else
			b = false;

		if (txtUserPassword.getText().length() != 0)
			b &= true;
		else
			b = false;

		if (txtDbServer.getText().length() != 0)
			b &= true;
		else
			b = false;

		if (txtDbPort.getText().length() != 0)
			b &= true;
		else
			b = false;

		if (txtDbName.getText().length() != 0)
			b &= true;
		else
			b = false;

		cmdPing.setEnabled(b);
	}
	
	private void populateDriverList()
	{
		cboDriverClass.add(NONE_DRIVER_SELECTED);
		for (JDBC_DRIVER_CLASS j : JDBC_DRIVER_CLASS.values())
			cboDriverClass.add(j.getName());
		
		cboDriverClass.select(NONE_DRIVER_SELECTED_IDX);
	}
	
	private void onDriverSelected(String driverName)
	{
		if (driverName.equals(NONE_DRIVER_SELECTED))
		{
			txtDbServer.setText("");
			txtDbPort.setText("");
		}
		else
		{
			JDBC_DRIVER_CLASS j = JDBC_DRIVER_CLASS.getFromName(driverName);
			if (txtDbServer.getText().length() == 0)
				txtDbServer.setText(j.getServer());
			
			if (txtDbPort.getText().length() == 0)
				txtDbPort.setText(j.getPort());
		}
	}
	
	public CteConnectionInfo getCteConnectionInfo()
	{
		String driverName = cboDriverClass.getText();
		if (!driverName.equals(NONE_DRIVER_SELECTED))
		{
			JDBC_DRIVER_CLASS j = JDBC_DRIVER_CLASS.getFromName(driverName);
			CteConnectionInfo cci = new CteConnectionInfo(j);
			cci.setUser(txtUserid.getText());
			cci.setPassword(txtUserPassword.getText());
			cci.setServer(txtDbServer.getText());
			cci.setPort(txtDbPort.getText());
			cci.setDbName(txtDbName.getText());
			cci.setConnectionStringTemplate(j.getConnectionStringTemplate());
			return cci;
		}
		
		return null;
	}
}
