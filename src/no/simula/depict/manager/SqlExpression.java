package no.simula.depict.manager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import no.simula.depict.model.CteClassReference;
import no.simula.depict.model.DepictEdge;
import no.simula.depict.model.DepictVertex;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;

public class SqlExpression implements Serializable
{
	private static final long serialVersionUID = -4815096875555062670L;
	private static final String SELECT_FORMAT_STRING1 = "SELECT %s FROM %s"; 
	private static final String SELECT_FORMAT_STRING2 = "SELECT %s FROM %s WHERE %s"; 
	//private static final String SELECT_COUNT_WRAPPER = "SELECT COUNT(*) FROM (%s) AS CNT_TBL";
	private static final String SELECT_COUNT_WRAPPER = "SELECT COUNT(*) FROM (%s) CNT_TBL";
	
	public static String toSQL(Graph<DepictVertex, DepictEdge> graph, List<String> whereConditions)
	{
		//
		// 1) Extract all the vertices 'v' having outdegree(v) >= 1 belonging to the vertices explicitly defined in the CTE-XL model 
		List<DepictVertex> roots = getRootVertices(graph);

		List<DepictEdge> es = bfsVisit(graph, roots.get(0));
		
		String from;
		if (es.size() != 0)
			from = toFromClause(graph, es);
		else
			from = roots.get(0).getSchemaName() != null ? roots.get(0).getSchemaName() + "." + roots.get(0).getName() : roots.get(0).getName();
		
		//System.out.println(from);
		String where = "";
		for (String s : whereConditions)
		{
			if (where.length() > 0)
				where += " AND ";
			
			where += s;
		}

		if (where.length() > 0)
			return String.format(SELECT_FORMAT_STRING2, getPrimaryKeySelectList(graph), from, where);
		else
			return String.format(SELECT_FORMAT_STRING1, getPrimaryKeySelectList(graph), from);
		
	}

	/**
	 * Graph walk according to breadth first search algorithm. This is a slight variation respect to the standard algorithm that guarantees the arc coverage while visiting nodes.
	 * @param graph Graph to be visited
	 * @param s Starting node
	 * @return List of the arcs according to the sequence of their visit
	 */
	private static List<DepictEdge> bfsVisit(Graph<DepictVertex, DepictEdge> graph, DepictVertex s)
	{
		clearVisitedFlags(graph);
		
		Queue<DepictVertex> q = new LinkedList<DepictVertex>(); //FIFO queue
		q.add(s);
		s.setVisited(true);
		//System.out.println(s.toString());
		List<DepictEdge> w = new ArrayList<DepictEdge>();
		
		while (!q.isEmpty())
		{
			DepictVertex v = q.remove();
			Collection<DepictEdge> es = graph.getInEdges(v);
			for (DepictEdge e : es)
			{
				if (!e.isVisited())
				{
					w.add(e);
					//System.out.println(String.format("join %s -> %s %s", e.getForeignTableName(), e.getPrimaryTableName(), toJoinCondition(e)));
					e.setVisited(true);
					
					Pair<DepictVertex> p = graph.getEndpoints(e);
					//DepictVertex d = p.getFirst().equals(v) ? p.getSecond() : p.getFirst();
					//DepictVertex d = p.getFirst().isVisited() ? p.getSecond() : p.getFirst();
					if (!p.getFirst().isVisited())
					{
						p.getFirst().setVisited(true);
						q.add(p.getFirst());
						//System.out.println(p.getFirst().toString());
					}
					if (!p.getSecond().isVisited())
					{
						p.getSecond().setVisited(true);
						q.add(p.getSecond());
						//System.out.println(p.getSecond().toString());
					}
				}
			}
		}
		
		return w;
	}
	
	private static void clearVisitedFlags(Graph<DepictVertex, DepictEdge> graph)
	{
		for (DepictVertex v : graph.getVertices())
			v.setVisited(false);
		
		for (DepictEdge e : graph.getEdges())
			e.setVisited(false);
	}
	 
