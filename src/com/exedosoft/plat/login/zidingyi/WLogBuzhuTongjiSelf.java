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
import com.exedosoft.plat.util.DOGlobals;

public class WLogBuzhuTongjiSelf extends DOAbstractAction {
	public String excute() {

		// ��Ҫ���յ����ݣ���ѯ����
		String seltype = null;// ��ѯ���ͣ�0���죻1���£�2����;
		String selname = null;// Ա������ ;
		String selrange = null;// ��ѯ��Χ��dept�����ţ�all������ ;
		String seldept_uid = null;// ����uid ;

		List users = new ArrayList();
		try {
			selrange = "geren";
			DOService service = DOService
					.getService("cw_worklog_browse_allowance_ck_geren");
			users = service.invokeSelect();
		} catch (Exception e) {
			this.setEchoValue("��ѯʧ�ܣ�error" + e.toString());
			return "notpass";
		}

		// �������е����ݣ�
		if (users != null && users.size() > 0) {
			BOInstance bi = (BOInstance) users.get(0);
			seltype = bi.getValue("seltype");
			selname = bi.getValue("selname");
			selrange = bi.getValue("selrange");
			seldept_uid = bi.getValue("seldept_uid");
			if (seltype != null
					&& (seltype.trim().length() == 0 || "0".equals(seltype
							.trim())))
				seltype = null;
			if (selname != null && selname.trim().length() == 0)
				selname = null;

			if (selname == null) {
				String user_uid = DOGlobals.getInstance().getSessoinContext()
						.getInstance().getUser().getUid();
				selname = user_uid;
			}

			String year = null;
			String month = null;
			if (seltype == null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH, -1);
				String strDate = sdf.format(calendar.getTime());
				seltype = strDate;
			}

			String[] one = seltype.split("-");
			year = one[0];
			month = one[1];

			String sql = "";
			Connection conn = MySqlOperation.getConnection();
			try {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				List<String> listemp_uid = new ArrayList<String>();
				
				//���˲�ѯ��ֱ�Ӽ��뼴��
				listemp_uid.add(selname);
				
				List<BOInstance> list = new ArrayList<BOInstance>();
				for (int cemp = 0 ; cemp < listemp_uid.size(); cemp++) {
					String emp_uid = listemp_uid.get(cemp);
					//�Ƿ����С����һ��ļ�¼����û�У���������
					DOService findminDate = DOService
					.getService("cw_worklog_browse_get_new_jilu_by_date");
					List<BOInstance> listmin = new ArrayList<BOInstance>();
					int monthDays = OperationUtil.getMonthDay(Integer.parseInt(year),
							Integer.parseInt(month));
					String mindate = year+"-"+month+"-"+monthDays;
					listmin = findminDate.invokeSelect(emp_uid,mindate);
					if(listmin == null || listmin.size() <= 0) {
						continue;
					}
					//��δ������ϣ���ͳ�ƣ�����ʾ
					boolean ifappstatus = false;
					String appstatus = null;
					if (emp_uid != null && !"".equals(emp_uid.trim())) {
						String workloguid = emp_uid + "-" + year + "-" + month;
						String date = year + "-" + month;
						double money = 0.00;
						sql = "select * from cw_worklog where  emp_uid = '"
								+ emp_uid
								+ "' and year(wdate) = "
								+ year
								+ " and month(wdate)="
								+ month
								+ " order by wdate ";

						 System.out.println("========================");
						 System.out.println(sql);
						 System.out.println("========================");
						
						/*
						 * ���ڼ���ȱʡ���ڵĲ��� cw_type: ǰһ��¼�����ͣ���˾'gs'������'cc'���ݼ�'xj'
						 */

						ResultSet rs = MySqlOperation.wLogBySql(conn, sql);
						int day = 1;
						String waddress = "1";
						String cw_type = "gs";
						while (rs != null && rs.next()) {
							String wlog_uid = rs.getString("WorklogUID");
							if(wlog_uid == null)
								wlog_uid = rs.getString("workloguid");
							Date wdate = rs.getDate("wdate");
							cal.setTime(wdate);
							String ifhavexc = rs.getString("ifhavexc");
							
							if(!ifappstatus){
								appstatus = rs.getString("appstatus");
								if(!"3".equals(appstatus)) {
									ifappstatus = true;
								}
							}
							if (cal.get(Calendar.DAY_OF_MONTH) == day) {
								if(ifhavexc != null && ifhavexc.indexOf("c") != -1){
									List<BOInstance> listxc = WLogBuzhuTongjiUtil.findCW_XC(wlog_uid);
									if(listxc != null && listxc.size() > 0) {
										for(int l = 0; l < listxc.size(); l ++) {
											BOInstance bixc = listxc.get(l);
											double xc_days = bixc.getDoubleValue("xc_days");
											double xc_money = bixc.getDoubleValue("xc_money");
//											if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
//													|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
//
//												// Ϊture����˫����Ϊ���������գ��ʲ������ظ��ģ������� *2/3
//												if (!WLogBuzhuTongjiUtil.ifHolidays(
//														format.format(wdate), true)) {
//													xc_money = xc_money * 2 / 3;
//												}
//											} else {
												// Ϊture������Ϊ�����ڼ��գ������� *1.5
												if (WLogBuzhuTongjiUtil.ifHolidays(
														format.format(wdate), false)) {
													xc_money = xc_money * 1.5;
												} 
//											}
											
											money = money + xc_money;
										}
									}
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
									String[] str = WLogBuzhuTongjiUtil.getCw_type(emp_uid,
											format.format(wdate));
									// ���� cw_type �� waddress
									cw_type = str[0];
									waddress = str[1];
								}
								// ȡ�ò�����׼
								double subsidy = WLogBuzhuTongjiUtil.getBZbasic(waddress);
								int nowDays = cal.get(Calendar.DAY_OF_MONTH)
										- day;
								// ����cw_type �� �ڼ��� �� ������׼ ���㲹��
								// ����ȱʡ����
								money = money + WLogBuzhuTongjiUtil.getQueshMoney(nowDays, wdate, subsidy, format, cw_type);

								// ������ȱʡ���ں��ټ�����һ���
								if(ifhavexc != null && ifhavexc.indexOf("c") != -1){
									List<BOInstance> listxc = WLogBuzhuTongjiUtil.findCW_XC(wlog_uid);
									if(listxc != null && listxc.size() > 0) {
										for(int l = 0; l < listxc.size(); l ++) {
											BOInstance bixc = listxc.get(l);
											double xc_days = bixc.getDoubleValue("xc_days");
											double xc_money = bixc.getDoubleValue("xc_money");
											
//											if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
//													|| cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
//
//												// Ϊture����˫����Ϊ���������գ��ʲ������ظ��ģ������� *2/3
//												if (!WLogBuzhuTongjiUtil.ifHolidays(
//														format.format(wdate), true)) {
//													xc_money = xc_money * 2 / 3;
//												}
//											} else {
												// Ϊture������Ϊ�����ڼ��գ������� *1.5
												if (WLogBuzhuTongjiUtil.ifHolidays(
														format.format(wdate), false)) {
													xc_money = xc_money * 1.5;
												} 
//											}
											
											money = money + xc_money;
										}
									}
								}

								// ���� cw_type �� waddress
								cw_type = rs.getString("cw_type");
								if("gscc".equals(cw_type)) {
									cw_type = "cc";
								}
								waddress = rs.getString("waddress");

								// ������ɺ�day����Ϊȱʡ�������ټ�����һ��
								day = day + nowDays + 1;
							}
						}
						
						//ȡ�õ�ǰ�µ����������������ڣ�����ǰ���������·ݲ�һ�£����õ�ǰ�µ�������������ֻ��ʹ����������
						int intYear = Integer.parseInt(year);
						int intMonth = Integer.parseInt(month);
						int currentDays = OperationUtil.getMonthDay(intYear, intMonth);
						Date nowDate = new Date();
						
						String strNowDate = format.format(nowDate);
						String strDate[] = strNowDate.split("-");
						String nowYear = strDate[0];
						String nowMonth = strDate[1];
						String nowDay = strDate[2];
						int intNowYear = Integer.parseInt(nowYear);
						int intNowMonth = Integer.parseInt(nowMonth);
						int intNowDay = Integer.parseInt(nowDay);
						
						if(intNowYear == intYear && intNowMonth == intMonth) {
							//dayС��������������+1������ȱʡ����
							if(intNowDay > day-1) {
								int countDays = intNowDay - day+1;
								double subsidy = WLogBuzhuTongjiUtil.getBZbasic(waddress);
								//wdateΪ��Ϊȱʡ���ڣ� ��nowDate���һ�죬
								long time = nowDate.getTime() + 24 * 60 * 60 * 1000L;
								Date nextDay = new Date(time);
								// ����cw_type �� �ڼ��� �� ������׼ ���㲹��
								// ����ȱʡ����
								money = money + WLogBuzhuTongjiUtil.getQueshMoney(countDays, nextDay, subsidy, format, cw_type);
							}
						} else if ((intNowYear == intYear && intNowMonth > intMonth) ||intNowYear > intYear) {
							//dayС���µ�������+1������ȱʡ����
							if(currentDays > day-1) {
								int countDays = currentDays - day+1;
								double subsidy = WLogBuzhuTongjiUtil.getBZbasic(waddress);
								//wdateΪ��Ϊȱʡ���ڣ� ��nowDate���һ�죬
								String strdate = year+"-"+month+"-"+currentDays;
								Date currdate = format.parse(strdate);
								long time = currdate.getTime() + 24 * 60 * 60 * 1000L;
								Date nextDay = new Date(time);
								// ����cw_type �� �ڼ��� �� ������׼ ���㲹��
								// ����ȱʡ����
								money = money + WLogBuzhuTongjiUtil.getQueshMoney(countDays, nextDay, subsidy, format, cw_type);
							}
						} else {
							//�������������δ�����������
						}
						
						
						if(rs != null) {
							rs.close();
						}
						BOInstance biresult = new BOInstance();
						biresult.setBo(this.service.getBo());
						biresult.setUid(workloguid);
						biresult.putValue("workloguid", workloguid);
						biresult.putValue("name", emp_uid);
						String dMoney = OperationUtil.round(money, 2);
						biresult.putValue("money", dMoney);
						if(ifappstatus) {
							if(appstatus == null)
								appstatus = "&nbsp;";
							biresult.putValue("appstatus", appstatus);
						} else {
							biresult.putValue("appstatus", "3");
						}
						biresult.putValue("date", date);
						list.add(biresult);
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

		} else {
			this.setEchoValue("��ѯʧ�ܣ�1002");
		}

		return DEFAULT_FORWARD;
	}

	public static void main(String[] args) {
	}

}
