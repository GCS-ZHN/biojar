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

import java.io.*;
import javax.swing.JOptionPane;

import biojar.function.GeneralMethod;

import java.util.*;
import java.util.regex.*;

/**
 * 临床药物成功率计算业务实现类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class SuccessRate {
	private boolean status = false;
	private HashMap<String, String> phase = new HashMap<>();
	private HashMap<String, Integer> numPhase = new HashMap<>();
	private HashMap<String, double[]> standardRate = new HashMap<>();
	/**
	 * 构造方法
	 * @param calc_for_lead 计算主要疾病成功率
	 */
	public SuccessRate(boolean calc_for_lead) {
		phase.put(".",					"P0");
		phase.put("Discontinued",			"Ds");
		phase.put("No development reported",	"Ds");
		phase.put("P1",					"P1");
		phase.put("P1/P12",				"P1");
		phase.put("P1/P2",				"P1/P2");
		phase.put("P1/P2/P3",				"P1/P3");
		phase.put("P1/P23",				"P1/P2");
		phase.put("P1/P3",				"P1/P3");
		phase.put("P12",					"P1");
		phase.put("P1/P12/P2"	,			"P1/P2");
		phase.put("P12/P2",				"P1/P2");
		phase.put("P12/P3",				"P1/P3");
		phase.put("P2",					"P2");
		phase.put("P12/P2/P4"	,			"P1/P4");
		phase.put("P2/P23",				"P2");
		phase.put("P2/P3",				"P2/P3");
		phase.put("P2/P4",				"P2/P4");
		phase.put("P23",					"P2");
		phase.put("P2/P23/P3"	,			"P2/P3");
		phase.put("P23/P3",				"P2/P3");
		phase.put("P3",					"P3");
		phase.put("P1/P3/P4",				"P3/P4");
		phase.put("P3/P4",				"P3/P4");
		phase.put("P12/P23",				"P1/P2");
		phase.put("P4",					"P4");
		phase.put("Suspended",				"Ds");
		phase.put("P23/P4",				"P3/P4");
		numPhase.put("P0",	0);
		numPhase.put("P1",	3);
		numPhase.put("P12",	4);
		numPhase.put("P2",	5);
		numPhase.put("P23",	6);
		numPhase.put("P3",	7);
		numPhase.put("P4",	8);
		numPhase.put("Ds",	0);
		//double[] all, auto, end, inf, neu, onc, res, car, oth;
		if (calc_for_lead) {
			double[] all	= {0.665,0.395,0.584};
			double[] aut	= {0.677,0.373,0.611};
			double[] end	= {0.612,0.381,0.624};
			double[] inf	= {0.669,0.459,0.628};
			double[] neu	= {0.627,0.344,0.568};
			double[] onc	= {0.689,0.423,0.453};
			double[] res	= {0.636,0.316,0.810};
			double[] car	= {0.627,0.274,0.506};
			double[] oth	= {0.753,0.503,0.646};
			standardRate.put("All diseases",	all);
			standardRate.put("Autoimmune",	aut);
			standardRate.put("Endocrine",		end);
			standardRate.put("Infection",		inf);
			standardRate.put("Neurology",		neu);
			standardRate.put("Oncology",		onc);
			standardRate.put("Respiratory",	res);
			standardRate.put("Cardiovascular",	car);
			standardRate.put("Other",		oth);
		} else {
			double[] all	= {0.645,0.324,0.500};
			double[] aut	= {0.680,0.340,0.550};
			double[] end	= {0.583,0.338,0.585};
			double[] inf	= {0.658,0.459,0.554};
			double[] neu	= {0.624,0.302,0.499};
			double[] onc	= {0.639,0.283,0.370};
			double[] res	= {0.667,0.275,0.608};
			double[] car	= {0.606,0.263,0.446};
			double[] oth	= {0.722,0.442,0.571};
			standardRate.put("All diseases",	all);
			standardRate.put("Autoimmune",	aut);
			standardRate.put("Endocrine",		end);
			standardRate.put("Infection",		inf);
			standardRate.put("Neurology",		neu);
			standardRate.put("Oncology",		onc);
			standardRate.put("Respiratory",	res);
			standardRate.put("Cardiovascular",	car);
			standardRate.put("Other",		oth);
		}
	}
	/**
	 * 计算药物临床发展成功率，采用默认输入
	 * @param calc_for_dis 是否为疾病类型计算，false为药物类型
	 * @param calc_for_lead 是否计算主要疾病，false为全部疾病
	 * @param start 开始年份
	 * @param end 结束年份
	 * @param type 计算的疾病或药物类型
	 * @throws TimelineFormatException 时间线格式异常
	 */
	public void RateCalculate(boolean calc_for_dis, boolean calc_for_lead, int start, int end, String type) 
	throws TimelineFormatException {
		RateCalculate(calc_for_dis, calc_for_lead, start, end, type, null);
	}
	/**
	 * 计算药物临床发展成功率
	 * @param calc_for_dis 根据疾病分类还是药物分类，true为药物分类
	 * @param calc_for_lead 计算leadtimeline还是all timeline，true为lead
	 * @param start 计算的开始年份（1990~2019）
	 * @param end 计算的结束年份 （1990~2019）
	 * @param type  计算的疾病或药物类型
	 * @param input 计算的输入文件
	 * @throws TimelineFormatException 时间线格式异常
	 */
	public void RateCalculate(boolean calc_for_dis, boolean calc_for_lead, int start, int end, String type, String input) 
	throws TimelineFormatException {
		try {
			ArrayList<String> classSet = new ArrayList<>();
			LineNumberReader in = GeneralMethod.BufferRead(
				"configure/Timeline_Calculate/"+(calc_for_dis?"disease":"drug")+"-class.txt"
			);
			String line = null;
			while ((line = in.readLine()) != null) classSet.add(line);
			HashMap<String, HashMap<String, ArrayList<String>>> new_timeline = new HashMap<>();
			//HashMap year_target = new HashMap();
			HashMap<String, String> drug_line = new HashMap<>();
			HashMap<String, Integer> repeat_time = new HashMap<>();
			boolean flag_wr = false;//有phase格式等错误就会变为true
			boolean flag_re = false;//有重复就变为true
			PrintWriter repeat = new PrintWriter("output/repeat_drug_id.txt");
			LineNumberReader tml = GeneralMethod.BufferRead(
				input==null?("output/"+(calc_for_lead?"lead":"all") + " timeline.txt"):input
			);
			while ((line = tml.readLine()) != null) {
				if (tml.getLineNumber() == 1) continue;//标题行
				ArrayList<String> arr = new ArrayList<>();
				for (String e: line.split(getDefaultDelimiter())) arr.add(e);
				String label = calc_for_lead?(arr.get(1)):(arr.get(arr.size() - 1));//疾病大类
				label = label + "; " + (calc_for_dis?"All diseases":"All classes");
				arr.set(0, (arr.get(0) +"\t"+ arr.remove(1)));//将药名与疾病名称合并
				repeat_time.putIfAbsent(arr.get(0), 0);
				if (drug_line.containsKey(arr.get(0))) {
					repeat_time.put(arr.get(0), repeat_time.get(arr.get(0)) + 1);
					if (repeat_time.get(arr.get(0)) == 1) {
						repeat.println(drug_line.get(arr.get(0)));
					}
					repeat.println(line);
					flag_re = true;//只要一个药为true，后文会输出报告
				}
				drug_line.put(arr.get(0), line);
				for (int i = 0; i < arr.size(); i++) {
					Pattern p = Pattern.compile(" \\(.+$");
					Matcher match = p.matcher(arr.get(i));
					arr.set(i, match.replaceAll(""));
				}
				String [] mutidis = label.split("; ");
				String lastdis = null;
				for (String thisdis: mutidis) {
					if (!classSet.contains(thisdis)) {
						flag_wr = true;
					}
					if (lastdis != null) {//不同分类中，同一药同一疾病，timeline一致，直接赋值
						new_timeline.putIfAbsent(thisdis, new HashMap<String, ArrayList<String>>());
						new_timeline.get(thisdis).put(arr.get(0), new_timeline.get(lastdis).get(arr.get(0)));
						continue;
					}
					lastdis = thisdis;
					int first_approved = 0;
					for (int y = 1; y <= end -1989; y++) {
						if (arr.get(y).indexOf("P4") == 0) {
							first_approved = y;
							break;
						}
					}
					ArrayList<String> new_line = new ArrayList<>();
					new_line.add(arr.get(0));
					if (first_approved > 0) {
						int y = 1;
						for (;y <= first_approved; y++) {
							if (phase.containsKey(arr.get(y))) new_line.add(phase.get(arr.get(y)));
							else {
								JOptionPane.showMessageDialog(null, 
									"record "+arr.get(0) + " in " +(y + 1989) + "is undef!"
								);
								flag_wr = true;
							}
						}
						for (; y <=(end - 1989); y++) {//初始值为前循环的结束值
							new_line.add("P4");//批准之后全部为P4
						}
					} else {
						for (int y = 1; y <= (end - 1989); y++) {
							if (phase.containsKey(arr.get(y))) new_line.add(phase.get(arr.get(y)));
							else {
								JOptionPane.showMessageDialog(null, 
									"record "+arr.get(0) + " in " +(y + 1989) + "is undef!"
								);
								flag_wr = true;
							}
						}
					}
					
					/*
					若thisdis未添加，则添加，value值为空HashMap
					*/
					new_timeline.putIfAbsent(thisdis, new HashMap<String, ArrayList<String>>());
					new_timeline.get(thisdis).putIfAbsent(arr.get(0), new_line);
				}
			}
			tml.close();
			repeat.close();
			if (flag_re) {//有重复记录
				throw new TimelineFormatException (
				"存在重复记录，请查看repeated_drug_ID.txt");
			} else new File("output/repeated_drug_ID.txt").delete();
			if (flag_wr)//有错误phase等格式问题
				throw new TimelineFormatException("输入格式有误");
			
			PrintWriter resout = new PrintWriter(
				"output/["+start+"-"+end+"]"+type+"-drug-scc-ratio-"+(calc_for_lead?"lead":"all")+".txt"
			);
			resout.println(
				"disease class	p1	p1 to p2/Ds	s_rate1	diff	LOA1\t" +
				"p2	p2 to p3/Ds	s_rate2	diff	LOA2\t"+
				"p3	p3 to p4/Ds	s_rate3	diff	LOA3"
			);

			String cal_dis = type;
			for (String e: classSet) {
				if (type.equals("All classes") || type.equals("All diseases")) cal_dis = e;
				PrintWriter numout = new PrintWriter(
					"output/["+start+"-"+end+"]"+cal_dis+"-phase-change-num-"+(calc_for_lead?"lead":"all")+".txt"
				);
				numout.println(
					"Drug ID	Dis	P1	P1->P2	P1->Ds	P2	P2->P3	P2->Ds	P3	"
					+ "P3->P4	P3->Ds	Drug ID	Dis	1990	1991	1992	1993	1994	1995	1996	"
					+ "1997	1998	1999	2000	2001	2002	2003	2004	2005	2006	2007	2008	2009	"
					+ "2010	2011	2012	2013	2014	2015	2016	2017	2018	2019"
					+(calc_for_lead?"":"	disease classification")
				);
				HashMap<String, HashSet<String>> phase_change = new HashMap<>();
				//记录发生phase change的药物
				for (String drug_dis: (new_timeline.get(cal_dis)).keySet()) {
					ArrayList<String> old_timeline = new_timeline.get(cal_dis).get(drug_dis);
					ArrayList<String> cal_timeline = new ArrayList<>();
					for (int y = start - 1989; y <= end - 1989; y++) {//P1/P2拆成P1、P2
						String[] sep = old_timeline.get(y).split("/");
						for (String ph: sep) cal_timeline.add(ph);
					}
					if (cal_timeline.contains("P1")) {
						addRecord("P1", drug_dis, phase_change);
					}
					if (cal_timeline.contains("P2")) {
						addRecord("P2", drug_dis, phase_change);
					}
					if (cal_timeline.contains("P3")) {
						addRecord("P3", drug_dis, phase_change);
					}
					if (!cal_timeline.contains("P4")) {
						String highest = "P0";
						int h1 = 0;
						/*
						确定最高临床phase和位置
						*/
						for (int i = 0; i < cal_timeline.size(); i++) {
							if (numPhase.get(cal_timeline.get(i))>= numPhase.get(highest)) {
								highest =cal_timeline.get(i);
								h1 = i;
							}
						}
						if (numPhase.get(highest) > numPhase.get(cal_timeline.get(cal_timeline.size()-1))) {
							addRecord(highest+"->Ds", drug_dis, phase_change);
							
							highest = "P0";
							/*
							确定第二最高期及其位置
							*/
							int h2 = 0;
							boolean flag1 = false;
							for (int i = h1+1; i < cal_timeline.size(); i++) {
								if (numPhase.get(cal_timeline.get(i))>= numPhase.get(highest)) {
									highest =cal_timeline.get(i);
									h2 = i;
									flag1 = true;
								}
							}
							if (flag1) {
								if (numPhase.get(highest)
									> numPhase.get(cal_timeline.get(cal_timeline.size()-1))
								) {
									addRecord(highest+"->Ds", drug_dis, phase_change);
								}
								/*
								第三最高期及其位置
								*/
								@SuppressWarnings("unused")
								int h3 = 0;
								boolean flag2 = false;
								for (int i = h2+1; i < cal_timeline.size(); i++) {
									if (numPhase.get(cal_timeline.get(i))>= numPhase.get(highest)) {
										highest =cal_timeline.get(i);
										h3 = i;
										flag2 = true;
									}
								}
								if (flag2) {
									if (numPhase.get(highest)
										> numPhase.get(cal_timeline.get(cal_timeline.size()-1))
									) {
										addRecord(highest+"->Ds", drug_dis, phase_change);
									}
								}
							}
						}
					}
					String last_highest = "P0";
					for (String p:cal_timeline) {
						if (numPhase.get(last_highest) < numPhase.get(p)) {
							if ((last_highest+"->"+p).equals("P1->P4")) {
								addRecord("P1->P2",	drug_dis, phase_change);
								addRecord("P2->P3",	drug_dis, phase_change);
								addRecord("P3->P4",	drug_dis, phase_change);
								addRecord("P2",		drug_dis, phase_change);
								addRecord("P3",		drug_dis, phase_change);
							} else if ((last_highest+"->"+p).equals("P1->P3")) {
								addRecord("P1->P2",	drug_dis, phase_change);
								addRecord("P2->P3",	drug_dis, phase_change);
								addRecord("P2",		drug_dis, phase_change);
							} else if ((last_highest+"->"+p).equals("P2->P4")) {
								addRecord("P2->P3",	drug_dis, phase_change);
								addRecord("P3->P4",	drug_dis, phase_change);
								addRecord("P3",		drug_dis, phase_change);
							} else {
								addRecord((last_highest+"->"+p), drug_dis, phase_change);
							}
							last_highest = p;
						}
					}
					/*
					输出数量表注意排除P1->P2和P1->Ds同时存在的情况
					*/
					numout.println(drug_dis+
						"\t"+(phase_change.getOrDefault("P1", new HashSet<>()).contains(drug_dis)?1:0)+
						"\t"+(phase_change.getOrDefault("P1->P2", new HashSet<>()).contains(drug_dis)?1:0)+
						"\t"+((!phase_change.getOrDefault("P1->P2", new HashSet<>()).contains(drug_dis) &&
							phase_change.getOrDefault("P1->Ds", new HashSet<>()).contains(drug_dis)
							)?1:0)+
						"\t"+(phase_change.getOrDefault("P2", new HashSet<>()).contains(drug_dis)?1:0)+
						"\t"+(phase_change.getOrDefault("P2->P3", new HashSet<>()).contains(drug_dis)?1:0)+
						"\t"+((!phase_change.getOrDefault("P2->P3", new HashSet<>()).contains(drug_dis) &&
							phase_change.getOrDefault("P2->Ds", new HashSet<>()).contains(drug_dis)
							)?1:0)+
						"\t"+(phase_change.getOrDefault("P3", new HashSet<>()).contains(drug_dis)?1:0)+
						"\t"+(phase_change.getOrDefault("P3->P4", new HashSet<>()).contains(drug_dis)?1:0)+
						"\t"+((!phase_change.getOrDefault("P3->P4", new HashSet<>()).contains(drug_dis) &&
							phase_change.getOrDefault("P3->Ds", new HashSet<>()).contains(drug_dis)
							)?1:0)+
						"\t"+drug_line.get(drug_dis)
					);
					
				}
					/*
					计算成功率部分
					*/
					double[] res12 = calculateRateValue(1, phase_change,cal_dis);
					double[] res23 = calculateRateValue(2, phase_change,cal_dis);
					double[] res34 = calculateRateValue(3, phase_change,cal_dis);
					double loa3 = res34[3];
					double loa2 = res23[3]*loa3;
					double loa1 = res12[3]*loa2;
					
					/*
					输出计算结果
					*/
					resout.println(
						cal_dis+"\t"+
						String.format(//格式化输出
						"%d\t%d:%d + %d\t%.2f%%\t%s\t%.2f%%\t",
						(int)res12[0],(int)res12[1]+(int)res12[2],(int)res12[1], (int)res12[2] ,res12[3]*100,
						((start == 2003) && (end == 2011) && calc_for_dis?//当年份为03-11才与NB文章对比
							String.format("%.2f%%", (res12[3]-standardRate.get(cal_dis)[0])*100)
						:"--")
						,loa1*100
						)+
						String.format(//格式化输出
						"%d\t%d:%d + %d\t%.2f%%\t%s\t%.2f%%\t",
						(int)res23[0],(int)res23[1]+(int)res23[2],(int)res23[1], (int)res23[2] ,res23[3]*100,
						((start == 2003) && (end == 2011) && calc_for_dis ?//当年份为03-11才与NB文章对比
							String.format("%.2f%%", (res23[3]-standardRate.get(cal_dis)[1])*100)
						:"--")
						,loa2*100
						)+
						String.format(//格式化输出
						"%d\t%d:%d + %d\t%.2f%%\t%s\t%.2f%%",
						(int)res34[0],(int)res34[1]+(int)res34[2],(int)res34[1], (int)res34[2] ,res34[3]*100,
						((start == 2003) && (end == 2011) && calc_for_dis?//当年份为03-11才与NB文章对比
							String.format("%.2f%%", (res34[3]-standardRate.get(cal_dis)[2])*100)
						:"--")
						,loa3*100
						)
					);
				numout.close();
				if (!(type.equals("All classes") || type.equals("All diseases"))) {
					break;
				};
			}
			resout.close();
			status = true;
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,e.getMessage());
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null,e.getMessage());
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, "输入格式错误");
		}
	}
	/**
	 * 增加临床记录
	 * @param key 临床记录键值
	 * @param value 临床记录值
	 * @param phase_change 临床状态新
	 */
	public static void addRecord(String key, String value, HashMap <String, HashSet<String>> phase_change) {
		if (!phase_change.containsKey(key))
			phase_change.put(key, new HashSet<String>());
		phase_change.get(key).add(value);
	}
	/**
	 * 计算成功率
	 * @param phase 临床状态
	 * @param phase_change 临床状态新
	 * @param cal_dis 计算的疾病
	 * @return 成功率结果数组
	 */
	public static double[] calculateRateValue(
		int phase, HashMap <String, HashSet<String>> phase_change, String cal_dis
	) {
		int p1_count = phase_change.get("P"+phase).size();
		HashSet<String> p1_p2_set = phase_change.get("P"+phase+"->P"+(phase+1));
		int p1_p2_count = p1_p2_set.size();
		int p1_ds_count = 0;
		if (phase_change.containsKey("P"+phase+"->Ds")) {
			HashSet <String> p1_ds_trueset = new HashSet<>();
			HashSet <String> p1_ds_set = phase_change.get("P"+phase+"->Ds");
			for (String item: p1_ds_set) {
				if (!p1_p2_set.contains(item)) p1_ds_trueset.add(item);
			}
			p1_ds_count = p1_ds_trueset.size();
		}
		double rate = 0.0;
		if((p1_p2_count+p1_ds_count) > 0)
			rate = (double) p1_p2_count/(p1_p2_count+p1_ds_count);
		else JOptionPane.showMessageDialog(
			null, cal_dis +":\"transition+Ds in P"+phase+"\" must not be equal to zero!"
		);
		double[] res = {p1_count, p1_p2_count, p1_ds_count, rate};
		return res;
	}
	/**
	 * @return 计算状态，用于线程控制
	 */
	public boolean getStatus() {
		return status;
	}
}