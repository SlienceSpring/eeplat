package com.exedosoft.plat.login.zidingyi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.exedosoft.plat.ExedoException;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.util.id.UUIDHex;

public class WLogBuzhuTongjiUtil {

	public static String[] getCw_type(String emp_uid, String wdate) {
		// û�б���ص���Ϣʱ����ȡ��һ����¼�ж�
		// str[0]:cw_type
		// str[1]:waddress
		// ���㲹��ʱ����Ҫ��������������

		String[] str = new String[2];

		// ///cw_worklog_browse_getcw_type_last_byform
		// //select cw_type,waddress from cw_worklog where emp_uid = ? and wdate
		// < ? order by wdate desc limit 0,1

		DOService cw_typeSevr = DOService
				.getService("cw_worklog_browse_getcw_type_last_byform");
		List cw_typeList = new ArrayList();
		try {
			cw_typeList = cw_typeSevr.invokeSelect(emp_uid, wdate);
			if (cw_typeList != null && cw_typeList.size() > 0) {
				// �м�¼����ȡcw_type
				BOInstance bi = (BOInstance) cw_typeList.get(0);
				str[0] = bi.getValue("cw_type");
				if ("gscc".equals(str[0])) {
					str[0] = "cc";
				}
				str[1] = bi.getValue("waddress");
			} else {
				// ��ǰ��û�м�¼����Ĭ��Ϊ�ڹ�˾��������Ϊ0
				str[0] = "gs";
				str[1] = "1";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return str;
	}

	public static double getBZbasic(String waddress) {
		// û�б���ص���Ϣʱ����ȡ��һ����¼�ж�
		double subsidy = 0.00;

		// ///cw_citysubsidy_browse_subsidy_by_typecode
		// //select subsidy from cw_citysubsidy where typecode = ?

		DOService cw_typeSevr = DOService
				.getService("cw_citysubsidy_browse_subsidy_by_typecode");
		List cw_typeList = new ArrayList();
		try {
			cw_typeList = cw_typeSevr.invokeSelect(waddress);
			if (cw_typeList != null && cw_typeList.size() > 0) {
				// �м�¼����ȡcw_type
				BOInstance bi = (BOInstance) cw_typeList.get(0);
				subsidy = bi.getDoubleValue("subsidy");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return subsidy;
	}

	public static boolean ifHolidays(String wdate, boolean isSunday) {
		// û�б���ص���Ϣʱ����ȡ��һ����¼�ж�
		boolean flag = false;

		if (isSunday) {
			// ����˫���ղ�����˫���������Ƿ��������գ����ø��ģ������谴 *2/3���㣻
			// ����˫�����Ƿ�Ϊ���������գ�select count(*) as result from cw_holidays h where
			// h.daytype='1' and h.holiday = wdate �� result > 0,�����ڼ���
			DOService sundaySevr = DOService
					.getService("cw_holidays_browse_if_sunday_by_holiday");

			List sundayList = new ArrayList();
			try {
				sundayList = sundaySevr.invokeSelect(wdate);
				if (sundayList != null && sundayList.size() > 0) {
					flag = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			// ���������Ƿ�Ϊ�����ڼ��գ����ǣ����谴*1.5����,�����ø��ģ�
			// ����˫�����Ƿ�Ϊ���������գ�select count(*) as result from cw_holidays h where
			// h.daytype='2' and h.holiday = wdate �� result > 0,����������
			DOService workdaySevr = DOService
					.getService("cw_holidays_browse_if_workday_by_holiday");
			List workdayList = new ArrayList();
			try {
				workdayList = workdaySevr.invokeSelect(wdate);
				if (workdayList != null && workdayList.size() > 0) {
					flag = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return flag;
	}

	//
	public static double getQueshMoney(int nowDays, Date wdate, double subsidy,
			SimpleDateFormat format, String cw_type) {
		double money = 0;
		// wdateΪ��Ϊȱʡ����
		// ����cw_type �� �ڼ��� �� ������׼ ���㲹��
		// ����ȱʡ����
		/*
		 * ���ڼ���ȱʡ���ڵĲ��� cw_type: ǰһ��¼�����ͣ���˾'gs'������'cc'���ݼ�'xj'����н�ݼ�'xx'
		 */
		// ��˾'gs'���ݼ�'xj'û�в��������ü���
		if ("cc".equals(cw_type) || "xx".equals(cw_type)) {
			for (int i = 0; i < nowDays; i++) {
				long time = wdate.getTime() - (nowDays - i) * 24 * 60 * 60
						* 1000L;
				Date qsDate = new Date(time);
				String stqsDate = format.format(qsDate);
				Calendar calQs = Calendar.getInstance();
				calQs.setTime(qsDate);

				// Ϊture������Ϊ�����ڼ��գ������׼������ *1.5
				if (WLogBuzhuTongjiUtil.ifHolidays(format.format(wdate), false)) {
					money = money + subsidy * 1.5;
				} else {
					money = money + subsidy;
				}
			}
		}
		return money;
	}

	// ������һ��Ĳ����Ƿ���ӳ�
	public static double getHolidaysMoney(Date wdate, double money) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		double hmoney = 0.00;
		Calendar calQs = Calendar.getInstance();
		calQs.setTime(wdate);
		if (money == 0) {
			return hmoney;
		}

		// Ϊture������Ϊ�����ڼ��գ������׼������ *1.5
		if (WLogBuzhuTongjiUtil.ifHolidays(format.format(wdate), false)) {
			money = money * 1.5;
		}
		return hmoney;
	}

	// ����ü�¼������ݼٵ�����
	public static void insertOrUpdateCW_XC(String wlog_uid, String xc_type,
			String xc_days, String xc_money,String xc_endtime) {
		try {
				String id = (String) UUIDHex.getInstance().generate();
				DOService cw_xcSer = DOService
						.getService("cw_xiujiaorchuchai_insert_cw_worklog");
				cw_xcSer.invokeUpdate(id, wlog_uid, xc_type, xc_days, xc_money,xc_endtime);

		} catch (ExedoException e) {
			// TODO Auto-generated catch block
			System.out.println("����cw_xiujiaorchuchai_insert���¼ʧ�ܣ�����������������");
			e.printStackTrace();
		}

	}
	
	// ɾ��������ݼٵ�����
	public static void deleteCW_XC(String wlog_uid) {
		try {
				DOService cw_xcSer = DOService
						.getService("cw_xiujiaorchuchai_delete_cw_worklog");
				cw_xcSer.invokeUpdate(wlog_uid);

		} catch (ExedoException e) {
			// TODO Auto-generated catch block
			System.out.println(" ɾ��������ݼٵ����ݱ��¼ʧ�ܣ�����������������");
			e.printStackTrace();
		}

	}

	// ��ѯ�ü�¼������ݼٵ�����
	public static List<BOInstance> findCW_XC(String wlog_uid) {
		// cw_xiujiaorchuchai_insert
		DOService cw_xcSer = DOService
				.getService("cw_xiujiaorchuchai_browse_by_wlog");
		List<BOInstance> list = new ArrayList<BOInstance>();
		list = cw_xcSer.invokeSelect(wlog_uid);
		return list;
	}

	public static void main(String[] args) {
		List<BOInstance> list = WLogBuzhuTongjiUtil.findCW_XC(
				"2c90b0e72ea360b1012ea3678c870002");
		System.out.println(list.size());
	}

	public static List<BOInstance> findCW_XC(String wlog_uid, String string) {
		// cw_xiujiaorchuchai_insert
		DOService cw_xcSer = DOService
				.getService("cw_xiujiaorchuchai_browse_by_wlog_type");
		List<BOInstance> list = new ArrayList<BOInstance>();
		list = cw_xcSer.invokeSelect(wlog_uid);
		return list;
	}

}
