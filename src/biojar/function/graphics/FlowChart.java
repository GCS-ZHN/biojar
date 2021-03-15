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

package biojar.function.graphics;



import static biojar.application.SettingFrame.getDefaultDelimiter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import biojar.function.GeneralMethod;

/**
 * 绘制流动连接图的业务实现类
 * @version 2.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class FlowChart {
	/**
	 * 图像等比放大比例
	 */
	private static int ratio = 1;
	/**
	 * 曲线粗细，单位像素
	 */
	private float[] curveBond = {6 *ratio};
	/**
	 * 曲张调控角
	 */
	private double[] curveAngle = {30}; //必须在（-90,90）开区间内，单位为度
	/**
	 * 曲线透明度A
	 */
	private int[] curveAlpha = {180};//必须在0~255
	/**
	 * 画布
	 */
	private Graphics2D graphics = null;
	/**
	 * 字体指标，用于对字体绘图进行定位
	 */
	private FontMetrics metrics = null;
	/**
	 * 默认字体大小
	 */
	private int fontSize = 28 * ratio;
	/**
	 * 默认字体类型
	 */
	private String fontName = "Courier New";
	/**
	 * 默认字体颜色
	 */
	private Color fontColor = Color.WHITE;
	/**
	 * 画布宽度
	 */
	private final int figwidth;
	/**
	 * 画布高度
	 */
	private final int figheight;
	/**
	 * 以方块节点ID名为K，NodeBox对象为V的哈希表
	 */
	private HashMap<String, NodeBox> nodeMap = new HashMap<>();
	/**
	 * 以节点深度为K，该深度下所有节点ID名为V的树状表
	 */
	private HashMap<Integer, ArrayList<String>> nodeTree = new HashMap<>();
	/**
	 * 表示不同深度的方块节点的尺寸大小以及不同深度的间距
	 */
	private int[][] sizePerLayer = new int[][]{
		{36 *ratio, 72 * ratio, 162 *ratio},//KNN~
		{36 *ratio, 72 * ratio, 162 *ratio},//KNN~
		{36 *ratio, 90 * ratio, 198 *ratio},//KNN~
		{36 *ratio, 108 * ratio, 144 *ratio},//KNN~
		{36 *ratio, 108 * ratio, 144 *ratio},//KNN~
		{36 *ratio, 108 * ratio, 144 *ratio}//KNN~
	};
	/**
	 * 画图左边距或者旋转垂直后上边距
	 */
	private int xlocation = 200 * ratio;
	/**
	 * 构造方法
	 * @param width 画图宽度
	 * @param height 画图高度
	 * @param inputfile 输入文件名
	 * @throws IOException 输入输出异常
	 * @throws Exception 其他异常
	 */
	public FlowChart (int width, int height, String inputfile) throws IOException, Exception {
		figwidth = width *ratio;
		figheight = height *ratio;
		loadData(inputfile, true);
	}
	
	/**
	 * 将十六进制的RGB或RGBA参数转为颜色
	 * @param nm 十六进制的RGB或RGBA参数
	 * @return Color对象
	 */
	private Color decodeRGBA(String nm) {
		Color color = Color.decode(nm.substring(0, 7));
		if (nm.length() == 9) {
			int a = Integer.parseInt(nm.substring(7), 16);
			color = new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
		}
		return color;
	}
	/**
	 * 调整画图的放大比例
	 * @param rt 新的放大比例
	 */
	public static void setRatio(int rt) {
		if (rt > 0) ratio = rt;
	}
	/**
	 * 设置曲线属性
	 * @param angles 曲线调整角，在-90~90°之间，开区间
	 * @param alphas 透明度，在0~255之间，闭区间
	 * @param bonds 曲线粗细，单位像素
	 */
	public void setCurveProperty(double[] angles, int[] alphas, float[] bonds) {
		boolean flag = true;
		for (double angle:angles) {
			flag = -90 < angle && angle < 90;
			if (!flag) break;
		}
		if (flag) {
			curveAngle = angles;
		} else flag = true;
		for (int alpha: alphas) {
			flag = 0 <= alpha && alpha <= 255;
			if (!flag) break;
		}
		if (flag) {
			curveAlpha = alphas;
		} else flag = true;
		for (int i = 0; i < bonds.length; i++) {
			flag = bonds[i] > 0;
			bonds[i] *= ratio;
			if (!flag) break;
		}
		if (flag) {
			curveBond = bonds;
		}
	}
	/**
	 * 加载节点数据
	 * @param filename 输入文件名称
	 * @param head 输入是否包含标题
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 */
	private void loadData (String filename, boolean head) throws FileNotFoundException, IOException {
		try (LineNumberReader lnr = GeneralMethod.BufferRead(filename)) {
			String line;
			while ((line = lnr.readLine()) != null) {
				if (head && lnr.getLineNumber() == 1) continue;
				String info[] = line.split(getDefaultDelimiter());//node name node deepth node name node deepth
				String nodeID = info[0];
				String[] downstreamNodes = info[1].split(";");
				String[] upstreamNodes = info[2].split(";");
				int deepth = Integer.parseInt(info[3]);
				String nodeLabel = info[4];
				Color filledColor = decodeRGBA(info[5]);
				Color bondColor = decodeRGBA(info[6]);
				/*
				如果没有，放入当前nodeMap
				*/
				nodeMap.putIfAbsent(nodeID, new NodeBox(deepth, filledColor, bondColor));
				NodeBox nb = nodeMap.get(nodeID);
				nb.setDeepth(deepth);
				nb.setColors(filledColor, bondColor);
				nb.setLabel(nodeLabel);
				nb.setID(nodeID);
				int dindex = deepth;
				while (dindex > sizePerLayer.length - 1) dindex -= sizePerLayer.length;
				nb.setSize(sizePerLayer[dindex][0], sizePerLayer[dindex][1]);
				nodeTree.putIfAbsent(deepth, new ArrayList<>());
				nodeTree.get(deepth).add(nodeID);//更新节点网络树
				for (String dsnodeID: downstreamNodes) {
					if (dsnodeID.equals("-")) break;
					nodeMap.putIfAbsent(dsnodeID, new NodeBox(0));
					NodeBox dsnode = nodeMap.get(dsnodeID);
					nb.addDownstreamNode(dsnode);
					dsnode.addUpstreamNode(nb);
				}
				if (!info[2].equals("-")) nb.clearUpstreamNode();
				for (String usnodeID: upstreamNodes) {
					if (usnodeID.equals("-")) break;
					nodeMap.putIfAbsent(usnodeID, new NodeBox(0));
					NodeBox usnode = nodeMap.get(usnodeID);
					nb.addUpstreamNode(usnode);
				}
			}
		}
	}
	/**
	 * 配置绘图字体
	 * @param font 字体
	 */
	private void setFont(Font font) {
		if (graphics == null) return;
		graphics.setFont(font);
		metrics = graphics.getFontMetrics();
	}
	/**
	 * 绘制流程图
	 * @param outputfile 输出文件
	 * @param isRotate 是否旋转顺时针90度最终图像，即逆时针旋转90度画布
	 * @throws IOException 输入输出异常
	 * @throws Exception 其他异常
	 */
	public void paintFlowChart(String outputfile, boolean isRotate) throws IOException, Exception {
		String[] tmp = outputfile.split("\\.");
		CreateGraphics cg = new CreateGraphics(figwidth, figheight, tmp[tmp.length - 1], outputfile);
		graphics = cg.getGraphics2D();
		if (isRotate) {
			graphics.rotate(Math.toRadians(90), figwidth/2, figheight/2);
			xlocation += (figwidth - figheight)/2;
		}
		setFont(new Font(fontName,  Font.BOLD, fontSize));
		paintFlowChart();
		cg.saveToFlie();
	}
	/**
	 * 绘制流程图
	 */
	private void paintFlowChart () throws Exception {
		if (graphics == null || nodeTree.isEmpty() || metrics == null) return;
		int  ydis = 30 * ratio;
		//第一轮循环确定各个节点坐标
		for (int deepth = 0; nodeTree.containsKey(deepth); deepth ++) {
			int dindex = deepth;
			while (dindex > sizePerLayer.length - 1) dindex -= sizePerLayer.length;
			int xdis = sizePerLayer[dindex][2];
			int ylocation = (figheight - nodeTree.get(deepth).size() * (ydis + sizePerLayer[dindex][1]) + ydis) / 2;
			for (String nodeID: nodeTree.get(deepth)) {
				NodeBox nb = nodeMap.get(nodeID);
				nb.setLocation(xlocation, ylocation);
				int[] nbsize = nb.getSize();
				ylocation += (nbsize[1] + ydis);
			}
			xlocation += (sizePerLayer[dindex][0] + xdis);
		}
		

		//第二轮循环绘制连接曲线
		for (int deepth = 0; nodeTree.containsKey(deepth); deepth ++) {
			for (String nodeID: nodeTree.get(deepth)) {
				NodeBox nb = nodeMap.get(nodeID);
				int[] nbsize = nb.getSize();
				int[] nbloc = nb.getLocation();
				for (int i=0; i < nb.downstreamNodes.size(); i++) {
					NodeBox dsnb = nb.downstreamNodes.get(i);
					int dsindex = dsnb.upstreamNodes.indexOf(nb);
					if (dsindex == -1) continue;
					int cbindex = deepth, caindex = deepth, calindex = deepth;
					while (cbindex >= curveBond.length) cbindex -= curveBond.length;
					while (caindex >= curveAngle.length) caindex -= curveAngle.length;
					while (calindex >= curveAlpha.length) calindex -= curveAlpha.length;
					float bond = curveBond[cbindex];//对NON2节点粗细进行特别调整
					int[] dsnbsize = dsnb.getSize();
					int[] dsnbloc = dsnb.getLocation();
					double modify_for_center1 = (nbsize[1] - nb.downstreamNodes.size() * curveBond[cbindex])/2;
					double modify_for_center2 = (dsnbsize[1] - dsnb.upstreamNodes.size() * bond)/2;
					double x1 = nbloc[0] + nbsize[0];
					double y1 = nbloc[1] + (0.5 + i) * curveBond[cbindex] + modify_for_center1;
					double x2 = dsnbloc[0];
					double y2 = dsnbloc[1] + (0.5 + dsindex) * bond + modify_for_center2;
					Color cl = nb.getColors()[0];
					Color lineColor = new Color(cl.getRed(), cl.getGreen(), cl.getBlue(), curveAlpha[calindex]);
					double Adangle = curveAngle[caindex];

					drawConnectCurve(x1, y1, x2, y2, Adangle, bond, 
						lineColor,
						true
					);
				}
			}
		}
		//第三轮循环绘制方块与标签
		graphics.setStroke(new BasicStroke(2f));
		for (int deepth = 0; nodeTree.containsKey(deepth); deepth ++) {
			for (String nodeID: nodeTree.get(deepth)) {
				NodeBox nb = nodeMap.get(nodeID);
				nb.paint(graphics, true);
				String labeltext = nb.getLabel();
				double[] centerloc = nb.getCenter();
				int fontsize = (int) (nb.getSize()[0] * 0.618 + 1);
				setFont(new Font(fontName,  Font.BOLD, fontsize));
				drawSimpleLabel(labeltext, centerloc[0], centerloc[1], -90, 0, "m", "m", fontColor);
			}
		}
	}
	/**
	 * 绘制连接曲线
	 * @param x1 第一个点横坐标
	 * @param y1 第一个点纵坐标
	 * @param x2 第二个点横坐标
	 * @param y2 第二个点纵坐标
	 * @param ang 曲线调整角，单位角度
	 * @param bond 曲线粗细
	 * @param lc 曲线颜色
	 */
	private void drawConnectCurve(double x1, double y1, double x2, double y2, double ang, float bond, Color lc, boolean solid) {
		if (graphics == null) return;
		if (x2 < x1) {//保证（x1， y1）点在（x2，y2）左侧
			double tmp = x2;
			x2 = x1;
			x1 = tmp;
			tmp = y1;
			y1 = y2;
			y2 = tmp;
		}
		boolean flag = (x1 - x2)*(y1 -y2) < 0; //判断是否为异号类型，即左下-右上类型
		if (flag) {//异号类型交换纵标为同号类型画图，再通过下面图像变换为异号类型。
			double tmp = y1;
			y1 = y2;
			y2 = tmp;
		}
		double radian = Math.toRadians(ang);
		double w = (x2 - x1) / Math.cos(radian);
		double h = Math.abs(y2 - y1) / (1 - Math.sin(radian));
		double locat_x1 = x1 - w/2;//椭圆弧定位点横标
		double locat_y1 = y1;
		double locat_x2 = x2 - w/2;
		double locat_y2 = y2 - h;
		/*
		二段圆弧，注意起止点是单向循环
		*/
		Arc2D arc1 = new Arc2D.Double(locat_x1, locat_y1, w, h, 90, ang-90, Arc2D.OPEN);
		Arc2D arc2 = new Arc2D.Double(locat_x2, locat_y2, w, h, 180 + ang, 90-ang, Arc2D.OPEN);
		Path2D path = new Path2D.Double(arc1);
		path.append(arc2, true);//将arc2与arc1收尾相连
		if (flag) {
			path.transform(AffineTransform.getScaleInstance(1d,  -1d));//图像横坐标缩放1倍，纵坐标缩放-1倍，即垂直对称
			path.transform(AffineTransform.getTranslateInstance(0, y1 + y2));//对称图在画布上面，平移变换入画布
		}
		Color olc = graphics.getColor();
		graphics.setColor(lc);
		Stroke obs = graphics.getStroke();
		BasicStroke solidStroke = new BasicStroke(bond);
		BasicStroke dashStroke = new BasicStroke(bond, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9 * ratio}, 0);
		graphics.setStroke(solid?solidStroke:dashStroke);//设置粗细
		graphics.draw(path);
		graphics.setStroke(obs);
		graphics.setColor(olc);
		
	}
	/**
	 * 绘制文本标签
	 * @param label 标签内容
	 * @param locat_x 标签定位横坐标（从左上角为0）
	 * @param locat_y 标签定位纵坐标（从左上角为0）
	 * @param h_mode 水平对齐模式符，m为水平居中对齐，l为水平左对齐，r为水平右对齐
	 * @param v_mode 垂直对齐模式符，m为垂直居中对齐，u为顶端对齐，d为底端对齐
	 * @param FontColor 文本标签字体颜色
	 * @throws Exception 异常
	 */
	private void drawSimpleLabel (String label, double rotateCenter_x, double rotateCenter_y, double rotateDegree, double rotateR, String h_mode, String v_mode, Color FontColor) throws Exception {
		if (graphics == null) return;
		float baseline_x;
		float baseline_y;
		int locat_x = (int) (rotateCenter_x - rotateR), locat_y = (int) rotateCenter_y;
		if (h_mode.equals("l")) {
			locat_x = (int) (rotateCenter_x + rotateR);
		}
		switch	 (v_mode) {//垂直对齐方式
			case "m":{baseline_y =locat_y*1f - metrics.getHeight()/2f + metrics.getAscent();break;}
			case "u":{baseline_y =locat_y + metrics.getAscent();break;}
			case "d":{baseline_y =locat_y - metrics.getHeight() + metrics.getAscent();break;}
			default: throw new Exception("Ilegal mode symbol: "+v_mode);
		}
		switch (h_mode) {//水平对齐方式
			case "m":{baseline_x =locat_x*1f - metrics.stringWidth(label)/2f;break;}
			case "l":{baseline_x =locat_x;break;}
			case "r": {baseline_x =locat_x - metrics.stringWidth(label);break;}
			default: throw new Exception("Ilegal mode symbol: "+h_mode);
		}
		graphics.setColor(FontColor);//设置字体颜色
		graphics.rotate(rotateDegree*Math.PI/180, rotateCenter_x, rotateCenter_y);
		graphics.drawString(label, baseline_x, baseline_y);
		graphics.rotate(-rotateDegree*Math.PI/180, rotateCenter_x, rotateCenter_y);
	}
	public static void main(String args[]) {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new FlowChartParamPane().setVisible(true);
			}
		});
	}
}
/**
 * 定义表示流程图矩形节点类
 */
