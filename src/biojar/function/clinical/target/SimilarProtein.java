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
import static biojar.application.SettingFrame.getDefaultDelimiter;

import java.io.BufferedReader;
import java.io.LineNumberReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JOptionPane;

import biojar.function.GeneralMethod;
import biojar.function.lwj.DownloadProgress;
/**
 * 靶点评价五原则中相似蛋白计算的业务实现类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class SimilarProtein {
	/**
	 * 是否被取消运行的boolean值
	 */
	private boolean isCancelled = false;
	/**
	 * 返回运行状态，若是被取消返回true
	 * @return 运行状态
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
	 * 依据method方法计算获得set中每个元素的返回值进行排序，要求method的返回值是int类型
	 * @param set 待排序集合
	 * @param order 升序为true，降序为false
	 * @param method 排序所需方法/函数，根据其返回值排序
	 * @param obj 排序所需方法所属对象
	 * @return 排序获得的ArrayList对象
	 * @throws IllegalAccessException 非法访问异常
	 * @throws InvocationTargetException 调用目标异常
	 */
	public static ArrayList<String> sorted(Set<String> set, boolean order, Method method, Object obj) throws IllegalAccessException, InvocationTargetException {
		ArrayList<String> tmp = new ArrayList<String>();
		for (String e: set) tmp.add(e);
		return sorted(tmp, order, method, obj);
	}
	/**
	 * 依据method方法计算获得arraylist中每个元素的返回值进行排序，要求method的返回值是int类型
	 * @param arraylist 带排序的ArrayList对象
	 * @param order 升序为true，降序为false
	 * @param method 排序所需方法/函数，根据其返回值排序
	 * @param obj 排序所需方法所属对象
	 * @return 排序获得的ArrayList对象
	 * @throws IllegalAccessException 非法访问异常
	 * @throws InvocationTargetException 调用目标异常
	 */
	public static ArrayList<String> sorted(ArrayList<String> arraylist, boolean order, Method method, Object obj) throws IllegalAccessException, InvocationTargetException {
		ArrayList<String> res = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		ArrayList<String> list = (ArrayList<String>) arraylist.clone();
		int length = list.size();
		if (length > 0) {
			for (int index = 0;index<length;index++) {
				String value = list.get(0);
				for (String e: list) {
					if (order) {
						if((int) method.invoke(obj, value) >(int) method.invoke(obj, e)) value = e;
					} else {
						if((int) method.invoke(obj, value) <(int) method.invoke(obj, e)) value = e;
					}
				}
				res.add(value);
				list.remove(list.indexOf(value));
			}
			//res.addAll(sorted(list, order));递归由于数据量大会造成StackOverflowError
		}
		return res;
	
	}
	/**
	 * 将组合的Uniprot ID拆分
	 * @param filename 输入文件名
	 * @param location Uniprot ID所在列，从0开始
	 * @throws FileNotFoundException 输入文件不存在时抛出
	 * @throws IOException 输入输出流异常
	 */
	public void splitUniprotID(String filename, int location) throws FileNotFoundException, IOException {
		try(LineNumberReader lnr = GeneralMethod.BufferRead(filename)){
			new File("output").mkdir();
			try(PrintWriter pw = new PrintWriter("output/" + new File(filename).getName().replace(".txt", "")+" splited.txt")) {
				String line;
				while ((line = lnr.readLine())!=null) {
					String[] tmp = line.split(getDefaultDelimiter());
					Pattern p = Pattern.compile("[A-Z]\\w+");
					Matcher m = p.matcher(tmp[location]);
					while (m.find()) {
						int index = 0;
						for (; index < location; index++) pw.print(tmp[index] + "\t");
						pw.print(m.group(0));
						for (index = index + 1; index < tmp.length; index++) pw.print("\t" + tmp[index]);
						pw.println();
					}
				}
			}
		}
	}
	/**
	 * 合并融合蛋白序列
	 * @param filename 输入文件名
	 * @param locatAC uniprot accession所在列索引（从0开始）
	 * @param locatID 蛋白ID所在列索引（从0开始）
	 * @param dp 进度条
	 * @throws FileNotFoundException 文件未找到异常
	 * @throws IOException 输入输出异常
	 */
	@SuppressWarnings("unused")
	public void makeFusionProteinFasta(String filename, int locatAC, int locatID, DownloadProgress dp) throws FileNotFoundException, IOException {
		String inputdir = "fusion protein fasta";
		HashMap <String, ArrayList<File>> fusion_protein = new HashMap<String, ArrayList<File>>();
		try (LineNumberReader lnr = GeneralMethod.BufferRead(filename)) {
			String line;
			while ((line = lnr.readLine())!=null) {
				String[] tmp = line.split(getDefaultDelimiter());
				fusion_protein.put(tmp[locatID], fusion_protein.getOrDefault(tmp[locatID], new <File> ArrayList<File>()));
				fusion_protein.get(tmp[locatID]).add(new File(inputdir+"/"+tmp[locatAC]+".fasta"));
			}
		}
		String outputdir = "fusion protein output fasta";
		int length = "MGAASGRRGPGLLLPLPLLLLLPPQPALALDPGLQPGNFSADEAGAQLFAQSYNSSAEQV".length();
		File output = new File(outputdir);
		try {
			GeneralMethod.removeDirectory(output);
		} catch (Exception e) {
			e.printStackTrace();
		}
		output.mkdir();
		for (String tid: fusion_protein.keySet()) {
			ArrayList<File> fastaarray = fusion_protein.get(tid);
			try (PrintWriter pw = new PrintWriter(outputdir+"/"+tid+".fasta")) {
				pw.println(">sp|"+tid+"|XXXXXXXXXXX");
				String content = "";
				for (File file: fastaarray) {
					try (LineNumberReader lnr = GeneralMethod.BufferRead(file)) {
						String line;
						while ((line = lnr.readLine())!=null) {
							if (lnr.getLineNumber()==1) continue;
							content += line;
						}
					}
				}
				for(int index =0; index < content.length(); index += length) {
					pw.println(content.substring(index, ((index + length) < content.length()?(index + length): content.length())));
				}
			}
		}
	}
	/**
	 * 将相似蛋白初步计算结果进行pfam去重，输入文件以默认tab分隔
	 * @param file 待去重文件
	 * @param containtitle 是否包含标题
	 * @param locat_pf 靶点蛋白pfam列索引（从0开始）
	 * @param locat_detail 相似蛋白信息列索引（从0开始）
	 * @throws FileNotFoundException 文件未找到异常
	 * @throws IOException 输入输出异常
	 */
	public void deduplicatePfam(File file, boolean containtitle, int locat_pf, int locat_detail) throws FileNotFoundException ,IOException, Exception {
		//读取输入文件
		try (LineNumberReader lnr = GeneralMethod.BufferRead(file)) {
			String line;
			try (PrintWriter pw = new PrintWriter("deduplication result.txt")) {
				while ((line = lnr.readLine()) != null) {
					if (containtitle && lnr.getLineNumber() == 1) continue;
					pw.println(line + "\t" + GeneralMethod.join(deduplicatePfam(line, locat_pf, locat_detail)));
				}
			}
		}
	}
	/**
	 * 将单行相似蛋白信息进行pfam去重，输入文件以默认tab分隔
	 * @param line 待去重相似蛋白字符串行
	 * @param locat_pf 靶点pfam列索引（从0开始）
	 * @param locat_detail 相似蛋白信息列所（从0开始）
	 * @return 去重后的相似蛋白信息字符串组，依次为去重保留的相似蛋白个数，去重保留的相似蛋白，去重删除的相似蛋白个数，去重删除的相似蛋白
	 * @throws IllegalAccessException 非法访问异常
	 * @throws InvocationTargetException 调用目标异常
	 * @throws ClassNotFoundException 类缺失异常
	 * @throws NoSuchMethodException 方法缺失异常
	 * @throws Exception 其他异常
	 */
	public String[] deduplicatePfam(String line, int locat_pf, int locat_detail) throws IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, Exception {
		String[] tmp = line.split(getDefaultDelimiter());
		String[] target_pfs = tmp[locat_pf].split("; ");
		String[] similar_details = tmp[locat_detail].split("\\|\\|");
		Pattern pattern = Pattern.compile("PF\\d+");
		for (int i = 0; i< target_pfs.length; i++) {
			Matcher m = pattern.matcher(target_pfs[i]);
			if (m.find()) {
				target_pfs[i] = m.group(0);
			} else if (!target_pfs[i].equals("")) {
				throw new Exception("Illegal PF ID");
			}
		}
		//构建一个以pfam字符串、蛋白信息arraylist为键值对的hashmap
		HashMap <String, ArrayList<String>> similarmap = new HashMap<>();
		for (String pf: target_pfs) {
			similarmap.putIfAbsent(pf, new ArrayList<>());
			similarmap.get(pf).add("this target");
		}
		for (String sp: similar_details) {
			for (String pf: sp.split(":")[2].split(" ")) {
				similarmap.putIfAbsent(pf, new ArrayList<>());
				similarmap.get(pf).add(sp);
			}
		}
		//构建一个以蛋白信息、重复蛋白HashSet为键值对的HashMap
		HashMap<String, HashSet<String>> repeat_proteins = new HashMap<>();
		for (String pf: similarmap.keySet()) {
			ArrayList <String> proteins = similarmap.get(pf);
			for (int i = 0; i < proteins.size() - 1; i++) {
				for (int j = i+1; j < proteins.size(); j++) {
					repeat_proteins.putIfAbsent(proteins.get(i), new HashSet<>());
					repeat_proteins.putIfAbsent(proteins.get(j), new HashSet<>());
					repeat_proteins.get(proteins.get(i)).add(proteins.get(j));
					repeat_proteins.get(proteins.get(j)).add(proteins.get(i));
				}
			}
			if (proteins.size() == 1) {
				repeat_proteins.putIfAbsent(proteins.get(0), new HashSet<>());
			}
		}
		//定义一个获取重复频数的内部类
		class InnerClass {
			@SuppressWarnings("unused")
			public int getSize(String e) {
				return repeat_proteins.get(e).size();
			}
		}
		//调用定义的sort方法根据频数进行排序，高频数在前
		ArrayList <String> sorted_protein = sorted(
				repeat_proteins.keySet(),
				false,//降序
				InnerClass.class.getMethod("getSize", Class.forName("java.lang.String")),
				new InnerClass());

		//构建一个已经删除的蛋白信息数据集合HashSet
		HashSet <String> trash = new HashSet<>();
		//构建一个去重后选出的相似蛋白信息数据集合HashSet
		HashSet <String> remain = new HashSet<>();
		remain.add("this target");
		/*对排序后的相似蛋白进行分类去重，保留的在remain中，不要的放在trash中
		保留原则，remain中已经有其同家族蛋白的不予保留，即去重
		由于进行频数降序排序，保留的蛋白其重复频数较高，最大限度降低了相似蛋白个数
		*/
		for (String p: sorted_protein) {
			boolean containSamePfam = false;
			for (String sp: remain) {
				if (repeat_proteins.get(p).contains(sp)) {
					containSamePfam = true;
					break;
				};
			}
			if (containSamePfam) {
				trash.add(p);
			} else {
				remain.add(p);
			}
		}
		remain.remove("this target");//去重后移除靶点本身
		String[] res = { 
			"" +remain.size(),
			(remain.isEmpty()?".": GeneralMethod.join("||", remain)),
			""+trash.size(),
			(trash.isEmpty()?".": GeneralMethod.join("||", trash))
		};
		return res;
	}
	/**
	 * fasta文件过滤器，用于过滤获得当前目录中（不含子目录）的fasta
	*/
	private class FastaFileFilter implements FileFilter {
		@Override
		public boolean accept(File pathname) {
			return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".fasta");
		}
	}
	/**
	 * 将两个蛋白进行blast序列比对，其中一个蛋白为database，需要先建为database生成对应的pin/phr/psq文件，另一个为待查询蛋白即目标蛋白，输入文件均为fasta序列文件
	 * @param databsefasta 作为数据库的蛋白fasta文件
	 * @param queryfasta 待查询蛋白fasta文件
	 * @param evalue E value值
	 * @return blast比对结果，如果不存在结果或发生错误均返回null
	 * @throws IOException 输入输出异常
	 * @throws InterruptedException 中断异常
	 * @throws Exception 其他异常
	 */
	public String blastp(String databsefasta, String queryfasta, Double evalue) throws IOException ,InterruptedException, Exception {
		String program = "";
		String sysname = System.getProperty("os.name").toLowerCase();
		if (sysname.contains("windows")) {
			program = "cmd /C blastp";
		} else if (sysname.contains("linux")) {
			program = "./blastp";
		} else {
			throw new UnsupportedOperationException("current OS isn't supported yet");
		}
		Object[] parameters = {
			"-db", "\"" + databsefasta + "\"",
			"-query","\"" + queryfasta + "\"",
			"-evalue", evalue,
			"-outfmt", 6,
			"-num_threads", 4
		};
		String command = program + " " + GeneralMethod.join(" ", parameters);
		Process p = Runtime.getRuntime().exec(command);
		InputStream err_is = p.getErrorStream();
		InputStream is = p.getInputStream();
		BufferedReader err_br = new BufferedReader(new InputStreamReader(err_is, "gb2312"));
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "gb2312"));
		p.waitFor();
		String err_message = null;
		boolean iserr = false;
		while ((err_message = err_br.readLine()) != null) {
			System.err.println(err_message);
			iserr = true;
		}
		if (iserr) return null;
		String message = "";
		String line;
		while ((line = br.readLine()) != null) {
			message = message.isEmpty()?line:message + ("\n" + line);
		}
		if (message.equals("")) message = null;
		return message;
	}
	/**
	 * 将指定输入的一些蛋白（fasta）与指定数据库进行blast序列比对
	 * @param dbDirectory 数据库文件夹
	 * @param queryDirectory 待查询蛋白fasta序列文件夹
	 * @param evalue E value值
	 * @param outputDirectory blast输出文件夹
	 * @param dp 进度条
	 * @throws FileNotFoundException 文件未找到异常
	 * @throws IOException 输入输出异常
	 * @throws InterruptedException 中断异常
	 * @throws Exception 其他异常
	 */
	public void blastp(File dbDirectory, File queryDirectory, double evalue, File outputDirectory, DownloadProgress dp) throws FileNotFoundException, IOException, InterruptedException, Exception {
		if (dbDirectory == null || queryDirectory == null || outputDirectory == null || evalue == 0.0) {
			cancel();
			return;
		}
		if (!queryDirectory.isDirectory()|| !queryDirectory.exists()) {
			throw new FileNotFoundException("query file directory should exists");
		}
		if (!dbDirectory.isDirectory() || !dbDirectory.exists()) {
			throw new FileNotFoundException("database directory should exists");
		}
		if (outputDirectory.isDirectory() && outputDirectory.exists()) {
			if (new File(".").getAbsolutePath().indexOf(outputDirectory.getAbsolutePath()) != 0) {
				if (JOptionPane.showConfirmDialog(null, "目录: "+outputDirectory.getAbsolutePath()+"已存在，是否清空？") == JOptionPane.OK_OPTION) {
					GeneralMethod.clearDirectory(outputDirectory);
				}
			}
		} else if (!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
		FastaFileFilter fff = new FastaFileFilter();
		File[] queryfasta = queryDirectory.listFiles(fff);
		File[] dbfasta = dbDirectory.listFiles(fff);
		if (dp != null){
			dp.setVisible(true);
			dp.now(0, "Total: 0", "" + queryfasta.length);
			dp.now(1, 0, "Current: 0", "" + dbfasta.length);
		}
		
		for (int q = 0; q <queryfasta.length; q++) {
			if (isCancelled) break;
			String queryfilename = queryfasta[q].getCanonicalPath();
			new File(outputDirectory.getAbsolutePath() + "\\"+queryfasta[q].getName() + ".result").delete();
			for (int db = 0; db < dbfasta.length; db++) {
				if (isCancelled) break;
				String dbfilename = dbfasta[db].getCanonicalPath();
				String res = blastp(dbfilename, queryfilename,evalue);
				if (res != null) {
					try (PrintWriter pw = new PrintWriter(new FileOutputStream(outputDirectory.getAbsolutePath() + "\\"+queryfasta[q].getName() + ".result", true))) {
						pw.println(res);
					}
				}
				if (dp != null) dp.now(1,(db+1)*100/dbfasta.length, "Current: "+(db + 1), "" + dbfasta.length);
			}
			if (dp != null) dp.now((q+1)*100/queryfasta.length, "Total: "+(q + 1), "" + queryfasta.length);
		}
	}
	/**
	 * 将blast结果进行筛选规范
	 * @param blastDirectory blast输出结果文件夹
	 * @param outputDirectory 筛选与规范后结果文件夹
	 * @param evalue E value值
	 * @param dp 进度条
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws IOException 输入输出异常
	 * @throws Exception 其他异常
	 */
	public void normalizeResult(File blastDirectory, File outputDirectory, double evalue, DownloadProgress dp) throws FileNotFoundException, IOException, Exception {
		if (blastDirectory == null || outputDirectory == null || evalue == 0.0) {
			cancel();
			return;//无返回结束程序
		}
		if (!blastDirectory.isDirectory()|| !blastDirectory.exists()) {
			throw new FileNotFoundException("blast result directory should exists");
		}
		if (outputDirectory.isDirectory() && outputDirectory.exists()) {
			if (new File(".").getAbsolutePath().indexOf(outputDirectory.getAbsolutePath()) != 0) {
				if (JOptionPane.showConfirmDialog(null, "目录: "+outputDirectory.getAbsolutePath()+"已存在，是否清空？") == JOptionPane.OK_OPTION) {
					GeneralMethod.clearDirectory(outputDirectory);
				}
			}
		} else if (!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}
		HashMap <String, TreeSet<String>> all_pfam = new HashMap<>();
		HashMap <String, TreeSet<String>> db_pfam = new HashMap<>();
		try (LineNumberReader lnr = GeneralMethod.BufferRead("input/01-targets-drug-binding-domain-5.index")) {
			String line;
			while ((line = lnr.readLine()) != null) {
				String[] arr = line.split("\t");
				String ac = arr[2].split("\\|\\|")[0];
				if (!arr[3].equals(".")) {
					TreeSet<String> a = new TreeSet<>();
					for (String s: arr[3].split("\\|\\|")) a.add(s);
					if (a.contains("PF07714")) a.add("PF00069");
					if (a.contains("PF00069")) a.add("PF07714");
					all_pfam.put(ac, a);
				}
				if (!arr[4].equals(".")) {
					TreeSet<String> a = new TreeSet<>();
					for (String s: arr[3].split("\\|\\|")) a.add(s);
					if (a.contains("PF07714")) a.add("PF00069");
					if (a.contains("PF00069")) a.add("PF07714");
					db_pfam.put(ac, a);
				}
			}
		}
		HashMap <String, String[]> ac_pfam = new HashMap<>();
		try (LineNumberReader lnr = GeneralMethod.BufferRead("input/uniprot_AC_pfam.index")) {
			String line;
			while ((line = lnr.readLine()) != null) {
				String[] arr = line.split("\t");
				ac_pfam.put(arr[0], arr[1].split("; "));
			}
		}
		String all_out_file = outputDirectory.getAbsolutePath() + "\\Blast_result_E-vale-"+evalue+"-all.txt";
		String db_out_file = outputDirectory.getAbsolutePath() + "\\Blast_result_E-vale-"+evalue+"-db.txt";
		new File(all_out_file).delete();
		new File(db_out_file).delete();
		
		File[] blastfiles = blastDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".fasta.result");
			}
		});
		
		if (dp != null) dp.setVisible(true);
		if (dp != null) dp.now(0, "0", "" + blastfiles.length);
		for (int index = 0; index < blastfiles.length; index++) {
			if (isCancelled) break;
			File thisfile = blastfiles[index];
			String ac = thisfile.getName().replace(".fasta.result", "");
			
			TreeSet <String> out_all = new TreeSet<>();
			TreeSet <String> exclude_all = new TreeSet<>();
			if (all_pfam.containsKey(ac)) exclude_all = all_pfam.get(ac);
			HashMap <String, Integer> used_all = new HashMap<>();
			
			TreeSet <String> out_db = new TreeSet<>();
			TreeSet <String> exclude_db = new TreeSet<>();
			if (db_pfam.containsKey(ac)) exclude_db = db_pfam.get(ac);
			HashMap <String, Integer> used_db = new HashMap<>();
			
			try (LineNumberReader lnr = GeneralMethod.BufferRead(thisfile)) {
				String line;
				while ((line = lnr.readLine()) != null) {
					String[] arr = line.split(getDefaultDelimiter());
					if (arr.length < 12) continue;
					
					String a = arr[1].split(",")[0];
					TreeSet <String> pfam = new TreeSet<>();
					if (ac_pfam.containsKey(a)) for (String s: ac_pfam.get(a)) pfam.add(s);
					boolean flag_all = true;
					boolean flag_db = true;
					for (String pf: pfam) {
						if (exclude_all.contains(pf)) flag_all = false;
						if (exclude_db.contains(pf)) flag_db = false;
					}
					if (flag_all) {
						if (Double.parseDouble(arr[10])< evalue && Double.parseDouble(arr[2]) >= 40) {
							if (used_all.containsKey(a)) continue;
							if (pfam.size() > 0) {
								out_all.add(GeneralMethod.join(":", a, arr[10], GeneralMethod.join(" ", pfam)));
							}
							used_all.put(a, 1);
						}
					}
					if (flag_db) {
						if (Double.parseDouble(arr[10])< evalue && Double.parseDouble(arr[2]) >= 40) {
							if (used_db.containsKey(a)) continue;
							if (pfam.size() > 0) {
								out_db.add(GeneralMethod.join(":", a, arr[10], GeneralMethod.join(" ", pfam)));
							}
							used_db.put(a, 1);
						}
					}
				}
			}
			String e_all = GeneralMethod.join("||", exclude_all);
			String e_db = GeneralMethod.join("||", exclude_db);
			String[] all = deduplicatePfam(getDefaultDelimiter() + GeneralMethod.join("||", out_all), 0, 1);
			String[] db = deduplicatePfam(getDefaultDelimiter() + GeneralMethod.join("||", out_db), 0, 1);
			PrintWriter pw_all = new PrintWriter(new FileOutputStream(all_out_file, true));
			PrintWriter pw_db = new PrintWriter(new FileOutputStream(db_out_file, true));
			pw_all.printf("%s\t%s\t%s\t%s\n", ac, e_all, all[0], all[1]);
			pw_db.printf("%s\t%s\t%s\t%s\n", ac, e_db, db[0], db[1]);
			pw_all.close();
			pw_db.close();
			if (dp != null) dp.now((index + 1)*100/blastfiles.length, "" + (index + 1), "" + blastfiles.length);
		}
	}
}
