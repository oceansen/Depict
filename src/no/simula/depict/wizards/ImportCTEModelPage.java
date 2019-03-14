package no.simula.depict.wizards;

import java.io.File;

import no.simula.depict.manager.CteModelManager;
import no.simula.depict.model.CteObject;
import no.simula.depict.wizards.ImportCTEModelWizard.ModelImportCargo;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImportCTEModelPage extends WizardPage 
{
	//private IStructuredSelection selection = null;
	private Text txtSelectedProject;
	private Text txtCTEModelFilePath;
	private Composite container;
	private IProject selectedProject;
	private ModelImportCargo modelImportCargo;
	
	public ImportCTEModelPage(String pageName, IProject selectedProject, ModelImportCargo modelImportCargo) 
	{
		super(pageName);
		//this.selection = selection;
		this.selectedProject = selectedProject;
		setTitle("Import a TESTONA model file into the selected project");
		this.modelImportCargo = modelImportCargo;
	}

	public void createControl(Composite parent) 
	{
		container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		
		Label lblProjectName = new Label(container, SWT.NULL);
		lblProjectName.setText("Selected project:");
		
		txtSelectedProject = new Text(container, SWT.BORDER | SWT.SINGLE);		    
        
        
		txtSelectedProject.setText(selectedProject.getName());
		txtSelectedProject.setEnabled(false);
		
	    Label desc = new Label(container, SWT.NULL);
	    desc.setText("TESTONA model file:");
		
	    txtCTEModelFilePath = new Text(container, SWT.BORDER | SWT.SINGLE);		    
	    txtCTEModelFilePath.addModifyListener(new ModifyListener() 
		{
			@Override
			public void modifyText(ModifyEvent e) 
			{
				onFieldChanged();
			}
		});
	    txtCTEModelFilePath.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
	    txtCTEModelFilePath.setToolTipText("The absolute path to the TESTONA model file");
		
		Button cmdGetTestcaseFile = new Button(container, SWT.PUSH);
		cmdGetTestcaseFile.setText("Browse...");
		cmdGetTestcaseFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) 
			{
				FileDialog fdlg = new FileDialog(container.getShell());
				fdlg.setFilterExtensions(new String [] {"*.testona"});
				String fileName = fdlg.open();
				if (fileName != null) 
				{
					txtCTEModelFilePath.setText(fileName);
					File cteModel = new File(fileName);
					CteModelManager ccte = new CteModelManager();
					CteObject cteo = ccte.toCteObject(cteModel, null);
					modelImportCargo.setCteObject(cteo);
				}
			}
		});
	    
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		txtSelectedProject.setLayoutData(gd);

		setControl(container);
		setPageComplete(false);		
		
		//txtTestcaseFilePath.setText(TitanPlugin.getDefault().getPreferenceStore().getString(TitanPreferenceConstants.PREF_IMPORT_FILEPATH));
		onFieldChanged();
	}

	private void onFieldChanged()
	{
		boolean b = true;
				
		if (txtCTEModelFilePath.getText().length() != 0)
			b &= true;
		else
			b = false;

		setPageComplete(b);
	}

	public String getCTEModelFilePath() {
		return txtCTEModelFilePath.getText();
	}

	public IProject getSelectedProject() {
		return selectedProject;
	}
}