class NodeBox {
	/**
	 * 矩形宽度与圆角直径的比例关系
	 */
	private int widthToD = 6;
	/**
	 * 节点对象形状宽度
	 */
	private int width;
	/**
	 * 节点对象形状高度
	 */
	private int height;
	/**
	 * 节点对象横坐标
	 */
	private int box_x;
	/**
	 * 节点对象纵坐标
	 */
	private int box_y;
	/**
	 * 节点对象在节点树中深度
	 */
	private int deepth;
	/**
	 * 节点对象标签
	 */
	private String label = "";
	/**
	 * 节点对象ID
	 */
	private String id = null;
	/**
	 * 节点对象填充色
	 */
	private Color filledColor;
	/**
	 * 节点对象边框色
	 */
	private Color sideColor;
	/**
	 * 下游节点列表
	 */
	protected final ArrayList <NodeBox> upstreamNodes = new ArrayList<>();
	/**
	 * 上游节点列表
	 */
	protected final ArrayList <NodeBox> downstreamNodes = new ArrayList<>();
	/**
	 * 节点对象构造函数
	 * @param x 节点横坐标
	 * @param y 节点纵坐标
	 * @param width 节点宽度
	 * @param height 节点高度
	 * @param deepth 节点深度
	 * @param filledColor 节点填充色
	 * @param sideColor  节点边框色
	 */
	public NodeBox(int x, int y, int width, int height, int deepth, Color filledColor, Color sideColor) {
		box_x = x;
		box_y = y;
		this.width = width;
		this.height = height;
		this.deepth = deepth;
		this.filledColor = filledColor;
		this.sideColor = sideColor;
	}
	/**
	 * 节点对象构造函数， 构造默认宽高深的节点对象
	 * @param x 节点横坐标
	 * @param y 节点纵坐标
	 * @param filledColor 节点填充色
	 * @param sideColor  节点边框色
	 */
	public NodeBox(int x, int y, Color filledColor, Color sideColor) {
		this(x, y, 40, 30, 0, filledColor, sideColor);
	}
	/**
	 * 节点对象构造函数，构造默认坐标、尺寸的节点对象
	 * @param deepth 节点深度
	 * @param filledColor 节点填充色
	 * @param sideColor 节点背景色
	 */
	public NodeBox(int deepth, Color filledColor, Color sideColor) {
		this(0, 0, 40, 30, deepth, filledColor, sideColor);
	}
	/**
	 * 节点对象构造函数，构造默认定位点坐标、节点尺寸、颜色的节点对象
	 * @param deepth 节点深度
	 */
	public NodeBox(int deepth) {
		this(0, 0, 40, 30, deepth, Color.BLUE, Color.BLUE);
	}
	/**
	 * 设置当前节点形状的宽度与高度
	 * @param width 宽度
	 * @param height 高度
	 */
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	/**
	 * 获取当前节点形状的宽度与高度
	 * @return 宽度、高度按次序组成的整型数组
	 */
	public int[] getSize() {
		return new int[] {width, height};
	}
	/**
	 * 设置当前节点的定位点坐标
	 * @param x 横坐标
	 * @param y 纵坐标
	 */
	public void setLocation (int x, int y) {
		box_x = x;
		box_y = y;
	}
	/**
	 * 获取当前节点的定位点坐标
	 * @return 横纵坐标组成的整型数组
	 */
	public int [] getLocation() {
		return new int[] {box_x, box_y};
	}
	/**
	 * 设置当前节点在节点树中的深度
	 * @param deepth 深度
	 */
	public void setDeepth(int deepth) {
		this.deepth = deepth;
	}
	/**
	 * 获取当前节点在节点树中的深度
	 * @return 代表深度的整型值
	 */
	public int getDeepth() {
		return deepth;
	}
	/**
	 * 设置当前节点的填充色与边框色
	 * @param filledColor 填充色
	 * @param sideColor 边框色
	 */
	public void setColors (Color filledColor, Color sideColor) {
		this.filledColor = filledColor;
		this.sideColor = sideColor;
	}
	/**
	 * 获取当前节点的填充色与边框色
	 * @return  填充色、边框色的Color对象数组
	 */
	public Color[] getColors () {
		return new Color[] {filledColor, sideColor};
	}
	/**
	 * 添加当前节点的上游节点
	 * @param nodebox 上游节点对象
	 */
	public void addUpstreamNode(NodeBox nodebox) {
		if (upstreamNodes.contains(nodebox)) return;
		upstreamNodes.add(nodebox);
	}
	/**
	 * 添加当前节点的下游节点
	 * @param nodeBox 下游节点对象
	 */
	public void addDownstreamNode(NodeBox nodeBox) {
		if (downstreamNodes.contains(nodeBox)) return;
		downstreamNodes.add(nodeBox);
	}
	/**
	 * 清空当前节点的上游节点列表
	 */
	public void clearUpstreamNode() {
		upstreamNodes.clear();
	}
	/**
	 * 移除当前节点上游节点列表中指定节点
	 * @param nodeBox 指定的节点
	 * @return 移除成功与否
	 */
	public boolean removeUpstreamNodeItem(NodeBox nodeBox) {
		return upstreamNodes.remove(nodeBox);
	}
	/**
	 * 清空当前节点的下游节点列表
	 */
	public void clearDownstreamNode() {
		downstreamNodes.clear();
	}
	/**
	 * 移除当前节点下游节点列表中指定节点
	 * @param nodeBox 指定的节点
	 * @return 移除成功与否
	 */
	public boolean removeDownstreamNodeItem(NodeBox nodeBox) {
		return downstreamNodes.remove(nodeBox);
	}
	/**
	 * 设置节点标签
	 * @param label 节点标签
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * 获取当前节点标签
	 * @return 节点标签值
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * 设置当前节点ID
	 * @param id 节点ID
	 */
	public void setID(String id) {
		this.id = id;
	}
	/**
	 * 获取当前节点ID
	 * @return 节点ID
	 */
	public String getID() {
		return id;
	}
	/**
	 * 获取当前节点中心定位
	 * @return 节点中心定位数组，第一个为横标，第二个为纵标
	 */
	public double[] getCenter() {
		return new double[] {box_x + width/2, box_y + height/2};
	}
	/**
	 * 绘制当前节点图形到指定Graphics2D对象。
	 * @param graphics 待绘制的Graphics2D对象
	 * @param filled 填充还是描边，true为填充
	 */
	public void paint(Graphics2D graphics, boolean filled) {
		Color olc = graphics.getColor();
		if (filled) {
			graphics.setColor(filledColor);
			if (filledColor.getAlpha() == 0) return;
			graphics.fillRoundRect(box_x, box_y, width, height, width/widthToD, width/widthToD);
		} else {
			graphics.setColor(sideColor);
			if (sideColor.getAlpha() == 0) return;
			graphics.drawRoundRect(box_x, box_y, width, height, width/widthToD, width/widthToD);
		}
		graphics.setColor(olc);
	}

}
/**
 * 配置面板
 * @version 1.0
 * @author Zhang Hongning
 */
