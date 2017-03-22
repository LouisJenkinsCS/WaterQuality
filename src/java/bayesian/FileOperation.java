/*
 * Defining some file operations:
 * Create folder or file
 * Delete folder or file
 * Copy or Move file to folder
 * Filter files with speified extension name
 * Read and Write file
 */
package bayesian;
import java.io.*;

/**
 *
 * @author Dong Zhang
 */
public class FileOperation {
    // Create a new folder
    public void newFolder(String folderPath) {
        try {
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            if (!myFilePath.exists()) {
                myFilePath.mkdir();
            }
        } catch (Exception e) {
            System.out.println("Error on creating the foilder...");
            e.printStackTrace();
        }
    }
    
    // Create a new file
    public void newFile(String filePathAndName, String fileContent) {
        try {
            String filePath = filePathAndName;
            filePath = filePath.toString();
            File myFilePath = new File(filePath);
            if (!myFilePath.exists()) {
                myFilePath.createNewFile();
            }
            FileWriter resultFile = new FileWriter(myFilePath);
            PrintWriter myFile = new PrintWriter(resultFile);
            String strContent = fileContent;
            myFile.println(strContent);
            resultFile.close();

        } catch (Exception e) {
            System.out.println("Error on creating the file...");
            e.printStackTrace();

        }

    }
    
    // Copy a file to the folder
    public void copyFile(String oldPath, String newPath) {
        try {
            //int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //File exist?
                InputStream inStream = new FileInputStream(oldPath);//Read file
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024 * 512];
                while ((byteread = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("Error on copying the file");
            e.printStackTrace();

        }

    }
    
    // Copy the folder
    public void copyFolder(String oldPath, String newPath) {
        try {
            (new File(newPath)).mkdirs(); //Create the des folder if dose not exist
            File a = new File(oldPath);
            String[] file = a.list();
            File temp = null;
            for (int i = 0; i < file.length; i++) {
                if (oldPath.endsWith(File.separator)) {
                    temp = new File(oldPath + file[i]);
                } else {
                    temp = new File(oldPath + File.separator + file[i]);
                }

                if (temp.isFile()) {
                    FileInputStream input = new FileInputStream(temp);
                    FileOutputStream output = new FileOutputStream(newPath + "/"
                            + (temp.getName()).toString());
                    byte[] b = new byte[1024 * 5];
                    int len;
                    while ((len = input.read(b)) != -1) {
                        output.write(b, 0, len);
                    }
                    output.flush();
                    output.close();
                    input.close();
                }
                if (temp.isDirectory()) {
                    //in case of a sub dir
                    copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("Error on copying the folder...");
            e.printStackTrace();

        }

    }
    
    // Move a file to the folder
    public void moveFile(String oldPath, String newPath) {
        copyFile(oldPath, newPath);
        delFile(oldPath);

    }
    
    // Delete all content in the folder
    public void delAllFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (!file.isDirectory()) {
            return;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                delAllFile(path + "/" + tempList[i]);   // Clean the folder first
                delFolder(path + "/" + tempList[i]);    // Delet the folder
            }
        }
    }
    
    // Delete a file
    public void delFile(String filePathAndName) {
        try {
            String filePath = filePathAndName;
            filePath = filePath.toString();
            java.io.File myDelFile = new java.io.File(filePath);
            myDelFile.delete();

        } catch (Exception e) {
            System.out.println("Error on deleting the file...");
            e.printStackTrace();

        }

    }
    
    // Delete a folder
    public void delFolder(String folderPath) {
        try {
            delAllFile(folderPath); //Clean the content inside
            String filePath = folderPath;
            filePath = filePath.toString();
            java.io.File myFilePath = new java.io.File(filePath);
            myFilePath.delete(); //Delet the empty folder

        } catch (Exception e) {
            System.out.println("Error on deleting the folder...");
            e.printStackTrace();

        }

    }
    
    // Filter a file list
    public File[] fileFilter(String foldername, String extName){
        File f = new File(foldername);

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith("." + extName);
            }
        };
        
        return f.listFiles(filter);
    }
   
}
