package stsc.performance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;

import stsc.common.TimeTracker;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.storage.StockStorage;
import stsc.general.simulator.SimulatorConfiguration;
import stsc.general.simulator.SimulatorConfigurationImpl;
import stsc.general.simulator.multistarter.genetic.SimulatorSettingsGeneticFactory;
import stsc.general.simulator.multistarter.genetic.GeneticList;
import stsc.general.simulator.multistarter.genetic.SimulatorSettingsGeneticListImpl;
import stsc.general.simulator.multistarter.grid.SimulatorSettingsGridFactory;
import stsc.general.simulator.multistarter.grid.SimulatorSettingsGridList;
import stsc.storage.mocks.StockStorageMock;

final class MemoryConsumption {

	private static boolean warmUp = true;
	private static boolean onlyReport = true;

	private final static StockStorage stockStorage = StockStorageMock.getStockStorage();

	static private String getDateRepresentation(LocalDate date) {
		int day = date.getDayOfMonth();
		int month = date.getMonthOfYear();
		int year = date.getYear();
		return String.format("%02d-%02d-%04d", day, month, year);
	}

	public MemoryConsumption(SearcherType type, long N, boolean testMemory) throws IOException, BadAlgorithmException {
		if (type == SearcherType.GRID_SEARCHER)
			gridConsumptionCheck(N, testMemory);
		else
			geneticRandomConsumptionCheck(N, testMemory);
	}

	private static void gridConsumptionCheck(long N, boolean testMemory) throws IOException {
		final List<String> elements = Arrays.asList(new String[] { "open", "high", "low", "close", "value", "open", "high", "low", "close" });
		final LocalDate startOfPeriod = new LocalDate(1970, 1, 1);
		final LocalDate endOfPeriod = new LocalDate(2014, 1, 1);
		final TimeTracker tt = new TimeTracker();
		final SimulatorSettingsGridFactory factory = SimulatorSettingsGenerator.getGridFactory(false, stockStorage, elements, getDateRepresentation(startOfPeriod),
				getDateRepresentation(endOfPeriod));
		final long size = factory.size();
		final SimulatorSettingsGridList list = factory.getList();
		final Iterator<SimulatorConfigurationImpl> iterator = list.iterator();
		final ArrayList<SimulatorConfigurationImpl> settings = new ArrayList<SimulatorConfigurationImpl>((int) Math.min(100000, N));
		for (long i = 0; i < N; ++i) {
			if (!iterator.hasNext()) {
				break;
			}
			if (!testMemory && (i % 1000000 == 0) && !onlyReport) {
				System.out.println(" " + i + " " + TimeTracker.lengthInSeconds(tt.lengthNonStop()));
			}
			final SimulatorConfigurationImpl setting = iterator.next();
			if (testMemory)
				settings.add(setting);
		}
		if (testMemory) {
			System.out.println("Time in secs: (" + N + "/" + size + ") " + TimeTracker.lengthInSeconds(tt.finish()));
			System.in.read();
		} else {
			if (!onlyReport)
				System.out.println(" " + N + " " + TimeTracker.lengthInSeconds(tt.lengthNonStop()));
			if (!warmUp)
				System.out.print(TimeTracker.lengthInSeconds(tt.finish()) + " ");
		}
	}

	static private void geneticRandomConsumptionCheck(long N, boolean testMemory) throws IOException, BadAlgorithmException {
		final List<String> elements = Arrays.asList(new String[] { "open", "high", "low", "close", "value", "open", "high", "low", "close" });
		final LocalDate startOfPeriod = new LocalDate(1970, 1, 1);
		final LocalDate endOfPeriod = new LocalDate(2014, 1, 1);
		final TimeTracker tt = new TimeTracker();
		final SimulatorSettingsGeneticFactory factory = SimulatorSettingsGenerator.getGeneticFactory(false, stockStorage, elements, getDateRepresentation(startOfPeriod),
				getDateRepresentation(endOfPeriod));
		final long size = factory.size();
		final GeneticList list = factory.getList();
		final ArrayList<SimulatorConfiguration> settings = new ArrayList<SimulatorConfiguration>((int) Math.min(100000, N));
		for (long i = 0; i < N; ++i) {
			if (!testMemory && (i % 1000000 == 0) && !onlyReport) {
				System.out.println(" " + i + " " + TimeTracker.lengthInSeconds(tt.lengthNonStop()));
			}
			final SimulatorConfiguration setting = list.generateRandom();
			if (testMemory)
				settings.add(setting);
		}
		if (testMemory) {
			System.out.println("Time in secs: (" + N + "/" + size + ") " + TimeTracker.lengthInSeconds(tt.finish()));
			System.in.read();
		} else {
			if (!onlyReport)
				System.out.println(" " + N + " " + TimeTracker.lengthInSeconds(tt.lengthNonStop()));
			if (!warmUp)
				System.out.print(TimeTracker.lengthInSeconds(tt.finish()) + " ");
		}
	}

