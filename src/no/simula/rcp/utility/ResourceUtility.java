package no.simula.rcp.utility;

import java.io.File;

import no.simula.depict.Activator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.resource.ImageDescriptor;

public class ResourceUtility 
{
	public static File toFile(IResource r)
	{
		return new File(r.getLocationURI());
	}

	public static IProject toProject(Object element)
	{
		if (element instanceof IResource)
			return ((IResource) element).getProject();
		
		return null;
	}

	public static IResource toResource(Object element)
	{
		if (element instanceof IResource)
			return ((IResource) element);
		
		return null;
	}

	public static boolean isProject(Object element)
	{
		if (element instanceof IResource)
		{
			IResource r = (IResource) element;
			return r.getType() == IResource.PROJECT ? true : false; 
		}	
		
		return false;
	}

	public static boolean isFolder(Object element)
	{
		if (element instanceof IResource)
		{
			IResource r = (IResource) element;
			return r.getType() == IResource.FOLDER ? true : false; 
		}	
		
		return false;
	}

	public static boolean isFile(Object element)
	{
		if (element instanceof IResource)
		{
			IResource r = (IResource) element;
			return r.getType() == IResource.FILE ? true : false; 
		}	
		
		return false;
	}
	
	public static ImageDescriptor getImageDescriptor(String fileName)
	{
		return Activator.getImageDescriptor("icons/" + fileName);
	}
}
