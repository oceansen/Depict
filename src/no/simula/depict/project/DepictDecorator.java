package no.simula.depict.project;

import no.simula.depict.Activator;
import no.simula.rcp.utility.ResourceUtility;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class DepictDecorator implements ILabelDecorator 
{
	Image notDecoratedImage = null;
	Image queryFolderImage = null;
	
	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image decorateImage(Image image, Object element) 
	{
		//System.out.println(element);
		if (ResourceUtility.isProject(element))
		{
			if (notDecoratedImage == null)
			{
				ImageDescriptor id = Activator.getImageDescriptor("icons/1371583308_3d_modelling.png");
				notDecoratedImage = id.createImage();
			}
			return notDecoratedImage;
		}
		/*
		else if (ResourceUtility.isFolder(element) && ResourceUtility.toResource(element).getName().equals(DepictProjectSupport.SURGICAL_QUERY_FOLDER_NAME))
		{
			if (queryFolderImage == null)
			{
				ImageDescriptor id = Activator.getImageDescriptor("icons/repo_rep.gif");
				queryFolderImage = id.createImage();
			}
			return queryFolderImage;
		}
		*/
		return null;
	}

	@Override
	public String decorateText(String text, Object element) {
		return null;
	}
}