	static private void geneticMergeConsumptionCheck(long N, boolean testMemory) throws IOException, BadAlgorithmException {
		final List<String> elements = Arrays.asList(new String[] { "open", "high", "low", "close", "value", "open", "high", "low", "close" });
		final LocalDate startOfPeriod = new LocalDate(1970, 1, 1);
		final LocalDate endOfPeriod = new LocalDate(2014, 1, 1);
		final TimeTracker tt = new TimeTracker();
		final SimulatorSettingsGeneticFactory factory = SimulatorSettingsGenerator.getGeneticFactory(false, stockStorage, elements, getDateRepresentation(startOfPeriod),
				getDateRepresentation(endOfPeriod));
		final long size = factory.size();
		final SimulatorSettingsGeneticListImpl list = factory.getList();
		SimulatorConfiguration left = list.generateRandom();
		SimulatorConfiguration right = list.generateRandom();
		final ArrayList<SimulatorConfiguration> settings = new ArrayList<SimulatorConfiguration>((int) Math.min(100000, N));
		for (long i = 0; i < N; ++i) {
			if (!testMemory && (i % 1000000 == 0) && !onlyReport) {
				System.out.println(" " + i + " " + TimeTracker.lengthInSeconds(tt.lengthNonStop()));
			}
			final SimulatorConfiguration setting = list.merge(left, right);
			if (testMemory)
				settings.add(setting);
		}
		if (testMemory) {
			System.out.println("Time in secs: (" + N + "/" + size + ") " + TimeTracker.lengthInSeconds(tt.finish()));
			System.in.read();
		} else {
			if (!onlyReport)
				System.out.println(" " + N + " " + TimeTracker.lengthInSeconds(tt.lengthNonStop()));
			if (!warmUp)
				System.out.print(TimeTracker.lengthInSeconds(tt.finish()) + " ");
		}
	}

	static private void geneticMutateConsumptionCheck(long N, boolean testMemory) throws IOException, BadAlgorithmException {
		final List<String> elements = Arrays.asList(new String[] { "open", "high", "low", "close", "value", "open", "high", "low", "close" });
		final LocalDate startOfPeriod = new LocalDate(1970, 1, 1);
		final LocalDate endOfPeriod = new LocalDate(2014, 1, 1);
		final TimeTracker tt = new TimeTracker();
		final SimulatorSettingsGeneticFactory factory = SimulatorSettingsGenerator.getGeneticFactory(false, stockStorage, elements, getDateRepresentation(startOfPeriod),
				getDateRepresentation(endOfPeriod));
		final long size = factory.size();
		final SimulatorSettingsGeneticListImpl list = factory.getList();
		SimulatorConfiguration left = list.generateRandom();
		final ArrayList<SimulatorConfiguration> settings = new ArrayList<SimulatorConfiguration>((int) Math.min(100000, N));
		for (long i = 0; i < N; ++i) {
			if (!testMemory && (i % 1000000 == 0) && !onlyReport) {
				System.out.println(" " + i + " " + TimeTracker.lengthInSeconds(tt.lengthNonStop()));
			}
			final SimulatorConfiguration setting = list.mutate(left);
			if (testMemory)
				settings.add(setting);
		}
		if (testMemory) {
			System.out.println("Time in secs: (" + N + "/" + size + ") " + TimeTracker.lengthInSeconds(tt.finish()));
			System.in.read();
		} else {
			if (!onlyReport)
				System.out.println(" " + N + " " + TimeTracker.lengthInSeconds(tt.lengthNonStop()));
			if (!warmUp)
				System.out.print(TimeTracker.lengthInSeconds(tt.finish()) + " ");
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			final long N = 5000000;
			final long lastN = N;
			final long stepN = N / 1;
			new MemoryConsumption(SearcherType.GRID_SEARCHER, 2 * N, false);
			new MemoryConsumption(SearcherType.GENETIC_SEARCHER, 2 * N, false);
			warmUp = false;
			System.out.print(" ");
			for (long i = N; i <= lastN; i += stepN) {
				System.out.print(i + " ");
			}
			System.out.println();
			System.out.print("Grid ");
			for (long i = N; i <= lastN; i += stepN) {
				gridConsumptionCheck(i, false);
			}
			System.out.println();
			System.out.print("Random ");
			for (long i = N; i <= lastN; i += stepN) {
				geneticRandomConsumptionCheck(i, false);
			}
			System.out.println();
			System.out.print("Merge ");
			for (long i = N; i <= lastN; i += stepN) {
				geneticMergeConsumptionCheck(i, false);
			}
			System.out.println();
			System.out.print("Mutate ");
			for (long i = N; i <= lastN; i += stepN) {
				geneticMutateConsumptionCheck(i, false);
			}
			System.out.println();
		} catch (BadAlgorithmException e) {
			e.printStackTrace();
		}
	}
}
