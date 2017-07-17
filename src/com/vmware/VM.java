package com.vmware;

public class VM{
	public int id;
	public float utilizationPrice;
	public float allocationPrice;
	public float propertyPrice;
	
	public VM(int id, float utilizationPrice, float allocationPrice, float propertyPrice) {
		this.id = id;
		this.utilizationPrice = utilizationPrice;
		this.allocationPrice = allocationPrice;
		this.propertyPrice = propertyPrice;
	}
}