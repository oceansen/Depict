package no.simula.depict.wizards;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import no.simula.depict.Activator;
import no.simula.depict.GlobalSymbols;
import no.simula.depict.data.CteConnectionInfo;
import no.simula.depict.data.TableMetadata;
import no.simula.depict.manager.CteModelManager;
import no.simula.depict.manager.EntityRelationshipModelManager;
import no.simula.depict.model.Cargo;
import no.simula.depict.model.CteObject;
import no.simula.depict.model.CteTestCase;
import no.simula.depict.model.TestCaseInteraction;
import no.simula.depict.project.DepictProjectSupport;
import no.simula.rcp.utility.ProjectUtility;
import no.simula.rcp.utility.ResourceUtility;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class ImportCTEModelWizard extends Wizard implements IImportWizard 
{
	public static final String WIZPAGE_TITLE = "Data Interaction Model Import Wizard";
	private IProject selectedProject;
	private ModelImportCargo modelImportCargo;
	private CteConnectionInfo preloadedConnectionInfo;
	
	ImportCTEModelPage importModelPage;
	ImportCTEModelDatabaseSettingsPage dbSettingsPage;
	CreateDbRelationshipsPage dbRelationshipsPage;
	
	public ImportCTEModelWizard() 
	{
		super();
		modelImportCargo = new ModelImportCargo();
		setNeedsProgressMonitor(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) 
	{
		selectedProject = ProjectUtility.toProject(selection);
        if (selectedProject == null)
        {
			MessageDialog.openError(Display.getDefault().getActiveShell(), 
					WIZPAGE_TITLE, 
					"Import failed!\nCause: You have to select a valid project before starting the import procedure");
			return;
        }	
        
		IResource r = ProjectUtility.toResource((IStructuredSelection) selection);
		if (ResourceUtility.isFile(r) && r.getFileExtension().equals(GlobalSymbols.FILE_EXT_TESTONA))
		{
			CteModelManager ctemh = new CteModelManager();
			IFile f = DepictProjectSupport.getGraphModelFile((IFile) r);
			CteObject cteo = ctemh.toCteObject(ResourceUtility.toFile(r), ResourceUtility.toFile(f));
			preloadedConnectionInfo = ctemh.toConnectionInfo(cteo);
		}

		setWindowTitle(WIZPAGE_TITLE); //NON-NLS-1
		setNeedsProgressMonitor(true);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() 
    {
        super.addPages(); 
        
		importModelPage = new ImportCTEModelPage("Import TESTONA File", selectedProject, modelImportCargo); //NON-NLS-1
        addPage(importModelPage);        
        
		dbSettingsPage = new ImportCTEModelDatabaseSettingsPage("Database settings", modelImportCargo, preloadedConnectionInfo);
        addPage(dbSettingsPage);

        dbRelationshipsPage = new CreateDbRelationshipsPage("Select Database relationships", selectedProject, modelImportCargo);
        addPage(dbRelationshipsPage);
    }

    /* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() 
	{
		IProject project = importModelPage.getSelectedProject();
		final IFolder fldCteModels = project.getFolder(DepictProjectSupport.IMPORTED_MODELS_FOLDER_NAME);
		final IFolder fldGraphs = project.getFolder(DepictProjectSupport.GRAPH_MODELS_FOLDER_NAME);

		//
		// Does model already exist?
		final File cteModel = new File(importModelPage.getCTEModelFilePath());
		IResource res = ProjectUtility.findResource(selectedProject, DepictProjectSupport.IMPORTED_MODELS_FOLDER_NAME + "/" + cteModel.getName());
		if (res != null)
		{
			if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(), WIZPAGE_TITLE, MessageFormat.format("The TESTONA model {0} already exists.\nDo you want to replace it?", cteModel.getName())))
			try 
			{
				res.delete(true, null);
			} 
			catch (CoreException e) 
			{
				IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
				Activator.getDefault().getLog().log(status);
			}
			else
				return false;
		}
		
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());    
		
		try 
		{
			dialog.run(false, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
				{
					try 
					{
						monitor.beginTask("Translating TESTONA model to E-R graph. Please wait...", 1);
						CteModelManager ccte = new CteModelManager();
						
						CteObject cteo = ccte.toCteObject(cteModel, null);
						ccte.setConnectionInfo(cteo, dbSettingsPage.getCteConnectionInfo());
						CteModelManager ctemh = new CteModelManager();
						List<CteTestCase> testCases = ctemh.getAllTestCases(cteo);
						List<TestCaseInteraction> tcis = ctemh.toTestcaseInteraction(cteo, testCases);

						EntityRelationshipModelManager ermh = new EntityRelationshipModelManager(cteo, dbSettingsPage.getCteConnectionInfo());
						Cargo c = ermh.buildModel(tcis, dbRelationshipsPage.getSubGraph(), modelImportCargo.getTables());
						
						IFile copiedFile = ProjectUtility.copyFile(fldCteModels, importModelPage.getCTEModelFilePath());
						ccte.persist(ResourceUtility.toFile(copiedFile), cteo, c, fldGraphs.getLocation());
						monitor.worked(1);
						MessageDialog.openInformation(Display.getDefault().getActiveShell(), 
								WIZPAGE_TITLE, 
								"TESTONA File Import successfully completed!");
					} 
					catch (Exception e) 
					{
						IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e);
						Activator.getDefault().getLog().log(status);
					
						MessageDialog.openError(Display.getDefault().getActiveShell(), 
								WIZPAGE_TITLE, 
								String.format("Import failed!\nCause:%s\nSee error log for more information.", e.getMessage()));
						e.printStackTrace();
					}
					finally
					{
						monitor.done();
					}
				}
			});
		} 
		catch (InvocationTargetException | InterruptedException e1) 
		{
			IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e1.getMessage(), e1);
			Activator.getDefault().getLog().log(status);
			e1.printStackTrace();
		}
        return true;
	}
	
	public CreateDbRelationshipsPage getCreateDbRelationshipsPage()
	{
		return dbRelationshipsPage;
	}
	
	public class ModelImportCargo
	{
		private CteObject cteObject;
		private CteConnectionInfo connectionInfo;
		private List<TableMetadata> tables;
		
		public CteObject getCteObject() {
			return cteObject;
		}

		public void setCteObject(CteObject cteObject) {
			this.cteObject = cteObject;
		}

		public CteConnectionInfo getConnectionInfo() {
			return connectionInfo;
		}

		public void setConnectionInfo(CteConnectionInfo connectionInfo) {
			this.connectionInfo = connectionInfo;
		}

		public List<TableMetadata> getTables() {
			if (tables == null)
				tables = new ArrayList<TableMetadata>();
			
			return tables;
		}
	}
}
