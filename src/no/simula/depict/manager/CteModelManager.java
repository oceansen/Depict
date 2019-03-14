package no.simula.depict.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.io.output.XmlStreamWriter;
import org.apache.commons.jxpath.JXPathContext;
import org.eclipse.core.runtime.IPath;

import no.simula.depict.data.CteConnectionInfo;
import no.simula.depict.model.Cargo;
import no.simula.depict.model.CteClass;
import no.simula.depict.model.CteClassReference;
import no.simula.depict.model.CteClassification;
import no.simula.depict.model.CteComposition;
import no.simula.depict.model.CteContent;
import no.simula.depict.model.CteModelSymbols;
import no.simula.depict.model.CteObject;
import no.simula.depict.model.CteTag;
import no.simula.depict.model.CteTestCase;
import no.simula.depict.model.CteTestGroup;
import no.simula.depict.model.TestCaseInteraction;

public class CteModelManager implements Serializable
{
	private static final long serialVersionUID = -7067054136284204536L;
	private static final String NO_UNIQUE_NAME_FMT = "The test case name '%s' has been duplicated.\nPlease check your data interaction model";
	private Cargo cargo;
	
	public CteObject toCteObject(File cteXlFile, File graphModelFile)
	{
		try 
		{
			JAXBContext ctx = JAXBContext.newInstance(CteObject.class);
			Unmarshaller um = ctx.createUnmarshaller();

			InputStream fis = new FileInputStream(cteXlFile);
			//
			// Remove BOM (byte order mark) if any
			BOMInputStream bomis = new BOMInputStream(fis, ByteOrderMark.UTF_8,
					   ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE,
					   ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE);

			ByteOrderMark bom = bomis.getBOM();
			CteObject cteo = (CteObject) um.unmarshal(bomis);
			cteo.setBom(bom);
			CteComposition catalog = getCatalog(cteo); 
			CteComposition schema = getSchema(cteo); 
			cteo.setCatalogName(catalog != null ? catalog.getName() : null);
			cteo.setSchemaName(schema.getName());
			cteo.setModelName(cteXlFile.getName());
			if (graphModelFile != null)
				cteo.setBinaryModelFilePath(graphModelFile.getAbsolutePath());

			//
			// Check if the current model has only two levels defined catalog..table instead of catalog.schema.table
			/*
			if (schema == null)
				cteo.setSchemaMissing(true);
			else
				cteo.setSchemaName(schema.getName());
			*/
			return cteo;
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
			return null;
		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
			return null;
		} catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}
	}

	public void persist(File cteXlFile, CteObject cteo, Cargo c, IPath binPath) throws JAXBException, IOException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException
	{
		Writer w = null;
		
		try
		{
			javax.xml.bind.JAXBContext ctx = JAXBContext.newInstance(CteObject.class); 
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			String charSet = "UTF-8";
			if (cteo.getBom() != null)
			{
				if (cteo.getBom().equals(ByteOrderMark.UTF_8))
					charSet = "UTF-8";
				else if (cteo.getBom().equals(ByteOrderMark.UTF_16BE))
					charSet = "UTF-16BE";
				else if (cteo.getBom().equals(ByteOrderMark.UTF_16LE))
					charSet = "UTF-16LE";
			}
			
			//w = new FileWriter(cteXlFile);
			//w = new FileWriterWithEncoding(cteXlFile, Charset.forName(charSet));
			w = new XmlStreamWriter(cteXlFile, charSet);
			//w.write(new String(cteo.getBom().getBytes()), 0, cteo.getBom().length());
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.setProperty(Marshaller.JAXB_ENCODING, charSet);
			m.marshal(cteo, w);
			IPath d = binPath.append(cteo.getModelName() + ".bin");
			serializeModel(cteo, c, d.toString());
		}
		finally
		{
			w.close();
		}
	}
	
	public CteConnectionInfo toConnectionInfo(CteObject cteo)
	{
		/*
		CteComposition catalog = getCatalog(cteo);
		if (catalog != null)
			return new CteConnectionInfo(catalog);
		*/
		CteComposition schema = getSchema(cteo);
		return new CteConnectionInfo(schema);
	}

	public void setConnectionInfo(CteObject cteo, CteConnectionInfo cci) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		//CteComposition catalog = getCatalog(cteo);
		CteComposition schema = getSchema(cteo);
		
		removeDbConnectionTag(schema);
		removeTagManagerTag(cteo);
		
		Integer id = getNextCteModelId(cteo);

		CteTag tag = new CteTag();
		tag.setId(String.format("c%d", id));
		tag.setType(CteModelSymbols.CTEXL_TAG_TAGMANAGER);
		
		//
		// Save the <Tag ... type="TagManager">
		//				...
		//				<Content value="..." key="dbConnection"/>
		//          </Tag>
		// structure
		//
		CteContent content = new CteContent();
		content.setValue(CteModelSymbols.CTEXL_TAG_DBMETACONTENT_VALUE);
		content.setKey(CteModelSymbols.CTEXL_TAG_DBCONNECTION);
		List<CteContent> contents = new ArrayList<CteContent>();
		contents.add(content);
		tag.setContents(contents);
		cteo.setTag(tag);
		
		//
		// Save the database connection info within root 'Composition' (AKA catalog) tag
		tag = new CteTag();
		tag.setId(String.format("c%d", id + 1));
		tag.setType(CteModelSymbols.CTEXL_TAG_DBCONNECTION);
		populateDbConnectionElements(tag, cci);
		schema.setTags(new ArrayList<CteTag>());
		schema.getTags().add(tag);
	}

	private void populateDbConnectionElements(CteTag dbConnectionTag, CteConnectionInfo cci)
	{
		List<CteContent> contents = new ArrayList<CteContent>();

		CteContent content = new CteContent();
		content.setKey(CteModelSymbols.CTEXL_ATTR_PORT);
		content.setValue(cci.getPort());
		contents.add(content);
		
		content = new CteContent();
		content.setKey(CteModelSymbols.CTEXL_ATTR_SERVER);
		content.setValue(cci.getServer());
		contents.add(content);

		content = new CteContent();
		content.setKey(CteModelSymbols.CTEXL_ATTR_DRIVERCLASS);
		content.setValue(cci.getDriverClass());
		contents.add(content);

		content = new CteContent();
		content.setKey(CteModelSymbols.CTEXL_ATTR_USER);
		content.setValue(cci.getUser());
		contents.add(content);

		content = new CteContent();
		content.setKey(CteModelSymbols.CTEXL_ATTR_PASSWORD);
		content.setValue(cci.getPassword());
		contents.add(content);

		content = new CteContent();
		content.setKey(CteModelSymbols.CTEXL_ATTR_DRIVERNAME);
		content.setValue(cci.getDriverName());
		contents.add(content);
		
		content = new CteContent();
		content.setKey(CteModelSymbols.CTEXL_ATTR_DBNAME);
		content.setValue(cci.getDbName());
		contents.add(content);

		content = new CteContent();
		content.setKey(CteModelSymbols.CTEXL_ATTR_CONNECTIONSTRING_TEMPLATE);
		content.setValue(cci.getConnectionStringTemplate());
		contents.add(content);

		dbConnectionTag.setContents(contents);
	}
	
	private void removeDbConnectionTag(CteComposition catalog)
	{
		if (catalog.getTags() == null)
			return;
		
		for (CteTag t : catalog.getTags())
		{
			if (t.getType().equals(CteModelSymbols.CTEXL_TAG_DBCONNECTION))
			{
				catalog.getTags().remove(t);
				return;
			}
		}
	}
	
	private void removeTagManagerTag(CteObject cteo)
	{
		if (cteo.getTag() == null)
			return;

		cteo.setTag(null);
	}

	private Integer getNextCteModelId(CteObject cteo) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException
	{
		List<Integer> ids = new ArrayList<Integer>();
		JXPathContext xpath = JXPathContext.newContext(cteo);
		Iterator<?> it = xpath.iterate("//*[id]");
		while (it.hasNext())
		{
			Object o = it.next();
			Class<?> c = o.getClass();

			Field fld = c.getDeclaredField("id");
			fld.setAccessible(true);
			String id = (String) fld.get(o);

			//
			// CTE-XL id has the format c<number>, so remove the 'c' in order to have an integer
			ids.add(Integer.valueOf(id.substring(1)));
		}

		Collections.sort(ids);
		return ids.get(ids.size() - 1) + 1;
	}
	
	public List<TestCaseInteraction> toTestcaseInteraction(CteObject cteo, List<CteTestCase> selectedTestcases) throws ClassNotFoundException, SQLException, IOException, CteModelConversionException
	{
		if (selectedTestcases == null)
			return null;

		int i = 0;
		List<TestCaseInteraction> tcis = new ArrayList<TestCaseInteraction>();
		for (CteTestCase tc : selectedTestcases)
		{
			String ids[] = tc.getMarks().getSelectedClassesAsList();
			List<CteClassReference> ccrs = new ArrayList<CteClassReference>();
			for (String id : ids)
				ccrs.add(findClassReference(cteo, id));
			
			TestCaseInteraction tci = new TestCaseInteraction(tc, ccrs);
			tci.setInteractionsId(++i);
			tcis.add(tci);
		}
		return tcis;
	}
	

	private void serializeModel(CteObject cteo, Cargo cargo, String destFilePath) throws IOException
	{
		// Write to disk with FileOutputStream
		FileOutputStream fos = new FileOutputStream(destFilePath);

		// Write object with ObjectOutputStream
		ObjectOutputStream oos = new ObjectOutputStream(fos);

		// Write object out to disk
		oos.writeObject(cargo);		
		oos.close();
	}
	
	public Cargo unserializeModel(CteObject cteo) throws IOException, ClassNotFoundException
	{
		if (cargo != null)
			return cargo;
		
		// Read from disk using FileInputStream
		FileInputStream fis = new FileInputStream(cteo.getBinaryModelFilePath());

		// Read object using ObjectInputStream
		ObjectInputStream ois = new ObjectInputStream (fis);

		// Read an object
		cargo = (Cargo) ois.readObject();
		return cargo;
	}

	// (catalog)-->(schema)-->(tables) (catalog) is optional.
	public CteComposition getCatalog(CteObject cteo)
	{
		//
		// How many levels of composition?
		// If 3 then the structure is: (catalog)-->(schema)-->(tables)
		// If 2 then the structure is: (schema)-->(tables)
		//
		CteComposition cc1 = cteo.getTree().getComposition(); //catalog?
		CteComposition cc2 = cc1.getCompositions().get(0); //schema?
		if (cc2.getCompositions() == null)
			return null;
		
		return cc1;
	}
	
	public CteComposition getSchema(CteObject cteo)
	{
		CteComposition cc1 = cteo.getTree().getComposition(); //catalog?
		CteComposition cc2 = cc1.getCompositions().get(0); //schema?
		if (cc2.getCompositions() == null)
			return cc1;
		
		return cc2;
	}

	/*

	//
	// Note: Detect if the CTE XL model has a definition for the hierarchy catalog/schema/table or simply catalog/table
	private CteComposition getSchema(CteComposition catalog)
	{
		CteComposition cc = catalog.getCompositions().get(0);
		
		//
		// If none level of composition exists under the one under the catalog, this means that no schema exists.
		if (cc.getCompositions() == null) 
			return null;

		return cc; //catalog.getCompositions().get(0);
	}
	*/
	
	public String getReadableCteExpressionFromTestcase(CteObject cteo, CteTestCase testCase)
	{
		String whereCondition = "";
		//System.out.println(tc);

		if (testCase == null)
			return whereCondition;
		
		if (testCase.getMarks() == null)
			return whereCondition;
			
		String ids[] = testCase.getMarks().getSelectedClassesAsList();
		for (String id : ids)
		{
			CteClassReference ctr = findClassReference(cteo, id);
			//System.out.println(ctr);
			if (ctr != null)
			{
				if (whereCondition.length() != 0)
					whereCondition += ", ";
				
				whereCondition += ctr.getFullyClassificationName() + "=" + ctr.getClassName(); 
			}
		}
		return whereCondition;
	}

	private CteClassReference findClassReference(CteObject cteo, String classId)
	{
		CteComposition catalog = getCatalog(cteo);
		CteComposition schema = getSchema(cteo);
		
		//
		// Iterate through tables
		List<CteComposition> tables = null;
		if (cteo.isSchemaMissing())
			tables = catalog.getCompositions();
		else
			tables = schema.getCompositions();
		
		for (CteComposition table : tables)
		{
			//
			// Iterate through columns
			for (CteClassification column : table.getClassifications())
			{
				//
				// Iterate through column values
				for (CteClass value : column.getClasses())
				{
					if (value.getId().equals(classId))
					{
						CteClassReference cr = new CteClassReference();
						cr.setClassId(classId);
						cr.setClassName(value.getName());
						if (!cteo.isSchemaMissing())
							cr.setFullyClassificationName(schema.getName() + "." + table.getName() + "." + column.getName());
						else
							cr.setFullyClassificationName(table.getName() + "." + column.getName());
						return cr;
					}
				}
			}
		}
		return null;
	}

	public List<CteTestCase> getAllTestCases(CteObject cteo)
	{
		List<CteTestCase> tcs = new ArrayList<CteTestCase>();
		Deque<String> q = new LinkedList<String>();
		CteTestGroup tg = cteo.getTestGroup();
		getAllTestCases(tcs, tg, q);
		return tcs;
	}
	
	public boolean checkNamesValidity(CteObject cteo) throws CteModelValidationException
	{
		List<CteTestCase> tcs = getAllTestCases(cteo);
		
		//
		// Check for uniqueness
		for (CteTestCase tc : tcs)
		{
			int i = tcs.indexOf(tc);
			int j = tcs.lastIndexOf(tc);
			if (i != j)
				throw new CteModelValidationException(String.format(NO_UNIQUE_NAME_FMT, tc.getFullName()));
		}
		
		return false;
	}
	
	private void getAllTestCases(List<CteTestCase> testCases, CteTestGroup tg, Deque<String> q)
	{
		if (tg == null)
			return;

		if (tg.getTestGroups() != null)
		{
			for (CteTestGroup tgi : tg.getTestGroups())
			{
				q.add(tgi.getName());
				getAllTestCases(testCases, tgi, q);
			}
		}
		
		if (tg.getTestCases() != null)
		{
			Iterator<String> it = q.iterator();
			String prefix = "";
			while (it.hasNext())
			{
				if (prefix.length() > 0)
					prefix += ".";
				prefix += it.next();
			}
			//System.out.println("prefix: " + prefix);
			
			for (CteTestCase tc : tg.getTestCases())
			{
				//tc.setFullName(tg.getName().length() > 0 ? tg.getName() + "." + tc.getName() : tc.getName());
				tc.setFullName(prefix.length() > 0 ? prefix + "." + tc.getName() : tc.getName());
				testCases.add(tc);
				//System.out.println("---" + tc.getName());
			}
			
			if (q.size() > 0)
				q.removeLast();
			//System.out.println("remove: " + q.removeLast());
		}	
	}

	public class CteModelConversionException extends Exception
	{
		private static final long serialVersionUID = -3568084747310171781L;
		
		public CteModelConversionException(String message)
		{
			super(message);
		}
	}

	public class CteModelValidationException extends Exception
	{
		private static final long serialVersionUID = -3568084747310171781L;
		
		public CteModelValidationException(String message)
		{
			super(message);
		}
	}
}
