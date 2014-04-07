package stsc.storage;

import java.text.ParseException;

import stsc.algorithms.AlgorithmSettings;
import stsc.algorithms.BadAlgorithmException;
import stsc.algorithms.EodExecution;
import stsc.algorithms.StockExecution;
import stsc.algorithms.factors.primitive.Sma;
import stsc.algorithms.primitive.TestingEodAlgorithm;
import stsc.testhelper.StockStorageHelper;
import stsc.trading.Broker;
import junit.framework.TestCase;

public class ExecutionsStorageTest extends TestCase {

	public void testExecutionsStorage() throws BadAlgorithmException, ParseException {
		AlgorithmSettings smaSettings = AlgorithmSettings.create00s();
		smaSettings.addSubExecutionName("asd");

		final ExecutionsStorage es = new ExecutionsStorage();
		es.addStockExecution(new StockExecution("t2", Sma.class, smaSettings));
		es.addEodExecution(new EodExecution("t1", TestingEodAlgorithm.class, AlgorithmSettings.create00s()));
		es.initialize(new Broker(new StockStorageHelper()));

		assertEquals(1, es.getEodAlgorithmsSize());

		assertNotNull(es.getEodAlgorithm("t1"));
		assertNull(es.getEodAlgorithm("t2"));

		assertNotNull(es.getStockAlgorithm("t2", "aapl"));
		assertNotNull(es.getStockAlgorithm("t2", "goog"));
		assertNotNull(es.getStockAlgorithm("t2", "epl"));

		assertNull(es.getStockAlgorithm("t1", "aapl"));
		assertNull(es.getStockAlgorithm("t1", "goog"));
		assertNull(es.getStockAlgorithm("t1", "epl"));

		assertNull(es.getStockAlgorithm("t2", "epl2"));
	}

	public void testExceptionOnInit() throws BadAlgorithmException, ParseException {
		final ExecutionsStorage es = new ExecutionsStorage();
		es.addStockExecution(new StockExecution("t2", Sma.class, AlgorithmSettings.create00s()));

		boolean throwed = false;
		try {
			es.initialize(new Broker(new StockStorageHelper()));
		} catch (BadAlgorithmException e) {
			throwed = true;
		}
		assertEquals(true, throwed);
	}
}