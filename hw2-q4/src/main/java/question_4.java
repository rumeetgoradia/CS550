import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class question_4 extends Configured implements Tool {

	private static final int NUM_ITERATIONS = 20;
	private static final int NUM_CLUSTERS = 10;
	private static final int NUM_DIMENSIONS = 58;

	private static List<Double[]> prevCentroids = new ArrayList<Double[]>(NUM_CLUSTERS);
	private static List<Double[]> currCentroids = new ArrayList<Double[]>(NUM_CLUSTERS);
	private static List<Double> clusterCosts = new ArrayList<Double>(NUM_CLUSTERS);
	private static List<Double> iterationCosts = new ArrayList<Double>(NUM_ITERATIONS);

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new question_4(), args);
		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("3 arguments required: initialCentroidsPath inputDataPath outputDirectoryPath");
			return -1;
		}

		String initialCentroidsPath = args[0];
		String inputPath = args[1];
		String outputDir = args[2];

		File centroidFile = new File(initialCentroidsPath);
		Scanner scanner = new Scanner(centroidFile);
		while (scanner.hasNextLine()) {
			Double[] centroid = parseDimensions(scanner.nextLine());
			prevCentroids.add(centroid);
		}
		scanner.close();

		for (int iteration = 1; iteration <= NUM_ITERATIONS; ++iteration) {
			if (!runJob(inputPath, outputDir, iteration)) {
				System.err.println("Iteration " + iteration + " failed.");
				return -1;
			}

			iterationCosts.add(clusterCosts.stream().mapToDouble(d -> d).sum());
			clusterCosts.clear();

			prevCentroids = new ArrayList<Double[]>(currCentroids);
			currCentroids.clear();
		}

		try {
			String fileName = outputDir + File.separator + "costs.txt";
			File costsFile = new File(fileName);
			costsFile.createNewFile();
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			for (int i = 1; i <= iterationCosts.size(); ++i) {
				writer.write(i + "\t" + iterationCosts.get(i - 1) + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public boolean runJob(String inputPath, String outputPrefix, int iteration)
			throws IOException, ClassNotFoundException, InterruptedException {
		Configuration configuration = new Configuration();

		Job job = new Job(configuration, "Question4 Iteration " + iteration);
		job.setJarByClass(question_4.class);

		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPrefix + File.separator + "output" + iteration));

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(Q4Map.class);
		job.setReducerClass(Q4Reduce.class);

		return job.waitForCompletion(true);
	}

	public static class Q4Map extends Mapper<LongWritable, Text, Text, Text> {
		private Text centroid = new Text();
		private Text dataPoint = new Text();

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			Double[] dimensions = parseDimensions(value.toString());
			Double minDistance = Double.MAX_VALUE;
			String minDistanceCentroid = "";

			for (int i = 0; i < prevCentroids.size(); ++i) {
				Double distance = 0.0;
				Double[] centroidDimensions = prevCentroids.get(i);
				for (int j = 0; j < Math.min(dimensions.length, centroidDimensions.length); ++j) {
					distance += getDistance(dimensions[j], centroidDimensions[j]);
				}
				if (distance < minDistance) {
					minDistance = distance;
					minDistanceCentroid = joinDimensions(centroidDimensions);
				}
			}

			centroid.set(minDistanceCentroid);
			dataPoint.set(value);
			context.write(centroid, dataPoint);

		}

	}

	public static class Q4Reduce extends Reducer<Text, Text, Text, Text> {
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			Double[] newCentroidDimensions = new Double[NUM_DIMENSIONS];

			List<Double[]> allValueDimensions = new ArrayList<Double[]>();
			for (Text value : values) {
				Double[] valueDimensions = parseDimensions(value.toString());
				allValueDimensions.add(valueDimensions);
				for (int i = 0; i < Math.min(valueDimensions.length, newCentroidDimensions.length); ++i) {
					if (newCentroidDimensions[i] == null) {
						newCentroidDimensions[i] = 0.0;
					}
					newCentroidDimensions[i] += valueDimensions[i];
				}
			}

			for (int i = 0; i < newCentroidDimensions.length; ++i) {
				newCentroidDimensions[i] /= allValueDimensions.size();
			}

			currCentroids.add(newCentroidDimensions);
			String newCentroid = joinDimensions(newCentroidDimensions);
			context.write(new Text(newCentroid), new Text(""));

			double clusterCost = 0;
			Double[] prevCentroidDimensions = parseDimensions(key.toString());
			for (int i = 0; i < allValueDimensions.size(); ++i) {
				Double[] valueDimensions = allValueDimensions.get(i);
				double cost = 0;
				for (int j = 0; j < Math.min(valueDimensions.length, prevCentroidDimensions.length); ++j) {
					cost += getDistance(valueDimensions[j], prevCentroidDimensions[j]);
				}
				clusterCost += cost;
			}

			clusterCosts.add(clusterCost);

		}

	}

	private static Double[] parseDimensions(String data) {
		return Arrays.stream(data.split(" ")).map(Double::parseDouble).toArray(Double[]::new);
	}

	private static String joinDimensions(Double[] newCentroidDimensions) {
		String joinedDimensions = "";
		for (int i = 0; i < newCentroidDimensions.length; ++i) {
			if (i != 0) {
				joinedDimensions += " ";
			}
			joinedDimensions += "" + newCentroidDimensions[i];
		}

		return joinedDimensions;
	}

	private static double getDistance(double x, double y) {
		return Math.pow(x - y, 2);
	}

}
