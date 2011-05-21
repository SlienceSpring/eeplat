/**
 * 
 */
package com.zephyr.erpscan;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

import com.zephyr.db.DBConnection;
import com.zephyr.erpscan.framework.exception.DatastoreException;
import com.zephyr.erpscan.framework.exception.PropertyNotFoundException;
import com.zephyr.erpscan.framework.util.Global;
import com.zephyr.erpscan.framework.util.IConstants;
import com.zephyr.erpscan.object.PicObject;

/**
 * @author t
 *
 */
public class UploadPicServiceImpl extends ErpScanServiceImpl {
	
	Logger log = Logger.getLogger(UploadPicServiceImpl.class);

	/**
	 * 
	 */
	public UploadPicServiceImpl() {
		super();

	}
	
	
	/**
	 * �ϴ�ͼƬ no use!!
	 * @param pic
	 * @param data
	 * @return
	 */
	public boolean uploadPic(PicObject pic, byte[] data) {
		
		// write file to hd
		Calendar c = Calendar.getInstance();
		
		//generate file store path
		String filePath = "";
		
		try {
			
			filePath += pic.getScanDate()+"/"
						+ pic.getDepno()+"/";
			
			//filePath += c.getTimeInMillis();
			
			//get file path prefix
			String prefix = Global.getERPScanProperties().getProperty("upload.file.store.prefix");
			
			log.debug("write file path : "+filePath);
			
			// retrieve the file data
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			
			// create folder if not exist
			File f = new File(prefix + filePath);
			
			if(!f.exists()){
				boolean ok = f.mkdirs();
				log.debug("mkdir ok? "+ok);
			}
			
			// write the file to the file specified
			OutputStream bos = new FileOutputStream(
									prefix 
									+ filePath 
									+ pic.getPath()
									);
			bos.write(data);
			
			bos.close();
			
		} catch (FileNotFoundException fnfe) {

			fnfe.printStackTrace();
			return false;
		} catch (IOException ioe) {

			ioe.printStackTrace();
			return false;
		} catch (Throwable e) {
			
			e.printStackTrace();
			return false;
		}
		
		// TODO
		// if write file to hd ok then write info to db
		String sql = "insert into imageinfo values('" 
				+ String.valueOf( c.getTimeInMillis() ).substring(0, 10) + "', '"
				+ pic.getSerialno() + "', '"
				+ pic.getScanDate() + "', '"
				+ pic.getClerk() + "', '"
				+ pic.getDepno() + "', '"
				+ filePath+pic.getPath() + "', "
				+ IConstants.PIC_STATE_NORMAL 
				+ ", null"+",'"
				+pic.getBar_code()
				+ "')";
		log.debug("insert db sql: "+sql);
		
		System.out.println("serialno code is  "+pic.getSerialno());
		System.out.println("build code is  "+pic.getBar_code());
		Connection dbcon = null;
		Statement stm = null;
		
		try {
			dbcon = Global.getConnection();

			stm = dbcon.createStatement();

			stm.execute(sql);

		} 
		catch (SQLException e) {

			e.printStackTrace();
		}
		finally{
			Global.releaseCon(stm, dbcon);
		}
		return true;
	}
	
	
	/**
	 * �ϴ�ͼƬ��ZIP��
	 * @param pic
	 * @param data
	 * @return
	 */
	public boolean uploadPicZip(PicObject pic, byte[] data, int i) {
		
		int state = IConstants.PIC_STATE_NORMAL;
		
		// ɾ���������ϴ�;
		if(i==1){
			
			int count =this.getOlddate(pic);
			/*
			// ɾ��db�е���ǰ�ϴ����ļ���Ϣ
			boolean delOK = this.deleteFromDB(pic);
			log.debug("delete from db ok?"+delOK);
			// db��ɾ���ɹ���ɾ��ʵ���ļ��������ѯ��ͼƬ��ʾ��������
			if(delOK){
				// ɾ��hd����ǰ�ϴ����ļ������ܲ����ɹ����
				log.debug("delete from hd");
				this.deleteFromHD(pic);
			}
			else{
				return false;
			}*/
			
			System.out.println("in  the one !~");
			if(count>0){
				state = IConstants.PIC_STATE_DUPLICATE;
			}
			this.delOld(pic);
		}
		// ������ǰ�ϴ������ϴ�״̬��Ϊdupliate;
		else if(i==2){
			
			//System.out.println("in  the two !~");
			// �����ϴ�ͼƬstateΪ�ظ�
			state = IConstants.PIC_STATE_DUPLICATE;
			this.reOldDup(pic);
		}
		
		
		// generate file store path, use to insert into DB
		StringBuffer filePath = new StringBuffer();
		
		// zip tmp file, will be deleted after a while
		String tmpFilename = "";
		
		// path prefix
		String prefix = "";
		
		// name of pics that in zip file
		String picNames[] = new String[0];
		
		try {
			String typePrefix = this.getInvoicePrefix(pic.getSerialno());
			
			// һ��Ŀ¼Ϊ�ϴ�����
			filePath.append(pic.getScanDate()+"/");
			// ����Ŀ¼Ϊ���б��
			filePath.append(pic.getDepno()+"/");
			// ����Ŀ¼Ϊ���ֵ������͵���Ŀ¼
			filePath.append(typePrefix+File.separator);
			
			//filePath += c.getTimeInMillis();
			
			//get file path prefix
			prefix = Global.getERPScanProperties().getProperty("upload.file.store.prefix");
			
			tmpFilename = Global.getERPScanProperties().getProperty("upload.tmp.store.path") 
						+ pic.getSerialno()+".zip";
			
			// retrieve the file data
			//ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//baos.write(data);
			
			// create folder if not exist
			File f = new File(prefix + filePath);
			
			if(!f.exists()){
				boolean ok = f.mkdirs();
				log.debug("mkdir: "+f.getAbsolutePath()+" ok? "+ok);
			}
			
			// write the zip file to the file specified
			OutputStream fos = new FileOutputStream(tmpFilename);
			/*
			fos.write(data);
			fos.flush();
			
			fos.close();
			*/
			DataOutputStream dos = new DataOutputStream(fos);
			dos.write(data, 0, data.length);
			dos.flush();
			
			// write zip ok, then extract pics and code it
			picNames = this.exZip(tmpFilename, prefix+filePath);
			
			if(picNames == null)
				return false;
			
			// if write file to hd ok then write info into db
			
			this.insertDB(pic, picNames, filePath.toString(), state);
			
		} catch (FileNotFoundException fnfe) {

			fnfe.printStackTrace();
			return false;
		} catch (IOException ioe) {

			ioe.printStackTrace();
			return false;
		} catch (Throwable e) {
			
			e.printStackTrace();
			return false;
		}
		finally{
			// delete tmp file
			new File(tmpFilename).delete();
		}
		
		return true;
	}
	
	
	/**
	 * 
	  * Method Name:getInvoicePrefix
	  * Note���õ��洢����Ŀ¼��IE��AP��FA��GL,HT��
	  * @param serialno
	  * @return
	  * Date��Nov 9, 2009, 4:50:51 PM
	  * @Author:liBing
	 */
	private String getInvoicePrefix(String serialno) {
		
		try {
			//String prefix = Global.getInvoicePrefix();
			
			if(serialno.startsWith(Global.getInvoicePrefix())){
				if(serialno.startsWith(Global.getInvoiceAdvancePrefix())){
					//Ԥ����
					return Global.getInvoicePrefix()+File.separator+Global.getInvoiceAdvancePrefix();
				}else if(serialno.startsWith(Global.getInvoiceCancelPrefix())){
					//����Ԥ����
					return Global.getInvoicePrefix()+File.separator+Global.getInvoiceCancelPrefix();
				}else if(serialno.startsWith(Global.getInvoiceNaturalPrefix())){
					//��������
					return Global.getInvoicePrefix()+File.separator+Global.getInvoiceCancelPrefix();
				}else{
					return Global.getInvoicePrefix();
				}
				//return Global.getInvoicePrefix();
			}
			else if(serialno.startsWith(Global.getExpensePrefix())){
				return Global.getExpensePrefix();
			}
			else if(serialno.startsWith(Global.getAssetPrefix())){
				return Global.getAssetPrefix();
			}
			else if(serialno.startsWith(Global.getLedgerPrefix())){
				return Global.getLedgerPrefix();
			}
			else if(serialno.startsWith(Global.getContractFzPrefix())){
				//��ͬ�������
				return Global.getContractPrefix()+File.separator+Global.getContractFzPrefix();
			}else if(serialno.startsWith(Global.getContractZxPrefix())){
				//��ͬ���װ��
				return Global.getContractPrefix()+File.separator+Global.getContractZxPrefix();
			}else if(serialno.startsWith(Global.getContractGxPrefix())){
				//��ͬ����������
				return Global.getContractPrefix()+File.separator+Global.getContractGxPrefix();
			}else if(serialno.startsWith(Global.getContractCgPrefix())){
				//��ͬ����ɹ�
				return Global.getContractPrefix()+File.separator+Global.getContractCgPrefix();
			}else if(serialno.startsWith(Global.getContractKcPrefix())){
				//��ͬ����Ƽ��ɹ�
				return Global.getContractPrefix()+File.separator+Global.getContractKcPrefix();
			}else if(serialno.startsWith(Global.getContractFwPrefix())){
				//��ͬ�������
				return Global.getContractPrefix()+File.separator+Global.getContractFwPrefix();
			}else if(serialno.startsWith(Global.getContractQtPrefix())){
				//��ͬ�������
				return Global.getContractPrefix()+File.separator+Global.getContractQtPrefix();
			}
			else{
				//return "";
				return Global.getExpensePrefix();
			}
		} catch (PropertyNotFoundException e) {
			
			e.printStackTrace();
		}
		
		
		return null;
	}


