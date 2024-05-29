package com.easybbs.utils;


import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTools {

    public static String encodeByMD5(String originString) {
        return StringTools.isEmpty(originString) ? null : DigestUtils.md5Hex(originString);
    }

    public static boolean isEmpty(String str) {

        if (null == str || "".equals(str) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else if ("".equals(str.trim())) {
            return true;
        }
        return false;
    }

    public static String getFileSuffix(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        return suffix;
    }

    public static final String getFileName(String fileName) {
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        return fileName;
    }

    public static final String getRandomString(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    public static final String getRandomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }


    public static String escapeTitle(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replace("<", "&lt;");
        return content;
    }

    public static String escapeHtml(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace(" ", "&nbsp;");
        content = content.replace("\n", "<br>");
        return content;
    }

    public static String delHTMLTag(String htmlStr) {
        if (StringTools.isEmpty(htmlStr)) {
            return htmlStr;
        }
        final String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
        final String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
        final String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
        final String regEx_space = "\\s*|\t|\r|\n";// 定义空格回车换行符
        final String regEx_w = "<w[^>]*?>[\\s\\S]*?<\\/w[^>]*?>";//定义所有w标签

        Pattern p_w = Pattern.compile(regEx_w, Pattern.CASE_INSENSITIVE);
        Matcher m_w = p_w.matcher(htmlStr);
        htmlStr = m_w.replaceAll(""); // 过滤script标签


        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签


        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签


        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签


        Pattern p_space = Pattern.compile(regEx_space, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(htmlStr);
        htmlStr = m_space.replaceAll(""); // 过滤空格回车标签

        htmlStr = htmlStr.replaceAll(" ", ""); //过滤
        return htmlStr.trim(); // 返回文本字符串
    }

    public static void main(String[] args) {
        System.out.println(getFileName("xxx.jpg"));
    }
}
