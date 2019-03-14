package no.simula.depict.manager;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.simula.depict.data.CteConnectionInfo;
import no.simula.depict.data.DataManager;
import no.simula.depict.data.TableColumnMetadata;
import no.simula.depict.data.TableMetadata;
import no.simula.depict.data.TableRelationship;
import no.simula.depict.model.Cargo;
import no.simula.depict.model.CteClassReference;
import no.simula.depict.model.CteComposition;
import no.simula.depict.model.CteObject;
import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;
import no.simula.depict.model.TestCaseInteraction;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

public class EntityRelationshipModelManager 
{
	//private static final int TRIES_MAX_COUNT = 5;
	private static final String DONT_CARE = "*";
	private static final String LIKE_OP_REGEX = "^\\s*LK\\(.*\\)\\s*$";
	private static final String NOT_LIKE_OP_REGEX = "^\\s*NOT\\s+LK\\(.*\\)\\s*$";
	private static final String RANGE_OP_REGEX = "^\\s*RNG\\((\\d+\\.?\\d*)?\\s*,\\s*(\\d+\\.?\\d*)?\\)\\s*$";
	private static final String IN_OP_REGEX = "^\\s*IN\\((\\w+\\s*,*\\s*)+\\)\\s*$";
	private static final String NOT_IN_OP_REGEX = "^\\s*NOT\\s+IN\\((\\w+\\s*,*\\s*)+\\)\\s*$";
	private static final String ISNULL_OP_REGEX = "^\\s*ISNULL\\s*$";
	private static final String NOT_ISNULL_OP_REGEX = "^\\s*NOT\\s+ISNULL\\s*$";
	private static final String ISBLANK_OP_REGEX = "^\\s*ISBLANK\\s*$";
	private static final String NOT_ISBLANK_OP_REGEX = "^\\s*NOT\\s+ISBLANK\\s*$";
	private static final String ISEMPTY_OP_REGEX = "^\\s*ISEMPTY\\s*$";
	private static final String NOT_ISEMPTY_OP_REGEX = "^\\s*NOT\\s+ISEMPTY\\s*$";
	private static final String RECURSIVE_OP_REGEX = "^\\s*REC\\((.*)?\\s*,\\s*(.*)?\\)\\s*$";

	private CteConnectionInfo cteConnectionInfo;
	private CteObject cteObject;

	public EntityRelationshipModelManager(CteObject cteObject, CteConnectionInfo cteConnectionInfo)
	{
		this.cteObject = cteObject;
		this.cteConnectionInfo = cteConnectionInfo;
	}
	
	public Graph<DepictVertex, DepictEdge> extract() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException
	{
		DataManager dm = new DataManager();
		Graph<DepictVertex, DepictEdge> graph  = graphFactory();
		
		try
		{
			dm.setup(cteConnectionInfo);
			dm.open();

			CteModelManager ctem = new CteModelManager();
			//CteComposition catalog = ctem.getCatalog(cteObject); 
			CteComposition schema = ctem.getSchema(cteObject);

			//
			// Create original vertices on the basis of the compositions coming from the CTE-XL model 
			//List<CteComposition> tables = cteObject.isSchemaMissing() ? catalog.getCompositions() : schema.getCompositions();
			List<CteComposition> tables = schema.getCompositions();
			Map<String, DepictVertex> sources = new HashMap<String, DepictVertex>();
			for (CteComposition table : tables)
			{
				DepictVertex v = new DepictVertex(cteObject.isSchemaMissing() ? table.getName() : schema.getName() + "." + table.getName(), 
						table.getName(), 
						schema != null ? schema.getName() : null); 
				v.setOutputVertex(true);
				v.setPrimaryKeyFields(dm.getPrimaryKey(cteObject.getCatalogName(), cteObject.getSchemaName(), table.getName()));
				graph.addVertex(v);
				sources.put(v.getId(), v);
			}			
			
			List<TableMetadata> tmds = dm.getTables(cteObject.getCatalogName(), cteObject.getSchemaName());
			buildGraph(graph, tmds, sources, dm);
		}
		finally
		{
			dm.close();
		}
		
		return graph;
	}
	
