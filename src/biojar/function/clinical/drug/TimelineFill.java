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

package biojar.function.clinical.drug;
import java.util.*;
import java.util.regex.*;

import biojar.function.GeneralMethod;

import static biojar.application.SettingFrame.getDefaultDelimiter;

import java.io.*;

/**
 * 药物临床发展时间线填充业务处理类
 * @version 2.8
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class TimelineFill {
	/**
	 * 代表timeline的开始时间年份
	 */
	private static int start_year = 1990;
	/**
	 * 代表timeline的结束时间年份
	 */
	private static int end_year = 2019;
	/**
	 * 填充平均时间，phase 1为18月，以此类推
	 */
	private static final int[] AVE_TIME= {18, 30, 30, 18};
	/**
	 * 填充的label值
	 */
	private static String label = "Program filled";
	/**
	 * 设置属性值
	 * @param attributeName 属性名称
	 * @param attributeValue 属性值
	 * @throws Exception 未知异常
	 */
	public static void setAttribute(String attributeName, Object attributeValue) throws Exception {
		if (attributeName.equals("timeline start year")) {
			if (attributeValue instanceof Integer) {
				start_year = (int) attributeValue;
			} else {
				throw new Exception("attributeValue should be a int");
			}
		} else if (attributeName.equals("timeline end year")) {
			if (attributeValue instanceof Integer) {
				end_year = (int) attributeValue;
			} else {
				throw new Exception("attributeValue should be a int");
			}
		} else if (attributeName.equals("filled label")) {
			if (attributeValue instanceof String) {
				label = (String) attributeValue;
			} else {
				throw new Exception("attributeValue should be a String");
			}
		} else {
			throw new Exception("undefined attributeName");
		}
	}
	/**
	 * 将一条指定的month timeline按规则填充
	 * @param tl month timeline字符串
	 * @param for_lead 对lead timeline还是all timeline进行填充
	 * @return 填充后的month timeline字符串
	 * @throws Exception 未知异常
	 */
	public static String Fill(String tl, boolean for_lead) throws Exception {
		String[] tmp = tl.split(getDefaultDelimiter());
		String id = tmp[0]+"\t"+tmp[1];
		ArrayList <String> timeline = new ArrayList<String>();
		for (int index = 2; index < (for_lead?tmp.length:tmp.length - 1); index++) {
			timeline.add(tmp[index]);
		}
		int y0 = timeline.size() -1;
		int y1, y2, y3, y4;
		boolean dy1, dy2, dy3, dy4;
		y1=y2=y3=y4=-1;
		dy1=dy2=dy3=dy4=false;
		/**
		 * 判断是否存在特定phase
		 */
		for (int index= 0; index <= y0; index++) {
			if (Pattern.matches("^.*?/?P4.+?\\(.+$", timeline.get(index))) {
				y4 = index;
				dy4 = true;
				break;
			}
		}
		y0 = (dy4?y4:y0);
		for (int index= 0; index <= y0; index++) {
			if (Pattern.matches("^.*?/?P3.+?\\(.+$", timeline.get(index))) {
				y3 = index;
				dy3 = true;
				break;
			}
		}
		y0 = (dy3?y3:y0);
		for (int index= 0; index <= y0; index++) {
			if (Pattern.matches("^.*?/?P2.+?\\(.+$", timeline.get(index))) {
				y2 = index;
				dy2 = true;
				break;
			}
		}
		y0 = (dy2?y2:y0);
		for (int index= 0; index <= y0; index++) {
			if (Pattern.matches("^.*?/?P1.+?\\(.+$", timeline.get(index))) {
				y1 = index;
				dy1 = true;
				break;
			}
		}
		ArrayList <int[]> change = new ArrayList<int[]>();
		int[][] cset = {
			{y1, y2},
			{y2, y3},
			{y3, y4}
		};
		boolean[][] csetflag = {
			{dy1, dy2},
			{dy2, dy3},
			{dy3, dy4}
		};
		for (int index=0; index <3; index++) {
			boolean or = (csetflag[index][1]||csetflag[index][0]);
			boolean and = (csetflag[index][1]&&csetflag[index][0]);
			if (or && !and) {//即一个定义一个未定义
				/*
				用正负值判断升降transition
				*/
				int val = (csetflag[index][1]?cset[index][1]:-cset[index][0]);
				int[] changepair = {index + 1, val};
				change.add(changepair);
			}
		}
		ArrayList <int[]> start_end = new ArrayList<int[]>();//诸如P2-P4的情况
		if (!change.isEmpty() && change.get(0)[1] > 0) {
			int pos = change.get(0)[1];
			int[] time_split = new int[change.get(0)[0]];
			for (int i = change.get(0)[0]-1; i >=0; i--) {
				time_split[change.get(0)[0]-1-i] = AVE_TIME[i];
			}
			for (int index = 0; index < time_split.length; index++) {
				String info = "P"+(change.get(0)[0] - index) +" ("+label+")";
				for (int j = (pos - time_split[index]); j <= (pos -1); j++) {
					if (j <0) continue;
					timeline.set(j, info);
				}
				pos -= time_split[index];
				if (change.size()==3) {
					start_end.add(change.get(1));
					start_end.add(change.get(2));
				}
			}
		} else if (change.size() >= 2) {
			start_end.add(change.get(0));
			start_end.add(change.get(1));
		}
		if (start_end.size() != 0) {
			int spend_time = start_end.get(1)[1] + start_end.get(0)[1];//注意下降用赋值，故取差值用+号
			int[] time_split = new int[start_end.get(1)[0] - start_end.get(0)[0] + 1];
			for (int i = (start_end.get(1)[0] - 1); i >=(start_end.get(0)[0] - 1); i--) {
				time_split[start_end.get(1)[0] - 1 -i] = AVE_TIME[i];
			}
			int pos = start_end.get(1)[1];
			int sum = GeneralMethod.sumNumber(time_split);
			for (int index = 0; index < time_split.length -1; index++) {
				String info = "P"+(start_end.get(1)[0] - index) + " ("+label+")";
				int time_per = time_split[index]*spend_time/sum;
				for (int j = (pos - time_per); j <= (pos -1); j++) {
					if (j <0) continue;
					timeline.set(j, info);
				}
				pos -= time_per;
			}
		}
		if (!for_lead) {
			timeline.add(tmp[tmp.length - 1]);
		}
		String res = GeneralMethod.join("\t", timeline);
		res = (id + "\t" + res);
		return res;
	}
	/**
	 * 将month timeline进行规则填充
	 * @param inputfile month timeline输入文件
	 * @param outputfile 填充后输出文件
	 * @param for_lead 对lead timeline还是all timeline进行填充
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public static void fillMonthTimeline(String inputfile, String outputfile, boolean for_lead) throws FileNotFoundException, IOException, Exception {
		LineNumberReader lnr = GeneralMethod.BufferRead(inputfile);
		PrintWriter pw = new PrintWriter(outputfile);
		String line = null;
		while ((line = lnr.readLine())!=null) {
			if (lnr.getLineNumber() > 1) line = Fill(line, for_lead);
			pw.println(line);
		}
		lnr.close();
		pw.close();
	}
	/**
	 * 将month Timeline转化为year timeline
	 * @param inputfile month timeline输入文件
	 * @param outputfile year timeline输出文件
	 * @param for_lead 是lead还是all timeline
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public static void convertToYearTimeline(String inputfile, String outputfile, boolean for_lead) throws FileNotFoundException, IOException, Exception {
		LineNumberReader lnr = GeneralMethod.BufferRead(inputfile);
		PrintWriter pw = new PrintWriter(outputfile);
		String line = null;
		while ((line = lnr.readLine())!=null) {
			String[] tmp = line.split(getDefaultDelimiter());
			if (lnr.getLineNumber() > 1) {
				line = shortTimeline(tmp, 2, (for_lead?tmp.length:tmp.length - 1), 12);
				pw.println(line);
			} else {
				pw.print(tmp[0]+"\t"+tmp[1]+"\t");
				for (int year = start_year; year <=end_year; year++) pw.print("\t"+year);
				if (!for_lead) {
					pw.print("\tDisease class");
				}
				pw.println();
			}
			
		}
		lnr.close();
		pw.close();
	}
	/**
	 * 生成填充的year timeline
	 * @param inputfile 未填充的month timeline输入
	 * @param outputfile 填充的year timeline输出
	 * @param for_lead 是lead还是all timeline
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public static void fillYearTimeline(String inputfile, String outputfile, boolean for_lead) throws FileNotFoundException, IOException, Exception {
		fillMonthTimeline(inputfile, "tmp", for_lead);
		convertToYearTimeline("tmp", outputfile, for_lead);
		new File("tmp").delete();
	}
	/**
	 * 将timeline按指定起始位置和步长进行压缩合并
	 * @param tl 未压缩的timeline
	 * @param startIndex 压缩起始索引
	 * @param endIndex 压缩终止索引（不包含）
	 * @param step 压缩步长
	 * @return 压缩后的timeline字符串
	 * @throws Exception 未知异常
	 */
	public static String shortTimeline(String[] tl, int startIndex, int endIndex, int step) throws Exception {
		HashMap<String, Double> numericalphase = new HashMap<String, Double>();
		numericalphase.put(".",0.0);
		numericalphase.put("Discontinued",0.0);
		numericalphase.put("P12",1.1);
		numericalphase.put("P1",1.0);
		numericalphase.put("P2",2.0);
		numericalphase.put("P3",3.0);
		numericalphase.put("P4",4.0);
		numericalphase.put("P1/P2",1.5);
		numericalphase.put("P2/P3",2.5);
		numericalphase.put("P3/P4",3.5);
		ArrayList <String> sTimeline = new ArrayList<String>();
		for (int index = 0; index < startIndex; index++) {
			sTimeline.add(tl[index]);
		}
		for (int index = startIndex; index < endIndex; index += step) {
			Pattern pattern = Pattern.compile("^(.+?) \\((.+)\\)$");
			Pattern sep = Pattern.compile("; |;");
			String highestPhase = ".";
			HashSet <String> highestRef = new HashSet<String>();//去重
			int end = (endIndex>=(index + step)?(index + step): endIndex);
			for (int subIndex = index; subIndex < end; subIndex++) {
				String thisPhase, thisRef;
				Matcher m = pattern.matcher(tl[subIndex]);
				if (m.find()) {
					thisPhase = m.group(1);
					thisRef = m.group(2);
				} else if (tl[subIndex].equals(".")) {
					thisPhase = ".";
					thisRef = "";
				} else {
					throw new Exception("Ilegal phase format!");
				}
				if (numericalphase.get(thisPhase) > numericalphase.get(highestPhase)) {
					highestPhase = thisPhase;
					highestRef.clear();
					for (String e: sep.split(thisRef)) highestRef.add(e);
				} else if (numericalphase.get(thisPhase) > 0) {
					if (numericalphase.get(thisPhase) == numericalphase.get(highestPhase)) {
						for (String e: sep.split(thisRef)) highestRef.add(e);
					}
				}
			}
			if (highestPhase.equals(".")) {
				sTimeline.add(".");
			} else {
				String ref = GeneralMethod.join("; ", highestRef);
				sTimeline.add(highestPhase+ " ("+ref+")");
			}
		}
		for (int index = endIndex; index < tl.length; index++) {
			sTimeline.add(tl[index]);
		}
		return GeneralMethod.join("\t", sTimeline);
	}
}
