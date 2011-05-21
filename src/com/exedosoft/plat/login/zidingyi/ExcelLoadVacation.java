package com.exedosoft.plat.login.zidingyi;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

public class ExcelLoadVacation extends DOAbstractAction {

	public String excute() {
		// System.out.println("??????????????????+++++++++++++++=");
		String path = null;
		String fileName = null;

		String isCover = DOGlobals.getInstance().getSessoinContext()
				.getFormInstance().getValue("iscover");
		String btn = DOGlobals.getInstance().getSessoinContext()
				.getFormInstance().getValue("vacation_load");
		path = DOGlobals.getInstance().getValue("uploadfiletemp")
				+ "vacationdate/";
		fileName = DOGlobals.getInstance().getSessoinContext()
				.getFormInstance().getValue("vacation_load");
		if(fileName != null) {
			fileName = unicode2GBK(fileName);
		}
		
		
		
		
		
		// fileName="���ҷ����ڼ���1.xls";

		if (fileName != null && fileName.trim().length() > 0) {
			// System.out.println("fileName" + fileName);
			// ��������ʱ��ֻȡ���һ���ļ�
			if (fileName.indexOf(";") != -1) {
				String[] fileArr = fileName.split(";");
				fileName = fileArr[fileArr.length - 1];
				if (fileName != null && fileName.trim().length() <= 0)
					fileName = fileArr[fileArr.length - 2];
			} else if (fileName.indexOf("%3B") != -1) {
				String[] fileArr = fileName.split("%3B");
				fileName = fileArr[fileArr.length - 1];
				if (fileName != null && fileName.trim().length() <= 0)
					fileName = fileArr[fileArr.length - 2];
			}
			fileName = fileName.trim();

		} else {
			this.setEchoValue("�ļ�������Ϊ�գ��뵼��ڼ���ʱ���");
			return DEFAULT_FORWARD;
		}

		System.out.println(fileName);

		if (path != null)
			path = path.trim();
		if (fileName != null)
			fileName = fileName.trim();
		String pathFile = path + fileName;
		String[] empty = new String[2];
		//1���ɹ�;0��ʧ��;2�����ݴ���
		try {

			empty = readExl(pathFile, isCover, fileName, btn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return DEFAULT_FORWARD;
		}

		// �޸��ļ���//�޷�ʵ�֣����ļ�����ʹ��
		// changeFile(pathFile);

		if ("0".equals(empty[0])) {
			BOInstance bi = new BOInstance();
			bi.putValue("empty", "notempty");
			bi.putValue("btn", btn);
			this.setInstance(bi);
			return this.DEFAULT_FORWARD;
		} else if ("1".equals(empty[0])) {
			return DEFAULT_FORWARD;
		} else if("2".equals(empty[0])){
			BOInstance bi = new BOInstance();
			String msg = empty[1];
			bi.putValue("msg", msg);
			bi.putValue("empty", "errordata");
			this.setInstance(bi);
			return this.DEFAULT_FORWARD;
		} else /*if("3".equals(empty[0]))*/{
			BOInstance bi = new BOInstance();
			String msg = empty[1];
			bi.putValue("msg", msg);
			bi.putValue("empty", "errordata2");
			this.setInstance(bi);
			return this.DEFAULT_FORWARD;
		} 

	}

	// List<String> list = new ArrayList<String>();
	InputStream fs = null;
	Workbook workBook = null;

	public String[] readExl(String filePath, String isCover, String fileName,
			String btn) throws Exception {

		String month = null;
		int countMonth = 0;

		// System.out.println("+++++ִ��readExl++++++");

		// ����excel�ļ�
		fs = new FileInputStream(filePath);
		// �õ� workbook
		workBook = Workbook.getWorkbook(fs);

		// ȡ��sheet��������workbook���ж��sheet �������� wb.getSheets()�������õ����еġ�
		// getSheets() �������� Sheet[] ���� Ȼ���������������������Ƕ��ѭ�����¡�
		Sheet sheet = workBook.getSheet(0);// ����ֻȡ�õ�һ��sheet��ֵ��Ĭ�ϴ�0��ʼ
		// System.out.println(sheet.getColumns());// �鿴sheet����
		// System.out.println(sheet.getRows());// �鿴sheet����
		Cell cell = null;// ���ǵ�����Ԫ��
		// ��ʼѭ����ȡ�� cell ������ݣ����ﶼ�ǰ�String��ȡ�� Ϊ��ʡ�£��������Լ����԰�ʵ��������ȡ�����߶���
		// String��ȡ��Ȼ���������Ҫǿ��ת��һ�¡�
		List<String[]> bzList = new ArrayList<String[]>();
		String[] dateStr = null;
		for (int j = 0; j < sheet.getRows(); j++) {
			cell = sheet.getCell(0, j);
			String temp = cell.getContents();
			if (temp != null)
				temp = temp.trim();
			if (temp.contains("�ڼ���") || temp.contains("������"))
				continue;

			dateStr = new String[4];
			for (int i = 0; i < sheet.getColumns(); i++) {
				cell = sheet.getCell(i, j);
				String value = null;
				Date date = null;
				value = cell.getContents();
				if ((i == 0 || i == 1 || i == 2 || i == 3) && value != null) {
					if (value.contains("��") && value.contains("��")) {
						date = castDate(value);
					} else if (value.contains("/")) {
						date = castDate(value);
					} else if (value.contains("-")) {
						date = castDateSimple(value);
					} else if (value.matches("^\\d+$")) {
						long days = Long.parseLong(value);
						date = getDate(days);
					}

				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				if (date != null) {
					if(i == 2 || i == 3) {
						Calendar calQs = Calendar.getInstance();
						calQs.setTime(date);
						if (calQs.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
								|| calQs.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
							BOInstance bi = new BOInstance();
							String msg = "";
							if(calQs.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
								msg = "errordata: " + date + " ��������";
							else if(calQs.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
								msg = "errordata: " + date + " ��������";
							String[] empty = new String[2];
							empty[0] = "2";
							empty[1] = msg;
							return empty;
						}
						
						
					} else if(i == 1) {
						Calendar calQs = Calendar.getInstance();
						calQs.setTime(date);
						if (calQs.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY
								&& calQs.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
							BOInstance bi = new BOInstance();
							String msg = "errordata: " + date + " ����˫����";
							String[] empty = new String[2];
							empty[0] = "3";
							empty[1] = msg;
							return empty;
						}
					}
				}
				switch (i) {
				case 0:
					if (date != null && format.format(date) != null) {
						month = format.format(date);
						dateStr[i] = month;
					} else {
						dateStr[i] = null;
					}
					break;
				case 1:
					if (date != null && format.format(date) != null) {
						month = format.format(date);
						dateStr[i] = month;
					} else {
						dateStr[i] = null;
					}
					break;
				case 2:
					if (date != null && format.format(date) != null) {
						month = format.format(date);
						dateStr[i] = month;
					} else {
						dateStr[i] = null;
					}
					break;
				case 3:
					if (date != null && format.format(date) != null) {
						month = format.format(date);
						dateStr[i] = month;
					} else {
						dateStr[i] = null;
					}
					break;
				}
			}
			bzList.add(dateStr);
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
			String[] bzstr = null;
			ResultSet rs = null;

			// System.out.println("isCover=" + isCover);
			// System.out.println("isCover=" + isCover);
			// System.out.println("isCover=" + isCover);

			if (isCover == null) {
				boolean notEmpty = false;
				for (int i = 0; i < bzList.size(); i++) {
					bzstr = bzList.get(i);
					if (bzList != null) {
						for (int j = 0; j < bzstr.length && bzstr[j] != null; j++) {
							rs = MySqlOperation.findVacationByDate(conn,
									bzstr[j]);
							while (rs != null && rs.next()) {
								notEmpty = true;
								break;
							}
						}

						if (notEmpty)
							break;
					}
				}

				// �Ƿ����ظ������ݣ����У�����ʾ��
				if (notEmpty) {
					BOInstance bi = new BOInstance();
					bi.putValue("notmpty", "notmpty");
					bi.putValue("btn", btn);
					this.setInstance(bi);
					String[] empty = new String[2];
					empty[0] = "0";
					empty[1] = "";
					return empty;
				}
				// �Ƿ����ظ������ݣ����ޣ����������ִ��
				/**
				 * ***************************
				 * */

			} else if (isCover.trim().equals("cover")) {
				// ȫ������
				// System.out.println("ȫ������");
				// /
				for (int i = 0; i < bzList.size(); i++) {

					bzstr = bzList.get(i);
					if (bzList != null) {
						for (int j = 0; j < bzstr.length && bzstr[j] != null; j++) {
							java.util.Date date = new java.util.Date();
							long objuid = date.getTime() + j;
							boolean flag = false;
							rs = MySqlOperation.findVacationByDate(conn,
									bzstr[j]);
							while (rs != null && rs.next()) {
								flag = true;
								break;
							}
							if (flag) {
								MySqlOperation.deleteVacationByDate(conn,
										bzstr[j]);
								MySqlOperation.insertVacation(conn, (j + 1)
										+ "", bzstr[j], objuid);
							} else {
								// System.out.println("���ݲ��ظ���" + name + "\t"
								// + sm.getMonth());
								MySqlOperation.insertVacation(conn, (j + 1)
										+ "", bzstr[j], objuid);
							}
						}
					}
				}
				conn.commit();
				if (conn != null && !conn.isClosed())
					conn.close();
				// System.out.println("�洢�ɹ���cover");
				String[] empty = new String[2];
				empty[0] = "1";
				empty[1] = "";
				return empty;
			} else if (isCover.trim().equals("nocover")) {
				// ȫ��������
				// /
				for (int i = 0; i < bzList.size(); i++) {

					bzstr = bzList.get(i);
					if (bzList != null) {
						for (int j = 0; j < bzstr.length && bzstr[j] != null; j++) {
							System.out.println("=========++++++++++++++"
									+ bzstr[j]);
							java.util.Date date = new java.util.Date();
							long objuid = date.getTime() + j;
							boolean flag = false;
							rs = MySqlOperation.findVacationByDate(conn,
									bzstr[j]);
							while (rs != null && rs.next()) {
								flag = true;
								break;
							}
							if (flag) {

							} else {
								// System.out.println("���ݲ��ظ���" + name + "\t"
								// + sm.getMonth());
								MySqlOperation.insertVacation(conn, (j + 1)
										+ "", bzstr[j], objuid);
							}
						}
					}
				}
				conn.commit();
				if (conn != null && !conn.isClosed())
					conn.close();
				// System.out.println("�洢�ɹ���nocover");
				String[] empty = new String[2];
				empty[0] = "1";
				empty[1] = "";
				return empty;
			} else {
				// ��ʱû�п��ǵ������
				System.out.println("��ʱû�п��ǵ��������ʱû�п��ǵ������");
				String[] empty = new String[2];
				empty[0] = "1";
				empty[1] = "";
				return empty;
			}
			// ��������ִ��
			for (int i = 0; i < bzList.size(); i++) {
				bzstr = bzList.get(i);
				if (bzList != null) {
					for (int j = 0; j < bzstr.length && bzstr[j] != null; j++) {
						java.util.Date date = new java.util.Date();
						long objuid = date.getTime();
						MySqlOperation.insertVacation(conn, (j + 1) + "",
								bzstr[j], objuid);
					}
				}
			}
			conn.commit();
			if (conn != null && !conn.isClosed())
				conn.close();
			// System.out.println("�洢�ɹ���noall");
		} catch (SQLException e) {
			System.out.println("�洢ʧ�ܣ�");
			e.printStackTrace();
			conn.rollback();
		}
		String[] empty = new String[2];
		empty[0] = "1";
		empty[1] = "";
		return empty;
	}
	
	

	private Date castDate(String value) {
		String year = "";
		String month = "";
		String day = "";
		Date date = null;
		if (value.contains("\"��\"") && value.contains("\"��\"")
				&& value.contains("\"��\"")) {
			year = value.substring(0, value.indexOf("\"��\""));
			month = value.substring(value.indexOf("\"��\"") + 3,
					value.indexOf("\"��\""));
			day = value.substring(value.indexOf("\"��\"") + 3,
					value.indexOf("\"��\""));
		} else if (value.contains("��") && value.contains("��")
				&& value.contains("��")) {
			year = value.substring(0, value.indexOf("��"));
			month = value.substring(value.indexOf("��") + 1, value.indexOf("��"));
			day = value.substring(value.indexOf("��") + 1, value.indexOf("��"));
		} else if (value.contains("/")) {
			String[] str = value.split("/");
			if(str.length == 3) {
				year = str[0];
				month = str[1];
				day = str[2];
			}
		}
		
		if(month.trim().length() == 1)
			month = "0"+month.trim();
		if(day.trim().length() == 1)
			day = "0"+day.trim();
		
		date = Date
				.valueOf(year.trim() + "-" + month.trim() + "-" + day.trim());
		return date;
	}

	private Date castDateSimple(String value) {
		String year = null;
		String month = null;
		String day = null;
		Date date = null;
		year = value.substring(0, value.indexOf("-"));
		month = value.substring(value.indexOf("-") + 1, value.lastIndexOf("-"));
		day = value.substring(value.lastIndexOf("-") + 1);
		if (year != null && year.trim().length() == 2) {
			if (Integer.parseInt(year) > 80) {
				year = "19" + year;
			} else {
				year = "20" + year;
			}
		}
		if(month.trim().length() == 1)
			month = "0"+month.trim();
		if(day.trim().length() == 1)
			day = "0"+day.trim();
		date = Date
				.valueOf(year.trim() + "-" + month.trim() + "-" + day.trim());
		return date;
	}

	private Date getDate(long dates) {
		Date date = null;
		// Date date0 = Date.valueOf("1900-01-00");
		// Date date1 = Date.valueOf("1970-01-01");

		// dates����1900-01-00������
		// ������1970-01-00������
		// System.out.println((date1.getTime() - date0.getTime()) / 3600000 / 24
		// + "++++++++++++++++????????????????");
		long timebewteen = 25568L;

		dates = dates - timebewteen;

		// nowTime
		Calendar cal = Calendar.getInstance();
		//
		// Date date2 = Date.valueOf("1970-01-00");
		// long d1 = date2.getTime();
		// System.out.println(d1 + "=1=");

		Date date2 = Date.valueOf("1970-01-01");
		long d1 = date2.getTime() - 24 * 3600 * 1000L;
		// System.out.println(d1 + "=4=");

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

	//Unicode����ת��Ϊ����
	public String unicode2GBK(String str) {
		StringBuffer ret = new StringBuffer();
		if(str.contains("%u")) {
			String[] s = str.split("%u");
			ret.append(s[0]);
			for(int i = 1 ; i < s.length; i++ ) {
				ret.append((char)Integer.parseInt(s[i].substring(0, 4), 16));
				ret.append(s[i].substring(4));
			}
		} else {
			ret.append(str);
		}
		return ret.toString();
	}

	public static void main(String[] args) {
		ExcelLoadVacation exl = new ExcelLoadVacation();
//		// exl.getDate(1234567);
//		exl.castDate("2008\"��\"2\"��\"13\"��\"");
		
		String s = exl.unicode2GBK("Vac.xls");
		System.out.println(s);
		
	}

}
