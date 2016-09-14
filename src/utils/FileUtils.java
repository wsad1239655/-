package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;




public class FileUtils {
	private File Path;

	//����һ�����캯������ʼ��һ���ڲ��洢��ַ��4.4���õ�����APP�����ⲿSDcard
	public FileUtils() {
		Path = Environment.getExternalStorageDirectory(); 
	}
	
	//����һ���ļ��������ļ�����ָ���Ĵ洢��ַ
	public File creatFileInSDCard(String fileName,String dir) throws IOException{
		File file = new File(Path + dir + File.separator + fileName);
		//���ô����ļ��ĺ���
		file.createNewFile();
		
		return file;
	}
	
	//����һ��Ŀ¼����ָ��Ŀ¼��ַ�� File.separator�Ƿָ���/
	public File creatSDDir(String dir){
		File dirFile = new File(Path + dir + File.separator);
		
		System.out.println(dirFile.mkdirs());
				
		return dirFile;		
	}
	
	//�ж��ļ��Ƿ����
	public boolean isFileExist(String fileName,String path){
		File file = new File(Path + path + File.separator + fileName);
		
		return file.exists();
	}
	
	//����һ�����������ڵ������Ϸ��������ļ�Ŀ¼���ļ�
	public File write2SDFromInput(String path,String fileName,InputStream input){
		File file = null;
		OutputStream output = null;
		try {
			creatSDDir(path);
			file = creatFileInSDCard(fileName, path);
			output = new FileOutputStream(file);
			byte buffer [] = new byte [4 * 1024];
			int temp;
			while((temp = input.read(buffer)) != -1){
				output.write(buffer, 0, temp);
			}
			output.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				output.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return file;
	}


	
}
