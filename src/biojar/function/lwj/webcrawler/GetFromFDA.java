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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.LineNumberReader;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import biojar.function.GeneralMethod;
import biojar.function.lwj.DownloadProgress;
import biojar.function.lwj.Requests;

import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

/**
 * <a href="https://www.fda.gov/">从美国食品药品监督管理局（FDA）</a>获取信息的业务处理类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class GetFromFDA {
	private boolean isCancelled = false;
	/**
	 * 返回运行状态，若是被取消返回true
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
	 * 从FDA获取指定输入Lisence的药物首次批准时间
	 * @param inputfile 输入文件，每行为药物的NDA/BLA/ANDA ID
	 * @param outputfile 输出文件，每行为药物的NDA/BLA/ANDA ID + 首次批准时间
	 * @throws IOException 输入输出异常
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws Exception 未知异常
	 */
	public void getOriginalApprovalDate(String inputfile, String outputfile) throws IOException, FileNotFoundException, Exception {
		getOriginalApprovalDate(inputfile, outputfile, new DownloadProgress("获取进度"));
	}
	/**
	 * 从FDA获取指定输入Lisence的药物首次批准时间
	 * @param inputfile 输入文件，每行为药物的NDA/BLA/ANDA ID
	 * @param outputfile 输出文件，每行为药物的NDA/BLA/ANDA ID + 首次批准时间
	 * @param dp 进度条对象
	 * @throws IOException 输入输出异常
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws Exception 未知异常
	 */
	public void getOriginalApprovalDate(String inputfile, String outputfile, DownloadProgress dp) throws IOException, FileNotFoundException, Exception {
		String url = "https://www.accessdata.fda.gov/scripts/cder/daf/index.cfm?event=BasicSearch.process";
		new File(outputfile+".tmp").delete();
		new File(outputfile+".tmp").deleteOnExit();
		LineNumberReader lnr = GeneralMethod.BufferRead(inputfile);
		String line = null;
		String FDAtype = "ANDA NDA BLA";
		int total = 0;
		lnr.mark(1000000);
		while ((line=lnr.readLine())!=null && !isCancelled) {
			total = lnr.getLineNumber();
		}
		lnr.reset();
		dp.setVisible(true);
		dp.now(lnr.getLineNumber()*100/total, String.valueOf(lnr.getLineNumber()), String.valueOf(total));
		while  ((line=lnr.readLine())!=null && !isCancelled) {
			PrintWriter pw = new PrintWriter(new FileOutputStream(outputfile+".tmp", true));
			Pattern lsp = Pattern.compile("^(.+?)(\\d+)$");
			Matcher lsm = lsp.matcher(line);
			boolean isfound = false;
			if (lsm.find() && FDAtype.indexOf(lsm.group(1)) >= 0) {
				Map<String, String> data = new HashMap<String, String>();
				data.put("searchterm", lsm.group(2));
				data.put("search", "");
				String web = new String(Requests.post(url, data));
				Pattern p = Pattern.compile("summary=\"Original Approvals or Tentative Approvals.+?>(.+?)<\\/table>", Pattern.DOTALL);
				Pattern np = Pattern.compile("Your Drugs@FDA Search Did Not Return Any Results", Pattern.DOTALL);
				Matcher m = p.matcher(web);
				Matcher nm = np.matcher(web);
				if (m.find()) {
					String table = m.group(1);
					Pattern rowp = Pattern.compile("<tr>(.+?)<\\/tr>", Pattern.DOTALL);
					Matcher rowm = rowp.matcher(table);
					
					while (rowm.find()) {
						String row = rowm.group(1);
						Pattern cellp = Pattern.compile("<td>(\\d{2}\\/\\d{2}\\/\\d{4})<\\/td>", Pattern.DOTALL);
						Matcher cellm = cellp.matcher(row);
						if (cellm.find()) {
							LocalDate thisld = LocalDate.parse(cellm.group(1), DateTimeFormatter.ofPattern("MM/dd/yyyy"));
							pw.println(line+"\t"+thisld);
							isfound = true;
						}
					}
				} else if (nm.find()) {
					pw.println(line+"\t"+nm.group(0));
					isfound = true;
				}
			}
			if (!isfound) {
				pw.println(line+"\t"+"Not get for unknown reason");
			}
			pw.close();
			dp.now(lnr.getLineNumber()*100/total, String.valueOf(lnr.getLineNumber()), String.valueOf(total));
		}
		lnr.close();
		if (isCancelled) {
			new File(outputfile+".tmp").delete();
		} else {
			new File(outputfile).delete();
			new File(outputfile+".tmp").renameTo(new File(outputfile));
		}
	}
}
