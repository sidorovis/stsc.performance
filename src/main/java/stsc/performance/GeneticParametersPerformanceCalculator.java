package stsc.performance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stsc.common.storage.StockStorage;
import stsc.storage.AlgorithmsStorage;
import stsc.storage.mocks.StockStorageMock;

class GeneticParametersPerformanceCalculator {

	private static Logger logger = LogManager.getLogger("GeneticParameterPerformance");

	private static StockStorage stockStorage;

	private static void initialize() {
		try {
			AlgorithmsStorage.getInstance();
			stockStorage = StockStorageMock.getStockStorage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		logger.debug("Process started");
		initialize();
		logger.debug("Algorithms and Stocks reader");
		try {
			final PerformanceCalculatorSettings settings = new PerformanceCalculatorSettings();
			settings.searcherType = SearcherType.GENETIC_SEARCHER;
			settings.printAdditionalInfo = true;
			settings.printStarterInfo = false;
			settings.printAvGainAndTime = true;
			settings.calculationsForAverage = 10;

			System.out.println("Size of stocks: " + stockStorage.getStockNames().size());
			PerformanceCalculator.calculateAmountOfSimulations(stockStorage, settings);

			for (int i = 50; i <= 350; i += 50)
				for (int u = 50; u <= 350; u += 50) {
					settings.maxSelectionIndex = i;
					settings.populationSize = u;
					new PerformanceCalculator(settings).calculateSmallStatistics();
				}
			logger.debug("Performance Calculator finished");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
