package no.simula.depict.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class DepictProjectNature implements IProjectNature 
{
    public static final String NATURE_ID = "no.simula.depict.projectnature";
    
	@Override
	public void configure() throws CoreException {
	}

	@Override
	public void deconfigure() throws CoreException {
	}

	@Override
	public IProject getProject() {
		return null;
	}

	@Override
	public void setProject(IProject project) {
	}
}
