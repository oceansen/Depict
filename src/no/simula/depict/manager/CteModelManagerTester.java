package no.simula.depict.manager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;

import no.simula.depict.model.CteComposition;
import no.simula.depict.model.CteObject;

public class CteModelManagerTester 
{
	public static void main(String[] args) 
	{
		//String cteXlFile = "C:\\Users\\carlo\\Downloads\\TASS_rolletilganger_DB.testona";
		String cteXlFile = "C:\\projects\\simula\\depict\\models\\n2a-models\\people-utf16.testona";
		//String cteXlFile = "C:\\projects\\eclipse-workspaces\\runtime-no.simula.depict.product\\demo\\Imported models\\people.testona";
		
		//String cteXlFile = "C:\\Users\\carlo\\Downloads\\TASS_rolletilganger_DB-UTF16NP.testona";
		
		try 
		{
			InputStream fis = new FileInputStream(cteXlFile);
			//
			// Remove BOM (byte order mark) if any
			BOMInputStream bomis = new BOMInputStream(fis, ByteOrderMark.UTF_8,
					   ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_16BE,
					   ByteOrderMark.UTF_32LE, ByteOrderMark.UTF_32BE
					   );
			//BOMInputStream bomis = new BOMInputStream(fis, true);

			try 
			{
				
				ByteOrderMark bom = bomis.getBOM();
				String s = new String(bom.getBytes());
				System.out.println(s);
			} catch (IOException e) {
				e.printStackTrace();
			}			
			
			JAXBContext ctx = JAXBContext.newInstance(CteObject.class);
			Unmarshaller um = ctx.createUnmarshaller();
			CteObject cteo = (CteObject) um.unmarshal(bomis);
			CteModelManager ctem = new CteModelManager();
			CteComposition catalog = ctem.getCatalog(cteo); //CteObject.getCatalog(cteo);
			System.out.println(catalog);
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (JAXBException e) 
		{
			e.printStackTrace();
		}
	}
}
