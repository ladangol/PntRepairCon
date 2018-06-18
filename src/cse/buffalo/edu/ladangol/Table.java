package cse.buffalo.edu.ladangol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Table {
	private ArrayList<Tuple> table;
	private int switchpoint;
	private ArrayList<Integer> infintindexes;
	private boolean isConsistant;
	public Table(){
		table = new ArrayList<>();
		switchpoint = Integer.MIN_VALUE;
	}
	public Table(int card){
		table = new ArrayList<>(card);
		switchpoint = Integer.MIN_VALUE;
	}
	public void add(Tuple tup){
		table.add(tup);
	}

	public int Cardinality(){
		return table.size();
	}
	public Tuple getTuple(int index){
		return table.get(index);
	}
	public void reduce(){
		
		findSwitchPoint();
		//Now reduce by e^* = switchpoint
		for(int i=0; i < infintindexes.size(); i++){
			Tuple temp = table.get(infintindexes.get(i));
			temp.setEndPoint(switchpoint+1);
		}
	}
	private void findSwitchPoint() {
		//First finding the switchpoint
				infintindexes = new ArrayList<Integer>();
				for(int i=0; i<table.size(); i++){
					Tuple tup = table.get(i);
					if(tup.isEndPointInfinite()){
						infintindexes.add(i);
						if(tup.getStartPoint() > switchpoint){
							switchpoint = tup.getStartPoint();
						}
					}
					else if(tup.getEndPoint() > switchpoint){
						switchpoint = tup.getEndPoint();
					}
						
				}
				
		
	}

	public void coalesce(){
		HashMap<String, ArrayList<Interval<Integer, Integer>>> cotbl = new HashMap<String, ArrayList<Interval<Integer,Integer>>>();
		//building the hash : tuples with the same data attributes are going to one key
		for(int i=0; i<table.size(); i++){
			Tuple tup = table.get(i);
			String[] dataatt = tup.getDataAttrs();
			//I have to learb how key can be String[] later
			String key = convertStringArrayToString(dataatt, ";");
			Interval<Integer, Integer> inv = Interval.of(tup.getStartPoint(), tup.getEndPoint());
			if(cotbl.containsKey(key)){
				ArrayList<Interval<Integer, Integer>> value = cotbl.get(key);
				value.add(inv);
				cotbl.put(key,value);				
			}
			else{
				ArrayList<Interval<Integer, Integer>> value = new ArrayList<Interval<Integer, Integer>>();
				value.add(inv);
				cotbl.put(key, value);
			}
			
				
	   }
	  // Now we need to sort each List of each key
		//Iterate over the hashmap and sort, then coalece 
		ArrayList<Tuple> tbl = new ArrayList<>();
		 Set<String> keySet = cotbl.keySet();
		 Iterator<String> keySetIterator = keySet.iterator();
		 while (keySetIterator.hasNext()) {
		    String key = keySetIterator.next();
		    ArrayList<Interval<Integer, Integer>> value = cotbl.get(key);
		    
		    Collections.sort(value, new IntervalComparator());
		    Iterator<Interval<Integer, Integer>> iter = value.iterator();
		    Interval<Integer, Integer> current = iter.next();
		    int startpoint = (Integer) current.sp;
		    Interval<Integer, Integer> next =null;
		    String[] data = key.split(";");
		    while(iter.hasNext())
		    {
		    	next = iter.next();
		    	if(!current.ep.equals(next.sp)){
		    		
		    		Tuple tup= new Tuple(data, startpoint,current.ep);
		    		tbl.add(tup);
		            startpoint = next.sp;
		    	}
		  
		   		current = next;
		    }
		    int endpoint;
		    if(next == null){
		    	endpoint = current.ep;
		    }
		    else{
		    	endpoint = (int) next.ep;
		    }
		    
		    Tuple tup = new Tuple(data,startpoint, endpoint);
		    tbl.add(tup);
		 }
		 table.clear();
		 table.addAll(tbl);
	}
	public void expand(){
		for(int i=0; i < infintindexes.size(); i++){
			Tuple temp = table.get(infintindexes.get(i));
			temp.setEndPoint(Integer.MAX_VALUE);
		}
		
	}
	public void PntRCon(ArrayList<tFD> tFDs){ 
		ArrayList<Tuple> repair = new ArrayList<Tuple>();
		Collections.shuffle(table);
		//Collections.shuffle(instance);
		Collections.shuffle(tFDs);
		List<Map<String, String>> tFDsMaps = new ArrayList<Map<String, String>>(tFDs.size());
		for (int i=0; i< tFDs.size(); i++){
			tFDsMaps.add(new HashMap<String, String>());
		}
		for(int t=0; t < table.size(); t++ ){
			Tuple tup = table.get(t);
			for(int currentpoint = tup.getStartPoint(); currentpoint < tup.getEndPoint(); currentpoint++){
				for(int d = 0; d <tFDs.size(); d++){
					HashMap<String, String> tfdMap = (HashMap<String, String>) tFDsMaps.get(d);
					tFD tfd = tFDs.get(d);
					int[] lhs = tfd.getlhs();
					StringBuilder key = new StringBuilder();
					for(int i = 0; i< lhs.length; i++){
						//lhs[i]-1  because use start from index 1
						key.append(tup.getDataAttr(lhs[i]-1).toLowerCase());
					}
					key.append(currentpoint); //adding the temporal attribute
					int[] rhs = tfd.getrhs();
					StringBuilder value = new StringBuilder();
					for(int i=0; i<rhs.length; i++){
						value.append(tup.getDataAttr(rhs[i]-1).toLowerCase());
					}
					//So if I do not put this bloody .toString it never understands they are equal
					//That is why types are super important. 
					if(tfdMap.containsKey(key.toString())){
						if(tfdMap.get(key.toString()).equals(value.toString())){
							//Data base still consistant
							//building the concrete tuple
							Tuple reptup = new Tuple(tup.getDataAttrs(), currentpoint, currentpoint+1);
							repair.add(reptup);

						}
						else{
							isConsistant = false;
						}
					}
					else{
						
						tfdMap.put(key.toString(), value.toString());
						Tuple reptup = new Tuple(tup.getDataAttrs(), currentpoint, currentpoint+1);
						repair.add(reptup);
					}
				}
			}
		}
		table.clear();
		table.addAll(repair);

			
}
	private static String convertStringArrayToString(String[] strArr, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (String str : strArr) 
			sb.append(str).append(delimiter);
		return sb.substring(0, sb.length() - 1);
	}

}
	
	


