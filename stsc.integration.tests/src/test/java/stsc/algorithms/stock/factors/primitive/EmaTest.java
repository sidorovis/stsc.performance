package stsc.algorithms.stock.factors.primitive;

import java.io.IOException;
import java.util.ArrayList;

import org.joda.time.LocalDate;

import stsc.algorithms.In;
import stsc.algorithms.stock.factors.primitive.Ema;
import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.Settings;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.stocks.Stock;
import stsc.common.stocks.UnitedFormatStock;
import stsc.signals.DoubleSignal;
import stsc.testhelper.StockAlgoInitHelper;
import junit.framework.TestCase;

public class EmaTest extends TestCase {
	public void testEma() throws IOException, BadSignalException, BadAlgorithmException {

		final StockAlgoInitHelper stockInit = new StockAlgoInitHelper("testIn", "aapl");
		stockInit.getSettings().set("e", "open");
		final In inAlgo = new In(stockInit.getInit());

		final StockAlgoInitHelper init = new StockAlgoInitHelper("testEma", "aapl", stockInit.getStorage());
		init.getSettings().set("P", 0.3);
		init.getSettings().setInteger("size", 100000);
		init.getSettings().addSubExecutionName("testIn");

		final Ema ema = new Ema(init.getInit());

		final Stock aapl = UnitedFormatStock.readFromUniteFormatFile("./test_data/aapl.uf");
		final int aaplIndex = aapl.findDayIndex(new LocalDate(2011, 9, 4).toDate());
		final ArrayList<Day> days = aapl.getDays();

		for (int i = aaplIndex; i < days.size(); ++i) {
			final Day day = days.get(i);
			inAlgo.process(day);
			ema.process(day);
		}

		assertEquals(days.get(aaplIndex).getPrices().getOpen(), init.getStorage().getStockSignal("aapl", "testEma", 0).getSignal(DoubleSignal.class).value);

		final double secondValue = days.get(aaplIndex).getPrices().getOpen() * 0.7 + 0.3 * days.get(aaplIndex + 1).getPrices().getOpen();

		assertEquals(secondValue, init.getStorage().getStockSignal("aapl", "testEma", 1).getSignal(DoubleSignal.class).value);

		final int size = init.getStorage().getIndexSize("aapl", "testEma");
		assertEquals(531.20111321, init.getStorage().getStockSignal("aapl", "testEma", size - 1).getSignal(DoubleSignal.class).value, Settings.doubleEpsilon);
	}
}