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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

import biojar.function.GeneralMethod;

import javax.swing.JButton;
import javax.swing.JComboBox;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import javax.swing.ImageIcon;

/**
 * 单选框组件类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class SelectFrame extends JFrame {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202102041352L;
	/**
	 * 组合框
	 */
	private final JComboBox <String> SELECT_BOX = new JComboBox<>();
	/**
	 * 确定按钮
	 */
	private final JButton OK_OPTION = new JButton("OK");
	/**
	 * 取消按钮
	 */
	private final JButton CANCEL_OPTION = new JButton("Cancel");
	/**
	 * 默认宽度
	 */
	private final int DEFAULT_WIDTH = 320;
	/**
	 * 默认高度
	 */
	private final int DEFAULT_HEIGHT = 180;
	/**
	 * 选择值
	 */
	private String select_value = null;
	/**
	 * 配置文件对象
	 */
	private File CONFIG_FILE;
	/**
	 * 构造方法
	 * @param title 标题
	 * @param configfile 配置文件名
	 */
	public SelectFrame(String title, String configfile) {
		initComponent(title, configfile);
	}
	/**
	 * 组件构造方法
	 * @param title 标题
	 * @param configfile 配置文件名
	 */
	private final void initComponent(String title, String configfile) {
		ArrayList <Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_256_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_128_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_64_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_32_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_16_ICON)).getImage());
		setIconImages(icons);//在图标列表中选择合适尺寸作为图标
		setTitle(title);
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setResizable(false);//固定宽高
		setLayout(null);//取消Lay manager，采取自定义坐标形式
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int screenwidth = (int)screen.getWidth();
		int screenhight = (int) screen.getHeight();
		setLocation(screenwidth/2-getWidth()/2, screenhight/2-getHeight()/2);//屏幕正中央
		CONFIG_FILE = new File(configfile);
		try {
			loadConfigureFile();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		int selectbox_width = 280;
		int selectbox_height = 40;
		SELECT_BOX.setBounds(DEFAULT_WIDTH/2 - selectbox_width/2, DEFAULT_HEIGHT/4-selectbox_height/2, selectbox_width, selectbox_height);
		add(SELECT_BOX);
		
		int button_width = 80;
		int button_height = 30;
		OK_OPTION.setBounds(DEFAULT_WIDTH/3 - button_width/2, 3*DEFAULT_HEIGHT/5-button_height/2, button_width, button_height);
		add(OK_OPTION);
		SelectFrame thisframe = this;//注意actionlistener里面的this不是同一个对象
		OK_OPTION.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				select_value = (String) SELECT_BOX.getSelectedItem();
				synchronized (thisframe) {thisframe.notifyAll();}//注意actionlistener里面的this不是同一个对象
				setVisible(false);
			}
		});
		CANCEL_OPTION.setBounds(2*DEFAULT_WIDTH/3 - button_width/2, 3*DEFAULT_HEIGHT/5-button_height/2, button_width, button_height);
		add(CANCEL_OPTION);
		CANCEL_OPTION.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (thisframe) {thisframe.notifyAll();}
				setVisible(false);
			}
		});
	}
	/**
	 * 加载配置文件
	 * @throws IOException
	 */
	private void loadConfigureFile() throws IOException {
		try (LineNumberReader lnr = GeneralMethod.BufferRead(CONFIG_FILE)) {
			String line;
			while((line = lnr.readLine())!=null) {
				SELECT_BOX.addItem(line);
			}
		}
		SELECT_BOX.setSelectedIndex(-1);
	}
	/**
	 * 返回选择值
	 * @return 返回选择值字符串
	 */
	public String getSelectValue() {
		return select_value;
	}
}