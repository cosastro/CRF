package org.postagging.data.brown;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.postagging.data.PosTagCorpusReader;
import org.postagging.data.TaggedToken;
import org.postagging.utilities.PosTaggerException;

/**
 * 
 * @author Asher Stern
 * Date: Nov 3, 2014
 *
 */
public class BrownCorpusReader implements PosTagCorpusReader
{
	public static final String CATALOG_FILE_NAME = "cats.txt";

	public BrownCorpusReader(String directoryName)
	{
		super();
		this.directoryName = directoryName;
		initFiles();
		goToNextFile();
	}

	@Override
	public boolean hasNext()
	{
		return thereIsNext;
	}

	@Override
	public List<TaggedToken> next()
	{
		List<TaggedToken> ret = null;
		if (thereIsNext)
		{
			String sentence = sentencesIterator.next();
			BrownTaggedSentenceReader sentenceReader = new BrownTaggedSentenceReader(sentence);
			ret = sentenceReader.read();
		}
		
		if (!sentencesIterator.hasNext())
		{
			goToNextFile();
		}
		
		return ret;
	}
	
	
	private void goToNextFile()
	{
		thereIsNext = false;
		sentencesIterator = null;
		
		boolean stop = false;
		while (!stop)
		{
			File nextFile = getNextFile();
			if (nextFile!=null)
			{
				createSentenceIterator(nextFile);
				stop = sentencesIterator.hasNext();
			}
			else
			{
				sentencesIterator = null;
				stop = true;
			}
		}
		if (sentencesIterator!=null)
		{
			if (!sentencesIterator.hasNext()) {throw new PosTaggerException("BUG");}
			thereIsNext=true;
		}
	}
	
	
	private void initFiles()
	{
		try(BufferedReader catalogReader = new BufferedReader(new FileReader(CATALOG_FILE_NAME)))
		{
			Set<String> fileNames = new LinkedHashSet<String>();
			String line = catalogReader.readLine();
			while (line!=null)
			{
				line = line.trim();
				if (line.length()>0)
				{
					String[] nameAndType = line.split("\\s+");
					if (nameAndType.length>=1)
					{
						String name = nameAndType[0];
						name = name.trim();
						fileNames.add(name);
					}
				}
				line = catalogReader.readLine();
			}
			File directory = new File(directoryName);
			if (!directory.exists()) {throw new PosTaggerException("Directory "+directory.getAbsolutePath()+" does not exist.");}
			if (!directory.isDirectory()) {throw new PosTaggerException("Directory "+directory.getAbsolutePath()+" is not a directory.");}
			
			files = directory.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					return pathname.isFile() && fileNames.contains(pathname.getName());
				}
			});
			nextFileIndex = 0;
		}
		catch (IOException e)
		{
			throw new PosTaggerException("Failed to read catalog file of Brown corpus.",e);
		}
	}
	
	private void createSentenceIterator(File file)
	{
		try(BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			List<String> sentences = new LinkedList<String>();
			String line = reader.readLine();
			while (line != null)
			{
				line = line.trim();
				if (line.length()>0)
				{
					sentences.add(line);
				}
				line = reader.readLine();
			}
			sentencesIterator = sentences.iterator();
		}
		catch (IOException e)
		{
			throw new PosTaggerException("Failed to read file: "+file.getAbsolutePath(),e);
		}
	}
	
	private File getNextFile()
	{
		if (nextFileIndex<files.length)
		{
			File ret = files[nextFileIndex];
			++nextFileIndex;
			return ret;
		}
		else
		{
			return null;
		}
	}

	private final String directoryName;
	
	private File[] files;
	private int nextFileIndex = 0;
	
	private Iterator<String> sentencesIterator = null;
	
	private boolean thereIsNext = false;
}
