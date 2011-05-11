package com.exedosoft.plat.login;

import java.util.List;

import com.exedosoft.plat.ExedoException;
import com.exedosoft.plat.action.DOAbstractAction;
import com.exedosoft.plat.bo.DOService;
import com.exedosoft.plat.login.zidingyi.excel.LDAPPeopleUtil;
import com.exedosoft.plat.util.DOGlobals;
import com.exedosoft.plat.util.StringUtil;

public class ResetPasswordLDAP extends DOAbstractAction {

	public String excute() throws ExedoException {
		// TODO Auto-generated method stub
		//do.bx.user.update.passowrd
		
		// 	do.bx.user.findbynameAndPassword
		String old_password = this.actionForm.getValue("old_password");
		String new_password1 = this.actionForm.getValue("new_password1");
		String new_password2 = this.actionForm.getValue("new_password2");
		
		if(old_password==null || "".equals(old_password.trim())){
			this.setEchoValue("�����벻��Ϊ��");
			return NO_FORWARD;
		}
		if(!new_password1.equals(new_password2)){
		   this.setEchoValue("��������������벻һ��");
		   return NO_FORWARD;
		   
		}
		String userName = DOGlobals.getInstance().getSessoinContext().getInstance().getUser().getUid();
		
		
		if (LoginActionLDAP.check(userName, old_password)) {
			DOService ser = DOService.getService("zf_employee_update_pwd_by_uid");
			try {
				String SHApwd = "{SHA}"+LDAPPeopleUtil.passwordKey(new_password2);
				ser.invokeUpdate(SHApwd,userName);
			} catch (ExedoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				this.setEchoValue("�����޸�ʧ�ܣ��������޸�!��");
			}
			this.setEchoValue("�����޸ĳɹ���");
		} else {
			this.setEchoValue("�������������������!");
			return "notpass";
		}
	
		return DEFAULT_FORWARD;
	}
	
	
	public static void main(String[] args) throws ExedoException{
		
		DOService aService = DOService.getService("DO_ORG_ACCOUNT_ResetPassword");
		aService.invokeUpdate("a","a");
		
		
	}

}
