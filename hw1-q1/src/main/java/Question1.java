import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Question1 extends Configured implements Tool {

	public static void main(String[] args) throws Exception {
		System.out.println(Arrays.toString(args));
		int res = ToolRunner.run(new Configuration(), new Question1(), args);
		System.exit(res);

	}

	public int run(String[] args) throws Exception {
		System.out.println(Arrays.toString(args));
		Job job = new Job(getConf(), "Question1");
		job.setJarByClass(Question1.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Question1Writable.class);

		job.setMapperClass(Q1Map.class);
		job.setReducerClass(Q1Reduce.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);

		return 0;
	}

	public static class Question1Writable implements Writable {
		private Integer user;
		private Integer friendshipDegree;

		public static final int NO_FRIENDS = -1;
		public static final int ALREADY_FRIENDS = 1;
		public static final int HAVE_MUTUAL_FRIEND = 2;

		public Question1Writable(Integer user, Integer friendshipDegree) {
			this.user = user;
			this.friendshipDegree = friendshipDegree;
		}

		public Question1Writable() {
			this.user = 0;
			this.friendshipDegree = 0;
		}

		public void readFields(DataInput arg0) throws IOException {
			this.user = arg0.readInt();
			this.friendshipDegree = arg0.readInt();
		}

		public void write(DataOutput arg0) throws IOException {
			arg0.writeInt(this.user);
			arg0.writeInt(this.friendshipDegree);
		}

		public Integer getUser() {
			return this.user;
		}

		public void setUser(Integer user) {
			this.user = user;
		}

		public Integer getFriendshipDegree() {
			return this.friendshipDegree;
		}

		public void setFriendshipDegree(Integer friendshipDegree) {
			this.friendshipDegree = friendshipDegree;
		}

	}

	public static class Q1Map extends Mapper<LongWritable, Text, IntWritable, Question1Writable> {

		@Override
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
			String[] dataLine = value.toString().split("\t");
			Integer user = Integer.parseInt(dataLine[0]);
			if (dataLine.length == 2) {
				String[] friends = dataLine[1].split(",");
				for (int i = 0; i < friends.length; ++i) {
					Integer friend1 = Integer.parseInt(friends[i]);
					context.write(new IntWritable(user),
							new Question1Writable(friend1, Question1Writable.ALREADY_FRIENDS));
					for (int j = i + 1; j < friends.length; ++j) {
						Integer friend2 = Integer.parseInt(friends[j]);
						context.write(new IntWritable(friend1),
								new Question1Writable(friend2, Question1Writable.HAVE_MUTUAL_FRIEND));
						context.write(new IntWritable(friend2),
								new Question1Writable(friend1, Question1Writable.HAVE_MUTUAL_FRIEND));
					}
				}
			} else {
				context.write(new IntWritable(user),
						new Question1Writable(Question1Writable.NO_FRIENDS, Question1Writable.NO_FRIENDS));
			}

		}
	}

	public static class Q1Reduce extends Reducer<IntWritable, Question1Writable, IntWritable, Text> {
		@Override
		public void reduce(IntWritable key, Iterable<Question1Writable> values, Context context)
				throws IOException, InterruptedException {
			final int FIRST_DEGREE_CONNECTION = -1;
			HashMap<Integer, Integer> secondDegreeConnections = new HashMap<Integer, Integer>();

			for (Question1Writable connection : values) {
				Integer user = connection.getUser();
				if (connection.getFriendshipDegree() == Question1Writable.NO_FRIENDS) {
					context.write(key, new Text(""));
					return;
				}
				if (connection.getFriendshipDegree() != Question1Writable.ALREADY_FRIENDS) {
					if (secondDegreeConnections.containsKey(user)) {
						Integer numConnections = secondDegreeConnections.get(user);
						if (numConnections != FIRST_DEGREE_CONNECTION) {
							secondDegreeConnections.put(user, numConnections + 1);
						}
					} else {
						secondDegreeConnections.put(user, 1);
					}
				} else {
					secondDegreeConnections.put(user, FIRST_DEGREE_CONNECTION);
				}
			}

			List<Entry<Integer, Integer>> sortedList = new ArrayList<Entry<Integer, Integer>>(
					secondDegreeConnections.entrySet());
			Collections.sort(sortedList, new Comparator<Entry<Integer, Integer>>() {
				public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
					if (o1.getValue() == o2.getValue()) {
						return o1.getKey().compareTo(o2.getKey());
					}
					return o2.getValue().compareTo(o1.getValue());
				}
			});

			final int NUM_RECOMMENDATIONS = 10;
			int count = 0;
			String recommendation = "";
			while (count < NUM_RECOMMENDATIONS && count < sortedList.size()) {
				if (sortedList.get(count).getValue() == FIRST_DEGREE_CONNECTION) {
					sortedList.remove(count);
					continue;
				}
				if (count > 0) {
					recommendation += ",";
				}
				recommendation += sortedList.get(count).getKey();
				++count;
			}
			context.write(key, new Text(recommendation));
		}
	}

}
