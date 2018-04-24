import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
//import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.StringUtils;




public class WordCoocurence {

	  public static class TokenizerMapper
      extends Mapper<Object, Text, Text, IntWritable>{

   static enum CountersEnum { INPUT_WORDS }

   private final static IntWritable one = new IntWritable(1);
   //private Text word = new Text();

   private boolean caseSensitive;
   private Set<String> patternsToSkip = new HashSet<String>();

   private Configuration conf;
   private BufferedReader fis;
   
   @Override
   public void setup(Context context) throws IOException,
       InterruptedException {
     conf = context.getConfiguration();
     caseSensitive = conf.getBoolean("wordcount.case.sensitive", true);
     if (conf.getBoolean("wordcount.skip.patterns", false)) {
       URI[] patternsURIs = Job.getInstance(conf).getCacheFiles();
       for (URI patternsURI : patternsURIs) {
         Path patternsPath = new Path(patternsURI.getPath());
         String patternsFileName = patternsPath.getName().toString();
         parseSkipFile(patternsFileName);
       }
     }
   }

   private void parseSkipFile(String fileName) {
     try {
       fis = new BufferedReader(new FileReader(fileName));
       String pattern = null;
       while ((pattern = fis.readLine()) != null) {
         patternsToSkip.add(pattern);
       }
     } catch (IOException ioe) {
       System.err.println("Caught exception while parsing the cached file '"
           + StringUtils.stringifyException(ioe));
     }
   }
   
   
   public void map(Object key, Text value, Context context
           ) throws IOException, InterruptedException {
	   
	   String line = (caseSensitive) ?
		          value.toString() : value.toString().toLowerCase();
		          
		          line = line.replaceAll("\"","").replaceAll("\\)","").replaceAll("\\\\", "").replaceAll("\\•", "");
		          line = line.replaceAll("\\…", "").replaceAll("\\“", "").replaceAll("\\”", "").replaceAll("\\‘", "").replaceAll("\\’", "");;
		          line = line.replaceAll("tokens", "token");
		          line = line.replaceAll("currencies", "currency");
		          line = line.replaceAll("banks", "bank");
		          line = line.replaceAll("�", "");
		          line = line.replaceAll("btc", "bitcoin");
		          line = line.replaceAll("bitcoins", "bitcoin");
		          line = line.replaceAll("eth ", "ethereum ");
		          
		             String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
		             Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
		             Matcher m = p.matcher(line);
		             int i = 0;
		             while (m.find()) {
		                 line = line.replaceAll(m.group(i),"").trim();
		                 i++;
		            }
		             
		             String username = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
		             Pattern s = Pattern.compile(username,Pattern.CASE_INSENSITIVE);
		             Matcher m1 = s.matcher(line);
		             while (m1.find()) {
		            	 line = line.replaceAll(m1.group(),"").trim();
		            	 
		             }
		             
		             
		             
		         /*  
		            String username = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
		             Pattern s = Pattern.compile(username,Pattern.CASE_INSENSITIVE);
		              Matcher m1 = s.matcher(line);
		              int j = 0;
		              while (m1.find()) {
		                  line = line.replaceAll(m1.group(j),"").trim();
		                  j++;
		              }
		              
		           */
		             String secondword=new String();
		             String firstword=new String();
		             
		              line = line.replaceAll("\\p{Punct}", "").replaceAll("\\d","");
		              line = line.replaceAll("bitcoinss", "bitcoin");
		              line = line.replaceAll("bethereum", "ethereum");
		              
		         /*     Iterator<String> iter = patternsToSkip.iterator();
		              while(iter.hasNext()){
		            	 String word_remove = (String) iter.next();
		            	 line = line.replaceAll(word_remove, "");
		              }
		              
		       */
		              
		              StringTokenizer itr = new StringTokenizer(line);
		              
		              
		              if (itr.hasMoreTokens()) {
		                  firstword = itr.nextToken();
		                  secondword = firstword;
		              }
 
		              firstword = secondword;
		              
		              while (itr.hasMoreTokens()) {
		            	 
		            	 
		            	  if(!patternsToSkip.contains(firstword) && firstword.length()>2){
		                      secondword = itr.nextToken();
		                      
		                      if(!patternsToSkip.contains(secondword)&& secondword.length()>2){
		                    	 if(!firstword.equals(secondword)){
		                      firstword = firstword+"-"+secondword;
                              
		                      Text outputKey = new Text(firstword.toLowerCase());
		                      context.write(outputKey, one);
		                      Counter counter = context.getCounter(CountersEnum.class.getName(),
		                      CountersEnum.INPUT_WORDS.toString());
		                      counter.increment(1);
		                      firstword = secondword;
		                    	 }
		                      }
		                     		                    	  
		            	  }
		            	  else
		            		   firstword = itr.nextToken();
		            	  
		              }   
	   
   }
	  }
	
	
	
	
   public static class IntSumReducer
   extends Reducer<Text,IntWritable,Text,IntWritable> {
	   
	    private IntWritable result = new IntWritable();

	    public void reduce(Text key, Iterable<IntWritable> values,
	                       Context context
	                       ) throws IOException, InterruptedException {
	      int sum = 0;
	      for (IntWritable val : values) {
	        sum += val.get();
	      }
	      result.set(sum);
	      context.write(key, result);
	    }
	   
	   
   }
	
	
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
	    GenericOptionsParser optionParser = new GenericOptionsParser(conf, args);
	    String[] remainingArgs = optionParser.getRemainingArgs();
	    if ((remainingArgs.length != 2) && (remainingArgs.length != 4)) {
	      System.err.println("Usage: wordcount <in> <out> [-skip skipPatternFile]");
	      System.exit(2);
	    }
	    
	    Job job = Job.getInstance(conf, "word count");
	    job.setJarByClass(WordCoocurence.class);
	    job.setMapperClass(TokenizerMapper.class);
	    job.setCombinerClass(IntSumReducer.class);
	    job.setReducerClass(IntSumReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);

	    List<String> otherArgs = new ArrayList<String>();
	    for (int i=0; i < remainingArgs.length; ++i) {
	      if ("-skip".equals(remainingArgs[i])) {
	        job.addCacheFile(new Path(remainingArgs[++i]).toUri());
	        job.getConfiguration().setBoolean("wordcount.skip.patterns", true);
	      } else {
	        otherArgs.add(remainingArgs[i]);
	      }
	    }
	    FileInputFormat.addInputPath(job, new Path(otherArgs.get(0)));
	    FileOutputFormat.setOutputPath(job, new Path(otherArgs.get(1)));

	    System.exit(job.waitForCompletion(true) ? 0 : 1);

	}

}
