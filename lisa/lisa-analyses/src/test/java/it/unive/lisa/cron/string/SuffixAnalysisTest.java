package it.unive.lisa.cron.string;

import static it.unive.lisa.LiSAFactory.getDefaultFor;

import it.unive.lisa.AnalysisSetupException;
import it.unive.lisa.AnalysisTestExecutor;
import it.unive.lisa.CronConfiguration;
import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.string.Suffix;
import it.unive.lisa.analysis.types.InferredTypes;
import org.junit.Test;

public class SuffixAnalysisTest extends AnalysisTestExecutor {

	@Test
	public void testSuffix() throws AnalysisSetupException {
		CronConfiguration conf = new CronConfiguration();
		conf.serializeResults = true;
		conf.abstractState = getDefaultFor(AbstractState.class, getDefaultFor(HeapDomain.class), new Suffix(),
				new TypeEnvironment<>(new InferredTypes()));
		conf.testDir = "suffix";
		conf.programFile = "program.imp";
		perform(conf);
	}
}
