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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
/**
 * 项目通用图像面板类，主应用于启动面板
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class FigurePane extends JPanel {// * Swing is based on AWT, so Canvas can be more lightweight and in lower layer ，If canvas meet all your requirements, just use Canvas
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202011071115L;
	/**
	 * 图片对象
	 */
	private ImageIcon image;
	/**
	 * 组件尺寸
	 */
	private int[] panelSize = new int[2];
	/**
	 * 默认压缩比
	 */
	private static final double RATIO = 3.705555;
	/**
	 * 默认构造方法
	 */
	public FigurePane() {
		this(new ImageIcon(MyIcon.getIconByteArray(MyIcon.DRUG_256_ICON)), RATIO);
	}
	/**
	 * 构造方法，默认压缩比例为3.705555：1
	 * @param figureURL 展示图的源地址
	 */
	public FigurePane(URL figureURL) {
		this(figureURL, RATIO);
	}
	/**
	 * 构造方法，默认压缩比例为3.705555：1
	 * @param image 展示图的ImageIcon对象
	 */
	public FigurePane(ImageIcon image) {
		this(image, RATIO);
	}
	/**
	 * 构造方法
	 * @param figureURL 展示图的源地址
	 * @param ratio 压缩比
	 */
	public FigurePane(URL figureURL, double ratio) {
		this(new ImageIcon(figureURL), ratio);
	}
	/**
	 * 构造方法
	 * @param image 展示图的ImageIcon对象
	 * @param ratio 压缩比
	 */
	public FigurePane(ImageIcon image, double ratio) {
		super();
		setImage(image, ratio);
	}
	/**
	 * 设置图像
	 * @param image 图像
	 */
	public void setImage(ImageIcon image) {
		setImage(image, RATIO);
	}
	/**
	 * 设置图像
	 * @param image 展示图的ImageIcon对象
	 * @param ratio 压缩比
	 */
	public void setImage(ImageIcon image, double ratio) {
		this.image = image;
		panelSize[0] = (int) (image.getIconWidth()/ratio);
		panelSize[1] = (int) (image.getIconHeight()/ratio);
		setSize(panelSize[0], panelSize[1]);
		setPreferredSize(new Dimension(panelSize[0], panelSize[1]));
	}
	/*
	@Override
	/**
	 * 重写实现自定义图像
	 * @param g 绘制对象
	*/
	public void paint(Graphics g) {
		Graphics2D graphics2d = (Graphics2D) g;//Graphics2D效果较好
		//graphics2d.clearRect(0, 0, getWidth(), getHeight());
		graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics2d.drawImage(image.getImage(), 0, 0, panelSize[0], panelSize[1], this);
	}
	/**
	 * 获取图像宽度
	 * @return 宽度
	 */
	public int getImageIconWidth() {
		return panelSize[0];
	}
	/**
	 * 获取图像高度
	 * @return 高度
	 */
	public int getImageIconHeight() {
		return panelSize[1];
	}
	/**
	 * 设置图像尺寸
	 * @param width 宽度
	 * @param height 高度
	 */
	public void setImageIconSize(int width, int height) {
		panelSize[0] = width;
		panelSize[1] = height;
		setSize(panelSize[0], panelSize[1]);
		setPreferredSize(new Dimension(panelSize[0], panelSize[1]));
	}
	/**
	 * 设置返回的preferredSize，用于JScrollPane判断是否需要滑动条
	 */
	public Dimension getPreferredSize() {
		return new Dimension(panelSize[0], panelSize[1]);
	}
	
}
