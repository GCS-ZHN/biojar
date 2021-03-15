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

import static biojar.application.SettingFrame.getDefaultDelimiter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.LineNumberReader;
import java.io.PrintWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import biojar.function.GeneralMethod;
import biojar.function.lwj.DownloadProgress;
import biojar.function.lwj.Requests;
import uk.ac.ebi.kraken.interfaces.uniprot.DatabaseType;
import uk.ac.ebi.uniprot.dataservice.client.ServiceFactory;
import uk.ac.ebi.uniprot.dataservice.client.Client;
import uk.ac.ebi.uniprot.dataservice.client.uniprot.UniProtService;
import uk.ac.ebi.uniprot.dataservice.client.exception.ServiceException;
import uk.ac.ebi.uniprot.dataservice.client.QueryResult;
import uk.ac.ebi.uniprot.dataservice.client.uniprot.UniProtQueryBuilder;
import uk.ac.ebi.uniprot.dataservice.query.Query;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntry;
import uk.ac.ebi.kraken.interfaces.uniprot.Gene;
import uk.ac.ebi.kraken.interfaces.uniprot.genename.GeneNameSynonym;
import uk.ac.ebi.kraken.interfaces.uniprot.genename.ORFName;
import uk.ac.ebi.kraken.interfaces.uniprot.description.Name;
import uk.ac.ebi.kraken.interfaces.uniprot.description.Field;
import uk.ac.ebi.kraken.interfaces.uniprot.UniProtEntryType;

