package com.exedosoft.plat.login.zidingyi;


import java.sql.Connection;


import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOBO;
import com.exedosoft.plat.bo.DOService;

import com.exedosoft.plat.login.zidingyi.excel.MySqlOperation;
import com.exedosoft.plat.util.DOGlobals;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class DelExcelFile extends DOAbstractAction {

	public String excute() {
		String path = DOGlobals.getInstance().getValue("uploadfiletemp");
		DOBO  theBO = DOBO.getDOBOByName("gz_salarymessage");
		BOInstance bi = theBO.getCorrInstance();
		String fileName = bi.getValue("name");
		String filePath = path + fileName;
		
		String deltype = DOGlobals.getInstance().getSessoinContext()
		.getFormInstance().getValue("deltype");
		
//		System.out.println("path+++++++++++++++++++=" + path);
//		System.out.println("fileName+++++++++++++++=" + fileName);
//		System.out.println("filePath+++++++++++++++=" + filePath);
		try {
			
			if("all".equals(deltype)) {
				//ɾ���ļ�
				if(FileOperation.isExistsFile(filePath)) {
					/**
					 * @param type:0,�ļ�������;1,�ļ�ΪĿ¼;2,�ļ��ɹ�ɾ��;3,�ļ�ɾ��ʧ��
					 */
					int type = FileOperation.delFile(filePath);
					if(type == 3) {
						return "�ļ��޷�ɾ����";
					}
				} 
				
				//ɾ����¼
				DOService delSer = service.getService("gz_salarymessage_delete_xlsAndData");
				if(delSer != null) {
					delSer.invokeUpdate(bi.getValue("month"));
					return "�ѳɹ�ɾ���ļ������ݼ�¼��";
				}
			} else if("file".equals(deltype)) {
				//ɾ���ļ�
				if(FileOperation.isExistsFile(filePath)) {
					/**
					 * @param type:0,�ļ�������;1,�ļ�ΪĿ¼;2,�ļ��ɹ�ɾ��;3,�ļ�ɾ��ʧ��
					 */
					int type = FileOperation.delFile(filePath);
					if(type == 3) {
						return "�ļ��޷�ɾ����";
					}
				} 
				
				//ɾ����¼
				DOService delSer = service.getService("gz_salarymessage_delete");
				if(delSer != null) {
					delSer.invokeUpdate(bi.getValue("objuid"));
					return "�ѳɹ�ɾ���ļ������¼��";
				}
			} else if("data".equals(deltype)) {
				//ɾ����¼
				Connection conn = MySqlOperation.getConnection();
				int datasNumber = MySqlOperation.SMRemoveByDate(conn, bi.getValue("month"));
				conn.close();
				return "�ѳɹ�ɾ�����·ݵ��������ݼ�¼����" + datasNumber + "����";
			} 
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "�޷�ɾ����";
		}
		return "ɾ������";
	}

	
	public static void main(String[] args) {
		
	}

}
