package stsc.algorithms.stock.indices;

import java.util.List;

import stsc.algorithms.AlgorithmSettingsImpl;
import stsc.algorithms.stock.factors.primitive.Sma;
import stsc.common.BadSignalException;
import stsc.common.Day;
import stsc.common.algorithms.AlgorithmSetting;
import stsc.common.algorithms.BadAlgorithmException;
import stsc.common.algorithms.StockAlgorithm;
import stsc.common.algorithms.StockAlgorithmInit;
import stsc.common.signals.SignalsSerie;
import stsc.common.signals.StockSignal;
import stsc.signals.DoubleSignal;
import stsc.signals.series.LimitSignalsSerie;

public class BollingerBands extends StockAlgorithm {

	private final AlgorithmSetting<Integer> N;
	private final AlgorithmSetting<Integer> K;
	private Integer size;

	private final Sma sma;
	private final String smaName;

	public BollingerBands(StockAlgorithmInit init) throws BadAlgorithmException {
		super(init);
		N = init.getSettings().getIntegerSetting("N", 20);
		K = init.getSettings().getIntegerSetting("K", 2);
		this.smaName = "BB_Sma_" + init.getExecutionName();

		this.sma = createSma(init);
		// this.stdev = createStdDev(init);
	}

	private Sma createSma(StockAlgorithmInit init) throws BadAlgorithmException {
		final AlgorithmSettingsImpl settings = new AlgorithmSettingsImpl(init);
		settings.setInteger("N", N.getValue());
		settings.setInteger("size", size);
		settings.getSubExecutions().addAll(init.getSettings().getSubExecutions());
		final StockAlgorithmInit smaInit = new StockAlgorithmInit(smaName, init, settings);
		return new Sma(smaInit);
	}

	@Override
	public SignalsSerie<StockSignal> registerSignalsClass(StockAlgorithmInit initialize) throws BadAlgorithmException {
		size = initialize.getSettings().getIntegerSetting("size", 2).getValue().intValue();
		return new LimitSignalsSerie<StockSignal>(DoubleSignal.class, size);
	}

	@Override
	public void process(Day day) throws BadSignalException {
		sma.process(day);
		// TODO Auto-generated method stub

	}

}