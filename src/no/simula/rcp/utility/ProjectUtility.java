package no.simula.rcp.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ProjectUtility 
{
	public static IProject findProject(String projectName) throws CoreException
	{
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) 
		{
			if (project.getName().equals(projectName))
				return project;

			/*
			IProjectDescription description;
			description = project.getDescription();
			String[] natures = description.getNatureIds();
			System.out.println(project);
			IResource res = project.findMember("CiscoVideoSystem.xfm");
			System.out.println(res);
			
			for (int i = 0; i < natures.length; i++)
			{
				if (natures[i].equals(ProjectNature.NATURE_ID))
				{
					if (project.exists() && !project.isOpen())
						project.open(null);
					projects.add(project);
				}
			}
			*/
		}
		
		return null;
	}

	public static boolean resourceAlreadyExist(IProject project, String resourceName)
	{
		return project.findMember(resourceName) != null ? true : false;
	}

	public static IResource findResource(IProject project, String resourceName)
	{
		return project.findMember(resourceName);
	}
	
	public static IProject toProject(IStructuredSelection selection)
	{
		if (!(selection.getFirstElement() instanceof IResource))
			return null;
		
        IResource resource = (IResource) selection.getFirstElement();
		return resource == null ? null : resource.getProject();
	}

	public static IResource toResource(IStructuredSelection selection)
	{
		if (!(selection.getFirstElement() instanceof IResource))
			return null;
		
        return (IResource) selection.getFirstElement();
	}

	public static List<IResource> getResourcesFromExtension(IProject project, String extension)
	{
		List<IResource> ret = new ArrayList<IResource>();
		
		try 
		{
			IResource resources [] = project.members();
			projectVisitor(ret, resources, extension);
		} 
		catch (CoreException e) 
		{
			e.printStackTrace();
		}
		
		return ret;
	}
	
	private static void projectVisitor(List<IResource> output, IResource resources [], String extension) throws CoreException
	{
		for (IResource res : resources)
		{
			if (res.getType() == IResource.FILE && 
				res.getFileExtension() != null && 
				res.getFileExtension().equalsIgnoreCase(extension))
				output.add(res);
			else if (res.getType() == IResource.FOLDER)
			{
				projectVisitor(output, ((IFolder) res).members(), extension);
			}
		}		
	}
	
	
	public static IResource getResourceFromExtension(IProject project, String extension)
	{
		try 
		{
			if (project == null)
				return null;
			
			IResource resources [] = project.members();
			for (IResource res : resources)
				if (res.getFileExtension() != null && res.getFileExtension().equalsIgnoreCase(extension))
					return res;
		} 
		catch (CoreException e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}

	public static List<IProject> getProjects() throws CoreException
	{
		List<IProject> projects = new ArrayList<IProject>();
		
		for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) 
		{
			IProjectDescription description = project.getDescription();
			String[] natures = description.getNatureIds();
			for (int i = 0; i < natures.length; i++)
			{
				System.out.println(String.format("Project:%s, nature:%s", project.getName(), natures[i]));
				/*
				if (natures[i].equals(ProjectNature.NATURE_ID))
				{
					//System.out.println(project);
					if (project.exists() && !project.isOpen())
						project.open(null);
					projects.add(project);
				}
				*/
			}
		}
		
		return projects;
	}

	public static IFile addFile(IProject project, String resourceName, String sourceFilePath) throws FileNotFoundException, CoreException
	{
		InputStream is = new FileInputStream(sourceFilePath);
		IFile f = project.getFile(resourceName);
		if (f.exists())
		{
			//
			// IMPORTANT: the use of setContents will trigger the ResourceChange listener that, as a consequence, starts the rebuild of the family model.
			// This leads to some weird problems related to race condition between the UI thread and the rebuild thread. 
			//f.setContents(is, IResource.FORCE, null);
			f.delete(true, null);
			f.create(is, IResource.FORCE, null);
		}
		else
			f.create(is, IResource.FORCE, null);
		
        return f;
	}

	public static IFile copyFile(IFolder folder, String sourceFilePath) throws FileNotFoundException, CoreException
	{
		if (folder.exists())
		{
			String fileName = new File(sourceFilePath).getName();
			IFile f = folder.getFile(fileName);
			FileInputStream fis = new FileInputStream(sourceFilePath);
			f.create(fis, IResource.FORCE, null);
			return f;
		}
		
		return null;
	}		


	public static void spyProject(IProject project)
	{
		try 
		{
			IResource resources [] = project.members();
			spy(resources);
			//for (IResource res : resources)
			//	System.out.println(String.format("Name [%s], Ext. [%s], URI [%s]", res.getName(), res.getFileExtension(), res.getLocationURI().toASCIIString()));
		} 
		catch (CoreException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void spy(IResource [] resources) throws CoreException
	{
		for (IResource res : resources)
		{
			if (res.getType() == IResource.FILE)
				System.out.println(String.format("File [%s], Ext. [%s], URI [%s]", res.getName(), res.getFileExtension(), res.getLocationURI().toASCIIString()));
			else if (res.getType() == IResource.FOLDER)
			{
				System.out.println(String.format("Folder [%s], URI [%s]", res.getName(), res.getLocationURI().toASCIIString()));
				spy(((IFolder) res).members());
			}
		}		
	}
}
