package no.simula.depict;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication 
{
	private static EntityManagerFactory emf;
	private static EntityManager em;
	
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception 
	{
		//
		// This way we instruct SWT/AWT bridge to use the native look and feel
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	try 
	        	{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} 
	        	catch (ClassNotFoundException | InstantiationException
						| IllegalAccessException
						| UnsupportedLookAndFeelException e) 
	        	{
					e.printStackTrace();
				}	        
        	}
	    });
		
		//
		// Force the bootstrapping of JPA stuff ... 
		//emf = getEntityManagerFactory();
		//em = getEntityManager();

		/*
		em.getTransaction().begin();
		Publisher p = new Publisher(4, "puppo");
		em.persist(p);
		em.getTransaction().commit();
		*/
		
		/*
		Query query = em.createQuery("Select p from Publisher p order by p.name asc");		

		@SuppressWarnings("unchecked")
		List<Publisher> publishers = query.getResultList();
		for (Publisher publisher : publishers) 
		{
			System.out.println(publisher.getName());
		}
		*/
		
		Display display = PlatformUI.createDisplay();
		
		try 
		{
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			else
				return IApplication.EXIT_OK;
		} 
		finally 
		{
			display.dispose();
			if (em != null)
				em.close();
		}
	}

	public static EntityManagerFactory getEntityManagerFactory() 
	{
        if (emf == null) 
        {
            HashMap<String, Object> properties = new HashMap<String, Object>();
            properties.put(PersistenceUnitProperties.CLASSLOADER, Activator.class.getClassLoader());
            emf = new PersistenceProvider().createEntityManagerFactory(GlobalSymbols.PU_NAME, properties);
        }
        return emf;
    }
	
	public static EntityManager getEntityManager()
	{
		if (em == null)
		{
			Map<String, Object> props = new HashMap<String, Object>();
			props.put(PersistenceUnitProperties.CLASSLOADER, Activator.class.getClassLoader());
			em = emf.createEntityManager(props);
		}
		return em;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() 
	{
		if (!PlatformUI.isWorkbenchRunning())
			return;
	
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() 
		{
			public void run() 
			{
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}
}
