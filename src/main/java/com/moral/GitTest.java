package com.moral;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by bin.shen on 03/01/2017.
 */
public class GitTest {

    // zip -d gitsync.jar META-INF/*.RSA META-INF/*.DSA META-INF/*.SF
    // java -cp gitsync.jar com.moral.GitTest

    private static String base_path = "/home/git";
    private static Connection conn;

    static {
        String url = "jdbc:mysql://localhost:3306/gitdb?user=root&password=&useUnicode=true&characterEnunicode=UTF8&useSSL=false";
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url);
        }
        catch(SQLException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {

        try {
            File dir = new File(base_path);
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if(f.isDirectory() && !f.getName().equals(".ssh")) {
                    String filename = files[i].getAbsolutePath();
                    String p_name = getProjectName(filename + "/description");
                    long p_id = insertProject(p_name, files[i].getName());
                    try {
                        Git git = Git.open(new File( filename ));
                        LogCommand command = git.log();
                        Iterator<RevCommit> commits = command.call().iterator();
                        while (commits.hasNext()) {
                            RevCommit commit = commits.next();
                            PersonIdent author = commit.getAuthorIdent();
                            PersonIdent committer = commit.getCommitterIdent();

                            Date date = new Date(new Long(commit.getCommitTime() + "000"));
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            String name = committer.getName();
                            if(name.equals("tony")) {
                                name = "陈良玉";
                            } else if(name.equals("bin.shen")) {
                                name = "沈斌";
                            } else if(name.equals("test")) {
                                name = "徐荣";
                            } else if(name.equals("april8888")) {
                                name = "丁芳敏";
                            } else if(name.equals("haijiang")) {
                                name = "张海江";
                            } else if(name.equals("WindShan")) {
                                name = "单军华";
                            }
                            insertCommit(p_id, author.getName(), name, commit.getFullMessage(), dateFormat.format(date));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProjectName(String filepath) throws Exception {

        File file = new File(filepath);
        if(file.exists()) {
            return readTxtFile(file);
        }
        return "";
    }

    public static String readTxtFile(File file) throws Exception {
        InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(read);
        String s = null;
        StringBuilder sb = new StringBuilder();
        while((s = bufferedReader.readLine()) != null){
            sb.append(s);
        }
        read.close();
        return sb.toString();
    }

    public static long insertProject(String name, String folder) throws SQLException {

        Statement stmt = conn.createStatement();
        String sql = "INSERT INTO projects (name, folder) VALUES ('" + name + "','" + folder + "')";
        System.out.println(sql);
        stmt.execute(sql);
        ResultSet rs = stmt.executeQuery("SELECT last_insert_id();");
        if (rs.next()) {
            long autoKey = rs.getInt(1);//取得ID
            stmt.close();
            return autoKey;
        } else {
            stmt.close();
        }
        return -1;
    }

    public static void insertCommit(long p_id, String author, String committer, String message, String date) throws SQLException {

        Statement stmt = conn.createStatement();
        String sql = "INSERT INTO commits (p_id, author, committer, message, created) VALUES (" + p_id + ",'" + author + "','" + committer + "','" + message.replaceAll("'", "''") + "','" + date + "')";
        System.out.println(sql);
        stmt.execute(sql);
        stmt.close();
    }
}
