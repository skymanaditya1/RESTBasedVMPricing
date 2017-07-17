package com.vmware;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

class Resource {
	public int id;
	public long start;
	public long end;
	
	public Resource(int id, long start, long end) {
		this.id = id;
		this.start = start;
		this.end = end;
	}
}

public class ComputePrice {
	
	static Connection connection = null;
	static final String DB_NAME = "VMRecord";
	static final String DB_USER = "postgres";
	static final String DB_PASS = "helloaditya123";
	
	public static ArrayList<VM> computePrice(ClusterBean bean) {

		HashMap<Integer, ArrayList<Resource>> vmCluster = VMClusterMapping();
		int cluster_id = bean.getCluster_id();
		float cluster_price = bean.getCluster_price();
		ArrayList<VM> vmPrices = computePrices(vmCluster, cluster_id, cluster_price);
		return vmPrices;
	}
	
	// method to compute the VM prices
	public static ArrayList<VM> computePrices(HashMap<Integer, ArrayList<Resource>> vmCluster, 
			int cluster_id, float cluster_price){
		ArrayList<VM> vmPrices = new ArrayList<VM>();
		
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + DB_NAME, DB_USER, DB_PASS);
			Statement statement = connection.createStatement();
			String query = "SELECT VM_ID FROM VM";
			ResultSet resultSet = statement.executeQuery(query);
			while(resultSet.next()) {
				// compute the utilization Price
				float utilizationPrice = 0;
				int vm_id = resultSet.getInt(1);
				Statement statement1 = connection.createStatement();
				String query1 = "SELECT Resource_Type, Units, Start, Ends FROM Utilization WHERE VM_ID = " + vm_id;
				ResultSet resultSet1 = statement1.executeQuery(query1);
				while(resultSet1.next()) {
					long initialStart = resultSet1.getLong(3);
					long initialEnd = resultSet1.getLong(4);
					while(initialStart < initialEnd) {
						long tempStart = initialStart;
						long tempEnd = initialEnd;
						int i = 0;
						while(i<vmCluster.size() && min(vmCluster.get(vm_id).get(i).end, tempEnd) - max(vmCluster.get(vm_id).get(i).start, tempStart) <=0 )
							i += 1;
						int c_id = vmCluster.get(vm_id).get(i).id;
						tempStart = max(tempStart, vmCluster.get(vm_id).get(i).start);
						tempEnd = min(tempEnd, vmCluster.get(vm_id).get(i).end);
						
						// compute the price as price = price + (time) * units * cluster_price
						// the cluster price for all the resources remains the same
						// if the c_id = cluster_id
						if(c_id == cluster_id)
							utilizationPrice = utilizationPrice + (tempEnd - tempStart) * cluster_price * resultSet1.getFloat(2);
						initialStart = tempEnd;
					}
				}
				resultSet1.close();
				statement1.close();
				
				// compute the allocation Price
				float allocationPrice = 0;
				statement1 = connection.createStatement();
				query1 = "SELECT Resource_Type, Units, Start, Ends FROM Allocation WHERE VM_ID = " + vm_id;
				resultSet1 = statement1.executeQuery(query1);
				while(resultSet1.next()) {
					long initialStart = resultSet1.getLong(3);
					long initialEnd = resultSet1.getLong(4);
					while(initialStart < initialEnd) {
						long tempStart = initialStart;
						long tempEnd = initialEnd;
						int i = 0;
						while(i<vmCluster.size() && min(vmCluster.get(vm_id).get(i).end, tempEnd) - max(vmCluster.get(vm_id).get(i).start, tempStart) <= 0)
							i+=1;
						int c_id = vmCluster.get(vm_id).get(i).id;
						tempStart = max(tempStart, vmCluster.get(vm_id).get(i).start);
						tempEnd = min(tempEnd, vmCluster.get(vm_id).get(i).end);
						
						// update the allocation price if the c_id == cluster_id
						if(c_id == cluster_id)
							allocationPrice = allocationPrice + (tempEnd - tempStart) * cluster_price * resultSet1.getFloat(2);
						initialStart = tempEnd;
					}
				}
				resultSet1.close();
				statement1.close();
				
				// compute the property price
				float propertyPrice = 0;
				statement1 = connection.createStatement();
				query1 = "SELECT Property, Value, Start, Ends FROM Property WHERE VM_ID = " + vm_id;
				resultSet1 = statement1.executeQuery(query1);
				while(resultSet1.next()) {
					Statement statement2 = connection.createStatement();
					String query2 = "SELECT Price FROM PropertyPricing WHERE Property_Name LIKE '" + resultSet1.getString(1) + "' AND Property_Value LIKE '" + resultSet1.getString(2) + "'";
					ResultSet resultSet2 = statement2.executeQuery(query2);
					while(resultSet2.next()) {
						propertyPrice = propertyPrice + (resultSet1.getLong(4) - resultSet1.getLong(3)) * resultSet2.getFloat(1);
					}
					resultSet2.close();
					statement2.close();
				}
				
				resultSet1.close();
				statement1.close();
				
				vmPrices.add(new VM(vm_id, utilizationPrice, allocationPrice, propertyPrice));
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return vmPrices;
	}
	
	// method to find the mapping between VM and cluster
	public static HashMap<Integer, ArrayList<Resource>> VMClusterMapping(){
		HashMap<Integer, ArrayList<Resource>> clusterVM = 
				new HashMap<Integer, ArrayList<Resource>>();
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/" + DB_NAME, DB_USER, DB_PASS);
			Statement statement = connection.createStatement();
			String query = "SELECT VM_ID FROM VM";
			ResultSet resultSet = statement.executeQuery(query);
			while(resultSet.next()) {
				ArrayList<Resource> hosts = new ArrayList<Resource>();
				int vm_id = resultSet.getInt(1);
				Statement statement1 = connection.createStatement();
				String query1 = "SELECT Host_ID, Start, Ends FROM VMHost WHERE VM_ID = " + vm_id;
				ResultSet resultSet1 = statement1.executeQuery(query1);
				while(resultSet1.next()) {
					hosts.add(new Resource(resultSet1.getInt(1), resultSet1.getLong(2), resultSet1.getLong(3)));
				}
				
				ArrayList<Resource> clusters = new ArrayList<Resource>();
				// find out the cluster to which the hosts belong for the time interval
				for(Resource host : hosts) {
					Statement statement2 = connection.createStatement();
					String query2 = "SELECT Cluster_ID, Start, Ends FROM HostCluster WHERE Host_ID = " + host.id;
					ResultSet resultSet2 = statement2.executeQuery(query2);
					while(resultSet2.next()) {
						if(min(host.end, resultSet2.getLong(3)) - max(host.start, resultSet2.getLong(2)) > 0) {
							clusters.add(new Resource(resultSet2.getInt(1), max(host.start, resultSet2.getLong(2)), min(host.end, resultSet2.getLong(3))));
						}
					}
					resultSet2.close();
					statement2.close();
				}
				resultSet1.close();
				statement1.close();
				
				clusterVM.put(vm_id, clusters);
			}
			resultSet.close();
			statement.close();
			connection.close();
			System.out.println("Completed this successfully");
		} catch (SQLException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return clusterVM;
	}
	
	// method to find the min of two long numbers
	public static long min(long number1, long number2) {
		return number1 < number2 ? number1 : number2;
	}
	
	// method to find the max of two long numbers
	public static long max(long number1, long number2) {
		return number1 > number2 ? number1 : number2;
	}
}