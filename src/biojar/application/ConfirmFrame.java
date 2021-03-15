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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

/**
 * 作为确认框框架的通用抽象类、泛型类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 * @param <T> 框架组件主体内容组件泛型类型
 */
public abstract class ConfirmFrame <T extends Component> extends JFrame {
	/**
	 * 主体内容面板
	 * */
	private JPanel mainPanel;
	/**
	 * 按钮面板
	 * */
	private JPanel buttPanel;
	/**
	 * 主体内容泛型
	 * */
	private T mainContent;
	/**
	 * 序列化ID, 值为{@value}
	 * */
	private static final long serialVersionUID = 202102032031L;
	/**
	 * 更改UI
	 */
	static {
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		}catch(Exception e) {
			System.out.println(e);
		}
	}
	/**
	 * 构造方法
	 * */
	public ConfirmFrame() {
		initComponent();
	}
	/**
	 * 初始化框架组件。声明为private而无法被子类构造器直接调用，确保super调用的是父类方法
	 * 声明为final也是为了确保无法被子类方法覆盖。
	 */
	private final void initComponent() {
		//声明内部组件
		JButton okButton = new JButton("OK");
		JButton caButton = new JButton("Cancel");
		mainPanel = new JPanel();
		buttPanel = new JPanel();
		
		//初始化组件尺寸
		okButton.setSize(70, 30);
		caButton.setSize(70, 30);
		okButton.setPreferredSize(okButton.getSize());
		caButton.setPreferredSize(caButton.getSize());
		setMinimumSize(new Dimension(200,150));
		
		//配置内容面板布局
		GroupLayout contentPaneLayout = new GroupLayout(getContentPane());
		getContentPane().setLayout(contentPaneLayout);
		contentPaneLayout.setVerticalGroup(contentPaneLayout.createSequentialGroup()
			.addComponent(mainPanel)
			.addComponent(buttPanel)
		);
		contentPaneLayout.setHorizontalGroup(contentPaneLayout.createParallelGroup()
			.addComponent(mainPanel)
			.addComponent(buttPanel)
		);
		
		//配置按钮面板布局
		GroupLayout buttPaneLayout = new GroupLayout(buttPanel);
		buttPanel.setLayout(buttPaneLayout);
		buttPaneLayout.setVerticalGroup(buttPaneLayout.createSequentialGroup()
			.addGap(10)
			.addGroup(buttPaneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(okButton, okButton.getHeight(), okButton.getHeight(), okButton.getHeight())
				.addComponent(caButton, caButton.getHeight(), caButton.getHeight(), caButton.getHeight())
			)
			.addGap(10)
		);
		buttPaneLayout.setHorizontalGroup(buttPaneLayout.createSequentialGroup()
			.addGap(0,0,getWidth()/2-okButton.getWidth()-6)
			.addComponent(okButton, okButton.getWidth(), okButton.getWidth(), okButton.getWidth())
			.addGap(12)
			.addComponent(caButton, caButton.getWidth(), caButton.getWidth(), caButton.getWidth())
			.addGap(0,0,getWidth()/2-okButton.getWidth()-6)
		);
		
		//按钮布局自适应事件
		addComponentListener(new ComponentAdapter(){
			@Override
			public void componentResized(ComponentEvent e) {
				buttPaneLayout.setHorizontalGroup(buttPaneLayout.createSequentialGroup()
					.addGap(0,0,getWidth()/2-okButton.getWidth()-6)
					.addComponent(okButton, okButton.getWidth(), okButton.getWidth(), okButton.getWidth())
					.addGap(12)
					.addComponent(caButton, caButton.getWidth(), caButton.getWidth(), caButton.getWidth())
					.addGap(0,0,getWidth()/2-okButton.getWidth()-6)
				);
			}
		});
		
		//按钮点击事件
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConfirmFrame.this.okClicked();
			}
		});
		caButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConfirmFrame.this.cancelClicked();
			}
		});
		
		//窗体关闭事件
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				ConfirmFrame.this.cancelClicked();
			}
		});
	}
	/**
	 * 设置内容面板内容组件，组件类型为泛型类型
	 * @param mainContent 内容组件
	 */
	protected void setMainContent(T mainContent) {
		mainPanel.removeAll();
		mainPanel.add(mainContent);
		this.mainContent = mainContent;
	}
	/**
	 * 获取内容面板内容组件
	 * @return 内容组件
	 */
	protected T getMainContent() {
		return mainContent;
	}
	/**
	 * OK按钮点击事件对应处理方法
	 */
	abstract void okClicked();
	/**
	 * Cancel按钮点击事件对应处理方法
	 */
	abstract void cancelClicked();
}
