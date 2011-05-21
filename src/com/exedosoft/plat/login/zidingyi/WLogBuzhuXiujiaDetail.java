package com.exedosoft.plat.login.zidingyi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.login.zidingyi.excel.LDAPPeopleUtil;
import com.exedosoft.plat.login.zidingyi.excel.MySqlOperation;
import com.exedosoft.plat.login.zidingyi.excel.OperationUtil;
import com.exedosoft.plat.search.customize.SearchTransCode;

public class WLogBuzhuXiujiaDetail extends DOAbstractAction {
	public String excute() {

		// ��Ҫ���յ����ݣ���ѯ����
		String name = null;// ����
		String year = null;// ���
		String month = null;// �·�
		String workloguid = null;
		List users = new ArrayList();
		try {
			DOService service = DOService
					.getService("cw_worklog_browse_bztj_gdjl");
			users = service.invokeSelect();

			// ��ѯ��ɾ����¼
			DOService servicedel = DOService
					.getService("cw_worklog_delete_bzxx_gdjl");
			servicedel.invokeUpdate();
		} catch (Exception e) {
			this.setEchoValue("��ѯʧ�ܣ�error" + e.toString());
			return "notpass";
		}

		// �������е����ݣ�
		if (users != null && users.size() > 0) {
			BOInstance bi = (BOInstance) users.get(0);
			workloguid = bi.getValue("backup2");
		}

		if (workloguid != null && workloguid.indexOf("-") != -1) {
			String[] array = workloguid.split("-");
			for (int i = 0; i < array.length; i++) {
				if (i == 0)
					name = array[i];
				if (i == 1)
					year = array[i];
				if (i == 2)
					month = array[i];
			}
		}

		Connection conn = MySqlOperation.getConnection();
		try {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			List<String> listemp_uid = new ArrayList<String>();

			// ���˲�ѯ��ֱ�Ӽ��뼴��
			listemp_uid.add(name);
			String sql = "";
			List<BOInstance> list = new ArrayList<BOInstance>();
			for (int cemp = 0; cemp < listemp_uid.size(); cemp++) {
				String emp_uid = listemp_uid.get(cemp);
				if (emp_uid != null && !"".equals(emp_uid.trim())) {
					String date = year + "-" + month;
					double money = 0.00;
					sql = "select * from cw_worklog where  emp_uid = '"
							+ emp_uid + "' and year(wdate) = " + year
							+ " and month(wdate)=" + month + " order by wdate ";
					/*
					 * ���ڼ���ȱʡ���ڵĲ��� cw_type: ǰһ��¼�����ͣ���˾'gs'������'cc'���ݼ�'xj'
					 */

					ResultSet rs = MySqlOperation.wLogBySql(conn, sql);
					int day = 1;
					String waddress = "1";
					String cw_type = "gs";
					//�ݼ����������ݼٲ��� 
					while (rs != null && rs.next()) {
						String wlog_uid = rs.getString("WorklogUID");
						if(wlog_uid == null)
							wlog_uid = rs.getString("workloguid");
						String curr_cw_type = rs.getString("cw_type");
						Date wdate = rs.getDate("wdate");
						String objuid = rs.getString("workloguid");
						String ifhavexc = rs.getString("ifhavexc");
						String appstatus = rs.getString("appstatus");
						cal.setTime(wdate);
						double wallowance = rs.getDouble("wallowance");
						if (cal.get(Calendar.DAY_OF_MONTH) == day) {
							if(ifhavexc != null && ifhavexc.indexOf("x") != -1){
								List<BOInstance> listxc = WLogBuzhuTongjiUtil.findCW_XC(wlog_uid, "x");
								double xj_money = 0.00;
								double xj_days = 0.00;
								if(listxc != null && listxc.size() > 0) {
									for(int l = 0; l < listxc.size(); l ++) {
										BOInstance bixc = listxc.get(l);
										double xc_days = bixc.getDoubleValue("xc_days");
										double xc_money = bixc.getDoubleValue("xc_money");
										xj_money = xc_money;
										xj_days = xc_days;
									}
								}
								BOInstance biresult = new BOInstance();
								biresult.setBo(this.service.getBo());
								biresult.setUid(objuid);
								biresult.putValue("workloguid", objuid);
								biresult.putValue("emp_uid", emp_uid);
								String dMoney = OperationUtil.round(xj_money, 2);
								biresult.putValue("wallowance", dMoney);
								biresult.putValue("wdate", wdate);
								if(appstatus == null)
									appstatus = "&nbsp;";
								biresult.putValue("appstatus", appstatus);
								biresult.putValue("xjdays", xj_days);
								list.add(biresult);
							}
								
							

							// ���� cw_type �� waddress
							cw_type = rs.getString("cw_type");
							if("gscc".equals(cw_type)) {
								cw_type = "cc";
							}
							waddress = rs.getString("waddress");
						} else {
							// 1��Ϊȱʡ����ʱ����������һ���µ�����¼ȡcw_type
							if (day == 1) {
								String[] str = WLogBuzhuTongjiUtil.getCw_type(
										emp_uid, format.format(wdate));
								// ���� cw_type �� waddress
								cw_type = str[0];
								waddress = str[1];
							}
							// ȡ�ò�����׼
							double subsidy = WLogBuzhuTongjiUtil
									.getBZbasic(waddress);
							int nowDays = cal.get(Calendar.DAY_OF_MONTH) - day;
							// ����cw_type �� �ڼ��� �� ������׼ ���㲹��
							// ����ȱʡ����
							// money = money +
							// WLogBuzhuTongjiUtil.getQueshMoney(nowDays, wdate,
							// subsidy, format, cw_type);

							for (int i = 0; i < nowDays; i++) {
								if ("cc".equals(cw_type)
										|| "xx".equals(cw_type)) {
									long time = wdate.getTime() - (nowDays - i)
											* 24 * 60 * 60 * 1000L;
									Date qsDate = new Date(time);
									Calendar calQs = Calendar.getInstance();
									calQs.setTime(qsDate);
										// Ϊture������Ϊ�����ڼ��գ������׼������ *1.5
										if (WLogBuzhuTongjiUtil.ifHolidays(
												format.format(wdate), false)) {
											money = subsidy * 1.5;
										} else {
											money = subsidy;
										}
								} else {
									money = 0.00;
								}
								String strdate = date;
								if(day < 10) 
									strdate = strdate + "-0"+day;
								else {
									strdate = strdate + "-"+day;
								}
								
								if(cw_type != null && ("xx".equals(cw_type) || "xj".equals(cw_type))){
									BOInstance biresult = new BOInstance();
									biresult.setBo(this.service.getBo());
									biresult.setUid("");
									biresult.putValue("workloguid", "");
									biresult.putValue("emp_uid", emp_uid);
									String dMoney = OperationUtil.round(money, 2);
									biresult.putValue("wallowance", dMoney);
									biresult.putValue("wdate", strdate);
									list.add(biresult);
								}
								day ++;
							}

							// ������ȱʡ���ں��ټ�����һ���
							
							if(ifhavexc != null && ifhavexc.indexOf("x") != -1){
								List<BOInstance> listxc = WLogBuzhuTongjiUtil.findCW_XC(wlog_uid, "x");
								double xj_money = 0.00;
								double xj_days = 0.00;
								if(listxc != null && listxc.size() > 0) {
									for(int l = 0; l < listxc.size(); l ++) {
										BOInstance bixc = listxc.get(l);
										double xc_days = bixc.getDoubleValue("xc_days");
										double xc_money = bixc.getDoubleValue("xc_money");
										xj_money = xc_money;
										xj_days = xc_days;
									}
								}
								BOInstance biresult = new BOInstance();
								biresult.setBo(this.service.getBo());
								biresult.setUid(objuid);
								biresult.putValue("workloguid", objuid);
								biresult.putValue("emp_uid", emp_uid);
								String dMoney = OperationUtil.round(xj_money, 2);
								biresult.putValue("wallowance", dMoney);
								biresult.putValue("wdate", wdate);
								if(appstatus == null)
									appstatus = "&nbsp;";
								biresult.putValue("appstatus", appstatus);
								biresult.putValue("xjdays", xj_days);
								list.add(biresult);
							}
							
							// ���� cw_type �� waddress
							cw_type = rs.getString("cw_type");
							if("gscc".equals(cw_type)) {
								cw_type = "cc";
							}
							waddress = rs.getString("waddress");

							// ������ɺ�day����Ϊȱʡ�������ټ�����һ��
							day = day + 1;
						}
					}

					// ȡ�õ�ǰ�µ����������������ڣ�����ǰ���������·ݲ�һ�£����õ�ǰ�µ�������������ֻ��ʹ����������
					int intYear = Integer.parseInt(year);
					int intMonth = Integer.parseInt(month);
					int currentDays = OperationUtil.getMonthDay(intYear,
							intMonth);
					Date nowDate = new Date();

					String strNowDate = format.format(nowDate);
					String strDate[] = strNowDate.split("-");
					String nowYear = strDate[0];
					String nowMonth = strDate[1];
					String nowDay = strDate[2];
					int intNowYear = Integer.parseInt(nowYear);
					int intNowMonth = Integer.parseInt(nowMonth);
					int intNowDay = Integer.parseInt(nowDay);

					if (intNowYear == intYear && intNowMonth == intMonth) {
						// dayС��������������������ȱʡ����
						if (intNowDay > day-1) {
							int countDays = intNowDay - day+1;
							double subsidy = WLogBuzhuTongjiUtil
									.getBZbasic(waddress);
							// wdateΪ��Ϊȱʡ���ڣ� ��nowDate���һ�죬
							long time = nowDate.getTime() + 24 * 60 * 60 * 1000L;
							Date nextDay = new Date(time);
							// ����cw_type �� �ڼ��� �� ������׼ ���㲹��
							// ����ȱʡ����
							// money = money +
							// WLogBuzhuTongjiUtil.getQueshMoney(countDays,
							// nextDay, subsidy, format, cw_type);
							
							for (int i = 0; i < countDays; i++) {
								
								if ("cc".equals(cw_type)
										|| "xx".equals(cw_type)) {
									long time1 = nextDay.getTime() - (countDays - i)
											* 24 * 60 * 60 * 1000L;
									Date qsDate = new Date(time1);
									Calendar calQs = Calendar.getInstance();
									calQs.setTime(qsDate);
										// Ϊture������Ϊ�����ڼ��գ������׼������ *1.5
										if (WLogBuzhuTongjiUtil.ifHolidays(
												format.format(nextDay), false)) {
											money = subsidy * 1.5;
										} else {
											money = subsidy;
										}
								} else {
									money = 0.00;
								}
								
								String strdate = date;
								int dayI = day + i;
								if(dayI < 10) 
									strdate = strdate + "-0"+ dayI;
								else {
									strdate = strdate + "-"+ dayI;
								}
								if(cw_type != null && ("xx".equals(cw_type) || "xj".equals(cw_type))){
									BOInstance biresult = new BOInstance();
									biresult.setBo(this.service.getBo());
									biresult.setUid("");
									biresult.putValue("workloguid", "");
									biresult.putValue("emp_uid", emp_uid);
									String dMoney = OperationUtil.round(money, 2);
									biresult.putValue("wallowance", dMoney);
									biresult.putValue("wdate", strdate);
									list.add(biresult);
								}
							}
						}
					} else if ((intNowYear == intYear && intNowMonth > intMonth)
							|| intNowYear > intYear) {
						// dayС���µ�������+1������ȱʡ����
						if (currentDays > day-1) {
							int countDays = currentDays - day+1;
							double subsidy = WLogBuzhuTongjiUtil
									.getBZbasic(waddress);
							// wdateΪ��Ϊȱʡ���ڣ� ��nowDate���һ�죬
							String strdate = year + "-" + month + "-"
									+ currentDays;
							Date currdate = format.parse(strdate);
							long time = currdate.getTime() + 24 * 60 * 60
									* 1000L;
							Date nextDay = new Date(time);
							// ����cw_type �� �ڼ��� �� ������׼ ���㲹��
							// ����ȱʡ����
							// money = money +
							// WLogBuzhuTongjiUtil.getQueshMoney(countDays,
							// nextDay, subsidy, format, cw_type);
							for (int i = 0; i < countDays; i++) {
								if ("cc".equals(cw_type)
										|| "xx".equals(cw_type)) {
									long time1 = nextDay.getTime() - (countDays - i)
											* 24 * 60 * 60 * 1000L;
									Date qsDate = new Date(time1);
									Calendar calQs = Calendar.getInstance();
									calQs.setTime(qsDate);
										// Ϊture������Ϊ�����ڼ��գ������׼������ *1.5
										if (WLogBuzhuTongjiUtil.ifHolidays(
												format.format(nextDay), false)) {
											money = subsidy * 1.5;
										} else {
											money = subsidy;
										}
//									}
								} else {
									money = 0.00;
								}
								
								String strdate1 = date;
								int cdays = day + i;
								if(cdays < 10) 
									strdate1 = strdate1 + "-0"+cdays;
								else {
									strdate1 = strdate1 + "-"+cdays;
								}
								if(cw_type != null && ("xx".equals(cw_type) || "xj".equals(cw_type))){
									BOInstance biresult = new BOInstance();
									biresult.setBo(this.service.getBo());
									biresult.setUid("");
									biresult.putValue("workloguid", "");
									biresult.putValue("emp_uid", emp_uid);
									String dMoney = OperationUtil.round(money, 2);
									biresult.putValue("wallowance", dMoney);
									biresult.putValue("wdate", strdate1);
									list.add(biresult);
								}
							}
							
						}
					} else {
						// �������������δ�����������
					}

					if (rs != null) {
						rs.close();
					}
				}
			}

			this.setInstances(list);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.setEchoValue("��ѯʧ�ܣ�1001");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return DEFAULT_FORWARD;
	}

	public static void main(String[] args) {
	}

}
