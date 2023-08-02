package com.hc360.rsf.registry;

/**
 * 一个服务提供者 
 * 
 * @author zhaorai
 *
 */
public class Provider {
	String serviceName;//服务名
	String groupName;//组名，目前未使用
	String version;//版本号，目前未使用
	String ip;//ip
	int port;//端口
	
	
	//-----以下值是属性，不参与equals方法的比较---------
	int weight =100;//默认值1000
	
	 public String getServiceKey() {
	        String inf = getServiceName();
	        if (inf == null) return null;
	        StringBuilder buf = new StringBuilder();
	        String group = getGroupName();
	        if (group != null && group.length() > 0) {
	            buf.append(group).append("/");
	        }
	        buf.append(inf);
	        String version = getVersion();
	        if (version != null && version.length() > 0) {
	            buf.append(":").append(version);
	        }
	        return buf.toString();
	    }


	public String toString(){
		StringBuilder sbl=new StringBuilder();
		sbl.append("Provider[");
		sbl.append("serviceName:");
		sbl.append(serviceName);
		sbl.append(",ip:");
		sbl.append(ip);
		sbl.append(",port:");
		sbl.append(port);
		sbl.append(",groupName:");
		sbl.append(groupName);
		sbl.append(",version:");
		sbl.append(version);
		sbl.append("]");
		return sbl.toString();
	}
	
	/**  
	 * 重写了equals方法 ，用于比较两个Provider对象是否相同.
	 * 
	 * 两个provicer对象，各个属性值都都相等，被初为“相等”。
	 * null == null
	 * "abc" == "abc"
	 * 1 = 1
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj!=null && obj instanceof Provider){
			Provider p=(Provider)obj;
			
			//比较serviceName属性
			if(this.getServiceName()!=null){
				if(!this.getServiceName().equals(p.getServiceName())){
					return false;
				}
			}else{
				if(p.getServiceName()!=null){
					return false;
				}
			}

			//比较version属性
			if(this.getVersion()!=null ){
				if(!this.getVersion().equals(p.getVersion())){
					return false;
				}
			}else{
				if(p.getVersion()!=null){
					return false;
				}
			}
			
			//比较getGroupName属性
			if(this.getGroupName()!=null ){
				if(!this.getGroupName().equals(p.getGroupName())){
					return false;
				}
			}else{
				if(p.getGroupName()!=null){
					return false;
				}
			}
			
			//比较getIp属性
			if(this.getIp()!=null ){
				if(!this.getIp().equals(p.getIp())){
					return false;
				}
			}else{
				if(p.getIp()!=null){
					return false;
				}
			}
			
			//比较getPort属性
			if(this.getPort()!=p.getPort() ){
				return false;
			}
			
			//各个属性都遍历完了，能都走到这里说明，两个对象的值相同
			return true;
			
		}else{
			return false;
		}
	}


	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
	
}
