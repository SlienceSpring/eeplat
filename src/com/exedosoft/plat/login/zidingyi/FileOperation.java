package com.exedosoft.plat.login.zidingyi;

import java.io.File;

public class FileOperation {

	/**
	 * @param delFile:0�ļ�������;1,�ļ�ΪĿ¼;2,�ļ��ɹ�ɾ��;3,�ļ�ɾ��ʧ��
	 */
	public static int delFile(String filePath) throws Exception {
		
		int delFile = 0;
		File file = new File(filePath);
		if(file.exists() && file.isDirectory()) {
			delFile = 1;
		} else if(file.exists() && file.isFile()) {
			boolean isSuccess = file.delete();
			if(isSuccess)
				delFile = 2;
			else 
				delFile = 3;
		}
		
		return delFile;
	}
	
	/**
	 * @param isExists:�ļ��Ƿ����
	 */
	public static boolean isExistsFile(String filePath) throws Exception {
		boolean isExists = false;
		File file = new File(filePath);
		if(file.exists())
			isExists = true;
		return isExists;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			boolean b = FileOperation.isExistsFile("F:\\workspI\\yiyi_new\\WebContent\\upload\\");
			
			int i = FileOperation.delFile("F:\\workspI\\yiyi_new\\WebContent\\upload\\");
//			System.out.println(i + " === " + b);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