	private void delOld(PicObject pic) {
		
		Connection dbcon = null;
		
		List names = new ArrayList();
		
		Statement stm = null;
		
		try {
			dbcon = Global.getConnection();
			//Connection con = dbcon.getConnection();
			//con.setAutoCommit(false);
			
			stm = dbcon.createStatement(
								ResultSet.TYPE_SCROLL_SENSITIVE, 
								ResultSet.CONCUR_UPDATABLE
								);
			
			stm.getConnection().setAutoCommit(false);
			
			String sql = "select path from imageinfo where serialno='" +
						pic.getSerialno() + "' and scandate='" +
						pic.getScanDate() + "' ";
			log.debug("will be deleted: "+sql);
			
			ResultSet rs = stm.executeQuery(sql);
			
			
			while(rs.next()){
				
				String path = rs.getString("path").trim();
				
				names.add(path);
				
				//delete one row
				//rs.deleteRow();
				// will be deleted filename
				//new File(Global.getPicPathPrefix() + path).delete();
			}
			
			stm.getConnection().commit();
			
			//delete from db
			stm.executeUpdate("delete from imageinfo where serialno='" +
						pic.getSerialno() + "' and scandate='" +
						pic.getScanDate() + "' ");
			
			for(int i=0; i<names.size(); i++){
				try{
					new File(Global.getPicPathPrefix() + names.get(i)).delete();
				}
				catch(Exception e){ 
					// do not care
					//e.printStackTrace();
				}
			}
		}
		catch(Exception e){
			try {
				stm.getConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			log.info("exception during delete operation");
		}
		finally{
			Global.releaseCon(stm, dbcon);
		}
	}
	
	public int getOlddate(PicObject pic){
		
		Connection dbcon = null;
		
		Statement stm = null;
		
		try {
			dbcon = Global.getConnection();
			//Connection con = dbcon.getConnection();
			//con.setAutoCommit(false);
			
			stm = dbcon.createStatement();
			
			String sql = "select count('X') from imageinfo where serialno='" +
						pic.getSerialno() + "' and scandate<'" +
						pic.getScanDate() + "' ";
			log.debug("will be get: "+sql);
			
			ResultSet rs = stm.executeQuery(sql);
			while(rs.next()){
				int count = rs.getInt(1);
				if(count>0) return 1;
			}
		} catch (SQLException e) {
				// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return 0;
	}
	
	private void reOldDup(PicObject pic) {
		
		Connection dbcon = null;
		
		Statement stm = null;
		
		try {
			dbcon = Global.getConnection();
			//Connection con = dbcon.getConnection();
			//con.setAutoCommit(false);
			
			stm = dbcon.createStatement();
		
			stm.executeUpdate("update imageinfo set flag=3 where serialno='" +
						pic.getSerialno()+"'"+" and flag=2 and scandate<"+pic.getScanDate());
			
		}
		catch(Exception e){
			try {
				stm.getConnection().rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			log.info("exception during delete operation");
		}
		finally{
			Global.releaseCon(stm, dbcon);
		}
	}

	/**
	 * delete pic from harddisk
	 * @param pic
	 */
	private void deleteFromHD(PicObject pic) {
		
		Connection dbcon = null;
		Statement stm = null;
		
		try {
			dbcon = Global.getConnection();

			stm = dbcon.createStatement();
			
			String sql = "select path from imageinfo where serialno='" +
						pic.getSerialno() + "' and scandate='" +
						pic.getScanDate() + "' ";
			log.debug("will be deleted: "+sql);
			
			ResultSet rs = stm.executeQuery(sql);
			
			while(rs.next()){
				String path = rs.getString("path").trim();
				// delete from db
				new File(Global.getPicPathPrefix() + path).delete();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			log.info("exception during delete operation");
		}
		finally{
			Global.releaseCon(stm, dbcon);
		}
	}

	
	/**
	 * delete pic info from database
	 * @param pic
	 * @return
	 */
	private boolean deleteFromDB(PicObject pic) {

		Connection dbcon = null;
		Statement stm = null;
		
		String serialno = pic.getSerialno();
		String scandate = pic.getScanDate();

		try {
			dbcon = Global.getConnection();

			stm = dbcon.createStatement();
			
			String sql = "delete from imageinfo where serialno='" +
							serialno +"' and scandate='" +
							scandate + "' ";
			log.debug("delete duplicate: "+sql);
			stm.execute(sql);
			
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		finally{
			Global.releaseCon(stm, dbcon);
		}

	}

	
	/**
	 * insert pic info into database
	 * @param pic
	 * @param picNames
	 * @param filePath
	 * @param state
	 * @throws DatastoreException
	 */
	private void insertDB(PicObject pic, String[] picNames, String filePath, int state) throws DatastoreException {
		// TODO Auto-generated method stub
		Calendar c = Calendar.getInstance();

		Connection dbcon = null;
		Statement stm = null;
		
		try {
			dbcon = Global.getConnection();

			stm = dbcon.createStatement();
		
			for (int i = 0; i < picNames.length; i++) {
				String status = "null";
				
				if(pic.getSerialno().startsWith( Global.getInvoicePrefix())
						|| pic.getSerialno().startsWith(Global.getAssetPrefix())
						|| pic.getSerialno().startsWith(Global.getContractFzPrefix())
						|| pic.getSerialno().startsWith(Global.getContractZxPrefix())
						|| pic.getSerialno().startsWith(Global.getContractGxPrefix())
						|| pic.getSerialno().startsWith(Global.getContractCgPrefix())
						|| pic.getSerialno().startsWith(Global.getContractKcPrefix())
						|| pic.getSerialno().startsWith(Global.getContractFwPrefix())
						|| pic.getSerialno().startsWith(Global.getContractQtPrefix())
						|| pic.getSerialno().startsWith(Global.getLedgerPrefix())){
					status = "" + IConstants.PIC_NOT_INPUT;
				}
				
				String imgPath=filePath+picNames[i];
				if(imgPath.indexOf("\\")>=0){
					imgPath=imgPath.replace("\\", "/");
				}
				log.info("imgPath is����"+imgPath);
				// oracle
					
				String sql = "insert into imageinfo(id,serialno,scandate," +
						"clerk,depno,path,flag,backuppath,type, status) values("
				
						// ͨ��ʱ������id
						//+ String.valueOf(c.getTimeInMillis())+i
						//+ "', '" 
				
						// ��Oracle���ѽ���sequence
						+ "image_seq.NEXTVAL, '"
						+ pic.getSerialno() + "', '"
						+ pic.getScanDate() + "', '" 
						+ pic.getClerk() + "', '"
						+ pic.getDepno() + "', '" 
						+ filePath +picNames[i]
						+ "', " + state 
						+ ", null "
						+ ", "+pic.getType()
						+ ", "+status
						+ ")";
				
				// mysql
				
					   /* String sql = "insert into imageinfo (serialno,scandate," +
						"clerk,depno,path,flag,backuppath,type, status) values('" 
							+ pic.getSerialno() + "', '"
							+ pic.getScanDate() + "', '" 
							+ pic.getClerk() + "', '"
							+ pic.getDepno() + "', '" 
							+ imgPath
							+ "', " + state 
							+ ", null "
							+ ", "+pic.getType()
							+ ", "+status
							+ ")";*/
				
				stm.addBatch(sql);

				log.debug("insert db sql: " + sql);
			}
     
			int[] i = stm.executeBatch();
			//for oracle
			dbcon.commit();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			//dbcon.rollback();
			e.printStackTrace();
			throw DatastoreException.datastoreError(e);
		}
		finally{
			Global.releaseCon(stm, dbcon);
		}
	}


	/**
	 * extract images from zip file to TMPPATH
	 * @param zipFile
	 * @param path
	 */
	private String[] exZip(String zipFile, String path) {

		try {
			final int BUFFER = 2048;

			BufferedOutputStream dest = null;

			BufferedInputStream is = null;

			ZipEntry entry;

			ZipFile zipfile = new ZipFile(zipFile);

			Enumeration e = zipfile.entries();
			
			String picNames[] = new String[zipfile.size()];
			
			int i = 0;

			while (e.hasMoreElements()) {
				// �õ�zip���е�һ���ļ�
				entry = (ZipEntry) e.nextElement();
				
				picNames[i] = "" + entry;
				i++;

				log.debug("Extracting: " + entry);

				is = new BufferedInputStream(zipfile.getInputStream(entry));

				int count;

				//byte data[] = new byte[BUFFER];
				byte data[] = new byte[is.available()];
				
				// create folder if path not exist
				if(!new File(path).exists()){
					new File(path).mkdir();
				}

				FileOutputStream fos = new FileOutputStream(path+entry.getName());

				//dest = new BufferedOutputStream(fos, BUFFER);
				dest = new BufferedOutputStream(fos, is.available());

				/*
				while ((count = is.read(data, 0, BUFFER))!= -1) {

					dest.write(data, 0, count);
				}*/
				is.read(data);
				
				// ��ͼƬ�򵥼��ܺ���д��
				data = Global.code(data);
				
				dest.write(data);

				dest.flush();
				dest.close();
				is.close();
			}
			
			return picNames;

		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
		
	}


	public int isExist(String serialno) {
		
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd");

		java.util.Date d = new java.util.Date();

		String scandate = formatter.format(d);
		
		Connection dbcon = null;
		Statement stm = null;
		
		String sql = "select count('X') from imageinfo where serialno='" +
					serialno + "' and scandate='" + scandate +"'";
		
		log.debug(sql);
		
		try {
			dbcon = Global.getConnection();

			stm = dbcon.createStatement();
			
			ResultSet rs = stm.executeQuery(sql);

			while(rs.next()){
				int count = rs.getInt(1);
				if(count>0) return 1;
			}
			
			rs = stm.executeQuery(sql.replaceAll("scandate=", "scandate<"));
			
			while(rs.next()){
				int count = rs.getInt(1);
				if(count>0) return 2;
			}
		}
		catch(SQLException e){
			e.printStackTrace();
			return -1;
		}
		catch(Exception ex){
			ex.printStackTrace();
			return -1;
		}
		finally{
			Global.releaseCon(stm, dbcon);
		}
		return 0;
	}
	

}
