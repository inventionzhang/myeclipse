package org.lmars.network.entity;

public enum DataBaseType {
	POSTGRESQL("postgresql"),
	ORACLE("oracle"),
	MYSQL("mysql"),
	NONE("none");
	
	private final String type;
	
	DataBaseType(String type){
		this.type = type.toLowerCase();
	}
	
	public static DataBaseType getTypeFromString(String type){
		type = type.toLowerCase();
		for(DataBaseType dl:DataBaseType.values()){
			if(dl.type.equals(type)){
				return dl;
			}
		}
		return DataBaseType.NONE;
	}
	
	public String getType() {
		return type;
	}
}