/**
 * 从著名蛋白质数据库<a href="https://www.uniprot.org/">Uniprot</a>获取蛋白质信息数据的业务处理类
 * @version 1.0
 * @since 14 2021-02-04
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class GetFromUniprot {
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
	 * 从UniProtEntry 对象获取信息
	 * @param entry UniProtEntry 对象
	 * @return 信息String对象
	 * @throws Exception 未知异常
	 */
	public String getUniprotEntry(UniProtEntry entry) throws Exception {
		String result = "";
		//获取Uniprot ID
		result += entry.getUniProtId().getValue();
		//获取recommendedname
		ArrayList <String> recommendedname = new ArrayList<>();
		for (Field field: entry.getProteinDescription().getRecommendedName().getFields()) {
			recommendedname.add(field.getValue() + " ["+field.getType().getValue()+"]");
		}
		result += ("\t" + (recommendedname.isEmpty()?".":GeneralMethod.join("; ", recommendedname)));
		//获取Alternativename
		if (entry.getProteinDescription().hasAlternativeNames()) {
			ArrayList <String> alternativename = new ArrayList<>();
			for (Name name: entry.getProteinDescription().getAlternativeNames()) {
				ArrayList <String> thisname = new ArrayList<>();
				for (Field field: name.getFields()) {
					thisname.add(field.getValue() + " ["+field.getType().getValue()+"]");
				}
				alternativename.add(GeneralMethod.join("||", thisname));
			}
			result += ("\t"+GeneralMethod.join("; ", alternativename));
		} else {
			result += ("\t.");
		}
		//获取genename
		ArrayList <String> genename = new ArrayList<>();
		for (Gene gene: entry.getGenes()) {
			ArrayList <String>genesynonyms = new ArrayList<>();
			for (GeneNameSynonym gns: gene.getGeneNameSynonyms()) {
				genesynonyms.add(gns.getValue());
			}
			ArrayList <String>orfs = new ArrayList<>();
			for (ORFName orfn: gene.getORFNames()) {
				orfs.add(orfn.getValue());
			}
			genename.add(gene.getGeneName().getValue() + " {"+
					"\"Synonyms\": \""+GeneralMethod.join("; ", genesynonyms)+"\", "+
					"\"ORFNames\": \""+GeneralMethod.join("; ", orfs)+"\""+
					"}");
		}
		result += ("\t"+ GeneralMethod.join("; ", genename));
		if(entry.getType().equals(UniProtEntryType.SWISSPROT)) {// if the entry belongs to the Swiss-Prot section of UniProtKB (reviewed) or to the computer-annotated TrEMBL section (unreviewed).
			result += ("\tReviewed");
		} else {
			result += ("\tUnreviewed");
		}
		ArrayList <String> pfam = new ArrayList<>();
		entry.getDatabaseCrossReferences(DatabaseType.PFAM).forEach(db->
				pfam.add(db.getPrimaryId().getValue()+" ["+db.getDescription().getValue()+"]")
		);
		result += ("\t"+(pfam.isEmpty()?".":GeneralMethod.join("; ", pfam)));
		return result;
	}
	/**
	 * 从Uniprot上逐个获取UniProtEntry信息，Accession索引位置应当为第一列（0）
	 * @param inputfile Uniprot Accession 输入文件名
	 * @param outputfile 结果输出文件名
	 * @throws IOException 输入输出异常
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws Exception 其他未知异常
	 */
	public void getUniprotEntry(String inputfile, String outputfile) throws IOException, FileNotFoundException, Exception {
		getUniprotEntry(inputfile, outputfile, new DownloadProgress("获取进度"), 0);
	}
	/**
	 * 从Uniprot上逐个获取UniProtEntry信息
	 * @param inputfile Uniprot Accession 输入文件名
	 * @param outputfile 结果输出文件名
	 * @param dp 自定义进度条
	 * @param location 输入文件中Accession所在列索引（从0开始）
	 * @throws IOException 输入输出异常
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws InterruptedException 中断异常
	 */
	public void getUniprotEntry(String inputfile, String outputfile, DownloadProgress dp, int location) throws IOException, FileNotFoundException, InterruptedException {
		ServiceFactory serviceFactoryInstance = Client.getServiceFactoryInstance();
		UniProtService uniprotService = serviceFactoryInstance.getUniProtQueryService();
		uniprotService.start();
		new File(outputfile+".tmp").delete();
		new File("err.txt").delete();
		new File(outputfile+".tmp").deleteOnExit();
		LineNumberReader lnr = GeneralMethod.BufferRead(inputfile);
		String line = null;
		int total = 0;
		lnr.mark(1000000);
		while ((line=lnr.readLine())!=null && !isCancelled) {
			total = lnr.getLineNumber();
		}
		lnr.reset();
		dp.setVisible(true);
		dp.now(lnr.getLineNumber()*100/total, String.valueOf(lnr.getLineNumber()), String.valueOf(total));
		while  ((line=lnr.readLine())!=null && !isCancelled) {
			String[] tmp = line.split(getDefaultDelimiter());
			String accession = tmp[location];
			PrintWriter pw = new PrintWriter(new FileOutputStream(outputfile+".tmp", true));
			PrintWriter err = new PrintWriter(new FileOutputStream("err.txt", true));
			try {
				if (lnr.getLineNumber()==1) {
					for(int i=0;i<tmp.length;i++) {
						pw.print("input column "+i+"\t");
					}
					pw.println("Unirpot ID\tRecommendedName\tAlternativeNames\tGenes\tReviewed\tPfam");
				}
				pw.print(line);
				UniProtEntry entry = uniprotService.getEntry(accession);
				if (entry != null) {
					pw.println("\t" + getUniprotEntry(entry));
				} else {
					pw.println("\t.\t.\t.\t.\t.\t.");
				}
			} catch (IOException ioe) {
				pw.println("\t.\t.\t.\t.\t.\t.");
				err.println(line+"\t"+ioe.getMessage());
			} catch (ServiceException se) {
				pw.println("\t.\t.\t.\t.\t.\t.");
				err.println(line+"\t"+se.getMessage());
			}
			catch (Exception e) {
				pw.println("\t.\t.\t.\t.\t.\t.");
				err.println(line+"\t"+e.getMessage());
			}
			pw.close();
			err.close();
			dp.now(lnr.getLineNumber()*100/total, String.valueOf(lnr.getLineNumber()), String.valueOf(total));
			Thread.sleep(100);
		}
		uniprotService.stop();
		lnr.close();
		if (isCancelled) {
			new File(outputfile+".tmp").delete();
		} else {
			new File(outputfile).delete();
			new File(outputfile+".tmp").renameTo(new File(outputfile));
		}
	}
	/**
	 * 从Uniprot上批量个获取UniProtEntry信息
	 * @param inputfile Uniprot Accession 输入文件名
	 * @param outputfile 结果输出文件名
	 * @param dp 自定义进度条
	 * @param location 输入文件中Accession所在列索引（从0开始）
	 * @param step 批量处理单位数量
	 * @throws IOException 输入输出异常
	 * @throws FileNotFoundException 文件缺失异常
	 * @throws InterruptedException 中断异常
	 */
	public void getUniprotEntrys(String inputfile, String outputfile, DownloadProgress dp, int location, int step) throws IOException, FileNotFoundException, InterruptedException {
		ServiceFactory serviceFactoryInstance = Client.getServiceFactoryInstance();
		UniProtService uniprotService = serviceFactoryInstance.getUniProtQueryService();
		uniprotService.start();
		new File(outputfile+".tmp").delete();
		new File("err.txt").delete();
		new File(outputfile+".tmp").deleteOnExit();
		LineNumberReader lnr = GeneralMethod.BufferRead(inputfile);
		String line;
		int total = 0;
		lnr.mark(1000000);
		while ((line=lnr.readLine())!=null && !isCancelled) {
			total = lnr.getLineNumber();
		}
		line = "";
		lnr.reset();
		dp.setVisible(true);
		dp.now(lnr.getLineNumber()*100/total, String.valueOf(lnr.getLineNumber()), String.valueOf(total));
		while  (line != null && !isCancelled) {
			PrintWriter pw = new PrintWriter(new FileOutputStream(outputfile+".tmp", true));
			PrintWriter err = new PrintWriter(new FileOutputStream("err.txt", true));
			TreeSet <String> accessions = new TreeSet<>();
			for (int i = 0; i < step; i++) {
				line = lnr.readLine();
				if (line == null) break;
				String[] tmp = line.split(getDefaultDelimiter());
				String accession = tmp[location];
				accessions.add(accession);
			}
			if (lnr.getLineNumber()==step) {
				pw.println("Accession\tUnirpot ID\tRecommendedName\tAlternativeNames\tGenes\tReviewed\tPfam");
			}
			@SuppressWarnings("unchecked")
			Query query = UniProtQueryBuilder.accessions((Set<String>) accessions.clone());
			HashMap <String, String> info = new HashMap<>();
			try {
				QueryResult<UniProtEntry> entrys = uniprotService.getEntries(query);
				while (entrys.hasNext()) {
					UniProtEntry entry = entrys.next();
					info.put(entry.getPrimaryUniProtAccession().getValue(), getUniprotEntry(entry));
				}
			} catch (ServiceException se) {
				err.println(se.getMessage());
			} catch (Exception e) {
				err.println(e.getMessage());
			}
			for (String accession: accessions) {
				pw.println(accession+"\t"+info.getOrDefault(accession, ".\t.\t.\t.\t.\t."));
			}
			pw.close();
			err.close();
			dp.now(lnr.getLineNumber()*100/total, String.valueOf(lnr.getLineNumber()), String.valueOf(total));
			Thread.sleep(100);
		}
		uniprotService.stop();
		lnr.close();
		if (isCancelled) {
			new File(outputfile+".tmp").delete();
		} else {
			new File(outputfile).delete();
			new File(outputfile+".tmp").renameTo(new File(outputfile));
		}
	}
	/**
	 * 利用<a href="https://www.uniprot.org/help/api_idmapping">Uniprot</a>的 Mapping database identifiers提供API进行ID转化
	 * @param from 起始值
	 * @param to 目标值
	 * @param format 输出格式
	 * @param query 查询字符串，空格分隔
	 * @return 查询结果字符串
	 * @throws IOException 输入输出异常
	 * @throws Exception 其他异常
	 */
	public String transferID(String from, String to, String format, String query) throws IOException, Exception {
		HashMap <String, String> data = new HashMap<>();
		data.put("from", from);
		data.put("to", to);
		data.put("format", format);
		data.put("query", query);
		return new String(Requests.post("https://www.uniprot.org/uploadlists/", data));
	}
	/**
	 * 从Uniprot 下载Fasta文件，默认输出至fasta_output文件夹
	 * @param filename 输入的文件名
	 * @param location 查询ID的列
	 * @param dp 进度条
	 * @throws FileNotFoundException 文件未找到异常
	 * @throws IOException 输入输出异常
	 */
	public void downloadFasta(String filename, int location, DownloadProgress dp) throws FileNotFoundException, IOException {
		downloadFasta(filename, location, dp, "fasta_output");
	}
	/**
	 * 从Uniprot 下载Fasta文件
	 * @param filename 输入的文件名
	 * @param location 查询ID的列
	 * @param dp 进度条
	 * @param outputdir 输出文件夹
	 * @throws FileNotFoundException 文件未找到异常
	 * @throws IOException 输入输出异常
	 */
	public void downloadFasta(String filename, int location, DownloadProgress dp, String outputdir) throws FileNotFoundException, IOException {
		File dir = new File(outputdir);
		if (dir.exists()) {
			if (JOptionPane.showConfirmDialog(null, "输出目录已存在，是否覆盖") == JOptionPane.OK_OPTION) {
				try {
					GeneralMethod.removeDirectory(dir);
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "删除目录失败，请关闭目录下所有程序、文件后重试");
					isCancelled = true;
				}
			}
		}
		dir.mkdir();
		ArrayList<String> al = new ArrayList<>();
		try (LineNumberReader lnr = GeneralMethod.BufferRead(filename)) {
			String line = null;
			while (!isCancelled && (line = lnr.readLine())!=null) {
				String[] tmp = line.split(getDefaultDelimiter());
				al.add(tmp[location]);
			}
		}
		dp.setVisible(true);
		dp.now(0, String.valueOf(0), String.valueOf(al.size()));
		File failfile = new File(outputdir+"/fastadownloadfailedid.txt");
		File errfile = new File(outputdir+"/fastadownloaderr.txt");
		try (PrintWriter failedid = new PrintWriter(failfile)) {
			for(int index =0; index < al.size(); index++) {
				if (isCancelled) break;
				String accession = al.get(index);
				try {
					boolean status = Requests.download("https://www.uniprot.org/uniprot/"+accession+".fasta", outputdir+"/"+accession+".fasta", false);
					if (!status) {
						failedid.println(accession);
					}
				} catch (Exception e){
					try(PrintWriter pw= new PrintWriter(new FileOutputStream(errfile, true))) {
						pw.print(accession + "\t"+e.getMessage());
					}
				}
				dp.now((index +1)*100/al.size(), String.valueOf(index+1), String.valueOf(al.size()));
			}
		}
		if (failfile.length() == 0L) {
			failfile.delete();
		}
		if (errfile.length() == 0L) {
			errfile.delete();
		}
	}
	/*
	public static void main(String[] args) {
		GetFromUniprot gfu = new GetFromUniprot();
		try {
			gfu.getUniprotEntrys("input/Uniprot id.txt", "result.txt", new DownloadProgress("获取进度"), 0, 50);
		} catch (IOException ex) {
			Logger.getLogger(GetFromUniprot.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InterruptedException ex) {
			Logger.getLogger(GetFromUniprot.class.getName()).log(Level.SEVERE, null, ex);
		}
	}*/
}
