package com.exedosoft.plat.login.zidingyi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.login.zidingyi.excel.MySqlOperation;

public class RemoveGZmessage extends DOAbstractAction {

	public String excute() {

		// ���յĲ���
		String resname = null;
		// ɾ���ļ�¼��;
		int removeNumber = 0;
		Connection conn = MySqlOperation.getConnection();
		List users = new ArrayList();
		try {
			users = service.invokeSelect();
			// String user = service.invokeSelectGetAValue();
			// users.add(user);

		} catch (Exception e) {
			this.setEchoValue("���չ�������Ϣʧ�ܣ�error" + e.toString());
			return this.DEFAULT_FORWARD;
			//return "���չ�������Ϣʧ�ܣ�";
		}

//		System.out.println("====================");
//		System.out.println(users);
//		System.out.println("====================");
		
		// �������е����ݣ�
		if (users != null && users.size() > 0) {
			BOInstance bo = (BOInstance) users.get(0);
			String smonth = bo.getValue("month");
			
			resname = bo.getValue("name");

//			System.out.println("====================");
//			System.out.println(smonth);
//			System.out.println(resname);
//			System.out.println("====================");
			
			try {
				if (smonth != null
						&& (resname == null || resname.length() <= 0)) {
					removeNumber = MySqlOperation
							.SMRemoveByDate(conn, smonth);
				} else if (smonth == null && resname != null
						&& resname.length() > 0) {
					removeNumber = MySqlOperation.SMRemoveByName(conn, resname);
				} else if (smonth != null && resname != null
						&& resname.length() > 0) {
					removeNumber = MySqlOperation.SMRemoveByNameAndDate(conn,
							resname, smonth);
				} else if (smonth == null
						&& (resname == null || resname.length() <= 0)) {
					this.setEchoValue("��ѡ��������");
//					System.out.println("====================");
//					System.out.println("====================");
//					System.out.println("====================");
//					System.out.println("====================");
					return this.DEFAULT_FORWARD;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			System.out.println("====================");
//			System.out.println(removeNumber);
//			System.out.println("====================");
			
			if (removeNumber > 0)
				this.setEchoValue("�ɹ�ɾ��" + removeNumber + "����¼��");
			else 
				this.setEchoValue("û�з��������ļ�¼��");
			
			return this.DEFAULT_FORWARD;
		} else {
			this.setEchoValue("ɾ��ʧ��");
			return this.DEFAULT_FORWARD;
			//return "ɾ��ʧ��";
		}
	}

	public static void main(String[] args) {

	}

}
