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
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;

import biojar.function.GeneralMethod;

import javax.swing.JPanel;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;

import java.util.ArrayList;

/**
 * 项目通用多选对话框组件
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class MutiSelectFrame extends JFrame {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202102041305L;
	private final ArrayList <JCheckBox> jcbarray = new ArrayList<>();
	private final JPanel jp = new JPanel();
	private final JScrollPane jsp = new JScrollPane(jp);
	private final JButton OK_OPTION = new JButton("OK");
	private final JButton CANCEL_OPTION = new JButton("Cancel");
	private final int DEFAULT_WIDTH = 440;
	private final int DEFAULT_HEIGHT = 330;
	private final ArrayList <String> select_value = new ArrayList<>();
	private final ArrayList <String> default_select = new ArrayList<>();
	private File CONFIG_FILE;
	/**
	 * 构造方法
	 * @param title 标题
	 * @param configfile 配置文件名
	 * @param defaultselect 默认选项
	 */
	public MutiSelectFrame(String title, String configfile, ArrayList <String> defaultselect) {
		initComponent(title, configfile, defaultselect);
	}
	/**
	 * 组件构造方法
	 * @param title 标题
	 * @param configfile 配置文件名
	 * @param defaultselect 默认选项
	 */
	private final void initComponent(String title, String configfile, ArrayList <String> defaultselect) {
		for (String s: defaultselect) default_select.add(s);
		ArrayList <Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_256_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_128_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_64_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_32_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_16_ICON)).getImage());
		setIconImages(icons);
		setTitle(title);
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setResizable(false);
		setLayout(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int screenwidth = (int)screen.getWidth();
		int screenhight = (int) screen.getHeight();
		setLocation(screenwidth/2-getWidth()/2, screenhight/2-getHeight()/2);
		CONFIG_FILE = new File(configfile);
		
		
		int jsp_width = DEFAULT_WIDTH*2/3;
		int jsp_height = DEFAULT_HEIGHT*7/8;
		jsp.setBounds(DEFAULT_WIDTH/3-jsp_width/2, 0, jsp_width, jsp_height);
		add(jsp);
		try {
			loadConfigureFile();
			jp.setLayout(new GridLayout(jcbarray.size(), 1));
			for(JCheckBox e: jcbarray) {
				jp.add(e);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		
		int button_width = 80;
		int button_height = 30;
		OK_OPTION.setBounds(DEFAULT_WIDTH*5/6 - button_width/2, DEFAULT_HEIGHT/3-button_height/2, button_width, button_height);
		add(OK_OPTION);
		MutiSelectFrame thisframe = this;
		OK_OPTION.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (JCheckBox jbox: jcbarray) {
					if (jbox.isSelected()) {
						select_value.add((String) jbox.getSelectedObjects()[0]);
					}
				}
				synchronized (thisframe) {thisframe.notifyAll();}
				setVisible(false);
			}
		});
		
		
		CANCEL_OPTION.setBounds(DEFAULT_WIDTH*5/6 - button_width/2, 2*DEFAULT_HEIGHT/3-button_height/2, button_width, button_height);
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
				JCheckBox tmp = new JCheckBox(line, false);
				jcbarray.add(tmp);
				if (default_select.contains(line)) tmp.setSelected(true);
			}
		}
	}
	/**
	 * @return 获取选项
	 */
	public ArrayList<String> getSelectValue() {
		return select_value;
	}
}