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
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import biojar.application.MyIcon;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;

/**
 * 提供LWP服务通用下载进度显示的工具类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class DownloadProgress extends JFrame {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202102041412L;
	/**
	 * 进度条集合
	 */
	JProgressBar[] probar_set;
	/**
	 * 构造方法
	 * @param title 标题
	 */
	public DownloadProgress(String title) {
		this(title, 1);
	}
	/**
	 * 进度条类构造方法
	 * @param title 进度条标题
	 * @param numbar 进度条数
	 */
	public DownloadProgress(String title, int numbar) {
		initComponent(title, numbar);
	}
	/**
	 * 进度条类组件构造方法
	 * @param title 进度条标题
	 * @param numbar 进度条数
	 */
	private final void initComponent(String title, int numbar) {
		ArrayList <Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_256_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_128_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_64_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_32_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_16_ICON)).getImage());
		setIconImages(icons);//在图标列表中选择合适尺寸作为图标
		setTitle(title);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 80*numbar);
		setLayout(new GridLayout(numbar,1));
		probar_set=new JProgressBar[numbar];
		for (int i = 0; i < probar_set.length; i++) {
			probar_set[i] = new JProgressBar();
			add(probar_set[i]);
		}
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int screenwidth = (int)screen.getWidth();
		int screenhight = (int) screen.getHeight();
		setLocation(screenwidth/2-getWidth()/2, screenhight/2-getHeight()/2);//屏幕正中央
	}
	/**
	 * 状态更新方法
	 * @param s 状态值
	 * @param current 当前信息
	 * @param total 总计信息
	 */
	public void now (int s, String current, String total) {
		now(0, s, current, total);
	}
	/**
	 * 状态更新方法
	 * @param index 更新的进度子条
	 * @param s 状态值
	 * @param current 当前信息
	 * @param total 总计信息
	 */
	public void now (int index, int s, String current, String total) {
		probar_set[index].setValue(s);
		probar_set[index].setString((s<100?(s+"%"):"已完成")+" ["+current+"/"+total+"]");
		probar_set[index].setStringPainted(true);
	}
}
