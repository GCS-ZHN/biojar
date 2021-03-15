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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;

import static biojar.application.SettingFrame.getDefaultDelimiter;
import static biojar.application.SettingFrame.setDefaultDelimiter;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.ArrayList;

import java.util.regex.*;

import biojar.function.GeneralMethod;
import biojar.function.lwj.DownloadProgress;
/**
 * 靶点评价五原则中组织分布计算的业务实现类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class Tissue {
	/**
	 * 默认数据库源文件
	 */
	private final String DEFAULT_DATABASE = "database/tissue/05-TissueDB-diff-percent-1.out";
	/**
	 * 数据库HashMap
	 */
	private final HashMap <String, String[]> DB = new HashMap<>(); 
	/**
	 * 数据库未加载异常
	 */
	private class DatabaseUnloadException extends Exception {
		/**
		 * 序列化ID
		 */
		private static final long serialVersionUID = 202102041427L;

		public DatabaseUnloadException() {
			super("Database hasn't be loaded yet or loaded failed!");
		}
	}
	/**
	 * 从默认数据库源加载数据库
	 * @return boolean值，true为加载成功，false为加载失败
	 */
	public boolean loadDatabase() {
		String default_delimiter_bak = getDefaultDelimiter();
		setDefaultDelimiter("\t");//默认数据库源用的是tab为分隔符
		boolean res = loadDatabase(DEFAULT_DATABASE);
		setDefaultDelimiter(default_delimiter_bak);
		return res;
	}
	/**
	 * 从文件中加载数据库
	 * @param filename 数据库源文件
	 * @return boolean值，true为加载成功，false为加载失败
	 */
	public boolean loadDatabase(String filename) {
		DB.clear();//clear for reload database
		try (LineNumberReader lnr = GeneralMethod.BufferRead(filename)) {
			String line;
			while ((line = lnr.readLine()) != null) {
				String[] arr = line.split(getDefaultDelimiter());
				if (arr.length > 2) {
					String id = arr[1].toLowerCase();
					String[] all_tissues = new String[arr.length - 2];
					for (int index = 0; index < all_tissues.length; index ++) all_tissues[index] = arr[index + 2];
					DB.put(id, all_tissues);
				}
			}
			return true;
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
			return false;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * 对单个蛋白，用其Uniprot accession查询其组织分布，结果已经被normalized
	 * @param uniprot_ac Uniprot AC号
	 * @return String对象，包括输入的uniprot_ac，组织个数，组织名称和分布分数，若没有结果者后三者以“.”显示
	 * @throws DatabaseUnloadException 数据未上传异常
	 */
	public String queryTissueDistribution(String uniprot_ac) throws DatabaseUnloadException {
		if (DB.isEmpty()) throw new DatabaseUnloadException();
		if (DB.containsKey(uniprot_ac.toLowerCase())) {
			String[] all_tissues = DB.get(uniprot_ac.toLowerCase());
			String[] tissues = all_tissues[all_tissues.length - 1].split("\\|\\|");
			ArrayList <Double> percents = new ArrayList<>();
			ArrayList <String> tissue_names = new ArrayList<>();
			Pattern p = Pattern.compile("\\(([0-9\\.]+)\\) (.+)$");
			for (String tissue: tissues) {
				if (tissue.contains("embryo or fetus")) continue;
				Matcher m = p.matcher(tissue);
				if (m.find()) {
					percents.add(Double.parseDouble(m.group(1)));
					tissue_names.add(m.group(2));
				} else {
					return uniprot_ac + "\t" + ".\t.";
				}
			}
			ArrayList <String> tissue_out = new ArrayList<>();
			for (int i = 0; i < percents.size(); i++) {
				if (percents.get(i) >= 5.141388174807198) {
					tissue_out.add(String.format("(%.2f) %s", percents.get(i), tissue_names.get(i)));
				}
			}
			try {
				return uniprot_ac + "\t" + tissue_out.size() + "\t" + GeneralMethod.join("||", tissue_out);
			} catch (Exception e) {
				e.printStackTrace();
				return uniprot_ac + "\t" + ".\t.";
			}
		} else {
			return uniprot_ac + "\t" + ".\t.";
		}
	}
	/**
	 * 查询输入文件中蛋白的组织分布
	 * @param inputfile 输入文件对象
	 * @param outputfile 输出文件对象
	 * @param index_ac 输入文件中uniprot accession所在列索引（从0开始）
	 * @param containtitle 输入文件是否包含标题，true为包含，false为不包含
	 * @param dp 查询进度条
	 */
	public void queryTissueDistribution(File inputfile, File outputfile, int index_ac, boolean containtitle, DownloadProgress dp) {
		try {
			int total = 0;
			int now = 0;
			try (LineNumberReader lnr = GeneralMethod.BufferRead(inputfile)) {
				@SuppressWarnings("unused")
				String line;
				while ((line = lnr.readLine()) != null) {
					if (containtitle && lnr.getLineNumber() == 1) continue;
					total ++;
				}
			}
			if (dp != null) dp.setVisible(true);
			if (dp != null) dp.now(now*100/total, "" + now, "" + total);
			try (LineNumberReader lnr = GeneralMethod.BufferRead(inputfile)) {
				try (PrintWriter pw = new PrintWriter(outputfile)) {
					String line;
					while ((line = lnr.readLine()) != null) {
						if (containtitle && lnr.getLineNumber() == 1) continue;
						String[] arr = line.split(getDefaultDelimiter());
						String uniprot_ac = arr[index_ac];
						pw.println(line + "\t" + queryTissueDistribution(uniprot_ac));
						now++;
						if (dp != null) dp.now(now*100/total, "" + now, "" + total);
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 查询输入文件中蛋白的组织分布
	 * @param inputfile 输入文件名
	 * @param outputfile 输出文件名
	 * @param index_ac 输入文件中uniprot accession所在列索引（从0开始）
	 * @param containtitle 输入文件是否包含标题，true为包含，false为不包含
	 * @param dp 查询进度条
	 */
	public void queryTissueDistribution(String inputfile, String outputfile, int index_ac, boolean containtitle, DownloadProgress dp) {
		queryTissueDistribution(new File(inputfile), new File(outputfile), index_ac, containtitle, dp);
	}
	/*
	public static void main(String[] args) {
		Tissue tissue = new Tissue();
		tissue.loadDatabase();
		//System.out.println(tissue.queryTissueDistribution("P25021"));
		tissue.queryTissueDistribution("input/tissue_uniprot_id.txt", "output/tissue_output.txt", 1, false, new DownloadProgress("查询进度"));
	}*/
}