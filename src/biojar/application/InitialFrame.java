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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
/**
 * 项目可执行程序GUI启动面板类
 * @version 3.8.5
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class InitialFrame extends JFrame {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202011071125L;
	/**
	 * 自定义UI的静态代码块
	 */
	static {
		try {//更改UI外观
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	/**
	 * 构造方法
	 */
	public InitialFrame() {
		super();
		ArrayList <Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_256_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_128_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_64_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_32_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_16_ICON)).getImage());
		setIconImages(icons);//在图标列表中选择合适尺寸作为图标
		setUndecorated(true);
		FigurePane contentPanel = new FigurePane(InitialFrame.class.getResource("/fig/initialFigure.png"));//head_large.jpg
		setSize(contentPanel.getSize());
		setContentPane(contentPanel);
		setBackground(new Color(0, 0, 0, 0));
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int screenwidth = (int)screen.getWidth();
		int screenhight = (int) screen.getHeight();
		setLocation(screenwidth/2-getWidth()/2, screenhight/2-getHeight()/2);
	}
}
