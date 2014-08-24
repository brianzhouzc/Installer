package me.codercloud.installer.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.json.simple.JSONValue;

public class Loader {
	
	private URL url;
	private byte[] postData;
	
	
	private byte[] result_b = null;
	private String result_s = null;
	private Object result_j = null;
	
	
	public Loader(String site, byte[] postData) throws MalformedURLException {
		this.url = new URL(site);
		this.postData = postData;
	}
	
	public URL getUrl() {
		return url;
	}
	
	public synchronized byte[] readURLBytes(Variable<Boolean> cancel) throws IOException, InterruptedException {
		if(result_b != null)
			return result_b;
		InputStream is = null;
		try {
			URLConnection con = url.openConnection();
			
			if (!(con instanceof HttpURLConnection))
				throw new IOException("Given URL is not a wabsite");

			HttpURLConnection hcon = (HttpURLConnection) con;

			
			if (postData != null) {
				hcon.setDoOutput(true);
				hcon.setRequestMethod("POST");
				hcon.setRequestProperty("Content-Length",
						String.valueOf(postData.length));
				
				OutputStream os = con.getOutputStream();
				os.write(postData);
				os.flush();
				os.close();
			}
			
			if(hcon.getResponseCode()/100!= 2)
				System.out.println(hcon.getResponseMessage());
			
			is = con.getInputStream();

			
			ByteArrayOutputStream res = new ByteArrayOutputStream();
			
			byte[] buff = new byte[1024];
			int l;
			
			while((l = is.read(buff)) != -1) {
				res.write(buff, 0, l);
				if(cancel.getValue() == true)
					throw new InterruptedException();
			}
			
			res.flush();
			
			return res.toByteArray();
		} finally {
			if(is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	public String readURLString(Variable<Boolean> cancel) throws IOException, InterruptedException {
		if(result_s != null)
			return result_s;
		return new String(readURLBytes(cancel));
	}
	
	public Object readURLJSON(Variable<Boolean> cancel) throws IOException, InterruptedException {
		if(result_j != null)
			return result_j;
		return JSONValue.parse(readURLString(cancel));
	}
	
}