	private static String toFromClause(Graph<DepictVertex, DepictEdge> graph, List<DepictEdge> es)
	{
		List<String> tables = new ArrayList<String>();
		StringBuffer from = new StringBuffer();
		List<String> selfTables = new ArrayList<String>();
		
		//
		// First pass: collect all tables participating in self-rels.
		for (DepictEdge e : es)
		{
			if (e.isRecursiveRelationship())
				selfTables.add(e.getPrimaryTableName());
		}

		//
		// Second pass: set alias names in edges whose end-points participate in self-rels.
		for (DepictEdge e : es)
		{
			Pair<DepictVertex> p = graph.getEndpoints(e);
			if (selfTables.contains(p.getFirst().getName()))
				setAlias(e, p.getFirst());
			
			if (selfTables.contains(p.getSecond().getName()))
				setAlias(e, p.getSecond());
		}
		
		//
		// Add the first element of the FROM clause
		DepictEdge first = es.get(0);
		String t = first.getForeignTableName() + (first.getForeignTableAlias() != null ? " " + first.getForeignTableAlias() : "");
		tables.add(t);
		t = first.getSchemaName() != null ? first.getSchemaName() + "." + t : t;

		from.append(t);
		
		for (DepictEdge e : es)
		{
			String f = e.getForeignTableName() + (e.getForeignTableAlias() != null ? " " + e.getForeignTableAlias() : "");
			String p = e.getPrimaryTableName() + (e.getPrimaryTableAlias() != null ? " " + e.getPrimaryTableAlias() : "");

			int n = 0;
			if (!tables.contains(f) || !tables.contains(p))
				from.append(" JOIN ");

			if (!tables.contains(f))
			{
				tables.add(f);
				from.append(e.getSchemaName() != null ? e.getSchemaName() + "." + f : f);
				++n;
			}
			
			if (!tables.contains(p))
			{
				tables.add(p);
				from.append(e.getSchemaName() != null ? e.getSchemaName() + "." + p : p);
				++n;
			}

			if (n >= 1)
				from.append(" ON ");
			else
				from.append(" AND ");
			
			from.append(toJoinCondition(e));
		}
		
		return from.toString();
	}
	
	private static void setAlias(DepictEdge e, DepictVertex v)
	{
		if (e.isRecursiveRelationship())
		{
			e.setPrimaryTableAlias(CteClassReference.addSuffixToTableName(e.getPrimaryTableName(), CteClassReference.PRIMARY_SUFFIX));
			e.setForeignTableAlias(CteClassReference.addSuffixToTableName(e.getForeignTableName(), CteClassReference.SECONDARY_SUFFIX));
		}
		else
		{
			if (e.getForeignTableName().equals(v.getName()))
				e.setForeignTableAlias(CteClassReference.addSuffixToTableName(e.getForeignTableName(), CteClassReference.PRIMARY_SUFFIX));
			else
				e.setPrimaryTableAlias(CteClassReference.addSuffixToTableName(e.getPrimaryTableName(), CteClassReference.PRIMARY_SUFFIX));
		}
	}
	
	public static String addFilter(String sql, String filter)
	{
		if (sql.contains("WHERE"))
		{
			sql += " AND " + filter;
			return sql;
		}
		
		sql += "WHERE " + filter;
		return sql;
	}
	
	//
	// Returns the list of vertices having outdegree(v) >= 1
	private static List<DepictVertex> getRootVertices(Graph<DepictVertex, DepictEdge> graph)
	{
		List<DepictVertex> roots = new ArrayList<DepictVertex>();
		
		//
		// In case we have a graph with a single node (so none edges at all)
		if (graph.getVertexCount() == 1)
		{
			Iterator<DepictVertex> it = graph.getVertices().iterator();
			roots.add(it.next());
			return roots;
		}
		
		for (DepictVertex v : graph.getVertices())
		{
			if (v.isOutputVertex())
				roots.add(v);
		}
		
		return roots;
	}

	public static String toWrappedSQLCount(String sql)
	{
		return String.format(SELECT_COUNT_WRAPPER, sql);
	}

	private static String toJoinCondition(DepictEdge e)
	{
		String t1; 
		String t2;
		
		t1 = e.getPrimaryTableAlias() != null ? e.getPrimaryTableAlias() : e.getPrimaryTableName();
		t2 = e.getForeignTableAlias() != null ? e.getForeignTableAlias() : e.getForeignTableName();
		
		String a = "";
		for (int i = 0; i < e.getForeignKeyColumns().size(); ++i)
		{
			String c1 = t1 + "." + e.getPrimaryKeyColumns().get(i);
			String c2 = t2 + "." + e.getForeignKeyColumns().get(i);
			if (a.length() == 0)
				a = c1 + "=" + c2;
			else
				a += " AND " + c1 + "=" + c2;
		}	

		return a;
	}

	private static String getPrimaryKeySelectList(Graph<DepictVertex, DepictEdge> graph)
	{
		String s = "";
		int i = 0;
		for (DepictVertex v : graph.getVertices())
		{
			if (v.isOutputVertex())
			{
				for (String pk : v.getPrimaryKeyFields())
				{
					if (s.length() > 0)
						s += ", ";
					
					String tableName;
					if (v.isRecursiveRelationship())
						tableName = CteClassReference.addSuffixToTableName(v.getId(), CteClassReference.PRIMARY_SUFFIX);
					else
						tableName = v.getId();
						
					s += String.format("%s.%s AS f_%d", tableName, pk, ++i);
				}
			}
		}
		return s.length() > 0 ? s : "*";
	}
}

