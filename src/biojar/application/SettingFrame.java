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
package biojar.application;

import javax.swing.JFrame;

/**
 * 软件配置类
 * @version 1.0
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class SettingFrame extends JFrame {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202102041356L;
	/**
	 * 默认列分隔符，默认值为tab
	 */
	private static String default_delimiter = "\t";
	/**
	 * 获取默认列分隔符
	 * @return 默认列分隔符
	 * @see SettingFrame#setDefaultDelimiter
	 */
	public static String getDefaultDelimiter() {
		return default_delimiter;
	}
	/**
	 * 设置默认列分隔符
	 * @param delimiter 新的列分隔符
	 * @see SettingFrame#getDefaultDelimiter
	 */
	public static void setDefaultDelimiter(String delimiter) {
		default_delimiter = delimiter;
	}
	/**
	 * 构造方法
	 */
	public SettingFrame() {
		setTitle("Setting");
	}
}