/**
 * Copyright 1997-2021 <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>.
 * 
 * Modified at 2021-02-05
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
package biojar;

import java.awt.EventQueue;

import javax.swing.JFrame;

import biojar.application.InitialFrame;
import biojar.application.MainFrame;

/**
 * 整个BioJar应用程序的入口类
 * @version 1.0
 * @since 14 2021-02-05
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class Main {
	/**
	 * 主方法，程序启动入口
	 * @param args 命令行入口参数
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(()->{//https://www.jb51.cc/java/129352.html 使用队列的好处
			InitialFrame initialFrame = new InitialFrame();//UI更新需要一起，使用相同UI更新线程。
			new Thread(()->{ //Lambda表达式构建Runnable接口实例
				initialFrame.setVisible(true);
				synchronized (initialFrame) {
					try {
						initialFrame.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				initialFrame.dispose();
			}).start();
			MainFrame mFrame = new MainFrame();
			mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			synchronized (initialFrame) {
				initialFrame.notify();
			}
			for (String fileName: args) {
				mFrame.reloadOrOpen(fileName);
			}
			mFrame.setVisible(true);
		});
	}
}
