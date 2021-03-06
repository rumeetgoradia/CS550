import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Question2 extends Configured implements Tool {

	private static Map<ArrayList<String>, Integer> prevItemSetSupports = new HashMap<ArrayList<String>, Integer>();
	private static Map<ArrayList<String>, Integer> currItemSetSupports = new HashMap<ArrayList<String>, Integer>();

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new Question2(), args);
		System.exit(res);
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("3 arguments required: inputPath outputDir numPasses");
			return -1;
		}

		final int MIN_SUPPORT = 100;

		String inputPath = args[0];
		String outputDir = args[1];
		int numPasses = Integer.parseInt(args[2]);

		for (int pass = 1; pass <= numPasses; ++pass) {
			if (!runJob(inputPath, outputDir, pass, MIN_SUPPORT)) {
				System.err.println("Pass " + pass + " failed.");
				return -1;
			}
			prevItemSetSupports = new HashMap<ArrayList<String>, Integer>(currItemSetSupports);
			currItemSetSupports.clear();
		}

		return 0;
	}

	public boolean runJob(String inputPath, String outputPrefix, int pass, int minSupport)
			throws IOException, ClassNotFoundException, InterruptedException {
		Configuration configuration = new Configuration();
		configuration.setInt("pass", pass);
		configuration.setInt("minSupport", minSupport);

		Job job = new Job(configuration, "Question2 Pass " + pass);
		job.setJarByClass(Question2.class);

		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileOutputFormat.setOutputPath(job, new Path(outputPrefix + File.separator + "output" + pass));

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Q2Map.class);
		job.setReducerClass(Q2Reduce.class);

		return job.waitForCompletion(true);
	}

	public static class Q2Map extends Mapper<LongWritable, Text, Text, IntWritable> {
		private final IntWritable ONE = new IntWritable(1);
		private Text word = new Text();
		private ArrayList<ArrayList<String>> candidateItemSets = new ArrayList<ArrayList<String>>();

		@Override
		public void setup(Context context) {
			int pass = context.getConfiguration().getInt("pass", 2);
			candidateItemSets.clear();
			if (pass > 1) {
				Set<String> candidateItems = prevItemSetSupports.keySet().stream().flatMap(List::stream)
						.collect(Collectors.toSet());
				ArrayList<String> candidateItemsList = new ArrayList<String>(candidateItems);
				String[] temp = new String[pass];
				generateCandidateItemSets(candidateItemsList, 0, temp, 0, pass);
			}
		}

		public void generateCandidateItemSets(ArrayList<String> items, int itemsIndex, String[] temp, int tempIndex,
				int size) {
			if (items.size() < 1) {
				return;
			}

			if (tempIndex == size - 1) {
				ArrayList<String> tempPresenceCheck = new ArrayList<String>(Arrays.asList(temp));
				tempPresenceCheck.remove(tempPresenceCheck.size() - 1);
				Collections.sort(tempPresenceCheck);
				if (!prevItemSetSupports.containsKey(tempPresenceCheck)) {
					return;
				}
			} else if (tempIndex == size) {
				ArrayList<String> candidateItemSet = new ArrayList<String>(Arrays.asList(temp));
				Collections.sort(candidateItemSet);
				this.candidateItemSets.add(candidateItemSet);
				return;
			}

			if (itemsIndex >= items.size()) {
				return;
			}

			temp[tempIndex] = items.get(itemsIndex);
			generateCandidateItemSets(items, itemsIndex + 1, temp, tempIndex + 1, size);
			generateCandidateItemSets(items, itemsIndex + 1, temp, tempIndex, size);
		}

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			int pass = context.getConfiguration().getInt("pass", 1);

			if (pass == 1) {
				String[] items = value.toString().trim().split(" ");
				for (String item : items) {
					word.set(item);
					context.write(word, ONE);
				}
			} else {
				ArrayList<ArrayList<String>> matchingItemSets = findItemSetMatches(
						new ArrayList<String>(Arrays.asList(value.toString().trim().split(" "))));
				for (ArrayList<String> itemSet : matchingItemSets) {
					Collections.sort(itemSet);
					String keyToWrite = "";
					for (int i = 0; i < itemSet.size(); ++i) {
						if (i != 0) {
							keyToWrite += ",";
						}
						keyToWrite += itemSet.get(i);
					}
					word.set(keyToWrite);
					context.write(word, ONE);
				}
			}

		}

		public ArrayList<ArrayList<String>> findItemSetMatches(ArrayList<String> items) {
			ArrayList<ArrayList<String>> matches = new ArrayList<ArrayList<String>>();
			for (ArrayList<String> itemSet : this.candidateItemSets) {
				if (items.containsAll(itemSet)) {
					matches.add(itemSet);
				}
			}
			return matches;
		}
	}

	public static class Q2Reduce extends Reducer<Text, IntWritable, Text, Text> {
		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context)
				throws IOException, InterruptedException {

			Integer minSupport = Integer.parseInt(context.getConfiguration().get("minSupport"));
			Integer pass = Integer.parseInt(context.getConfiguration().get("pass"));

			int support = 0;
			for (IntWritable value : values) {
				support += value.get();
			}

			if (support >= minSupport) {
				ArrayList<String> items = new ArrayList<String>(Arrays.asList(key.toString().split(",")));
				Collections.sort(items);
				currItemSetSupports.put(items, support);
				if (!prevItemSetSupports.isEmpty()) {
					for (String item : items) {
						ArrayList<String> clonedItems = new ArrayList<String>(items);
						clonedItems.remove(item);
						Integer prevSupport = prevItemSetSupports.get(clonedItems);
						if (prevSupport != null) {
							String rule = "";
							for (int i = 0; i < clonedItems.size(); ++i) {
								if (i != 0) {
									rule += ",";
								}
								rule += clonedItems.get(i);
							}
							rule += "->" + item;
							String confidence = new DecimalFormat("###.##%").format(((double) support) / prevSupport);
							context.write(new Text(rule), new Text(confidence));
						}
					}
				} else {
					context.write(new Text(key), new Text("" + support));
				}
			}
		}

	}

}
