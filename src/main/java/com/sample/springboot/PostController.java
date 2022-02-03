package com.sample.springboot;

import com.sample.utils.LoggerPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PostController {

    @Autowired
    private Environment env;

    @RequestMapping(value = "/")
    public String Index() {
        return "index";
    }

    @RequestMapping(value = "/api")
    public String AddPost(@RequestParam("post_text") String inputTest,
    Model model) {
        model.addAttribute("title","Post Page");
        System.out.println(inputTest);
        String logPath = env.getProperty("post.log.file");
        System.out.println(logPath);
        LoggerPost pl = new LoggerPost(logPath);
        pl.PostToDB(inputTest);
        return "index";
    }


    @RequestMapping(value = "/history")
    public String GetAllPosts(Model model) {
        String logPath = env.getProperty("post.log.file");
        LoggerPost pl = new LoggerPost(logPath);
        String history = pl.GetPostsFromDB();
        model.addAttribute("history", history);
        model.addAttribute("newLineChar", '\n');
        return "history";
    }

    @RequestMapping(value = "/delete")
    public String DeletePost(@RequestParam("post_text") String deleteText, 
    Model model) {
        model.addAttribute("title", "Delete Page");
        String logPath = env.getProperty("post.log.file");
        LoggerPost pl = new LoggerPost(logPath);
        if (!deleteText.isEmpty()) {
            boolean deleted = pl.DeletePostFromDB(deleteText);
            model.addAttribute("deleted", deleted);
            model.addAttribute("deleteAttempted", true);
        }
        return "delete";
    }

}

