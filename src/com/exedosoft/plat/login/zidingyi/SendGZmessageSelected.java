package com.exedosoft.plat.login.zidingyi;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.login.zidingyi.excel.LDAPPeopleUtil;
import com.exedosoft.plat.login.zidingyi.excel.MySqlOperation;
import com.lowagie.text.Table;

public class SendGZmessageSelected extends DOAbstractAction {

	public String excute() {

		String[] checks = this.actionForm.getValueArray("checkinstance");
		if (checks == null) {
//			System.out.println("û������");
			this.setEchoValue("û������");
			return NO_FORWARD;
		}

		// �Թ���Ա��ݷ�������
		String manager_email = "yxxts_zep@163.com";// ����Ա�����ַ;
		String emailTo = null;// �����˵�ַ ;
		StringBuffer noEmail = new StringBuffer();// û�������Ա��;
		String name = null; // ʵ��Ա������;
		String emailName = null; // ����������;
		String eamilSelf = null;// Ա���Զ�������;
		int countAll = 0;
		int countSend = 0;
		int countFail = 0;
		// ���յĲ���
		Connection conn = MySqlOperation.getConnection();
		for (int n = 0; n < checks.length; n++) {
//			System.out.println("checks[" + n + "]=" + checks[n]);
//			System.out.println("checks[" + n + "]=" + checks[n]);
//			System.out.println("checks[" + n + "]=" + checks[n]);
//			System.out.println("checks[" + n + "]=" + checks[n]);
//			System.out.println("checks[" + n + "]=" + checks[n]);
			List users = new ArrayList();

			String objuid = checks[n];
			DOService newServ = service.getService("gz_salarymessage_browse");
			if (newServ != null)
				users = newServ.invokeSelect(objuid);
			// �������е����ݣ�
			if (users != null && users.size() > 0) {
				BOInstance bi = (BOInstance) users.get(0);
				SalaryMessage sm = new SalaryMessage();
				sm.setObjuid(bi.getValue("objuid"));
				sm.setMonth(bi.getValue("month"));
				sm.setName(bi.getValue("name"));
				sm.setBasesalary(Double.parseDouble(bi.getValue("basesalary")));
				sm.setBuckshee(Double.parseDouble(bi.getValue("buckshee")));
				sm.setRentdeduct(Double.parseDouble(bi.getValue("rentdeduct")));
				sm.setLeavededuct(Double.parseDouble(bi.getValue("leavededuct")));
				sm.setFactsalary(Double.parseDouble(bi.getValue("factsalary")));
				sm.setPayyanglaoinsure(Double.parseDouble(bi.getValue("payyanglaoinsure")));
				sm.setPayshiyeinsure(Double.parseDouble(bi.getValue("payshiyeinsure")));
				sm.setPayyilaioinsure(Double.parseDouble(bi.getValue("payyilaioinsure")));
				sm.setPayshebaofee(Double.parseDouble(bi.getValue("payshebaofee")));
				sm.setPayhousingsurplus(Double.parseDouble(bi.getValue("payhousingsurplus")));
				sm.setTaxbefore(Double.parseDouble(bi.getValue("taxbefore")));

				sm.setTaxget(Double.parseDouble(bi.getValue("taxget")));
				sm.setTaxlv(bi.getValue("taxlv"));
				sm.setTaxrm(Double.parseDouble(bi.getValue("taxrm")));

				sm.setTax(Double.parseDouble(bi.getValue("tax")));
				sm.setTaxafter(Double.parseDouble(bi.getValue("taxafter")));
				sm.setRemark(bi.getValue("remark"));

				/**
				 * ȡ�÷�������
				 */

				// �ʼ�����
				StringBuffer content = new StringBuffer();
				String stMonth = sm.getMonth() + " �·�";

				String title = sm.getName() + " " + stMonth + " �Ĺ�����Ϣ";
				// �ʼ�����
				content.append("<center><h3>" + title + "</h3></center>");
				content.append("<table align='center' border='0' bordercolor='gray' cellspacing='5' cellpadding='5'  bgcolor='oldlace'>");
				content.append("<tr align='center' bgcolor='wheat'>");
				content.append("<th>����</th><th>�¹���</th><th>����</th><th>�ⷿ�ۼ�</th><th>��(��)�ٿۼ�</th><th>Ӧ����</th></tr>");
				content.append("<tr  align='center' bgcolor='floralwhite'>");
				content.append("<td>" + sm.getName() + "</td><td>"
						+ sm.getBasesalary() + "</td>");
				content.append("<td>" + sm.getBuckshee() + "</td><td>"
						+ sm.getRentdeduct() + "</td>");
				content.append("<td>" + sm.getLeavededuct() + "</td><td>"
						+ sm.getFactsalary() + "</td>");
				content.append("</tr><tr align='center' bgcolor='wheat'>");
				content.append("<th>���ɸ������ϱ���</th><th>���ɸ���ʧҵ����</th><th>���ɸ���ҽ�Ʊ���</th><th>����Ӧ���籣С��</th>"
						+ "<th>���ɸ���ס��������</th><th>˰ǰӦ��</th></tr><tr  align='center' bgcolor='floralwhite'>");
				content.append("<td>" + sm.getPayyanglaoinsure() + "</td>");
				content.append("<td>" + sm.getPayshiyeinsure() + "</td><td>"
						+ sm.getPayyilaioinsure() + "</td>");
				content.append("<td>" + sm.getPayshebaofee() + "</td><td>"
						+ sm.getPayhousingsurplus() + "</td>");
				content.append("<td>" + sm.getTaxbefore() + "</td>");

				content.append("</tr><tr align='center' bgcolor='wheat'>");
				content.append("<th>Ӧ˰����G=F-2000</th><th>˰��H</th><th>����۳�</th><th>˰</th>"
						+ "<th>˰��ʵ��</th><th>��ע</th></tr><tr  align='center' bgcolor='floralwhite'>");
				content.append("<td>" + sm.getTaxget() + "</td>");
				content.append("<td>" + sm.getTaxlv() + "</td><td>"
						+ sm.getTaxrm() + "</td>");
				content.append("<td>" + sm.getTax() + "</td><td>"
						+ sm.getTaxafter() + "</td>");
				content.append("<td>" + sm.getRemark() + "</td>");
				content.append("</tr></table>");

				content.append("<br><a href='http://192.168.0.3:8880/yiyi/allsm?uid="
						+ sm.getObjuid()
						+ "'><h4 align='center'>�鿴���й�����Ϣ</h4></a>");
				String contentText = content.toString();
//				System.out.println(contentText);

				/**
				 * // ȡ�������ַ;
				 */

				// ȡ�ô�������������ȡ�������ַ
				name = sm.getName();

				try {
					eamilSelf = MySqlOperation.findEmailByName(conn, name);

					// ʹ���Զ�������
					if (eamilSelf != null && eamilSelf.length() > 0) {
						emailTo = eamilSelf.trim();
					} else {// ʹ�ô����������ַ
						emailName = MySqlOperation.findTonameByName(conn, name);
						if (emailName != null && emailName.length() > 0) {
						 	DOService empser = DOService.getService("zf_employee_browse_sn");
							List<BOInstance> emp = new ArrayList<BOInstance>();
							emp = empser.invokeSelect(emailName); 
							if(emp.size()>0) {
								BOInstance empBi = emp.get(0);
								emailTo = empBi.getValue("mail");
							}
						} else {
							// ʹ�ñ��������ַ
							DOService empser = DOService.getService("zf_employee_browse_cn");
							List<BOInstance> emp = new ArrayList<BOInstance>();
							emp = empser.invokeSelect(name); 
							if(emp.size()>0) {
								BOInstance empBi = emp.get(0);
								emailTo = empBi.getValue("mail");
							}
						}
					}

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// month, name, basesalary, buckshee, rentdeduct,
				// leavededuct, 6
				// factsalary, payyanglaoinsure, payshiyeinsure,
				// payyilaioinsure, 4
				// payshebaofee, payhousingsurplus, taxbefore, tax,
				// taxafter, remark 6
				// /�����ʼ�
				countAll++;
				if (emailTo == null || emailTo.trim().length() <= 0) {
					countFail++;
					String tsname = "";
					String addname = "[" + name + "]";
					if (noEmail != null)
						tsname = noEmail.toString();
					if (tsname.contains(addname)) {
						// �Ѵ��ڣ��򲻼���
					} else if (noEmail == null || noEmail.length() <= 0)
						noEmail.append("Ա��[" + name + "], ");
					else
						noEmail.append("[" + name + "],");
				} else {
					try {
						String password = "1234567890";
//						System.out.println("$$$$$�����ʼ�������Ϣ�鿴$$$$$$");
//						System.out.println("�����ʼ�======================"
//								+ manager_email);
//						System.out.println("ʵ�ʽ�����====================" + name);
//						System.out.println("���ս�����===================="
//								+ emailName);
//						System.out.println("��������======================"
//								+ emailTo);
//						System.out.println("ʵ�ʽ�������=================="
//								+ "yuanxx@zephyr.com.cn");
//						System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
						countSend++;
						sendEmail(manager_email, password, emailTo, title,
								contentText);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} else {
				this.setEchoValue("�����ʼ�ʧ��");
				return "notpass";
			}
		}

		try {
			if (conn != null && !conn.isClosed())
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (noEmail != null && noEmail.length() > 0) {
			noEmail.append("û�ж�Ӧ�������ַ���������Ա��ϵ��");
			if (countSend > 0)
				this.setEchoValue("�����ʼ��ɹ���������" + countSend + "���ʼ���\n" + "����"
						+ countFail + "��" + noEmail);
			else
				this.setEchoValue("�����ʼ�ʧ�ܣ�\n" + "��" + countFail + "��" + noEmail);

			return "notpass";
		} else {
			this.setEchoValue("�����ʼ��ɹ���������" + countSend + "���ʼ���\n");
			return "notpass";
		}
	}

	// �����ʼ�
	private void sendEmail(String from, String password, String to,
			String title, String text) throws AddressException,
			MessagingException {

//		// **************************************************8
//		// ������
//		to = "yuanxx@zephyr.com.cn";
//		// *****************************************************8

		String smtpHost = "smtp." + from.substring(from.lastIndexOf("@") + 1);

//		System.out.println("$$$$$$$$$$LoginActionLDAP()$$$$$$$$$$$$" + from
//				+ "===" + password + "$$$$$$$$$$$$$$$$$$$$$$$4");
//		System.out.println("$$$$$$$$$$$$$$$�����ʼ���Ϣ�鿴$$$$$$$$$$$$$$4");
//		System.out.println("�����������ַ==========================" + from);
//		System.out.println("�����������ַ==========================" + to);
//		System.out.println("���ʼ�����Э�������==========================" + smtpHost);
//		System.out.println("�ʼ�����==========================" + title);
//		System.out.println("�ʼ�����==========================" + text);
//		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$4");

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
		Multipart mp = new MimeMultipart("related");// related��ζ�ſ��Է���html��ʽ���ʼ�
		BodyPart bodyPart = new MimeBodyPart();// ����
		bodyPart.setDataHandler(new DataHandler(text, "text/html;charset=UTF-8"));// ��ҳ��ʽ
		mp.addBodyPart(bodyPart);
		msg.setContent(mp);
//		System.out.println("1.Please wait for sending two...");

		// �����ʼ�
		Transport myTransport = myMailSession.getTransport("smtp");
		myTransport.connect(smtpHost, from, password);
		myTransport.sendMessage(msg,
				msg.getRecipients(Message.RecipientType.TO));
		myTransport.close();
		// javax.mail.Transport.send(msg); // ���в���ʹ�á�
//		System.out.println("2.Your message had send!");
	}

	public static Double castDouble(String value) {
		Double number = 0.00;
		if (value != null && value.trim().length() > 0
				&& value.matches("^\\d+.\\d+|\\d+$")) {
			number = Double.parseDouble(value);
		}
//		System.out.println(number);
		return number;
	}

	public static void main(String[] args) {
	}

}
