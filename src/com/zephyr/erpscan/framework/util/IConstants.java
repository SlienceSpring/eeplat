/**
 * 
 */
package com.zephyr.erpscan.framework.util;

/**
 * @author t
 * 
 */
public interface IConstants {
	
	public static final String SERVICE_FACTORY_KEY = "com.zephyr.erpscan.IErpScanServiceFactory";
	//service class name define in web.xml
	public static final String SERVICE_CLASS_KEY = "ErpScan-Service-class";
	
	public static final String SESSION_USER = "ErpScan-Session-User";		//session user
	
	//define pic state: flag
	public static final int PIC_STATE_NORMAL = 0;			//����״̬
	public static final int PIC_STATE_ALREADY_BACKUP = 1;//�ѱ���
	public static final int PIC_STATE_DUPLICATE = 2;		//���ظ����ݣ�����
	public static final int PIC_STATE_ABNORMAL = 9;		//��������Ԥ�����ã�
	
	// define pic type: type
	public static final int PIC_TYPE_INVOICE=1;	//Ӧ����Ʊ
	public static final int PIC_TYPE_EXPENSE=2;	//���ϱ���
	public static final int PIC_TYPE_ASSET=3;		//�ʲ�����
	public static final int PIC_TYPE_LEDGER=4;	//���ʵ���
	public static final int PIC_TYPE_CONTRACT=8;//��ͬ����
	
	//Ӧ����Ʊ������
	public static final int INVOICE_TYPE_ADVANCE = 5; 	//Ԥ����
	public static final int INVOICE_TYPE_CANCEL = 6; 	//����Ԥ����
	public static final int INVOICE_TYPE_NATURAL = 7;	//��������
	
	//��ͬ����������
	//FZ ����/��ҵ
	public static final int PIC_TYPE_FZCONTRACT=81;
	//ZX װ��
	public static final int PIC_TYPE_ZXCONTRACT=82;
	//GX �������
	public static final int PIC_TYPE_GXCONTRACT=83;
	//CG �ɹ�
	public static final int PIC_TYPE_CGCONTRACT=84;
	//KC �Ƽ��ɹ�
	public static final int PIC_TYPE_KCCONTRACT=85;
	//FW ����
	public static final int PIC_TYPE_FWCONTRACT=86;
	//QT ����
	public static final int PIC_TYPE_QTCONTRACT=87;
	
	
	// define invoice status ��ʾ��oracleϵͳ���Ƿ�¼��: status
	// ��չ�� �ʲ������� 20070614
	
	public static final int PIC_ALREADY_INPUT=2;	    //��¼��
	public static final int PIC_NOT_INPUT=1;		    //δ¼��
	public static final int PIC_STATUS_UNKNOW=3;	    //δ֪
	public static final int PIC_RELAY_INPUT=4;         //������
	public static final int PIC_OLD_INPUT=5;           //��¼��
	
	//define priv_explain field
	//�޸ı�������
	public static final String MODIFY_SELF_PWD = "modify_self_pwd";
	//�û�����
	public static final String MANAGE_CLERK = "manage_clerk_p";
	//���ݲ�ѯ
	public static final String BROWSER_IMAGE = "browser_image_p";
	//ɨ���ϴ�
	public static final String SCAN_UPDATE = "scan_update_p";
	//����ظ�����
	public static final String CHECK_REDUPLICATE = "check_reduplicate_p";
	//	���е���¼��
	public static final String WRITER_IMAGE = "writer_image_p";
	//���ÿ�����¼��
	public static final String WRITER_CREDIT_IMAGE = "writer_credit_image_p";
	
}
