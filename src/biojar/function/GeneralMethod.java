/**
 * Copyright 1997-2021 <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>.
 * 
 * Modified at 2021-02-04
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
package biojar.function;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
/**
 * 程序包中通用自定义方法的集合类
 * @version 1.0
 * @since 14 2021-02-05
 * @author <a href="mailto:zhanghn@zju.edu.cn">Zhang Hongning</a>
 */
public class GeneralMethod {
	/**
	 * 编码方式，默认为UTF-8
	 */
	private static String encoding = "UTF-8";
	
	/**
	 * 清空目录下内容但保留目录
	 * <pre class="code">
	 * GeneralMethod.clearDirectory(new java.io.File("dir"));
	 * </pre>
	 * @param directory 需要清空的目录
	 * @throws Exception 当删除子目录失败或者输入参数不是目录时会抛出
	 */
	public static void clearDirectory(File directory) throws Exception {
		if (directory!=null&&directory.isDirectory()) {
			File[] subfiles = directory.listFiles();
			for (File subfile: subfiles) {
				if (subfile.isDirectory()) {
					removeDirectory(subfile);
				} else {
					if (!subfile.delete()) {
						throw new Exception("Can't delete file "+subfile.getAbsolutePath());
					}
				}
			}
		} else if (directory.exists()) {
			throw new Exception("Argument should be represent of a directory!");
		}
	}
	/**
	 * 删除目录，包括非空目录
	 * <pre class="code">
	 * GeneralMethod.removeDirectory(new java.io.File("dir"));
	 * </pre>
	 * @param directory 待删除目录
	 * @throws Exception 当删除子目录失败或者输入参数不是目录时会抛出
	 */
	public static void removeDirectory(File directory) throws Exception {
		if (directory!=null&&directory.isDirectory()) {
			File[] subfiles = directory.listFiles();
			for (File subfile: subfiles) {
				if (subfile.isDirectory()) {
					removeDirectory(subfile);
				} else {
					if (!subfile.delete()) {
						throw new Exception("Can't delete file "+subfile.getAbsolutePath());
					}
				}
			}
			if(!directory.delete()) {
				throw new Exception("Can't delete directory "+ directory.getAbsolutePath());
			}
		} else if (directory.exists()) {
			throw new Exception("Argument should be represent of a directory!");
		}
	}
	/**
	 * 将不定个数的{@code Object}或特定{@code Object[]}的元素调用{@code toString}方法转为字符串并用连接符拼接
	 * <pre class="code">
	 * GeneralMethod.join("\t", 12, "Zhang", true);
	 * GeneralMethod.join("\t", new String[]{"Zhang", "ZHN"});
	 * </pre>
	 * @param connect 连接符字符串
	 * @param args 待拼接的{@code Object[]}
	 * @return 拼接完成的{@code String}对象
	 * @see GeneralMethod#join(Object[])
	 * @see GeneralMethod#join(String, Collection)
	 */
	public static String join(String connect, Object... args) {
		Object res = null;
		if (args == null||connect==null) return null;
		for (Object e:args) {
			if (res == null) {
				res = e;
			} else {
				res += (connect + e);
			}
		}
		return (String) res;
	}
	/**
	 * 将特定{@code Object[]}的元素调用{@code toString}方法转为字符串并用水平制表符拼接
	 * @param args 需要合并成字符串的数组或不定个数参数对象
	 * @return 以回车符号连接数组返回的@{code String}
	 * @see GeneralMethod#join(String, Object...)
	 * @see GeneralMethod#join(String, Collection)
	 */
	public static String join(Object[] args) {
		return join("\t", args);
	}
	/**
	 * 将{@code java.until.Collection}接口实现类的元素转为字符串并以特定连接符拼接
	 * <pre class="code">
	 * GeneralMethod.join("\t", new ArrayList());
	 * GeneralMethod.join("\t", new List());
	 * GeneralMethod.join("\t", new HashSet());
	 * GeneralMethod.join("\t", new Set());
	 * </pre>
	 * @param connect 连接符字符串
	 * @param collection 待连接元素的具体集合
	 * @return 拼接完成的{@code String}
	 */
	public static String join(String connect, Collection<?> collection) {
		String res = "";
		boolean isFirst = true;
		for (Object curr: collection) {
			if (isFirst) {
				res += curr;
				isFirst = false;
			} else {
				res += (connect + curr);
			}
		}
		return res;
	}
	/**
	 * 构建{@code java.io.LineNumberReader}对象以读取文件
	 * @param filename 待读取文件的名称
	 * @return	{@code java.io.LineNumberReader}对象
	 * @throws FileNotFoundException 文件未找到时抛出
	 * @see GeneralMethod#BufferRead(File)
	 */
	public static LineNumberReader BufferRead(String filename) throws FileNotFoundException {
		File inputfile =  new File(filename);
		return BufferRead(inputfile);
	}
	/**
	 * 构建{@code java.io.LineNumberReader}对象以读取文件
	 * @param inputfile 待读取的{@code java.io.File}对象
	 * @return	{@code java.io.LineNumberReader}对象
	 * @throws FileNotFoundException 文件未找到时抛出
	 * @see GeneralMethod#BufferRead(String)
	 */
	public static LineNumberReader BufferRead(File inputfile) throws FileNotFoundException {
			FileInputStream fis = new FileInputStream(inputfile);
			try {
				InputStreamReader isr = new InputStreamReader(fis, encoding);
				return new LineNumberReader(isr);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return new LineNumberReader(new InputStreamReader(fis));//转化为字符输入流并带缓冲区读取
	}
	/**
	 * 指定构建{@code java.util.Scanner}对象以读取文件
	 * @param filename 待读取文件的名称
	 * @return 构建的{@code java.util.Scanner}对象
	 * @throws FileNotFoundException 文件缺失时抛出
	 */
	public static Scanner ScanRead(String filename) throws FileNotFoundException {
		return new Scanner(new FileInputStream(filename) ,encoding);
	}
	/**
	 * 解压gz压缩文件
	 * @param gzipfile 指定的gz文件的名称
	 * @param releasefile 指定的输出文件名
	 * @return 解压状态，{@code true}为解压成功, {@code false}为解压失败
	 */
	public static boolean ungzip(String gzipfile, String releasefile) {
		try {
			try (GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(gzipfile))) {
				try (FileOutputStream fos = new FileOutputStream(releasefile)) {
					byte[] bytes = new byte[10240];
					int len;
					while ((len = gzip.read(bytes))!= -1) {
						fos.write(bytes, 0, len);
					}
				}
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	/**
	 * 判断一个{@code java.util.ArrayList}对象的某索引值是否已经定义
	 * @param args 要检查的{@code java.util.ArrayList}对象
	 * @param index 要检查的索引值
	 * @return boolean值，true代表已经定义
	 */
	public static boolean defined(ArrayList<?> args, int index) {//判断arraylist值是否定义
		return index >=0 && index < args.size();
	}
	/**
	 * 对字符串数组排序，order=true为升序
	 * @param array 要排序的字符串数组
	 * @param order 排序设定， true为升序，false为降序
	 * @return 排序后产生的字符串数组
	 */
	public static String[] sorted(String[] array, boolean order) {
		String[] newArray = Arrays.copyOf(array, array.length);
		if (order) {
			Arrays.sort(newArray, Comparator.naturalOrder());
		} else {
			Arrays.sort(newArray, Comparator.reverseOrder());
		}
		return newArray;
	}
	/**
	 * 用{@code java.util.Set<String>}接口实例对象产生一个有序的{@code java.util.ArrayList<String>}对象
	 * @param set 要排序的{@code java.util.Set<String>}t对象
	 * @param order 排序设定， true为升序，false为降序
	 * @return 排序后产生的{@code java.util.ArrayList<String>}对象
	 */
	public static ArrayList<String> sorted(Set<String> set, boolean order) {
		ArrayList<String> tmp = new ArrayList<>(set);
		return sorted(tmp, order);
	}
	/**
	 * 将{@code java.until.List<String>}接口实现类进行自定义排序，并返回排序后的对象。不改变原先对象的元素顺序。
	 * 支持通过Lambda表达式自定义参与排序比较的整型值。
	 * @param <T> {@code List<String>}接口实现类的泛型
	 * @param list 具体的{@code List<String>}接口实现类
	 * @param order 是否为升序排序，{@code true}为升序排序，{@code false}为降序排序
	 * @param lambda 函数式接口（Lambda表达式），若不为null，将以其返回值作为排序依据。
	 * 其应接收一个{@code String}对象，返回一个{@code Integer}对象
	 * @return 排序后产生的新{@code List<String>}接口实现类
	 */
	public static <T extends List<String>> T sorted(T list, boolean order, Function<String, Integer> lambda) {
		if (list == null) return null;
		try {
			/*
			 * 获取泛型方法中泛型T的真实类型，基于反射创建真实类型, Class类的泛型指定了具体是什么类型的Class
			 * Class<?>代表了任意类型，由传入参数决定。单独Class，不指定泛型则代表Object类
			 * */
			@SuppressWarnings("unchecked")
			Class<T> cl = (Class<T>) list.getClass();
			T res = cl.getDeclaredConstructor().newInstance();//拷贝对象
			res.addAll(list);//部分List实现类构造器不支持直接用Collection为参数进行构造，但addAll方法是接口方法
			if (order) {
				if (lambda==null) {
					res.sort(Comparator.naturalOrder());
				} else {
					res.sort((String s1, String s2)->{
						return lambda.apply(s1).compareTo(lambda.apply(s2));
					});
				}
			} else {
				if (lambda==null) {
					res.sort(Comparator.reverseOrder());
				} else {
					res.sort((String s1, String s2)->{
						return lambda.apply(s2).compareTo(lambda.apply(s1));
					});
				}
			}
			return res;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException | ClassCastException e) {
			e.printStackTrace();
		}
		return list;
	}
	/**
	 * 将{@code java.until.List<String>}接口实现类进行字典序排序，并返回排序后的对象。不改变原先对象的元素顺序。
	 * @param <T> {@code List<String>}接口实现类的泛型
	 * @param list 具体的{@code List<String>}接口实现类
	 * @param order 是否为升序排序，{@code true}为升序排序，{@code false}为降序排序
	 * @return 排序后产生的新{@code List<String>}接口实现类
	 */
	public static <T extends List<String>> T sorted(T list, boolean order) {
		return sorted(list, order, null);
	}
	/**
	 * 对数组求和
	 * @param args 输入求和的数组
	 * @return 输出数组的元素和
	 */
	public static int sumNumber(int[] args) {
		int res = 0;
		for (int e: args) res += e;
		return res;
	}
}