package no.simula.depict.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class GenerateData 
{
	private static final int KUNDENR_MAX = 2400000;
	private static final int INNFDEKL_MAX = 300000;
	private static final int LAND_MAX = 236;
	private static final int VARELINJ_MAX = 1800000;
	private static final int REGEL_MAX = 1100;
		
	/**
	 * @param args
	 */
	
	public static void main(String[] args) 
	{
		try 
		{
			Class.forName("com.sybase.jdbc4.jdbc.SybDriver").newInstance();
			String url = "jdbc:sybase:Tds:localhost:2638/TVINN";
			Connection cn = DriverManager.getConnection(url, "dba", "sql");
			/*
			Class.forName("org.postgresql.Driver").newInstance();
			String url = "jdbc:postgresql://localhost:5432/tvinn";
			Connection cn = DriverManager.getConnection(url, "postgres", "postgres");
			*/
			// 1)
			/*
			generateKunde(cn);
			
			// 2)
			generateOmrade(cn);

			// 3)
			generateRegel(cn);
			
			// 4)
			generateRegkobl(cn);
			
			//
			// 5)
			generateMedland(cn);
			
			//
			// 6)
			generateUnntdok(cn);

			//
			// 7)
			generateInnfdekl(cn);
			
			//
			// 8)
			generateVarelinj(cn);
			*/
			//
			// 9)
			generateAvglinje(cn);
			
			cn.close();
		} 
		catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private static void generateOmrade(Connection cn)
	{
		try 
		{
			cn.setAutoCommit(false);
		 
			String cmd = "INSERT INTO OMRADE (OMRADEKO) VALUES (?)";				
			PreparedStatement st = cn.prepareStatement(cmd);
			
			for (long i = 1; i <= 110; ++i)
			{
				st.setString(1, String.format("%04d", i));
				st.addBatch();
				if (i % 10 == 0)
				{
					st.executeBatch();
					cn.commit(); 
					System.out.println(String.format("Created %d OMRADE recs.", i));
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void generateRegel(Connection cn)
	{
		String [] codes = {"M2", "VT", "V5", "I2", "MV", "FA", "TL"};
		
		try 
		{
			cn.setAutoCommit(false);
		 
			String cmd = "INSERT INTO REGEL (OMRADEKO,REGELKOD,RSEKVENS) VALUES (?,?,?)";				
			PreparedStatement st = cn.prepareStatement(cmd);

			for (int i = 1; i <= REGEL_MAX; ++i)
			{
				st.setString(1, String.format("%04d", random(1, 110)));
				st.setString(2, codes[random(0, codes.length - 1)]);
				st.setInt(3, i);	
				st.addBatch();
				if (i % 100 == 0)
				{
					st.executeBatch();
					cn.commit(); 
					System.out.println(String.format("Created %d REGEL recs.", i));
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void generateMedland(Connection cn)
	{
		try 
		{
			cn.setAutoCommit(false);
		 
			String cmd = "INSERT INTO MEDLLAND (OMRADEKO,LANDKODE,FOMDATO) VALUES (?,?,?)";				
			PreparedStatement st = cn.prepareStatement(cmd);
			
			String omradeko;
			String regelkod;
			ResultSet rs1 = getResultSet(cn, "SELECT * FROM OMRADE;");
			ResultSet rs2 = getResultSet(cn, "SELECT * FROM LAND;");
			
			for (int i = 1; i <= 2000; ++i)
			{
				rs1.absolute(random(1, 110));
				rs2.absolute(random(1, LAND_MAX));
				omradeko = rs1.getString("OMRADEKO");
				regelkod = rs2.getString("LANDKODE");
				st.setString(1, omradeko);
				st.setString(2, regelkod);
				st.setInt(3, i);
				
				st.addBatch();
				if (i % 1000 == 0)
				{
					st.executeBatch();
					cn.commit(); 
					System.out.println(String.format("Created %d MEDLAND recs.", i));
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void generateRegkobl(Connection cn)
	{
		try 
		{
			cn.setAutoCommit(false);
		 
			String cmd = "INSERT INTO REGKOBL (TARIFFID,FOMDATO,OMRADEKO,REGELKOD,RSEKVENS) VALUES (?,?,?,?,?)";				
			PreparedStatement st = cn.prepareStatement(cmd);
			
			String omradeko;
			String regelkod;
			int rsekvens;
			ResultSet rs = getResultSet(cn, "SELECT * FROM REGEL;");
			
			for (long i = 1; i <= 250000; ++i)
			{
				rs.absolute(random(1, 1100));
				st.setLong(1, i);
				st.setLong(2, 0);
				omradeko = rs.getString("OMRADEKO"); //String.format("%04d", random(1, 110));
				regelkod = rs.getString("REGELKOD"); //codes[random(0, codes.length - 1)];
				rsekvens = rs.getInt("RSEKVENS"); //random(1, 1100);
				st.setString(3, omradeko);
				st.setString(4, regelkod);
				st.setInt(5, rsekvens);
				
				st.addBatch();
				if (i % 1000 == 0)
				{
					st.executeBatch();
					cn.commit(); 
					System.out.println(String.format("Created %d REGKOBL recs.", i));
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void generateUnntdok(Connection cn)
	{
		try 
		{
			cn.setAutoCommit(false);
		 
			String cmd = "INSERT INTO UNNTDOK (JNRUNNT, KUNDENR, OMRADEKO) VALUES (?,?,?)";				
			PreparedStatement st = cn.prepareStatement(cmd);
			
			String omradeko;
			int kundenr;
			ResultSet rs = getResultSet(cn, "SELECT OMRADEKO FROM OMRADE;");
			
			for (long i = 1; i <= 92000; ++i)
			{
				rs.absolute(random(1, 110));
				st.setString(1, String.format("%010d", i));
				kundenr = random(1, 2400000);
				omradeko = rs.getString("OMRADEKO");
				st.setInt(2, kundenr);
				st.setString(3, omradeko);
				
				st.addBatch();
				if (i % 1000 == 0)
				{
					st.executeBatch();
					cn.commit(); 
					System.out.println(String.format("Created %d UNNTDOK recs.", i));
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void generateInnfdekl(Connection cn)
	{
		String [] deklretn = {"I", "U"};
		String [] kategori = {"FU", "MA", "EN"};
		String [] betkode = {"M", "D", "K"};
		String [] lagrsted = {"X", "A", "B", "C", "D"};
		
		try 
		{
			cn.setAutoCommit(false);
		 
			String cmd = "INSERT INTO INNFDEKL (KUNDENR, KUNDENRA, KUNDENRI, DEKLDATO, DEKLSEKV, VERSNR, DEKLRETN, KATEGORI, HERVANG, BETKODE, LAGRSTED, LANDKODE, LANDKODA) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";				
			PreparedStatement st = cn.prepareStatement(cmd);
			
			ResultSet rs = getResultSet(cn, "SELECT LANDKODE FROM LAND;");
			
			for (long i = 1; i <= INNFDEKL_MAX; ++i)
			{
				rs.absolute(random(1, LAND_MAX));
				String landkode = rs.getString("LANDKODE");
				rs.absolute(random(1, LAND_MAX));
				String landkodea = rs.getString("LANDKODE");
				
				st.setInt(1, random(1, KUNDENR_MAX));
				st.setInt(2, random(1, KUNDENR_MAX));
				st.setInt(3, random(1, KUNDENR_MAX));
				st.setLong(4, i);
				st.setLong(5, random(1, 2000000));
				st.setLong(6, random(1, 10));
				st.setString(7, deklretn[random(0, deklretn.length - 1)]);
				st.setString(8, kategori[random(0, kategori.length - 1)]);
				st.setInt(9, random(0, 9));
				st.setString(10, betkode[random(0, betkode.length - 1)]);
				st.setString(11, lagrsted[random(0, lagrsted.length - 1)]);
				st.setString(12, landkode);
				st.setString(13, landkodea);
				
				st.addBatch();
				if (i % 1000 == 0)
				{
					st.executeBatch();
					cn.commit(); 
					System.out.println(String.format("Created %d INNFDEKL recs.", i));
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private static void generateVarelinj(Connection cn)
	{
		String [] tariffid = {"96034000", "04029900", "00000011", "00000055", "02031101", "00000077", "00000088", "02031109", "03021109", "00000000"};
		String [] prosedyr = {"40", "66", "72", "73", "70", "10", "04", "01", "03", "06", "24", "00"};
		String [] preftoll = {"N", "0"};
		
		try 
		{
			cn.setAutoCommit(false);
		 
			String cmd = "INSERT INTO VARELINJ (KUNDENR,DEKLDATO,DEKLSEKV,VERSNR,VLINJENR,TARIFFID,LANDKODE,PROSEDYR,PREFTOLL) VALUES (?,?,?,?,?,?,?,?,?)";				
			PreparedStatement st = cn.prepareStatement(cmd);
			
			ResultSet rs1 = getResultSet(cn, "SELECT LANDKODE FROM LAND;");
			ResultSet rs2 = getResultSet(cn, "SELECT KUNDENR,DEKLDATO,DEKLSEKV,VERSNR FROM INNFDEKL;");
			long err = 0;
			long n = 0;
			for (long i = 1; i <= INNFDEKL_MAX; ++i)
			{
				rs1.absolute(random(1, LAND_MAX));
				String landkode = rs1.getString("LANDKODE");

				rs2.absolute((int)i);
				int kundenr = rs2.getInt("KUNDENR");
				int dekldato = rs2.getInt("DEKLDATO");
				int deklsekv = rs2.getInt("DEKLSEKV");
				int versnr = rs2.getInt("VERSNR");
				
				for (int j = 1; j <= 6; ++j)
				{
					st.setLong(1, kundenr);
					st.setLong(2, dekldato);
					st.setLong(3, deklsekv);
					st.setLong(4, versnr);
					st.setLong(5, j);
					st.setString(6, tariffid[random(0, tariffid.length - 1)]);
					st.setString(7, landkode);
					st.setString(8, prosedyr[random(0, prosedyr.length - 1)]);
					st.setString(9, preftoll[random(0, preftoll.length - 1)]);
	
					/*
					try
					{
						st.execute();
						cn.commit();
						if (i % 1000 == 0)
							System.out.println(String.format("Created %d VARELINJ recs.", i));
					}
					catch (Exception e)
					{
						++err;
					}
					*/
					++n;
					st.addBatch();
					if (n % 1000 == 0)
					{
						st.executeBatch();
						cn.commit(); 
						System.out.println(String.format("Created %d VARELINJ recs.", n));
					}
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	private static void generateAvglinje(Connection cn)
	{
		int [] rsekvens = {1, 100, 2, 0};
		
		try 
		{
			//cn.setAutoCommit(false);
		 
			String cmd = "INSERT INTO AVGLINJE (KUNDENR,DEKLDATO,DEKLSEKV,VERSNR,VLINJENR,LOPENR,REGELKOD,RSEKVENS) VALUES (?,?,?,?,?,?,?,?)";				
			PreparedStatement st = cn.prepareStatement(cmd);
			
			ResultSet rs1 = getResultSet(cn, "SELECT REGELKOD FROM REGEL;");
			ResultSet rs2 = getResultSet(cn, "SELECT KUNDENR,DEKLDATO,DEKLSEKV,VERSNR,VLINJENR FROM VARELINJ;");
			for (long i = 554001; i <= VARELINJ_MAX; ++i)
			{
				rs1.absolute(random(1, REGEL_MAX));
				String regelkode = rs1.getString("REGELKOD");

				rs2.absolute((int)i);
				int kundenr = rs2.getInt("KUNDENR");
				int dekldato = rs2.getInt("DEKLDATO");
				int deklsekv = rs2.getInt("DEKLSEKV");
				int versnr = rs2.getInt("VERSNR");
				int vlinjer = rs2.getInt("VLINJENR");
				
				st.setLong(1, kundenr);
				st.setLong(2, dekldato);
				st.setLong(3, deklsekv);
				st.setLong(4, versnr);
				st.setLong(5, vlinjer);
				st.setLong(6, 1);
				st.setString(7, regelkode);
				st.setInt(8, rsekvens[random(0, rsekvens.length - 1)]);

				st.addBatch();
				if (i % 1000 == 0)
				{
					st.executeBatch();
					cn.commit(); 
				}
				
				if (i % 1000 == 0)
					System.out.println(String.format("Created %d AVGLINJE recs.", i));
				
				/*
				if (i % 100000 == 0)
				{
					st.close();
					cn.close();

					String url = "jdbc:sybase:Tds:localhost:2638/TVINN";
					cn = DriverManager.getConnection(url, "dba", "sql");
					st = cn.prepareStatement(cmd);
					rs1 = getResultSet(cn, "SELECT REGELKOD FROM REGEL;");
					rs2 = getResultSet(cn, "SELECT KUNDENR,DEKLDATO,DEKLSEKV,VERSNR,VLINJENR FROM VARELINJ;");
				}
				*/	
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	private static ResultSet getResultSet(Connection cn, String sql) throws SQLException
	{
		PreparedStatement st = cn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs = st.executeQuery();
		return rs;
	}
	
	@SuppressWarnings("unused")
	private static void generateKunde(Connection cn)
	{
		try 
		{
			cn.setAutoCommit(false);
		 
			String cmd = "INSERT INTO KUNDE (KUNDENR) VALUES (?)";				
			PreparedStatement st = cn.prepareStatement(cmd);
			
			for (long i = 1; i <= KUNDENR_MAX; ++i)
			{
				st.setLong(1, i);
				st.addBatch();
				if (i % 1000 == 0)
				{
					st.executeBatch();
					cn.commit(); 
					System.out.println(String.format("Created %d customers", i));
				}
			}
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	private static int random(int min, int max)
	{
		return min + (int)(Math.random() * ((max - min) + 1));		
	}
}
