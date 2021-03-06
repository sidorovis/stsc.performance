package stsc.performance;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;

import org.joda.time.LocalDate;

import stsc.common.FromToPeriod;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.storage.StockStorage;
import stsc.general.simulator.multistarter.AlgorithmSettingsIteratorFactory;
import stsc.general.simulator.multistarter.BadParameterException;
import stsc.general.simulator.multistarter.MpDouble;
import stsc.general.simulator.multistarter.MpInteger;
import stsc.general.simulator.multistarter.MpString;
import stsc.general.simulator.multistarter.MpSubExecution;
import stsc.general.simulator.multistarter.StrategySearcherException;
import stsc.general.simulator.multistarter.genetic.SimulatorSettingsGeneticFactory;
import stsc.general.simulator.multistarter.genetic.SimulatorSettingsGeneticListImpl;
import stsc.general.simulator.multistarter.genetic.StrategyGeneticSearcher;
import stsc.general.statistic.MetricType;
import stsc.general.statistic.Metrics;
import stsc.general.statistic.cost.comparator.MetricsSameComparator;
import stsc.general.statistic.cost.function.CostWeightedSumFunction;
import stsc.general.strategy.TradingStrategy;
import stsc.general.strategy.selector.StatisticsByCostSelector;
import stsc.general.strategy.selector.StrategySelector;
import stsc.storage.AlgorithmsStorage;
import stsc.storage.mocks.StockStorageMock;

final class GetBestStatistics {

	private static DecimalFormat df = new DecimalFormat("#0.00");

	static private int maxSelectionIndex = 250;
	static private int populationSize = 250;
	static private int thread = 6;

	static private double bestPart = 0.75;
	static private double crossoverPart = 0.7;

	static private SimulatorSettingsGeneticFactory getFactory() throws BadParameterException, BadAlgorithmException {
		final StockStorage stockStorage = StockStorageMock.getStockStorage();
		final LocalDate startOfPeriod = new LocalDate(2013, 1, 1);
		final LocalDate endOfPeriod = new LocalDate(2014, 1, 1);

		final FromToPeriod period = new FromToPeriod(startOfPeriod.toDate(), endOfPeriod.toDate());

		final SimulatorSettingsGeneticFactory settings = new SimulatorSettingsGeneticFactory(stockStorage, period);

		final AlgorithmSettingsIteratorFactory factoryIn = new AlgorithmSettingsIteratorFactory();
		factoryIn.add(new MpString("e", new String[] { "open", "close" }));
		settings.addStock("in", algoStockName("In"), factoryIn);

		final AlgorithmSettingsIteratorFactory factoryEma = new AlgorithmSettingsIteratorFactory();
		factoryEma.add(new MpDouble("P", 0.1, 1.11, 0.2));
		factoryEma.add(new MpSubExecution("", Arrays.asList(new String[] { "in" })));
		settings.addStock("ema", algoStockName("Ema"), factoryEma);

		final AlgorithmSettingsIteratorFactory factorySma = new AlgorithmSettingsIteratorFactory();
		factorySma.add(new MpDouble("n", 5, 15, 1));
		factorySma.add(new MpSubExecution("", Arrays.asList(new String[] { "in" })));
		settings.addStock("sma", algoStockName("Sma"), factorySma);

		final AlgorithmSettingsIteratorFactory factoryPositionSide = new AlgorithmSettingsIteratorFactory();
		factoryPositionSide.add(new MpSubExecution("", Arrays.asList(new String[] { "ema", "sma" })));
		factoryPositionSide.add(new MpInteger("n", 22, 250, 20));
		factoryPositionSide.add(new MpInteger("m", 20, 40, 2));
		factoryPositionSide.add(new MpDouble("ps", 2500.0, 50000.0, 2500.0));
		factoryPositionSide.add(new MpString("side", new String[] { "long", "short" }));

		return settings.addEod("pnm", algoEodName("PositionNDayMStocks"), factoryPositionSide);
	}

	static private boolean lookForStrategy(int searchIndex, int N)
			throws IOException, BadAlgorithmException, InterruptedException, StrategySearcherException, BadParameterException {
		final SimulatorSettingsGeneticFactory settings = getFactory();

		final SimulatorSettingsGeneticListImpl list = settings.getList();

		final CostWeightedSumFunction cf = new CostWeightedSumFunction();
		cf.withParameter(MetricType.avGain, 0.5);
		cf.withParameter(MetricType.winProb, 1000.0);
		cf.withParameter(MetricType.kelly, 0.6);
		cf.withParameter(MetricType.maxLoss, -0.4);
		cf.withParameter(MetricType.month12AvGain, 0.6);

		final StrategyGeneticSearcher searcher = StrategyGeneticSearcher.getBuilder().withGeneticList(list).withPopulationSize(N)
				.withStrategySelector(new StatisticsByCostSelector(populationSize, cf, new MetricsSameComparator())).withThreadAmount(thread).withPopulationCostFunction(cf)
				.withMaxPopulationsAmount(maxSelectionIndex).withBestPart(bestPart).withCrossoverPart(crossoverPart).build();
		final StrategySelector selector = searcher.waitAndGetSelector();
		final Iterator<TradingStrategy> ts = selector.getStrategies().iterator();
		for (int i = 0; i < N; ++i) {
			TradingStrategy ts1 = ts.next();
			if (printStatistics(String.valueOf(searchIndex) + "+", ts1))
				return true;
			if (!ts.hasNext())
				break;
		}
		return false;
	}

	private static boolean printStatistics(String prefix, TradingStrategy ts) {
		final Metrics s = ts.getMetrics();
		System.out.println(prefix + "\t" + //
				df.format(s.getMetric(MetricType.avGain)) + "\t" + //
				df.format(s.getMetric(MetricType.winProb)) + "\t" + //
				df.format(s.getMetric(MetricType.freq)) + "\t" + //
				df.format(s.getMetric(MetricType.kelly)) + "\t\t" + //
				ts.getSettings().stringHashCode());
		return false;
	}

	private static String algoStockName(String aname) throws BadAlgorithmException {
		return AlgorithmsStorage.getInstance().getStock(aname).getName();
	}

	private static String algoEodName(String aname) throws BadAlgorithmException {
		return AlgorithmsStorage.getInstance().getEod(aname).getName();
	}

	private static void initialize() {
		try {
			AlgorithmsStorage.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			initialize();
			System.out.println("Size:" + getFactory().size());
			System.out.println("  P\tAvGain\tWinProb\tFreq\tKelly");
			for (int i = 0; i < 2; ++i) {
				if (lookForStrategy(i, 10))
					return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
