package no.simula.depict.ui;

import java.sql.SQLException;

import no.simula.depict.Activator;
import no.simula.depict.GlobalSymbols;
import no.simula.depict.data.CteConnectionInfo;
import no.simula.depict.data.DataManager;
import no.simula.depict.manager.CteModelManager;
import no.simula.depict.model.CteObject;
import no.simula.depict.project.DepictProjectSupport;
import no.simula.rcp.utility.ProjectUtility;
import no.simula.rcp.utility.ResourceUtility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class DBInformationView extends ViewPart implements ISelectionListener
{
	private static final int GRID_LAYOUT_COLUMNS = 1;
	private Text txtDatabaseInfo;

	public DBInformationView() {
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		getViewSite().getPage().addSelectionListener(GlobalSymbols.VIEW_ID_NAVIGATOR, this);	
		createView(parent);
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() 
	{
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
		DataManager dm = new DataManager();
		CteConnectionInfo ci = ctemh.toConnectionInfo(cteo);
		try 
		{
			dm.setup(ci);
			dm.open();
			txtDatabaseInfo.setText(dm.getDatabaseInfo().toString());
			dm.close();
		} 
		catch (SQLException | ClassNotFoundException | InstantiationException | IllegalAccessException e) 
		{
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "DB connection", 
					String.format("The following error occured while trying to connect to the database:\n%s\nPlease look into the log for more details.", e.getMessage()));
					
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
			Activator.getDefault().getLog().log(status);
		}
	}
	
	private void createView(Composite parent)
	{
		GridLayout gl = new GridLayout(GRID_LAYOUT_COLUMNS, false);
	    parent.setLayout(gl);
		
		txtDatabaseInfo = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.READ_ONLY);
		
		// Layout the viewer
		GridData gd = null;
		
		gd = new GridData(GridData.FILL, GridData.FILL, true, true);
		txtDatabaseInfo.setLayoutData(gd);
	}
}
