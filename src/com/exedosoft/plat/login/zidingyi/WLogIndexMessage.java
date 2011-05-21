package com.exedosoft.plat.login.zidingyi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.login.zidingyi.excel.MySqlOperation;
import com.exedosoft.plat.login.zidingyi.excel.OperationUtil;
import com.exedosoft.plat.util.DOGlobals;

public class WLogIndexMessage {
	/**
	 * ������ҳ��ʾ��Ϣ
	 * */

	/**
	 * ��ȡ���β�����Ϣ messages[0]:�������� messages[1]:�������ͳ��; messages[2]:����ص�;
	 * messages[3]:�ݼ�����; messages[4]:��н�ݼ�����
	 * */
	public static String[] getBZMessage() {
		String[] messages = new String[5];
		String loginEmp = DOGlobals.getInstance().getSessoinContext()
				.getInstance().getUser().getUid();

		// ��ѯ�Ƿ��г����¼��
		String sql = "select cw_type, wdate, wseladdress from cw_worklog where wdate = (select max(l.wdate) from cw_worklog l where l.emp_uid = '"
				+ loginEmp + "' and l.cw_type like '%cc%')";
		System.out.println("sql::::::::::"+sql);
		Connection conn = MySqlOperation.getConnection();
		try {

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			// �Ƿ��г����¼
			boolean isHaveCC = false;
			// ����ĳ����¼�Ƿ�Ϊ'gscc'
			boolean isGSCC = false;
			// ����ĳ����¼����
			Date lastedWdate = null;
			// ��ǰ״̬�Ƿ�Ϊ����
			boolean isChuchai = false;
			ResultSet rs = MySqlOperation.wLogBySql(conn, sql);
			String wseladdress = "";
			while (rs.next()) {
				isHaveCC = true;
				lastedWdate = rs.getDate("wdate");
				wseladdress = rs.getString("wseladdress");
				String cwtype = rs.getString("cw_type");
				if (cwtype != null && "gscc".equals(cwtype)) {
					isGSCC = true;
				}
			}
			rs.close();

			if (!isHaveCC) {
				// û�г����¼����ǰ״̬Ϊ���ڳ���״̬;
				isChuchai = false;
			} else {
				// �г����¼�����жϵ�ǰ״̬ʼ��Ϊ����״̬;
				// �ж��Ƿ����ڹ�˾�ļ�¼��������������ĳ����¼���ڣ����У���ǰ״̬Ϊ���ڳ��
				// �ж��Ƿ����ڹ�˾�ļ�¼��������������ĳ����¼���ڵı�־
				boolean isHaveGs = false;
				sql = "select w.* from cw_worklog w where w.cw_type = 'gs' and w.emp_uid = '"
						+ loginEmp + "' and w.wdate > '" + lastedWdate + "' ";
				rs = MySqlOperation.wLogBySql(conn, sql);

				while (rs.next()) {
					isHaveGs = true;
					break;
				}
				rs.close();
				if (isHaveGs) {
					isChuchai = false;
				} else {
					isChuchai = true;
				}
			}

			// ����ǰû�г����ֱ�ӷ���messages[0] = "0";messages[1] = "0";
			if (!isChuchai) {
				messages[0] = "0";
				messages[1] = "0";
				messages[2] = "";
				messages[3] = "0";
				messages[4] = "0";
			} else {
				// ����ǰΪ�������ȡʱ�����Ķ���cw_type��Ϊ'cc'�ļ�¼������ڸ�ʱ�����С���ڼ�Ϊ����Ŀ�ʼʱ��;
				// ������Ҫ�ж��Ƿ����ڹ�˾(cw_type='gs')�ļ�¼,��û����ȡʱ����С��Ϊmindate
				if (isGSCC) {
					String gsccDate = format.format(lastedWdate);
					sql = "select * from cw_worklog w where  emp_uid = '"
							+ loginEmp + "' and wdate >= '" + gsccDate
							+ "' order by w.wdate";
				} else {
					sql = "select w.* from cw_worklog w where  w.emp_uid = '"
							+ loginEmp
							+ "' and (if((select max(l.wdate) from cw_worklog l where l.cw_type like '%gs%' and l.emp_uid = '"
							+ loginEmp
							+ "') is null, w.wdate >= (select min(l.wdate) from cw_worklog l where l.emp_uid = '"
							+ loginEmp
							+ "'),if((select cw_type from cw_worklog where emp_uid = w.emp_uid and wdate = (select max(l.wdate) from cw_worklog l where l.cw_type like '%gs%' and l.emp_uid = '"
							+ loginEmp
							+ "'))='gs',w.wdate >(select max(l.wdate) from cw_worklog l where l.cw_type like '%gs%' and l.emp_uid = '"
							+ loginEmp
							+ "'),w.wdate >= (select max(l.wdate) from cw_worklog l where l.cw_type like '%gs%' and l.emp_uid = '"
							+ loginEmp + "')))) order by w.wdate";
				}

				rs = MySqlOperation.wLogBySql(conn, sql);
				Date mindate = new Date();
				Date tempdate = null;
				Date maxdate = new Date();
				long time = 0;

				int days = 1;
				double ccmoney = 0.00;
				double ccdays = 0.00;
				double xiujiaDays = 0;
				double xinjiaDays = 0;
				String cw_type = "gs";
				Calendar cal = Calendar.getInstance();
				// ȱʡ��¼��Ҫ�Ĳ���
				String lastcw_type = "cc";
				double subsidy = 60;

				while (rs.next()) {
					String wlog_uid = rs.getString("WorklogUID");
					if (wlog_uid == null)
						wlog_uid = rs.getString("workloguid");
					cw_type = rs.getString("cw_type");
					if ("gscc".equals(cw_type)) {
						cw_type = "cc";
					}
					Date wdate = rs.getDate("wdate");
					String waddress = rs.getString("waddress");
					double wallowance = rs.getDouble("wallowance");
					String ifhavexc = rs.getString("ifhavexc");
					cal.setTime(wdate);
					// �ų��ݼ����
					if ("cc".equals(cw_type) && days == 1) {
						mindate = wdate;
						// ������һ��Ĳ�����Ȼ��days++
						// �ж���һ���Ƿ�Ϊ�ڼ���

						if (ifhavexc != null) {
							List<BOInstance> list = WLogBuzhuTongjiUtil
									.findCW_XC(wlog_uid);
							if (list != null && list.size() > 0) {
								for (int l = 0; l < list.size(); l++) {
									BOInstance bi = list.get(l);
									double xc_days = bi
											.getDoubleValue("xc_days");
									double xc_money = bi
											.getDoubleValue("xc_money");

									// if (cal.get(Calendar.DAY_OF_WEEK) ==
									// Calendar.SUNDAY
									// || cal.get(Calendar.DAY_OF_WEEK) ==
									// Calendar.SATURDAY) {
									//
									// // Ϊture����˫����Ϊ���������գ��ʲ������ظ��ģ������� *2/3
									// if (!WLogBuzhuTongjiUtil.ifHolidays(
									// format.format(wdate), true)) {
									// xc_money = xc_money * 2 / 3;
									// }
									// } else {
									// Ϊture������Ϊ�����ڼ��գ������� *1.5
									if (WLogBuzhuTongjiUtil.ifHolidays(
											format.format(wdate), false)) {
										xc_money = xc_money * 1.5;
									}
									// }

									ccmoney = ccmoney + xc_money;
									ccdays = ccdays + xc_days;
								}
							}
							
							if ("xj".equals(cw_type)) {
								List<BOInstance> xjlist = WLogBuzhuTongjiUtil
										.findCW_XC(wlog_uid, "x");
								if (xjlist != null && xjlist.size() > 0) {
									for (int l = 0; l < xjlist.size(); l++) {
										BOInstance bi = xjlist.get(l);
										double xc_days = bi
												.getDoubleValue("xc_days");
										double xc_money = bi
												.getDoubleValue("xc_money");
										xiujiaDays = xiujiaDays + xc_days;
									}
								}
						} else if ("xx".equals(cw_type)) {
								List<BOInstance> xxlist = WLogBuzhuTongjiUtil
										.findCW_XC(wlog_uid, "x");
								if (xxlist != null && xxlist.size() > 0) {
									for (int l = 0; l < xxlist.size(); l++) {
										BOInstance bi = xxlist.get(l);
										double xc_days = bi
												.getDoubleValue("xc_days");
										double xc_money = bi
												.getDoubleValue("xc_money");
										xinjiaDays = xinjiaDays + xc_days;
									}
								}
						}
							
						}
						// ��ӳ�������
						days++;
						// ����ȱʡ��Ҫ�Ĳ���
						lastcw_type = "cc";
						subsidy = WLogBuzhuTongjiUtil.getBZbasic(waddress);
					} else if (days > 1) {
						// �����ж�ʼ��Ϊ��������
						// ����˺�Ĳ������谴cw_type���ͼ��㣬�ݼ�����Ϊ�㣬н��������㲹��
						time = mindate.getTime() + (days - 1) * 24 * 60 * 60
								* 1000L;
						tempdate = new Date(time);
						int tempdays = (int) ((wdate.getTime() - tempdate
								.getTime()) / (24 * 60 * 60 * 1000L));

						// tempdays > 0,����ȱʡ���ڣ������
						if (tempdays > 0) {
							ccmoney = ccmoney
									+ WLogBuzhuTongjiUtil.getQueshMoney(
											tempdays, wdate, subsidy, format,
											lastcw_type);

							ccdays = ccdays + tempdays;
							days = days + tempdays;

							if ("xj".equals(lastcw_type)) {
								xiujiaDays = xiujiaDays + tempdays;
							} else if ("xx".equals(lastcw_type)) {
								xinjiaDays = xinjiaDays + tempdays;
							}
						}
						// ����ȱʡ���ں��ټ��㱾����¼
						if (ifhavexc != null) {
							List<BOInstance> list = WLogBuzhuTongjiUtil
									.findCW_XC(wlog_uid);
							if (list != null && list.size() > 0) {
								for (int l = 0; l < list.size(); l++) {
									BOInstance bi = list.get(l);
									double xc_days = bi
											.getDoubleValue("xc_days");
									double xc_money = bi
											.getDoubleValue("xc_money");
									// if (cal.get(Calendar.DAY_OF_WEEK) ==
									// Calendar.SUNDAY
									// || cal.get(Calendar.DAY_OF_WEEK) ==
									// Calendar.SATURDAY) {
									//
									// // Ϊture����˫����Ϊ���������գ��ʲ������ظ��ģ������� *2/3
									// if (!WLogBuzhuTongjiUtil.ifHolidays(
									// format.format(wdate), true)) {
									// xc_money = xc_money * 2 / 3;
									// }
									// } else {
									// Ϊture������Ϊ�����ڼ��գ������� *1.5
									if (WLogBuzhuTongjiUtil.ifHolidays(
											format.format(wdate), false)) {
										xc_money = xc_money * 1.5;
									}
									// }

									ccmoney = ccmoney + xc_money;
									ccdays = ccdays + xc_days;
								}
							}
						}
						// ����ȱʡ��Ҫ�Ĳ���
						lastcw_type = cw_type;
						subsidy = WLogBuzhuTongjiUtil.getBZbasic(waddress);
						// ��ӳ�������
						days++;
						if ("xj".equals(cw_type)) {
								List<BOInstance> list = WLogBuzhuTongjiUtil
										.findCW_XC(wlog_uid, "x");
								if (list != null && list.size() > 0) {
									for (int l = 0; l < list.size(); l++) {
										BOInstance bi = list.get(l);
										double xc_days = bi
												.getDoubleValue("xc_days");
										double xc_money = bi
												.getDoubleValue("xc_money");
										xiujiaDays = xiujiaDays + xc_days;
									}
								}
						} else if ("xx".equals(cw_type)) {
								List<BOInstance> list = WLogBuzhuTongjiUtil
										.findCW_XC(wlog_uid, "x");
								if (list != null && list.size() > 0) {
									for (int l = 0; l < list.size(); l++) {
										BOInstance bi = list.get(l);
										double xc_days = bi
												.getDoubleValue("xc_days");
										double xc_money = bi
												.getDoubleValue("xc_money");
										xinjiaDays = xinjiaDays + xc_days;
									}
								}
						}
					}

				}
				rs.close();

				// ��¼�����󣬲鿴���һ����¼�Ƿ�Ϊ���죬�����ǣ����������ȱʡ������
				// days
				// ������һ����������days=1���������mindate��Ŀǰmindate+(days-1)�Ծ�Ϊ��һ����������mindate+1���������mindate����һ��
				time = mindate.getTime() + (days - 1) * 24 * 60 * 60 * 1000L;
				tempdate = new Date(time);

				// ����Ϊ WLogBuzhuTongjiUtil.getQueshMoney(tempdays,maxdate,
				// subsidy, format, lastcw_type);�е�maxdateΪ��ȱʡ���ڣ���maxdate���һ��
				// ��tempdays = (int) ((maxdate.getTime() - tempdate.getTime()) /
				// (24 * 60 * 60 * 1000L));������+1
				time = maxdate.getTime() + 24 * 60 * 60 * 1000L;
				maxdate = new Date(time);
				int tempdays = (int) ((maxdate.getTime() - tempdate.getTime()) / (24 * 60 * 60 * 1000L));
				if (tempdays > 0) {
					ccmoney = ccmoney
							+ WLogBuzhuTongjiUtil.getQueshMoney(tempdays,
									maxdate, subsidy, format, lastcw_type);
					ccdays = ccdays + tempdays;
					days = days + tempdays;
					if ("xj".equals(lastcw_type)) {
						xiujiaDays = xiujiaDays + tempdays;
					} else if ("xx".equals(lastcw_type)) {
						xinjiaDays = xinjiaDays + tempdays;
					}
				}
				messages[0] = OperationUtil.round(ccdays, 2);
				messages[1] = OperationUtil.round(ccmoney, 2);
				messages[2] = wseladdress;
				messages[3] = OperationUtil.round(xiujiaDays, 2);
				messages[4] = OperationUtil.round(xinjiaDays, 2);

			}

		} catch (SQLException e) {
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

		return messages;
	}

	public static List<String[]> getEmpMessage() {
		List<String[]> list = new ArrayList<String[]>();
		String[] empMgs = new String[2];
		String loginEmp = DOGlobals.getInstance().getSessoinContext()
				.getInstance().getUser().getUid();
		String loginDept = DOGlobals.getInstance().getSessoinContext()
				.getInstance().getUser().getValue("deptuid");
		//do_org_user_link_browse_dept_by_form
		DOService deptSer = DOService.getService("do_org_user_link_browse_dept_by_form");
		List<BOInstance> deptList = new ArrayList<BOInstance>();
		deptList = deptSer.invokeSelect(loginDept);
		if(deptList != null && deptList.size() > 0) {
			//cw_worklog_browse_get_new_jilu_by_emp_uid
			String[] str = null;
			for(int i = 0; i < deptList.size(); i++) {
				str = new String[2];
				BOInstance empBi = deptList.get(i);
				String user_uid = empBi.getValue("user_uid");
				DOService empser = DOService.getService("zf_employee_browse");
				List<BOInstance> emp = new ArrayList<BOInstance>();
				emp = empser.invokeSelect(user_uid); 
				String userName = null;
				if(emp.size()>0) {
					BOInstance empBi2 = emp.get(0);
					userName = empBi2.getValue("cn");
				}
				if(userName == null || "".equals(userName.trim())){
					userName = user_uid;
				}
				DOService wlogSer = DOService.getService("cw_worklog_browse_get_new_jilu_by_emp_uid");
				List<BOInstance> wlogList = new ArrayList<BOInstance>();
				wlogList = wlogSer.invokeSelect(user_uid);
				
				String address = "δ֪";
				if(wlogList != null && wlogList.size() > 0) {
					BOInstance wlogBi = wlogList.get(0);
					String waddress = wlogBi.getValue("waddress");
					String wseladdress = wlogBi.getValue("wseladdress");
					String wdate = wlogBi.getValue("wdate");
					if(waddress != null && "1".equals(waddress.trim())){
						address = "��˾�򱾵�";
					} else if(waddress != null) {
						address = wseladdress;
					}
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					String nowsd = format.format(new Date());
					if(wdate.compareTo(nowsd)== 0) {
						
					} else if(wdate.compareTo(nowsd) > 0){
						
					} else {
						
					}
				}
				str[0] = userName;
				str[1] = address;
				list.add(str);
			}
		}
		
		return list;
	}
	
	public static String[] getReWuList() {
		String[] rwList = new String[2];
		String loginEmp = DOGlobals.getInstance().getSessoinContext()
				.getInstance().getUser().getUid();
		String loginDept = DOGlobals.getInstance().getSessoinContext()
				.getInstance().getUser().getValue("deptuid");
		List<String> roles = new ArrayList<String>();
		DOService deptmSer = DOService.getService("do_org_user_link_browse_find_depm_by_dept");
		List<BOInstance> deptmList = new ArrayList<BOInstance>();
		deptmList = deptmSer.invokeSelect(loginDept);
		if(deptmList != null && deptmList.size() > 0) {
			for(int i = 0; i < deptmList.size(); i++) {
				String role = null;
				BOInstance roleBi = deptmList.get(i);
				role = roleBi.getValue("user_uid");
				if(role != null)
					roles.add(role);
			}
			
		}
		
		DOService rwlbSer  = null;
		
		//��¼��Ϊ���ž���
		if(roles != null && loginEmp != null && roles.contains(loginEmp.trim())) {
			rwlbSer = DOService.getService("cw_worklog_auto_condition_logsp_dept");
			rwList[1] = "bmjl";
		}
		//��¼��Ϊ�ܾ���
		else if(roles != null && loginEmp != null && !roles.contains(loginEmp.trim())) {
			DOService deptzSer = DOService.getService("do_org_user_link_browse_find_zjl");
			List<BOInstance> deptzList = new ArrayList<BOInstance>();
			deptzList = deptzSer.invokeSelect(loginDept);
			if(deptzList != null && deptzList.size() > 0) {
				for(int i = 0; i < deptzList.size(); i++) {
					String role = null;
					BOInstance roleBi = deptzList.get(i);
					role = roleBi.getValue("user_uid");
					if(role != null)
						roles.add(role);
				}
			}
			if(roles != null && loginEmp != null && roles.contains(loginEmp.trim())) {
				rwlbSer = DOService.getService("cw_worklog_auto_condition_logsp_zongjl");
				rwList[1] = "zjl";
			}
		}
		//�����б����Ϊ�գ���õ�¼��Ϊ���ž�����ܾ���
		if(rwlbSer != null) {
			List<BOInstance> wlogList = new ArrayList<BOInstance>();
			wlogList = rwlbSer.invokeSelect();
			if(wlogList != null)
				rwList[0] = wlogList.size()+"";
		} else {
			return null;
		}
		
		
		return rwList;
	}
	

	public static void main(String[] args) {
		String wdate = "2010-11-09";
		String nowsd = "2010-11-10";
		if(wdate.compareTo(nowsd)== 0) {
			System.out.println(wdate.compareTo(nowsd));
		} else if(wdate.compareTo(nowsd) > 0){
			System.out.println(wdate.compareTo(nowsd));
		} else {
			System.out.println(wdate.compareTo(nowsd));
		}
		
		
		

	}

}
