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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * 文件树面板
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class TreePanel extends JScrollPane {
	/**
	 * 序列化ID
	 */
	private static final long serialVersionUID = 202011061916L;
	/**
	 * 树组件
	 */
	private JTree tree = null;
	/**
	 * 树根节点
	 */
	private FileTreeNode rootTreeNode;
	/**
	 * 右键弹出菜单
	 */
	private JPopupMenu popupMenu = new JPopupMenu();
	/**
	 * 主程序
	 */
	private MainFrame mf;
	/**
	 * 树加载与储存对象
	 */
	private File initFile = new File(".init/tree.obj");
	/**
	 * 构造方法
	 * @param rootFile 树根节点文件
	 * @param mf 主程序对象
	 */
	public TreePanel(File rootFile, MainFrame mf) {
		super();
		initComponent(rootFile);
		this.mf = mf;
	}
	/**
	 * 初始化方法
	 * @param rootFile 根节点文件
	 */
	private final void initComponent(File rootFile) {
		//加载默认树
		if (!initFile.getParentFile().exists()) initFile.getParentFile().mkdir();
		else if (initFile.getParentFile().isFile()) {
			initFile.getParentFile().delete();
			initFile.getParentFile().mkdir();
			
		}
		try {
			if (initFile.exists() && initFile.isFile()) {
				ObjectInputStream ois;
				ois = new ObjectInputStream(new FileInputStream(initFile));
				Object inputObject = ois.readObject();
				ois.close();
				if (inputObject instanceof FileTreeNode) {
					rootTreeNode = (FileTreeNode) inputObject;
				} else {
					rootTreeNode = FileTreeNode.createTreeNodebyFile(rootFile);
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(initFile));
					oos.writeObject(rootTreeNode);
					oos.close();
				}
			} else {
				rootTreeNode = FileTreeNode.createTreeNodebyFile(rootFile);
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(initFile));
				oos.writeObject(rootTreeNode);
				oos.close();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		tree = new JTree(rootTreeNode);
		
		//菜单配置
		JMenuItem refreshItem = new JMenuItem("Refresh");
		popupMenu.add(refreshItem);
		refreshItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		
		JMenuItem returnItem = new JMenuItem("Return");
		popupMenu.add(returnItem);
		returnItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				returnParent();
			}
		});
		tree.setComponentPopupMenu(popupMenu);
		
		
		tree.setRootVisible(false);//不显示根节点
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);//任意选择模式
		tree.addTreeSelectionListener(new TreeSelectionListener() {//选择文件时打开文件
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				TreePath[] selecTreePaths = tree.getSelectionPaths(); //选中的所有节点形成的TreePath
				if (selecTreePaths == null) return;
				if (selecTreePaths.length == 1) {
					//每个TreePath对象包含了从根节点到当前选中节点的所有节点对象，LastPathComponent正是当前选中节点
					FileTreeNode node =(FileTreeNode) selecTreePaths[0].getLastPathComponent();
					File file = node.getFile();
					if (file.isFile()) {
						action(file);
					}
				}
				
			}
		});
		setViewportView(tree);
	}
	/**
	 * 刷新树对象
	 */
	public void refresh () {
		File file = mf.getSelectedFile();
		file = file == null?rootTreeNode.getFile():file.getParentFile();
		refresh(file);
	}
	/**
	 * 用指定根文件刷新对象
	 * @param file 待刷新的文件对象
	 */
	public void refresh(File file) {
		if (file == null) return;
		rootTreeNode = FileTreeNode.createTreeNodebyFile(file);
		tree.setModel(new DefaultTreeModel(rootTreeNode));
		tree.repaint();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(initFile));
			oos.writeObject(rootTreeNode);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 对点击文件做出反应
	 * @param nodeFile 节点对应的文件对象
	 */
	public void action(File nodeFile) {
		mf.reloadOrOpen(nodeFile);
	}
	/**
	 * 返回父目录节点
	 */
	public void returnParent() {
		File rootFile = rootTreeNode.getFile();
		if (rootFile!= null && rootFile.getParentFile() != null) {
			refresh(rootFile.getParentFile());
		}
	}
}


/**
 * 文件专用树节点，需要序列化而定义成外部类。内部类在其他类中序列化会容易出错
 * @author 张洪宁
 * @version 1.0
 */
class FileTreeNode extends DefaultMutableTreeNode {
	/**
	 * 序列化对象ID
	 */
	private static final long serialVersionUID = 202011061917L;
	/**
	 * 根目录文件
	 */
	private File rootFile;
	/**
	 * 构造方法，设为私有而只能通过createTreeNodebyFile静态方法构建
	 * @param rootFile 根节点目录
	 * @param hasChild 是否有子节点
	 */
	private FileTreeNode(File rootFile, boolean hasChild) {
		super(rootFile.getName(), hasChild);
		this.rootFile = rootFile;
	}
	/**
	 * 获取对应的节点文件对象
	 * @return 节点文件对象
	 */
	public File getFile() {
		return rootFile;
	}
	/**
	 * 设置对应的节点文件对象
	 * @param rootFile 节点文件对象
	 */
	public void setFile(File rootFile) {
		this.rootFile = rootFile;
		setUserObject(rootFile.getName());
	}
	/**
	 * 根据指定根节点文件对象创建文件树节点
	 * @param rootFile 根节点文件对象
	 * @return 文件树节点
	 */
	public static FileTreeNode createTreeNodebyFile(File rootFile) {
		FileTreeNode rootTreeNode = null;
		if (rootFile == null||!rootFile.exists()) return rootTreeNode;
		if (rootFile.isFile()) {
			rootTreeNode = new FileTreeNode(rootFile, false);
		} else {
			rootTreeNode = new FileTreeNode(rootFile, true);
			File[] filelistFiles = rootFile.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isHidden()) return false;
					if (pathname.isDirectory() && pathname.listFiles()==null) return false;
					return true;
				}
			});
			if (filelistFiles == null) return rootTreeNode;
			for (File childFile: filelistFiles) {
				DefaultMutableTreeNode childTredNode = createTreeNodebyFile(childFile);
				if (childTredNode != null) {
					rootTreeNode.add(childTredNode);
				}
			}
		}
		return rootTreeNode;
	}
}
