<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Compute VM Price</title>
</head>
<body>
	<h3>Compute VM Price</h3>
	<form action="computeprice.jsp" method="get">
		<p>Cluster ID : <select name="cluster_id">
			<option value="7">7</option>
			<option value="8">8</option>
		</select></p>
		<p>Cluster Price : <input type="text" name="cluster_price" /></p>
		<input type="submit" value="Compute Price" />
	</form>
</body>
</html>