<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" import="com.vmware.*" import="java.util.ArrayList"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Computing Cluster Prices</title>
</head>
<body>
	<jsp:useBean id="cluster" class="com.vmware.ClusterBean" />
	<jsp:setProperty property="*" name="cluster" />
	<%
		ArrayList<com.vmware.VM> vmPrices  = ComputePrice.computePrice(cluster);
		out.println("Cluster ID : " + cluster.getCluster_id() + "\n");
		out.println("Cluster Price : " + cluster.getCluster_price() + "\n");
		for(int i=0; i<vmPrices.size(); i++){
			out.println("VM ID : " + vmPrices.get(i).id + ", Utilization Price : " + vmPrices.get(i).utilizationPrice 
				+ ", Allocation Price : " + vmPrices.get(i).allocationPrice + ", Property Price : " + vmPrices.get(i).propertyPrice + "\n");
		}
	%>
</body>
</html>