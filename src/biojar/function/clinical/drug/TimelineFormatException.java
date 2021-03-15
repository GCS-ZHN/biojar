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

package biojar.function.clinical.drug;

/**
 * 药物/靶点临床发展时间线格式异常
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class TimelineFormatException extends Exception {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202102042017L;

	/**
	 * 异常的构造方法
	 * @param info 异常信息
	 */
	public TimelineFormatException (String info) {
		super(info);
	}
}