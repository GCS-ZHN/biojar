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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.File;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;

import biojar.function.GeneralMethod;
/**
 * 绘制药物靶标临床状态热点图的业务实现类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class TargetHeatMap {
	/**
	 * 2D绘图对象，用于绘制图形
	 */
	private Graphics2D graphics2D = null;
	/**
	 * 字体指标，用于对字体绘图进行定位
	 */
	private FontMetrics metrics;
	/**
	 * 颜色对象数组，用于对不同phase的上色
	 */
	private final Color[] colorReflect = {
		new Color(198,218,241),//蓝色 phase 1
		new Color(167,212,140),//绿色 phase 2
		new Color(243,236,24),//黄色 phase 3
		new Color(255,195,0),//橙色 phase 4
		new Color(244,245,246),//淡蓝灰色，背景色
		new Color(254,248,233)//淡粉红色，背景色
	};
	/**
	 * 是否绘制图例
	 */
	public boolean paintLegand = true;
	public boolean paintTick = true;
	public boolean paintDrugName = true;
	/**
	 * 储存不同药物类型图标
	 */
	private final HashMap<String, BufferedImage> drugIconMap = new HashMap<>();
	public TargetHeatMap() throws IOException {
		drugIconMap.put("Small molecule", ImageIO.read(new File("configure/drugicon/SMD.png")));
		drugIconMap.put("Antibody", ImageIO.read(new File("configure/drugicon/AD.png")));
		drugIconMap.put("Protein drug", ImageIO.read(new File("configure/drugicon/PD.png")));
		drugIconMap.put("Antisense", ImageIO.read(new File("configure/drugicon/ATD.png")));
		drugIconMap.put("Others", ImageIO.read(new File("configure/drugicon/OD.png")));
		drugIconMap.put("Unknown", ImageIO.read(new File("configure/drugicon/OD.png")));
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
	private void drawLabel (String label, int locat_x, int locat_y, String h_mode, String v_mode, Color FontColor) throws Exception {
		float baseline_x;
		float baseline_y;
		switch	 (v_mode) {
			case "m":{baseline_y =locat_y*1f - metrics.getHeight()/2f + metrics.getAscent();break;}
			case "u":{baseline_y =locat_y + metrics.getAscent();break;}
			case "d":{baseline_y =locat_y - metrics.getHeight() + metrics.getAscent();break;}
			default: throw new Exception("Ilegal mode symbol: "+v_mode);
		}
		switch (h_mode) {
			case "m":{baseline_x =locat_x*1f - metrics.stringWidth(label)/2f;break;}
			case "l":{baseline_x =locat_x;break;}
			case "r": {baseline_x =locat_x - metrics.stringWidth(label);break;}
			default: throw new Exception("Ilegal mode symbol: "+h_mode);
		}
		graphics2D.setColor(FontColor);//设置字体颜色
		graphics2D.drawString(label, baseline_x, baseline_y);
		//graphics2D.drawS
	}
	/**
	 * 绘制靶点复合方块
	 * @param label 方块标签文本
	 * @param phase1 方块所表示的临床状态1，如p1/p2中的p1
	 * @param phase2 方块所表示的临床状态2，如p1/p2中的p2
	 * @param center_x 方块中心横坐标（从左上角为0）
	 * @param center_y 方块中心纵坐标（从左上角为0）
	 * @param cellSize 方块尺寸
	 * @throws Exception 异常
	 */
	private void drawCombineCell (String label, int phase1, int phase2, int center_x, int center_y ,int cellSize) throws Exception {
		drawCell(phase1, center_x - cellSize/4, center_y, cellSize/2, cellSize);
		drawCell(phase2, center_x + cellSize/4, center_y, cellSize/2, cellSize);
		drawLabel(label, center_x, center_y, "m", "m", Color.BLACK);
	}
	/**
	 * 绘制简单靶点矩形
	 * @param phase 矩形所表示的临床状态
	 * @param center_x 矩形中心横坐标（从左上角为0）
	 * @param center_y 矩形中心纵坐标（从左上角为0）
	 * @param width 矩形宽度
	 * @param height 矩形高度
	 */
	private void drawCell (int phase, int center_x, int center_y, int width, int height) {
		graphics2D.setColor(colorReflect[phase - 1]);
		graphics2D.fillRect(center_x - width/2, center_y-height/2, width, height);
	}
	/**
	 * 绘制坐标轴短线
	 * @param center_x 短线中心横坐标
	 * @param center_y 短线中心纵坐标
	 * @param length 短线长度
	 * @param LColor 短线颜色
	 * @param mode 短线模式，h为水平线，v为垂直线
	 * @throws Exception 异常
	 */
	private void drawShortLine (int center_x, int center_y, int length, Color LColor, String mode, int ratio) throws Exception {
		graphics2D.setColor(LColor);
		if (length%(2*ratio)!=0) length++;
		switch(mode) {
			case "h":{graphics2D.fillRect(center_x - length/2, center_y-length/(2*ratio), length, length/ratio);break;}
			case "v":{graphics2D.fillRect(center_x - length/(2*ratio), center_y-length/2, length/ratio, length);break;}
			default: throw new Exception("Ilegal mode symbol: "+mode);
		}
		
	}
	/**
	 * 绘制批准药物药丸图标
	 * @param img 药丸BufferedImage对象
	 * @param center_x 药丸中心横坐标
	 * @param center_y 药丸中心纵坐标
	 * @param width 图像显示宽度
	 * @param height 图像显示高度
	 */
	private void drawIcon(BufferedImage img, int center_x, int center_y, int width, int height) {
		int base_x = center_x - width/2;
		int base_y = center_y - height/2;
		graphics2D.drawImage(img, base_x, base_y, width, height, null);
	}
	/**
	 * 绘制折叠双斜线
	 * @param center_x 双斜线中心横坐标
	 * @param center_y 双斜线中心纵坐标
	 * @param length 双斜线单线长度
	 * @param LColor 双斜线颜色
	 * @param rotateDegree 双斜线倾斜角度
	 * @throws Exception 异常
	 */
	private void drawDoubleSlash(int center_x, int center_y, int length, Color LColor, double rotateDegree) throws Exception {
		int ratio = 5;
		while (length%(2*ratio)!=0) {
			length++;
		}
		graphics2D.rotate( Math.toRadians(rotateDegree), center_x*1.000, center_y*1.000);
		
		drawShortLine(center_x-length/ratio, center_y, length, LColor, "v", ratio);
		drawShortLine(center_x+length/ratio, center_y, length, LColor, "v", ratio);
		graphics2D.rotate(-Math.toRadians(rotateDegree), center_x*1.000, center_y*1.000);
	}
	/**
	 * 绘制靶点热图
	 * @param inputfile 输入文件，第一列为靶点名称，第二列为年份，第三第四列为临床状态，第五列为label, 第六列为疾病
	 * @param outputNameFormat 输出文件名格式化字符串
	 * @param type 输出文件类型
	 * @param beginYear 起始年份
	 * @param endYear 结束年份
	 * @param foldedYears 折叠时间区间
	 * @param dpi 图像DPI
	 * @param hasTitle 是否包含标题行
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public void drawTargetFigure(String inputfile, String outputNameFormat, String type, int beginYear, int endYear, int[][] foldedYears, int dpi, boolean hasTitle) throws FileNotFoundException, IOException, Exception {
		/*
		load Data
		*/
		HashMap <String,  Object[]> phase = new HashMap<>();
		ArrayList <String> targetNames = new ArrayList<>();
		ArrayList<String> diseases = new ArrayList<>();
		HashMap <String, String[]> targetApprovalDrug = new HashMap<>();
		try (LineNumberReader lnr = GeneralMethod.BufferRead(inputfile)) {
			String line;
			while ((line = lnr.readLine()) != null) {
				if (hasTitle && lnr.getLineNumber()==1) continue;
				String[] arr = line.split(getDefaultDelimiter());
				String target = arr[0];
				int year = Integer.parseInt(arr[1]);
				int phase1 = Integer.parseInt(arr[2]);
				int phase2 = Integer.parseInt(arr[3]);
				String label = arr[4];
				Object[] tmp = {phase1, phase2, label};
				String[] approvalDrug = {arr[6], arr[7], arr[8]};
				if (!diseases.contains(arr[8])) diseases.add(arr[8]);
				targetApprovalDrug.putIfAbsent(target, approvalDrug);
				if (!targetNames.contains(target)) targetNames.add(target);
				phase.put(target+year, tmp);
			}
		}
		/*
		设置绘图基本参数
		*/
		int cellSize = 100;
		int rightYearEx = 6;//图右边留白年份长度，为文字当背景
		Color bgColor = Color.WHITE;//背景色
		Color fontColor = Color.BLACK;//字体颜色
		String fontName = "Times New Roman";//"";//字体名称Calibri
		int fontSize = (int) (cellSize*0.618);
		Font font = new Font(fontName, Font.PLAIN, fontSize);
		String longlestTargetName = "";//最长y轴标签名，用于自定义标签区域宽度
		for (String name: targetNames) {
			if (ImageHelp.getStringFontWidth(font, name) > ImageHelp.getStringFontWidth(font, longlestTargetName))
				longlestTargetName = name;
		}
		int step = (int) (cellSize*1.3);//方块坐标增长步长
		ArrayList <Integer> foldedYearsList = new ArrayList<>();//被折叠的年份，用于年份是否在折叠区间的判断
		for (int[] yPair: foldedYears) {
			for (int i = yPair[0]; i <= yPair[1]; i++) {
				if (i < beginYear|| i > endYear) {
					throw new Exception("Folded years should between begin year and end year!");
				}
				foldedYearsList.add(i);
			}
		}
		int foldedYearsLength = foldedYearsList.size() - foldedYears.length;//被折叠年份导致缩短的X轴长度
		int MarginLeft = 12 * cellSize, MarginRight = 12* cellSize;//左右页边距
		int MarginTop = 12 * cellSize, MarginDown = 16*cellSize;//上下页边距
		int yLabelWidth = ImageHelp.getStringFontWidth(font, longlestTargetName);//y轴标签区间宽度
		int xLabelHeight = 4* cellSize;//x轴标签区间高度
		int width = MarginLeft +yLabelWidth+ step*(endYear - beginYear + rightYearEx - foldedYearsLength) + cellSize  + MarginRight;
		int height =MarginTop + step*(targetNames.size() - 1) + cellSize + xLabelHeight + + MarginDown;
		if (!type.equals("jpg")) {//对矢量图eps/pdf，固定总尺寸
			int newHeight = 4000;
			cellSize = cellSize*newHeight/height;
			while(cellSize%4 != 0) cellSize ++;//保证其四分之一为整数
			height = newHeight;
			fontSize = (int) (cellSize*1.1);//0.618
			font = new Font(fontName, Font.PLAIN, fontSize);
			yLabelWidth = ImageHelp.getStringFontWidth(font, longlestTargetName);
			xLabelHeight = 4* cellSize;
			step = (int) (cellSize*1.1);//1.3
			MarginLeft =12* cellSize;
			MarginRight =12* cellSize;
			MarginTop = 12 * cellSize;
			MarginDown = 16*cellSize;
			width = MarginLeft +yLabelWidth+ step*(endYear - beginYear + rightYearEx - foldedYearsLength) + cellSize  + MarginRight;
		}

		if (width%2 != 0) width++;
		if (height%2 != 0) height++;
		String outputFileName = String.format(outputNameFormat, beginYear, endYear, type);
		CreateGraphics cg = new CreateGraphics(width, height, type, outputFileName);
		graphics2D = cg.getGraphics2D();
		font = new Font(fontName, Font.PLAIN, fontSize);

		graphics2D.setFont(font);
		metrics = graphics2D.getFontMetrics();

		
		/*
		设置图片背景色为白色
		 */
		graphics2D.setBackground(bgColor);
		graphics2D.clearRect(0, 0, width, height);
		/*
		开始绘制核心内容
		*/
		int baseX = MarginLeft + yLabelWidth;//绘图起始坐标横标
		int baseY = MarginTop;//绘图起始坐标纵标
		int center_y = baseY;//当前绘图坐标纵标
		int length = cellSize/5;//短横线长度
		int dis = length/2;//短横线与label距离，由于水平垂直存在空隙差异，故视觉上不一样
		int[] approvalYear = new int[targetNames.size()];//批准年份的数组，用于加批准药物药丸
		HashMap <Integer, Integer> yearToIndex = new HashMap<>();//年份与真实水平定位的匹配
		ArrayList <Integer> foldedIndex = new ArrayList<>();//被折叠处定位，用于加双斜线
		for (int targetIndex =0; targetIndex<targetNames.size();targetIndex++) {
			center_y = baseY + targetIndex*step;//当前绘图坐标纵标
			String target = targetNames.get(targetIndex);
			String[] approvalDrug = targetApprovalDrug.get(target);
			int yearIndex = 0;//年份对应实际绘图横向排位
			boolean lastJumped = false;//是否刚刚跳过折叠区域
			for (int year = beginYear; year <= endYear + rightYearEx; year++) {
				if (foldedYearsList.contains(year)) {
					lastJumped = true;
					continue;//for循环还是会year++
				}
				int center_x = baseX +yearIndex*step;//当前绘图坐标横标
				if (lastJumped) {//若刚刚跳过折叠年份，则当前位置用来绘制折叠标记，年份值+1应当用-1抵消
					year --; 
					foldedIndex.add(yearIndex);
				} else {
					yearToIndex.put(year, yearIndex);
				}
				/*
				定义填充的phase与label，当刚刚跳过折叠年份区间或当前年份无记录时，用背景色填充，无label
				*/
				int v = diseases.indexOf(approvalDrug[2])%2;//根据疾病奇偶顺序交替背景色
				int phase1 = (!lastJumped)&&phase.containsKey(target+year)?(int) phase.get(target+year)[0]:5 + v;
				int phase2 = (!lastJumped)&&phase.containsKey(target+year)?(int) phase.get(target+year)[1]:5 + v;
				String label = (!lastJumped)&&phase.containsKey(target+year)?(String) phase.get(target+year)[2]:"";
				if (year > endYear) {//在扩展区间部分不绘制，只充当背景
					phase1 = 5 + v;
					phase2 = 5 + v;
					label = "";
				}
				if (phase1 == 4) {//记录批准药物时间，用于下文绘制药丸
					approvalYear[targetIndex] = year;
					/*
					当phase 4药物类型在规定类型范围内时，方块用背景色，无label，下文用图标代替方块
					*/
					if (drugIconMap.containsKey(approvalDrug[1])) {
						phase1 = 5 + v;
						phase2 = 5 + v;
						label = "";
					}
				}
				drawCombineCell(label, phase1, phase2, center_x, center_y, cellSize);
				yearIndex++;
				lastJumped = false;
			}
			/*
			加纵坐标
			*/
			if (paintTick) {
				drawLabel(target, baseX - cellSize/2 - length-dis, center_y, "r", "m", fontColor);
				drawShortLine(baseX - (cellSize + length)/2, center_y, length, fontColor, "h", 2);
			}
		}
		phase = null;//销毁无用对象
		/*
		 绘制phase 4时药丸显示
		 */
		TreeSet <String> appearDrugType = new TreeSet<>();//图中出现的药物类型，用于图例绘制，用TreeSet保证图例每次生成顺序一致
		for (int targetIndex=0; targetIndex < targetNames.size(); targetIndex++) {
			center_y = baseY + targetIndex*step;
			if (approvalYear[targetIndex] == 0l) continue;//若年份为0代表在选定绘图区段内无phase 4，予以跳过
			int center_x = baseX +yearToIndex.get(approvalYear[targetIndex])*step;
			String[] approvalDrug = targetApprovalDrug.get(targetNames.get(targetIndex));
			if (drugIconMap.containsKey(approvalDrug[1])) {
				appearDrugType.add(approvalDrug[1]);
				BufferedImage bi = drugIconMap.get(approvalDrug[1]);
				int drugwidth = cellSize;
				int drugheight = bi.getHeight()*drugwidth/bi.getWidth();
				while (drugwidth%2!=0||drugheight%2!=0) {
					drugwidth ++;
					drugheight = bi.getHeight()*drugwidth/bi.getWidth();
				}
				drawIcon(bi, center_x, center_y, drugwidth, drugheight);
				if (paintDrugName) drawLabel(approvalDrug[0], center_x + step- cellSize/2, center_y, "l", "m", fontColor);
			}
		}
		approvalYear = null;//销毁无用对象
		targetNames = null;//销毁无用对象
		/*
		加横坐标
		*/
		if (paintTick) {
			for (int year = beginYear; year <= endYear + rightYearEx; year+=2) {
				if (foldedYearsList.contains(year)) continue;//跳过折叠年份
				int center_x  = baseX +yearToIndex.get(year)*step;
				drawShortLine(center_x, center_y + (cellSize + length)/2, length, fontColor, "v", 2);
				drawLabel(""+year, center_x, center_y + cellSize/2 + length + dis,"m", "u", fontColor);
			}
		}
		yearToIndex = null;//销毁无用对象
		foldedYearsList = null;//销毁无用对象
		/*
		加双斜线折叠符
		*/
		for (int yearIndex: foldedIndex) {
			int center_x = baseX + yearIndex*step;
			drawDoubleSlash(center_x, center_y + cellSize/2, 2*cellSize/3, fontColor, 30);
		}
		foldedIndex = null;//销毁无用对象
		/*
		加横向图例
		*/
		if (paintLegand) {
			center_y += 6 * cellSize;//图例与x轴间距定义
			int noteSize = 2 * cellSize;//图标宽度
			int noteTotalWidth = 0;//图例总宽度
			int extWidth = 9 * noteSize/5;//图标扩展宽度
			int figLabelDis = 7*noteSize/10;//图标定位点与label定位点距离
			font = new Font(fontName, Font.BOLD, (int) (0.618*noteSize));
			graphics2D.setFont(font);
			metrics = graphics2D.getFontMetrics();
			for (int i = 0; i <3; i++) {
				noteTotalWidth += extWidth + metrics.stringWidth("Phase " + (i + 1));
			}
			for (String drugType: appearDrugType) {
				noteTotalWidth += extWidth + metrics.stringWidth(drugType);
			}
			int center_x = width/2 - noteTotalWidth/2 + cellSize/2;//图例起始横坐标
			for (int i = 0; i < 3; i++) {//绘制方块图例
				drawCombineCell("n", i + 1, i + 1, center_x, center_y, noteSize);
				drawLabel("Phase " + (i + 1), (int) (center_x + figLabelDis), center_y, "l", "m", fontColor);
				center_x +=extWidth + metrics.stringWidth("Phase " + (i + 1));
			}
			for (String drugType: appearDrugType) {//绘制药丸图例
				BufferedImage img = drugIconMap.get(drugType);
				int drugwidth = noteSize;
				int drugheight = img.getHeight()*drugwidth/img.getWidth();
				while (drugwidth % 2 != 0||drugheight % 2 != 0) {
					drugwidth ++;
					drugheight = img.getHeight()*drugwidth/img.getWidth();
				}
				drawIcon(img, center_x, center_y, drugwidth, drugheight);
				drawLabel(drugType, (int) (center_x + figLabelDis), center_y, "l", "m", fontColor);
				center_x +=extWidth + metrics.stringWidth(drugType);
			}	
		}
		/*
		输出图片
		*/
		cg.setJpegDPI(dpi);
		cg.saveToFlie();
	}
	/**
	 * 将timeline格式转化为绘图所需输入格式
	 * @param inputfile timeline输入文件，要求前四列依次分别为靶点名称、靶点类型，首次获批药物，药物类型
	 * ，后面为具体年份的timeline。要求第一行为标题，且年份标题为YYYY格式的纯数字
	 * @param outputfile 输出结果
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 */
	public static void convertTimelineToInput(String inputfile, String outputfile) throws FileNotFoundException, IOException {
		try (LineNumberReader lnr = GeneralMethod.BufferRead(inputfile)) {
			String line;
			String[] years = null;
			Pattern p1 = Pattern.compile("^P(\\d) \\((\\d+)");
			Pattern p2 = Pattern.compile("^P(\\d)\\/P(\\d) \\((\\d+)");
			Pattern p3 = Pattern.compile("^\\?P(\\d)");
			File parentdir = new File(outputfile).getParentFile();
			if (parentdir== null || !parentdir.exists()) {
				throw new FileNotFoundException("输出目录不存在");
			}
			try (PrintWriter pw = new PrintWriter(outputfile)) {
				pw.println("Target Name	Year	Phase1	Phase2	Num	Label	First Approval Drug	Drug Type	Disease");
				while ((line = lnr.readLine()) != null) {
					String[] arr = line.split(getDefaultDelimiter());
					if (lnr.getLineNumber() == 1) {//记录下年份标签
						years = new String[arr.length - 5];
						for (int i = 5; i < arr.length; i++) years[i-5] = arr[i];
					} else {
						String target = arr[0], label = arr[1], drug = arr[2], drugtype = arr[3], dis=arr[4];
						for (int i = 5; i < arr.length; i++) {
							String current = years[i-5];
							String info = arr[i];
							String phase1 = null, phase2 = null, num = null;
							Matcher m1 = p1.matcher(info);
							Matcher m2 = p2.matcher(info);
							Matcher m3 = p3.matcher(info);
							if (m1.find()) {
								phase1 = m1.group(1);
								phase2 = phase1;
								num = m1.group(2);
							} else if (m2.find()) {
								phase1 = m2.group(1);
								phase2 = m2.group(2);
								num = m2.group(3);
							} else if (m3.find()) {
								phase1 = m3.group(1);
								phase2 = phase1;
								num = "?";
							}
							if (phase1 != null && phase2 != null && num != null) {
								pw.println(target+"\t"+current+"\t"+phase1+"\t"+phase2+"\t"+num+"\t"+label+"\t"+drug+"\t"+drugtype+"\t"+dis);
							} 
						}
					}
				}
			}
		}
	}
	/**
	 * 设置绘图参数的可视化窗口内部类
	 */
	public static class ParamConfigFrame extends JFrame {
		/**
		 * 
		 */
		private static final long serialVersionUID = 202012012033L;
		/**
		 * 构造方法
		 * @param flagobj 线程交互的共享对象，用于多线程
		 */
		public ParamConfigFrame(Object flagobj) {
			flag = flagobj;
			initComponents();
		}
		/**
		 * 返回指定年份区间的数组
		 * @param start 开始年份
		 * @param end 结束年份
		 * @return Integer类包装的整数组
		 */
		private Integer[] getYearRangeArray(int start, int end) {
			Integer[] res = new Integer[end - start + 1];
			for (int year = start; year <= end; year++) res[year - start] = year;
			return res;
		}
		/**
		 * 初始化组件
		 */
		private void initComponents() {
			/*
			配置默认UI
			*/
			try {
				for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			}catch(Exception e) {
				System.out.println(e);
			}
			setTitle("Parameter Configure");
			setUndecorated(true);
			/*
			初始化参数
			*/
			Ok_Button = new JButton("OK");
			Cancel_Button = new JButton("Cancel");
			Start_Label = new JLabel("Start");
			End_Label = new JLabel("End");
			Start_Cbox = new JComboBox<>();
			End_Cbox = new JComboBox<>();
			Array1_Label = new JLabel("→");
			SetDPILabel = new JLabel("JPG DPI: 360");
			HiddenRangePane = new JPanel();
			HiddenRangeScrollPane = new JScrollPane();
			hiddenBoxSet = new ArrayList<>();
			AddHiddenButton = new JButton();
			HidenRangeLabel = new JLabel("Hidden Range");
			PDFOutCBox = new JCheckBox("pdf");
			EPSOutCBox = new JCheckBox("eps");
			JPGOoutCBox = new JCheckBox("jpg");
			JCheckBox[] outputTypeBoxs = {PDFOutCBox, EPSOutCBox, JPGOoutCBox};
			OutputFormatLabel = new JLabel("Output File Format");
			DPISilder = new JSlider();
			RemoveTitleLabel = new JLabel("Remove Title");
			RmTitleYesButton = new JRadioButton("Yes", true);
			RmTitleNoButton = new JRadioButton("No", false);
			JRadioButton[] rmTitleButtons = {RmTitleYesButton, RmTitleNoButton};
			/*
			设置OK按钮
			*/
			Ok_Button.setMaximumSize(new Dimension(67, 30));
			Ok_Button.setMinimumSize(new Dimension(67, 30));
			Ok_Button.setPreferredSize(new Dimension(67, 30));
			Ok_Button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					drawRangeYears[0] = (int)Start_Cbox.getSelectedItem();
					drawRangeYears[1] = (int)End_Cbox.getSelectedItem();
					dpi = DPISilder.getValue();
					rmTitle = RmTitleYesButton.isSelected();
					for (JCheckBox jcb: outputTypeBoxs) {
						if (jcb.isSelected()) outputtype.add(jcb.getText());
					}
					hiddenYears = new int[hiddenBoxSet.size()][2];
					for (int i = 0; i < hiddenBoxSet.size(); i++) {
						for (int j =0; j<2; j++) {
							hiddenYears[i][j] = (int) hiddenBoxSet.get(i)[j].getSelectedItem();
						}
					}
					
					ParamConfigFrame.this.dispose();
					synchronized(flag) {flag.notifyAll();}//必须封装使当前线程获得对象锁才能调用，否则抛出java.lang.IllegalMonitorStateException
				}
			});
			/*
			设置取消按钮
			*/
			Cancel_Button.setMaximumSize(new Dimension(67, 30));
			Cancel_Button.setMinimumSize(new Dimension(67, 30));
			Cancel_Button.setPreferredSize(new Dimension(67, 30));
			Cancel_Button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent evt) {
					ParamConfigFrame.this.dispose();
					outputtype = null;
					synchronized(flag) {flag.notifyAll();}
				}
			});
			/*
			设置DPI取值滑动杆
			*/
			DPISilder.setMinimum(1);
			DPISilder.setMaximum(1000);
			DPISilder.setValue(360);
			DPISilder.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					SetDPILabel.setText("JPG DPI: " + DPISilder.getValue());
				}
			});
			/*
			设置是否移除标题行选项为二选一
			*/
			for (int i = 0; i < rmTitleButtons.length; i++) {
				final int index = i;
				rmTitleButtons[index].addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (rmTitleButtons[index].isSelected()) rmTitleButtons[1- index].setSelected(false);
						else rmTitleButtons[1- index].setSelected(false);
					}
				});
			}
			/*
			设置绘图起始时间
			*/
			Start_Cbox.setModel(new DefaultComboBoxModel<>(getYearRangeArray(1900, 2100)));
			Start_Cbox.setEditable(false);
			Start_Cbox.setSelectedItem("1900");
			Start_Cbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int startCurrent = (int) Start_Cbox.getSelectedItem();
					Object boxSelected = End_Cbox.getSelectedItem();
					End_Cbox.setModel(new DefaultComboBoxModel<>(getYearRangeArray(startCurrent, 2100)));
					End_Cbox.setSelectedItem(boxSelected);
				}
			});
			/*
			设置绘图结束时间
			*/
			End_Cbox.setModel(new DefaultComboBoxModel<>(getYearRangeArray(1900, 2100)));
			End_Cbox.setEditable(false);
			End_Cbox.setSelectedItem("2100");
			End_Cbox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int endCurrent = (int) End_Cbox.getSelectedItem();
					Object boxSelected = Start_Cbox.getSelectedItem();
					Start_Cbox.setModel(new DefaultComboBoxModel<>(getYearRangeArray(1900, endCurrent)));
					Start_Cbox.setSelectedItem(boxSelected);
				}
			});
			/*
			设置默认输出格式选项为PDF
			*/
			PDFOutCBox.setSelected(true);
			/*
			设置隐藏折叠区间选项版及按钮
			*/
			HiddenRangePane.setPreferredSize(new Dimension(300, 0));//https://blog.csdn.net/leongod/article/details/5967838?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.channel_param
			updateHiddenPane();
			AddHiddenButton.setFont(new Font("sansserif", 0, 5)); // NOI18N
			AddHiddenButton.setText("＋");
			AddHiddenButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					JComboBox<Integer> newHiddenStart = new JComboBox<>();
					JComboBox<Integer> newHiddenEnd = new JComboBox<>();
					hiddenBoxSet.add(new JComboBox[] {newHiddenStart, newHiddenEnd});
					newHiddenStart.setModel(new DefaultComboBoxModel<>(getYearRangeArray(1900, 2100)));
					newHiddenEnd.setModel(new DefaultComboBoxModel<>(getYearRangeArray(1900, 2100)));
					HiddenRangePane.setPreferredSize(new Dimension(HiddenRangePane.getWidth(), HiddenRangePane.getHeight() + 50));
					updateHiddenPane();
					HiddenRangePane.repaint();
					HiddenRangeScrollPane.validate();
				}
			});
			HiddenRangeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			HiddenRangeScrollPane.setViewportView(HiddenRangePane);
			HiddenRangeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			/*
			用GroupLayout对ContentPane进行水平/垂直布局管理
			*/
			GroupLayout ContentPaneLayout = new GroupLayout(getContentPane());
			getContentPane().setLayout(ContentPaneLayout);
			ContentPaneLayout.setHorizontalGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(ContentPaneLayout.createSequentialGroup()
					.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(Start_Label)
						.addComponent(Start_Cbox, 80, 80 ,80)
					)
					.addGap(24)
					.addComponent(Array1_Label)
					.addGap(24)
					.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(End_Label)
						.addComponent(End_Cbox, 80, 80 ,80)
					)
				)
				.addGroup(ContentPaneLayout.createSequentialGroup()
					.addComponent(HidenRangeLabel)
					.addGap(10)
					.addComponent(AddHiddenButton)
				)
				.addGroup(ContentPaneLayout.createSequentialGroup()
					.addGap(10)
					.addComponent(HiddenRangeScrollPane, 300, 300, 300)
					.addGap(10)
				)
				.addGroup(ContentPaneLayout.createSequentialGroup()
					.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(OutputFormatLabel)
						.addComponent(PDFOutCBox)
						.addComponent(EPSOutCBox)
						.addComponent(JPGOoutCBox)
					)
					.addGap(30)
					.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(SetDPILabel)
						.addComponent(DPISilder, 80, 80, 80)
						.addComponent(RemoveTitleLabel)
						.addGroup(ContentPaneLayout.createSequentialGroup()
							.addComponent(RmTitleYesButton)
							.addContainerGap()
							.addComponent(RmTitleNoButton)
						)
					)
				)
				.addGroup(ContentPaneLayout.createSequentialGroup()
					.addComponent(Ok_Button)
					.addGap(50)
					.addComponent(Cancel_Button)
				)
			);
			ContentPaneLayout.setVerticalGroup(ContentPaneLayout.createSequentialGroup()
				.addGap(15)
				.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(Start_Label)
					.addComponent(End_Label)
				)
				.addGap(10)
				.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(Start_Cbox, 26, 26, 26)
					.addComponent(Array1_Label)
					.addComponent(End_Cbox, 26, 26 ,26)
				)
				.addGap(10)
				.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(HidenRangeLabel)
					.addComponent(AddHiddenButton)
				)
				.addGap(10)
				.addComponent(HiddenRangeScrollPane, 100, 100, 100)
				.addGap(10)
				.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(OutputFormatLabel)
					.addComponent(SetDPILabel)
				)
				.addGap(10)
				.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(PDFOutCBox)
					.addComponent(DPISilder)
				)
				.addGap(10)
				.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(EPSOutCBox)
					.addComponent(RemoveTitleLabel)
				)
				.addGap(10)
				.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(JPGOoutCBox)
					.addComponent(RmTitleYesButton)
					.addComponent(RmTitleNoButton)
				)
				.addGap(10)
				.addGroup(ContentPaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
					.addComponent(Ok_Button)
					.addComponent(Cancel_Button)
				)
				.addGap(15)
			);
			pack();//根据组件重设尺寸
			/*
			设置窗口现在于屏幕中央
			*/
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			int screenwidth = (int)screen.getWidth();
			int screenhight = (int) screen.getHeight();
			setLocation(screenwidth/2-getWidth()/2, screenhight/2-getHeight()/2);//屏幕正中央
		}
		/**
		 * 增加需要隐藏时间区间选项
		 */
		private void updateHiddenPane() {
			if (HiddenRangePane == null) return;
			GroupLayout HiddenRangePaneLayout = new GroupLayout(HiddenRangePane);
			HiddenRangePane.setLayout(HiddenRangePaneLayout);
			GroupLayout.ParallelGroup hhParallelGroup = HiddenRangePaneLayout.createParallelGroup(GroupLayout.Alignment.CENTER);
			GroupLayout.SequentialGroup hvSequentialGroup = HiddenRangePaneLayout.createSequentialGroup();
			int i = 0;
			for (@SuppressWarnings("rawtypes") JComboBox[] jcbs: hiddenBoxSet) {
				i++;
				JLabel newArray = new JLabel("→");
				hhParallelGroup.addGroup(HiddenRangePaneLayout.createSequentialGroup()
					.addGap(36)
					.addComponent(jcbs[0], 80, 80, 80)
					.addGap(24)
					.addComponent(newArray)
					.addGap(24)
					.addComponent(jcbs[1], 80, 80, 80)
					.addGap(36)
				);
				hvSequentialGroup
					.addGap(i==1?12:24)
					.addGroup(HiddenRangePaneLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jcbs[0], 26, 26, 26)
						.addComponent(jcbs[1], 26, 26, 26)
						.addComponent(newArray)
					);
			}
			HiddenRangePaneLayout.setHorizontalGroup(hhParallelGroup);
			HiddenRangePaneLayout.setVerticalGroup(hvSequentialGroup);
		}
		/**
		 * 获取隐藏时间段
		 * @return 代表多个隐藏时间段的二维数组
		 */
		public int [][] getHiddenRange() {
			return hiddenYears;
		}
		/**
		 * 判断是否需要移除标题行
		 * @return 布尔值，true为移除标题行
		 */
		public boolean isRemoveTitle() {
			return rmTitle;
		}
		/**
		 * 获取绘图起始年份
		 * @return 代表起始年份的整数
		 */
		public int getStart() {
			return drawRangeYears[0];
		}
		/**
		 * 获取绘图结束年份
		 * @return 代表结束年份的整数
		 */
		public int getEnd() {
			return drawRangeYears[1];
		}
		/**
		 * 获取绘图DPI
		 * @return 代表绘图DPI的整数
		 */
		public int getDPI() {
			return dpi;
		}
		/**
		 * 获取所有要求的输出格式
		 * @return 代表所有输出格式的ArrayList列表
		 */
		public ArrayList<String> getOutputFormats() {
			return outputtype;
		}
		/**
		 * 代表是否移除标题行，true为移除标题行
		 */
		private boolean rmTitle;
		/**
		 * 收集所有要求的输出格式的ArrayList对象
		 */
		private ArrayList<String> outputtype = new ArrayList<>();
		/**
		 * 代表绘图时间区间的数组，0号元素为开始时间，1号元素为结束时间
		 */
		private final int [] drawRangeYears = new int[2];
		/**
		 * 代表绘图DPI
		 */
		private int dpi;
		/**
		 * 代表绘图隐藏时间段的二维数组
		 */
		private int [][] hiddenYears;
		/**
		 * OK按钮，按下时获取参数并继续下面的绘图步骤
		 */
		private JButton Ok_Button;
		/**
		 * 取消按钮，按下时取消绘图
		 */
		private JButton Cancel_Button;
		/**
		 * 增加隐藏区间按钮，按下会增加隐藏区间设置选项
		 */
		private JButton AddHiddenButton;
		/**
		 * 输出为PDF选项的复选框
		 */
		private JCheckBox PDFOutCBox;
		/**
		 * 输出为EPS选项的复选框
		 */
		private JCheckBox EPSOutCBox;
		/**
		 * 输出文JPG选项的复选框
		 */
		private JCheckBox JPGOoutCBox;
		/**
		 * 绘图起始时间的选择组合框
		 */
		private JComboBox<Integer> Start_Cbox;
		/**
		 * 绘图结束时间的选择组合框
		 */
		private JComboBox<Integer> End_Cbox;
		/**
		 * 绘图隐藏区间的组件列表，每个元素代表一对隐藏区间设置选择组合框
		 */
		@SuppressWarnings("rawtypes")
		private ArrayList<JComboBox[]> hiddenBoxSet;
		/**
		 * Start字样标签
		 */
		private JLabel Start_Label;
		/**
		 * End字样标签
		 */
		private JLabel End_Label;
		/**
		 * 箭头标签
		 */
		private JLabel Array1_Label;
		/**
		 * DPI信息标签
		 */
		private JLabel SetDPILabel;
		/**
		 * Hidden Range字样标签
		 */
		private JLabel HidenRangeLabel;
		/**
		 * Output File Format字样标签
		 */
		private JLabel OutputFormatLabel;
		/**
		 * Remove Title字样标签
		 */
		private JLabel RemoveTitleLabel;
		/**
		 * 隐藏区间设置组件所在面板
		 */
		private JPanel HiddenRangePane = null;
		/**
		 * 代表移除标题行的选项-Yes
		 */
		private JRadioButton RmTitleYesButton;
		/**
		 * 代表不移除标题行的选项-No
		 */
		private JRadioButton RmTitleNoButton;
		/**
		 * 隐藏区间组件面板所需的滚动面板
		 */
		private JScrollPane HiddenRangeScrollPane;
		/**
		 * DPI值滑动杆组件
		 */
		private JSlider DPISilder;
		/**
		 * 用于线程通信的共享对象
		 */
		private final Object flag;
	}
}