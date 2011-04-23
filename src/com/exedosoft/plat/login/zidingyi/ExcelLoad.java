package com.exedosoft.plat.login.zidingyi;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.BOInstance;

import com.exedosoft.plat.login.zidingyi.excel.MySqlOperation;
import com.exedosoft.plat.util.DOGlobals;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

public class ExcelLoad extends DOAbstractAction {

	public String excute() {
//		System.out.println("??????????????????+++++++++++++++=");
		String path = null;
		String fileName = null;
		String sqlDate = null;
		String sqlFile = null;
		
		String isCover = DOGlobals.getInstance().getSessoinContext()
				.getFormInstance().getValue("iscover");
		path = DOGlobals.getInstance().getValue("uploadfiletemp");
		fileName = DOGlobals.getInstance().getSessoinContext()
				.getFormInstance().getValue("excel_load");
		System.out.println("===============" + isCover);
		System.out.println("===============" + path);
		System.out.println("===============" + fileName);
		System.out.println("===============" + fileName);
		
//		System.out.println(path);
		java.util.Date date = new java.util.Date();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
		String sdate = format.format(date);
		sqlDate = sdate;
		
		if(fileName != null && fileName.trim().length() > 0) {
//			System.out.println("fileName" + fileName);
			//��������ʱ��ֻȡ���һ���ļ�
			if(fileName.indexOf(";") != -1) {
				String[] fileArr = fileName.split(";");
				fileName = fileArr[fileArr.length-1];
				if(fileName != null && fileName.trim().length() <= 0)
					fileName = fileArr[fileArr.length-2];
			} else if(fileName.indexOf("%3B") != -1) {
				String[] fileArr = fileName.split("%3B");
				fileName = fileArr[fileArr.length-1];
				if(fileName != null && fileName.trim().length() <= 0)
					fileName = fileArr[fileArr.length-2];
			}
			fileName = fileName.trim();
			sqlFile = fileName;
			
			
			if(path != null && path.trim().length() > 0) {
				path = path.trim();
				if(path.lastIndexOf("\\\\") != -1) {
					fileName = sdate + "\\\\" + fileName;
				} else if(path.lastIndexOf("\\") != -1) {
					fileName = sdate + "\\" + fileName;
				} else if(path.lastIndexOf("/") != -1) {
					fileName = sdate + "/" + fileName;
				}
			}
		}else {
			this.setEchoValue("�ļ�������Ϊ�գ��뵼�빤������");
			return DEFAULT_FORWARD;
		}
		

		try {
//			System.out.println(sqlDate);
//			System.out.println(sqlFile);
//			System.out.println(fileName);
			Connection conn = MySqlOperation.getConnection();
			ResultSet rs = MySqlOperation.SMfindByFileName(conn, sqlDate, sqlFile);
			if(rs != null && rs.next()) {
				BOInstance bi = new BOInstance();
				bi.putValue("isexist", "isexist");
				this.setInstance(bi);
				this.setEchoValue("�ļ��Ѵ��ڣ���Ҫ���µ��룬������ļ����ƺ��ٵ��룡");
				return DEFAULT_FORWARD;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			this.setEchoValue("���빤����ʧ�ܣ�");
			return DEFAULT_FORWARD;
		}
		
//		System.out.println(fileName);
//		System.out.println("??????????????????+++++++++++++++=");

		if (path != null)
			path = path.trim();
		if (fileName != null)
			fileName = fileName.trim();
		String pathFile = path + fileName;
		boolean empty = false;
		try {
			empty = readExl(pathFile, isCover, fileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.setEchoValue("���빤����ʧ�ܣ�");
			return DEFAULT_FORWARD;
		}

		// �޸��ļ���//�޷�ʵ�֣����ļ�����ʹ��
		// changeFile(pathFile);

		if (!empty) {
			BOInstance bi = new BOInstance();
			bi.putValue("empty", "notempty");
			this.setInstance(bi);
			return this.DEFAULT_FORWARD;
		} else {
			this.setEchoValue("���빤�����ɹ���");
			return DEFAULT_FORWARD;
		}

	}

	// List<String> list = new ArrayList<String>();
	InputStream fs = null;
	Workbook workBook = null;

	public boolean readExl(String filePath, String isCover, String fileName) throws Exception {

		String month = null;
		int countMonth = 0;

//		System.out.println("+++++ִ��readExl++++++");

		// ����excel�ļ�
		fs = new FileInputStream(filePath);
		// �õ� workbook
		workBook = Workbook.getWorkbook(fs);

		// ȡ��sheet��������workbook���ж��sheet �������� wb.getSheets()�������õ����еġ�
		// getSheets() �������� Sheet[] ���� Ȼ���������������������Ƕ��ѭ�����¡�
		Sheet sheet = workBook.getSheet(0);// ����ֻȡ�õ�һ��sheet��ֵ��Ĭ�ϴ�0��ʼ
//		System.out.println(sheet.getColumns());// �鿴sheet����
//		System.out.println(sheet.getRows());// �鿴sheet����
		Cell cell = null;// ���ǵ�����Ԫ��
		// ��ʼѭ����ȡ�� cell ������ݣ����ﶼ�ǰ�String��ȡ�� Ϊ��ʡ�£��������Լ����԰�ʵ��������ȡ�����߶���
		// String��ȡ��Ȼ���������Ҫǿ��ת��һ�¡�
		List<SalaryMessage> smList = new ArrayList<SalaryMessage>();
		for (int j = 0; j < sheet.getRows(); j++) {
			StringBuffer sb = new StringBuffer();
			SalaryMessage sm = new SalaryMessage();
			cell = sheet.getCell(0, j);
			String temp = cell.getContents();
			if (temp != null)
				temp = temp.trim();
			if (temp.contains("�·�") || temp.contains("����")
					|| temp.contains("�¹���"))
				continue;
			for (int i = 0; i < sheet.getColumns(); i++) {
				cell = sheet.getCell(i, j);
				String value = null;
				Date date = null;
				value = cell.getContents();
				if (i == 0) {
						if (value.contains("��") && value.contains("��")) {
							date = castDate(value);
						} else if (value.contains("-")) {
							date = castDateSimple(value);
						} else if (value.matches("^\\d+$")) {
							long days = Long.parseLong(value);
							date = getDate(days);
						}
//					System.out.println(i + "date=" + date);
					if(date == null)
						continue;
				} else {
					value = cell.getContents();
//					System.out.println(i + "value=" + value);
				}

				if (date == null)
					sb.append(value);
				else
					sb.append(date);
				sb.append(",\t");// ����Ԫ���ÿ�������ö��Ÿ���

				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
				
				switch (i) {
				case 0:
					if(!format.format(date).equals(month)) {
						month = format.format(date);
						countMonth ++;
					}
					sm.setMonth(format.format(date));
					break;
				case 1:
					if (value != null)
						value = value.trim();
					sm.setName(value);
					break;
				case 2:
					sm.setBasesalary(castDouble(value));
					break;
				case 3:
					sm.setBuckshee(castDouble(value));
					break;
				case 4:
					sm.setRentdeduct(castDouble(value));
					break;
				case 5:
					sm.setLeavededuct(castDouble(value));
					break;
				case 6:
					sm.setFactsalary(castDouble(value));
					break;
				case 7:
					sm.setPayyanglaoinsure(castDouble(value));
					break;
				case 8:
					sm.setPayshiyeinsure(castDouble(value));
					break;
				case 9:
					sm.setPayyilaioinsure(castDouble(value));
					break;
				case 10:
					sm.setPayshebaofee(castDouble(value));
					break;
				case 11:
					sm.setPayhousingsurplus(castDouble(value));
					break;
				case 12:
					sm.setTaxbefore(castDouble(value));
					break;
				case 13:
					sm.setTaxget(castDouble(value));
					break;
				case 14:
					sm.setTaxlv(value);
					break;
				case 15:
					sm.setTaxrm(castDouble(value));
					break;
				case 16:
					sm.setTax(castDouble(value));
					break;
				case 17:
					sm.setTaxafter(castDouble(value));
					break;
				case 18:
					sm.setRemark(value);
					break;
				}
			}
			if(sm.getMonth() != null && (sm.getName() != null && sm.getName().trim().length() > 0))
				smList.add(sm);
			// list.add(sb.toString());// ��ÿ�е��ַ�����һ��String���͵ļ��ϱ��档
		}
		workBook.close();// �ǵùر�

		// // �������ϲ鿴ÿ�е�����
		// for (String ss : list) {
		// System.out.println(ss);
		// }

		// �洢�����ݿ���
		Connection conn = MySqlOperation.getConnection();
		try {
			conn.setAutoCommit(false);
			SalaryMessage sm = null;
			ResultSet rs = null;
			long time = 0;
			
//			System.out.println("isCover=" + isCover);
//			System.out.println("isCover=" + isCover);
//			System.out.println("isCover=" + isCover);
			
			if (isCover == null) {
				boolean notEmpty = false;
				for (int i = 0; i < smList.size(); i++) {
					sm = smList.get(i);
					if (sm != null) {
						time = new java.util.Date().getTime() + i;
						String name = null;
						name = sm.getName();
						if (name != null)
							name = name.trim();
						rs = MySqlOperation.SMfindByNameAndDate(conn, name, sm
								.getMonth());
						while (rs != null && rs.next()) {
							notEmpty = true;
							break;
						}
						if (notEmpty)
							break;
					}
				}

				// �Ƿ����ظ������ݣ����У�����ʾ��
				if (notEmpty) {
					BOInstance bi = new BOInstance();
					bi.putValue("notmpty", "notmpty");
					this.setInstance(bi);
					return false;
				}
				// �Ƿ����ظ������ݣ����ޣ����������ִ��
				/**
				 * ***************************
				 * */

			} else if (isCover.trim().equals("cover")) {
				// ȫ������
//				System.out.println("ȫ������");
				//��������¹�������ʷ��¼
				GZtiaoLiShi.InsertLiShi(fileName,month);
				///
				for (int i = 0; i < smList.size(); i++) {
					boolean flag = false;
					sm = smList.get(i);
					if (sm != null) {
						time = new java.util.Date().getTime() + i;
						String name = null;
						name = sm.getName();
						if (name != null)
							name = name.trim();

						rs = MySqlOperation.SMfindByNameAndDate(conn, name, sm
								.getMonth());
						while (rs != null && rs.next()) {
							flag = true;
						}
//						System.out.println("������¼�� " + name + "\t"
//								+ sm.getMonth());
						rs.beforeFirst();
						if (flag) {
							while (rs != null && rs.next()) {
//								System.out.println("�����ظ���" + name + "\t"
//										+ sm.getMonth());
								MySqlOperation.SMDeleteByNameAndDate(conn,
										name, sm.getMonth());
//								System.out.println("delete: " + name);
							}
							MySqlOperation.insert(conn, sm, time);
						} else {
//							System.out.println("���ݲ��ظ���" + name + "\t"
//									+ sm.getMonth());
							MySqlOperation.insert(conn, sm, time);
						}
					}
				}
				conn.commit();
				if (conn != null && !conn.isClosed())
					conn.close();
//				System.out.println("�洢�ɹ���cover");
				return true;
			} else if (isCover.trim().equals("nocover")) {
				// ȫ��������
				//��������¹�������ʷ��¼
				GZtiaoLiShi.InsertLiShi(fileName,month);
				///
				for (int i = 0; i < smList.size(); i++) {
					boolean flag = false;
					sm = smList.get(i);
					if (sm != null) {
						time = new java.util.Date().getTime() + i;
						String name = null;
						name = sm.getName();
						if (name != null)
							name = name.trim();
						rs = MySqlOperation.SMfindByNameAndDate(conn, name, sm
								.getMonth());
						while (rs != null && rs.next()) {
							flag = true;
						}
						if (flag) {
							// �����ظ��������� �����κβ���
//							System.out.println("�����ظ���" + name + "\t"
//									+ sm.getMonth());
						} else {
							// ���ݲ��ظ������

//							System.out.println("���ݲ��ظ���" + name + "\t"
//									+ sm.getMonth());
							MySqlOperation.insert(conn, sm, time);
						}
					}
				}
				conn.commit();
				if (conn != null && !conn.isClosed())
					conn.close();
//				System.out.println("�洢�ɹ���nocover");
				return true;
			} else {
				// ��ʱû�п��ǵ������
				System.out.println("��ʱû�п��ǵ��������ʱû�п��ǵ������");
				return true;
			}
			// ��������ִ��
			rs.beforeFirst();
			//��������¹�������ʷ��¼
			GZtiaoLiShi.InsertLiShi(fileName,month);
			///
			for (int i = 0; i < smList.size(); i++) {
				sm = smList.get(i);
				if (sm != null) {
					time = new java.util.Date().getTime() + i;
					MySqlOperation.insert(conn, sm, time);
					
				}
			}

			//
			conn.commit();
			if (conn != null && !conn.isClosed())
				conn.close();
//			System.out.println("�洢�ɹ���noall");
		} catch (SQLException e) {
			System.out.println("�洢ʧ�ܣ�");
			e.printStackTrace();
			conn.rollback();
		}
		return true;
	}

	private Double castDouble(String value) {
		Double number = 0D;
		if (value != null && value.length() > 0
				&& value.matches("^\\d+.\\d+|\\d+$")) {
			number = Double.parseDouble(value);
		}
		return number;
	}

	private Date castDate(String value) {
		String year = null;
		String month = null;
		Date date = null;
		if (value.contains("\"��\"") && value.contains("\"��\"")) {
			year = value.substring(0, value.indexOf("\"��\""));
			month = value.substring(value.indexOf("\"��\"") + 3, value
					.indexOf("\"��\""));
		} else if (value.contains("��") && value.contains("��")) {
			year = value.substring(0, value.indexOf("��"));
			month = value.substring(value.indexOf("��") + 1, value.indexOf("��"));
		}
		if (year != null && month != null) {
			if(month.trim().length()<2)
				month = "0"+month.trim();
//			System.out.println(year.trim() + "=" + month.trim() + "=");
			date = Date.valueOf(year.trim() + "-" + month.trim() + "-15");
		}
		return date;
	}
	
	private Date castDateSimple(String value) {
		String year = null;
		String month = null;
		String day = null;
		Date date = null;
		year = value.substring(0, value.indexOf("-"));
		month = value.substring(value.indexOf("-") + 1,value.lastIndexOf("-"));
		day = value.substring(value.lastIndexOf("-") + 1);
		if(year != null && year.trim().length() == 2) {
			if(Integer.parseInt(year) > 80) {
				year = "19"+year;
			} else {
				year = "20"+year;
			}
		}
		date = Date.valueOf(year.trim() + "-" + month.trim() + "-" + day.trim());
		return date;
	}

	private Date getDate(long dates) {
		Date date = null;
//		Date date0 = Date.valueOf("1900-01-00");
//		Date date1 = Date.valueOf("1970-01-01");

		// dates����1900-01-00������
		// ������1970-01-00������
//		System.out.println((date1.getTime() - date0.getTime()) / 3600000 / 24 + "++++++++++++++++????????????????");
		long timebewteen = 25568L;
		
		dates = dates - timebewteen;
		
		// nowTime
		Calendar cal = Calendar.getInstance();
		//
//		Date date2 = Date.valueOf("1970-01-00");
//		long d1 = date2.getTime();
//		System.out.println(d1 + "=1=");
		
		Date date2 = Date.valueOf("1970-01-01");
		long d1 = date2.getTime() - 24*3600*1000L;
//		System.out.println(d1 + "=4=");
		
		long c = cal.getTimeInMillis();
		// ����������1970-01-00������
		long days = (c - d1) / 3600000 / 24;
		// temp = ������1970-01-00������ - Excel�е�ʱ����1970-01-00������
		// temp < 0, ��Excel�е�ʱ���������ʱ�䡣
		// temp > 0, ��Excel�е�ʱ��С������ʱ�䡣
		int temp = (int) (days - dates);

		// ///
		java.util.Date dTest = (java.util.Date) cal.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String sDate = format.format(dTest);

		cal.add(Calendar.DATE, (0 - temp));

		dTest = (java.util.Date) cal.getTime();
		sDate = format.format(dTest);

		if (sDate != null)
			date = Date.valueOf(sDate);
		return date;
	}

	public static void main(String[] args) {
		ExcelLoad exl = new ExcelLoad();
//		exl.getDate(1234567);
		exl.castDate("2008\"��\"2\"��\"13\"��\"");
	}

}
