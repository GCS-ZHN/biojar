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
import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.LineNumberReader;

import static biojar.application.SettingFrame.getDefaultDelimiter;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JOptionPane;

import biojar.function.GeneralMethod;
import biojar.function.lwj.Requests;
/**
 * 靶点评价五原则中靶点相互作用计算的业务处理类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class InteractionCalculate {
	/**
	 * 储存相互作用蛋白对
	 */
	private final HashMap <String, HashSet <String>> protein_map = new HashMap<>();
	/**
	 * 储存蛋白的degree值，该值指的是与该蛋白直接相互作用的蛋白个数
	 */
	private final HashMap <String, Integer> degree = new HashMap<>();
	/**
	 * 储存蛋白的neighborhoodconnectivity值，该值指的是与该蛋白直接相互作用蛋白的degree值平均
	 */
	private final HashMap <String, Double> neighbor = new HashMap<>();
	/**
	 * 默认的相互作用蛋白对输入源
	 */
	private String defaultDataSource = "input/protein.links.txt";
	/**
	 * 相互作用结合分数的阈值
	 */
	private int score_threshold = 950;
	/**
	 * 设置combine score的threshold值
	 * @param newthreshold 新的threshold值
	 */
	public void setThreshold(int newthreshold) {
		score_threshold = newthreshold;
	}
	/**
	 * 加载相互作用蛋白对输入源，默认源
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 */
	public void loadData() throws FileNotFoundException, IOException {
		loadData(defaultDataSource, true);
	}
	/**
	 * 加载相互作用蛋白对输入源，指定文件输入源
	 * @param inputfile 指定文件输入源
	 * @param head_included 指定文件输入源是否包含标题，true为包含
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 */
	public void loadData(String inputfile, boolean head_included) throws FileNotFoundException, IOException {
		ArrayList <String> cols = new ArrayList<String>();
		try (LineNumberReader lnr = GeneralMethod.BufferRead(inputfile)) {
			String line = null;
			while ((line = lnr.readLine()) != null) {
				if (head_included && lnr.getLineNumber() == 1) {
					continue;
				}
				cols.add(line);
			}
		}
		for (int i = 0; i < cols.size(); i++) {
			String[] line = cols.get(i).split(getDefaultDelimiter());
			if (Integer.parseInt(line[2]) < score_threshold) continue;
			protein_map.putIfAbsent(line[0], new HashSet<String>());
			protein_map.get(line[0]).add(line[1]);
		}
	}
	/**
	 * 计算蛋白的degree值，默认源加载数据，该值指的是与该蛋白直接相互作用的蛋白个数
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 */
	public void calculateDegree() throws FileNotFoundException, IOException {
		calculateDegree(defaultDataSource, true);
	}
	/**
	 * 计算蛋白的degree值，指定源加载数据，该值指的是与该蛋白直接相互作用的蛋白个数
	 * @param inputfile 指定的数据加载源
	 * @param head_included 指定的数据加载源是否包含标题，true为包含
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 */
	public void calculateDegree(String inputfile, boolean head_included) throws FileNotFoundException, IOException {
		if (degree.isEmpty()) {
			if (protein_map.isEmpty()) loadData(inputfile, head_included);
			for (String protein1: protein_map.keySet()) degree.put(protein1, protein_map.get(protein1).size());
		}
	}
	/**
	 * 计算蛋白的neighborhoodconnectivity值，默认源加载数据，该值指的是与该蛋白直接相互作用蛋白的degree值平均
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @see InteractionCalculate#getNeighborhoodConnectivityMap
	 */
	public void calculateNeighborhoodConnectivity() throws FileNotFoundException, IOException {
		calculateNeighborhoodConnectivity(defaultDataSource, true);
	}
	/**
	 * 计算蛋白的neighborhoodconnectivity值，指定源加载数据，该值指的是与该蛋白直接相互作用蛋白的degree值平均
	 * @param inputfile 指定的数据加载源
	 * @param head_included 指定的数据加载源是否包含标题，true为包含
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @see InteractionCalculate#getNeighborhoodConnectivityMap
	 */
	public void calculateNeighborhoodConnectivity(String inputfile, boolean head_included) throws FileNotFoundException, IOException {
		if (protein_map.isEmpty() || degree.isEmpty()) calculateDegree(inputfile, head_included);
		for (String protein1: protein_map.keySet()) {
			Double nc = 0.0;
			for (String protein2: protein_map.get(protein1)) {
				nc += degree.get(protein2);
			}
			nc = nc/protein_map.get(protein1).size();
			neighbor.put(protein1, nc);
		}
	}
	/**
	 * 返回计算得到的degree值HashMap，若未计算，则用默认源计算后返回
	 * @return 计算得到的degree值HashMap, Key为蛋白ENSP ID, Value为degree值
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @see calculateDegree()
	 * @see calculateDegree(String inputfile, boolean head_included)
	 */
	public HashMap <String, Integer> getDegreeMap() throws FileNotFoundException, IOException {
		if (degree.isEmpty()) calculateDegree();
		return degree;
	}
	/**
	 * 返回计算得到的neighborhoodconnectivity值HashMap，若未计算，则用默认源计算后返回
	 * @return 计算得到的neighborhoodconnectivity值HashMap， Key为蛋白ENSP ID，Value为neighborhoodconnectivity值
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @see InteractionCalculate#calculateNeighborhoodConnectivity()
	 * @see InteractionCalculate#calculateNeighborhoodConnectivity(String, boolean)
	 */
	public HashMap <String, Double> getNeighborhoodConnectivityMap() throws FileNotFoundException, IOException {
		if (neighbor.isEmpty()) calculateNeighborhoodConnectivity();
		return neighbor;
	}
	/**
	 * 从String database上更新default源
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public void updateSource() throws IOException, Exception {
		String web = new String(Requests.get("https://stringdb-static.org/cgi/download.pl?species_text=Homo+sapiens"));
		Pattern p = Pattern.compile("Interaction Data.+?href=\"(.+?)\"", Pattern.DOTALL);
		Matcher m = p.matcher(web);
		if (m.find()) {
			String url = m.group(1);
			String[] tmp = url.split("/");
			String filename = "input/"+ tmp[tmp.length - 1];
			Requests.download(url, filename, true);
			GeneralMethod.ungzip(filename, defaultDataSource);//解压下载的gz文件
			
		} else {
			JOptionPane.showMessageDialog(null, "No sources found! Please check it!");
		}
	}
}