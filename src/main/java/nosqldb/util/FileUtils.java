package nosqldb.util;

import nosqldb.tree.Btree;
import nosqldb.tree.KeyAndValue;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils {
    public static void saveAsFileWriter(String filePath, String content,boolean override) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        FileWriter fwriter = null;
        try {
            // true表示不覆盖原来的内容，而是加到文件的后面。若要覆盖原来的内容，直接省略这个参数就好
            fwriter = new FileWriter(filePath, override);
            fwriter.write(content+"\r\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                fwriter.flush();
                fwriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    /**
     * 读取一个文本 一行一行读取
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static List<String> readFile02(String path){
        // 使用一个字符串集合来存储文本中的路径 ，也可用String []数组
        List<String> list = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream(path);
            // 防止路径乱码   如果utf-8 乱码  改GBK     eclipse里创建的txt  用UTF-8，在电脑上自己创建的txt  用GBK
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.equals("")){
                    list.add(line);
                }
            }
            br.close();
            isr.close();
            fis.close();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取索引
     */
    public static Btree readFileIndex(String path){
        // 使用一个字符串集合来存储文本中的路径 ，也可用String []数组
        Btree btree = new Btree(4 );
        try {
            FileInputStream fis = new FileInputStream(path);
            // 防止路径乱码   如果utf-8 乱码  改GBK     eclipse里创建的txt  用UTF-8，在电脑上自己创建的txt  用GBK
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.equals("")){
                    String[] split = line.split("\\|");
                    KeyAndValue keyAndValue = new KeyAndValue(Integer.parseInt(split[0]),Long.valueOf(split[1]));
                    btree.insert(keyAndValue);
                }
            }
            br.close();
            isr.close();
            fis.close();
            return btree;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * @param file 文件路径
     * @param startLine 第n行开始读，Java 下标为 0
     * @param limit 每次读取的行数
     * @return
     */
    public  static List<String> readFileToLineGoLine(String file , int startLine, int limit) {
        Path path = Paths.get(file);
        //读取文件
        Stream<String> linesAll = null;
        try {
            linesAll = Files.lines(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> collect = linesAll.skip(startLine)
                .limit(limit)
                .collect(Collectors.toList());
        return collect;
    }

    public  static String readLine(String file , int line) {
        List<String> list = readFileToLineGoLine(file, line, 1);
        return list.get(0);
    }

    /**
     * delete data
     * @param fileName
     * @param oldstr
     * @return
     */
    public static int deleteData(String fileName, String oldstr) {
        RandomAccessFile raf = null;
        int count=0;
        try {
            raf = new RandomAccessFile(fileName, "rw");
            // 记住上一次的偏移量
            long lastPoint = 0;
            String line= new String(raf.readLine().getBytes("iso-8859-1"));
            while (line != null) {
                // 文件当前偏移量
                final long ponit = raf.getFilePointer();
                // 查找要替换的内容
                boolean update=false;
                if (line.contains(oldstr)) {
                    count++;
                    //delete
                    String str = line.replaceFirst("0", "1");
                    raf.seek(lastPoint);
                    raf.write(str.getBytes());
                }
                lastPoint = ponit;
                String s = raf.readLine();
                if (s!=null){
                    line= new String(s.getBytes("iso-8859-1"));
                }else {
                    line=null;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return count;
    }


    public static String selectDataByPrimary(String fileName, String key,int primaryPosition) {
        RandomAccessFile raf = null;
        int count=0;
        try {
            raf = new RandomAccessFile(fileName, "rw");
            // 记住上一次的偏移量
            long lastPoint = 0;
            String line= new String(raf.readLine().getBytes("iso-8859-1"));
            while (line != null) {
                // 文件当前偏移量
                final long ponit = raf.getFilePointer();
                // 查找要替换的内容
                boolean update=false;
                String[] split = line.split("\\|");
                String s1 = split[primaryPosition + 1];
                if (s1.equals(key)){
                    return line;
                }
                lastPoint = ponit;
                String s = raf.readLine();
                if (s!=null){
                    line= new String(s.getBytes("iso-8859-1"));
                }else {
                    line=null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static List<String> getValueByLike(String path,String value){
        // 使用一个字符串集合来存储文本中的路径 ，也可用String []数组
        List<String> list = new ArrayList<String>();
        try {
            FileInputStream fis = new FileInputStream(path);
            // 防止路径乱码   如果utf-8 乱码  改GBK     eclipse里创建的txt  用UTF-8，在电脑上自己创建的txt  用GBK
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line = "";
            while ((line = br.readLine()) != null) {
                if (!line.equals("")&&line.contains(value)){
                    list.add(line);
                }
            }
            br.close();
            isr.close();
            fis.close();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
