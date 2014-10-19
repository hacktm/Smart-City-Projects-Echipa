package ro.hacktm.cashacab;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class ReaderCVS {

	 static List<Positions> lista;
	 public static void main(String[] args) {
		  lista = new ArrayList<Positions>();
		 
		 ReaderCVS obj = new ReaderCVS();
			obj.run();
		 
		  }
		 
		  public void run() {
		 
			String csvFile = "/home/aurel/trajectory.csv";
			BufferedReader br = null;
			String line = "";
			String cvsSplitBy = ",";
		 
			try {
		 int count=0;
				br = new BufferedReader(new FileReader(csvFile));
				while ((line = br.readLine()) != null) {
					Positions p = new Positions();
		 count ++;
				        // use comma as separator
					String[] country = line.split(cvsSplitBy);
					p.setLat(Double.valueOf(country[2]));
					p.setLng(Double.valueOf(country[3]));

					lista.add(p);
		 
					
		 
				}
		 System.out.println(count);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		 
			System.out.println("Done");
			System.out.println(lista);
			
		  }
		 
		}