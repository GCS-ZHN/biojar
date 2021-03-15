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
import javax.swing.*;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.Image;
import java.util.ArrayList;

/**
 * 进度条实现类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class ProgressBar extends JFrame {
	/**
	 * 序列化ID, 值为{@value}
	 */
	private static final long serialVersionUID = 202102041324L;
	/**
	 * 进度条对象
	 */
	private JProgressBar pro1;
	/**
	 * 构造方法
	 * @param title 标题
	 */
	public ProgressBar(String title) {
		initComponent(title);
	}
	/**
	 * 组件初始化方法
	 * @param title 标题
	 */
	private final void initComponent(String title) {
		ArrayList <Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_256_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_128_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_64_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_32_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_16_ICON)).getImage());
		setIconImages(icons);
		setTitle(title);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(500, 80);
		pro1=new JProgressBar();
		this.add(pro1);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int screenwidth = (int)screen.getWidth();
		int screenhight = (int) screen.getHeight();
		setLocation(screenwidth/2-getWidth()/2, screenhight/2-getHeight()/2);
	}
	/**
	 * 实时更新状态
	 * @param s 当前状态值，为0~100
	 * @param current 当前信息
	 * @param total 总信息
	 */
	public void now(int s, String current, String total) {
		pro1.setValue(s);
		pro1.setString((s<100?(s+"%"):"OK")+" ["+current+"/"+total+"]");
		pro1.setStringPainted(true);
	}
}
