package com.hc360.rsf.common.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;

public class FileUtil{
	public final static DecimalFormat DF = new DecimalFormat("#0.00");
	
	/**
	 * 把文件的大小(字节) 转换为 K 或 M
	 * 
	 * @param fileSize
	 *            文件的大小(单位字节)
	 * @return 文件的大小 单位K 或 M
	 */
	public static String fileSize(final long fileSize)
	{
		try
		{
			if (0 <= fileSize && fileSize < 1024)
			{
				return fileSize + "字节";
			}
			else if (1024 <= fileSize && fileSize < (1024 * 1024))
			{
				final float f = (float) fileSize / 1024;

				return new Float(DF.format(f)).toString() + "KB";
			}
			else if ((1024 * 1024) <= fileSize && fileSize < (1024 * 1024 * 1024l))
			{
				final float f = (float) fileSize / (1024 * 1024);
				return new Float(DF.format(f)).toString() + "MB";
			}
			else if ((1024 * 1024 * 1024) <= fileSize && fileSize < (1024 * 1024 * 1024 * 1024l))
			{
				final float f = (float) fileSize / (1024 * 1024 * 1024);
				return new Float(DF.format(f)).toString() + "GB";
			}
			return fileSize + "字节";
		}
		catch (final Exception e)
		{
			return fileSize + "字节";
		}
	}
	
	/**
	 * 创建文件, 父目录不存在则创建
	 * 
	 * @param pathAndFileName
	 *            文件全名 路径+文件名
	 * @return 成功:File对象 , 失败:null
	 * @throws IOException
	 */

	public static File createFile(final String pathAndFileName) throws IOException
	{
		if (null == pathAndFileName)
		{
			return null;
		}
		final File f = new File(pathAndFileName);
		final File pf = f.getParentFile();
		if (null != pf && !pf.exists())
		{
			pf.mkdirs(); //创建所须的父目录
		}
		final boolean bl = f.createNewFile();
		if (bl)
		{
			return f;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 创建多级目录
	 * 
	 * @param[in] sPath 目录
	 * @return 是否创建成功
	 */
	public static boolean createFolder(final String sPath)
	{
		try
		{
			final File oPath = new File(sPath);
			if (!oPath.exists())
			{
				oPath.mkdirs();
			}
			return true;
		}
		catch (final Exception e)
		{
			return false;
		}
	}

	/**
	 * 创建指定的目录，包括创建必需但不存在的父目录。注意，如果此操作失败，可能已成功创建了一些必需的父目录。
	 * 
	 * @param mrName
	 *            指定的目录
	 * @return 成功:File对象 , 失败:null
	 */
	public static File createMr(final String mrName)
	{
		if (null == mrName)
		{
			return null;
		}
		final File file = new File(mrName.trim());
		final boolean bl = file.mkdirs();
		if (bl)
		{
			return file;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 复制文件
	 * 
	 * @param[in] sFile1
	 * @param[in] sFile2
	 * @throws IOException
	 */
	public static void copyFile(final String sFile1, final String sFile2) throws IOException
	{
		final File oFile1 = new File(sFile1);
		if (oFile1.exists())
		{
			final String sPath = sFile2.substring(0, sFile2.lastIndexOf('/'));
			createFolder(sPath); // 确保目标目录存在

			final File oFile2 = new File(sFile2);
			final RandomAccessFile inData = new RandomAccessFile(oFile1, "r");
			final RandomAccessFile opData = new RandomAccessFile(oFile2, "rw");
			final FileChannel inChannel = inData.getChannel();
			final FileChannel opChannel = opData.getChannel();
			inChannel.transferTo(0, inChannel.size(), opChannel);
			//=========================上一行代码与下面的代码功能相同=========================
			//			final long size = inChannel.size();
			//			final MappedByteBuffer buf = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, size);
			//			opChannel.write(buf);
			//=================================================================
			inChannel.close();
			inData.close();
			opChannel.close();
			opData.close();
		}
	}

	/**
	 * 规格化路径
	 * 
	 * @param[in] sPath
	 * @return
	 */
	public static String FormatPath(final String sPath)
	{
		final String sTemp = sPath.replace('/', '\\');
		return sTemp.endsWith("\\") ? sTemp : (sTemp + "\\");
	}

	/**
	 * 嵌套删除多级目录
	 * 
	 * @param[in] oPath 目录
	 */
	private static void deleteFolder(final File oPath)
	{
		final File[] dirs = oPath.listFiles();
		if (dirs != null)
		{
			for (final File oSubPath : dirs)
			{
				if (oSubPath.isDirectory())
				{
					deleteFolder(oSubPath);
				}
			}
		}
		oPath.delete();
	}

	/**
	 * 删除单级目录
	 * 
	 * @param[in] sPath 目录
	 */
	public static void deleteFolder(final String sPath)
	{
		final File oPath = new File(sPath);
		if (!oPath.exists() || !oPath.isDirectory())
		{
			return;
		}

		deleteFolder(oPath);
	}

	/**
	 * 分解文件名, 分解出文件名与扩展名,从文件全名的中的最后一个"."做分割
	 * 
	 * @param FileName
	 *            文件全名 ,如 abc.jpg
	 * @return String数组 下标0:文件名 如 abc ,下标1:扩展名 如: .jpg
	 */
	public static String[] apartFileName(final String FileName)
	{
		if (FileName == null)
		{
			return null;
		}
		final String[] r = new String[2];
		try
		{
			final int end = FileName.lastIndexOf(".");
			final String p1 = FileName.substring(0, end); //文件名 ,无扩展名
			final String p2 = FileName.substring(end); //扩展名  如 .jpg
			r[0] = p1;
			r[1] = p2;
		}
		catch (final Exception e)
		{
		}
		return r;
	}
	/**
	 * 从文件名中提取文件扩展名
	 * 
	 * @param fileName
	 *            文件名
	 * @return 扩展名(小写)
	 */
	public static String getFileType(final String fileName)
	{
		if (null == fileName || fileName.equals(""))
		{
			return "";
		}
		if (fileName.lastIndexOf(".") != -1)
		{
			final String fileType = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
			return fileType.toLowerCase();
		}
		else
		{
			return "";
		}
	}
}
