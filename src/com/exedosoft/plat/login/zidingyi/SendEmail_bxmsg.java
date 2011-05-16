package com.exedosoft.plat.login.zidingyi;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.login.zidingyi.excel.MySqlOperationII;

/**
 * 
 * this page must be the first page of huidian system. the classify default
 * config must initiazation.
 * 
 * @author aa
 * 
 */
public class SendEmail_bxmsg extends DOAbstractAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	public String excute() {
//		System.out.println("======================================");
//		System.out.println("======================================");
		String baoxiaoempuid = null;// ������;
		String baoxiaoid = null;// ���������;
		String baoxiaostate = null;// ������״̬;
		
		String receivepeople = null;// �����˲����쵼(����)����һ��������;
		
		//�����ʼ����Թ���Ա��ݷ���
		String manager_email = "yxxts_zep@163.com";// ����Ա�����ַ;
		String baoxiaoemail = null;// �����˵�ַ;
		 String emailTo = null;// �����˵�ַ ;

		List users = new ArrayList();
		
		try {
			users = service.invokeSelect();
		} catch (Exception e) {
			//return this.DEFAULT_FORWARD;
			this.setEchoValue("error" + e.toString());
			return "notpass";
		}

		// �ж��Ƿ������ݽ��ܣ�
		if (users != null && users.size() > 0) {
			 // �������е����ݣ�
			
			 for (int n = 0; n < users.size(); n++) {
			 String s = users.get(n).toString();
			 String st = s.substring(s.indexOf("{") + 1, s.lastIndexOf("}"));
			 String[] sarray = st.split(",");
							
			 
			
			
			 // ��ÿ�����ݽ��д���ȡ����Ч���ԣ�
			 for (int i = 0; i < sarray.length; i++) {
			 String temp = sarray[i];
			 String[] nv = temp.split("=");
			
			 if (nv.length == 2 && "baoxiaoempuid".equals(nv[0].trim()))
			 baoxiaoempuid = nv[1];
			 if (nv.length == 2 && "baoxiaoid".equals(nv[0].trim()))
			 baoxiaoid = nv[1];
			 if (nv.length == 2 && "receivepeople".equals(nv[0].trim()))
			 receivepeople = nv[1];
			 if (nv.length == 2 && "baoxiaostate".equals(nv[0].trim()))
			 baoxiaostate = nv[1];			
			}
			 
			 /**
			  * // ȡ�������ַ;
			  */
			 
			// �����������ַ
//			 	String sn = LDAPPeopleUtil.getLDAPSnByUid(baoxiaoempuid);
//			 	if(sn == null || sn.trim().equals("")) {
//			 		baoxiaoemail = LDAPPeopleUtil.getLDAPEmailBySN(baoxiaoempuid);
//			 	} else {
//			 		baoxiaoemail = LDAPPeopleUtil.getLDAPEmailBySN(sn);
//			 	}
			 
			 String sn = baoxiaoempuid;
			 	
			 	DOService empser = DOService.getService("zf_employee_browse");
				List<BOInstance> emp = new ArrayList<BOInstance>();
				emp = empser.invokeSelect(baoxiaoempuid); 
				if(emp.size()>0) {
					BOInstance empBi = emp.get(0);
					sn = empBi.getValue("sn");
					baoxiaoemail = empBi.getValue("mail");
				}	
//				
//				// �����������ַ
//			 	String sn1 = LDAPPeopleUtil.getLDAPSnByUid(receivepeople);
//			 	if(sn1 == null || sn1.trim().equals("")) {
//			 		emailTo = LDAPPeopleUtil.getLDAPEmailBySN(receivepeople);
//			 	} else {
//			 		emailTo = LDAPPeopleUtil.getLDAPEmailBySN(sn1);
//			 	}
				 String sn1 = baoxiaoempuid;
				 	
				 	DOService empser1 = DOService.getService("zf_employee_browse");
					List<BOInstance> emp1 = new ArrayList<BOInstance>();
					emp1 = empser1.invokeSelect(receivepeople); 
					if(emp1.size()>0) {
						BOInstance empBi = emp.get(0);
						sn1 = empBi.getValue("sn");
						emailTo = empBi.getValue("mail");
					}	

			
			//LDAP sn ȡ��cn	
//			String baoxiaoemp =  LDAPPeopleUtil.getLDAPCNBySN(baoxiaoempuid);
			
			//do_org_user_link user_uid ȡ��user_cn		
			String baoxiaoemp = null;
			try {
				Connection conii = MySqlOperationII.getConnection();
				baoxiaoemp =  MySqlOperationII.getUserCNByUserUid(conii, baoxiaoempuid);
				conii.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			/**
			 * �����ʼ����������
			 */
			// �ύ�������ı�����
			String emailTitle = "���д������ı�����";// �ʼ�����;
			String emailText = "���������: " + baoxiaoid + "��\n������: "
					+ baoxiaoemp + "��\n������״̬�� " + baoxiaostate + "��\n����������";// �ʼ�����;

			// �˻صı�����
			String emailTitle_back = "���ı��������˻�";// �ʼ�����;
			String emailText_back = "���������: " + baoxiaoid + "��\n������: "
					+ baoxiaoemp + "��\n������״̬�� " + baoxiaostate + "��";// �ʼ�����;

			// ����ͨ���ı�����
			String emailTitle_success = "���ı�����ͨ��������";// �ʼ�����;
			String emailText_success = "���������: " + baoxiaoid + "��\n������: "
					+ baoxiaoemp + "��\n������״̬�� " + baoxiaostate + "��";// �ʼ�����;
			//������ַ
			String webaddress = "\n\n\t��¼����ϵͳ��\nhttp://192.168.0.3:8880/";
			
			

			// //���ݱ�����״̬�������ʼ���
			try {
				if (baoxiaostate.contains("�˻�")) {
					sendEmail(manager_email, baoxiaoemail, emailTitle_back,
							emailText_back+webaddress);
				} else if (baoxiaostate.contains("�ܾ�������ͨ��")) {
					sendEmail(manager_email, baoxiaoemail, emailTitle_success,
							emailText_success+webaddress);
				} else if (baoxiaostate != null) {
					sendEmail(manager_email, emailTo, emailTitle, emailText+webaddress);
				} else {
					//return this.DEFAULT_FORWARD;
					this.setEchoValue("����״̬");
					return "notpass";
				}
			} catch (Exception error) {
//				System.out.println("*.I am sorry to tell you the fail for "
//						+ error);
				//return this.DEFAULT_FORWARD;
				this.setEchoValue("�����ʼ�ʧ��");
				return "notpass";
			}

			 }

			// return this.DEFAULT_FORWARD;
			 this.setEchoValue("�ύ�ɹ�");
				return "notpass";

		} else {
			//return this.DEFAULT_FORWARD;
			this.setEchoValue("users <= 0 || null");
			return "notpass";
		}
	}
	////////////////////////////////////////////////////////////////
	// �����ʼ�
	private void sendEmail(String from, String to, String title, String text)
			throws AddressException, MessagingException {
		
		//**************************************************8
//		System.out.println("======================================");
//		System.out.println(from);
//		System.out.println(to);
//		System.out.println(title);
//		System.out.println(text);		
//		System.out.println("======================================");
		//������
		to = "yuanxx@zephyr.com.cn";
		//*****************************************************8
		
		String smtpHost = "smtp." + from.substring(from.lastIndexOf("@")+1);
		String password = "1234567890";
		
		final Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.auth", "true");

		
		   Session myMailSession = Session.getInstance(props);
		   myMailSession.setDebug(true); // ��DEBUGģʽ
		   Message msg = new MimeMessage(myMailSession);
		   msg.setFrom(new InternetAddress(from));
		   msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		   msg.setContent("I have a email!", "text/plain");
		   msg.setSentDate(new java.util.Date());
		   msg.setSubject(title);
		   msg.setText(text);
//		   System.out.println("1.Please wait for sending two...");

		   // �����ʼ�
		   Transport myTransport = myMailSession.getTransport("smtp");
		   myTransport.connect(smtpHost, from, password);
		   myTransport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
		   myTransport.close();
		   // javax.mail.Transport.send(msg); // ���в���ʹ�á�
//		   System.out.println("2.Your message had send!");
		

	}

	

	public static void main(String[] args) {
		
		
	}
}
