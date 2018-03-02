package org.youknow;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class App {

	public static void main(String[] args) throws Exception {
		String basePath = "E:/cache/";

		// magnet:?xt=urn:btih:6fe0f2c3987a39eedd864ad873bd7d34dca55d89&dn=%E7%AC%AC%E4%B8%80%E6%9C%83%E6%89%80%E6%96%B0%E7%89%87%40SIS001%40%28HIBINO%29%28HAVD-863%29%E7%94%9F%E6%84%8F%E6%B0%97%E3%81%AA%E3%82%A4%E3%83%9E%E3%83%89%E3%82%ADGAL%E3%82%82%E3%83%A1%E3%83%AD%E3%83%A1%E3%83%AD%E3%81%AB%E3%81%AA%E3%82%8B%E2%80%A6%E4%B8%AD%E5%B9%B4%E7%94%B7%E3%81%A8%E3%81%AE%E3%81%84%E3%82%84%E3%82%89%E3%81%97%E3%81%84%E6%BF%83%E5%8E%9A%E6%8E%A5%E5%90%BB%E3%81%A8SEX_%E9%9B%AB%E8%8A%B1
		List<String> list = Arrays.asList(new String[] { "https://en.jav321.com/video/h_094ktds00694" });
		List<String> cache;

		while (true) {
			cache = new ArrayList<String>();
			for (String page : list) {
				Document doc = Jsoup.connect(page).get();

				Elements imgs = doc.select(".row .col-md-3 .col-xs-12 img");
				Iterator<Element> it = imgs.iterator();
				while (it.hasNext()) {
					Element img = it.next();
					String src = img.attr("src");
					if (img.attr("class").equals("img-responsive")) {
						System.out.println(src);
						downImages(basePath, src);
					}
				}
				Elements links = doc.select("a");
				Iterator<Element> link = links.iterator();
				while (link.hasNext()) {
					Element lk = link.next();
					String url = lk.attr("href");
					if (url.startsWith("/video/")) {
						cache.add(lk.attr("abs:href"));
					}
				}
			}
			list = cache;
		}
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
			File files = new File(filePath);
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