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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 提供LWP服务基础业务处理的实现类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class CommonMethod {
	/**
	 * 从指定InputStream获得字节数组数据
	 * @param is 指定的InputStream
	 * @param totalsize 指定的InputStream的内容字节大小，若为0则不会显示进度条
	 * @return 获得的字节数组
	 * @throws IOException 输入输出异常
	 */
	protected static byte[] getBytesFromInputStream(InputStream is, long totalsize) throws IOException {
		long readsize = 0;//文件字节数较大，采用int会发生溢出
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] kb = new byte[1024];
		int len;
		DownloadProgress dp;
		if (totalsize > 0) {
			String strtotalsize = fileSizeFormat(totalsize*1.0);
			dp = new DownloadProgress("下载进度");
			dp.setVisible(true);
			while ((len = is.read(kb)) != -1) {
				baos.write(kb, 0, len);//对于末端，kb字节数组长度大于实际读取长度len，故只输出至第len个字节
				readsize += len;
				dp.now((int)(readsize*100/totalsize), fileSizeFormat(readsize*1.0), strtotalsize);
			}
		} else {
			while ((len = is.read(kb)) != -1) {
				baos.write(kb, 0, len);
				readsize += len;
			}
		}
		byte[] bytes = baos.toByteArray();
		baos.close();
		is.close();
		return bytes;
	}
	/**
	 * 从指定InputStream获得字节数组数据
	 * @param is 指定的InputStream
	 * @param totalsize 指定的InputStream的内容字节大小，若为0则不会显示进度条
	 * @return 获得的字节数组
	 * @throws IOException 输入输出异常
	 */
	protected static byte[] getBytesFromInputStream(InputStream is) throws IOException {
		return getBytesFromInputStream(is, 0);
	}
	/**
	 * 将特定字节数组通过特定输出流输出
	 * @param os 特定的输出流
	 * @throws IOException 输入输出异常
	 */
	protected static void setBytesToOutputStream(OutputStream os, byte[] bytes) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);//用字节数组建立字节数组输入流
		byte[] kb = new byte[1024];//一次读取1024B，作为缓冲
		int len;
		while ((len = bais.read(kb)) != -1) {
			os.write(kb, 0, len);
		}
		os.flush();
		os.close();
		bais.close();
	}
	/**
	 * 判断指定状态码是否为成功状态
	 * @param code HTTP状态码
	 * @return 成功与否
	 */
	public static boolean isSuccess(int code) {
		if (code < 400) {
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 从URL中解析获得get请求的参数，以Map对象形式返回
	 * @param params 解析的URL
	 * @return	获得的Map对象
	 * @throws Exception 未知异常
	 */
	public static Map<String, String> GetArgs(String params) throws Exception{
		Map<String, String> map=new HashMap<String, String>();
		String[] pairs=params.split("&");
		for(int i=0;i<pairs.length;i++){
			int pos=pairs[i].indexOf("=");
			if(pos==-1) continue;
			String argname=pairs[i].substring(0,pos);
			String value=pairs[i].substring(pos+1);
			value= URLEncoder.encode(value,"utf-8");
			map.put(argname,value);
		}
		return map;
	}
	/**
	 * 将Map对象转化为URL的编码字符串格式
	 * @param map 指定的Map对象
	 * @return  编码的URL字符串
	 */
	@SuppressWarnings("rawtypes")
	public static String TransMapToString(Map<?, ?> map){
		Map.Entry entry;
		StringBuffer sb = new StringBuffer();
		for(Iterator<?> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			entry = (Map.Entry)iterator.next();
			sb.append(entry.getKey().toString()).append( "=" ).append(null==entry.getValue()?"":
			entry.getValue().toString()).append (iterator.hasNext() ? "&" : "");
		}
		return sb.toString();
	}
	/**
	 * 将URL参数编码字符串转化为Map对象
	 * @param mapString URL参数编码字符串
	 * @return 解码转化后的Map对象
	 */
	public static Map<String, Object> TransStringToMap(String mapString){
		Map<String, Object> map = new HashMap<String, Object>();
		StringTokenizer items;
		for(StringTokenizer entrys = new StringTokenizer(mapString, "&"); entrys.hasMoreTokens();
			map.put(items.nextToken(), items.hasMoreTokens() ? ((Object) (items.nextToken())) : null)) {
				items = new StringTokenizer(entrys.nextToken(), "=");
		}
		return map;
	}
	/**
	 * 将字节数（B）根据大小转化为特定单位（B/KB/MB/GB/TB/PB）显示
	 * @param bytesize 字节大小
	 * @return 人性化单位值
	 */
	public static String fileSizeFormat(Double bytesize) {
		String[] unit = {"B", "KB", "MB", "GB", "TB", "PB"};
		int level = 0;
		while (bytesize >= 1024 && level < 5) {
			bytesize /= 1024;
			level ++;
		}
		return String.format("%.1f%s", bytesize, unit[level]);
	}

}