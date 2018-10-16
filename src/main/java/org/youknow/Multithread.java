package org.youknow;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class Multithread implements Runnable {

	private static String basePath = "E:/cache/";
	
	public String url;
	
	public static void main(String[] args) {
		String[] arr = new String[] {
			"http://7y8k.com/?m=vod-type-id-5.html",
			"http://7y8k.com/?m=vod-type-id-6.html",
			"http://7y8k.com/?m=vod-type-id-7.html",
			"http://7y8k.com/?m=vod-type-id-8.html",
		};
		
		for(String url : arr) {
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
			for (String page : list) {
				try {
					Document doc = Jsoup.connect(page).get();
					Elements a = doc.select(".l h2 a");
				
					Iterator<Element> tag = a.iterator();
					while (tag.hasNext()) {
						Element lk = tag.next();
						String url = lk.attr("abs:href");
						
						System.out.println(url);
						
						level2(url);
					}
					
					Elements next = doc.select(".page .pagelink_a");
					Iterator<Element> it = next.iterator();

					boolean off = false;
					
					while (it.hasNext()) {
						Element link = it.next();
						String text = link.text();
						if (text.equals("下一页")) {
							System.out.println(link.attr("abs:href"));
							cache.add(link.attr("abs:href"));
						}
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
	
	public static void level2(String url) throws Exception {
		Document doc = Jsoup.connect(url).get();
		Elements a = doc.select("#vlink_1 ul li a");
		System.out.println(a.attr("abs:href"));
		
		doc = Jsoup.connect(a.attr("abs:href")).get();
		String html = doc.toString();
		String name = substr(html, "mac_name='", "',mac_from");
		String source = substr(html, "unescape('", "'); <");
		
		System.out.println(name);
		System.out.println(URLDecoder.decode(source, "UTF-8"));
		
		downImages(basePath + name, URLDecoder.decode(source, "UTF-8"));
	}
	
	private static String substr(String html, String start, String end) {
		return html.substring(html.indexOf(start) + 10, html.indexOf(end));
	}

	public static void downImages(String filePath, String imgUrl) throws Exception {
		// 图片url中的前面部分：例如"http://images.csdn.net/"
		String beforeUrl = imgUrl.substring(0, imgUrl.lastIndexOf("/") + 1);
		// 图片url中的后面部分：例如“20150529/PP6A7429_副本1.jpg”
		String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
		// 编码之后的fileName，空格会变成字符"+"
		String newFileName = URLEncoder.encode(fileName, "UTF-8");
		// 把编码之后的fileName中的字符"+"，替换为UTF-8中的空格表示："%20"
		newFileName = newFileName.replaceAll("\\+", "\\%20");
		// 编码之后的url
		imgUrl = beforeUrl + newFileName;
		
		InputStream is = null;
		FileOutputStream out = null;
		
		try {
			// 创建文件目录
			File files = new File(basePath);
			if (!files.exists()) {
				files.mkdirs();
			}
			// 获取下载地址
			URL url = new URL(imgUrl);
			// 链接网络地址
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// 获取链接的输出流
			is = connection.getInputStream();
			// 创建文件，fileName为编码之前的文件名
			File file = new File(filePath + fileName);
			if(file.exists())
				return;
			
			// 根据输入流写入文件
			out = new FileOutputStream(file);
			int i = 0;
			while ((i = is.read()) != -1) {
				out.write(i);
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			is.close();
		}
	}
}