package org.youknow;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.Connection.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Multithread implements Runnable {

	/**
	 * 文件存储路径
	 */
	private String basePath = "E:/cache/";
	
	public String url;
	
	public static void main(String[] args) {
		// 这里写一个就开一个新线程
		String[] arr = {
			"http://7y8k.com/?m=vod-type-id-5.html",
			"http://7y8k.com/?m=vod-type-id-6.html",
			"http://7y8k.com/?m=vod-type-id-7.html",
			"http://7y8k.com/?m=vod-type-id-8.html",
			"http://7y8k.com/?m=vod-type-id-9.html",
		};
		
		for(String url : arr) {
			// 多线程同时下载
			Multithread t = new Multithread();
			t.url = url;
			new Thread(t).start();
		}
	}

	public void run() {
		List<String> list = Arrays.asList(new String[] { url });
		List<String> cache;

		o: while (true) {
			cache = new ArrayList<String>();

			boolean off = false;
			
			for (String page : list) {
				try {
					Document doc = Jsoup.connect(page).get();
					
					Elements next = doc.select(".page .pagelink_a");
					Iterator<Element> it = next.iterator();
					
					while (it.hasNext()) {
						Element link = it.next();
						String text = link.text();
						if (text.equals("下一页")) {
							System.out.println("发现：" + link.attr("abs:href"));
							cache.add(link.attr("abs:href"));
							
							off = true;
						}
					}
					
					Elements a = doc.select(".l h2 a");
					Iterator<Element> tag = a.iterator();
					while (tag.hasNext()) {
						Element lk = tag.next();
						String url = lk.attr("abs:href");
						
						System.out.println("当前下载：" + url);
						
						level2(url);
					}

					if(!off)
						break o;
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			list = cache;
		}
	}
	
	public void level2(String url) throws Exception {
		if(check(url))
			return;
		
		Document doc = Jsoup.connect(url).get();
		Elements a = doc.select("#vlink_1 ul li a");
		System.out.println("分析地址：" + a.attr("abs:href"));
		
		doc = Jsoup.connect(a.attr("abs:href")).get();
		String html = doc.toString();
		String name = substr(html, "mac_name='", "',mac_from");
		String source = substr(html, "unescape('", "'); <");
		
		int index = name.indexOf("-");
		if(index != -1 && index < 4)
			name = name.substring(index + 1);
			
		System.out.println("文件名：" + name);
		System.out.println("文件路径：" + URLDecoder.decode(source, "UTF-8"));
		
		downVideo(basePath + name, URLDecoder.decode(source, "UTF-8"));
		
		save(url);
	}
	
	private String substr(String html, String start, String end) {
		return html.substring(html.indexOf(start) + 10, html.indexOf(end));
	}

	public void downVideo(String filePath, String videoUrl) throws Exception {
		String beforeUrl = videoUrl.substring(0, videoUrl.lastIndexOf("/") + 1);
		String fileName = videoUrl.substring(videoUrl.lastIndexOf("/") + 1);
		String newFileName = URLEncoder.encode(fileName, "UTF-8");
		newFileName = newFileName.replaceAll("\\+", "\\%20");
		
		// 编码之后的url
		videoUrl = beforeUrl + newFileName;
		
		FileOutputStream out = null;
		File file = null;
		
		try {
			// 创建文件目录
			File files = new File(basePath);
			if (!files.exists()) {
				files.mkdirs();
			}
			
			// 创建文件，fileName为编码之前的文件名
			file = new File(filePath + fileName);
			if(file.exists())
				return;
			
			file = new File(filePath + fileName + ".tmp");
			if(file.exists())
				file.delete();

			Response res = Jsoup.connect(videoUrl).ignoreContentType(true).execute();
			
			// 根据输入流写入文件
			out = new FileOutputStream(file);
			out.write(res.bodyAsBytes());
			out.close();
			
			file.renameTo(new File(filePath + fileName));
		} catch (Exception e) {
			out.close();
			file.delete();
			
			throw e;
		}
	}
	
	private java.sql.Connection getConn() throws Exception {
		Class.forName("org.sqlite.JDBC");
		return DriverManager.getConnection("jdbc:sqlite:cache.db");
	}
	
	private boolean check(String url) {
		java.sql.Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			conn = getConn();
			ps = conn.prepareStatement("select * from cache where url = ?");
			ps.setString(1, url);
			rs = ps.executeQuery();
			return rs.next();
		} catch(Exception e) {
			e.printStackTrace();
			return true;
		} finally {
			close(rs);
			close(ps);
			close(conn);
		}
	}
	
	private void save(String url) {
		java.sql.Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			conn = getConn();
			ps = conn.prepareStatement("insert into cache(url) values(?)");
			ps.setString(1, url);
			ps.executeUpdate();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			close(ps);
			close(conn);
		}
	}
	
	private void close(Object obj) {
		if(obj == null)
			return;
		
		try {
			java.lang.reflect.Method method = obj.getClass().getMethod("close");
			if (method != null)
				method.invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}