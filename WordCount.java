import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCount {

    public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();
        private Set<String> stopWords = new HashSet<>();
        private boolean hasStopWords = false;

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            String stopWordsFilePath = conf.get("stopwords.filepath", "");
            if (!stopWordsFilePath.isEmpty()) {
                try (BufferedReader br = new BufferedReader(new FileReader(stopWordsFilePath))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        stopWords.add(line.trim().toLowerCase());
                    }
                }
                hasStopWords = !stopWords.isEmpty();
            }
        }

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String[] tokens = value.toString().split("\\s+");
            for (String token : tokens) {
                String cleanedWord = token.replaceAll("[^a-zA-Z]", "").toLowerCase();
                if (!cleanedWord.isEmpty() && (!hasStopWords || !stopWords.contains(cleanedWord))) {
                    word.set(cleanedWord);
                    context.write(word, one);
                }
            }
        }
    }

    public static class IntSumReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        private Map<String, Integer> wordCountMap = new HashMap<>();

        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            wordCountMap.put(key.toString(), sum);
        }

        @Override
        protected void cleanup(Context context) throws IOException, InterruptedException {
            PriorityQueue<Map.Entry<String, Integer>> topWords = new PriorityQueue<>(
                    (a, b) -> b.getValue().equals(a.getValue()) ? a.getKey().compareTo(b.getKey())
                            : b.getValue() - a.getValue());

            for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
                topWords.offer(entry);
            }

            List<Map.Entry<String, Integer>> sortedTopWords = new ArrayList<>();
            int count = 0;
            while (!topWords.isEmpty() && count < 25) {
                sortedTopWords.add(topWords.poll());
                count++;
            }

            for (Map.Entry<String, Integer> entry : sortedTopWords) {
                context.write(new Text(entry.getKey()), new IntWritable(entry.getValue()));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: WordCount <input path> <output path> [stopwords file path]");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        if (args.length == 3) {
            conf.set("stopwords.filepath", args[2]);
        }

        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
