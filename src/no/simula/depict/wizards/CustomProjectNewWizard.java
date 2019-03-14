package no.simula.depict.wizards;

import java.net.URI;

import no.simula.depict.project.DepictProjectSupport;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

public class CustomProjectNewWizard extends Wizard implements INewWizard, IExecutableExtension 
{
	private static final String WIZARD_NAME = "Create a new Depict project";
	private static final String PAGE_NAME = "Depict Project Wizard";
	private static final String PAGE_TITLE = "Depict Project Wizard";
	private static final String PAGE_DESCRIPTION = "Create an empty Depict Project";
	
	private WizardNewProjectCreationPage pageOne;
	//private IConfigurationElement configurationElement;
	
	public CustomProjectNewWizard() 
	{
		setWindowTitle(WIZARD_NAME);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) 
	{
	}

	@Override
	public boolean performFinish() 
	{
	    String name = pageOne.getProjectName();
	    URI location = null;
	    if (!pageOne.useDefaults()) 
	    {
	        location = pageOne.getLocationURI();
	    } // else location == null
	 
	    DepictProjectSupport.createProject(name, location);
	    //BasicNewProjectResourceWizard.updatePerspective(configurationElement);
		return true;
	}

	@Override
	public void addPages() 
	{
	    super.addPages();
	    pageOne = new WizardNewProjectCreationPage(PAGE_NAME);
	    pageOne.setTitle(PAGE_TITLE);
	    pageOne.setDescription(PAGE_DESCRIPTION);
	 
	    addPage(pageOne);
	}

	@Override
	public void setInitializationData(IConfigurationElement config,	String propertyName, Object data) throws CoreException 
	{
		//configurationElement = config;
	}	
}
