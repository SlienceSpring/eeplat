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
import com.exedosoft.plat.util.DOGlobals;

public class WLogBuzhuCurrentMonthDetail extends DOAbstractAction {
	public String excute() {

		// ��Ҫ���յ����ݣ���ѯ����
		int year = 0;// ���
		int month = 0;// �·�
	
		Connection conn = MySqlOperation.getConnection();
		try {
			Calendar cal = Calendar.getInstance();

			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH)+1;
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			String sql = "";
			List<BOInstance> list = new ArrayList<BOInstance>();
				String emp_uid = DOGlobals.getInstance().getSessoinContext().getInstance().getUser().getUid();
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
					while (rs != null && rs.next()) {
						String wlog_uid  = rs.getString("workloguid");
						Date wdate = rs.getDate("wdate");
						String objuid = rs.getString("workloguid");
						String ifhavexc = rs.getString("ifhavexc");
						String appstatus = rs.getString("appstatus");
						cal.setTime(wdate);
						if (cal.get(Calendar.DAY_OF_MONTH) == day) {
							double xj_money = 0.00;
							double xj_days = 0.00;
							if(ifhavexc != null && ifhavexc.indexOf("c") != -1){
								List<BOInstance> listxc = WLogBuzhuTongjiUtil.findCW_XC(wlog_uid);
								if(listxc != null && listxc.size() > 0) {
									for(int l = 0; l < listxc.size(); l ++) {
										BOInstance bixc = listxc.get(l);
										double xc_days = bixc.getDoubleValue("xc_days");
										double xc_money = bixc.getDoubleValue("xc_money");
											// Ϊture������Ϊ�����ڼ��գ������� *1.5
											if (WLogBuzhuTongjiUtil.ifHolidays(
													format.format(wdate), false)) {
												xc_money = xc_money * 1.5;
											} 
										xj_money = xc_money;
										xj_days = xc_days;
									}
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
							biresult.putValue("appstatus", appstatus);
							biresult.putValue("xjdays", xj_days);
							list.add(biresult);

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
								
								
								
								BOInstance biresult = new BOInstance();
								biresult.setBo(this.service.getBo());
								biresult.setUid("");
								biresult.putValue("workloguid", "");
								biresult.putValue("emp_uid", emp_uid);
								String dMoney = OperationUtil.round(money, 2);
								biresult.putValue("wallowance", dMoney);
								biresult.putValue("wdate", strdate);
								list.add(biresult);
								day ++;
							}

							
							double xj_money = 0.00;
							double xj_days = 0.00;
							// ������ȱʡ���ں��ټ�����һ���
							if(ifhavexc != null && ifhavexc.indexOf("c") != -1){
								List<BOInstance> listxc = WLogBuzhuTongjiUtil.findCW_XC(wlog_uid);
								
								if(listxc != null && listxc.size() > 0) {
									for(int l = 0; l < listxc.size(); l ++) {
										BOInstance bixc = listxc.get(l);
										double xc_days = bixc.getDoubleValue("xc_days");
										double xc_money = bixc.getDoubleValue("xc_money");
											// Ϊture������Ϊ�����ڼ��գ������� *1.5
											if (WLogBuzhuTongjiUtil.ifHolidays(
													format.format(wdate), false)) {
												xc_money = xc_money * 1.5;
											} 
										xj_money = xc_money;
										xj_days = xc_days;
									}
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
							biresult.putValue("appstatus", appstatus);
							biresult.putValue("xjdays", xj_days);
							list.add(biresult);

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
					int intYear = year;
					int intMonth = month;
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

//									if (calQs.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
//											|| calQs.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
//										// Ϊture����˫����Ϊ���������գ��ʱ�׼���� ���ø��ģ������� *1.5
//										if (WLogBuzhuTongjiUtil.ifHolidays(
//												format.format(nextDay), true)) {
//											money = subsidy;
//										} else {
//											money = subsidy * 1.5;
//										}
//									} else {
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
								
								String strdate = date;
								int dayI = day + i;
								if(dayI < 10) 
									strdate = strdate + "-0"+ dayI;
								else {
									strdate = strdate + "-"+ dayI;
								}
								
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
							long time = currdate.getTime() + 24 * 60 * 60 * 1000L;
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

//									if (calQs.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
//											|| calQs.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
//										// Ϊture����˫����Ϊ���������գ��ʱ�׼���� ���ø��ģ������� *1.5
//										if (WLogBuzhuTongjiUtil.ifHolidays(
//												format.format(nextDay), true)) {
//											money = subsidy;
//										} else {
//											money = subsidy * 1.5;
//										}
//									} else {
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
								int intday = day + i;
								if(intday < 10) 
									strdate1 = strdate1 + "-0"+ intday;
								else {
									strdate1 = strdate1 + "-"+ intday;
								}
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
					} else {
						// �������������δ�����������
					}

					if (rs != null) {
						rs.close();
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
		Calendar currCal = Calendar.getInstance();
		int year = currCal.get(Calendar.YEAR);
		int month = currCal.get(Calendar.MONTH);
		int day = currCal.get(Calendar.DATE);
		System.out.println(year);
		System.out.println(month);
		System.out.println(day);
	}

}
