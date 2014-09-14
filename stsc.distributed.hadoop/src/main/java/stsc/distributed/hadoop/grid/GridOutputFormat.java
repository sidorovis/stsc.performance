package stsc.distributed.hadoop.grid;

import java.io.IOException;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;

import stsc.distributed.hadoop.types.SimulatorSettingsWritable;
import stsc.distributed.hadoop.types.StatisticsWritable;

public class GridOutputFormat extends OutputFormat<SimulatorSettingsWritable, StatisticsWritable> {

	public static final Path OUT_PATH = new Path("./test_data/");

	@Override
	public RecordWriter<SimulatorSettingsWritable, StatisticsWritable> getRecordWriter(TaskAttemptContext context) throws IOException, InterruptedException {
		final FileSystem hdfs = FileSystem.get(context.getConfiguration());
		return new GridRecordWriter(hdfs, OUT_PATH);
	}

	@Override
	public void checkOutputSpecs(JobContext context) throws IOException, InterruptedException {
	}

	@Override
	public OutputCommitter getOutputCommitter(TaskAttemptContext context) throws IOException, InterruptedException {
		return new FileOutputCommitter(OUT_PATH, context);
	}

}