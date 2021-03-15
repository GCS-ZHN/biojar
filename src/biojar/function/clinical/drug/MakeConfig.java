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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

import biojar.application.MyIcon;

/**
 * 成功率时间线生成处理参数GUI配置类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class MakeConfig extends JFrame {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202102042047L;
	private static boolean make_for_year = true;
	private static boolean make_for_fill = false;
	private static int timeline_start = 1990;
	private static int timeline_end = 2019;
	private final int DEFAULT_WIDTH = 360;
	private final int DEFAULT_HEIGHT = 360;
	/**
	 * 构造方法
	 * @param title 标题
	 */
	public MakeConfig (String title) {
		initComponent(title);
	}
	/**
	 * 组件构造方法
	 * @param title 标题
	 */
	private final void initComponent(String title) {
		ArrayList <Image> icons = new ArrayList<>();

		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_256_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_128_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_64_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_32_ICON)).getImage());
		icons.add(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_16_ICON)).getImage());
		setIconImages(icons);//在图标列表中选择合适尺寸作为图标
		setTitle(title);
		setResizable(false);//不可变动大小
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int screenwidth = (int)screen.getWidth();
		int screenhight = (int) screen.getHeight();
		setLocation(screenwidth/2-getWidth()/2, screenhight/2-getHeight()/2);//屏幕正中央
		JComboBox<String> yearselect = new JComboBox<>();
		JComboBox<String> fillselect = new JComboBox<>();
		JComboBox<Integer> beginselect = new JComboBox<>();
		JComboBox<Integer> endselect = new JComboBox<>();
		JComboBox<?>[] jcbset = {yearselect, fillselect, beginselect, endselect};
	
		JLabel yeartype = new JLabel("Year or month timeline");
		JLabel filltype = new JLabel("Fill or not fill timeline");
		JLabel begintype = new JLabel("Timeline beginning");
		JLabel endtype = new JLabel("Timeline ending");
		JLabel[] labset = {yeartype, filltype, begintype, endtype};
		
		int hight = 20;
		setLayout(null);
		for(int index = 0; index < labset.length; index++) {
			jcbset[index].setBounds(200, hight, 100, 20);
			labset[index].setBounds(50, hight, 150, 20);
			add(labset[index]);
			add(jcbset[index]);
			hight+=50;
		}
		
		yearselect.addItem("year timeline");
		yearselect.addItem("month timeline");
		yearselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				make_for_year= (yearselect.getSelectedItem().toString().equals("year timeline")?true:false);
			}
		});
		fillselect.addItem("not fill");
		fillselect.addItem("fill");
		fillselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				make_for_fill= (fillselect.getSelectedItem().toString().equals("fill")?true:false);
			}
		});
		for (int y = 1900; y <= 2100; y++) {
			beginselect.addItem(y);
			endselect.addItem(y);
		}
		beginselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeline_start = (int) beginselect.getSelectedItem();
			}
		});
		endselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				timeline_end = (int) endselect.getSelectedItem();
			}
		});
	}
	/**
	 * @return 配置信息对象数组
	 */
	public Object[] getConfigure() {
		Object[] res = {make_for_year, make_for_fill, timeline_start, timeline_end};
		return res;
	}
}