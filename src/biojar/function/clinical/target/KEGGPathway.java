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

package biojar.function.clinical.target;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;

import static biojar.application.SettingFrame.getDefaultDelimiter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import biojar.function.GeneralMethod;
import biojar.function.lwj.DownloadProgress;
import biojar.function.lwj.Requests;

/**
 * 靶点评价五原则中KEGG信号通路处理的业务实现类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class KEGGPathway {
	/**
	 * 红色kegg图示
	 */
	public static final String RED = "red";
	/**
	 * 蓝色kegg图示
	 */
	public static final String BLUE = "blue";
	/**
	 * 运行状态值
	 */
	private boolean isCancelled = false;
	/**
	 * 返回运行状态
	 * @return 运行状态，若是被取消返回true
	 */
	public boolean isCancel() {
		return isCancelled;
	}
	/**
	 * 取消运行，中断循环.
	 */
	public void cancel() {
		isCancelled = true;
	}
	/**
	 * 从KEGG中获取下载KEGG Pathway图片
	 * @param inputfile 输入文件，里面有包含kegg pathway id列
	 * @param highlightColor 对蛋白进行颜色强调的颜色
	 * @param outputdir 输出图下载目录
	 * @param ishead 输入是否包含标题
	 * @param pathwayIdLoca 输入位置pathway id索引
	 * @param highlightTermIdLoca 输入位置要强调的蛋白索引
	 * @param dp 进度条
	 * @throws FileNotFoundException 文件不存在异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 其他异常
	 */
	public void downloadPathwayFigure(
		String inputfile,String highlightColor, String outputdir, 
		boolean ishead, int pathwayIdLoca, int highlightTermIdLoca,
		DownloadProgress dp
	) throws  FileNotFoundException, IOException, Exception {
		
		if (new File(outputdir).exists() && new File(outputdir).isDirectory()) {
			if (JOptionPane.showConfirmDialog(null, "Output directory exists, overwrited it?") == JOptionPane.OK_OPTION) {
				GeneralMethod.removeDirectory(new File(outputdir));
				new File(outputdir).mkdir();
			}
		} else if (!new File(outputdir).exists()) {
			new File(outputdir).mkdir();
		} else {
			outputdir = outputdir + "(1)";
			new File(outputdir).mkdir();
			JOptionPane.showMessageDialog(null, "Files with the same name exists! mkdir with new name: "+ outputdir);
		}
		ArrayList <String[]> inputIDs = new ArrayList<>();
		try (LineNumberReader lnr = GeneralMethod.BufferRead(inputfile)) {
			String line;
			while ((line = lnr.readLine())!=null) {
				if (ishead && lnr.getLineNumber() == 1) continue;
				String[] tmp = line.split(getDefaultDelimiter());
				inputIDs.add(tmp);
			}
		}
		int index = 0;
		dp.setVisible(true);
		dp.now(index*100/inputIDs.size(), String.valueOf(index), String.valueOf(inputIDs.size()));
		for (index =0; index < inputIDs.size();index++) {
			if (isCancelled) break;
			String pathwayId =  inputIDs.get(index)[pathwayIdLoca];
			String highlightTermId = null;
			if (highlightTermIdLoca >= 0) {
				highlightTermId = inputIDs.get(index)[highlightTermIdLoca];
			}
			downloadPathwayFigure(pathwayId, highlightTermId, highlightColor, outputdir);
			dp.now((index+1)*100/inputIDs.size(), String.valueOf(index+1), String.valueOf(inputIDs.size()));
		}
	}
	/**
	 * 下载特定pathway并用颜色突出强调蛋白
	 * @param pathwayId KEGG Pathway id
	 * @param highlightTermId KEGG Entry id to be highlighted
	 * @param highlightColor 强调色
	 * @param outputdir 输出目录
	 * @throws IOException 文件输入输出异常
	 * @throws Exception 其他未知异常
	 */
	public  void downloadPathwayFigure(String pathwayId, String highlightTermId, String highlightColor, String outputdir) throws IOException ,Exception {
		String webURL = "https://www.kegg.jp/kegg-bin/show_pathway?"+pathwayId;
		String outputfilename = outputdir + "/" + pathwayId + ".png";
		if (highlightColor != null && highlightTermId!= null) {
			webURL = webURL +"/" + highlightTermId + "%09" + highlightColor;
			outputfilename = outputdir + "/" + pathwayId + "_" + highlightTermId.replace(":", "_") + ".png";
		}
		String webPage = new String(Requests.get(webURL));
		Pattern p = Pattern.compile("<img src=\"(.+?png)\"", Pattern.DOTALL);
		Matcher m = p.matcher(webPage);
		if (m.find()) {
			String downloadURL = "https://www.kegg.jp"+m.group(1);
			Requests.download(downloadURL, outputfilename, true);
		}
	}
	/**
	 * 获取KEGG Entries的Pathway
	 * @param inputfile 输入文件
	 * @param outputfile 输出文件
	 * @param entryindex 输入文件中entry id所在列索引，从0开始
	 * @param ishead 输入文件是否包含标题
	 * @param dp 下载进度条
	 * @throws FileNotFoundException 文件未找到异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 其他异常
	 */
	public void getKEGGPathwaybyEntry(String inputfile, String outputfile, int entryindex, boolean ishead, DownloadProgress dp) throws FileNotFoundException, IOException, Exception {
		String baseurl = "http://rest.kegg.jp/link/pathway/";
		new File(outputfile).delete();
		int total = 0;
		try (LineNumberReader lnr = GeneralMethod.BufferRead(inputfile)) {
			while (lnr.readLine()!=null) {
				total = lnr.getLineNumber();
			}
			if (ishead) total -= 1;
		}
		dp.setVisible(true);
		dp.now(0, "0", String.valueOf(total));
		try (LineNumberReader lnr = GeneralMethod.BufferRead(inputfile)) {
			String line;
			while (!isCancelled && (line = lnr.readLine()) != null) {
				try (PrintWriter pw = new PrintWriter(new FileOutputStream(outputfile, true))) {
					if (ishead && lnr.getLineNumber()==1) continue;
					int now = lnr.getLineNumber();
					if (ishead) now -= 1;
					String[] tmp = line.split(getDefaultDelimiter());
					if (entryindex<0 && entryindex >= tmp.length) entryindex = 0;
					String entry = tmp[entryindex];
					String content= new String(Requests.get(baseurl+entry));
					for (String thisline: content.split("\n")) {
						String keggpathwayid = thisline.split("\t")[1].replace("path:", "");
						pw.println(line + "\t" + keggpathwayid + "\t" + getKEGGPathwayName(keggpathwayid));
					}
					dp.now(now*100/total, String.valueOf(now), String.valueOf(total));
				}
			}
		}
		if (isCancelled) {
			new File(outputfile).delete();
		}
	}
	/**
	 * 获取特定KEGG Pathway的名称
	 * @param keggid 输入的KEGG Pathway id
	 * @return KEGG Pathway名称字符串
	 * @throws Exception 未知异常
	 */
	public String getKEGGPathwayName(String keggid) throws Exception {
		return new String(Requests.get("http://rest.kegg.jp/list/" + keggid)).split("\t")[1].replace("\n", "");
	}
}