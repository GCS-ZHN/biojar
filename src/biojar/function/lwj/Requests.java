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
import java.util.Map;

/**
 * HTTP/HTTPS通用请求类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class Requests {
	/**
	 * 判断请求的协议类型，支持http和https
	 * @param url 请求的URL
	 * @return 请求类型
	 */
	public static String requestType(String url) {
		String[] tmp = url.split(":");
		return tmp[0];
	}
	/**
	 * 根据请求的协议类型，发送对应的get请求
	 * @param url 请求的URL
	 * @return 返回请求获得的字节数组
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public static byte[] get(String url) throws IOException, Exception {
		switch (requestType(url)) {
			case "https": return HttpsRequest.get(url);
			case "http": return HttpRequest.get(url);
			default: throw new Exception("Unsupported Protocol");
		}
	}
	/**
	 * 根据请求的协议类型，发送对应的post请求
	 * @param url 请求的URL
	 * @param data post请求的参数
	 * @return 返回请求获得的字节数组
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public static byte[] post(String url, Map<?, ?> data) throws IOException, Exception {
		switch (requestType(url)) {
			case "https": return HttpsRequest.post(url, data);
			case "http": return HttpRequest.post(url, data);
			default: throw new Exception("Unsupported Protocol");
		}
	}
	/**
	 * 根据请求的协议类型，发送get请求并下载文件至指定文件名（含路径）
	 * @param url 请求的URL
	 * @param filename 输出文件名
	 * @param isbar 是否需要下载进度条
	 * @return 下载状态，true为成功，false为失败
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public static boolean download(String url, String filename, boolean isbar) throws IOException, Exception {
		switch (requestType(url)) {
			case "https": return HttpsRequest.download(url, filename, isbar);
			case "http": return HttpRequest.download(url, filename, isbar);
			default: throw new Exception("Unsupported Protocol");
		}
	}
}