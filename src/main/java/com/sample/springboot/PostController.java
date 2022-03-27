package com.sample.springboot;

import com.sample.utils.LoggerPost;
import com.sample.utils.LoginInfo;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class PostController {

    @Autowired
    private Environment env;

    private static final String logAttr = "LOGGERPOST";

    LoggerPost getLogger(HttpSession sesh) {
        String logPath = env.getProperty("post.log.file");
        String known_hosts_path = env.getProperty("known_hosts_path");
        LoggerPost pl = (LoggerPost)sesh.getAttribute(logAttr);
        if (pl == null) {
            pl = new LoggerPost(logPath, known_hosts_path);
            sesh.setAttribute(logAttr, pl);
        }
        return pl;
    }

    @RequestMapping(value = "/")
    public String Index(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        return "index";
    }

    @RequestMapping(value = "/login", method=RequestMethod.GET)
    public String Login(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        return "login";
    }

    @PostMapping(value = "/login")
    public String Login(Model model, HttpSession sesh, HttpServletRequest req) {
        LoggerPost pl = getLogger(sesh);
        LoginInfo login = new LoginInfo(
            req.getParameter("dbHost"),
            req.getParameter("dbDBName"),
            req.getParameter("dbUser"),
            req.getParameter("dbPass"),
            req.getParameter("jumpHost"),
            req.getParameter("jumpUser"),
            req.getParameter("jumpPass"));
        if (login.jumpHost == "") {
            login.jumpHost = null; login.jumpUser = null; login.jumpPass = null;
        }
        pl.Login(login);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        if (!pl.GetLoggedIn())
            model.addAttribute("loginFailed", true);
        else
            return "index";

        return "login";
    }

    @RequestMapping(value = "/logout")
    public String Logout(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        pl.Logout();
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        return "index";
    }

    @RequestMapping(value = "/api")
    public String AddPost(@RequestParam("post_text") String post,
    Model model, HttpSession sesh) {
        model.addAttribute("title","Post Page");
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        if (pl.GetLoggedIn())
            pl.PostToDB(post);
        return "index";
    }


    @RequestMapping(value = "/history")
    public String GetAllPosts(Model model, HttpSession sesh) {
        LoggerPost pl = getLogger(sesh);
        String history = pl.GetPostsFromDB();
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        model.addAttribute("history", history);
        model.addAttribute("newLineChar", '\n');
        return "history";
    }

    @RequestMapping(value = "/lexihistory")
    public String GetSortedPosts(Model model, HttpSession sesh)
    {
        LoggerPost pl = getLogger(sesh);
        String lexihistory = pl.GetSortedPostsFromDB();
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        model.addAttribute("lexihistory", lexihistory);
        model.addAttribute("newLineChar", '\n');
        return "lexihistory";
    }

    @RequestMapping(value = "/delete")
    public String DeletePost(@RequestParam("post_text") String deleteText, 
    Model model, HttpSession sesh) {
        model.addAttribute("title", "Delete Page");
        LoggerPost pl = getLogger(sesh);
        model.addAttribute("isLoggedIn", pl.GetLoggedIn());
        if (!deleteText.isEmpty()) {
            boolean deleted = pl.DeletePostFromDB(deleteText);
            model.addAttribute("deleted", deleted);
            model.addAttribute("deleteAttempted", true);
        }
        return "delete";
    }

}

