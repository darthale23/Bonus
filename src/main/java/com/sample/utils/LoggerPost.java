package com.sample.utils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayDeque;
import java.util.Iterator;

public class LoggerPost {
    private String filePath;

    public LoggerPost(String path) {
        PostDB.connect();
        this.filePath = path;
        File logFile = new File(path);
        try {
            if (logFile.createNewFile()) {
                System.out.println("Log file created: " + logFile.getName());
            } else {
                System.out.println("Log file already exists.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public void PostToLog(String post) {
        Path p = Paths.get(filePath);
        String s = System.lineSeparator() + post;
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(p,
                StandardOpenOption.APPEND))) {
            out.write(s.getBytes());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void PostToDB(String post) {
        PostDB.addPost(post);
    }

    @Deprecated
    public String GetPostsFromLog() {
        String content = "";
        ArrayDeque<String> stack = new ArrayDeque<>();
        try {
            File file = new File(filePath);
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            //read the file and add only not an empty string to the stack
            String line = reader.readLine();
            while (line != null) {
                if (line.trim().length() > 0)
                    stack.push(line.trim());
                line = reader.readLine();
            }
            reader.close();
            fr.close();
            //add posts to the content in inverse order
            Iterator<String> it = stack.iterator();
            while (it.hasNext()) {
                content += it.next() +"\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(content);
        return content;
    }

    public String GetPostsFromDB() {
        return PostDB.getAllPosts();
    }

    @Deprecated
    public boolean DeletePostFromLog(String post) {
    	boolean delete = false;
        String tmpPath = filePath+".tmp";
        try {
            File file1 = new File(filePath);
            FileReader fr1 = new FileReader(file1);
            BufferedReader r1 = new BufferedReader(fr1);

            File file2 = new File(tmpPath);
            FileWriter fr2 = new FileWriter(file2);
            BufferedWriter r2 = new BufferedWriter(fr2);

            String line = r1.readLine();
            while (line != null) {
                String trimmedLine = line.trim();
                String postTrimmed = post.trim();
                if (line.isEmpty() || !postTrimmed.equals(trimmedLine)) {
                    r2.write(line+System.lineSeparator());
                }
                else
                    delete = true;
                line = r1.readLine();
            }
            r1.close();  r2.close();
            fr1.close(); fr2.close();
            Files.move(Paths.get(tmpPath), Paths.get(filePath),
                       StandardCopyOption.REPLACE_EXISTING);
            return delete;
    	} catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DeletePostFromDB(String post) {
        return PostDB.deletePost(post);
    }
}
