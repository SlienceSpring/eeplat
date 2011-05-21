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

import com.exedosoft.plat.ExedoException;
import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.BOInstance;
import com.exedosoft.plat.bo.DOBO;
import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.login.zidingyi.excel.MySqlOperation;
import com.exedosoft.plat.login.zidingyi.excel.OperationUtil;
import com.exedosoft.plat.search.customize.SearchTransCode;
import com.exedosoft.plat.util.DOGlobals;

public class WLogCountBuZhu extends DOAbstractAction {
	public String excute() {

		/***
		 * ��н�ݼٳ����ݼٹ�û������� ��н�ݼٳ����ݼٹ�û������� ��н�ݼٳ����ݼٹ�û�������
		 * */
		// ��һ����¼������
		String upperwaddress = null;
		String upperwseladdress = null;

		// �Ƿ���Ϊ�ݼ����������
		String xj_type = null;
		String cw_type = null;
		String cw_xjsp = null;
		String is_xinjia = null;

		// ���㵱��Ĳ���
		double allbzbasic = 0.00;
		String curr_uid = null;
		boolean isXinjia = true;
		boolean ifhavexj = false;
		boolean isXJSp = false;
		/**
		 * ifhavexc:�Ƿ���ڳ�����ݼٵ����� cg�������, cq����ȥ, cgcq�������ٳ���ȥ xg�ݼٹ�, xq�ݼ�ȥ,
		 * xgxq�ݼٹ�������ȥ�ݼ� xgcg�ݼٹ��������� xgcq�ݼٹ��������ȥ cgxq����������ݼ�ȥ cqxq�����ȥ�ݼ�
		 * */
		String ifhavexc = null;
		Date dateWdate = null;

		String wlog_uid = null;
		String xc_days = null;
		String xc_money = null;

		String waddress = DOGlobals.getInstance().getSessoinContext()
				.getFormInstance().getValue("waddress");
		String wseladdress = DOGlobals.getInstance().getSessoinContext()
				.getFormInstance().getValue("wseladdress");
		String wdate = DOGlobals.getInstance().getSessoinContext()
				.getFormInstance().getValue("wdate");
		String user_uid = DOGlobals.getInstance().getSessoinContext()
				.getInstance().getUser().getUid();
		
		if(wdate == null || "".equals(wdate)) {
			isXJSp = true;
		}
		List users = new ArrayList();
		try {
			DOBO cwtheBO = DOBO.getDOBOByName("cw_worklog");
			BOInstance bi = cwtheBO.getCorrInstance();
			if (bi != null) {
				curr_uid = bi.getUid();
				wlog_uid = curr_uid;
				if (waddress == null || "".equals(waddress.trim())) {
					waddress = bi.getValue("waddress");
				}
				xj_type = bi.getValue("xj_type");

				if (wdate == null || "".equals(wdate.trim())) {
					wdate = bi.getValue("wdate");
				}
				if (cw_type == null || "".equals(cw_type.trim())) {
					cw_type = bi.getValue("cw_type");
				}
				if (is_xinjia == null || "".equals(is_xinjia.trim())) {
					is_xinjia = bi.getValue("is_xinjia");
				}

				String is_xinjia1 = bi.getValue("is_xinjia");
				if (is_xinjia1 != null && "xj".equals(is_xinjia1)) {
					isXinjia = false;
				} else {
					isXinjia = true;
				}

				DOService bzservice = DOService
						.getService("cw_modifycity_browse_by_wlog");
				List bzList = new ArrayList();
				bzList = bzservice.invokeSelect(user_uid, wdate);
				if (bzList != null && bzList.size() > 0) {
					BOInstance bzBi = (BOInstance) bzList.get(0);
					upperwaddress = bzBi.getValue("waddress");
					upperwseladdress = bzBi.getValue("wseladdress");
				}
				DOService service = DOService
						.getService("cw_modifycity_browse_by_wlog");
				users = service.invokeSelect(curr_uid);
			}

			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			dateWdate = format.parse(wdate);
		} catch (Exception e) {
			this.setEchoValue("����ʧ�ܣ�error" + e.toString());
			return "notpass";
		}

		/**
		 * ȡ��һ����¼��Ȼ�󰴶����������
		 * */
		if (users != null && users.size() > 0) {

			// ��־���ж���һ�����ͣ�
			boolean isChucha = false;
			boolean isZhongzhuan = false;
			boolean isFanhui = false;
			boolean isXiujiaChu = false;
			boolean isXiujiaGui = false;

			BOInstance bi = (BOInstance) users.get(0);
			String modifytype = bi.getValue("modifytype");
			String lasttime = bi.getValue("modifytime");
			String lasttime2 = bi.getValue("modifytime2");

			String stbz = bi.getValue("bzbasice");
			double bzbasic = 0.00;
			if (stbz != null) {
				bzbasic = Double.parseDouble(stbz);
			}

			String stbzend = bi.getValue("bzbasiceend");
			double bzbasicend = 0.00;
			if (stbzend != null) {
				bzbasicend = Double.parseDouble(stbzend);
			}

			// ͳһ ������׼��ÿ��������
			double basic = bzbasicend;
			// ��һ����ʼʱ�����ֹʱ��
			double lastBeginTime = getBeginHours(lasttime);
			double lastEndTime = getEndHours(lasttime2);

			WLogBuzhuTongjiUtil.deleteCW_XC(wlog_uid);

			// ֻ��һ����¼ʱ
			if (users.size() == 1) {
				// ���
				if (modifytype != null && "1".equals(modifytype)) {
					basic = bzbasicend;
					allbzbasic = (lastEndTime - lastBeginTime) * 1.00 * basic
							/ 24;

					allbzbasic = allbzbasic + (24 - lastEndTime) * 1.00 * basic
							/ 24;
					//
					// ��Сʱ����
					double hours = (24 - lastBeginTime);
					xc_days = OperationUtil.round(hours/24, 2);
					String cx_endtime = OperationUtil.round(24, 2);
					ifhavexc = "cq";
					xc_money = OperationUtil.round(allbzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "cq",
							xc_days, xc_money, cx_endtime);
				}
				// ��ת
				else if (modifytype != null && "2".equals(modifytype)) {
					// ��Сʱ������תǰ
					basic = bzbasic;
					allbzbasic = lastBeginTime * 1.00 / 24 * basic;

					// ��Сʱ���� ��ת��
					basic = bzbasicend;
					allbzbasic = allbzbasic + (lastEndTime - lastBeginTime)
							* 1.00 / 24 * basic;

					// ��Сʱ���� ��ת��
					basic = bzbasicend;
					allbzbasic = allbzbasic + (24 - lastEndTime) * 1.00 / 24
							* basic;

					double hours = 24;
					xc_days = OperationUtil.round(1, 2);
					String cx_endtime = OperationUtil.round(24, 2);
					ifhavexc = "cq";
					xc_money = OperationUtil.round(allbzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "cq",
							xc_days, xc_money, cx_endtime);

				}
				// ����
				else if (modifytype != null && "3".equals(modifytype)) {
					basic = bzbasic;
					allbzbasic = lastEndTime * 1.00 / 24 * basic;

					// ��Сʱ����
					double hours = lastEndTime;
					xc_days = OperationUtil.round(hours / 24, 2);
					String cx_endtime = OperationUtil.round(lastEndTime, 2);
					ifhavexc = "cg";
					xc_money = OperationUtil.round(allbzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "cg",
							xc_days, xc_money, cx_endtime);
				}
				// �ݼٳ�
				else if (modifytype != null && "4".equals(modifytype)) {
					basic = bzbasic;
					String cx_endtime = "24.00";
					double xc_bzbasic = 0.00D;
					xc_bzbasic = lastBeginTime * 1.00 / 24 * basic;
					xc_days = OperationUtil.round(lastBeginTime / 24, 2);
					cx_endtime = OperationUtil.round(lastBeginTime, 2);
					ifhavexc = "cq";
					xc_money = OperationUtil.round(xc_bzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "cq",
							xc_days, xc_money, cx_endtime);

					allbzbasic = xc_bzbasic;
					if (isXinjia) {
						xc_bzbasic = (24 - lastBeginTime) * 1.00 / 24 * basic;
						is_xinjia = "xx";
					} else {
						xc_bzbasic = 0;
						is_xinjia = "xj";
					}

					xc_days = OperationUtil.round((24 - lastBeginTime), 2);
					cx_endtime = OperationUtil.round(24, 2);

					ifhavexc = "xq";
					xc_money = OperationUtil.round(xc_bzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "xq",
							xc_days, xc_money, cx_endtime);
					allbzbasic = allbzbasic + xc_bzbasic;
					ifhavexj = true;
				}
				// �ݼٹ�
				else if (modifytype != null && "5".equals(modifytype)) {
					basic = bzbasicend;
					String cx_endtime = "24.00";
					double xc_bzbasic = 0.00D;
					if (isXinjia) {
						xc_bzbasic = lastEndTime * 1.00 / 24 * basic;
					} else {
						xc_bzbasic = 0;
					}
					xc_days = OperationUtil.round(lastEndTime, 2);
					cx_endtime = OperationUtil.round(lastEndTime, 2);
					ifhavexc = "xg";
					xc_money = OperationUtil.round(xc_bzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "xg",
							xc_days, xc_money, cx_endtime);
					allbzbasic = xc_bzbasic;

					xc_bzbasic = (24 - lastEndTime) * 1.00 / 24 * basic;
					xc_days = OperationUtil.round((24 - lastEndTime), 2);
					cx_endtime = OperationUtil.round(24, 2);
					ifhavexc = "xg";
					xc_money = OperationUtil.round(xc_bzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "xg",
							xc_days, xc_money, cx_endtime);
					allbzbasic = allbzbasic + xc_bzbasic;
				}

				// ������¼ʱ
			} else {

				// ��ʼ����һ����¼����

				// ���
				if (modifytype != null && "1".equals(modifytype)) {
					basic = bzbasicend;
					allbzbasic = (lastEndTime - lastBeginTime) * 1.00 * basic
							/ 24;
					//
					// ��Сʱ����
					double hours = (lastEndTime - lastBeginTime);
					xc_days = OperationUtil.round(hours / 24, 2);
					String cx_endtime = OperationUtil.round(
							(lastEndTime - lastBeginTime), 2);
					ifhavexc = "cq";
					xc_money = OperationUtil.round(allbzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "cq",
							xc_days, xc_money, cx_endtime);
				}
				// ��ת
				else if (modifytype != null && "2".equals(modifytype)) {
					// ��Сʱ������תǰ
					basic = bzbasic;
					allbzbasic = lastBeginTime * 1.00 / 24 * basic;

					// ��Сʱ���� ��ת��
					basic = bzbasicend;
					allbzbasic = allbzbasic + (lastEndTime - lastBeginTime)
							* 1.00 / 24 * basic;

					// ��Сʱ���� ��ת��
					// ���ü���

					double hours = lastEndTime;
					xc_days = OperationUtil.round(hours / 24, 2);
					String cx_endtime = OperationUtil.round(lastEndTime, 2);
					ifhavexc = "cq";
					xc_money = OperationUtil.round(allbzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "cq",
							xc_days, xc_money, cx_endtime);

				}
				// ����
				else if (modifytype != null && "3".equals(modifytype)) {
					basic = bzbasic;
					allbzbasic = lastEndTime * 1.00 / 24 * basic;

					// ��Сʱ����
					double hours = lastEndTime;
					xc_days = OperationUtil.round(hours / 24, 2);
					String cx_endtime = OperationUtil.round(lastEndTime, 2);
					ifhavexc = "cg";
					xc_money = OperationUtil.round(allbzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "cg",
							xc_days, xc_money, cx_endtime);
				}
				// �ݼٳ�
				else if (modifytype != null && "4".equals(modifytype)) {
					basic = bzbasic;
					String cx_endtime = "24.00";
					double xc_bzbasic = 0.00D;
					xc_bzbasic = lastBeginTime * 1.00 / 24 * basic;
					xc_days = OperationUtil.round(lastBeginTime / 24, 2);
					cx_endtime = OperationUtil.round(lastBeginTime, 2);
					ifhavexc = "cq";
					xc_money = OperationUtil.round(xc_bzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "cq",
							xc_days, xc_money, cx_endtime);

					allbzbasic = xc_bzbasic;
					if (isXinjia) {
						xc_bzbasic = (lastEndTime - lastBeginTime) * 1.00 / 24
								* basic;
						is_xinjia = "xx";
					} else {
						xc_bzbasic = 0;
						is_xinjia = "xj";
					}

					xc_days = OperationUtil.round(
							(lastEndTime - lastBeginTime), 2);
					cx_endtime = OperationUtil.round(lastEndTime, 2);

					ifhavexc = "xq";
					xc_money = OperationUtil.round(xc_bzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "xq",
							xc_days, xc_money, cx_endtime);
					allbzbasic = allbzbasic + xc_bzbasic;
					is_xinjia = "xx";ifhavexj = true;
				}
				// �ݼٹ�
				else if (modifytype != null && "5".equals(modifytype)) {
					basic = bzbasicend;
					String cx_endtime = "24.00";
					double xc_bzbasic = 0.00D;
					if (isXinjia) {
						xc_bzbasic = lastEndTime * 1.00 / 24 * basic;
					} else {
						xc_bzbasic = 0;
					}
					xc_days = OperationUtil.round(lastEndTime, 2);
					cx_endtime = OperationUtil.round(lastEndTime, 2);
					ifhavexc = "xg";
					xc_money = OperationUtil.round(xc_bzbasic, 2);
					WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "xg",
							xc_days, xc_money, cx_endtime);
					allbzbasic = xc_bzbasic;
				}

				boolean isFinally = false;
				for (int i = 1; i < users.size(); i++) {
					if (i == users.size() - 1) {
						isFinally = true;
					}

					BOInstance nextbi = (BOInstance) users.get(i);
					String nexttype = nextbi.getValue("modifytype");
					String nexttime = nextbi.getValue("modifytime");
					String nexttime2 = nextbi.getValue("modifytime2");

					String nextstbz = nextbi.getValue("bzbasice");
					double nextbasic = 0.00;
					if (nextstbz != null) {
						nextbasic = Double.parseDouble(nextstbz);
					}

					String nextbzend = nextbi.getValue("bzbasiceend");
					double nextbasicend = 0.00;
					if (nextbzend != null) {
						nextbasicend = Double.parseDouble(nextbzend);
					}

					// ��һ����ʼʱ�����ֹʱ��
					double nextBeginTime = getBeginHours(nexttime);
					double nextEndTime = getEndHours(nexttime2);

					// ������¼Ϊ�������ת:���μ�¼����Ϊ��ת(modifytype==2)������(modifytype==3)���ݼٳ�(modifytype==4)
					if (isChucha || isZhongzhuan) {
						// ��ת�������ʼ���صĲ�������>0�����������趨����;����ֻ����lastEndTime=nextEndTime;
						if (nexttype != null && "2".equals(nexttype.trim())) {
							if (isFinally) {
								// ��Сʱ������תǰ
								basic = bzbasic;
								double tempbzbasic = (nextBeginTime - lastEndTime)
										* 1.00 / 24 * basic;

								// ��Сʱ���� ��ת��
								basic = bzbasicend;
								tempbzbasic = tempbzbasic
										+ (nextEndTime - nextBeginTime) * 1.00
										/ 24 * basic;

								// ��Сʱ���� ��ת��
								basic = bzbasicend;
								tempbzbasic = tempbzbasic + (24 - nextEndTime)
										* 1.00 / 24 * basic;

								double hours = (24 - lastEndTime);
								xc_days = OperationUtil.round(hours / 24, 2);
								String cx_endtime = OperationUtil.round(24, 2);
								ifhavexc = "cq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cq", xc_days, xc_money,
										cx_endtime);

								allbzbasic = allbzbasic + tempbzbasic;
							} else {
								// ��Сʱ������תǰ
								basic = bzbasic;
								double tempbzbasic = (nextBeginTime - lastEndTime)
										* 1.00 / 24 * basic;

								// ��Сʱ���� ��ת��
								basic = bzbasicend;
								tempbzbasic = tempbzbasic
										+ (nextEndTime - nextBeginTime) * 1.00
										/ 24 * basic;

								double hours = (nextEndTime - lastEndTime);
								xc_days = OperationUtil.round(hours / 24, 2);
								String cx_endtime = OperationUtil.round(
										nextEndTime, 2);
								ifhavexc = "cq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cq", xc_days, xc_money,
										cx_endtime);

								allbzbasic = allbzbasic + tempbzbasic;
							}
						}
						// ���أ����㲹�����ɣ������趨����
						else if (nexttype != null && "3".equals(nexttype)) {
							if (isFinally) {
								basic = bzbasic;
								double tempbzbasic = (nextEndTime - lastEndTime)
										* 1.00 / 24 * basic;

								// ��Сʱ����
								double hours = (nextEndTime - lastEndTime);
								xc_days = OperationUtil.round(hours / 24, 2);
								String cx_endtime = OperationUtil.round(
										(nextEndTime - lastEndTime), 2);
								ifhavexc = "cg";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cg", xc_days, xc_money,
										cx_endtime);
								allbzbasic = allbzbasic + tempbzbasic;
							} else {
								basic = bzbasic;
								double tempbzbasic = (nextEndTime - lastEndTime)
										* 1.00 / 24 * basic;

								// ��Сʱ����
								double hours = (nextEndTime - lastEndTime);
								xc_days = OperationUtil.round(hours / 24, 2);
								String cx_endtime = OperationUtil.round(
										(nextEndTime - lastEndTime), 2);
								ifhavexc = "cg";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cg", xc_days, xc_money,
										cx_endtime);
								allbzbasic = allbzbasic + tempbzbasic;
							}
						}
						// �ݼٳ������㲹�����ɣ������趨����
						else if (nexttype != null && "4".equals(nexttype)) {
							if (isFinally) {
								basic = bzbasic;
								String cx_endtime = "24.00";
								double tempbzbasic = 0.00D;

								tempbzbasic = (nextBeginTime - lastEndTime)
										* 1.00 / 24 * basic;
								xc_days = OperationUtil.round(
										(nextBeginTime - lastEndTime) / 24, 2);
								cx_endtime = OperationUtil.round(nextBeginTime,
										2);
								ifhavexc = "cq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cq", xc_days, xc_money,
										cx_endtime);

								allbzbasic = tempbzbasic;

								if (isXinjia) {
									tempbzbasic = (24 - nextBeginTime) * basic
											/ 24;
									xc_days = OperationUtil.round(
											(24 - nextBeginTime) / 24, 2);
									cx_endtime = OperationUtil.round(24, 2);
									is_xinjia = "xx";
								} else {
									tempbzbasic = 0;
									xc_days = OperationUtil.round(
											(24 - nextBeginTime) / 24, 2);
									cx_endtime = OperationUtil.round(24, 2);
									is_xinjia = "xj";ifhavexj = true;
								}

								ifhavexc = "xq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "xq", xc_days, xc_money,
										cx_endtime);
								allbzbasic = allbzbasic + tempbzbasic;
								ifhavexj = true;
							} else {
								basic = bzbasic;
								String cx_endtime = "24.00";
								double tempbzbasic = 0.00D;

								tempbzbasic = (nextBeginTime - lastEndTime)
										* 1.00 / 24 * basic;
								xc_days = OperationUtil.round(
										(nextBeginTime - lastEndTime) / 24, 2);
								cx_endtime = OperationUtil.round(nextBeginTime,
										2);
								ifhavexc = "cq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cq", xc_days, xc_money,
										cx_endtime);

								if (isXinjia) {
									tempbzbasic = (nextEndTime - lastEndTime)
											* basic / 24;
									xc_days = OperationUtil
											.round((nextEndTime - lastEndTime) / 24,
													2);
									cx_endtime = OperationUtil.round(
											nextEndTime, 2);
									is_xinjia = "xx";
								} else {
									tempbzbasic = 0;
									xc_days = OperationUtil
											.round((nextEndTime - lastEndTime) / 24,
													2);
									cx_endtime = OperationUtil.round(
											nextEndTime, 2);
									is_xinjia = "xj";
								}

								ifhavexc = "xq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "xq", xc_days, xc_money,
										cx_endtime);
								allbzbasic = allbzbasic + tempbzbasic;
								ifhavexj = true;
							}

						} else {
							System.out.println("������д����1001");
							System.out.println("������д����1001");
						}
					}

					// ������¼Ϊ����:��������Ϊ����(modifytype==1)���ݼٳ�(modifytype==4)
					else if (isFanhui) {
						// ���ֻ���趨����
						if (nexttype != null && "1".equals(nexttype)) {
							if (isFinally) {
								double tempbzbasic = 0.00D;
								basic = bzbasicend;
								tempbzbasic = (nextEndTime - nextBeginTime)
										* 1.00 * basic / 24;

								tempbzbasic = tempbzbasic + (24 - nextEndTime)
										* 1.00 * basic / 24;
								//
								// ��Сʱ����
								double hours = 24 - nextBeginTime;
								xc_days = OperationUtil.round(hours / 24, 2);
								String cx_endtime = OperationUtil.round(hours,
										2);
								ifhavexc = "cq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cq", xc_days, xc_money,
										cx_endtime);
								allbzbasic = allbzbasic + tempbzbasic;
							} else {
								double tempbzbasic = 0.00D;
								basic = bzbasicend;
								tempbzbasic = (nextEndTime - nextBeginTime)
										* 1.00 * basic / 24;
								//
								// ��Сʱ����
								double hours = (nextEndTime - nextBeginTime);
								xc_days = OperationUtil.round(hours / 24, 2);
								String cx_endtime = OperationUtil.round(hours,
										2);
								ifhavexc = "cq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cq", xc_days, xc_money,
										cx_endtime);
								allbzbasic = allbzbasic + tempbzbasic;
							}

						}
						// �ݼٳ�����Ϊ��һ��Ϊ���أ�������ݼٳ��������
						else if (modifytype != null && "4".equals(modifytype)) {
							if (isFinally) {
								basic = bzbasic;
								String cx_endtime = "24.00";
								double tempbzbasic = 0.00D;

								tempbzbasic = (nextBeginTime - lastEndTime)
										* 1.00 / 24 * basic;
								xc_days = OperationUtil.round(
										(nextBeginTime - lastEndTime) / 24, 2);
								cx_endtime = OperationUtil.round(nextBeginTime,
										2);
								ifhavexc = "cq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cq", xc_days, xc_money,
										cx_endtime);

								allbzbasic = tempbzbasic;

								if (isXinjia) {
									tempbzbasic = (24 - nextBeginTime) * basic
											/ 24;
									xc_days = OperationUtil.round(
											(24 - nextBeginTime) / 24, 2);
									cx_endtime = OperationUtil.round(24, 2);
									is_xinjia = "xx";
								} else {
									tempbzbasic = 0;
									xc_days = OperationUtil.round(
											(24 - nextBeginTime) / 24, 2);
									cx_endtime = OperationUtil.round(24, 2);
									is_xinjia = "xj";
								}

								ifhavexc = "xq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "xq", xc_days, xc_money,
										cx_endtime);
								allbzbasic = allbzbasic + tempbzbasic;
								ifhavexj = true;
							} else {
								basic = bzbasic;
								String cx_endtime = "24.00";
								double tempbzbasic = 0.00D;

								tempbzbasic = (nextBeginTime - lastEndTime)
										* 1.00 / 24 * basic;
								xc_days = OperationUtil.round(
										(nextBeginTime - lastEndTime) / 24, 2);
								cx_endtime = OperationUtil.round(nextBeginTime,
										2);
								ifhavexc = "cq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "cq", xc_days, xc_money,
										cx_endtime);

								if (isXinjia) {
									tempbzbasic = (nextEndTime - lastEndTime)
											* basic / 24;
									xc_days = OperationUtil
											.round((nextEndTime - lastEndTime) / 24,
													2);
									cx_endtime = OperationUtil.round(
											nextEndTime, 2);
									is_xinjia = "xx";
								} else {
									tempbzbasic = 0;
									xc_days = OperationUtil
											.round((nextEndTime - lastEndTime) / 24,
													2);
									cx_endtime = OperationUtil.round(
											nextEndTime, 2);
									is_xinjia = "xj";
								}

								ifhavexc = "xq";
								xc_money = OperationUtil.round(tempbzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "xq", xc_days, xc_money,
										cx_endtime);
								allbzbasic = allbzbasic + tempbzbasic;
								ifhavexj = true;
							}
						}

						// ������¼Ϊ�ݼٳ�:���μ�¼����Ϊ����(modifytype==1)����ת(modifytype==2)������(modifytype==3)���ݼٹ�(modifytype==5)

						else if (isXiujiaChu) {
							// ���ֻ���趨���ݣ�ʱ���ѿ�ʼʱ��Ϊ����㣬��Ϊ���һ����¼���������
							if (nexttype != null && "1".equals(nexttype)) {
								if (isXinjia) {
									if (isFinally) {
										double tempbzbasic = 0.00D;
										basic = bzbasicend;
										tempbzbasic = (nextEndTime - lastEndTime)
												* 1.00 * basic / 24;

										tempbzbasic = tempbzbasic
												+ (24 - nextEndTime) * 1.00
												* basic / 24;
										//
										// ��Сʱ����
										double hours = 24 - lastEndTime;
										xc_days = OperationUtil.round(
												hours / 24, 2);
										String cx_endtime = OperationUtil
												.round(hours, 2);
										ifhavexc = "cq";
										xc_money = OperationUtil.round(
												tempbzbasic, 2);
										WLogBuzhuTongjiUtil
												.insertOrUpdateCW_XC(wlog_uid,
														"cq", xc_days,
														xc_money, cx_endtime);
										allbzbasic = allbzbasic + tempbzbasic;
									} else {
										double tempbzbasic = 0.00D;
										basic = bzbasicend;
										tempbzbasic = (nextEndTime - lastEndTime)
												* 1.00 * basic / 24;
										//
										// ��Сʱ����
										double hours = (nextEndTime - lastEndTime);
										xc_days = OperationUtil.round(
												hours / 24, 2);
										String cx_endtime = OperationUtil
												.round(hours, 2);
										ifhavexc = "cq";
										xc_money = OperationUtil.round(
												tempbzbasic, 2);
										WLogBuzhuTongjiUtil
												.insertOrUpdateCW_XC(wlog_uid,
														"cq", xc_days,
														xc_money, cx_endtime);
										allbzbasic = allbzbasic + tempbzbasic;
									}
								} else {
									if (isFinally) {
										double tempbzbasic = 0.00D;
										basic = bzbasicend;
										tempbzbasic = (nextEndTime - nextBeginTime)
												* 1.00 * basic / 24;

										tempbzbasic = tempbzbasic
												+ (24 - nextEndTime) * 1.00
												* basic / 24;
										//
										// ��Сʱ����
										double hours = 24 - nextBeginTime;
										xc_days = OperationUtil.round(
												hours / 24, 2);
										String cx_endtime = OperationUtil
												.round(hours, 2);
										ifhavexc = "cq";
										xc_money = OperationUtil.round(
												tempbzbasic, 2);
										WLogBuzhuTongjiUtil
												.insertOrUpdateCW_XC(wlog_uid,
														"cq", xc_days,
														xc_money, cx_endtime);
										allbzbasic = allbzbasic + tempbzbasic;
									} else {
										double tempbzbasic = 0.00D;
										basic = bzbasicend;
										tempbzbasic = (nextEndTime - nextBeginTime)
												* 1.00 * basic / 24;
										//
										// ��Сʱ����
										double hours = (nextEndTime - nextBeginTime);
										xc_days = OperationUtil.round(
												hours / 24, 2);
										String cx_endtime = OperationUtil
												.round(hours, 2);
										ifhavexc = "cq";
										xc_money = OperationUtil.round(
												tempbzbasic, 2);
										WLogBuzhuTongjiUtil
												.insertOrUpdateCW_XC(wlog_uid,
														"cq", xc_days,
														xc_money, cx_endtime);
										allbzbasic = allbzbasic + tempbzbasic;
									}
								}

							}
							// ��ת��ֻ���趨���ݣ�ʱ���ѿ�ʼʱ��Ϊ����㣬��Ϊ���һ����¼���������(�����ʽ����)
							else if (nexttype != null && "2".equals(nexttype)) {
								if (isXinjia) {
									if (isFinally) {
										double tempbzbasic = 0.00D;
										basic = bzbasicend;
										tempbzbasic = (nextEndTime - lastEndTime)
												* 1.00 * basic / 24;

										tempbzbasic = tempbzbasic
												+ (24 - nextEndTime) * 1.00
												* basic / 24;
										//
										// ��Сʱ����
										double hours = 24 - lastEndTime;
										xc_days = OperationUtil.round(
												hours / 24, 2);
										String cx_endtime = OperationUtil
												.round(hours, 2);
										ifhavexc = "cq";
										xc_money = OperationUtil.round(
												tempbzbasic, 2);
										WLogBuzhuTongjiUtil
												.insertOrUpdateCW_XC(wlog_uid,
														"cq", xc_days,
														xc_money, cx_endtime);
										allbzbasic = allbzbasic + tempbzbasic;
									} else {
										double tempbzbasic = 0.00D;
										basic = bzbasicend;
										tempbzbasic = (nextEndTime - lastEndTime)
												* 1.00 * basic / 24;
										//
										// ��Сʱ����
										double hours = (nextEndTime - lastEndTime);
										xc_days = OperationUtil.round(
												hours / 24, 2);
										String cx_endtime = OperationUtil
												.round(hours, 2);
										ifhavexc = "cq";
										xc_money = OperationUtil.round(
												tempbzbasic, 2);
										WLogBuzhuTongjiUtil
												.insertOrUpdateCW_XC(wlog_uid,
														"cq", xc_days,
														xc_money, cx_endtime);
										allbzbasic = allbzbasic + tempbzbasic;
									}
								} else {
									if (isFinally) {
										double tempbzbasic = 0.00D;
										basic = bzbasicend;
										tempbzbasic = (nextEndTime - nextBeginTime)
												* 1.00 * basic / 24;

										tempbzbasic = tempbzbasic
												+ (24 - nextEndTime) * 1.00
												* basic / 24;
										//
										// ��Сʱ����
										double hours = 24 - nextBeginTime;
										xc_days = OperationUtil.round(
												hours / 24, 2);
										String cx_endtime = OperationUtil
												.round(hours, 2);
										ifhavexc = "cq";
										xc_money = OperationUtil.round(
												tempbzbasic, 2);
										WLogBuzhuTongjiUtil
												.insertOrUpdateCW_XC(wlog_uid,
														"cq", xc_days,
														xc_money, cx_endtime);
										allbzbasic = allbzbasic + tempbzbasic;
									} else {
										double tempbzbasic = 0.00D;
										basic = bzbasicend;
										tempbzbasic = (nextEndTime - nextBeginTime)
												* 1.00 * basic / 24;
										//
										// ��Сʱ����
										double hours = (nextEndTime - nextBeginTime);
										xc_days = OperationUtil.round(
												hours / 24, 2);
										String cx_endtime = OperationUtil
												.round(hours, 2);
										ifhavexc = "cq";
										xc_money = OperationUtil.round(
												tempbzbasic, 2);
										WLogBuzhuTongjiUtil
												.insertOrUpdateCW_XC(wlog_uid,
														"cq", xc_days,
														xc_money, cx_endtime);
										allbzbasic = allbzbasic + tempbzbasic;
									}
								}
							}

							// ���أ�ֻ���趨���ݣ�ʱ���ѿ�ʼʱ��Ϊ����㣬���㲹��
							else if (nexttype != null && "3".equals(nexttype)) {
								double tempbzbasic = 0.00D;
								basic = bzbasicend;
								if (isXinjia) {
									
									tempbzbasic = (nextEndTime - lastBeginTime)
											* 1.00 * basic / 24;
									
								} else {
									tempbzbasic = 0;
								}
								
								//
								// ��Сʱ����
								double hours = (nextEndTime - nextBeginTime);
								xc_days = OperationUtil
										.round(hours / 24, 2);
								String cx_endtime = OperationUtil.round(
										nextEndTime, 2);
								ifhavexc = "xg";
								xc_money = OperationUtil.round(tempbzbasic,
										2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
										wlog_uid, "xg", xc_days, xc_money,
										cx_endtime);
								allbzbasic = allbzbasic + tempbzbasic;

							}
							/**
							 * �ݼٹ飺�����������ݼٹ鵽ԭ���ػ�����һ���ط�������nextEndTimeΪ�ݼٽ���ʱ��
							 * ֻ���趨����:lastBeginTime��lastEndTime��ΪnextEndTime
							 * ��Ϊ���һ����¼���������
							 * */
							else if (modifytype != null
									&& "5".equals(modifytype)) {
								basic = bzbasicend;
								String cx_endtime = "24.00";
								double xc_bzbasic = 0.00D;
								if (isXinjia) {
									xc_bzbasic = (nextEndTime-lastEndTime) * 1.00 / 24 * basic;
								} else {
									xc_bzbasic = 0;
								}
								xc_days = OperationUtil.round((nextEndTime-lastEndTime), 2);
								cx_endtime = OperationUtil.round(nextEndTime, 2);
								ifhavexc = "xg";
								xc_money = OperationUtil.round(xc_bzbasic, 2);
								WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "xg",
										xc_days, xc_money, cx_endtime);
								allbzbasic = allbzbasic + xc_bzbasic;
								if(isFinally) {
									xc_bzbasic = (24 - nextEndTime) * 1.00 / 24 * basic;
									xc_days = OperationUtil.round((24 - nextEndTime), 2);
									cx_endtime = OperationUtil.round(24, 2);
									ifhavexc = "xg";
									xc_money = OperationUtil.round(xc_bzbasic, 2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(wlog_uid, "xg",
											xc_days, xc_money, cx_endtime);
									allbzbasic = allbzbasic + xc_bzbasic;
								}
								

							} else {
								System.out.println("������д����1003");
								System.out.println("������д����1003");
							}
						}

						// ������¼Ϊ�ݼٹ�:���μ�¼����Ϊ�����ת�����ء��ݼٳ�
						else if (isXiujiaGui) {
							// ���ֻ���趨����
							if (nexttype != null && "1".equals(nexttype)) {
								if (isFinally) {
									double tempbzbasic = 0.00D;
									basic = bzbasicend;
									tempbzbasic = (nextEndTime - nextBeginTime)
											* 1.00 * basic / 24;

									tempbzbasic = tempbzbasic
											+ (24 - nextEndTime) * 1.00 * basic
											/ 24;
									//
									// ��Сʱ����
									double hours = 24 - nextBeginTime;
									xc_days = OperationUtil
											.round(hours / 24, 2);
									String cx_endtime = OperationUtil.round(
											hours, 2);
									ifhavexc = "cq";
									xc_money = OperationUtil.round(tempbzbasic,
											2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "cq", xc_days, xc_money,
											cx_endtime);
									allbzbasic = allbzbasic + tempbzbasic;
								} else {
									double tempbzbasic = 0.00D;
									basic = bzbasicend;
									tempbzbasic = (nextEndTime - nextBeginTime)
											* 1.00 * basic / 24;
									//
									// ��Сʱ����
									double hours = (nextEndTime - nextBeginTime);
									xc_days = OperationUtil
											.round(hours / 24, 2);
									String cx_endtime = OperationUtil.round(
											hours, 2);
									ifhavexc = "cq";
									xc_money = OperationUtil.round(tempbzbasic,
											2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "cq", xc_days, xc_money,
											cx_endtime);
									allbzbasic = allbzbasic + tempbzbasic;
								}
							}

							// ��ת�������ʼ���صĲ�������>0�����������趨����;����ֻ����lastEndTime=nextEndTime;
							else if (nexttype != null
									&& "2".equals(nexttype.trim())) {
								if (isFinally) {
									// ��Сʱ������תǰ
									basic = bzbasic;
									double tempbzbasic = (nextBeginTime - lastEndTime)
											* 1.00 / 24 * basic;

									// ��Сʱ���� ��ת��
									basic = bzbasicend;
									tempbzbasic = tempbzbasic
											+ (nextEndTime - nextBeginTime)
											* 1.00 / 24 * basic;

									// ��Сʱ���� ��ת��
									basic = bzbasicend;
									tempbzbasic = tempbzbasic
											+ (24 - nextEndTime) * 1.00 / 24
											* basic;

									double hours = (24 - lastEndTime);
									xc_days = OperationUtil
											.round(hours / 24, 2);
									String cx_endtime = OperationUtil.round(24,
											2);
									ifhavexc = "cq";
									xc_money = OperationUtil.round(tempbzbasic,
											2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "cq", xc_days, xc_money,
											cx_endtime);

									allbzbasic = allbzbasic + tempbzbasic;
								} else {
									// ��Сʱ������תǰ
									basic = bzbasic;
									double tempbzbasic = (nextBeginTime - lastEndTime)
											* 1.00 / 24 * basic;

									// ��Сʱ���� ��ת��
									basic = bzbasicend;
									tempbzbasic = tempbzbasic
											+ (nextEndTime - nextBeginTime)
											* 1.00 / 24 * basic;

									double hours = (nextEndTime - lastEndTime);
									xc_days = OperationUtil
											.round(hours / 24, 2);
									String cx_endtime = OperationUtil.round(
											nextEndTime, 2);
									ifhavexc = "cq";
									xc_money = OperationUtil.round(tempbzbasic,
											2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "cq", xc_days, xc_money,
											cx_endtime);

									allbzbasic = allbzbasic + tempbzbasic;
								}
							}

							// ���أ����㲹�����ɣ������趨����
							else if (nexttype != null && "3".equals(nexttype)) {
								if (isFinally) {
									basic = bzbasic;
									double tempbzbasic = (nextEndTime - lastEndTime)
											* 1.00 / 24 * basic;

									// ��Сʱ����
									double hours = (nextEndTime - lastEndTime);
									xc_days = OperationUtil
											.round(hours / 24, 2);
									String cx_endtime = OperationUtil.round(
											(nextEndTime - lastEndTime), 2);
									ifhavexc = "cg";
									xc_money = OperationUtil.round(tempbzbasic,
											2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "cg", xc_days, xc_money,
											cx_endtime);
									allbzbasic = allbzbasic + tempbzbasic;
								} else {
									basic = bzbasic;
									double tempbzbasic = (nextEndTime - lastEndTime)
											* 1.00 / 24 * basic;

									// ��Сʱ����
									double hours = (nextEndTime - lastEndTime);
									xc_days = OperationUtil
											.round(hours / 24, 2);
									String cx_endtime = OperationUtil.round(
											(nextEndTime - lastEndTime), 2);
									ifhavexc = "cg";
									xc_money = OperationUtil.round(tempbzbasic,
											2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "cg", xc_days, xc_money,
											cx_endtime);
									allbzbasic = allbzbasic + tempbzbasic;
								}
							}

							// �ݼٳ������㲹�����ɣ������趨����
							else if (modifytype != null
									&& "4".equals(modifytype)) {
								if (isFinally) {
									basic = bzbasic;
									String cx_endtime = "24.00";
									double tempbzbasic = 0.00D;

									tempbzbasic = (nextBeginTime - lastEndTime)
											* 1.00 / 24 * basic;
									xc_days = OperationUtil.round(
											(nextBeginTime - lastEndTime) / 24, 2);
									cx_endtime = OperationUtil.round(nextBeginTime,
											2);
									ifhavexc = "cq";
									xc_money = OperationUtil.round(tempbzbasic, 2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "cq", xc_days, xc_money,
											cx_endtime);

									allbzbasic = tempbzbasic;

									if (isXinjia) {
										tempbzbasic = (24 - nextBeginTime) * basic
												/ 24;
										xc_days = OperationUtil.round(
												(24 - nextBeginTime) / 24, 2);
										cx_endtime = OperationUtil.round(24, 2);
										is_xinjia = "xx";
									} else {
										tempbzbasic = 0;
										xc_days = OperationUtil.round(
												(24 - nextBeginTime) / 24, 2);
										cx_endtime = OperationUtil.round(24, 2);
										is_xinjia = "xj";
									}

									ifhavexc = "xq";
									xc_money = OperationUtil.round(tempbzbasic, 2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "xq", xc_days, xc_money,
											cx_endtime);
									allbzbasic = allbzbasic + tempbzbasic;
									ifhavexj = true;
								} else {
									basic = bzbasic;
									String cx_endtime = "24.00";
									double tempbzbasic = 0.00D;

									tempbzbasic = (nextBeginTime - lastEndTime)
											* 1.00 / 24 * basic;
									xc_days = OperationUtil.round(
											(nextBeginTime - lastEndTime) / 24, 2);
									cx_endtime = OperationUtil.round(nextBeginTime,
											2);
									ifhavexc = "cq";
									xc_money = OperationUtil.round(tempbzbasic, 2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "cq", xc_days, xc_money,
											cx_endtime);

									if (isXinjia) {
										tempbzbasic = (nextEndTime - lastEndTime)
												* basic / 24;
										xc_days = OperationUtil
												.round((nextEndTime - lastEndTime) / 24,
														2);
										cx_endtime = OperationUtil.round(
												nextEndTime, 2);
										is_xinjia = "xx";
									} else {
										tempbzbasic = 0;
										xc_days = OperationUtil
												.round((nextEndTime - lastEndTime) / 24,
														2);
										cx_endtime = OperationUtil.round(
												nextEndTime, 2);
										is_xinjia = "xj";
									}

									ifhavexc = "xq";
									xc_money = OperationUtil.round(tempbzbasic, 2);
									WLogBuzhuTongjiUtil.insertOrUpdateCW_XC(
											wlog_uid, "xq", xc_days, xc_money,
											cx_endtime);
									allbzbasic = allbzbasic + tempbzbasic;
									ifhavexj = true;
								}

							} else {
								System.out.println("������д����1004");
								System.out.println("������д����1004");
							}
						}

					}
					lastBeginTime = nextBeginTime;
					lastEndTime = nextEndTime;

				}
			}
		} else {
//			if (waddress != null && waddress.equals(upperwaddress)) {
//				if (!waddress.equals("1")) {
//					if (upperwaddress != null && wseladdress != null
//							&& !upperwaddress.equals(wseladdress)) {
//						this.setEchoValue("�ص��ѱ��������д�����Ϣ��");
//						return "notpass";
//					}
//				}
//			} else if (waddress != null) {
//				this.setEchoValue("�ص��ѱ��������д�����Ϣ��");
//				return "notpass";
//			}

			allbzbasic = WLogBuzhuTongjiUtil.getBZbasic(waddress);
		}

		
		if(!isXJSp) {
//			cw_worklog_browse_getcw_type_last
			if(users != null && users.size() > 0) {
				//cw_type:״̬:�ڹ�˾'gs',�Ȼع�˾�ٳ���'gscc',�ڳ���'cc',�ݼٳ�'xj',,н�ٳ�'xx',
				//cw_xjsp:��������ٲ���أ���������٣��ɲ��ž��������ġ�y
				//ֻ�������һ����¼�ж�
				//modifytype = 1: ���
				//modifytype = 2: ��ת��
				//modifytype = 3: ���أ�
				//modifytype = 4: �ݼٳ���
				//modifytype = 5: �ݼٹ�
				

				//ȫ�����ǣ�Ϊʵ��
				boolean isHaveCC = false;
				boolean isHaveFH = false;
				boolean isHaveXJC = false;
				boolean isHaveXJG = false;
				for(int n = 0; n < users.size(); n++) {
					BOInstance bi = (BOInstance) users.get(n);
					String modifytype = bi.getValue("modifytype");
					String vacationtype = bi.getValue("vacationtype");
					String modifytime2 = bi.getValue("modifytime2");
					String endcitytype = bi.getValue("endcitytype");
					
					if(n == users.size()-1) {
						if("1".equals(modifytype) || "2".equals(modifytype)) {
							if(isHaveFH) {
								cw_type = "gscc";
							} else {
								cw_type = "cc";
							}
							if(isHaveXJC && isHaveXJG) {
								cw_xjsp = "y";
							}
						} else if("3".equals(modifytype)) {
							if(isHaveXJC && isHaveXJG) {
								cw_xjsp = "y";
							}
							if(modifytime2 == null || "".equals(modifytime2.trim())) {
								cw_type = "cc";
							} else {
								cw_type = "gs";
							}
						} else if("4".equals(modifytype)) {
							xj_type = vacationtype;
							is_xinjia = "xj";
							cw_type = "xj";
							cw_xjsp = "y";
						} else if("5".equals(modifytype)) {
							if(modifytime2 == null || "".equals(modifytime2.trim())) {
								cw_type = "xj";
							} else {
								if("1".equals(endcitytype)) {
									cw_type = "gs";
								} else {
									cw_type = "cc";
								}
							}
						}
						
						
					} else {
						if("1".equals(modifytype) || "2".equals(modifytype)) {
							isHaveCC = true;
						}
						if("3".equals(modifytype)) {
							isHaveFH = true;
						}
						if("4".equals(modifytype)) {
							xj_type = vacationtype;
							is_xinjia = "xj";
							isHaveXJC = true;
						}
						if("5".equals(modifytype)) {
							isHaveXJG = true;
						}
					}
					
				}
				
				

				//���ǲ�������ֻȡ���һ����¼
//				BOInstance bi = (BOInstance) users.get(users.size()-1);
//				String modifytype = bi.getValue("modifytype");
//				String modifytime2 = bi.getValue("modifytime2");
//				String endcitytype = bi.getValue("endcitytype");
//				if("1".equals(modifytype) || "2".equals(modifytype)) {
//					cw_type = "cc";
//				} else if("3".equals(modifytype)) {
//					if(modifytime2 == null || "".equals(modifytime2.trim())) {
//						cw_type = "cc";
//					} else {
//						cw_type = "gs";
//					}
//				} else if("4".equals(modifytype)) {
//					cw_type = "xjc";
//				} else if("5".equals(modifytype)) {
//					if(modifytime2 == null || "".equals(modifytime2.trim())) {
//						cw_type = "xjc";
//					} else {
//						if("1".equals(endcitytype)) {
//							cw_type = "gs";
//						} else {
//							cw_type = "cc";
//						}
//					}
//				}
				
				
			} else {
				//û�б���ص���Ϣʱ����ȡ��һ����¼�ж�
				cw_type = getCw_type(waddress);
			}
		} else {
			cw_type = is_xinjia;
			cw_xjsp = "y";
		}
		if(ifhavexj) {
			cw_xjsp = "y";
		}
		
		// ���ݴ洢�����ݿ�
		DOService bzservice = DOService
				.getService("cw_worklog_update_logManage_buzhu");
		String buzhu = OperationUtil.round(allbzbasic, 2);
		Double dbz = Double.valueOf(buzhu);
		try {
			bzservice.invokeUpdate(dbz.toString(), cw_type, cw_xjsp, xj_type,
					is_xinjia, ifhavexc, curr_uid);
		} catch (ExedoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.setEchoValue("�����洢����");
		}

		return DEFAULT_FORWARD;
	}

	 public String getCw_type(String waddress) {
	 // û�б���ص���Ϣʱ����ȡ��һ����¼�ж�
	 String cw_type = "";
	 DOService cw_typeSevr = DOService
	 .getService("cw_worklog_browse_getcw_type_last");
	 List cw_typeList = new ArrayList();
	 try {
	 cw_typeList = cw_typeSevr.invokeSelect();
	 if (cw_typeList != null && cw_typeList.size() > 0) {
	 // �м�¼����ȡcw_type
	 BOInstance bi = (BOInstance) cw_typeList.get(0);
	 cw_type = bi.getValue("cw_type");
	 if ("gscc".equals(cw_type)) {
	 cw_type = "cc";
	 }
	 if (cw_type == null || "".equals(cw_type.trim())) {
	 if ("1".equals(waddress)) {
	 cw_type = "gs";
	 } else {
	 cw_type = "cc";
	 }
	 }
	 } else {
	 // û�м�¼����ȡ��ǰ��¼�Ĺ����ص����ж�
	 if ("1".equals(waddress)) {
	 cw_type = "gs";
	 } else {
	 cw_type = "cc";
	 }
	 }
	 } catch (Exception e) {
	 this.setEchoValue("error" + e.toString());
	 return "notpass";
	 }
	
	 return cw_type;
	 }

	/**
	 * return 0.00:beginTimeΪnull ���� :����ʱ�䵱�죬������Ϊ��ʱ����+��/ʱ��
	 * */
	public double getBeginHours(String beginTime) {

		if (beginTime == null || beginTime.trim().equals("")) {
			return 0.00;
		} else {
			String[] one = beginTime.trim().split(":");
			String hour = one[0];
			String min = one[1];

			double beginHours = Integer.parseInt(hour) * 1.00 + Integer
					.parseInt(min) * 1.00 / 60;
			String strDou = OperationUtil.round(beginHours, 2);
			return Double.parseDouble(strDou);
		}

	}

	/**
	 * return 0:�ȹ���ʱ��Сһ������ 24.00:endTimeΪnull �� ���ڹ���ʱ�� ���� :����ʱ�䵱�죬������Ϊ��ʱ����+��/ʱ��
	 * */
	public double getEndHours(String endTime) {

		if (endTime == null || endTime.trim().equals("")) {
			return 24.00;
		} else {
			String[] one = endTime.trim().split(":");
			String hour = one[0];
			String min = one[1];

			double beginHours = Integer.parseInt(hour) * 1.00 + Integer
					.parseInt(min) * 1.00 / 60;
			String strDou = OperationUtil.round(beginHours, 2);
			return Double.parseDouble(strDou);
		}
	}

	public static void main(String[] args) {
		WLogCountBuZhu wlc = new WLogCountBuZhu();

	}
}