	public List<TableColumnMetadata> getTableColumns(String tableName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException
	{
		DataManager dm = new DataManager();
		
		try
		{
			dm.setup(cteConnectionInfo);
			dm.open();
			return dm.getColumns(cteObject.getCatalogName(), cteObject.getSchemaName(), tableName);
		}
		finally
		{
			dm.close();
		}
	}
	
	public Graph<DepictVertex, DepictEdge> copyToUndirected(Graph<DepictVertex, DepictEdge> source)
	{
		Graph<DepictVertex, DepictEdge> dest = graphFactory();
		for (DepictVertex v : source.getVertices())
			dest.addVertex(v);
		
		for (DepictEdge e : source.getEdges())
		{
			DepictEdge e1 = new DepictEdge(e);
			Pair<DepictVertex> p = source.getEndpoints(e);
			dest.addEdge(e1, p.getFirst(), p.getSecond());
		}
		
		return dest;
	}

	private Graph<DepictVertex, DepictEdge> graphFactory()
	{
		//Graph<DepictVertex, DepictEdge> graph  = new DirectedSparseGraph<DepictVertex, DepictEdge>();
		return new SparseMultigraph<DepictVertex, DepictEdge>();
	}
	
	private void buildGraph(Graph<DepictVertex, DepictEdge> graph, List<TableMetadata> tmds, Map<String, DepictVertex> sources, DataManager dm) throws SQLException 
	{
		for (TableMetadata tmd : tmds)
		{
			List<TableRelationship> impRels = dm.getImportedTables(tmd.getCatalog(), tmd.getSchema(), tmd.getName());

			//
			// Check for imported relationships
			for (TableRelationship r : impRels)
			{
				DepictVertex v1 = findVertex(graph, tmd);
		    	if (v1 == null)
		    	{			    		
		    		v1 = new DepictVertex(tmd); //tmd.getId(), tmd.getName());
		    		graph.addVertex(v1);
		    	}

		    	DepictVertex v2 = findVertex(graph, r.getPrimaryTable());
				if (v2 == null)
				{
					v2 = new DepictVertex(r.getPrimaryTable().getId(), r.getPrimaryTable().getName(), r.getSchemaName());
					graph.addVertex(v2);
				}					
	    		DepictEdge e = new DepictEdge(r);
	    		graph.addEdge(e, v1, v2);
			}
		}
	
		buildGraph2(graph, tmds);
	}

	//
	// Add 'floating' tables or views: those without any relationships toward other entities
	//
	private void buildGraph2(Graph<DepictVertex, DepictEdge> graph, List<TableMetadata> tmds) throws SQLException 
	{
		for (TableMetadata tmd : tmds)
		{
			DepictVertex v = findVertex(graph, tmd);
	    	if (v == null)
	    	{			    		
	    		v = new DepictVertex(tmd);
	    		graph.addVertex(v);
	    	}
		}
	}

	private DepictVertex findVertex(Graph<DepictVertex, DepictEdge> graph, TableMetadata t)
	{
		DepictVertex v = new DepictVertex(t); //t.getId(), t.getName());
		for (DepictVertex v2 : graph.getVertices())
		{
			if (v2.equals(v))
				return v2;
		}
		
		return null;
	}
	
	//
	// The method tries to obtain a spanning tree (a tree built with all the nodes of an input graph)
	public Cargo buildModel(List<TestCaseInteraction> tcis, Graph<DepictVertex, DepictEdge> graph, List<TableMetadata> tables) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException, GraphToSQLConversiontException
	{
		if (graph == null)
			return null;
		
		Graph<DepictVertex, DepictEdge> graph1 = copyToUndirected(graph);
		createTestCaseSQLStatements(graph1, tcis, tables);
		Cargo c = new Cargo(graph, tcis, tables);
		return c;
	}

	private void createTestCaseSQLStatements(Graph<DepictVertex, DepictEdge> graph, List<TestCaseInteraction> tcis, List<TableMetadata> tablesMetadata) throws GraphToSQLConversiontException
	{
		for (TestCaseInteraction tci : tcis)
		{
			// CI 16/06/14 Remove tables joining optimization (the spanning tree doesn't consider paths where its leaf is not an 'outputvertex'
			createExpressionSet(tci, tablesMetadata);
			tci.setSqlStatement(SqlExpression.toSQL(graph, tci.getExpressions()));
		}
	}
	
	private void createExpressionSet(TestCaseInteraction tci, List<TableMetadata> tables) throws GraphToSQLConversiontException
	{
		for (int i = 0; i < tci.getClassReferences().size(); ++i)
		{
			String expr;
			CteClassReference ccr = tci.getClassReferences().get(i);
			
			TableColumnMetadata tcm = getColumnMetadataFromClassReference(tables, ccr);
			
			expr = toWhereExpression(ccr.getFullyClassificationName(), ccr.getClassName(), tcm.getSqlDataType());
			//System.out.println(expr);
			
			if (expr.length() > 0)
			{
				tci.getExpressions().add(expr);
				//
				// Avoid unnecessary duplications (each test case can have multiple references to the same table)
				if (!tci.getTableNames().contains(tcm.getTableName()))
					tci.getTableNames().add(tcm.getTableName());
			}
		}
	}

	private TableColumnMetadata getColumnMetadataFromClassReference(List<TableMetadata> tables, CteClassReference ctr) throws GraphToSQLConversiontException
	{
		//
		// The fully classification name contains at least the column name and the table name. The schema name is optional.
		String[] path = ctr.getFullyClassificationName().split("\\.");
		String s = path.length == 3 ? path[0] : null;
		String c = path[path.length - 1]; //Last element: column name
		String t = path[path.length - 2]; //Element before the last element: table name

		if (s != null)
			t = s + "." + t;
		
		int i = tables.indexOf(new TableMetadata(t));
		if (i == -1)
			throw new GraphToSQLConversiontException(String.format("Invalid table name:%s. Cannot find that table within the binary representation of the metadata.", t));

		TableMetadata tmd = tables.get(i);
		i = tmd.getColumns().indexOf(new TableColumnMetadata(c));
		if (i == -1)
			throw new GraphToSQLConversiontException(String.format("Invalid column name [table.column]:%s.%s. Cannot find that column within the binary representation of the metadata.", t, c));
		TableColumnMetadata tcm = tmd.getColumns().get(i);
		tcm.setTableName(t);
		return tcm;
	}
	
	private String toWhereExpression(String name, String value, int sqlDataType) //, String operator)
	{
		value = value.trim();
		
		if (value.equals(DONT_CARE))
			return "";
		
		//
		// Examples:
		//	LK(S%) -> name LIKE 'S%' -- all values starting with 'S'
		//	NOT LK(%3_) -> name NOT LIKE '%3_' -- all values starting with any string and ending with a '3' followed by any character
		//	RNG(a,b) -> name BETWEEN a AND b
		// 	RNG(a) -> name >= a
		//	RNG(,b)-> name <= b
		//	IN(a,b,...,n) -> IN(a,b,...,n)
		//	NOT IN(a,b,...,n) -> NOT IN(a,b,...,n)
		//  ISN -> name IS NULL
		//  NOT ISN -> name IS NOT NULL
		//  ISBLANK -> name = ''
		//  NOT ISBLANK -> name <> ''
		//	ISEMPTY_OP_REGEX -> (name IS NULL OR name = '')
		//	NOT_ISEMPTY_OP_REGEX -> (name IS NOT NULL AND name <> '')

		if (value.matches(LIKE_OP_REGEX))
			return String.format("%s LIKE %s", name, wrapValue(sqlDataType, getContentWithinParentheses(value)));
		
		if (value.matches(NOT_LIKE_OP_REGEX))
			return String.format("%s NOT LIKE %s", name, wrapValue(sqlDataType, getContentWithinParentheses(value)));

		if (value.matches(RANGE_OP_REGEX))
		{
			String[] vals = getContentWithinParentheses(value).split(",");

			if (vals.length == 1)
				return String.format("%s >= %s", name, wrapValue(sqlDataType, vals[0].trim()));
				
			String v1 = vals[0].trim();
			String v2 = vals[1].trim();
			if (v1.length() > 0 && v2.length() > 0)
				return String.format("%s BETWEEN %s AND %s", name, wrapValue(sqlDataType, v1), wrapValue(sqlDataType, v2));
			
			if (v1.length() > 0 && v2.length() == 0)
				return String.format("%s >= %s", name, wrapValue(sqlDataType, v1));

			if (v1.length() == 0 && v2.length() > 0)
				return String.format("%s <= %s", name, wrapValue(sqlDataType, v2));
		}
		
		if (value.matches(IN_OP_REGEX))
			return String.format("%s IN (%s)", name, extractValuesSet(sqlDataType, value));

		if (value.matches(NOT_IN_OP_REGEX))
			return String.format("%s NOT IN (%s)", name, extractValuesSet(sqlDataType, value));
		
		if (value.matches(ISNULL_OP_REGEX))
			return String.format("%s IS NULL", name);

		if (value.matches(NOT_ISNULL_OP_REGEX))
			return String.format("%s IS NOT NULL", name);

		if (value.matches(ISBLANK_OP_REGEX))
			return String.format("%s = ''", name);

		if (value.matches(NOT_ISBLANK_OP_REGEX))
			return String.format("%s <> ''", name);

		if (value.matches(ISEMPTY_OP_REGEX))
			return String.format("(%s IS NULL OR %s = '')", name, name);

		if (value.matches(NOT_ISEMPTY_OP_REGEX))
			return String.format("(%s IS NOT NULL AND %s <> '')", name, name);

		if (value.matches(RECURSIVE_OP_REGEX))
		{
			String s = getContentWithinParentheses(value);
			int i = getCommaPositionBetweenParms(s); //s.lastIndexOf(',');

			String v1 = s.substring(0, i); //vals[0].trim();
			String v2 = s.substring(i + 1, s.length()); //vals[1].trim();

			if (v1.length() > 0 && v2.length() > 0)
			{
				String se1 = "";
				String se2 = "";
				
				if (!isGroundValue(v1))
					se1 = toWhereExpression(CteClassReference.addSuffixToTableName(name, CteClassReference.PRIMARY_SUFFIX), v1, sqlDataType);
				else
					se1 = String.format("%s = %s", CteClassReference.addSuffixToTableName(name, CteClassReference.PRIMARY_SUFFIX), wrapValue(sqlDataType, v1));

				if (!isGroundValue(v2))
					se2 = toWhereExpression(CteClassReference.addSuffixToTableName(name, CteClassReference.SECONDARY_SUFFIX), v2, sqlDataType);
				else
					se2 = String.format("%s = %s", CteClassReference.addSuffixToTableName(name, CteClassReference.SECONDARY_SUFFIX), wrapValue(sqlDataType, v2));
				
				if (se1.length() > 0 && se2.length() > 0)	
					return String.format("%s AND %s", se1, se2);
				else if (se1.length() > 0 && se2.length() == 0)
					return se1;
				else if (se1.length() == 0 && se2.length() > 0)
					return se2;
				return "";
			}
			
			if (v1.length() > 0 && v2.length() == 0)
			{
				String se1 = "";
				
				if (!isGroundValue(v1))
					se1 = toWhereExpression(CteClassReference.addSuffixToTableName(name, CteClassReference.PRIMARY_SUFFIX), v1, sqlDataType);
				else
					se1 = String.format("%s = %s", CteClassReference.addSuffixToTableName(name, CteClassReference.PRIMARY_SUFFIX), wrapValue(sqlDataType, v1));
				
				return se1;
			}
			
			if (v1.length() == 0 && v2.length() > 0)
			{
				String se2 = "";
				
				if (!isGroundValue(v2))
					se2 = toWhereExpression(CteClassReference.addSuffixToTableName(name, CteClassReference.SECONDARY_SUFFIX), v2, sqlDataType);
				else
					se2 = String.format("%s = %s", CteClassReference.addSuffixToTableName(name, CteClassReference.SECONDARY_SUFFIX), wrapValue(sqlDataType, v2));
				
				return se2;
			}
		}
		
		return String.format("%s%s%s", name, "=", wrapValue(sqlDataType, value));
	}

	private boolean isGroundValue(String value)
	{
		if (value.equals(DONT_CARE) || value.matches(LIKE_OP_REGEX) || value.matches(RANGE_OP_REGEX) || value.matches(IN_OP_REGEX) || value.matches(NOT_IN_OP_REGEX) 
				|| value.matches(ISNULL_OP_REGEX) || value.matches(NOT_ISNULL_OP_REGEX) || value.matches(ISBLANK_OP_REGEX) || value.matches(NOT_ISBLANK_OP_REGEX) 
				|| value.matches(ISEMPTY_OP_REGEX) || value.matches(NOT_ISEMPTY_OP_REGEX) || value.matches(RECURSIVE_OP_REGEX)
				)
			return false;
		
		return true;
	}
	
	private int getCommaPositionBetweenParms(String s)
	{
		int i;
		boolean openParentheses = false;
		
		for (i = 0; i < s.length(); ++i)
		{
			char c = s.charAt(i);
			switch (c)
			{
				case '(':
				openParentheses = true;
				break;
				
				case ')':
				openParentheses = false;
				break;
				
				case ',':
				if (!openParentheses)
					return i;
				break;
			}
		}
		
		return -1;
	}
	
	private String extractValuesSet(int sqlDataType, String value)
	{
		String[] vals = getContentWithinParentheses(value).split(",");
		if (vals.length == 1)
			return wrapValue(sqlDataType, vals[0].trim());
		
		String t = "";
		for (String r : vals)
		{
			if (t.length() > 0)
				t += ",";
			
			t += wrapValue(sqlDataType, r);
		}
		
		return t;
	}
	
	private String wrapValue(int sqlDataType, String value)
	{
		String w = "";
		switch (sqlDataType)
		{
			case java.sql.Types.INTEGER: 
			w = value; 
			break;
			
			case java.sql.Types.SMALLINT: 
			w = value; 
			break;
			
			case java.sql.Types.BIT: 
			w = value; 
			break;

			case java.sql.Types.NUMERIC: 
			w = value; 
			break;

			case java.sql.Types.DOUBLE:
			w = value;
			break;

			case java.sql.Types.DECIMAL:
			w = value;
			break;
				
			case java.sql.Types.VARCHAR:
			w = "'" + value + "'";
			break;
				
			case java.sql.Types.CHAR:
			w = "'" + value + "'";
			break;

			default:
			w = "'" + value + "'";
			break;
		}
		
		return w;
	}
	
	private String getContentWithinParentheses(String value)
	{
		int s = value.indexOf('(');
		int e = value.lastIndexOf(')');
		return value.substring(s + 1, e);
	}
	
	public class GraphToSQLConversiontException extends Exception
	{		
		private static final long serialVersionUID = -7113709826727111265L;

		public GraphToSQLConversiontException(String message)
		{
			super(message);
		}
	}

	public class TestCaseToGraphConversiontException extends Exception
	{		
		private static final long serialVersionUID = -7113709826727111265L;

		public TestCaseToGraphConversiontException(String message)
		{
			super(message);
		}
	}
}
