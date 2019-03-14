package no.simula.depict.project;

import java.net.URI;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class DepictProjectSupport 
{
	public static final String IMPORTED_MODELS_FOLDER_NAME = "Imported models";
	public static final String GRAPH_MODELS_FOLDER_NAME = "Graphs";
    /**
     *
     * @param projectName
     * @param location
     * @param natureId
     * @return
     */
    public static IProject createProject(String projectName, URI location) 
    {
        Assert.isNotNull(projectName);
        Assert.isTrue(projectName.trim().length() > 0);
 
        IProject project = createBaseProject(projectName, location);
        try 
        {
            addNature(project);
 
            //String[] paths = { "parent/child1-1/child2", "parent/child1-2/child2/child3" }; //$NON-NLS-1$ //$NON-NLS-2$
            //String[] paths = {IMPORTED_MODELS_FOLDER_NAME, SURGICAL_QUERY_FOLDER_NAME}; //$NON-NLS-1$ //$NON-NLS-2$
            String[] paths = {IMPORTED_MODELS_FOLDER_NAME, GRAPH_MODELS_FOLDER_NAME}; //$NON-NLS-1$ //$NON-NLS-2$
            addToProjectStructure(project, paths);
        } 
        catch (CoreException e) 
        {
            e.printStackTrace();
            project = null;
        }
 
        return project;
    }
 
    /**
     * Just do the basics: create a basic project.
     *
     * @param location
     * @param projectName
     */
    private static IProject createBaseProject(String projectName, URI location) 
    {
        // it is acceptable to use the ResourcesPlugin class
        IProject newProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 
        if (!newProject.exists()) 
        {
            URI projectLocation = location;
            IProjectDescription desc = newProject.getWorkspace().newProjectDescription(newProject.getName());
            if (location != null && ResourcesPlugin.getWorkspace().getRoot().getLocationURI().equals(location)) 
            {
                projectLocation = null;
            }
 
            desc.setLocationURI(projectLocation);
            try 
            {
                newProject.create(desc, null);
                if (!newProject.isOpen()) 
                {
                    newProject.open(null);
                }
            } 
            catch (CoreException e) 
            {
                e.printStackTrace();
            }
        }
 
        return newProject;
    }
 
    private static void createFolder(IFolder folder) throws CoreException 
    {
        IContainer parent = folder.getParent();
        if (parent instanceof IFolder) 
        {
            createFolder((IFolder) parent);
        }
        
        if (!folder.exists()) 
        {
            folder.create(false, true, null);
            if (folder.getName().equals(GRAPH_MODELS_FOLDER_NAME))
            	folder.setHidden(true);
        }
    }
 
    /**
     * Create a folder structure with a parent root, overlay, and a few child
     * folders.
     *
     * @param newProject
     * @param paths
     * @throws CoreException
     */
    private static void addToProjectStructure(IProject newProject, String[] paths) throws CoreException 
    {
        for (String path : paths) 
        {
            IFolder etcFolders = newProject.getFolder(path);
            createFolder(etcFolders);
        }
    }
 
    private static void addNature(IProject project) throws CoreException 
    {
        if (!project.hasNature(DepictProjectNature.NATURE_ID)) 
        {
            IProjectDescription description = project.getDescription();
            String[] prevNatures = description.getNatureIds();
            String[] newNatures = new String[prevNatures.length + 1];
            System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
            newNatures[prevNatures.length] = DepictProjectNature.NATURE_ID;
            description.setNatureIds(newNatures);
 
            IProgressMonitor monitor = null;
            project.setDescription(description, monitor);
        }
    }
    
    public static IFile getGraphModelFile(IFile cteXlFile)
    {
    	IProject p = cteXlFile.getProject();
    	IFolder f = p.getFolder(GRAPH_MODELS_FOLDER_NAME);
    	return f.getFile(cteXlFile.getName() + ".bin");
    }
}
