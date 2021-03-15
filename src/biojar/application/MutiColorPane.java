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
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 项目通用多颜色选择面板组件
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class MutiColorPane extends ConfirmFrame<JPanel> {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202102031810L;
	/**
	 * 标签对象集
	 */
	private JLabel[] labelSet;
	/**
	 * 选择颜色集
	 */
	private Color[] colorSet;
	/**
	 * 组件构造方法，声明为private和final以确保在子类中正确调用父类方法构造
	 * @param n 标签数，上限为10个
	 */
	private final void initComponent(int n) {
		if (n > 10) n = 10;
		setTitle("Choose color");
		JPanel colorPanel = new JPanel();
		setSize(400, 150);
		setResizable(false);
		labelSet = new JLabel[n];
		colorSet = new Color[n];
		setMainContent(colorPanel);
		colorPanel.setLayout(new GridLayout(0, 5, 5, 5));//行数与列数只设置一个，另一个为0，同时设置，列数失效
		colorPanel.setSize(350, 65);
		colorPanel.setPreferredSize(colorPanel.getSize());
		for (int i=0; i < n ;i++) {
			JLabel colorLabel = new JLabel(i+1+"");
			colorPanel.add(colorLabel);
			colorLabel.setOpaque(true);
			colorLabel.setBackground(Color.white);
			labelSet[i] = colorLabel;
			colorLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Color bgColor = JColorChooser.showDialog(MutiColorPane.this, "选择颜色", colorLabel.getBackground());
					colorLabel.setBackground(bgColor);
				}
			});
		}
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int screenwidth = (int)screen.getWidth();
		int screenhight = (int) screen.getHeight();
		setLocation(screenwidth/2-getWidth()/2, screenhight/2-getHeight()/2);
	}
	/**
	 * 构造方法，只能由showDiag()方法调用创建，无法外部直接创建
	 * @param n 标签数
	 */
	private MutiColorPane(int n) {
		super();
		initComponent(n);
	}
	@Override
	void okClicked() {
		for (int i = 0; i < colorSet.length; i++) {
			Color labelColor = labelSet[i].getBackground();
			colorSet[i] = labelColor;
		}
		synchronized(this) {
			this.notify();
		}
	}
	@Override
	void cancelClicked() {
		colorSet = null;
		synchronized(this) {
			this.notify();
		}
	}
	/**
	 * 创建对话面板，并阻塞当前线程。当关闭面板时才恢复当前线程
	 * @return 选中的颜色集，取消或者关闭，返回null
	 */
	public static Color[] showDiag() {
		/* 对象创建在当前方法的线程，而监听事件在GUI事件分发线程中，故可以线程分离。
		 * 若在javax.swing.SwingUtilities.invokeLater中创建，则是在GUI事件分发线程，
		 * 线程阻塞后会无法响应按钮事件、GUI渲染事件。
		 * */
		MutiColorPane mcp = new MutiColorPane(10);
		mcp.setVisible(true);
		//阻塞当前线程，直至点击ok键
		synchronized(mcp) {
			try {
				mcp.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		mcp.dispose();
		return mcp.colorSet;
	}
}