class FlowChartParamPane extends JFrame {
	private static final long serialVersionUID = 202011121803L;
	/**
	 * 构造方法
	 */
	public FlowChartParamPane() {
		initComponents();
	}
	/**
	 * 组件构造方法
	 */
	private final void initComponents() {
		figWidthLabel = new JLabel("width");
		figHeightLabel = new JLabel("height");
		figRatioLabel = new JLabel("raito");
		figWidthText = new JTextField("1800");
		figHeightText = new JTextField("1200");
		figRatioText = new JTextField("1");
		cureAngleLabel = new JLabel("curve ange");
		curveAlphaLabel = new JLabel("curve alpha");
		curveBondLabel = new JLabel("curve bond");
		curveAngleText = new JTextField("45");
		curveAlphaText = new JTextField("120");
		curveBondText = new JTextField("5");
		//jSeparator1 = new JSeparator();
		LayerParamScrolPane = new JScrollPane();
		LayerParamTable = new JTable();
		jSeparator2 = new JSeparator();
		LayerParamLabel = new JLabel("box size per layer");
		OKButton = new JButton("OK");
		CancelButton = new JButton("Cancel");
		pdfCheckBox = new JCheckBox("pdf");
		epsCheckBox = new JCheckBox("eps");
		pngCheckBox = new JCheckBox("png");
		jpgCheckBox = new JCheckBox("jpg");
		TypeLabel = new JLabel("Type");
		dpiLabel = new JLabel("DPI");
		dpiText = new JTextField("72");
		yesRadioButton = new JRadioButton("yes");
		noRadioButton = new JRadioButton("no");
		titleLabel = new JLabel("contain title");
		jSeparator4 = new JSeparator();
	
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		figWidthText.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				//figWidthTextActionPerformed(evt);
			}
		});
		
		LayerParamTable.setModel(new DefaultTableModel(
			new Object [][] {
				{null, null, null},
				{null, null, null},
				{null, null, null},
				{null, null, null}
			},
			new String [] {
				"width", "height", "distance"
			}
		));
		LayerParamScrolPane.setViewportView(LayerParamTable);
		OKButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//OKButtonActionPerformed(evt);
			}
		});
		CancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//CancelButtonActionPerformed(evt);
			}
		});
		
		yesRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//yesRadioButtonActionPerformed(evt);
			}
		});

		noRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//noRadioButtonActionPerformed(evt);
			}
		});
		//ButtonG
		jSeparator4.setOrientation(SwingConstants.VERTICAL);
	
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addGroup(layout.createSequentialGroup()
				.addGap(10)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addComponent(figWidthLabel).addComponent(figWidthText, 70, 70, 70))
					.addGroup(layout.createSequentialGroup().addComponent(figHeightLabel).addComponent(figHeightText, 70, 70, 70))
					.addGroup(layout.createSequentialGroup().addComponent(figRatioLabel).addComponent(figRatioText, 70, 70, 70))
				)
				.addGap(20)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addComponent(cureAngleLabel).addComponent(curveAngleText, 70, 70, 70))
					.addGroup(layout.createSequentialGroup().addComponent(curveAlphaLabel).addComponent(curveAlphaText, 70, 70, 70))
					.addGroup(layout.createSequentialGroup().addComponent(curveBondLabel).addComponent(curveBondText, 70, 70, 70))
				)
				.addGap(10)
			)
			.addComponent(jSeparator2, 300, 300, 300)
			
			.addComponent(LayerParamLabel)
			.addComponent(LayerParamScrolPane, 300, 300, 300)
			
			.addGroup(layout.createSequentialGroup()
				.addGap(10)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(TypeLabel)
					.addGroup(layout.createSequentialGroup().addComponent(pdfCheckBox).addGap(10).addComponent(epsCheckBox))
					.addGroup(layout.createSequentialGroup().addComponent(pngCheckBox).addGap(10).addComponent(jpgCheckBox))
				)
				.addComponent(jSeparator4, 2, 2, 2)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup().addComponent(dpiLabel).addComponent(dpiText, 70, 70 , 70))
					.addComponent(titleLabel)
					.addGroup(layout.createSequentialGroup().addComponent(yesRadioButton).addComponent(noRadioButton))
				)
				.addGap(10)
			)
			
			.addGroup(layout.createSequentialGroup().addComponent(OKButton, 50, 50, 50).addGap(20).addComponent(CancelButton, 50, 50 ,50))
		);

		layout.setVerticalGroup(
			layout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(layout.createSequentialGroup()
				.addGap(20, 20, 20)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(figWidthLabel)
					.addComponent(figWidthText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(cureAngleLabel)
					.addComponent(curveAngleText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(figHeightLabel)
					.addComponent(figHeightText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(curveAlphaLabel)
					.addComponent(curveAlphaText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(18, 18, 18)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(figRatioLabel)
					.addComponent(figRatioText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(curveBondLabel)
					.addComponent(curveBondText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(18, 18, 18)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							//.addComponent(jSeparator1, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
							.addComponent(jSeparator2, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))
						.addGap(18, 18, 18))
					.addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
						.addComponent(LayerParamLabel)
						.addGap(9, 9, 9)))
				.addComponent(LayerParamScrolPane, GroupLayout.PREFERRED_SIZE, 89, GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
							.addGroup(layout.createSequentialGroup()
								.addGap(18, 18, 18)
								.addComponent(TypeLabel)
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(pdfCheckBox)
									.addComponent(jpgCheckBox))
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(epsCheckBox)
									.addComponent(pngCheckBox)))
							.addGroup(layout.createSequentialGroup()
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(dpiLabel)
									.addComponent(dpiText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
								.addComponent(titleLabel)
								.addGap(18, 18, 18)
								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
									.addComponent(yesRadioButton)
									.addComponent(noRadioButton))))
						.addGap(34, 34, 34)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(OKButton)
							.addComponent(CancelButton)))
					.addGroup(layout.createSequentialGroup()
						.addGap(18, 18, 18)
						.addComponent(jSeparator4, GroupLayout.PREFERRED_SIZE, 105, GroupLayout.PREFERRED_SIZE)))
				.addContainerGap(62, Short.MAX_VALUE))
		);
	
		pack();
	}
	private JButton CancelButton;
	private JLabel LayerParamLabel;
	private JScrollPane LayerParamScrolPane;
	private JTable LayerParamTable;
	private JButton OKButton;
	private JLabel TypeLabel;
	private JLabel cureAngleLabel;
	private JLabel curveAlphaLabel;
	private JTextField curveAlphaText;
	private JTextField curveAngleText;
	private JLabel curveBondLabel;
	private JTextField curveBondText;
	private JLabel dpiLabel;
	private JTextField dpiText;
	private JCheckBox epsCheckBox;
	private JLabel figHeightLabel;
	private JTextField figHeightText;
	private JLabel figRatioLabel;
	private JLabel figWidthLabel;
	private JTextField figWidthText;
	private JSeparator jSeparator2;
	private JSeparator jSeparator4;
	private JCheckBox jpgCheckBox;
	private JRadioButton noRadioButton;
	private JCheckBox pdfCheckBox;
	private JCheckBox pngCheckBox;
	private JTextField figRatioText;
	private JLabel titleLabel;
	private JRadioButton yesRadioButton;
	}