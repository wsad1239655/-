package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;




public class FileUtils {
	private File Path;

	//创建一个构造函数，初始化一个内部存储地址，4.4禁用第三方APP访问外部SDcard
	public FileUtils() {
		Path = Environment.getExternalStorageDirectory(); 
	}
	
	//创建一个文件，并将文件放入指定的存储地址
	public File creatFileInSDCard(String fileName,String dir) throws IOException{
		File file = new File(Path + dir + File.separator + fileName);
		//调用创建文件的函数
		file.createNewFile();
		
		return file;
	}
	
	//创建一个目录，并指定目录地址， File.separator是分隔符/
	public File creatSDDir(String dir){
		File dirFile = new File(Path + dir + File.separator);
		
		System.out.println(dirFile.mkdirs());
				
		return dirFile;		
	}
	
	//判断文件是否存在
	public boolean isFileExist(String fileName,String path){
		File file = new File(Path + path + File.separator + fileName);
		
		return file.exists();
	}
	
	//创建一个函数，用于调用以上方法创建文件目录与文件
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
