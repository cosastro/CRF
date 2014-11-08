package org.postagging.evaluation;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.postagging.data.InMemoryPosTagCorpus;
import org.postagging.data.InMemoryPosTagCorpusImplementation;
import org.postagging.data.PosTagCorpus;
import org.postagging.data.PosTagCorpusReader;
import org.postagging.data.TrainTestPosTagCorpus;
import org.postagging.data.brown.BrownCorpusReader;
import org.postagging.lingpipe.LingPipeWrapperPosTaggerTrainer;
import org.postagging.postaggers.PosTagger;
import org.postagging.utilities.ExceptionUtil;
import org.postagging.utilities.RuntimeUtilities;
import org.postagging.utilities.log4j.Log4jInit;

/**
 * 
 * @author Asher Stern
 * Date: Nov 4, 2014
 *
 */
public class TrainAndEvaluate
{

	/**
	 * 
	 * @param args 1. corpus. 2. train-size (how many train sentences, where the rest are test sentences).
	 * <BR>
	 * If train-size <=0, then the whole corpus is train, and the text is on the training data.
	 */
	public static void main(String[] args)
	{
		Log4jInit.init(Level.DEBUG);
		try
		{
			int testSize = 0;
			if (args.length>=3) {testSize = Integer.parseInt(args[2]);}
			new TrainAndEvaluate(args[0],Integer.parseInt(args[1]),testSize).go();
		}
		catch(Throwable t)
		{
			ExceptionUtil.logException(t, logger);
		}
	}
	

	
	
	
	public TrainAndEvaluate(String brownDirectory, int trainSize, int testSize)
	{
		super();
		this.brownDirectory = brownDirectory;
		this.trainSize = trainSize;
		this.testSize = testSize;
	}





	public void go()
	{
		TrainTestPosTagCorpus corpus = createCorpus();
		logger.info("Training...");
		PosTagger posTagger = train(corpus.createTrainCorpus());
		logger.info("Training - done.");
		logger.info(RuntimeUtilities.getUsedMemory());
		
		logger.info("Evaluating...");
		AccuracyEvaluator evaluator = new AccuracyEvaluator(corpus.createTestCorpus(), posTagger);
		evaluator.evaluate();
		logger.info("Accuracy = " + String.format("%-3.3f", evaluator.getAccuracy()));
		logger.info("Correct = "+evaluator.getCorrect());
		logger.info("Incorrect = "+evaluator.getIncorrect());
	}
	

	private TrainTestPosTagCorpus createCorpus()
	{
		return new TrainTestPosTagCorpus(trainSize, testSize,
				new PosTagCorpus()
				{
					@Override
					public PosTagCorpusReader createReader()
					{
						return new BrownCorpusReader(brownDirectory);
					}
				}
		);
	}

	
	private PosTagger train(PosTagCorpus corpus)
	{
		LingPipeWrapperPosTaggerTrainer trainer = new LingPipeWrapperPosTaggerTrainer();
		InMemoryPosTagCorpus inMemoryCorpus = new InMemoryPosTagCorpusImplementation(corpus);
		trainer.train(inMemoryCorpus);
		PosTagger posTagger = trainer.getTrainedPosTagger();
		return posTagger;
	}
	

	private final String brownDirectory;
	private final int trainSize;
	private final int testSize;
	
	private static final Logger logger = Logger.getLogger(TrainAndEvaluate.class);
}
