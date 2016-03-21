package com.exedosoft.plat.ui.jquery.form;

import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.ui.DOFormModel;
import com.exedosoft.plat.ui.DOIModel;

public class Warnlight extends DOBaseForm {
	
	
	
	public String getHtmlCode(DOIModel iModel){
		DOFormModel property = (DOFormModel) iModel;
		return fromSelectStr(property);
	}
	
	String fromSelectStr(DOFormModel property){
		
		//get value from grid option
		String value = property.getValue();
		value = property.getData().getValue(
				property.getRelationProperty().getColName());
		double salary = Double.parseDouble(value);
		
		//warning png
		String level1 = "images/level1.png";		
		String level2 = "images/level2.png";
		String level3 = "images/level3.png";
		
		String light = "";
		
		StringBuffer ss = new StringBuffer();
		
		if(value != null && !value.trim().equals("")){
			if((salary >= 1)&&(salary <= 3000)){
				light = level1;
			}
			if((salary > 3000)&&(salary <= 6000)){
				light = level2;
			}
			if((salary >= 6000)&&(salary <= 10000)){
				light = level3;
			}
		
		}
		String img = "<img src=\""+ light +"\"></img>";
		ss.append("<span style='").append(property.getStyle())
		  .append("'>")
		  .append(img)
		  .append("</span>");
		return ss.toString();
	}
	private void holo(){
		// 淘宝放大镜实现
		byte a= 1;
		DOService aService = DOService.getService("student.list");
	}
}
