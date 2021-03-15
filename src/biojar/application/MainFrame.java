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

import static biojar.application.SettingFrame.getDefaultDelimiter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import biojar.function.GeneralMethod;
import biojar.function.clinical.drug.CalConfig;
import biojar.function.clinical.drug.MakeConfig;
import biojar.function.clinical.drug.SuccessRate;
import biojar.function.clinical.drug.TimelineFormatException;
import biojar.function.clinical.drug.TimelineMaker;
import biojar.function.clinical.target.InteractionCalculate;
import biojar.function.clinical.target.KEGGPathway;
import biojar.function.clinical.target.SimilarProtein;
import biojar.function.clinical.target.Tissue;
import biojar.function.graphics.CircularHistogram;
import biojar.function.graphics.TargetHeatMap;
import biojar.function.lwj.DownloadProgress;
import biojar.function.lwj.webcrawler.GetFromClinicalTrial;
import biojar.function.lwj.webcrawler.GetFromFDA;
import biojar.function.lwj.webcrawler.GetFromUniprot;

/**
 * 项目可执行应用程序核心GUI类
 * @version 3.8.5
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class MainFrame extends JFrame {
	/**
	 * 对象序列化ID
	 */
	private static final long serialVersionUID = 19971120L;
	/**
	 * 软件名称
	 */
	private static final String TITLE = "Biojar 3.8.5";
	/**
	 * 非最大化下默认窗口宽度
	 */
	private static final int DEFAULT_WIDTH = 800;
	/**
	 * 非最大化下默认窗口高度
	 */
	private static final int DEFAULT_HEIGHT = 450;
	/**
	 * 选项板，放置文件
	 */
	private JTabbedPane jtabpane;
	/**
	 * 当前选中选项的文件名，带地址
	 */
	private String selected_file = null;
	/**
	 * 窗体字体，包括菜单栏
	 */
	private Font f = new Font("微软雅黑", Font.PLAIN, 16);
	/**
	 * 计算的开始年份
	 */
	private int cal_start = 2003;
	/**
	 * 计算的结束年份
	 */
	private int cal_end = 2011;
	/**
	 * 计算的类型是疾病还是药物
	 */
	private boolean cal_for_dis = true;
	/**
	 * 计算的类型是lead还是all
	 */
	private boolean cal_for_lead = true;
	/**
	 * 生成timeline的类型是year还是month
	 */
	private boolean make_for_year = true;
	/**
	 * 生成timeline的类型是不填充还是填充
	 */
	private boolean make_for_fill = false;
	/**
	 * 计算的默认疾病类型
	 */
	private String type = "Oncology";
	/**
	 * timeline生成的起始年份
	 */
	private int timeline_start = 1990;
	/**
	 * timeline生成的终止年份
	 */
	private int timeline_end = 2019;
	/**
	 * 计算的属性配置面板对象
	 */
	CalConfig calconfig = new CalConfig("Calculation configure");
	/**
	 * timeline生成的属性配置面板对象
	 */
	MakeConfig makconfig = new MakeConfig("Making configure");
	/**
	 * 文件选择器对象
	 * FileDialog类可以调用Window打开窗口，但不支持文件筛选器在Window下
	 */
	private final JFileChooser chooser = new JFileChooser();
	protected boolean initialStatus = false;
	/**
	 * 重置属性值的函数，在属性面板关闭时被调用
	 */
	public void ResetRateParam() {
		Object[] resp = calconfig.getConfigure();
		cal_for_dis = (boolean) resp[0];
		cal_for_lead = (boolean) resp[1];
		make_for_year = (boolean) makconfig.getConfigure()[0];
		make_for_fill = (boolean) makconfig.getConfigure()[1];
		timeline_start = (int) makconfig.getConfigure()[2];
		timeline_end = (int) makconfig.getConfigure()[3];
		cal_start = (int) resp[2];
		cal_end = (int) resp[3];
		type = (String) resp[4];
	}
	/**
	 * 构造方法，构建MainFrame对象
	 */
	public MainFrame() {
		initComponent(TITLE);
		initialStatus = true;
	}
	/**
	 * 构建组件，不可继承，用于避免在构造方法中使用可覆盖重写的函数
	 * @param title 窗体标题
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
		setExtendedState(JFrame.MAXIMIZED_BOTH);//设置打开默认为最大化
		Container contentPane = getContentPane();
		contentPane.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);//设置非最大化状态下默认大小
		
		calconfig.setVisible(false);//需要按按键才能显示
		makconfig.setVisible(false);
		ResetRateParam();
		makconfig.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				ResetRateParam();
			};
		});
		calconfig.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {
				ResetRateParam();
			};
		});
		
		
		//开始设置菜单栏
		JMenuBar menuBar =new JMenuBar();//添加菜单栏
		/*
		 * 文件菜单
		 */
		JMenu filemenu = new JMenu("File");
		/*
		 * 打开菜单项
		 */
		//配置文件类型
		chooser.setCurrentDirectory(new File("."));//设置默认目录
		chooser.setMultiSelectionEnabled(true);//运行多选打开文件
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("text file (*.txt)", "txt"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("java file (*.java)", "java"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("perl file (*.pl; *.pm)", "pl", "pm"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("python file (*.py)", "py"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("html file (*.html; *.htm)", "html", "htm"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("XML file (*.xml)", "xml"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("JSON file (*.json)", "json"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("javascript file (*.js)", "js"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Markdown file (*.md; *.markdown)", "md", "markdown"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("C file (*.c; *.h)", "c", "h"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("php file (*.php)", "php"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("jpeg file (*.jpeg; *.jpg)", "jpeg", "jpg"));
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("png file (*.png)", "png"));
		JMenuItem openItem = new JMenuItem("Open");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		filemenu.add(openItem);
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event) {
				int r = chooser.showOpenDialog(MainFrame.this);
				if (r == JFileChooser.APPROVE_OPTION) {
					for (File file: chooser.getSelectedFiles()) reloadOrOpen(file);
				}
			}
		});
		/*
		 * 退出菜单项
		 */
		JMenuItem exitItem = new JMenuItem("Exit");//添加退出按钮
		filemenu.add(exitItem);
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK));
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		/*
		 * 关闭文件菜单项
		 */
		JMenuItem closefileitem = new JMenuItem("Close file");
		filemenu.add(closefileitem);
		closefileitem.setEnabled(false);
		closefileitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
		closefileitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				int index = jtabpane.getSelectedIndex();
				if (index >=0) {
					//jtabpane.setSelectedIndex(index-1);
					//没有这句话的话自动判断，只要还有打开的就会选中
					jtabpane.remove(index);
				}
			}
		});
		/*
		 * 保存文件菜单项
		 */
		JMenuItem savefileitem = new JMenuItem("Save");
		savefileitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		savefileitem.setEnabled(false);
		filemenu.add(savefileitem);
		savefileitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FileJScrollPane tm= (FileJScrollPane) jtabpane.getSelectedComponent();
				try {
					if (selected_file != null) {
						try (PrintWriter pw = new PrintWriter(selected_file)) { // try-with-resources语句，保证无论正常与否都要关闭
							pw.print(tm.getJTextArea().getText());
						}
						JOptionPane.showMessageDialog(null,"文件已保存："+selected_file);
					}
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			}
		});
		/*
		 * 文件另存为菜单项
		 */
		JMenuItem savefileasitem = new JMenuItem("Save as");
		savefileasitem.setEnabled(false);
		savefileasitem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK|InputEvent.ALT_DOWN_MASK));
		filemenu.add(savefileasitem);
		savefileasitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				FileJScrollPane tm= (FileJScrollPane) jtabpane.getSelectedComponent();
				try {
					if (selected_file != null) {
						chooser.setCurrentDirectory(new File("."));
						chooser.setSelectedFile(new File(selected_file));//设置默认另存为文件名为同名
						int r = chooser.showSaveDialog(MainFrame.this);
						if (r == JFileChooser.APPROVE_OPTION) {
							selected_file = chooser.getSelectedFile().getAbsolutePath();
							try (PrintWriter pw = new PrintWriter(selected_file)) { //save 以selected_file为地址，故这个值也得更新
								pw.print(tm.getJTextArea().getText());
							}
							JOptionPane.showMessageDialog(null,"文件已另存为："+selected_file);
							tm.setFilePath(selected_file);//更新路径
							jtabpane.setTitleAt(//更新标题，内容不用reload，另存的就是当前内容
									jtabpane.getSelectedIndex(), chooser.getSelectedFile().getName()
							);
							setTitle(TITLE + "--"+selected_file);
						}
					}
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			}
		});
		/*
		 * 工具菜单
		 */
		JMenu toolsmenu = new JMenu("Tools");
		/*
		 * timeline计算二级菜单
		 */
		JMenu timelinemenu = new JMenu("Timeline calculation");
		toolsmenu.add(timelinemenu);
		/*
		 * 生成lead timeline菜单项
		 */
		JMenuItem transferitem1 = new JMenuItem("Transfer to lead timeline");
		transferitem1.setEnabled(false);
		timelinemenu.add(transferitem1);
		transferitem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
		transferitem1.addActionListener(new ActionListener() {//调用Timeline_maker类进行生成leadtimeline
			@Override
			public void actionPerformed (ActionEvent event)  {
				TimelineMaker tlm = new TimelineMaker();
				try {
					tlm.setAttribute("timeline start year", timeline_start);
					tlm.setAttribute("timeline end year", timeline_end);
					String n = tlm.makeLeadTimeline(selected_file, make_for_year, make_for_fill);
					if(selected_file != null && tlm.getStatus()) {
						reloadOrOpen(n);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			}
		});
		JMenuItem transferitem2 = new JMenuItem("Transfer to all timeline");
		/*
		 * 生成all timeline菜单项
		 */
		transferitem2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_DOWN_MASK));
		transferitem2.setEnabled(false);
		timelinemenu.add(transferitem2);
		transferitem2.addActionListener(new ActionListener() {//调用Timeline_maker类进行生成leadtimeline
			@Override
			public void actionPerformed (ActionEvent event) {
				TimelineMaker tlm = new TimelineMaker();
				try {
					tlm.setAttribute("timeline start year", timeline_start);
					tlm.setAttribute("timeline end year", timeline_end);
					String n = tlm.makeAllTimeline(selected_file, make_for_year, make_for_fill);
					if(selected_file != null && tlm.getStatus()) {
						reloadOrOpen(n);
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			}
		});
		JMenuItem transferconfigitem = new JMenuItem("Timeline making configure");
		transferconfigitem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK|InputEvent.ALT_DOWN_MASK));
		timelinemenu.add(transferconfigitem);
		transferconfigitem.setEnabled(false);
		transferconfigitem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				makconfig.setVisible(true);
			}
		});
		/**
		 * 计算结果菜单项
		 */
		JMenuItem calculateitem = new JMenuItem("Calculate rate");
		calculateitem.setEnabled(false);
		calculateitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
		timelinemenu.add(calculateitem);
		calculateitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent event)  {
				SuccessRate sr = new SuccessRate(cal_for_lead);
				try {
					sr.RateCalculate(cal_for_dis, cal_for_lead, cal_start, cal_end, type, selected_file);
				} catch (TimelineFormatException e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
				if (sr.getStatus()) {
					JOptionPane.showMessageDialog(null, "计算完成...");
					reloadOrOpen(
						String.format("output/[%d-%d]%s-drug-scc-ratio-%s.txt",
						cal_start, cal_end, type, (cal_for_lead?"lead":"all")
					));
				}
			}
		});
		/**
		 * 计算配置菜单项
		 */
		JMenuItem calculateconfigitem = new JMenuItem("Calculation configure");
		calculateconfigitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
		timelinemenu.add(calculateconfigitem);
		calculateconfigitem.setEnabled(false);
		calculateconfigitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				calconfig.setVisible(true);
			}
		});
		
		/**
		 * 网络数据获取二级菜单
		 */
		JMenu fetchdatamenu = new JMenu("Web crawler");
		toolsmenu.add(fetchdatamenu);
		/**
		 * 获取FDA批准信息菜单项
		 */
		JMenuItem approvaldatefdaitem = new JMenuItem("Get FDA original approval date");
		approvaldatefdaitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, InputEvent.CTRL_DOWN_MASK));
		fetchdatamenu.add(approvaldatefdaitem);
		approvaldatefdaitem.setEnabled(false);
		approvaldatefdaitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				/**
				 * 实现从fda获取批准时间的内部类，线程类。匿名内部类使用变量为final，不适合进度条
				 */
				class fda extends Thread {
					@Override
					public void run() {
						try {
							GetFromFDA gff = new GetFromFDA();//实例化，避免类变量改变引起的问题
							DownloadProgress dp = new DownloadProgress("获取进度");
							dp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
							dp.addWindowListener(new WindowAdapter() {
								@Override
								public void windowClosing(WindowEvent we) {//实现关闭按钮终止该线程
									int value = JOptionPane.showConfirmDialog(null, "确定要终止获取批准时间吗？");
									if (value == JOptionPane.OK_OPTION) {
										gff.cancel();
									}
								}
							});
							gff.getOriginalApprovalDate(selected_file, "FDA approval date.txt", dp);
							dp.dispose();
							if (!gff.isCancel()) {
								reloadOrOpen("FDA approval date.txt");
							}
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(null, ioe.getMessage());
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
						}
					}
				}
				fda f1 = new fda();//创建新线程
				f1.start();
			}
		});
		/*
		 * 获取Uniprot信息菜单项
		 */
		JMenuItem uniprotitem = new JMenuItem("Get protein basic information");
		uniprotitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, InputEvent.CTRL_DOWN_MASK));
		fetchdatamenu.add(uniprotitem);
		uniprotitem.setEnabled(false);
		uniprotitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				/**
				 * 实现从uniprot获取信息的内部类，线程类。匿名内部类使用变量为final，不适合进度条
				 */
				class uniprot extends Thread {
					@Override
					public void run() {
						try {
							GetFromUniprot gfu = new GetFromUniprot();//实例化，避免类变量改变引起的问题
							DownloadProgress dp = new DownloadProgress("获取进度");
							dp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
							dp.addWindowListener(new WindowAdapter() {
								@Override
								public void windowClosing(WindowEvent we) {//实现关闭按钮终止该线程
									int value = JOptionPane.showConfirmDialog(null, "确定要终止获取Uniprot信息吗？");
									if (value == JOptionPane.OK_OPTION) {
										gfu.cancel();
									}
								}
							});
							gfu.getUniprotEntrys(
								selected_file,
								"Uniprot entry detail.txt",
								dp,
								Integer.parseInt(JOptionPane.showInputDialog("请输入Uniprot ID列索引（从0开始）")),
								50
							);
							dp.dispose();
							if (!gfu.isCancel()) {
								reloadOrOpen("Uniprot entry detail.txt");
							}
						} catch (FileNotFoundException e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(null, ioe.getMessage());
						} catch (InterruptedException ie) {
							JOptionPane.showMessageDialog(null, ie.getMessage());
						}
					}
				}
				uniprot u1 = new uniprot();//创建新线程
				u1.start();
			}
		});
		/*
		 * 获取fasta文件菜单项
		 */
		JMenuItem fastaitem = new JMenuItem("Get protein fasta");
		fastaitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.CTRL_DOWN_MASK));
		fetchdatamenu.add(fastaitem);
		fastaitem.setEnabled(false);
		fastaitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				class fasta extends Thread {
					@Override
					public void run() {
						try {
							GetFromUniprot gfu = new GetFromUniprot();//实例化，避免类变量改变引起的问题
							DownloadProgress dp = new DownloadProgress("获取进度");
							dp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
							dp.addWindowListener(new WindowAdapter() {
								@Override
								public void windowClosing(WindowEvent we) {//实现关闭按钮终止该线程
									int value = JOptionPane.showConfirmDialog(null, "确定要终止获取fasta信息吗？");
									if (value == JOptionPane.OK_OPTION) {
										gfu.cancel();
									}
								}
							});
							gfu.downloadFasta(selected_file, Integer.parseInt(JOptionPane.showInputDialog("请输入Uniprot ID列索引（从0开始）")), dp);
							dp.dispose();
						} catch (FileNotFoundException e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
						} catch (IOException ioe) {
							JOptionPane.showMessageDialog(null, ioe.getMessage());
						}
					}
				}
				fasta f1 = new fasta();
				f1.start();
			}
		});
		
		/*
		 * ID转化菜单项
		 */
		JMenuItem mappingitem = new JMenuItem("ID mapping");
		mappingitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.CTRL_DOWN_MASK));
		fetchdatamenu.add(mappingitem);
		mappingitem.setEnabled(false);
		mappingitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				class FromThread extends Thread {
					SelectFrame fromframe;
					public FromThread(SelectFrame sf) {
						fromframe = sf;
					}
					@Override
					public void run() {
						fromframe.setVisible(true);
					}
				}
				class ToThread extends Thread {
					SelectFrame toframe;
					SelectFrame fromframe;
					public ToThread(SelectFrame sf, SelectFrame st) {
						fromframe = sf;
						toframe = st;
					}
					@Override
					public void run() {
						try {
							synchronized (fromframe) {fromframe.wait();} //先用synchronized封装对象在线程里才能调用wait/notify，等获得from值后再进行
							toframe.setVisible(true);
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
						
					}
				}
				ArrayList <String> al = new ArrayList<String>();
				SelectFrame sff = new SelectFrame("from", "configure/ID_mapping/from.txt");
				SelectFrame sft = new SelectFrame("to", "configure/ID_mapping/to.txt");
				class CalThread extends Thread {
					@Override
					public void run() {
						try {
							synchronized (sff) {sff.wait();}//等获得from值后再进行
							synchronized (sft) {sft.wait();}//等获得to值后再进行
							HashMap <String, String> name_abbr = new HashMap<>();
							try (LineNumberReader ln = GeneralMethod.BufferRead("configure/ID_mapping/name_abbr.txt")) {
								String line = null;
								while ((line = ln.readLine()) != null) {
									String[] tmp = line.split("\t");
									name_abbr.put(tmp[0], tmp[1]);
								}
							}
							String selectFrom = sff.getSelectValue();
							String selectTo = sft.getSelectValue();
							if (selectFrom != null && selectTo != null && !selectFrom.equals(selectTo)) {
								try (LineNumberReader lnr = GeneralMethod.BufferRead(selected_file)) {
									String line = null;
									while ((line = lnr.readLine())!= null) al.add(line);
								}
								GetFromUniprot gfu = new GetFromUniprot();
								String outputfile ="From " + name_abbr.get(selectFrom) + " to " + name_abbr.get(selectTo) +".txt";
								try (PrintWriter pw = new PrintWriter(outputfile)) {
									pw.print(gfu.transferID(name_abbr.get(selectFrom),name_abbr.get(selectTo), "tab", GeneralMethod.join(" ", al)));
								}
								reloadOrOpen(outputfile);
							}
						} catch (IOException ioe) {
							ioe.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				CalThread ct = new CalThread();
				ct.start();
				ToThread ptt = new ToThread(sff, sft);
				ptt.start();
				FromThread ptf = new FromThread(sff);
				ptf.start();
			}
		});
		/*
		 * 尚待开发从https://www.clinicaltrials.gov上获取数据
		 */
		JMenuItem nctitem = new JMenuItem("Get from clinicaltrials.gov");
		nctitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, InputEvent.CTRL_DOWN_MASK));
		fetchdatamenu.add(nctitem);
		nctitem.setEnabled(false);
		nctitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//定义默认选项配置
				ArrayList <String> default_select_field = new ArrayList<>();
				try (LineNumberReader lnr = GeneralMethod.BufferRead("configure/NCT/DefaultNCTFieldList.txt")) {
					String line;
					while ((line = lnr.readLine()) != null) {
						default_select_field.add(line);
					}
				} catch (FileNotFoundException fnfe) {
					JOptionPane.showMessageDialog(null, "默认选项配置文件缺失");
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(null, "配置文件读取异常");
				}
				MutiSelectFrame msf = new MutiSelectFrame("Fields", "configure/NCT/NCTFieldList.txt", default_select_field);
				class MutiSelectThread extends Thread {
					@Override
					public void run() {
						msf.setVisible(true);
					}
				}
				class ClinicalTrialThread extends Thread {
					@Override
					public void run() {
						synchronized(msf) {
							try {
								msf.wait();
							} catch (InterruptedException ex) {
								JOptionPane.showMessageDialog(null, ex.getMessage());
							}
						}
						ArrayList <String> field = msf.getSelectValue();
						String outputfile = "output/NCT info.txt";
						if (field.size() > 0) {
							GetFromClinicalTrial gfct = new GetFromClinicalTrial();
							try {
								gfct.getNCTInfo(selected_file, outputfile, field);
							} catch (Exception ex) {
								JOptionPane.showMessageDialog(null, ex.getMessage());
								ex.printStackTrace();
							}
							if (!gfct.isCancelStatus()) reloadOrOpen(outputfile);
						}
					}
				}
				ClinicalTrialThread ctt = new ClinicalTrialThread();
				ctt.start();
				MutiSelectThread mst = new MutiSelectThread();
				mst.start();
			}
		});
		/*
		 * 蛋白质靶点五原则计算二级菜单
		 */
		JMenu principlecalmenu = new JMenu("Five principle calculation");
		toolsmenu.add(principlecalmenu);
		JMenuItem dg_ncitem = new JMenuItem("Degree & neighborhood calculation");
		dg_ncitem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
		principlecalmenu.add(dg_ncitem);
		dg_ncitem.setEnabled(false);
		dg_ncitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				/**
				 * 实现计算Degree和neighborhood connectivity的内部类，线程类。匿名内部类使用变量为final，不适合进度条
				 */
				class InteractProtein extends Thread {
					@Override
					public void run() {
						InteractionCalculate ic = new InteractionCalculate();
						try {
							ic.calculateNeighborhoodConnectivity(selected_file, true);//("C:\\Users\\idrb\\Desktop\\IDRB\\药物靶点进展速率\\04-calculate target profile\\Degree_Neighbor\\selected_protein.txt", true);
							ic.calculateDegree(selected_file, true);//("C:\\Users\\idrb\\Desktop\\IDRB\\药物靶点进展速率\\04-calculate target profile\\Degree_Neighbor\\selected_protein.txt", true);
							HashMap <String, Double> neighbor_map = ic.getNeighborhoodConnectivityMap();
							HashMap <String, Integer> degree_map = ic.getDegreeMap();
							try (java.io.PrintWriter pw = new java.io.PrintWriter("degree_nc.txt")) {
								pw.println("ENSP ID\tDegree\tNeighborhoodConnectivity");
								for (String protein1: degree_map.keySet()) pw.println(protein1 + "\t" + degree_map.get(protein1) + "\t" + neighbor_map.get(protein1));
							}
							reloadOrOpen("degree_nc.txt");
						} catch (FileNotFoundException e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
						}
					}
				}
				InteractProtein ip = new InteractProtein();
				ip.start();
			}
		});
		/*
		 * 进行相似蛋白的去重
		 */
		JMenuItem similar_Item = new JMenuItem("Similar protein calculation");
		similar_Item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
		principlecalmenu.add(similar_Item);
		similar_Item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				class similarproteinThread implements Runnable {
					@Override
					public void run() {
						JFileChooser jfc = new JFileChooser(selected_file==null?".":new File(selected_file).getParent());
						jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//选择文件夹
						jfc.setDialogTitle("选择数据库源");
						jfc.showOpenDialog(null);
						File dbDirectory = jfc.getSelectedFile();
						jfc.setDialogTitle("选择待查询fasta源");
						if (dbDirectory != null) jfc.showOpenDialog(null);
						File queryDirectory = jfc.getSelectedFile();
						jfc.setDialogTitle("选择blast输出地址");
						if (queryDirectory != null) jfc.showOpenDialog(null);
						File blastDirectory = jfc.getSelectedFile();
						jfc.setDialogTitle("选择最终输出地址");
						if (blastDirectory != null) jfc.showOpenDialog(null);
						File outputDirectory = jfc.getSelectedFile();
						String eString = null;
						if (outputDirectory != null)  eString = JOptionPane.showInputDialog("请输入E value值");
						double evalue = eString==null?0.0:Double.parseDouble(eString);
						SimilarProtein sp = new SimilarProtein();
						DownloadProgress dp1 = new DownloadProgress("blast进度", 2);
						DownloadProgress dp2 = new DownloadProgress("normalize进度");
						DownloadProgress[] dpset = {dp1, dp2};
						for (DownloadProgress dp: dpset) {
							dp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
							dp.addWindowListener(new WindowAdapter() {
								@Override
								public void windowClosing(WindowEvent we) {//实现关闭按钮终止该线程
									int value = JOptionPane.showConfirmDialog(null, "确定要终止BLAST吗？");
									if (value == JOptionPane.OK_OPTION) {
										sp.cancel();
									}
								}
							});
						}
						try {
							sp.blastp(dbDirectory, queryDirectory, evalue, blastDirectory, dp1);
							dp1.dispose();
							sp.normalizeResult(blastDirectory, outputDirectory, evalue, dp2);
							dp2.dispose();
							if (!sp.isCancel()) {
								reloadOrOpen(outputDirectory.getAbsolutePath() + "\\Blast_result_E-vale-"+evalue+"-all.txt");
								reloadOrOpen(outputDirectory.getAbsolutePath() + "\\Blast_result_E-vale-"+evalue+"-db.txt");
							}
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
				Thread t1 = new Thread(new similarproteinThread());
				t1.start();
				
			}
		});
		/*
		 * 获取KEGG pathway
		 */
		JMenuItem getKEGGPathwayItem = new JMenuItem("Get KEGG pathways");
		getKEGGPathwayItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
		principlecalmenu.add(getKEGGPathwayItem);
		getKEGGPathwayItem.setEnabled(false);
		getKEGGPathwayItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				class myThread extends Thread {
					@Override
					public void run() {
						KEGGPathway kp = new KEGGPathway();
						DownloadProgress dp = new DownloadProgress("获取进度");
						dp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						dp.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosing(WindowEvent we) {//实现关闭按钮终止该线程
								int value = JOptionPane.showConfirmDialog(null, "确定要终止获取kegg pathway信息吗？");
								if (value == JOptionPane.OK_OPTION) {
									kp.cancel();
								}
							}
						});
						try {
							kp.getKEGGPathwaybyEntry(
									selected_file,
									"kegg pathway.txt",
									Integer.parseInt(JOptionPane.showInputDialog(null, "kegg entry id列索引（从0开始）")),
									JOptionPane.showConfirmDialog(null, "是否包含标题行")==JOptionPane.OK_OPTION,
									dp);
							dp.dispose();
							if (!kp.isCancel()) {
								reloadOrOpen("kegg pathway.txt");
							}
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null, ex.getMessage());
						}
					}
				}
				new myThread().start();
			}
		});
		/*
		 * 从KEGG中下载通路图
		 */
		JMenuItem downloadKEGGFigureItem = new JMenuItem("Download KEGG pathway figures");
		downloadKEGGFigureItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_DOWN_MASK));
		principlecalmenu.add(downloadKEGGFigureItem);
		downloadKEGGFigureItem.setEnabled(false);
		downloadKEGGFigureItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				class downloadKEGGThread extends Thread {
					@Override
					public void run() {
						DownloadProgress dp = new DownloadProgress("KEGG通路下载进度");
						KEGGPathway kp = new KEGGPathway();
						dp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						dp.addWindowListener(new WindowAdapter() {
							@Override
							public void windowClosing(WindowEvent we) {//实现关闭按钮终止该线程
								int value = JOptionPane.showConfirmDialog(null, "确定要终止下载KEGG通路图吗？");
								if (value == JOptionPane.OK_OPTION) {
									kp.cancel();
								}
							}
						});
						String hightlightcolor = null;
						String outputdir = "KEGG pathway figure";
						int pathwayIdLoca = Integer.parseInt(JOptionPane.showInputDialog(null, "请输入pathway id列索引（从0开始）")); 
						int highlightTermIdLoca = 0;
						if (JOptionPane.showConfirmDialog(null, "是否需要进行颜色强调") == JOptionPane.OK_OPTION) {
							highlightTermIdLoca = Integer.parseInt(JOptionPane.showInputDialog(null, "请输入强调信息列索引（从0开始）"));
							hightlightcolor = KEGGPathway.RED;
						}
						boolean ishead = (JOptionPane.showConfirmDialog(null, "输入是否包含标题行")==JOptionPane.OK_OPTION);
						try {
							kp.downloadPathwayFigure(selected_file, hightlightcolor, outputdir, ishead, pathwayIdLoca, highlightTermIdLoca, dp);
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null, ex.getMessage());
						}
						dp.dispose();
					}
				}
				downloadKEGGThread d1 = new downloadKEGGThread();
				d1.start();
			}
		});
		JMenuItem tissueItem = new JMenuItem("Protein tissue distribution query");
		tissueItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_DOWN_MASK));
		principlecalmenu.add(tissueItem);
		tissueItem.setEnabled(false);
		tissueItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				class tissuethread extends Thread {
					@Override
					public void run() {
						Tissue tissue = new Tissue();
						tissue.loadDatabase();
						DownloadProgress dp = new DownloadProgress("查询进度");
						tissue.queryTissueDistribution(
							selected_file,
							"tissue_distribution.txt",
							Integer.parseInt(JOptionPane.showInputDialog(null, "请输入uniprot accession列索引（从0开始）")),
							JOptionPane.showConfirmDialog(null, "输入是否包含标题") == JOptionPane.OK_OPTION,
							dp
						);
						dp.dispose();
						reloadOrOpen("tissue_distribution.txt");
					}
				}
				new tissuethread().start();
			}
		});
		/*
		 * 绘图菜单
		 */
		JMenu paintMenu = new JMenu("Painting");
		toolsmenu.add(paintMenu);
		/**
		 * 绘制靶点热图菜单项
		 */
		JMenuItem targetHeatMapItem = new JMenuItem("Draw target heat map");
		targetHeatMapItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));//Ctrl + F
		targetHeatMapItem.setEnabled(false);
		paintMenu.add(targetHeatMapItem);
		targetHeatMapItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object flag = new Object();//线程并发控制对象
				TargetHeatMap.ParamConfigFrame pcf = new TargetHeatMap.ParamConfigFrame(flag);//绘制参数设置
				class targetHeatMapThread extends Thread {
					@Override
					public void run() {
						try {
							synchronized(flag) {
								flag.wait();
							}
							while (true) {
								ArrayList<String> outputFormat = pcf.getOutputFormats();
								if (outputFormat == null) break;//取消键
								int startYear = pcf.getStart();
								int endYear = pcf.getEnd();
								int[][] hiddenYears = pcf.getHiddenRange();
								int dpi = pcf.getDPI();
								boolean rmTitle = pcf.isRemoveTitle();
								for (String type:  outputFormat) {
									try {
										TargetHeatMap thm = new TargetHeatMap();
										thm.drawTargetFigure(
											selected_file,
											"target_fig_%d_%d.%s",
											type,
											startYear,
											endYear,
											hiddenYears,
											dpi,
											rmTitle
										);
									} catch (NumberFormatException nfe) {
										throw new Exception("输入数字包含非数字内容");
									}
									File outputfile = new File(
										String.format(
											"target_fig_%d_%d.%s",
											startYear,
											endYear,
											type));
									JOptionPane.showMessageDialog(null, 
										"已完成绘制，存储至\"" + outputfile.getCanonicalPath() + "\""
									);
								}
								break;
							}
						} catch (Exception ex) {
							JOptionPane.showMessageDialog(null, ex.getMessage());
						}
					}
				}
				new targetHeatMapThread().start();
				pcf.setVisible(true);
			}
		});
		/*
		多层环形柱状图
		*/
		JMenuItem circularHistogramItem = new JMenuItem("Draw circular histogram");
		circularHistogramItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));//Ctrl + H
		circularHistogramItem.setEnabled(false);
		paintMenu.add(circularHistogramItem);
		circularHistogramItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				class CircularHistogramThread extends Thread {
					@Override
					public void run() {
						try {
							while (true) {
								CircularHistogram ch = new CircularHistogram();
								int hasTitle = JOptionPane.showConfirmDialog(null, "输入是否包含标题行？");
								if (hasTitle==JOptionPane.CANCEL_OPTION||hasTitle==JOptionPane.CLOSED_OPTION)
									break;
								String cutoffString = JOptionPane.showInputDialog("请输入想要输出绘图的TOP N值");
								if (cutoffString == null) break;
								int cutoff = 100;
								try {
									cutoff = Integer.parseInt(cutoffString);
								} catch (NumberFormatException e) {
									throw new Exception("输入的N值非纯数字");
								}
								ch.loadData(selected_file, hasTitle==JOptionPane.OK_OPTION, cutoff);
								String maxValueString = JOptionPane.showInputDialog("请输入柱形值上限");
								double maxValue;
								try {
									maxValue = Double.parseDouble(maxValueString);
								} catch (NumberFormatException e) {
									throw new Exception("输入的最大值非纯数字");
								}
								String[] fileTypeOption = {"pdf", "eps", "jpg"};
								int selectedIndex = JOptionPane.showOptionDialog(
									null,
									"请选择输出类型",
									null,
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.QUESTION_MESSAGE,
									null,
									fileTypeOption,
									null
								);
								if (selectedIndex == JOptionPane.CLOSED_OPTION) break;
								if (selectedIndex == 2) {
									String dpiString = JOptionPane.showInputDialog("输入JPG图像DPI");
									if (dpiString == null) break;
									try {
										ch.setJpegDpi(Integer.parseInt(dpiString));
									} catch(NumberFormatException e) {
										throw new Exception("输入的DPI非纯整数");
									}
								}
								String totalAngleString = JOptionPane.showInputDialog("请输入扇形角度（角度制）");
								if (totalAngleString == null) break;
								try {
									ch.setTotalAngle(Double.parseDouble(totalAngleString));
								} catch (NumberFormatException e) {
									throw new Exception("输入的角度非纯数字");
								}
								ch.setMaxValue(maxValue);
								Color[] colorSet = MutiColorPane.showDiag();
								if (colorSet==null) break;
								ch.setBarColorSet(colorSet);
								ch.drawFigure("Circular histogram %dd.%s", true, fileTypeOption[selectedIndex]);
								
								JOptionPane.showMessageDialog(
									null, 
									"绘图成功\""+ch.getOutputFile().getCanonicalPath()+"\"");
								break;
							}
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
						}
					}
				}
				CircularHistogramThread cht = new CircularHistogramThread();
				cht.start();
			}
		});
		/*
		 * 观看菜单
		 */
		JMenu viewmenu = new JMenu("View");
		/**
		 * 重载文件菜单项
		 */
		JMenuItem reloaditem = new JMenuItem("Reload file");
		reloaditem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
		reloaditem.setEnabled(false);
		viewmenu.add(reloaditem);
		reloaditem.addActionListener(new ActionListener() {//重新加载当前选项文件
			@Override
			public void actionPerformed(ActionEvent ev) {
				if (selected_file != null) {
					FileJScrollPane tm = (FileJScrollPane) jtabpane.getSelectedComponent();
					JComponent component;
					switch(getFileExtension(selected_file)) {
						case "jpeg":
						case "png":
						case "jpg":{component = tm.getFigurePane();break;}
						default:component = tm.getJTextArea();
					}
					if (component == null) {
						JOptionPane.showMessageDialog(null, "Null type object!");
						return;
					}
					reloadFile(selected_file, component);
					JOptionPane.showMessageDialog(null, "已经重新加载" + selected_file);
				}
			}
		});
		/*
		 * 转为表格显示菜单项
		 */
		JMenuItem showastabitem = new JMenuItem("Show in table");
		viewmenu.add(showastabitem);
		showastabitem.setEnabled(false);
		showastabitem.setAccelerator(
			KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK|InputEvent.ALT_DOWN_MASK));
		/*
		table暂时只用于预览, 待添加其他功能，包括选中单元格可在上方显示全部，类似Excel，保存为Excel等
		*/
		showastabitem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				JTable jtab = loadFileByTable(selected_file);
				FileJScrollPane jscrol = new FileJScrollPane(jtab, selected_file);
				String name = new File(selected_file).getName();
				jtabpane.add(name, jscrol);
				jtabpane.setSelectedComponent(jscrol);
			}
		});
		
		menuBar.add(filemenu);
		menuBar.add(toolsmenu);
		menuBar.add(viewmenu);
		
		//配置菜单栏字号，暂时只有两层，优化可以考虑递归调用以满足任意层次
		for (int index = 0; index < menuBar.getMenuCount(); index++) {
			JMenu jm =  menuBar.getMenu(index);
			jm.setFont(f);
			for (Component c0: jm.getMenuComponents()) {
				c0.setFont(f);
				if (c0 instanceof JMenu) {
					for(Component c1: ((JMenu) c0).getMenuComponents()) c1.setFont(f);
				}
			}
		}
		setJMenuBar(menuBar);
		
		//开始设置主选项板
		jtabpane = new JTabbedPane();
		jtabpane.setTabPlacement(JTabbedPane.BOTTOM);
		jtabpane.addChangeListener(new ChangeListener() {//当标签等状态发生改变，变更选择的文件地址
			@Override
			public void stateChanged(ChangeEvent ev) {
				if (jtabpane.getTabCount() > 0) {
					selected_file = ((FileJScrollPane) jtabpane.getSelectedComponent()).getFilePath();
					setTitle(TITLE + "--"+selected_file);
				} else {//考虑到当标签由由到无时没有选项
					selected_file = null;
					setTitle(TITLE);
				}
				JMenuItem[] set = {
					closefileitem, savefileitem, savefileasitem, transferitem1,transferitem2,
					calculateitem, reloaditem, calculateconfigitem,showastabitem,transferconfigitem,
					approvaldatefdaitem, uniprotitem, dg_ncitem,mappingitem, fastaitem,
					downloadKEGGFigureItem, nctitem, getKEGGPathwayItem, tissueItem, targetHeatMapItem,
					circularHistogramItem
				};
				for (JMenuItem jmi: set) jmi.setEnabled((selected_file != null));
				/**
				 * table模式下，这些功能暂未开发
				 */
				if (selected_file != null&&((FileJScrollPane)jtabpane.getSelectedComponent()).getJTable()!=null) {
					JMenuItem[] set2 = {savefileitem, savefileasitem,showastabitem, reloaditem};
					for(JMenuItem jmi: set2) jmi.setEnabled(false);
				}
				if (selected_file != null&&((FileJScrollPane)jtabpane.getSelectedComponent()).getFigurePane()!=null) {
					JMenuItem[] set2 = {savefileitem, savefileasitem,showastabitem};
					for(JMenuItem jmi: set2) jmi.setEnabled(false);
				}
			}
		});
		jtabpane.setFont(f);
		File rootFile = new File("..");
		TreePanel treePanel = new TreePanel(rootFile, this);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, jtabpane);

		splitPane.setOneTouchExpandable(true);
		splitPane.setSize(contentPane.getSize());
		splitPane.setDividerLocation(0.3);
		contentPane.add(splitPane, BorderLayout.CENTER);//添加选项板到内容面板中央，由BorderLayout布局管理
	}
	/**
	 * 打开或重载指定文件名的文件
	 * @param filename 指定文件名
	 */
	public void reloadOrOpen(String filename) {
		reloadOrOpen(new File(filename));
	}
	/**
	 * 打开或重载指定文件对象的文件
	 * @param file 指定文件对象
	 */
	public void reloadOrOpen(File file) {
		if (file==null||!file.exists()||file.isDirectory()) {
			JOptionPane.showMessageDialog(this, "File Not Exist! Please Check it!");
			return;
		}
		String path = file.getAbsolutePath();
		String name = file.getName();
		int is_open = isFileOpen(path);
		if (is_open >= 0) {
			jtabpane.setSelectedIndex(is_open);
			FileJScrollPane tm = (FileJScrollPane) jtabpane.getSelectedComponent();
			switch(getFileExtension(name)) {
				case "png":
				case "jpg":
				case "jpeg":{reloadFile(selected_file, tm.getFigurePane());break;} //break缺失会导致JLabel的NullPointerException,因为default语句也执行了
				default:reloadFile(selected_file, tm.getJTextArea());
			}
			JOptionPane.showMessageDialog(null, "已经重新加载" + selected_file);
		} else {
			JComponent component = loadFile(path);
			FileJScrollPane jscrol = new FileJScrollPane(component, path);
			
			jtabpane.add(name, jscrol);
			jtabpane.setSelectedComponent(jscrol);

			switch(getFileExtension(name)) {
				case "png":
				case "jpg":
				case "jpeg":{
					FigurePane figurePanel = jscrol.getFigurePane();
					BufferedImage rawImage;//原图信息只读取一次
					try {
						rawImage = ImageIO.read(new File(selected_file));
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(null, ex.getMessage());
						return;
					}
					
					jscrol.addMouseWheelListener(new MouseWheelListener() {//实现Ctrl + 滚轮的放缩图片
						@SuppressWarnings("unused")
						@Override
						public void mouseWheelMoved(MouseWheelEvent e) {
							int width = figurePanel.getImageIconWidth();
							int height = figurePanel.getImageIconHeight();
							if (figurePanel==null) return;
							if (e.getWheelRotation() == 1 && e.isControlDown()) {//向下转动滚轮，缩小
								width  = 4* width/5;
								height = 4*height/5;
							}
							if (e.getWheelRotation() == -1&& e.isControlDown()) {//向上转动滚轮，放大
								width  = 5*width/4;
								height = 5*height/4;
							}
							if(width <= rawImage.getWidth() && height <= rawImage.getHeight()) {//限制最大显示为原图尺寸
								figurePanel.setImageIconSize(width, height);
								figurePanel.paint(figurePanel.getGraphics());
							}
						}
					});
					break;
				}
				default:{
					JTextArea fileText =(JTextArea) component;
					fileText.setFont(f);
				}
			}
		}
		chooser.setCurrentDirectory(file.getParentFile());
	}
	/**
	 * 返回Windows文件名的小写后缀
	 * @param filename 文件名
	 * @return 文件扩展名
	 */
	public String getFileExtension(String filename) {
		String[] arr = filename.split("\\.");
		return arr[arr.length - 1].toLowerCase();
	}
	/**
	* 判断文件是否已经打开，是则返回打开的选项卡索引
	* @param filename 要判断的文件名，带地址
	* @return int值，-1代表未打开，大于0代表打卡的位置索引
	*/
	public int isFileOpen(String filename) {
		String absolutepath = new File(filename).getAbsolutePath();
		int res = -1;
		for (int index = 0; index < jtabpane.getComponentCount(); index++) {
			if (jtabpane.getComponent(index) instanceof FileJScrollPane) {
				FileJScrollPane e = (FileJScrollPane) jtabpane.getComponent(index);
				if(e.getFilePath().equals(absolutepath)) {
					res = index;
					break;
				}
			}
		}
		return res;
	}
	/**
	 * 加载文件并返回JTextArea对象
	 * @param name 要加载的文件名，带地址
	 * @return 加载了文件的JTextArea对象
	 */
	public JComponent loadFile(final String name) {
		switch(getFileExtension(name)) {
			case "png":
			case "jpg":
			case "jpeg":{
				//JLabel figureLabel = new JLabel();
				FigurePane figureLabel = new FigurePane();
				reloadFile(name, figureLabel);
				return figureLabel;
			}
			default:{
				JTextArea fileText = new JTextArea();
				reloadFile(name, fileText);
				return fileText;
			}
		}
	}
	/**
	 * 对指定JTextArea对象进行文本重载
	 * @param name 重载的文件源
	 * @param Component 要重载的对象
	 */
	public void reloadFile(final String name, JComponent Component) {
		switch(getFileExtension(name)) {
			case "png":
			case "jpg":
			case "jpeg":{
				FigurePane figurePane = (FigurePane) Component;
				figurePane.setImage(new ImageIcon(name));
				break;
			}
			default:{
				JTextArea jta = (JTextArea) Component;
				jtabpane.setEnabled(false);//选项板由false到true，显示加载完毕
				jta.setText("");//清空原先文本
				new SwingWorker<Void, Void> () {
					@Override
					protected Void doInBackground () throws Exception {
						try {
							try (Scanner in = GeneralMethod.ScanRead(name)) {
								while (in.hasNext()) {
									jta.append(in.nextLine());
									jta.append("\r\n");
								}
							}
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
						}
						return null;
					}
					@Override
					protected void done() {
						jtabpane.setEnabled(true);
					}
				}.execute();
			}
		}
	}
	/**
	 * 将特定文件用JTable展示
	 * @param name 文件名
	 * @return JTable对象
	 */
	public JTable loadFileByTable(String name) {
		try {
			String[] columnsName;
			Object[][] Data;
			try (LineNumberReader in = GeneralMethod.BufferRead(name)) {
				String line = null;
				String title = null;
				ArrayList <Object[]> datalist = new ArrayList<Object[]>();
				while ((line = in.readLine())!= null) {
					if (in.getLineNumber() == 1) {
						title = line;
					} else {
						datalist.add(line.split(getDefaultDelimiter()));
					}
				}	columnsName = title.split(getDefaultDelimiter());
				Data = datalist.toArray(new Object[0][]); //转为二维数组
			}
			JTable res = new JTable(Data, columnsName);
			res.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			return res;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return new JTable();
		}
	}
	/**
	 * 对象自身
	 */
	MainFrame mf = this;
	/**
	 * 继承了JScrollPane类，添加了对文件地址的保存
	 * @author Zhang Hongning
	 */
	class FileJScrollPane extends JScrollPane {
		/**
		 * 序列化ID
		 */
		private static final long serialVersionUID = 19971121L;
		private String filepath = null;
		private Component component =null;
		private BufferedImage rawImage = null;
		/**
		 * 对象初始化
		 * @param fileText 要放入显示的JTextArea对象
		 * @param path 对应的文件地址
		 */
		public FileJScrollPane(Component newComponent, String path) {
			super(newComponent);
			filepath = path;
			component = newComponent;
		}
		public void setBufferedImage(BufferedImage image) {
			rawImage = image;
		}
		public BufferedImage getBufferedImage() {
			return rawImage;
		}
		/**
		 * 返回载入的TextArea对象
		 * @return TextArea Object
		 */
		public JTextArea getJTextArea() {
			return (component instanceof JTextArea) ? (JTextArea) component : null;
		}
		public JTable getJTable() {
			return (component instanceof JTable) ? (JTable) component: null;
		}
		public FigurePane getFigurePane() {
			return (component instanceof FigurePane) ? (FigurePane) component: null;
		}
		/**
		 * 返回载入的TextArea对象所对应的Text文件存储的绝对路径
		 * @return 
		 */
		public String getFilePath() {
			return filepath;
		}
		/**
		 * 设置文件对应的储存地址
		 * @param path 
		 */
		public void setFilePath(String path) {
			filepath = path;
		}
	}
	/**
	 * @return 返回当前选中的文件对象
	 */
	public File getSelectedFile () {
		return selected_file == null ? null: new File(selected_file);
	}
}