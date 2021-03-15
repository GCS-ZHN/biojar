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

import biojar.application.MyIcon;
import biojar.function.GeneralMethod;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * 药物临床发展成功率计算业务处理类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class CalConfig extends JFrame {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202102041427L;
	private static int start = 2003;
	private static int end = 2011;
	private static boolean cal_for_dis = true;
	private static boolean cal_for_lead = true;
	private static String type = "Infection";
	private final int DEFAULT_WIDTH = 360;
	private final int DEFAULT_HEIGHT = 360;
	/**
	 * 构造方法
	 * @param title 标题
	 */
	public CalConfig (String title) {
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
		JComboBox<String> clselect = new JComboBox<>();
		JComboBox<Integer> syselect = new JComboBox<>();
		JComboBox<Integer> eyselect = new JComboBox<>();
		JComboBox<String> diselect = new JComboBox<>();
		JComboBox<String> leadselect = new JComboBox<>();
		JComboBox<?>[] jcbset = {clselect, syselect, eyselect, diselect, leadselect};
	
		JLabel types = new JLabel("Class");
		JLabel syear = new JLabel("Start");
		JLabel eyear = new JLabel("End");
		JLabel distype = new JLabel("Disease or Drug class");
		JLabel leadtype = new JLabel("Lead or all timeline");
		JLabel[] labset = {types, syear, eyear, distype, leadtype};
		
		int hight = 20;
		setLayout(null);
		for(int index = 0; index < labset.length; index++) {
			jcbset[index].setBounds(200, hight, 100, 20);
			labset[index].setBounds(50, hight, 150, 20);
			add(labset[index]);
			add(jcbset[index]);
			hight+=50;
		}
		clselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					type = clselect.getSelectedItem().toString();
				} catch (NullPointerException e) {
					type = null;
				}
			}
		});
		try {
			LineNumberReader lnr = GeneralMethod.BufferRead(
				"configure/Timeline_Calculate/"+(cal_for_dis?"disease":"drug")+"-class.txt");
			String line = null;
			while ((line = lnr.readLine())!= null) {
				clselect.addItem(line);
			}
			lnr.close();
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		syselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				start = (int) syselect.getSelectedItem();
				syselect.setSelectedItem(start);
			}
		});
		for (int y = 1990; y <= 2018; y++) {
			syselect.addItem(y);
		}
		syselect.setSelectedItem(2003);//默认选择开始年份
		eyselect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				end = (int) eyselect.getSelectedItem();
			}
		});
		for (int y = 1991; y <= 2019; y++) {
			eyselect.addItem(y);
		}
		eyselect.setSelectedItem(2011);//默认选择结束年份
		diselect.addItem("disease class");
		diselect.addItem("drug class");
		diselect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				if(diselect.getSelectedItem().toString().equals("disease class")) {
					cal_for_dis = true;
				} else {
					cal_for_dis = false;
				}
				clselect.removeAllItems();
				try {
					LineNumberReader lnr = GeneralMethod.BufferRead(
						"configure/Timeline_Calculate/"+(cal_for_dis?"disease":"drug")+"-class.txt");
					String line = null;
					while ((line = lnr.readLine())!= null) {
						clselect.addItem(line);
					}
					lnr.close();
				} catch(FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		leadselect.addItem("lead timeline");
		leadselect.addItem("all timeline");
		leadselect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				cal_for_lead = (leadselect.getSelectedItem().toString().equals("lead timeline")?true:false);
			}
		});
	}
	/**
	 * @return 配置信息对象数组
	 */
	public Object[] getConfigure() {
		Object[] res = {cal_for_dis, cal_for_lead, start, end, type};
		return res;
	}
}