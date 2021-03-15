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
import static biojar.application.SettingFrame.getDefaultDelimiter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import biojar.function.GeneralMethod;

/**
 * 生成药物临床发展时间线的业务处理类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class TimelineMaker {
	/**
	 * 代表timeline的生成状态，true为成功，false为失败
	 */
	private boolean status = false;
	/**
	 * 代表timeline的开始时间年份
	 */
	private int start_year = 1990;
	/**
	 * 代表timeline的结束时间年份
	 */
	private int end_year = 2019;
	/**
	 * 代表默认文件输出目录
	 */
	private String outdir = "output";
	/**
	 * 代表默认lead timeline输出文件名（无地址）
	 */
	private String out_lead_file ="lead timeline.txt";
	/**
	 * 代表默认all timeline输出文件名（无地址）
	 */
	private String out_all_file = "all timeline.txt";
	/**
	 * 记录timeline信息的HashMap，key值为“药名	疾病名”,
	 * value为记录每条timeline的ArrayList
	 */
	@SuppressWarnings("rawtypes")
	private HashMap<String, ArrayList> drug_timeline = new HashMap<>();
	/**
	 * 记录临床phase及其对应数值化phase的HashMap
	 */
	private HashMap<String, Double> numericalphase = new HashMap<>();
	/**
	 * 记录具体疾病及其对应疾病大类类型、ICD编码的HashMap
	 */
	private HashMap<String, String[]> dis_type_icd = new HashMap<>();
	/**
	 * 记录疾病为血液肿瘤的记录
	 */
	private HashMap<String, ArrayList<String[]>> haema_P1_record = new HashMap<>();
	/**
	 * 记录肿瘤每个药的疾病个数及每个疾病下是否只有Phase 1
	 */
	private HashMap<String, HashMap<String, Boolean>> Only_P1_oncology =new HashMap<>();
	/**
	 * 记录疾病为实体瘤的记录
	 */
	private HashMap<String, ArrayList<String[]>> solid_P1_record = new HashMap<>();
	/**
	 * 记录输入血液或淋巴瘤的ICD11编码前三位的HashSet，用于滤过
	 */
	private HashSet<String> haema_icd = new HashSet<String>();
	/**
	 * 构造方法初始化Time_maker对象
	 */
	public TimelineMaker() {
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
		for (int index = 0; index < 10; index ++) {
			if (index <=3) haema_icd.add("2B"+index);//2B0*-2B3*
			if (index >=2) haema_icd.add("2A"+index);//2A2*-2A9*
		}
	}
	/**
	 * 设置类属性
	 * @param attributeName 属性名称
	 * @param attributeValue 属性值
	 * @throws Exception 未知异常
	 */
	public void setAttribute(String attributeName, Object attributeValue) throws Exception {
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
		} else if (attributeName.equals("output directory")) {
			if (attributeValue instanceof String) {
				outdir = (String) attributeValue;
			} else {
				throw new Exception("attributeValue should be a String");
			}
		} else {
			throw new Exception("undefined attributeName");
		}
	}
	/**
	 * 生成Year unfilled lead timeline
	 * @param fileinput 带绝对地址或相对地址的输入文件名
	 * @return 生成的timeline文件名
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public String makeLeadTimeline(String fileinput) throws FileNotFoundException, IOException, Exception {
		return makeLeadTimeline(fileinput,true, false);
	}
	/**
	 * 生成lead timeline
	 * @param fileinput 带绝对地址或相对地址的输入文件名
	 * @param for_year 生成timeline的类型，true为year timeline, false为month timeline
	 * @param for_fill 生成timeline的类型，true为filled timeline, false为unfilled timeline
	 * @return 生成的timeline文件名
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public String makeLeadTimeline(String fileinput, boolean for_year, boolean for_fill) throws FileNotFoundException, IOException, Exception {
		String outfile = (for_year?"Year ":"Month ")+(for_fill?"filled ":"unfilled ")+out_lead_file;
		if (for_fill) {
			makeTimeline(fileinput, outdir, "tem", true, false);//注意先生成month timeline，故最后一个参数是false
			TimelineFill.setAttribute("timeline start year", start_year);
			TimelineFill.setAttribute("timeline end year", end_year);
			if (for_year) {
				TimelineFill.fillYearTimeline(outdir+"/tem", outdir+"/"+outfile, true);
			} else {
				TimelineFill.fillMonthTimeline(outdir+"/tem", outdir+"/"+outfile, true);
			}
			new File(outdir+"/tem").delete();
		} else {
			makeTimeline(fileinput, outdir, outfile, true, for_year);
		}
		return outdir+"/"+outfile;
	}
	/**
	 * 生成 Year unfilled all timeline
	 * @param fileinput 带绝对地址或相对地址的输入文件名
	 * @return 生成的timeline文件名
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public String makeAllTimeline(String fileinput) throws FileNotFoundException, IOException, Exception {
		return makeAllTimeline(fileinput, true, false);
	}
	/**
	 * 生成all timeline
	 * @param fileinput 带绝对地址或相对地址的输入文件名
	 * @param for_year 生成timeline的类型，true为year timeline, false为month timeline
	 * @param for_fill 生成timeline的类型，true为filled timeline, false为unfilled timeline
	 * @return 生成的timeline文件名
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 未知异常
	 */
	public String makeAllTimeline(String fileinput, boolean for_year, boolean for_fill) throws FileNotFoundException, IOException, Exception {
		String outfile = (for_year?"Year ":"Month ")+(for_fill?"filled ":"unfilled ")+out_all_file;
		if (for_fill) {
			makeTimeline(fileinput, outdir, "tem", true, false);
			TimelineFill.setAttribute("timeline start year", start_year);
			TimelineFill.setAttribute("timeline end year", end_year);
			if (for_year) {
				TimelineFill.fillYearTimeline(outdir+"/tem", outdir+"/"+outfile, false);
			} else {
				TimelineFill.fillMonthTimeline(outdir+"/tem", outdir+"/"+outfile, false);
			}
			new File(outdir+"/tem").delete();
		} else {
			makeTimeline(fileinput, outdir, outfile, false, for_year);
		}
		return outdir+"/"+outfile;
	}
	/**
	 * 
	 * @param fileinput 带绝对地址或相对地址的输入文件名
	 * @param outdir 文件输出的绝对地址或相对地址
	 * @param fileoutput 文件输出名，不带地址
	 * @param type 生成timeline的类型，true为lead timeline, false为all timeline
	 * @param for_year 生成timeline的类型，true为year timeline, false为month timeline
	 */
	public void makeTimeline(String fileinput, String outdir, String fileoutput, boolean type, boolean for_year) {
		try {
			HashMap<String, String> dis_icd = new HashMap<>();
			dis_icd.put("Certain infectious or parasitic diseases","Infection");
			dis_icd.put("Neoplasms","Oncology");
			LineNumberReader lnr = GeneralMethod.BufferRead(fileinput);//"input/timeline_input.txt"
			String line = null;
			while ((line = lnr.readLine()) != null) {
				String[] record = line.split(getDefaultDelimiter());
				String drugid = record[0];
				String alldis = record[2];
				String icd = record[3];
				String[] leaddis = record[5].split("\\|");//转义字符用\\，与perl不同
				String ref = record[6];
				int start = 0;//临床试验开始时间
				int end = 0;//临床试验结束时间
				for (int i = 7; i <10; i++ ) {
					String e = record[i].substring(5);
					if (e.equals("-")) continue;//equals判断是否为字符串值相等， ==判断是否为同一字符串对象
					String[] dates = e.split("/");
					int year = Integer.parseInt(dates[2]);
					int month = Integer.parseInt(dates[0]);
					if (!for_year) {//月份timeline时格式，如199011
						year = year*100+month;
					}
					if (start == 0) {
						start = year;
					} else if (start >= year) {
						start = year;
					}
					if (end == 0) {
						end = year;
					} else if (end <= year) {
						end = year;
					}
				}
				/**
				 * 对phase信息进行格式规范筛选
				 */
				String phase = record[10].replaceAll("hase ", "").replaceAll("Early ", "");
				if (phase.equals("N/A") || phase.equals("-")) continue;
				phase = phase.replaceAll("/1", "/P1");
				phase = phase.replaceAll("/2", "/P2");
				phase = phase.replaceAll("/3", "/P3");
				phase = phase.replaceAll("/4", "/P4");
				if (!phase.equals("Discontinued")) phase = phase.toUpperCase();
				if (type) {
					for (String dis: leaddis) {
						String newdis = dis_icd.getOrDefault(dis, "Other");
						String[] info = {drugid,newdis,ref,String.valueOf(start),String.valueOf(end),phase};
						addRecord(info, type, for_year);
					}
				} else {
					HashSet<String> newdises = new HashSet<>();//合并多个疾病类型的Other
					for (String dis:leaddis) {
						newdises.add(dis_icd.getOrDefault(dis, "Other"));
					}
					String icdtmp = icd.replaceFirst("ICD11:", "");
					Pattern pattern = Pattern.compile("-|\\|");
					String[] spicd = pattern.split(icdtmp);
					boolean is_cancer = false;
					boolean is_haema = true;
					for (String sub: spicd) {
						if (!haema_icd.contains(sub.substring(0, 3))) {
							is_haema = false;
						}
						if (sub.substring(0, 1).equals("2")) {
							is_cancer = true;
						}
					}
					if (newdises.contains("Oncology")) {
						is_cancer = true;
					}
					String[] info = {
						drugid,alldis,ref,String.valueOf(start),String.valueOf(end),phase,
						GeneralMethod.join("; ",newdises.toArray()),icd
					};
					addRecord(info, type, for_year);
					Only_P1_oncology.putIfAbsent(drugid, new HashMap<String, Boolean>());
					if (is_cancer && phase.equals("P1")) {
						if (is_haema) {
							haema_P1_record.putIfAbsent(drugid, new ArrayList<String[]>());
							haema_P1_record.get(drugid).add(info);
						} else {
							solid_P1_record.putIfAbsent(drugid, new ArrayList<String[]>());
							solid_P1_record.get(drugid).add(info);
						}
						Only_P1_oncology.get(drugid).putIfAbsent(alldis, true);
					} else if (is_cancer) {
						Only_P1_oncology.get(drugid).put(alldis, false);
					}
				}
			}
			lnr.close();
			
			new File(outdir).mkdir();
			@SuppressWarnings("resource")
			PrintWriter output = new PrintWriter(outdir+"/"+fileoutput);
			output.print("DrugIDs\tIndication");
			if (for_year) {
				for (int year = start_year; year <=end_year; year++) output.print("\t"+year);
			} else {
				for (int y = start_year; y <= end_year; y++) {
					for (int m = 1; m <= 12; m++) {
						output.print("\t");
						output.print(y*100+m);
					}
				}
			}
			if (!type) output.print("\tClass");
			output.println();
			Set<String> key = drug_timeline.keySet();
			/*
			疾病过滤与分配代码片段
			 */
			if (!type) {//可改进，将疾病大类分段直线代码改为循环代码，减少代码量
				HashSet <String> need_remove = new HashSet<>();
				for (String e: key) {
					String[] drug_dis = e.split("\t");
					String icd = dis_type_icd.get(drug_dis[1])[1];
					icd = icd.replaceFirst("ICD11:", "");
					Pattern pattern = Pattern.compile("-|\\|");
					String[] spicd = pattern.split(icd);
					boolean is_cancer = false;
					boolean is_haema = true;
					for (String sub: spicd) {
						if (!haema_icd.contains(sub.substring(0, 3))) {
							is_haema = false;
						}
						if (sub.substring(0, 1).equals("2")) {
							is_cancer = true;
						}
					}
					if (is_cancer && is_haema) {
						ArrayList<String[]> oldlist = haema_P1_record.getOrDefault(drug_dis[0], new ArrayList<>());
						for (String[] old: oldlist) {//同病、同药可有多个记录getClass()
							ArrayList<String> newarr = new ArrayList<>();
							if (!old[0].equals(drug_dis[0])) throw new Exception("药名不等");
							if (old[1].equals(drug_dis[1])) continue;
							for (int index = 0;index < old.length; index++) 
								newarr.add(index==1?drug_dis[1]:old[index]);
							addRecord(newarr.toArray(new String[newarr.size()]), false, for_year);
						}
					} else if (is_cancer) {
						ArrayList<String[]> oldlist = solid_P1_record.getOrDefault(drug_dis[0], new ArrayList<>());
						for (String[] old: oldlist) {//同病、同药可有多个记录getClass()
							ArrayList<String> newarr = new ArrayList<>();
							if (!old[0].equals(drug_dis[0])) throw new Exception("药名不等");
							if (old[1].equals(drug_dis[1])) continue;
							for (int index = 0;index < old.length; index++) 
								newarr.add(index==1?drug_dis[1]:old[index]);
							addRecord(newarr.toArray(new String[newarr.size()]), false, for_year);
						}
					}
				}
				for (String e: need_remove) drug_timeline.remove(e);
			}
			
			ArrayList<String> sortedlist = GeneralMethod.sorted(key, true);
			for (String id: sortedlist) {
				String[] drug_dis = id.split("\t");
				output.print(id);
				@SuppressWarnings("unchecked")
				ArrayList <String[]> al = drug_timeline.get(id);
				for (String[] phase_ref: al) {
					String dat;
					if (phase_ref[0].equals(".")) {
						dat = ".";
					} else {
						dat = phase_ref[0] + " (" + phase_ref[1] + ")";
					}
					output.print("\t" + dat);
				}
				if (!type) output.print("\t"+dis_type_icd.get(drug_dis[1])[0]);
				output.println();
			}
			output.close();
			if (type) {
				JOptionPane.showMessageDialog(null, "Lead timeline 已经成功生成");
			} else {
				JOptionPane.showMessageDialog(null, "All timeline 已经成功生成");
			}
			status = true;
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		} catch (StringIndexOutOfBoundsException e) {
			JOptionPane.showMessageDialog(null,"输入文件格式非法1："+e.getMessage());
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"输入文件格式非法2："+e.getMessage());
			e.printStackTrace();
		}
	}
	/**
	 * 给Year drug_timeline添加记录
	 * @param record 记录一条临床信息的数组，包括药物、疾病、参考资料、临床状态、时间等信息
	 * @param type 添加的timeline类型，true为lead timeline， false为all timeline
	 */
	public void addRecord(String[] record, boolean type) {
		addRecord(record, type, true);
	}
	/**
	 * 给drug_timeline添加记录
	 * @param record 记录一条临床信息的数组，包括药物、疾病、参考资料、临床状态、时间等信息
	 * @param type 添加的timeline类型，true为lead timeline， false为all timeline
	 * @param for_year 添加的timeline类型，true为year timeline， false为month timeline
	 */
	public void addRecord(String[] record, boolean type, boolean for_year) {
		String id = (record[0] +"\t" + record[1]);
		String ref = record[2];
		int start = Integer.parseInt(record[3]);
		int end = Integer.parseInt(record[4]);
		String phase = record[5];
		if (!type) {
			try {
				String[] class_icd = {record[6],record[7]};
				dis_type_icd.putIfAbsent(record[1], class_icd);
			} catch (ArrayIndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		}
		String[] phase_ref = {phase, ref};
		if (!drug_timeline.containsKey(id)) {
			drug_timeline.put(id, new ArrayList<String[]>());
		}
		boolean is_approval = false;
		ArrayList <Integer> years = new ArrayList<Integer>();
		if (for_year) {
			for(int y = start_year; y <= end_year; y++) years.add(y);
		} else {
			for (int y = start_year; y <= end_year; y++) 
				for (int m = 1; m <= 12; m++) 
					years.add(y*100+m);
		}
		@SuppressWarnings("unchecked")
		ArrayList <String[]> mirror = drug_timeline.get(id);//创建别名，用clone才是复制
		for (int index = 0; index < years.size(); index++) {
			int year = years.get(index);
			if (year >= start && year <= end && !is_approval) {
				if (GeneralMethod.defined(mirror, index)) {
					String[] old = mirror.get(index);
					String lastphase = old[0];
					String lastref = old[1];
					try {
						if (numericalphase.get(phase) > numericalphase.get(lastphase)) {
							mirror.set(index, phase_ref);
						} else if (numericalphase.get(phase) == numericalphase.get(lastphase) 
								&& numericalphase.get(lastphase)>0) {
							String new_ref;
							if (lastref.indexOf(phase_ref[1])>=0) {
								new_ref = lastref;
							} else {
								new_ref = lastref + "; "+phase_ref[1];
							}
							String[] new_phase_ref = {lastphase, new_ref};
							mirror.set(index, new_phase_ref);
						} 
					} catch (NullPointerException e) {//phase 或 lastphase不在hashmap中会报错
						JOptionPane.showMessageDialog(null, lastphase + "或" + phase+ "格式有问题！");
					}
				} else {
					mirror.add(phase_ref);
				}
			} else {
				if (!GeneralMethod.defined(mirror, index)) {
					String[] tmp = {".",null};
					mirror.add(tmp);
				}
			}
			if (mirror.get(index)[0].indexOf("P4")>=0) is_approval = true;
		}
	}
	/**
	 * 疾病类型异常内部类，在疾病异常时抛出
	 */
	public class DiseaseClassException extends Exception {
		/**
		 * 序列化ID
		 */
		private static final long serialVersionUID = 202102042035L;
		/**
		 * 构造方法
		 * @param info 异常信息
		 */
		public DiseaseClassException(String info) {
			super(info);
		}
	}
	/**
	 * 返回是否成功生成timeline
	 * @return boolean类型，true表示成功生成timeline
	 */
	public boolean getStatus() {
		return status;
	}
}
