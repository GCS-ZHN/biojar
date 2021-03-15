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

package biojar.function.lwj.webcrawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import biojar.function.GeneralMethod;
import biojar.function.lwj.CommonMethod;
import biojar.function.lwj.DownloadProgress;
import biojar.function.lwj.Requests;

import com.alibaba.fastjson.JSONArray;

import java.io.LineNumberReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.io.PrintWriter;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * 从<a href="https://www.clinicaltrials.gov/">美国临床试验数据库</a>获取信息的业务处理类
 * @version 2.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class GetFromClinicalTrial {
	private boolean isCancelled = false;
	private DownloadProgress dp = new DownloadProgress("获取进度");
	public DownloadProgress getProgressBar() {
		return dp;
	}
	/**
	 * 返回是否取消运行获取NCT
	 * @return 是否取消运行获取NCT
	 */
	public boolean isCancelStatus() {
		return isCancelled;
	}
	/**
	 * 构造方法初始化dp窗口事件
	 */
	public GetFromClinicalTrial() {
		dp.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		dp.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent we) {//实现关闭按钮终止该线程
				int value = JOptionPane.showConfirmDialog(null, "确定要终止获取NCT信息吗？");
				if (value == JOptionPane.OK_OPTION) {
					cancel();
				}
			}
		});
	}
	/**
	 * 取消获取NCT
	 */
	public void cancel() {
		isCancelled = true;
	}
	/**
	 * 更新NCT信息field文件，数据源自<a href="https://clinicaltrials.gov/api/info/study_fields_list">ClinicalTrials.gov</a>
	 * @return 是否成功更新
	 */
	public static boolean updateField() {
		String fieldURL = "https://clinicaltrials.gov/api/info/study_fields_list";
		try {
			String fieldContent = new String(Requests.get(fieldURL));
			Pattern p = Pattern.compile("<Field Name=\"(\\w+?)\"/>");
			Matcher m = p.matcher(fieldContent);
			try (PrintWriter pw = new PrintWriter("configure/NCTFieldList.txt")) {
				while (m.find()) {
					pw.println(m.group(1));
				}
			}
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	/**
	 * 获取指定field的NCT信息
	 * @param inputfile NCT ID输入文件
	 * @param outputfile 结果输出文件
	 * @param fields 查询的NCT Field信息
	 * @throws IOException 输入输出异常
	 * @throws FileNotFoundException 文件未找到异常
	 * @throws Exception 其他异常
	 */
	@SuppressWarnings("unchecked")
	public void getNCTInfo(String inputfile, String outputfile, ArrayList <String> fields) throws IOException, FileNotFoundException, Exception {
		fields = (ArrayList<String>) fields.clone();
		String baseurl = "https://clinicaltrials.gov/api/query/study_fields?";
		HashMap <String, String> queryparam = new HashMap<>();
		if (!fields.contains("NCTId")) {
			fields.add("NCTId");
		}
		//
		try (LineNumberReader lnr = GeneralMethod.BufferRead(inputfile)) {
			new File(outputfile+".tmp").delete();
			new File("err.txt").delete();
			new File(outputfile+".tmp").deleteOnExit();
			@SuppressWarnings("unused")
			String tline = null;
			int total = 0;
			lnr.mark(1000000);
			while ((tline=lnr.readLine())!=null && !isCancelled) {
				total = lnr.getLineNumber();
			}
			lnr.reset();
			dp.setVisible(true);
			dp.now(lnr.getLineNumber()*100/total, String.valueOf(lnr.getLineNumber()), String.valueOf(total));
			try (PrintWriter pw = new PrintWriter(outputfile+".tmp")) {
				pw.print("Input ID\t");
				pw.println(GeneralMethod.join("\t", fields));
				String line = "";
				while (line != null && !isCancelled) {
					String[] nct = new String[50];//一次上传50个
					int i;
					for (i = 0; i<nct.length; i++) {
						line = lnr.readLine();
						if (line==null) break;
						nct[i] = line;
					}
					ArrayList <String> nctlist = new ArrayList<>();
					for (int index =0; index < i; index++) nctlist.add(nct[index]);
					String queryString = GeneralMethod.join("+OR+", nctlist);
					String fieldString = GeneralMethod.join("%2C", fields);
					queryparam.put("expr", queryString);
					queryparam.put("fields", fieldString);
					queryparam.put("fmt", "json");
					queryparam.put("max_rnk", String.valueOf(nct.length));
					queryparam.put("min_rnk", "");
					String url = baseurl + CommonMethod.TransMapToString(queryparam);

					String jsonString = new String(Requests.get(url));//获取信息
					JSONObject jo = JSON.parseObject(jsonString).getJSONObject("StudyFieldsResponse");
					JSONArray ja = jo.getJSONArray("StudyFields");
					HashMap <String, String> nct_info = new HashMap<>();
					for (Object obj: ja) {
						JSONObject record = (JSONObject) obj;
						int flag = 0;
						String info = "";
						String id = "";
						for (String o: fields) {
							if (flag > 0) info += "\t";
							Object content = record.get(o);
							if (content instanceof JSONArray) {
								content = GeneralMethod.join("; ", (JSONArray)content).replaceAll("\n", "\\\\n");
							} else if (content instanceof String) {
								content = ((String) content).replaceAll("\n", "\\\\n");
							}
							if (o.equals("NCTId")) id = String.valueOf(content);
							info += (content==""?".":content);
							flag++;
						}
						nct_info.put(id, info);
					}
					for (String id: nctlist) {
						pw.println(id + "\t" + nct_info.getOrDefault(id, "Not found"));
					}
					dp.now(lnr.getLineNumber()*100/total, String.valueOf(lnr.getLineNumber()), String.valueOf(total));
				}
			}
		}
		if (isCancelled) {
			new File(outputfile+".tmp").delete();
		} else {
			new File(outputfile).delete();
			new File(outputfile+".tmp").renameTo(new File(outputfile));
		}
		Thread.sleep(2000);
		dp.dispose();
	}
}
