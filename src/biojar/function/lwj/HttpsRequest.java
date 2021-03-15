/**
 * Copyright 1997-2021 <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>.
 * 
 * Modified at 2020-02-04
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 * 
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */

package biojar.function.lwj;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * HTTPS协议下的请求类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class HttpsRequest extends CommonMethod {
	/**
	 * 配置默认数字证书
	 */
	private static final class DefaultTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}
	/**
	 * 建立https协议的连接
	 * @param url 连接的服务器URL地址
	 * @param method 连接的方法，如GET、POST等
	 * @return 返回一个HttpsURLConnection对象
	 */
	private static HttpsURLConnection getHttpsURLConnection(String url, String method) throws IOException {
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		SSLSocketFactory ssf = ctx.getSocketFactory();
		URL realurl = new URL(url);
		HttpsURLConnection httpsConn = (HttpsURLConnection) realurl.openConnection();
		httpsConn.setRequestProperty("accept", "*/*");
		httpsConn.setRequestProperty("connection", "Keep-Alive");
		httpsConn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		httpsConn.setSSLSocketFactory(ssf);
		httpsConn.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		});
		httpsConn.setRequestMethod(method);
		httpsConn.setDoInput(true);
		httpsConn.setDoOutput(true);
		return httpsConn;
	}
	/**
	 * https协议下的get请求
	 * @param url 请求的URL
	 * @return 返回请求获得的字节数组
	 * @throws IOException 输入输出异常
	 */
	public static byte[] get(String url) throws IOException {
		HttpsURLConnection httpsConn = getHttpsURLConnection(url, "GET");
		if (isSuccess(httpsConn.getResponseCode())) {
			InputStream is = httpsConn.getInputStream();
			return getBytesFromInputStream(is);
		} else {
			return null;
		}
	}
	/**
	 * https协议下的post请求
	 * @param url 请求的URL
	 * @param data 请求所需数据
	 * @return 返回请求获得的字节数组
	 * @throws IOException 输入输出异常
	 */
	public static byte[] post(String url, Map<?, ?> data) throws IOException {
		HttpsURLConnection httpsConn = getHttpsURLConnection(url, "POST");
		String datastring = TransMapToString(data);
		setBytesToOutputStream(httpsConn.getOutputStream(), datastring.getBytes());
		if (isSuccess(httpsConn.getResponseCode())) {
			return getBytesFromInputStream(httpsConn.getInputStream());
		} else {
			return null;
		}
	}
	/**
	 * https协议下发送get请求，下载文件
	 * @param url 请求的URL
	 * @param outfile 下载文件名
	 * @param isbar 是否需要下载进度条
	 * @return true为下载成功，false为下载失败
	 * @throws IOException 输入输出异常
	 */
	public static boolean download(String url, String outfile, boolean isbar) throws IOException {
		HttpsURLConnection httpsConn = getHttpsURLConnection(url, "GET");
		String noextendfile = "";
		String[] outfilesplit= outfile.split("\\.");
		for (int index = 0; index < outfilesplit.length - 1; index ++) {
			if (!noextendfile.equals("")) {
				noextendfile += ".";
			}
			noextendfile += outfilesplit[index];
		}
		while (new File(outfile).isFile() &&new File(outfile).exists()) {
			noextendfile += "(1)";
			outfile = noextendfile+"."+outfilesplit[outfilesplit.length - 1];
		}
		String tmpfile = noextendfile + ".tmp";
		new File(tmpfile).delete();
		new File(tmpfile).deleteOnExit();//下载中途关闭程序时删除临时文件
		if (isSuccess(httpsConn.getResponseCode())) {
			InputStream is = httpsConn.getInputStream();
			Long totalsize = httpsConn.getContentLengthLong();
			if (totalsize < 0) isbar = false;
			String strtotalsize = fileSizeFormat(totalsize*1.0);
			DownloadProgress dp = new DownloadProgress("下载进度");
			dp.setEnabled(isbar);
			dp.setVisible(isbar);
			int len = 0;
			long dsize = 0;
			byte[] kb = new byte[1024];
			while ((len = is.read(kb)) != -1) {
				FileOutputStream fos = new FileOutputStream(tmpfile, true);//true为追加写入
				fos.write(kb, 0, len);//对于末端，kb字节数组长度大于实际读取长度len，故只输出至第len个字节
				fos.close();
				dsize += len;
				dp.now((int)(dsize*100/totalsize), fileSizeFormat(dsize*1.0), strtotalsize);
			}
			new File(tmpfile).renameTo(new File(outfile));
			return true;
		} else {
			return false;
		}
	}
}